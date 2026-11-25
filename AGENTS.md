# LLM integration instructions — Fisker Ocean 3D Model v3.0.0

Read this file when maintaining this repository or using the supplied model in another project. These instructions are independent of any particular LLM vendor. Follow the consuming project's applicable instructions and the developer's requested scope.

## Read in this order

1. `README.md`: human overview, local preview and feature inventory.
2. `LICENSE.md` and `ATTRIBUTION.md`: preserve the model and dependency credits.
3. `model-contract.json`, `paint-presets.json`, `wheel-options.json`: exact names and parameters.
4. `docs/INTEGRATION.md`, `docs/STATE.md`, `docs/OPENINGS_AND_SOLAR.md`: complete behaviour contract.
5. The relevant guide in `docs/platforms/`.
6. `runtime/kotlin/VehicleTwinController.kt`, `runtime/kotlin/VehicleOpeningController.kt` and `preview/src/main.js`: reference behaviour.
7. `docs/VALIDATION.md` and `tests/README.md`: checks and current verification limits.

Release version is **3.0.0**. Runtime contract version remains **2.0**. Do not invent a schema migration or rename runtime nodes just to match the release number.

## Inspect before implementing

Identify the destination framework, renderer, asset pipeline, units/axes, lifecycle, state transport, telemetry fields and settings storage. Reuse the existing architecture and the component's allocated viewport. Preserve the rest of the application. A native project does not need to become a WebView merely because a web reference is supplied.

Use one loaded model instance and a renderer adapter. Cache name-to-node and name-to-material-instance lookups and all rest transforms. Port the Kotlin behaviour when the project uses another language. The repository supplies working browser reference code and renderer-independent Kotlin controllers, not finished integrations for every platform.

## Required model capabilities

Implement the features requested by the developer, with bindings available for the complete set:

- Paint presets/custom colour isolated to Paint_Body; six wheel presets across four families.
- Vehicle yaw, front-wheel steering, speed-driven wheel roll, reverse and stale-speed handling.
- Independent front headlights OFF/DIPPED/HIGH and tailLights; brake/spoiler-centre brake; left/right/hazard blink; reverse lamps; optional front spill.
- Four independent opening doors, hatch and lift/slide roof; four retractable handles.
- Shared whole-roof solar gradient/power intensity across the moving and fixed sections.
- Fixed windscreen and rear quarter panes, and complete moving door window frames.

Preview camera buttons, orbit gestures, simulation sliders, Open all and the driving demo are not mandatory production UI. Do not add them to a driver's interface unless requested. The showcase MP4 is illustrative camera choreography, not a glTF animation clip.

## Preserve these invariants

- The GLB is metres, +Y up, +X vehicle right, -Z forward. Apply engine coordinate conversion once. Imported pivots, axes and rests must survive import; no mesh merging/flattening that destroys controls.
- Resolve by names, never hard-coded node/material indices. Update every material instance sharing a controlled name.
- Transform methods apply absolute offsets from saved rest transforms. Do not accumulate last frame's transform or move wheel meshes away from their hubs.
- Glass_Windscreen and Glass_Quarter_L/R remain under VehicleYaw. Frame_Door_* follows its matching Hinge_Door_*. Mirrors and handles stay with their doors; hatch children stay with the hatch.
- Front and rear bumper indicator/reverse surfaces are material regions of Body_Main. Legacy lamp nodes can be empty markers. Switch material emission; never hide or transform Body_Main to toggle lights.
- High adds front lighting only, tailLights is independent, brake is brighter than tail, and centre brake is on the spoiler. All indicators share one monotonic phase.
- Only one wheel family renders at each hub. Restore unit scale for initially disabled wheel/beam groups before using real visibility controls. Keep steering/roll phase during style changes.
- Paint transform is linearRGB = 0.022 + 0.978 * requestedLinearRGB, exactly once. Store the user's original sRGB hex. The default GLB paint is already prepared. Keep paint lighter than dark trim and preserve distinct opaque glass and fixed grey wheel-well liners.
- Keep calm diffuse lighting, roughness >=0.70, restrained specular and zero paint clearcoat. Use restrained renderer bloom for lamps; do not add sparkling reflections or bloom the white body.
- Preserve the single closed-roof UV0 atlas and shared texture across both solar panels. Never renormalise each half's UVs or recompute them from animated world positions.
- Keep preview/local-file loading functional when changing source. Rebuild preview/app.js after editing preview/src/main.js.

## Validation and completion

Run `npm test`, `npm run build` after viewer source edits, and `npm run validate` after asset changes. Run the Kotlin smoke tests when modifying controllers and a compiler is available. Exercise local-file and HTTP preview routes when changing asset loading. Check all feature combinations listed in docs/VALIDATION.md in the destination renderer. Measure performance on actual target devices; do not present desktop checks as device benchmarks.

Report the framework/renderer found, files changed, mapped real signals versus simulated/default values, build/check results, attribution placement and any unsupported feature or untested device. Do not add actuator commands: this component visualises state. Do not publish, tag or upload a release merely because this file describes release preparation; follow the developer's current instruction.
