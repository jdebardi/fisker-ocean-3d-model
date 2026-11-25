# Android

Inspect the actual application first: Views/Compose, Flutter, React Native or a web-based UI. Reuse an existing compatible renderer. For a native implementation, Filament/gltfio is one available GLB path; [ANDROID_FILAMENT.md](ANDROID_FILAMENT.md) explains the adapter. Match all Filament Android artifacts to the same selected version.

- Bundle the model and JSON in application assets; load once per owned scene and retain named entity/material maps.
- Implement TwinSceneSink for VehicleTwinController and OpeningSceneSink for VehicleOpeningController from `../../runtime/kotlin/`. Solar texture generation remains a separate renderer binding.
- Keep render-thread calls, Choreographer/frame scheduling, surface lifecycle and disposal consistent with the application's architecture. Preserve cached rest transforms; query transient engine instance handles as required by its pinned API.
- Bind real project signals and preferences. Compose/View sliders used in a development screen should not become mandatory dashboard controls.
- Use a TextureView/SurfaceView/compositing strategy suitable for the existing UI; verify transparent rendering if the car overlays another background.
- Validate all names, lamp combinations, fixed glass, window frames, openings, handles and shared solar atlas against the web reference.

The original target was an Android dashboard on Orange Pi 5 Pro and a board described by its owner as ROCK 5C Pro. These devices are not benchmarked by this release. [ANDROID_HARDWARE.md](ANDROID_HARDWARE.md) gives environment/ABI/driver checks and starting rendering settings; confirm the actual board and installed Android image. Start with a small OpenGL ES viewport and lightweight geometry, then profile. Other Android phones/tablets/boards also require their own measurements.

Primary references: [Filament gltfio](https://google.github.io/filament/dup/gltfio.html), [official Android ModelViewer](https://github.com/google/filament/blob/main/android/filament-utils-android/src/main/java/com/google/android/filament/utils/ModelViewer.kt).
