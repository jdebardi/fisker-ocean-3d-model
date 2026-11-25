# Native Android Filament binding

Use this when the app does not already have a suitable 3D renderer. Check the exact Filament version selected by the project and use its matching `filament-android`, `gltfio-android` and `filament-utils-android` artifacts. The supplied controller is renderer-independent; this file specifies its native binding rather than claiming a compiled app integration.

## Load and bind

Filament's official `ModelViewer` accepts GLB buffers and supports SurfaceView or TextureView. It reduces setup work, but the application still needs its own scene lighting/IBL, frame scheduling and lifecycle management. For a dashboard overlaid on an existing background, choose the surface type and transparent compositing approach that fit the existing view hierarchy. A TextureView often integrates more naturally with existing UI layers; measure the cost on-device.

Initialise the chosen loader/provider, load the GLB once, wait for resources to be ready, and build lookup tables. `FilamentAsset.getFirstEntityByName(name)` resolves named nodes; scan renderable primitive material instances and group them by `MaterialInstance.getName()`. Keep a list for each material name because loader instances may not all share the same material object. Update every instance of `Paint_Body`, for example, without touching other materials.

Store entity IDs, not long-lived TransformManager or LightManager instance handles. Current APIs document those instance handles as transient. Obtain `getInstance(entity)` when issuing updates. Resolve this against the pinned library version.

## Implement TwinSceneSink

- `yawRadians`: rebuild the local transform from the saved rest translation/rotation/scale plus an absolute +Y rotation. For `VehicleYaw`, also include the controller's current ride-height offset. For `Steer_FL/FR`, preserve imported hub translations.
- `rollRadians`: apply the absolute +X roll to the saved local rest transform. Preserve the parent steering transform.
- `yOffsetFromRest`: set an absolute offset from the saved `VehicleYaw` translation; do not add it repeatedly each frame.
- `visibleGroup`: restore the selected group to unit scale, then show/hide all descendant renderables through layer masks. Using zero scale as a portable default is not a substitute for efficient runtime visibility. Never hide the common Roll/Steer parent.
- `emissiveLinear`: set the gltfio `emissiveFactor` float3 to the supplied HDR linear RGB. Where present, keep `emissiveStrength` at 1 because the controller already multiplies strength into RGB. Query the material parameter before setting a version-specific parameter.
- `paintLinear`: set `baseColorFactor` float4 to linear RGB plus alpha 1, `metallicFactor`, `roughnessFactor` and, when available, `clearCoatFactor`. The driver profile sets clearcoat to zero, roughness to at least 0.70 and paint metallic to at most 0.12. Honour the imported `KHR_materials_specular` factor of 0.12. No other material receives this update.
- `wheelAccentLinear`: change the selected named accent material's linear RGB and roughness only.
- `beamMaterial`: set transparent `baseColorFactor` to a cool white with the supplied alpha; set emission to the supplied strength. The GLB uses alpha blending and vertex alpha for edge/throw falloff. Preserve this; alpha masking would produce hard cutoffs.

A typical emissive update for a compatible gltfio material is:

```kotlin
// r/g/b are already linear HDR values produced by the controller.
materialInstance.setParameter("emissiveFactor", r, g, b)
// Only if this parameter exists in the pinned material provider:
materialInstance.setParameter("emissiveStrength", 1.0f)
```

Check parameter names/types against the pinned material provider. Do not silently ignore a missing required factor. Filament's base gltfio material declares `emissiveFactor` as float3, `emissiveStrength` as float, and `baseColorFactor` as float4 in the reviewed source.

## Glow, depth and colour

Use broad diffuse fill and a restrained key light so the silhouette is readable. Do not use a studio/HDR environment with bright point highlights, moving reflections or glossy clearcoat. The supplied assets and presets intentionally use a calm dashboard finish, even for colours whose real factory finish is glossy. Configure exposure and tone mapping before tuning bloom. Enable a modest HDR bloom only when needed; it is a post-process cost, not part of the GLB. Test white paint with all lamps off so body highlights do not look self-illuminated. Then compare running lamps against brakes at a fixed exposure.

The model's body/glass materials are opaque to keep occlusion predictable and avoid requiring an interior. Beam meshes are blended, double-sided, depth-tested and should not write depth. Front/rear lower-bumper indicators and rear reversing lights are material regions of Body_Main; legacy bumper lamp nodes are empty markers. Never hide Body_Main to switch lamps off. The roof-spoiler brake lamp is a separate physical part. GPU depth testing, not a 2D mask pasted over the view, should occlude the front spill.

The four punctual spotlights use glTF's `KHR_lights_punctual` and are initially off. They are optional. If enabled, prefer `LightManager.setIntensityCandela` when available because glTF spot intensities are expressed in candela; do not feed a candela value into an API overload expecting lumens. Match units to the pinned API. Keep spill geometry and real-light intensity calibrated so they do not double the same illumination.

## Camera and state

Read the camera position/target and orthographic height from `model-contract.json`. Recompute orthographic width from the actual car viewport aspect ratio. Do not normalise the entire asset with optional beams included in its bounds. Keep the camera fixed for production unless the existing UX supports interaction.

Use a Choreographer-driven loop owned by the component lifecycle. There are no imported driving animation clips to apply. The runtime controller directly controls transforms and material state. UI preference changes should update the same loaded asset, not recreate the renderer.

## Reviewed primary sources

- [Filament Android ModelViewer](https://github.com/google/filament/blob/main/android/filament-utils-android/src/main/java/com/google/android/filament/utils/ModelViewer.kt)
- [Official Android glTF viewer sample](https://github.com/google/filament/blob/main/android/samples/sample-gltf-viewer/src/main/java/com/google/android/filament/gltf/MainActivity.kt)
- [gltfio loader and material providers](https://google.github.io/filament/dup/gltfio.html)
- [gltfio base material parameters](https://github.com/google/filament/blob/main/libs/gltfio/materials/base.mat.in)
- [TransformManager API and transient instance lifetime](https://github.com/google/filament/blob/main/android/filament-android/src/main/java/com/google/android/filament/TransformManager.java)
- [MaterialInstance API](https://github.com/google/filament/blob/main/android/filament-android/src/main/java/com/google/android/filament/MaterialInstance.java)
- [LightManager API and intensity units](https://github.com/google/filament/blob/main/android/filament-android/src/main/java/com/google/android/filament/LightManager.java)

## Version 1.1 lighting geometry

Each side shares `Mat_Indicator_L` or `Mat_Indicator_R` across the rear bumper, front bumper, two mirror ribbons and D-pillar See Me blade. Update every material instance bearing that name; no extra blink timers are needed. See `indicator_groups` in the contract for node names. The beam meshes now form wide, feathered pools on the road plane; high beam adds a longer, narrower pool. Preserve COLOR_0 alpha, BLEND mode and depth testing. The car paint must not show a travelling white specular spot when yaw/steering animates.

## Contract 2.0

Bind the additional pivots using ../OPENINGS_AND_SOLAR.md. Implement OpeningSceneSink alongside TwinSceneSink. Assign the same gradient Texture and sampler to all instances of Mat_SolarCells and Mat_SolarPower, preserving their factors and TEXCOORD_0. A procedural gradient requires a custom material; changing baseColorFactor alone cannot express a spatial gradient. Retain one full-roof UV domain rather than deriving separate coordinates for each panel.
