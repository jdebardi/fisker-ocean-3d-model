# Validation and acceptance

## Supplied checks

- `npm test`: release metadata, local documentation links, file layout, licence files, required named nodes/materials, fixed-glass/frame parentage and matching controls across both GLBs.
- `npm run build`: reproducibly bundles the editable web source into preview/app.js.
- `npm run validate`: Khronos glTF validator for both GLBs, reporting errors/warnings and recording asset-validation.json under validation/.
- `tests/ControllerTest.kt` and `tests/OpeningTest.kt`: optional JVM reference-logic smoke tests. See tests/README.md.
- The inherited development evidence records geometry checks for mirrors, lamp regions, badge mounting, wheel fit, fixed glass, door boundaries and handles. It does not establish validation on every renderer or device.

The v3.0.0 release preparation changes layout, documentation, generic integration guidance, local loading and version metadata. The model geometry is the completed internal 2.3.3 geometry. Its MP4 demonstrates that geometry. Runtime schema remains 2.0.

## Destination acceptance matrix

1. Resolve every contract binding after import; preserve all rests, units and axes.
2. Fit the car without including invisible beam extents or every inactive wheel family.
3. Test positive/negative yaw and steering, forward/reverse speed, stale speed, and style switching without a spin-phase reset or hub displacement.
4. Select all six wheels, all paint presets and custom black/white. Paint changes only Paint_Body; dark paint stays distinct from trim/glass/wheel wells.
5. Test front-only, tail-only, both and neither. High affects the front only; brake remains brighter than tail and lights the spoiler centre lamp independently.
6. Test each side's front/mirror/D-pillar/rear indicator, synchronised hazards and separate reverse lamps. Integrated Body_Main material regions never move or disappear independently of the skin.
7. Compare lamps off/tail/brake/high at fixed exposure; verify white paint does not bloom and headlights do not shine through the body.
8. Open every door at several fractions. Whole main door panes and complete black sashes move together; windscreen and third quarter panes remain fixed. Mirrors/handles follow their door. Check closure and intersections from side/front/rear/elevated views.
9. Extend/retract handles with doors both open and closed. Rear handles are forward of the C-pillar shut line.
10. Open/close the hatch and roof; hatch children stay attached, front roof lifts then slides, rear roof stays fixed.
11. Sweep solar intensity and gradient endpoints at closed/open roof positions. There is one continuous closed-roof atlas, no gradient restart at the panel seam.
12. Verify local-file and HTTP viewer loading after viewer changes; check pause/resume, resize, view/surface/device loss and resource cleanup in the destination app.
13. Record actual device/OS/driver, render target, model choice, frame times and memory with the full application active.

Only report a platform/device as tested if it was exercised there. The guides for Unity, Unreal, Apple, mobile wrappers and Qt are implementation guidance; no complete adapters or platform-specific benchmark claims are supplied.

## v3.0.0 release-preparation record

The served preview and its local GLB picker were exercised in the browser. Automated direct file:// opening is blocked by the browser tooling, so double-click/file:// startup has been code-reviewed but not browser-verified here. No target Android boards or other native engine integrations were exercised for this release preparation.
