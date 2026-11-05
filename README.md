# Overgeared — Fabric 26.3 port

This repository ports [Overgeared](https://github.com/phuccom000/Overgeared) to Minecraft **Fabric 26.3** with **Java 25**.

**Status (November 5, 2026):** The Fabric build, dedicated-server game tests, and client startup/workstation-screen tests pass. The development JAR and screenshots are available from the [verified Actions run](https://github.com/Irishrune/Overgeared---26.3/actions/runs/42033479804). Changes are in [draft PR #1](https://github.com/Irishrune/Overgeared---26.3/pull/1). See [verification and remaining manual checks](PORTING-STATUS.md); older phase reports are historical.

## Upstream and credits

Original mod: **Overgeared: A Blacksmith Mod**, by StirDrem. Porting changes must preserve the upstream license and copyright notices; see the source archive's `UPSTREAM-LICENSE-NOTICE.md`.

## Porting source

The source was imported from pinned upstream commit `c898a0ce` plus the earlier porting delta stored under `porting/`. The active build includes `common/` and `fabric/`; `neoforge/` retains historical source and recipe generators. [GitHub Actions](../../actions) runs source checks, the Fabric build, server game tests, and client screen tests for pull requests.

Required runtime dependencies: Fabric Loader 0.20.1 or newer, Fabric API for 26.3, and Forge Config API Port 26.3.1 or newer. The build is pinned in `gradle.properties`. Optional legacy JEI, EMI, and Sable adapters are excluded until compatible ports are available.

Build with `./gradlew :fabric:build`. Run the client checks with `./gradlew :fabric:runClientGameTest` (Linux CI uses Xvfb). Test worlds are disposable; the test configuration accepts the Minecraft EULA for the authorized test environment.

## Priorities

1. Preserve anvils, hammering, alloy furnaces, casting, and forging-quality mechanics.
2. Keep the Java 25 build and dedicated-server gameplay tests passing.
3. Complete client, multiplayer, and survival-progression verification.
4. Only after the port is playable, add mixed-material modular equipment.

Mixed-material casting data and component transfer are preserved and tested. A broader modular equipment system remains planned after the core port is playable; this port does not add a new ore progression.

Use a disposable test world while this remains a draft development build.
