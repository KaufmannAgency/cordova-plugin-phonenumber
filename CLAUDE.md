# CLAUDE.md — cordova-plugin-phonenumber

Our own Android-only Cordova plugin (no upstream; 3 commits, 2024-08..10) returning the device's own SIM
phone number. Human docs: `README.md`; system overview: `../pake-scripts/CLAUDE.md`.

## Layout and behaviour

- `plugin.xml`: id `cordova-plugin-phonenumber` (v0.0.1), JS clobbers `cordovaPhonenumber`, feature
  `CordovaPhonenumber` with `onload=true`, permissions `READ_PHONE_STATE` + `READ_PHONE_NUMBERS`,
  `src/android/phonenumber.gradle` (only `jcenter()` + packaging excludes — `jcenter()` is removable).
  Its `<keywords>` still say "sso,imprivata" (copied from cordova-plugin-imprivata).
- `src/android/CordovaPhonenumber.java` (`fi.nhg.paketti.cordova.plugin`): `initialize` resolves the number
  once — `SubscriptionManager.getPhoneNumber(subId)` over active subscriptions, fallback
  `TelephonyManager.getLine1Number()` (in try/catch), `+358` → `0`. `execute("number")` returns `{phone}`.
  Log tag `PhonenumberPlugin`.
- `www/phonenumber.js`: `number(listener)` → Promise; error texts say "Imprivata" (copy-paste).
- Gotchas (unfixed, worth knowing before touching it):
  - `SubscriptionManager.getPhoneNumber` is **API 33+**, and the subscription loop is not in try/catch;
    with minSdk 24 an older device, or a null subscription list (permission missing), throws inside
    `initialize`.
  - No runtime permission request — the number is read at plugin load, so a permission granted later only
    takes effect after an app restart.

## Consumer

`pake-transporter` only (porter app, pake module `porter-front`): `cordova/package.json`
`github:KaufmannAgency/cordova-plugin-phonenumber` (default branch `main`; lock pins `ebdcd64`).
`src/index.js` calls `registerPhoneNumber()` (`src/utils/cordova.utils.js`) after authentication, which
POSTs `{mobile}` to `/users/me/currentPhone/` on porter-back. `do_run_porter-front_mobile` in
`pake-transporter/pake-tools.sh` grants `READ_PHONE_NUMBERS` via `adb shell pm grant` after install.

## Toolchain

This repo is not built on its own — it is compiled inside the consuming app's Cordova Android project.

| tool | version | where pinned / why |
|---|---|---|
| cordova CLI | 13.0.0 | `npm install -g cordova@13.0.0` in the app's `do_init_*_mobile` (`pake-tools.sh`) |
| cordova-android | 14.0.1 (pake-front), 15.1.0 (pake-transporter), 11.0.0 (pake-specialist) | app `cordova/package.json` devDependencies |
| Gradle / AGP | 8.13 / 8.7.3 (cdv-android 14), 8.14.2 / 8.10.1 (cdv-android 15) | bundled by cordova-android; plugins must use `implementation`, not `compile` (Gradle 7+) |
| JDK | 21 (porter-front) / 18 (paketti-front packaging) | `export JAVA_HOME=$(/usr/libexec/java_home -v …)` in app `pake-tools.sh`; JDK 25 is too new for this Gradle |
| Android SDK | compileSdk 35 (front) / 36 (transporter), minSdk 24 | app `config.xml` + cordova-android defaults; `ANDROID_HOME` in `~/.zshenv` |
| node | nvm default (25.8.2) or 24.13.0 | no `.nvmrc`; `nvm use` lines in app `pake-tools.sh` are commented out |

## How a consuming app picks up a change

Consumers reference this repo as an npm git dependency in their `cordova/package.json`; their
`cordova/package-lock.json` **pins the exact commit SHA**, and `cordova/plugins/` + `cordova/platforms/` are
git-ignored copies. So pushing to the branch alone changes nothing in an app build.

1. Commit and push here, on the branch the consumer references (see table above).
2. In the consumer's `cordova/` dir (node from nvm — cordova CLI 13.0.0 is installed into node 24.13.0 / 25.8.2):
   `npm install <spec-from-package.json>` (e.g. `npm install github:KaufmannAgency/<repo>#<branch>`) to move
   the lock to the new commit, then `cordova plugin rm <plugin-id> && cordova plugin add <plugin-id>` to
   refresh `cordova/plugins/` and `platforms/android`. `pake init <module> --mobile --clean` also works but
   wipes `platforms/` (re-prompts for `pakeapp.keystore`).
   - Quick iteration without pushing: `cordova plugin add /Users/joni/Github/<repo>` (local path) —
     this rewrites the spec in `package.json`/`config.xml`, so restore the github spec before committing the app.
3. Build + install a debug APK on a USB device: `pake build,package,run <module> --mobile --named-env remote`
   (`-u <user>` sets `TEST_USER`, which **bypasses Imprivata**), then `pake log <module> --mobile` (adb logcat).
4. Commit the consumer's updated `cordova/package-lock.json` (and `package.json` if the spec changed).

`<module>` is `paketti-front` (repo `pake-front`) or `porter-front` (repo `pake-transporter`); the
`do_*_mobile` functions live in each app repo's `pake-tools.sh`. There are no tests in this repo.

Here: plugin id `cordova-plugin-phonenumber`, spec `github:KaufmannAgency/cordova-plugin-phonenumber`,
module `porter-front`. Needs a physical phone with a SIM that stores its own number.
