# Unreal Engine

Unreal supports glTF/GLB import, but a successful static-mesh import is not sufficient for this interactive model. Choose an import/scene pipeline for the selected Unreal version that preserves separate named nodes, pivots, materials and parts. Verify that an Actor/component hierarchy survives, or reconstruct it from the GLB contract. Runtime GLB loading may require a separately selected runtime-capable importer; this release does not provide one.

- Convert metres/+Y-up glTF into Unreal's centimetres/Z-up coordinate system through one consistent basis transform. Convert local hinge/steering/roll axes and signs, not just camera positions. Keep imported pivot translations and rest rotations.
- Build a reusable Actor/component with cached references for VehicleYaw, Steer_*, Roll_*, Hinge_Door_*, Hinge_Tailgate, Slide_Sunroof_Front and Slide_Handle_*. Blueprint or C++ state setters can use the common contract; port smoothing and speed integration from the reference logic.
- Keep fixed windscreen and quarter panes under the body. Complete Frame_Door_* components must follow their hinges. Avoid merging or static-batching articulated parts into the fixed body.
- Use dynamic material instances for this car's paint, accents and all matching lamp surfaces. Import Body_Main's multiple material slots intact: integrated indicator/reverse regions must remain individually addressable.
- Map glTF PBR/emission/specular and alpha/depth behaviour into the project's chosen materials. Tune HDR bloom/exposure consistently; keep the matte driver profile and verify beams remain occluded by the body. Do not assume an imported glTF material has the same parameter names as the reference code.
- Update one shared solar texture through the original UV0 domain on both panels. Preserve all moving/fixed relationships.
- Profile with the consuming project, and package credits with the application.

The release includes no Unreal plugin, Blueprint asset or verified runtime adapter. Validate the complete feature set after conversion, including zero-scale style groups and the single-sided mirror pane.

Primary reference: [Epic glTF support](https://dev.epicgames.com/documentation/en-us/unreal-engine/gltf-file-format-support-in-unreal-engine).
