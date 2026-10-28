# Deutsch Duo Starter — GitHub Pages + Firebase

This is a single-file static web app starter configured with the Firebase web config supplied in chat.

## Included
- `index.html`: responsive Dark Rose & Plum / Light Pink & Cream / System themes
- Firebase Email/Password authentication
- Shared one-to-one chat UI
- Shared daily checklist: breakfast, lunch, dinner, shower, Sunday–Thursday college, five prayers
- German A1 9-week curriculum summary and weekly grammar progress
- Vocabulary notebook
- Private profile-photo upload/read using Firebase Storage SDK
- `firestore.rules` and `storage.rules` templates with two-member UID allowlist

## Important before use
The app is a starter, not yet a production-audited application. Shared chat, shared logs and profile photo access will not work until you replace the UID placeholders in BOTH rules files with the two actual Firebase Authentication UIDs and publish the rules. Never publish rules that allow everyone to read/write.

## Setup
1. In Firebase Console → Authentication → Sign-in method, enable Email/Password.
2. Create `index.html` in the root of the existing `deutsch-duo` GitHub repository and paste this file. If the existing site already contains code, download/back it up before replacing anything.
3. Add `firestore.rules` and `storage.rules` files to the repo for reference.
4. Commit and push `index.html` to the branch/folder used by GitHub Pages. This app uses browser ES modules from Firebase CDN and does not require a build step.
5. Open the published site and create your account, then Nadiya creates hers. Use strong unique passwords.
6. Firebase Console → Authentication → Users: copy both UIDs.
7. Replace `REPLACE_WITH_YOUR_UID` and `REPLACE_WITH_NADIYA_UID` in `firestore.rules` with the exact UIDs. Do the same in `storage.rules`.
8. Firebase Console → Firestore Database → Rules: paste/publish the contents of `firestore.rules`.
9. Firebase Console → Storage → Rules: paste/publish the contents of `storage.rules`.
10. Sign in again. In Settings, paste the partner's UID and save. Do this on both accounts (each person enters the other's UID).
11. Test chat and daily logs with both accounts, and verify a third test account cannot read them.

## Firebase config
The Firebase web config is intended for client-side use. It is not an admin credential. Security must be enforced with Firebase Authentication, Firestore Rules, Storage Rules, and appropriate API key restrictions. Never add service-account JSON or private keys to the repo.

## Storage photo CORS
The app reads image bytes through the authenticated Firebase Storage SDK instead of placing a permanent download-token URL in the public UI. Some buckets require CORS configuration for browser reads. If photos fail while other features work, configure the bucket CORS to allow your GitHub Pages origin (e.g. `https://YOUR_GITHUB_USERNAME.github.io`) for GET/HEAD/OPTIONS and reload.

## GitHub Pages
Settings → Pages: select the correct deployment branch and folder (commonly `main` and `/ (root)`). Wait for the deployment URL to be published.

## Current limitations / next improvements
- The existing repository was not directly accessed or edited from this chat. This is a standalone starter to add to your repository.
- The German curriculum here preserves the weekly topics and weekday structure from the supplied plan summary, but it does not yet implement every timed task as individual daily checkboxes.
- Password reset, account invitation, message deletion, notifications, full audit logs, and comprehensive automated tests are not included.
- Review Firebase billing/plan requirements for Storage and any future messaging/notification services.
- Do not upload private photos unless the other person consents.


## Profile photos without Firebase Storage (free setup)

This version of `index.html` does not use Firebase Storage. Upload these two image files to the **root** of the same GitHub Pages repository, next to `index.html`, using exactly these filenames:

- `saimoon.png` — Saimoon's photo
- `nadiya.png` — Nadiya's photo

The app chooses the photo by the known Firebase Authentication UID. Filenames are case-sensitive. If the images are missing, a heart placeholder appears.
