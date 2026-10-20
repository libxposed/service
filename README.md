# libxposed Service

[![API](https://img.shields.io/badge/API-102-brightgreen)](https://github.com/libxposed/api)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.libxposed/service?color=blue)](https://central.sonatype.com/artifact/io.github.libxposed/service)
[![Android Min SDK](https://img.shields.io/badge/minSdk-26-orange)](https://developer.android.com/about/versions/oreo)
[![License](https://img.shields.io/github/license/libxposed/service)](LICENSE)

Modern Xposed Service — communication interface between Xposed framework and module app.

## Integration

### For Module Developers

```kotlin
dependencies {
    implementation("io.github.libxposed:service:102.0.0")
}
```

### Direct Boot

The library's `XposedProvider` is Direct Boot aware, so Xposed frameworks can deliver
service binders before the user unlocks the device for the first time after a reboot.
The module app's `Application` initialization and service listener callbacks must
therefore be safe to run before user unlock. Use
`Context.createDeviceProtectedStorageContext()` for local data needed at that time,
or defer access to credential-protected storage until `UserManager.isUserUnlocked()`
returns `true`. Remote preferences and files are managed by the Xposed framework;
their availability before unlock depends on the framework implementation.

See [Android's Direct Boot guide](https://developer.android.com/privacy-and-security/direct-boot)
for storage and lifecycle details.

### For Framework Developers

```kotlin
dependencies {
    implementation("io.github.libxposed:interface:102.0.0")
}
```

## Documentation

- [Javadoc](https://libxposed.github.io/service/) — API reference

## Related Projects

- [libxposed/api](https://github.com/libxposed/api) — Modern Xposed Module API
- [libxposed/helper](https://github.com/libxposed/helper) — Friendly development kit library
- [libxposed/example](https://github.com/libxposed/example) — Example module using the modern Xposed API

## License

This project is licensed under the [Apache License 2.0](LICENSE).
