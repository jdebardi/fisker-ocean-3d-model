# Opening parts and SolarSky colour — contract 2.0

Read this with [INTEGRATION.md](INTEGRATION.md) and the exact `openings` and `solar` entries in `../model-contract.json`. Both GLBs provide the same controls. Previous wheel, paint, lighting, yaw and steering controls remain available.

## Independent opening inputs

Use six independent normalised values: `frontLeft`, `frontRight`, `rearLeft`, `rearRight`, `tailgate`, `sunroof`. Zero means closed; one means the supplied fully-open visual pose. Intermediate fractions are supported. A boolean ajar signal can select zero/one, but it does not measure a physical angle. Use real position telemetry when available. Do not send actuator commands from this component.

The pure Kotlin `../runtime/kotlin/VehicleOpeningController.kt` implements the following operations through `OpeningSceneSink`. Implement the sink in the app's renderer; it is separate from the existing `TwinSceneSink` so old consumers do not need unrelated interface changes.

| Part | Node | Local axis | Full visual opening |
|---|---|---|---|
| Front left door | Hinge_Door_FL | +Y | -68 degrees |
| Front right door | Hinge_Door_FR | +Y | +68 degrees |
| Rear left door | Hinge_Door_RL | +Y | -68 degrees |
| Rear right door | Hinge_Door_RR | +Y | +68 degrees |
| Rear hatch | Hinge_Tailgate | +X | -82 degrees |
| Front sunroof | Slide_Sunroof_Front | translation | +0.060 m Y, then +0.922 m Z |

All axes are LOCAL glTF axes: +Y up, +X vehicle right, -Z forward. Apply rotations/translation offsets relative to saved imported rest transforms. Do not overwrite hub/pivot positions, normalise whole-asset transforms, or accumulate last frame's matrices. The existing VehicleYaw transform remains the parent pose of the complete car.

For the roof, let `f` be the clamped sunroof fraction:

```
lift = clamp(f / 0.18, 0, 1)
slide = clamp((f - 0.18) / 0.82, 0, 1)
translationOffset = (0, 0.060 * lift, 0.922 * slide) metres
```

The front glass and its solar meshes belong to `Slide_Sunroof_Front`. The rear glass and solar meshes belong to `Roof_Rear_Fixed`. Never move the rear group when opening the roof.

The opening controller smooths values with a 180 ms time constant and clamps frame steps to 50 ms. Use its `snap=true` option to apply an initial known state immediately. NaN/infinite inputs are handled conservatively as closed by the reference implementation; the app should separately indicate unavailable telemetry according to its existing UI policy.

The visual limits/travel above are illustrative values, not factory actuator calibration. There is no baked driving/opening clip or skinning dependency. The app animates the named pivot nodes continuously.

## What follows the openings

- Each front mirror casing AND both of its indicator strips follow the corresponding front door. The strips remain on the forward casing, not the mirror glass side.
- The rear hatch carries its glass, central rear body surface, inner taillight segments, OCEAN wordmark, circular badge, spoiler and centre brake lamp.
- Outer rear light segments, lower-bumper lamps and D-pillar indicators stay on the fixed body.
- Lighting is still controlled through the existing named material instances. Update ALL matching material instances, including split hatch-mounted taillight geometry.
- A simplified dark cabin, seat shapes, door inner skins, sills and cargo floor make open states readable. These are an illustrative interior, not a detailed factory cabin.

## One solar gradient across two moving panels

The model represents this directly using ordinary glTF geometry, materials and UV coordinates. A custom glTF extension is not required. Dynamic vehicle-data binding still belongs to the app.

Meshes:

- Front moving section: `SolarCells_Front`, `SolarOverlay_Front`.
- Rear fixed section: `SolarCells_Rear`, `SolarOverlay_Rear`.

Materials:

- `Mat_SolarCells`: fine photovoltaic-cell layout, with a 0.22 base-colour multiplier for a restrained appearance.
- `Mat_SolarPower`: the larger nested stepped traces, with full base-colour multiplier.

Both materials point to the SAME embedded texture, named `Solar_Shared_Gradient`. All four meshes use TEXCOORD_0 from ONE closed-roof atlas. U runs from the FRONT of the complete roof (0) to the REAR (1). V runs from vehicle LEFT (0) to RIGHT (1). The front section occupies roughly U=0..0.484; the rear occupies roughly U=0.491..1. The small gap is the physical panel seam.

CRITICAL: do not renormalise either half to 0..1, generate UVs from each mesh's own bounding box, or derive the gradient from animated world positions. That would restart or shift the gradient at the seam.

At the closed position, sample the same colour field with the existing UVs for both halves. When opened, keep those UVs unchanged: the front pattern and its colours travel with the glass, while the rear remains fixed. Closing restores the same continuous whole-roof colour field.

### Portable implementation: update a tiny shared texture

1. Allocate one RGBA texture, for example 256 x 2 pixels, using linear filtering and clamp-to-edge sampling. No mipmaps are needed for the dynamic texture with a linear minification sampler.
2. Generate the front-to-rear gradient along pixel X; both rows have identical colours.
3. Assign that ONE texture object to the base-colour sampler of EVERY instance of `Mat_SolarCells` and `Mat_SolarPower`.
4. Preserve the existing material multipliers and the meshes' UVs. In particular, do not make the cell field as bright as the larger traces.
5. Upload new pixels only when power, palette or display mode changes. Reuse the texture; do not reload the GLB or recreate materials each frame.
6. Mark RGB pixels as sRGB when the renderer decodes sRGB textures. A custom shader receiving linear colours must use linear inputs instead. Avoid applying the conversion twice.

For a different gradient direction, use a small 2D texture sampled through the same full-roof UVs. Both roof sections still share the same texture and coordinate domain.

### Custom-material alternative

A renderer with custom material support can avoid texture uploads by evaluating a shared uniform colour function:

```
t = clamp(dot(uv0, gradientDirection) + gradientOffset, 0, 1)
gradient = mix(startLinearRGB, endLinearRGB, t)
colour = mix(neutralLinearRGB, gradient, powerFraction)
baseColour = colour * existingMaterialMultiplier
```

Use the SAME uniforms for the cell and trace materials and both panel sections. Keep the original UV0 and low-glare surface behaviour. This is an alternative to the shared-texture method, not an additional layer to apply on top of it.

## Power telemetry and appearance

Map measured solar watts using a configurable/calibrated maximum:

```
powerFraction = clamp(actualSolarWatts / configuredExpectedMaximumWatts, 0, 1)
```

Do not hardcode an unverified maximum or claim per-cell/per-panel measurements from one whole-roof signal. Without measured spatial data, the gradient is a visual indication of total capture, not a heatmap of actual local panel output. Distinguish unavailable/stale data from a measured zero; use a neutral appearance when data is unavailable.

The preview slider is explicitly simulated and demonstrates the colour behaviour. It is not connected to a car.

Keep solar colouring matte, stable and restrained. Do not add pulsing, scrolling light effects, specular twinkles, strong emission or bloom to the solar layer. Preserve the calm paint profile and existing lamp behaviour.

## References and limitations

The cell grid and nested stepped traces were modelled after production roof photographs, including the Car and Driver 2023 Ocean One top view and Fisker's published open-roof press photograph. Source links are in `reference/SOURCES.md`. The pattern and articulation are visual interpretations of the existing low-poly car, not manufacturer CAD, production dimensions or actuator limits.

## Check before shipping

Test each opening alone, partial positions, all openings together, closed-pose restoration, both GLB variants, and combined yaw/steering. Verify that mirrors and their lamps follow front doors; the spoiler brake lamp and inner rear light segments follow the hatch; D-pillar and bumper lamps stay fixed. Check the roof at 0%, lift threshold, partial slide and 100%.

With the roof closed, use a conspicuous test gradient to verify it crosses the seam without restarting. Then open the roof and confirm the front section retains its assigned colours. Test 0%, 50% and 100% power, a custom palette, and unavailable telemetry. Profile on the actual target devices; desktop checks do not establish device performance.


## Sliding handles

Use `OpeningState(handlesExtended = 0f)` for locked/retracted and `1f` for unlocked/extended. The four `Slide_Handle_*` nodes are door-local sliders, 35mm outward along -X for left and +X for right, always added to saved local rest translations. Their parent doors carry them through opening motion. The exact vectors are in `../model-contract.json` under `handles`. See `STATE.md` for lock-state mapping and `INTEGRATION.md` for the shared integration contract.


## Current cabin and fixed rear signals

This model has a continuous three-seat rear bench with three headrests. Front bumper indicators and lower rear signals/reversing lights are material regions of the fixed body skin in `Body_Main`, outside the opening hatch. Their empty legacy-name markers are not renderable lenses. Bind the named materials across the model.


## Fixed glass and framed doors — release 3.0.0

`Glass_Windscreen` and `Glass_Quarter_L/R` stay under `VehicleYaw`. They do not follow door hinges. `Frame_Door_FL/FR/RL/RR` carry complete dark window frames and follow their corresponding door hinges automatically. Do not recolour those frames with paint. Rear handles are farther forward; no controller changes are needed. See section 6 of `INTEGRATION.md`.
