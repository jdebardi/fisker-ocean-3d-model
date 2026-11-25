# Web and desktop web

The included viewer is the complete JavaScript/Three.js reference. Root `index.html` loads bundled `preview/app.js`; editable source is `preview/src/main.js`. No CDN is required. Over HTTP it loads `models/ocean.glb` and root JSON files. From file:// it asks the user to select a GLB, then reads the embedded digitalTwin metadata and textures.

## Integrating into a website

1. Reuse the destination's Three.js renderer where practical; do not create a second render loop unnecessarily. Load GLB with GLTFLoader, or parse a user-selected ArrayBuffer with parseAsync. Treat load errors and WebGL unavailability visibly.
2. Cache nodes using names and traverse meshes to gather every material instance. The source's bind/allMaterials helpers demonstrate this. Preserve glTF local transforms and the separate Steer/Roll hierarchy.
3. Copy state logic from applyPaint/selectWheel/lamps/openings and the motion loop into a reusable component. Keep state/DOM controls separate so real application inputs can replace preview sliders.
4. Use camera aspect/viewport resize handling. In React, own resources inside the component lifecycle; a compatible React Three Fiber project can use its existing glTF loading and frame loop rather than mounting this whole page.
5. Keep materials in linear colour space, imported texture colour-space handling, stable tone mapping/exposure, soft lighting and optional restrained bloom. Preserve blended beam alpha/depth and opaque body depth. Reuse one solar DataTexture and update its pixels rather than allocating it every frame.
6. Pause/dispose on unmount and prevent loading races. Cap device pixel ratio and profile the car component together with navigation/media UI.

The preview's debug state, camera presets, panels and simulation loop are examples; choose the production UI explicitly. Local-file loading of a different GLB expects this project's embedded metadata and named contract, not any arbitrary GLB.

## Electron and Tauri

The web renderer and binding logic can run inside a desktop web container. Keep assets local, use the wrapper's supported local-resource URL scheme, and connect application state through its normal bridge. Desktop access to native files or vehicle data is not provided by the GLB or viewer. Verify GPU acceleration, lifecycle, path handling and attribution packaging on each OS. No Electron/Tauri app shell is supplied.

Primary reference: [Three.js GLTFLoader documentation](https://threejs.org/docs/#GLTFLoader), [Three.js colour management](https://threejs.org/manual/en/color-management.html). Pin the installed Three.js version; the supplied preview uses 0.187.0.
