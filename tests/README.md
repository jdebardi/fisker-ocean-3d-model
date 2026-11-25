# Tests

From the project root, `npm test` checks the release layout, documentation links and model contract. `npm run validate` runs the Khronos glTF validator. Install development dependencies with `npm ci` first.

The Kotlin files are renderer-independent JVM smoke tests. With a JDK and Kotlin compiler installed, compile each main separately:

```sh
kotlinc runtime/kotlin/VehicleTwinController.kt tests/ControllerTest.kt -include-runtime -d controller-tests.jar
java -jar controller-tests.jar
kotlinc runtime/kotlin/VehicleOpeningController.kt tests/OpeningTest.kt -include-runtime -d opening-tests.jar
java -jar opening-tests.jar
```

These exercise controller logic, not an Android/Filament adapter or target-device performance. The current paint expectation is the 0.022 linear floor. Generated JARs are ignored by Git and excluded from release packaging.
