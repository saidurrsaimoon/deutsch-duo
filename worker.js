const PROJECT="deutsch-duo-8f489";
const ALLOWED=["R0xCX48rkDa9VyyK7tDkyyH5qIk1","lGnuAexcJzaJLMsIjYKeR4bzyXJ3"];
const ORIGIN="https://saidurrsaimoon.github.io";
const cors={"Access-Control-Allow-Origin":ORIGIN,"Access-Control-Allow-Headers":"Authorization,Content-Type","Access-Control-Allow-Methods":"POST,OPTIONS"};
const enc=new TextEncoder(),dec=new TextDecoder();
const fromB64u=s=>Uint8Array.from(atob(s.replace(/-/g,"+").replace(/_/g,"/").padEnd(Math.ceil(s.length/4)*4,"=")),c=>c.charCodeAt(0));
const toB64u=b=>btoa(String.fromCharCode(...new Uint8Array(b))).replace(/\+/g,"-").replace(/\//g,"_").replace(/=+$/,"");

async function verify(idToken){
  const [h,p,sig]=idToken.split(".");
  const head=JSON.parse(dec.decode(fromB64u(h))),pay=JSON.parse(dec.decode(fromB64u(p)));
  const jwks=await(await fetch("https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com")).json();
  const jwk=jwks.keys.find(k=>k.kid===head.kid);if(!jwk)throw 0;
  const key=await crypto.subtle.importKey("jwk",jwk,{name:"RSASSA-PKCS1-v1_5",hash:"SHA-256"},false,["verify"]);
  const ok=await crypto.subtle.verify("RSASSA-PKCS1-v1_5",key,fromB64u(sig),enc.encode(h+"."+p));
  if(!ok||pay.aud!==PROJECT||pay.iss!=="https://securetoken.google.com/"+PROJECT||pay.exp<Date.now()/1000||!ALLOWED.includes(pay.sub))throw 0;
}

async function accessToken(env){
  const sa=JSON.parse(env.SA_JSON),now=Math.floor(Date.now()/1000);
  const head=toB64u(enc.encode(JSON.stringify({alg:"RS256",typ:"JWT"})));
  const claim=toB64u(enc.encode(JSON.stringify({iss:sa.client_email,scope:"https://www.googleapis.com/auth/firebase.messaging",aud:"https://oauth2.googleapis.com/token",iat:now,exp:now+3600})));
  const pem=sa.private_key.replace(/-----[^-]+-----/g,"").replace(/\s/g,"");
  const key=await crypto.subtle.importKey("pkcs8",Uint8Array.from(atob(pem),c=>c.charCodeAt(0)),{name:"RSASSA-PKCS1-v1_5",hash:"SHA-256"},false,["sign"]);
  const sig=toB64u(await crypto.subtle.sign("RSASSA-PKCS1-v1_5",key,enc.encode(head+"."+claim)));
  const r=await fetch("https://oauth2.googleapis.com/token",{method:"POST",headers:{"Content-Type":"application/x-www-form-urlencoded"},body:"grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion="+head+"."+claim+"."+sig});
  return(await r.json()).access_token;
}

export default{async fetch(req,env){
  if(req.method==="OPTIONS")return new Response(null,{headers:cors});
  if(req.method!=="POST")return new Response("Our Life push is running",{headers:cors});
  try{
    await verify((req.headers.get("Authorization")||"").replace("Bearer ",""));
    const{tokens,title,body}=await req.json();
    const at=await accessToken(env);
    const out=await Promise.all((tokens||[]).slice(0,3).map(async t=>{
      const r=await fetch(`https://fcm.googleapis.com/v1/projects/${PROJECT}/messages:send`,{method:"POST",headers:{Authorization:"Bearer "+at,"Content-Type":"application/json"},body:JSON.stringify({message:{token:t,data:{title:String(title||"Our Life").slice(0,60),body:String(body||"").slice(0,140)},webpush:{headers:{Urgency:"high",TTL:"86400"}}}})});
      const raw=await r.text();let detail={};try{detail=JSON.parse(raw)}catch{}
      return {status:r.status,ok:r.ok,error:r.ok?"":(detail.error?.message||raw).slice(0,240)};
    }));
    return new Response(JSON.stringify({results:out}),{headers:{...cors,"Content-Type":"application/json"}});
  }catch(e){return new Response("denied",{status:401,headers:cors})}
}};
