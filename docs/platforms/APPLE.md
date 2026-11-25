# iOS, macOS and visionOS

Choose the consuming project's renderer. A web-based app can use the supplied web approach. For native RealityKit, plan a supported USD/USDZ conversion/import workflow rather than assuming GLB can be loaded as a native RealityKit entity without conversion. Another native glTF renderer may avoid that conversion; inspect its maintained API and extension support.

## Native workflow

1. Convert from the supplied GLB or editable Blender scene using the project's chosen toolchain. Retain hierarchy, node/material names, local pivots, UV0, texture sharing, alpha and sidedness. A converter may omit zero-scale groups, rename materials or bake meshes together; audit the result before binding.
2. Use a Swift component/state adapter that ports the common controller behaviour. Preserve imported local rest transforms and resolve any unit/axis conversion once. Update on the renderer's appropriate thread/frame lifecycle.
3. Map names to entities/material instances and preserve independent part ownership. Fixed windscreen/quarter panes stay with the body; door frames and mirrors follow their doors.
4. Implement per-car paint, wheel family visibility, HDR lamp emission, optional glow and depth-correct spill using material capabilities available in the selected OS/rendering framework. If custom materials are needed for solar gradients or beam falloff, implement them explicitly and preserve the original shared roof UVs.
5. Test every feature after conversion. PBR appearance, HDR glow, embedded spotlights and unusual alpha handling can change across formats. Keep any unsupported feature visible in the integration report.
6. Profile on the actual device and manage backgrounding, view attachment, resource reuse and attribution.

No USDZ conversion, Swift runtime adapter or Apple device benchmark is included in this release. GLB remains the canonical runtime asset and Blender the editable source.

Primary reference: [Apple: loading entities from a file](https://developer.apple.com/documentation/realitykit/loading-entities-from-a-file).
