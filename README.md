# Overgeared — Fabric 26.3 port

This repository is for an **in-progress, unverified port** of [Overgeared](https://github.com/phuccom000/Overgeared) to Minecraft **Fabric 26.3**.

**Status:** Work in progress. No playable JAR has been produced. Java 25 compilation and in-game testing remain outstanding.

## Upstream and credits

Original mod: **Overgeared: A Blacksmith Mod**, by StirDrem. Porting changes must preserve the upstream license and copyright notices; see the source archive's `UPSTREAM-LICENSE-NOTICE.md`.

## Porting source

The most recent modified source archive is **Phase 9** of the port, created November 3, 2026. The complete Phase 9 archive will be committed here before GitHub Actions can build that revision. An upstream 1.21.1 build is **not** equivalent to the Phase 9 port.

## Priorities

1. Preserve anvils, hammering, alloy furnaces, casting, and forging-quality mechanics.
2. Compile successfully against Minecraft 26.3 and Fabric Loader, Java 25.
3. Verify client and dedicated-server startup and gameplay.
4. Only after the port is playable, add mixed-material modular equipment.

**Do not install this project into a survival world until a release is explicitly marked tested.**
