const C="duo-v8";
self.addEventListener("install",e=>{self.skipWaiting()});
self.addEventListener("activate",e=>e.waitUntil(clients.claim()));
self.addEventListener("fetch",e=>{const u=new URL(e.request.url);
 if(e.request.method!=="GET"||(u.origin!==location.origin&&!["www.gstatic.com","cdn.jsdelivr.net"].includes(u.hostname)))return;
 e.respondWith(fetch(e.request).then(r=>{if(r.ok){const c=r.clone();caches.open(C).then(x=>x.put(e.request,c))}return r}).catch(()=>caches.match(e.request)))});
self.addEventListener("notificationclick",e=>{e.notification.close();e.waitUntil(clients.matchAll({type:"window"}).then(l=>l[0]?l[0].focus():clients.openWindow("./")))});
self.addEventListener("push",e=>{let j={};try{j=e.data.json()}catch(x){}const d=j.data||j,n=j.notification||{};e.waitUntil(self.registration.showNotification(d.title||n.title||"Our Life",{body:d.body||n.body||"New message",icon:"icon-192.png",badge:"icon-192.png",tag:"chat",renotify:true}))});
