# State inputs and bindings

The following is an engine-independent state surface. It is descriptive, not a new JSON schema or network protocol. Choose field names/types that fit the consuming project and map them to this behaviour.

| Input | Unit/range | Binding/behaviour |
| --- | --- | --- |
| Visual car yaw | Degrees; reference clamps ±15 | VehicleYaw local Y |
| Front road-wheel steering | Degrees; reference clamps ±35 | Steer_FL/FR local Y; independent of visual yaw |
| Vehicle speed | m/s, signed | Integrate Roll_* around local X using selected tyre radius; negative speed reverses spin |
| Telemetry fresh | Boolean | Reference suppresses inferred motion on stale speed; destination UI handles unavailable state |
| Wheel preset | F3A_22, F3B_22, F5A_22, F5B_22, F6_22, F7_20 | Family visibility, accent colour/roughness, radius and small ride-height compensation |
| Body paint | Preset or requested sRGB hex | Paint_Body only; linear conversion and 0.022 floor exactly once |
| Headlights | OFF / DIPPED / HIGH | Front lamps; HIGH adds to DIPPED |
| Tail lights | Boolean | Independent rear running lights |
| Brake | Boolean | Brighter rear lens emission plus spoiler centre brake |
| Indicators | OFF / LEFT / RIGHT / HAZARD | Shared blink phase and side material emission across all repeaters |
| Reversing lamps | Boolean | Independent Mat_Reverse_L/R emission |
| Beam cards enabled | Boolean | Optional soft spill geometry; separate from surface lamp emission |
| Four door openings | Four fractions 0..1 | Hinge_Door_FL/FR/RL/RR |
| Hatch opening | Fraction 0..1 | Hinge_Tailgate |
| Sunroof opening | Fraction 0..1 | Slide_Sunroof_Front lift then slide; rear stays fixed |
| Handles extended | Fraction 0..1 | Four Slide_Handle_* pivots; 0 retracted, 1 extended |
| Solar power intensity | Fraction 0..1 | Application generates shared roof colour texture; use real watts/calibration if available |
| Solar gradient endpoints | Two colours or a whole texture field | Shared Mat_SolarCells/Mat_SolarPower texture, original whole-roof UV0 |

VehicleTwinController handles driving, wheels, paint, lamps and optional beam material updates. VehicleOpeningController handles openings and handles. Solar texture creation, cameras, bloom, telemetry transport and UI are renderer/application responsibilities. Scene sink methods consume already-converted **linear** colours and **radian** rotations from the controllers; do not convert those a second time.

The preview speed slider uses km/h and divides by 3.6. Its driving demo synthesises values. Neither is a connection to the car. A lock-state boolean may drive handles 0/1, but keep the presentation mapping explicit: this viewer does not lock or unlock a vehicle.
