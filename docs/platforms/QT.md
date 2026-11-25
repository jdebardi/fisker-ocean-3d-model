# Qt Quick 3D and embedded desktop

Qt Quick 3D RuntimeLoader supports glTF 2.0 and GLB. Name-query APIs depend on Qt version: the documented query/queryAll methods are introduced in Qt 6.12. Do not assume older Qt versions expose those APIs; use a suitable version-specific importer or generated scene representation that keeps bindings accessible.

- Load once, retain named hierarchy and local rest transforms, and build node/material maps using APIs supported by the selected Qt version.
- Implement yaw, steering, wheel roll, openings and handles in QML/C++ through an adapter to the common state contract. Convert imported axes/units consistently and keep fixed windscreen/quarter glass out of door hinges.
- Preserve multiple Body_Main materials and update all instances of each lamp/paint name. Use an appropriate material/texture path for the shared whole-roof gradient.
- Check glTF PBR, single-sided mirror glass, soft beam vertex alpha, depth behaviour, tone mapping and bloom against the browser reference. Document any importer/material limitation.
- Use efficient family visibility, bounded viewport resolution and a lifecycle-owned update loop. Measure frame times with the complete embedded UI, not a blank isolated car view.

This release does not include a Qt application or board benchmark. The same GLB can be used on desktop and embedded Qt only when the selected Qt build, graphics backend and importer support the required features.

Primary reference: [Qt RuntimeLoader documentation](https://doc.qt.io/qt-6/qml-qtquick3d-assetutils-runtimeloader.html).
