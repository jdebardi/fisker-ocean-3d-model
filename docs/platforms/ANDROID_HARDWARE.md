# Android board profile

The package targets a small dashboard car viewport on Android. It has not been benchmarked on the user's boards. The proposed starting profile is ARM64, OpenGL ES, a roughly 512-pixel car render target, the lightweight GLB, restrained bloom and 30 fps. These are starting settings to measure, not measured performance claims.

## Orange Pi 5 Pro

The manufacturer's manual identifies RK3588S and Mali-G610, with advertised OpenGL ES 3.2 and Vulkan 1.2 support. The installed Android image and GPU driver determine what the app can actually use. Verify hardware acceleration and the app ABI rather than selecting a renderer solely from the chip specification.

## ROCK board

The user calls this board ROCK 5C Pro. The official Radxa pages reviewed describe ROCK 5C Standard (RK3588S2, Mali-G610MC4) and ROCK 5C Lite (RK3582, no GPU). They do not establish a distinct Pro configuration. Identify the actual board/SoC before assuming it is the standard GPU-equipped version; do not substitute a ROCK 5B configuration. This naming uncertainty does not change the GLB format.

## Collect on both installed Android systems

- Android release/API level, build fingerprint and security/driver build.
- `Build.SUPPORTED_ABIS`; prefer an `arm64-v8a` app/library build where supported.
- Advertised GLES version and actual GL renderer/vendor/version from a valid rendering context or the renderer's diagnostic API.
- Whether the renderer is hardware accelerated; a software renderer is not a suitable basis for the driving animation target.
- Component viewport size and display resolution.
- Frame timing, CPU/GPU load and thermal behaviour with navigation, media and the rest of the dashboard active.

Useful read-only collection commands, with the actual package name substituted where required:

```sh
adb shell getprop ro.product.model
adb shell getprop ro.board.platform
adb shell getprop ro.product.cpu.abilist
adb shell getprop ro.build.version.sdk
adb shell getprop ro.build.fingerprint
adb shell dumpsys SurfaceFlinger
adb shell dumpsys gfxinfo YOUR.APP.PACKAGE framestats
```

SurfaceFlinger output may include the GL vendor/renderer/version depending on the Android image. `reqGlEsVersion` is advertised support, not proof of the active renderer. Do not call GLES APIs without a current GL context just to print diagnostics.

## Quality ladder

1. Load once, use the light model, no real spotlights/shadows, a neutral environment, and a capped 30 fps viewport.
2. Add modest bloom for lamp glow. Confirm that white paint remains readable with lights off.
3. Add depth-tested visual spill cards if desired. They are much cheaper than adding a road scene with multiple shadowed lights.
4. Try the standard model and/or 60 fps after the full dashboard is profiled.
5. Test Vulkan as a separate measured option if the Android vendor driver supports it reliably. Keep OpenGL ES as the initial implementation path.

Use the app's lifecycle to stop or reduce rendering when hidden. Avoid a full-display 4K/8K post-process just to draw a small car. No NPU functionality is needed for this component.

## Manufacturer references

- [Orange Pi 5 Pro manufacturer manual](https://orangepi.net/wp-content/uploads/2024/05/OrangePi_5_Pro_RK3588S_User-Manual_v1.1.pdf)
- [Radxa ROCK 5C introduction and specifications](https://docs.radxa.com/en/rock5/rock5c/getting-started/introduction)
- [Filament rendering engine and Android support](https://github.com/google/filament)
