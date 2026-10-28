# Deutsch Duo (Firebase Spark-friendly)

## Files to upload to the root of your GitHub Pages repository
- `index.html`
- `saimoon.png` (your photo)
- `nadiya.png` (Nadiya's photo)

Images are loaded as local repository files; Firebase Storage is not used.

## Firebase setup
1. Firebase Authentication: keep Email/Password enabled. Use the two existing accounts; this app has sign-in only, not account creation.
2. Firestore Database: open Rules, paste the complete `firestore.rules` file, then Publish.
3. Keep the project on Spark. Do not upgrade to Blaze if you want to avoid billing.
4. In Settings, each account should save the other person's UID. The UIDs are already allowlisted in the rules file.

## Features
- Mobile-friendly pages; Chat opens in a dedicated full-screen view.
- Couple chat with swipe-right or Reply button, and delete-own-message.
- Daily meals, shower, five prayers, Sunday–Thursday college attendance, and custom daily activities.
- German A1 dated checklist for 29 Oct–31 Dec 2026. Future tasks are locked until the date; KKS appears Sunday/Monday and Tuesday only in heavy weeks 5 and 8.
- Vocabulary categories, noun article + plural, adjective opposite, examples, and a 30/40/50-word test from saved vocabulary.
- Photos use `saimoon.png` and `nadiya.png` in the same folder as `index.html`.

## Security note
Only the two specified Firebase UIDs are allowed. Daily logs are readable by the pair; each person writes their own. Vocabulary and German progress are private to each account. Never upload Firebase service-account private keys or passwords to GitHub.

The files are a static starter and should be tested on the live Firebase project after publishing the rules. If a permission error remains, check that the rules were published in the correct Firebase project and both accounts' UIDs match exactly.
