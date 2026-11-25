# Unity

Use a scene-preserving glTF importer compatible with the project's Unity version/render pipeline. Unity glTFast supports runtime loading; inspect its pinned-version documentation and extension/material support. Do not import this as one merged static car mesh.

1. Import/instantiate once, keeping named empty/pivot GameObjects and opening-part ownership. Verify zero-scale wheel/beam groups were not discarded.
2. Record local rest transforms and map all contract names. Unity's coordinate conventions and importer conversion differ from glTF's basis. Determine the imported forward axis and steering signs visually; transform glTF local axes into the imported basis instead of copying Euler signs blindly.
3. Port the Kotlin logic to C# or implement an equivalent state adapter. Apply local rotation offsets relative to rest, maintain speed/radius spin phase and show only one wheel family per hub.
4. Create appropriate per-car material instances for paint, lamps and accents. Gather every named primitive material, including Body_Main's integrated signal regions and hatch-mounted segments. Avoid editing global shared materials when several cars need independent state.
5. Implement HDR emissive factors and restrained bloom in the project's URP/HDRP/Built-in pipeline. Check imported PBR, double-sided windows, single-sided mirrors, specular factor and blended beam vertex alpha. Engine material names/properties may differ after conversion: explicitly map them.
6. Use one shared roof Texture2D or equivalent shader input, preserving the original UV0 atlas on both panels. Changing two unrelated material colours is not the required continuous gradient.
7. Validate converted units, fixed windscreen/quarter glass, window-frame hinges, doors, hatch, roof and handles. Suspend updates when hidden and dispose runtime-created assets/materials/textures.

This guide is not a supplied Unity package or tested C# adapter. Native model animation clips are not required; ordinary transforms drive the model.

Primary reference: [Unity glTFast runtime import](https://docs.unity3d.com/Packages/com.unity.cloud.gltfast@6.0/manual/ImportRuntime.html). Use documentation matching the package actually selected by the project.
