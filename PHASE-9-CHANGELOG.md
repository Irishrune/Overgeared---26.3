# Phase 9 — forging component preservation and optional compatibility boundaries

**Date:** 2026-11-03  
**Status:** Work-in-progress **source**, not a Minecraft 26.3 compiled mod or playable JAR.

## Implemented

1. **Corrected shifted-pattern component transfer.** `ForgingRecipe.assemble` now identifies the matched position of each 1x1, 2x2, or 3x3 forging pattern and maps pattern-relative ingredient indices to their corresponding 3x3 grid slots. Previously a shifted pattern could copy components from unrelated slots. A no-match input retains the historical result-preview behavior but does not transfer component data.
2. **Connected the production crafting path.** `AbstractSmithingAnvilBlockEntity.craftItem` now calls `recipe.assemble(new RecipeWrapper(itemHandler))` **before** ingredient extraction, instead of directly creating the static result. This lets existing `transfer_nbt` flags affect actual forged output rather than only an unused recipe method. Craftsmanship/quality application remains in its original post-assembly order.
3. **Defensive recipe geometry validation.** Forging JSON patterns must be 1–3 cells wide by 1–3 rows, rectangular, contain at least one material, and define all nonblank keys. Otherwise loading fails clearly rather than silently treating an undefined material symbol as empty.
4. **Correct server-side anvil-session lookup.** `ModEvents` now uses `ModItemInteractEvents.playerAnvilPositions` for anvil-distance enforcement. The previous implementation read `AnvilMinigameEvents`' **client-only** state while executing server-player tick code. Removed a duplicate invocation of the distance check per player tick.
5. **Isolated incompatible optional third-party integrations.** Excluded the existing JEI, EMI, and Sable source adapters from the active Fabric 26.3 Gradle compilation target. Those files are still preserved in the archive for future migration, and no JEI/EMI entrypoints are registered. Core Sable-specific distance and water checks now use the original vanilla fallbacks. This deliberately means **no new 26.3 JEI/EMI recipe viewer support** and **no Sable sublevel behavior** yet.

## Verification run

- `python -m unittest discover -s port-tests -p 'test_*.py' -q`: **31 passed** (27 previous + 4 new); source checks include wiring of actual `craftItem` and server-only session map, output recipe JSON shape, and optional-class import boundaries.
- `python port-tests/run.py`: **6 standalone Java tests passed**, including a new Java 21 compile/run of the **actual** `ForgingGridSlots.java` and **35 assertions** for shifted grid indices, malformed shapes, undefined keys, and out-of-bounds slots. The legacy tests for forging score, durability math, registry IDs, equipment materials, and smithing tools remain green.
- `python tools/generate_263_assets.py --check`: **180 generated definitions consistent**.
- `python tools/check_263_namespaces.py`: **0 known stale Yarn-style namespaces**.
- `./gradlew :fabric:compileJava --offline --console=plain`: **failed before compilation** because the Gradle distribution cannot be fetched from `services.gradle.org` (`UnknownHostException`); only JDK 21 is installed locally. No full Java 25/Fabric build completed.

## Not verified

**No Minecraft 26.3 compile, runtime, server startup, recipe codec decoding, data reload, or game-world test** has occurred. Other 1.21.1 APIs remain in core and client files. The class exclusions remove a subset of compile blockers, not all blockers. Existing component merging and multiplayer synchronization must still be tested with a real Minecraft client and dedicated server.

## Next milestone

Run the included GitHub Actions Java 25 compilation workflow (`.github/workflows/fabric-263-build.yml`) in an accessible repository or a compatible network-enabled build environment. Address the actual compiler errors in the **core Fabric target**, then reintroduce compatible JEI/EMI adapters as a separate optional module. Never install this development archive into an existing world.
