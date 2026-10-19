# cordova-plugin-phonenumber

Our own (Kaufmann Agency / NHG) Android-only Cordova plugin that reads the phone's own number from the SIM.
Not a fork. Used by the porter app (`pake-transporter`, `fi.kaufmann.paketti.kuljettaja`): the driver phones
are shared, so on every start the app reports "this user is now reachable at this number" to the backend
(`POST /users/me/currentPhone/`), so the number shown for a driver (e.g. with a call link in the porter
app's group view) follows whoever is using the phone.

## JavaScript API

Exposed as `window.cordovaPhonenumber`:

```js
cordovaPhonenumber.number(info => console.log(info.phone))   // also returns a Promise
// info = { phone: "0401234567" }   or { } when no number could be read
```

## How the number is found

At plugin load (`onload`, i.e. app start — not when `number()` is called):

1. `SubscriptionManager.getPhoneNumber(subId)` for each active SIM subscription; first non-empty wins.
2. Fallback: `TelephonyManager.getLine1Number()`.
3. `+358` is replaced by `0` (Finnish national format). If nothing is found, `phone` is null and the app
   skips the backend call.

Requires the `READ_PHONE_NUMBERS` / `READ_PHONE_STATE` permissions (declared in `plugin.xml`). The plugin does
**not** request them at runtime, so they must already be granted (device management or app settings); in development
`pake run porter-front --mobile` grants `READ_PHONE_NUMBERS` with `adb shell pm grant`. Many SIMs don't store
their own number at all, in which case the result is empty regardless.

## Development

- Branch: `main` (the app references the default branch, no `#branch`).
- Logs: `adb logcat -s PhonenumberPlugin`, or `pake log porter-front --mobile`.
- To ship a change: commit and push, then in `pake-transporter/cordova`:

  ```bash
  npm install github:KaufmannAgency/cordova-plugin-phonenumber   # moves the lock to the new commit
  cordova plugin rm cordova-plugin-phonenumber && cordova plugin add cordova-plugin-phonenumber
  ```

  build and install with `pake build,package,run porter-front --mobile --named-env remote`, and commit the
  app's `cordova/package-lock.json`.
