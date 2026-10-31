const CACHE_NAME = "our-life-shell-v18";
const APP_SHELL = [
  "./",
  "./index.html",
  "./chat.html",
  "./manifest.json",
  "./icon-192.png",
  "./icon-512.png",
  "./badge-72.png"
];

self.addEventListener("install", event => {
  event.waitUntil((async () => {
    const cache = await caches.open(CACHE_NAME);
    await cache.addAll(APP_SHELL);
    await self.skipWaiting();
  })());
});

self.addEventListener("activate", event => {
  event.waitUntil((async () => {
    const keys = await caches.keys();
    await Promise.all(keys.filter(key => key !== CACHE_NAME).map(key => caches.delete(key)));
    await self.clients.claim();
  })());
});

self.addEventListener("fetch", event => {
  const request = event.request;
  if (request.method !== "GET") return;

  const url = new URL(request.url);
  const sameOrigin = url.origin === self.location.origin;
  const firebaseStatic = url.hostname === "www.gstatic.com" || url.hostname === "cdn.jsdelivr.net";

  // Never cache Firebase data/auth endpoints or other third-party requests.
  if (!sameOrigin && !firebaseStatic) return;

  if (request.mode === "navigate") {
    event.respondWith((async () => {
      const cached = await caches.match("./index.html");
      const refresh = fetch(request).then(async fresh => {
        if (fresh.ok) {
          const cache = await caches.open(CACHE_NAME);
          await cache.put("./index.html", fresh.clone());
        }
        return fresh;
      });
      if (cached) {
        event.waitUntil(refresh.catch(() => {}));
        return cached;
      }
      try { return await refresh; }
      catch { return new Response("Please connect to the internet and open Our Life again.", {
        status: 503, headers: { "Content-Type": "text/plain; charset=utf-8" }
      }); }
    })());
    return;
  }

  event.respondWith((async () => {
    const cached = await caches.match(request);
    if (cached) return cached;
    try {
      const fresh = await fetch(request);
      if (fresh.ok && (sameOrigin || firebaseStatic)) {
        const cache = await caches.open(CACHE_NAME);
        cache.put(request, fresh.clone());
      }
      return fresh;
    } catch {
      return new Response("You are offline. Open Our Life again when a connection is available.", {
        status: 503,
        headers: { "Content-Type": "text/plain; charset=utf-8" }
      });
    }
  })());
});

self.addEventListener("notificationclick", event => {
  event.notification.close();
  const target = event.notification.data && event.notification.data.url
    ? new URL(event.notification.data.url, self.location.href).href
    : new URL("./", self.location.href).href;
  event.waitUntil(self.clients.matchAll({ type: "window", includeUncontrolled: true }).then(async list => {
    const sameApp = list.find(client => new URL(client.url).origin === self.location.origin);
    if (sameApp) {
      try { await sameApp.navigate(target); } catch {}
      return sameApp.focus();
    }
    return self.clients.openWindow(target);
  }));
});

self.addEventListener("push", event => {
  let data = {};
  try { data = event.data.json(); } catch {}
  const payload = data.data || data;
  const note = data.notification || {};
  const title = payload.title || note.title || "Our Life";
  const icon = /^(saimoon|nadiya)$/i.test(title) ? title.toLowerCase() + ".png" : "icon-192.png";
  event.waitUntil(self.registration.showNotification(title, {
    body: payload.body || note.body || "New message",
    icon,
    badge: "badge-72.png",
    tag: "chat",
    renotify: true,
    vibrate: [80, 40, 80],
    timestamp: Date.now()
  }));
});
