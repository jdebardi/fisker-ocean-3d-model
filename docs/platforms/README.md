# Platform guides

Read [the common integration contract](../INTEGRATION.md) and [state mapping](../STATE.md) first. Engine import behaviour and APIs change; pin a supported version and verify against its official documentation.

- [Web / desktop web](WEB.md): Three.js reference, React integration and Electron/Tauri packaging.
- [Android](ANDROID.md): native Filament, existing app frameworks and reference Kotlin controllers.
- [Detailed Filament binding](ANDROID_FILAMENT.md) and [Android board checks](ANDROID_HARDWARE.md).
- [Unity](UNITY.md): glTF import, transform basis, materials and C# state adapter.
- [Unreal](UNREAL.md): scene-preserving import, centimetres/Z-up, dynamic materials and Blueprint/C++.
- [Apple](APPLE.md): native conversion/import, material validation and Swift state adapter.
- [Flutter / React Native](CROSS_PLATFORM.md): existing compatible renderer, native component or web reference.
- [Qt Quick 3D](QT.md): GLB loading, version-dependent name access and embedded profiling.

The browser viewer runs as supplied. The Kotlin controllers are renderer-independent logic references. Other guides describe implementation work; this release does not contain production-ready adapters or claim device validation for those platforms.
