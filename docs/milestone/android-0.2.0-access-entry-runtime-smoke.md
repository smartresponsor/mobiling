# Android 0.2.0 Access Entry Runtime Smoke

- Date: 2026-06-21
- Result: passed
- Package: `com.smartresponsor.mobile`
- Launcher activity: `com.smartresponsor.mobile/.MainActivity`

## Evidence

- `com.smartresponsor.mobile` starts successfully.
- `MainActivity` is displayed.
- No `FATAL EXCEPTION` for `com.smartresponsor.mobile`.
- No application crash loop was detected during the filtered logcat run.

## Observed Non-Blocking Noise

- `ziparchive: Unable to open ... base.dm`
- `GraphicsEnvironment`
- `CoreBackPreview`
- `InputManager-JNI`
- Google Play services / Binder noise

## Conclusion

The Android access-entry runtime smoke does not show an application-level crash for this launch.
