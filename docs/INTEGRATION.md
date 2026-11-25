# Integrating the model into any project

Release **3.0.0** uses **glTF 2.0** and the project's **runtime contract 2.0**. Start from the named hierarchy in `../model-contract.json`. The browser reference is `../preview/src/main.js`; the two renderer-independent Kotlin controllers are in `../runtime/kotlin/`.

## 1. Choose and preserve an import path

Use a glTF-capable renderer or convert the asset through your engine's supported pipeline. Preserve node names, parent-child relationships, local transforms, material names, UV0 and vertex alpha. Test the resulting asset rather than assuming an import that displays a car also preserves every interactive control.

Load `models/ocean.glb` or `models/ocean-lite.glb` once. Cache named nodes and **all** instances of each controlled material. Retain imported local rest translation, rotation and scale. Missing required bindings should produce a useful startup error. Multiple renderable primitives can share one material name; updating just the first produces partial lights/paint.

The GLB is metres, right-handed, +Y up, +X vehicle right and -Z forward. Engines with different units/handedness must convert positions, axes and rotation signs once. Avoid writing glTF Euler angles blindly into a converted hierarchy. Apply an offset rotation in the imported node's correctly converted local basis, then combine with its saved rest transform. Keep the front-wheel steering parent separate from its roll child.

Do not merge/flatten the whole car, weld opening parts to the body, bake zero-scale groups away, or rename required pivots. There is no skeletal/baked driving animation to play. The runtime sets ordinary local transforms and material factors.

## 2. Bind project state

[STATE.md](STATE.md) lists inputs, units and outputs. Inspect the destination application's real data models. Wheel speed uses metres/second; the preview labels its slider km/h and converts it. Steering-wheel angle is not road-wheel angle. Document any signal conversion and defaults. Distinguish unknown/stale values from confirmed off/closed states according to the destination application's UI policy.

Use immutable state or a latest-value mailbox into the owning render thread. The Kotlin reference smooths yaw/steering over about 120ms, openings/handles over 180ms, and integrates wheel rotation from speed/radius. Port that logic to another language, preserving frame-time bounds and finite-value handling. Solar texture generation is a separate renderer responsibility, not part of VehicleOpeningController.

The model visualises state. It does not send commands to vehicle actuators or obtain telemetry by itself.

## 3. Driving and wheel selection

- `VehicleYaw`: car pose about +Y; positive glTF yaw is a visual left turn. Ride-height offset for the selected tyre radius belongs here too.
- `Steer_FL/FR`: front road wheels turn about local +Y at the existing hubs. Rear wheels do not steer.
- `Roll_FL/FR/RL/RR`: rotate around local +X. Forward speed uses negative roll: deltaRoll = -speedMps * dt / selectedRadius.
- `WheelStyle_F3/F5/F6/F7_*`: only one family is visible per hub. F3a/F3b and F5a/F5b are finish variants sharing geometry. Read IDs/materials/radii from `wheel-options.json`.

Inactive families initially have zero scale for portable invisibility. Restore their scale to 1 at binding, then hide their descendant renderables with the engine's real visibility/layer mechanism to avoid draw calls. Keep common roll/steer parents active. Changing style must retain wheel phase and pivot translation. F7 uses its listed smaller radius and corresponding ride-height adjustment.

## 4. Paint, glass and trim

Change only `Paint_Body`. Convert requested hex sRGB to linear RGB, then apply `displayLinear = 0.022 + 0.978 * requestedLinear` per channel **once**. The default GLB paint is already lifted; do not process it again on load. Preserve the original requested hex for preferences. Use presets' restrained metallic/roughness settings, roughness >=0.70, paint metallic <=0.12, clearcoat 0 and imported specular factor 0.12.

Black paint must remain distinguishable from the darker trim. Preserve the model's separate opaque muted blue-grey `Glass_Smoked` and `Mirror_Glass`, black `Roof_Glass`, fixed grey wheel-well liners, badges, tyres and trim. Do not apply the paint floor to those materials. Mirror_Glass is single-sided and rear-facing with a separate dark backing; avoid copying double-sided window settings onto it.

## 5. Lamps, indicators and spill

Headlight state is OFF / DIPPED / HIGH; `tailLights` is an independent boolean. HIGH includes dipped front lighting and adds brighter, longer front illumination. It does not switch rear lights. Braking overrides rear running-lamp emission and adds the spoiler centre brake lamp, regardless of headlight/tail state. Reverse lights are independent. Left/right/hazard indicators share one monotonic blink phase (1.4Hz, 50% duty by default), including bumper, mirror and D-pillar surfaces.

Read material names and strengths from the contract. The reference values are relative HDR linear emission values: dipped 2, high additional 5, tail .40, brake 7, centre brake 10, orange indicator 4 and reverse 4. Tune exposure/bloom for the target renderer and display rather than assuming identical pixels in every engine.

**Bumper signals are material regions of `Body_Main`.** Front/rear legacy lamp nodes may be empty binding markers. Update `Mat_Indicator_L/R` and `Mat_Reverse_L/R` emission on all matching instances; never hide, transform or replace Body_Main to turn lights off. Split hatch/outer taillight surfaces must all receive the same side's material update. Indicator colour is deep orange, not yellow.

Emission supplies bright surfaces; visible glow requires the renderer's bloom/post-processing. Use a restrained HDR pass and stable exposure, compare tail versus brake, and prevent white paint reflections from blooming. Physical PBR materials still need scene lighting; the GLB alone is not a lit showroom.

Optional `Beam_Dipped_L/R` and `Beam_High_L/R` start at zero scale. Restore unit scale before showing them. Preserve glTF BLEND, vertex COLOR_0 alpha, soft falloff, depth testing and no depth writes. Opaque body depth must occlude the beams. High cards add to dipped cards. Four optional `Spot_*` glTF punctual lights start at zero intensity; leave them off for the cheapest view, or calibrate them against the spill cards. A spotlight needs a receiving surface to illuminate and does not create a visible cone in empty space. Avoid doubling the same spill with several representations.

## 6. Doors, hatch, roof and handles

Read [OPENINGS_AND_SOLAR.md](OPENINGS_AND_SOLAR.md) and the JSON `openings`/`handles` entries. All fractions are independent and clamped 0..1. Apply absolute offsets from each rest transform; do not accumulate transforms.

- Doors: `Hinge_Door_FL/FR/RL/RR`, local Y, -68 degrees left / +68 right at full visual opening.
- Hatch: `Hinge_Tailgate`, local X, -82 degrees full opening.
- Sunroof: `Slide_Sunroof_Front`, lifts +0.060m Y during the first 18%, then slides +0.922m Z. The rear roof stays fixed.
- Handles: `Slide_Handle_FL/FR/RL/RR`, local X, -0.035m left / +0.035m right fully extended. The supplied controller drives all four with one extension fraction; an application may drive the four named pivots separately if its signals require that.

The corrected rear shut lines lie in the black C-pillars, with forward rear handles and lower edges following the arch. `Glass_Windscreen` and `Glass_Quarter_L/R` are fixed under VehicleYaw. `Frame_Door_*`, windows, mirrors and handles follow their corresponding doors. Hatch glass, branding, spoiler, centre brake lamp and inner taillight portions follow the hatch. Do not reparent these when importing or animating.

The limits and travel are illustrative poses, not factory actuator calibration. Use measured position telemetry where available; an ajar boolean can choose closed/open but does not encode a measured angle. The cabin is intentionally simplified.

## 7. Solar visualisation

The front moving and rear fixed solar meshes already share one embedded texture and one closed-roof UV0 domain. Update the **same** small RGBA texture on all instances of `Mat_SolarCells` and `Mat_SolarPower`; preserve their material multipliers. A simple base-colour change cannot produce a spatial gradient.

Keep both sections' original UVs while the front moves. U spans the whole roof front-to-rear, V left-to-right. Do not normalise each panel to 0..1 or compute colour from animated world positions. The pattern travels with the moving panel; closing restores the continuous colour field. Normalise real captured watts to a calibrated maximum if the application wants a 0..1 intensity; the model neither measures nor predicts solar power.

## 8. Camera, lifecycle and performance

Use the contract's rear/elevated orthographic camera as a starting point and adapt width to the actual viewport. Fit the car's visible geometry, excluding optional beam extents and inactive wheel styles. A destination app can choose its own camera/interaction; preview orbit buttons and simulation controls are separate features.

Use soft diffuse fill and a restrained key light; avoid animated/studio reflections. Render at the component's resolution. Start with the lightweight model, a modest render target and 30fps on embedded/mobile hardware, then measure with the rest of the application active. These are starting settings, not measured performance guarantees.

Create/destroy GPU resources on the renderer's required thread. Pause when hidden/backgrounded, handle surface/device loss, dispose replaced assets/textures/materials, and avoid per-frame allocations or reloading the model for state changes. Maintain a fallback illustration if loading or acceleration is unavailable.

## 9. Completion

Run the destination framework's build/tests and [VALIDATION.md](VALIDATION.md) acceptance checks. Preserve the credits in the app's attribution surface. Report actual device/driver measurements separately from desktop/browser verification. Read the platform guides for renderer-specific import/axis/material considerations; they are integration guidance, not tested complete apps for every engine.
