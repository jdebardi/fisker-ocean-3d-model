# Flutter and React Native

First inspect the app's existing rendering stack and supported OS targets. Choose one compatible scene renderer rather than introducing several competing 3D runtimes.

## Integration choices

- **Existing compatible 3D component:** load the GLB through it and implement the common name/transform/material contract. Check that all named nodes, material slots, UVs and zero-scale groups remain accessible.
- **Native rendering view:** wrap the project's chosen native renderer behind a platform component and feed it immutable state through the framework's bridge. Android can use the supplied Kotlin controller logic; other native targets need equivalent adapters.
- **Web-based component:** embed a tailored build of the supplied web renderer when that is an explicit project choice. Replace preview sliders with a defined state-message bridge, bundle assets, and manage WebGL/view lifecycle. Do not substitute the full development viewer for the surrounding app UI automatically.

Keep the framework state bridge separate from the renderer implementation. Pass coalesced latest state rather than moving the full scene graph over the bridge every frame. Integrate smoothing, wheel spin and indicator phase on the rendering side, with timestamps as appropriate. Pause rendering when the view is hidden/backgrounded.

Use the same fixed-glass, complete window-frame, paint isolation, lamp material-region, shared roof texture and rest-transform rules as every other platform. A wrapper that only exposes whole-model rotation is insufficient: it must expose named node transforms, material-instance updates, textures and descendant visibility, or supply a native adapter that does.

React web with React Three Fiber is covered by [WEB.md](WEB.md); React Native has different rendering/runtime requirements. This release contains no drop-in Flutter widget, React Native native module or benchmark for either framework. Pin and check the selected wrapper's own documentation, then run the full acceptance matrix on each OS.
