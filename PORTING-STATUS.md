# Current status — Fabric build, server and client tests pass (2026-11-05)

Work remains in [draft PR #1](https://github.com/Irishrune/Overgeared---26.3/pull/1). `main` is unchanged. The reports below this section describe historical states, not the current compiler status.

## Verified

- At `7fe49cab7e8fc6dd845b8b695751e77a0403d1d9`, [GitHub Actions](https://github.com/Irishrune/Overgeared---26.3/actions/runs/42033479804) passed all 144 Python source tests, the isolated Java checks, generated-resource checks, `:fabric:build :fabric:runGameTest`, and `:fabric:runClientGameTest` under Java 25. The server reports 14 required tests passing (13 Overgeared tests plus the framework's test).
- The server test suite covers world data loading; forging codecs, displays and flags; hammering and assembly; casting; inventory and ownership persistence; copper/steel mining progression; mixed-material cast save/network round trips; transferred quality/creator components without replacing result-item defaults; recipe-book settings; disabled vanilla shortcuts; independent tipping-use counts when brewing; and both alloy furnaces' fuel-container handling and exact input/output quantities.
- Client tests create and render a world, receive unlocked forging recipes, and open all four anvil screens, both alloy furnaces, the casting furnace and drafting table. Anvil output previews and recipe-book toggling pass. Screenshots are included in the client diagnostic artifact. CI uses Mesa software Vulkan rendering under Xvfb.
- Restored 473 generated recipe, advancement, tag and loot definitions from the original providers. `tools/generate_263_gameplay.py --check` checks reproducibility. Item and arrow models are checked by `tools/generate_263_assets.py --check`.

## Port changes

The compilation migrations cover client screens and rendering, recipe-book displays/settings, item-break and sweep callbacks, potion/brewing hooks, and ingredient codecs. Recipe and reload-listener resolution now respects 26.3's registry/component binding lifecycle. Anvil previews are synchronized from the server. Quality and custom material component patches transfer to finished parts while keeping the finished item's own defaults. Legacy text colors now include explicit alpha so workstation and configuration labels remain visible.

The original quantities, blueprint rules, quality mechanics, quenching, polishing, casting mold reuse, vanilla shortcut restrictions, and registry identifiers are retained. Mixed-material casting is covered; a new modular mixed-material equipment system remains future work after the core port is playable.

## Remaining verification and scope

- A real two-player session, minigame timing under network latency, and a complete survival progression are not yet verified.
- The automated client check uses software Vulkan on Linux; Windows GPU rendering, sound, every arrow/potion visual, and the interactive configuration editor still warrant manual playtesting.
- Legacy JEI/EMI/Sable integrations are excluded from the Fabric build. NeoForge is historical source only. Copper armor currently uses the existing vanilla equipment texture fallback; its old custom silhouette is not enabled.
- Use disposable worlds for development testing. No existing user world has been opened or migrated.

---

# Historical status — Phase 26 (2026-11-04)

**Still not a playable Minecraft Fabric 26.3 mod.** All changes remain in draft pull request [#1](https://github.com/Irishrune/Overgeared---26.3/pull/1); no changes to `main` or any actual Minecraft world.

- **Verified Java 25 CI, source SHA `7376b132da1b0dbba863b7bf00cc786d1d6e93ae`: 98/98 source tests pass; full `:fabric:compileJava` reports 512 errors.** This is down from 619 at the start of phases 23–26 and from 780 on the old baseline. The mod still fails to compile.
- Phase 25 uses server-side `Prediction.SERVER_ONLY` for fletching material returns, grinding conversions, polishing and arrow tipping; uses `SwingAnimation.DEFAULT`, event hand equipment slot, and the updated sound event holder types. Forge ownership sync now uses `CompoundTag.store/read` plus `UUIDUtil.CODEC` and null-checked owner IDs. The limited-crafting rule still checks the recipe-book `RecipeHolder.id()` against the current server level's game rules.
- Phase 26 updates fired and unfired casting molds to the `InteractionResult` item-use contract and `TooltipDisplay`/`Consumer<Component>` tooltips. It retains insertion of cast materials, retrieval of contents, forged-quality/type/amount hints, and server-only item drops. Both item source files currently have **zero reported javac errors**.
- New test modules `port-tests/test_phase25_forging_and_fletching.py` and `port-tests/test_phase26_tool_cast_item.py` cover those source invariants.
- **Not yet demonstrated:** launched client/server, valid 26.3 assets and recipes, actual hand-forging and cast interaction in-world, multiplayer session sync, or clean all-project Java compilation. Many renderers, screens, projectile classes, advancement triggers and Mixins still require migration.

Verified CI: https://github.com/Irishrune/Overgeared---26.3/actions/runs/41963174905

---

# Current status — Phases 23–25 (2026-11-04)

**Overgeared is not yet buildable on Fabric 26.3 and must NOT be installed in the Forever World.** Ongoing draft PR: https://github.com/Irishrune/Overgeared---26.3/pull/1

- The Minecraft 26.3 workstation block lifecycle was migrated from removed `onRemove` callbacks to `affectNeighborsAfterRemoval` and block-entity `preRemoveSideEffects`. The casting, alloy and nether-alloy smelters retain automatic vanilla container inventory drops plus explicit stored-XP payout; custom item-handler smithing anvils drop their contents and clear minigame ownership. Updated state properties use `EnumProperty<Direction>`; the falling-anvil neighbour tick and player hammer interaction use the new block signatures.
- Deferred registry holders use the current `Registry.get`/`getValue` and `ResourceKey.identifier()` APIs. Isolated Java registration stubs and assertions were migrated in tandem.
- Hammer hit packet serialization now uses `Identifier.STREAM_CODEC`. On the server, a packet is rejected unless the player is within reach, currently owns an active forge minigame, and holds a smithing hammer. Sound playback uses registered server-defined sound events, not client-provided IDs. These are source-level safeguards; actual networking and multiplayer behavior still require runtime testing.
- Forge recipes retain their armor-only quenching exemption with `EQUIPPABLE` components. Fletching potion lookup uses `Registry.getValue`. Fletching and grinding/knapping/tipping item drop and swing callbacks use 26.3 APIs; forging ownership sync uses `CompoundTag.store/read` with `UUIDUtil.CODEC`, and limited-crafting checks use `RecipeHolder.id()`.
- Regression modules: `port-tests/test_phase23_workstation_blocks.py`, `port-tests/test_phase24_registry_and_hammer_packet.py`, `port-tests/test_phase25_forging_and_fletching.py`.
- **Verified CI milestones:** Phase 23: **84/84** source tests passed, `:fabric:compileJava` still failed with **572** javac diagnostics; phase 24: **88/88** passed, **554** errors; subsequent phase 25 intermediate run: **92/92** passed, **537** javac errors. Phase 25's subsequent UUID-sync updates require fresh CI verification. Compilation counts represent compiler diagnostics, not distinct runtime defects.
- Remaining: real successful build and Minecraft startup; GUI/renderer API updates, recipe-book categories, projectiles, advancements and mixins, plus end-to-end forging, saved data and dedicated-server runtime testing. Keep this draft separate from `main`.

---

# Current status — Phases 20–22 (2026-11-04)

**This is a source migration in draft PR #1, not a runnable Fabric 26.3 mod.** Do not load it into the Forever World.

- Java 25 CI compilation errors have decreased from **732** to **619** (a reduction of 113 javac diagnostics in this batch). The full Minecraft build still fails.
- The old generic `RecipeBookMenu` menu contract was replaced by the 26.3 interface, while retaining the manual 3×3 forging grid, hammer slot, blueprint slot, and forged output slot. Recipe placement now preflights inventory before transferring resources; incomplete placements do not clear the grid. The actual minigame still requires runtime verification.
- `ItemStackHandler`, `BaseSmithingAnvilBlockEntity` and `AbstractSmithingAnvilBlockEntity` now use 26.3 `ValueInput`/`ValueOutput` for inventory, progress, player-ownership and occupied-anvil data. Original inventory `Items`/`Slot`/`Size` and progress keys were preserved, and redundant shadowed owner/session fields were removed.
- All seven anvil and smelter block entity registrations use Fabric's 26.3 `FabricBlockEntityTypeBuilder` with **unchanged registry IDs**.
- The new smithing source tests live in `port-tests/test_phase20_smithing_menu.py`, `port-tests/test_phase21_smithing_persistence.py`, and `port-tests/test_phase22_block_entity_registry.py`.
- **Verified checks:** 79/79 tests passed before the additional phase 22 regression test; this phase 22 test was corrected to support multiline `.build()` and requires latest-branch CI revalidation. The 619-error compiler result was verified before adding/correcting this final test.
- **Not tested:** full compilation, actual client startup, dedicated-server startup, recipe-book UI, block placement/break behavior, or multiplayer forging/ownership.

Key remaining work includes block interaction overrides in 26.3, renderer/UI changes, arrows, advancements, recipe-book categories, network packet APIs, and Mixins.

---

# Current status — Phase 19 (2026-11-04)

**Fabric 26.3 port is NOT yet buildable or playable.** Draft pull request
[#1](https://github.com/Irishrune/Overgeared---26.3/pull/1) carries the latest
API migrations without changing `main`.

- GitHub Actions with Java 25: **70 of 70 Python source regression tests pass**.
- Real `:fabric:compileJava` still fails, but compiler errors declined from
  **769 to 732** after this batch. These are javac diagnostic counts, not 732
  independent gameplay bugs.
- Fabric 26.3 extended menus, registry identifiers, crafting remainders, and
  context-aware cooking-fuel components have been migrated at source level.
- All three smelters use a server block-entity loot context for fuel durations,
  retain container remainders when fuel is exhausted, drop remainders safely for
  stacked fuel, and detect fuel components during hopper / shift-click routing.
- Their saved inventory, burner, cooking progress, and XP fields now use
  `ValueInput` / `ValueOutput` while preserving original field keys.
- New source regression coverage is in
  `port-tests/test_phase18_menu_api.py` and
  `port-tests/test_phase19_cooking_fuel.py`.
- **Not tested in a running client or multiplayer server.** Recipe-book
  integration, smithing interfaces, sprites/renderers, arrows, advancement
  triggers, Mixins, and other migrated APIs require further compilation and
  gameplay verification. Do not load the port into the Forever World.

---

# Current status — Phase 9 (2026-11-03)

**Source-only progress; not yet compilable/verified on Minecraft 26.3.** The live anvil craft path now calls the updated recipe assembly routine, which uses the matched forging-grid location to preserve components correctly. Malformed forge patterns are rejected; server anvil tracking uses server state; and incompatible 1.21.1 JEI/EMI/Sable adapters are excluded from the core build. The full Gradle compile remains blocked locally before compilation. See `PHASE-9-CHANGELOG.md` for tests, scope and remaining blockers.

---

# Current status — Phase 8 (2026-11-03)

**Recipe source-migration milestone complete, but no full Minecraft 26.3 compile or playable JAR.**

The Phase 7 missing-ingredient sentinel, custom recipe result codecs, cooking constructors, custom shapeless crafting and recipe-manager enumeration issues are addressed in the Phase 8 source. A central server recipe lookup guards client paths. The core recipe audit now reports zero occurrences of the removed `Ingredient.EMPTY`, old `ItemStack` result codecs and removed `getAllRecipesFor` call. A broader compile and runtime validation are still blocked; JEI/EMI adapters, client recipe synchronization, Mixins, rendering, network handling and other 1.21.1 APIs remain.

**Verification:** 27 Python source/regression tests, 5 isolated Java tests, 180 asset consistency checks. The Gradle build attempt stopped *before compilation* because `services.gradle.org` could not be resolved and JDK 25 is unavailable. See `PHASE-8-CHANGELOG.md` for a detailed change list and exact known limitations.

---

# Overgeared → Fabric 26.3: actual source port, phase 7

**Current state: Work in progress. NOT a working mod, NOT yet build-verified.**

The 1.21.1-multiloader **upstream source**, not a starter-only template, is included here. Original upstream revision: `c898a0ce89b55a311ddf1b89079808a73f3604a1`, 2026-11-03. The `common/`, `fabric/`, and `neoforge/` source trees and resource assets are retained. Only `:fabric` is an active Gradle module for this phase.

## Phase 7 changes (2026-11-03)

- Implemented the 26.3 custom workstation `Recipe` interface contract for 11 direct recipe classes through a shared bridge.
- Converted five basic recipe outputs to `ItemStackTemplate` codecs, retaining `assemble` to create a stack only when needed.
- Fixed 49 Level package imports and 15 NonNullList package imports, plus obsolete ingredient codec references in cooling and grinding.
- Added six new Python tests, a standalone Java contract compilation smoke test, and a reproducible recipe migration audit.
- **No actual Minecraft 26.3 compile or runtime test yet**. See `PHASE-7-CHANGELOG.md`.

## Phase 6 changes (2026-11-03)

- **Recipe serializer API migration:** Replaced all 19 obsolete nested `implements RecipeSerializer` implementations with static codec holders. `ModRecipeSerializers` now instantiates the Minecraft 26.3 `RecipeSerializer<>(MapCodec, StreamCodec)` record for all **20** registered custom recipes. This is a structural migration; the underlying codecs still reference legacy Minecraft APIs and need further work.
- **Dynamic tool casting:** Replaced the removed `SimpleCraftingRecipeSerializer` with a singleton `MapCodec.unit` / `StreamCodec.unit` registration. The actual `CustomRecipe` implementation still needs full 26.3 adaptation and a game test.
- **Recipe assembly:** Updated the `assemble(input, HolderLookup.Provider)` override to `assemble(input)` in 20 recipes, and updated the two FletchingStationMenu callers. Kept legacy helper methods like `getResultItem` without obsolete Recipe override annotations until their callers are migrated.
- Added two source-contract tests and one **real Java compilation/invocation check** for `ModRecipeSerializers` using isolated 26.3-shaped API stubs. **This is not a Minecraft build** and it does not compile the other recipe classes.
- Full 26.3 compilation, recipe data decoding, multiplayer and actual smithing gameplay are **not verified**.
- See `PHASE-6-CHANGELOG.md`.

## Phase 5 changes (2026-11-03)

- Migrated **10 steel/copper weapon-and-tool factories** and **8 armor factories** to Minecraft 26.3 `Item.Properties` components instead of the old `SwordItem`/`ArmorItem` constructors. Preserved original attack-damage targets by subtracting the material bonus before passing the attack baseline.
- Migrated tool tiers to **`ToolMaterial` records**, and armor definitions to **`ArmorMaterial` records**, preserving their previous durability, mining speed, defense and toughness numbers.
- Migrated **five tongs/smithing-hammer factories** to 26.3 item components; `SmithingHammer.java` and `Tongs.java` now extend `Item` while preserving the original **two durability damage per mined block** and the tongs creative-block rule.
- Added steel/copper repair tags, steel/copper mining-tier tags, and vanilla armor texture fallbacks via new equipment assets. The old custom copper armor renderer is not yet ported; visuals should be checked once the game loads.
- **Corrected a serious flaw in phases 2–4:** those older import scripts used Yarn-style packages, while the Fabric 26.3 unobfuscated source uses Mojang packages. Converted the known incorrect imports back; the official Fabric 26.3 example uses `net.minecraft.resources.Identifier`. Retired the old scripts into `tools/legacy/` as nonexecuting references.
- Added a `tools/check_263_namespaces.py` CI gate to prevent the same mappings mistake. This is only a known-pattern check, not a complete API audit.
- Verified three prior standalone Java tests, two new equipment/smithing Java tests, 13 Python tests, 180 generated resource definitions, and the known namespace check. **Full Java 25/Fabric compilation has not occurred.**
- See `PHASE-5-CHANGELOG.md` for exact details and limitations.

## Implemented in this version

- Updated Minecraft target to `26.3`, Java toolchain to 25, Gradle wrapper target to 9.8.2.
- Changed to **`net.fabricmc.fabric-loom`**, the non-remapping Loom plugin required for modern Minecraft; Loom `1.19-SNAPSHOT`.
- Updated loader `0.20.1`, Fabric API `0.165.0+26.3`, and Forge Config API Port `26.3.1`. These values were checked against the official Fabric 26.3 example project and the config-port maintainer's Maven coordinates.
- Preserved upstream Forge Config API usage for later porting rather than silently deleting it.
- Kept upstream entrypoint and Mixin configs; raised bytecode compatibility declarations to Java 25. **Mixin targets/access widener references are not validated against 26.3.**
- **Historical mapping note:** Phases 2 and 3 attempted broad package and symbol migrations using Yarn names. That approach was **incorrect for the unobfuscated 26.3 target** and the known changes have been reversed in Phase 5. The old audit counters and scripts are retained only for historical context; see `PHASE-5-CHANGELOG.md` for the correction.
- **26.3 item and block resources:** generated 180 resource definitions based on the upstream registrations (90 client item definitions, 81 item geometry definitions, 8 blockstate definitions, 1 steel block model). Original textures and custom block geometry were preserved. `tools/generate_263_assets.py --check` verifies reproducibility. Asset schemas still require a game test.
- **Actually modified upstream Java:** extracted the original minigame's quality scoring into `common/.../util/ForgingScore.java` and replaced the inline `AnvilMinigameEvents.finishForging` formula with that tested helper. This retains upstream hit weights and ranking behavior.
- Extracted `DurabilityMath.java` from the old `ItemStackMixin` while keeping the existing multiplier order and quality/grinding mechanics. Added regression tests for this calculation.
- Added independent Java regression tests (`python port-tests/run.py`) that run without Minecraft dependencies, **plus Python resource, source API, and codec-pair registration tests** (`python -m unittest discover -s port-tests -p 'test_*.py'`). CI now checks those before attempting actual compilation.
- Generated `PORT-AUDIT.md` (API/system risk scanning) and `IMPORT-AUDIT.md` (legacy source imports). See these for the next code-change targets.

## Phase 4 additions

- **Registration IDs (partially ported):** all nine custom block factories and all 32 custom item factories now receive the typed registry key and set it before construction. The simple-item and block-item helpers also do this, and the Fabric platform bridge passes a typed key to `Registry.register`.
- The diamond template factory now uses the mapped 26.3 `SmithingTemplateItem` constructor with `Item.Properties` at the end.
- Added an isolated Java test that compiles the actual deferred-registration source against minimal stubs; and four additional Python source checks. These tests passed but **do not verify actual Minecraft compatibility**.
- For precise diffs, tests and outstanding blockers see `PHASE-4-CHANGELOG.md`.

## Unresolved items that block a working game JAR

1. **Minecraft API packages/classes.** The known accidental Yarn packages were reverted to the Mojang/unobfuscated names used by the official Fabric 26.3 example, including `net.minecraft.world.item.ItemStack`, `net.minecraft.world.level.block.Block` and `net.minecraft.resources.Identifier`. The remaining legacy API signatures, method names, and many classes are not yet validated; no whole-mod compilation has occurred.
2. **Registries and recipes.** The 26.3 `RecipeSerializer` *record-registration layer* has now been migrated, but the 1.21.1 recipe `MapCodec` and `StreamCodec` implementations still contain old `ItemStack` codecs, cooking recipe constructors and recipe-book APIs. 26.3 uses `ItemStackTemplate` in many recipe codecs, and the full recipe method contracts (`placementInfo`, `recipeBookCategory`, etc.), ingredients, loot and reload listeners still require migration. No recipe has been decoded or tested in Minecraft 26.3.
3. **Client rendering/UI.** Anvil overlay, block entities, equipment models, rendering, and input need upgrades; the 26.3 renderer differs substantially.
4. **Third-party APIs.** JEI/EMI/Sable 1.21.1 compatibility hooks are not ported. They are deliberately not pulled in as incompatible binaries. Existing upstream source still references some of them.
5. **Mixin/access widener audit.** Every upstream mixin target and JVM descriptor needs confirmation against 26.3 before launching a server.
6. **End-to-end verification.** A Gradle compile, game loading, dedicated-server boot, and forging gameplay tests remain outstanding.

## Check what works right now

Run the standalone forging, durability, and registry regression tests:

```bash
python port-tests/run.py
python -m unittest discover -s port-tests -p 'test_*.py' -v
python tools/generate_263_assets.py --check
python tools/check_263_namespaces.py
```

With JDK 25 and external Maven connectivity, attempt the **real port build**:

```bash
./gradlew :fabric:compileJava --console=plain --stacktrace
```

**Local actual build attempt (phase 2, still unresolved):** `./gradlew :fabric:compileJava --offline --console=plain` attempted to download Gradle but stopped at `java.net.UnknownHostException: services.gradle.org`. No Java 25 build occurred in this environment. An actual compilation failure is expected until remaining Minecraft APIs are migrated. Do not drop the output JAR into your Forever World. Use a disposable test instance once it builds and initializes.

## Upstream and references

- Overgeared upstream: https://github.com/phuccom000/Overgeared/tree/1.21.1-multiloader
- Official Fabric 26.3 example: https://github.com/FabricMC/fabric-example-mod/tree/26.3
- Fabric Loom docs: https://docs.fabricmc.net/develop/loom/
- New unobfuscated mapping explorer: https://mappings.relativitymc.org/26.3/index.html
- Fabric 26.3 changes: https://www.fabricmc.net/2026/09/15/263.html
- Forge Config API Port 26.3 release: https://github.com/Fuzss/forge-config-api-port

The upstream repository declares the mod license MIT. This archive retains original attribution; downstream resources and integrations may have additional terms. No claim is made that the package is an official Overgeared release.
