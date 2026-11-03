# Overgeared — Fabric 26.3 port

This repository is for an **in-progress, unverified port** of [Overgeared](https://github.com/phuccom000/Overgeared) to Minecraft **Fabric 26.3**.

**Status:** Phase 9 source is imported and its isolated source tests pass. The first full Java 25 Gradle run reaches compilation but fails on Minecraft 26.3 API incompatibilities. **No playable JAR has been produced.** See [the compiler baseline](docs/build-status-2026-11-03.md).

## Upstream and credits

Original mod: **Overgeared: A Blacksmith Mod**, by StirDrem. Porting changes must preserve the upstream license and copyright notices; see the source archive's `UPSTREAM-LICENSE-NOTICE.md`.

## Porting source

The complete **Phase 9 modified source** was imported on November 3, 2026 from pinned upstream commit `c898a0ce` plus the verified delta stored under `porting/`. It is not a compiled or tested Minecraft mod. [GitHub Actions](../../actions) now runs Java 25 compilation on every source push.

## Priorities

1. Preserve anvils, hammering, alloy furnaces, casting, and forging-quality mechanics.
2. Compile successfully against Minecraft 26.3 and Fabric Loader, Java 25.
3. Verify client and dedicated-server startup and gameplay.
4. Only after the port is playable, add mixed-material modular equipment.

**Do not install this project into a survival world until a release is explicitly marked tested.**
