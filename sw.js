const C="duo-v3";
self.addEventListener("install",e=>{self.skipWaiting()});
self.addEventListener("activate",e=>e.waitUntil(clients.claim()));
self.addEventListener("fetch",e=>{const u=new URL(e.request.url);
 if(e.request.method!=="GET"||(u.origin!==location.origin&&!["www.gstatic.com","cdn.jsdelivr.net"].includes(u.hostname)))return;
 e.respondWith(fetch(e.request).then(r=>{if(r.ok||r.type==="opaque"){const c=r.clone();caches.open(C).then(x=>x.put(e.request,c))}return r}).catch(()=>caches.match(e.request)))});
