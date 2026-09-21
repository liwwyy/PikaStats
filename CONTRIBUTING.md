# Contributing

## Requirements

- JDK 21 to run Gradle, plus JDK 8 for the legacy Forge compiler toolchain.
- The mod itself is compiled to Java 8 bytecode for Minecraft Forge 1.8.9.

## Build

```bash
./gradlew :1.8.9-forge:build
```

On Windows:

```bat
gradlew.bat :1.8.9-forge:build
```

The remapped release JAR is written to `versions/1.8.9-forge/build/libs/`.

## Pull requests

Keep changes client-side, avoid blocking/network work on the render thread, and preserve the Pika API failure semantics (profile 400/404 = nicked; leaderboard 400/404 is not nick detection; 204 = API disabled).
