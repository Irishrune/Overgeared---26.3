# Phase 5 — Equipment API and namespace correction (2026-11-03)

**This is source code in progress. It is not a playable JAR.**

## 26.3 equipment changes

1. Replaced legacy `Tier` enum implementation with two `ToolMaterial` records (steel and copper), preserving durability 500/190, speed 7/5, material attack bonuses 3/1 and enchantability 12.
2. Replaced `Holder<ArmorMaterial>` / `ArmorItem.Type` usage with armor material records + `ArmorType`, preserving original defense values and durability bases (steel 26 and copper 10). Generated data tags for their repair ingredients.
3. Replaced 10 old `SwordItem`, `AxeItem`, `PickaxeItem`, `HoeItem`, `ShovelItem` registrations with `Item.Properties.{sword,axe,pickaxe,hoe,shovel}`; preserved their *intended* existing attack damage targets using a tested helper, but actual live combat values still require validation.
4. Replaced eight `ArmorItem` registrations with `Item.Properties.humanoidArmor`. Defined steel and copper equipment assets referencing **vanilla iron/copper armor textures as temporary fallbacks**. The removed custom copper armor renderer has not yet been ported; original model classes are retained for later work.
5. Converted SmithingHammer and Tongs to item components and retained two durability points per nonzero-hardness block mined. Tongs still prevent creative players from breaking blocks through the tool's custom check.
6. Added `repairs_steel_tools`, `repairs_copper_tools`, and respective mining-tier tags; steel uses iron-equivalent mining restrictions, and copper uses stone-equivalent restrictions until playtesting determines the intended progression.

## Critical namespace correction

The earlier Phase 2/3 source migration incorrectly treated Yarn names (`net.minecraft.item.Item`, `net.minecraft.util.Identifier`, etc.) as the unobfuscated Fabric 26.3 API. The official Fabric 26.3 example project imports `net.minecraft.resources.Identifier`, and 26.3 javadocs use the Mojang-style item and world packages. This phase reverses the known incorrect import/qualified-class transformations in common and Fabric source. Old scripts are archived in `tools/legacy/` and must **not** be run.

This fixes known **package naming** errors, **not** changed methods, constructors, recipe serializers, networking or Mixin targets.

## Tests completed here

- `python port-tests/run.py` — **5 isolated Java test programs passed**: forging score, durability math, deferred registry, equipment material/stat data, and smithing-hammer/tongs behavior. The latter tests compile source against intentionally lightweight stubs, not actual Minecraft.
- `python -m unittest discover -s port-tests -p 'test_*.py' -v` — **13 tests passed**.
- `python tools/generate_263_assets.py --check` — **180 generated definitions match source**.
- `python tools/check_263_namespaces.py` — **0 instances of selected known stale namespaces**.
- No JDK 25 available locally. Full Gradle/Fabric compile, client startup, dedicated-server startup and gameplay were **not** tested.

## Outstanding blockers

- Migration of 26.3 recipe serializers and data registries, and obsolete custom menu logic.
- Replacement of 1.21.1 networking packets/events and client HUD/rendering.
- Repair of remaining old method names and constructor signatures, including within older code not touched this phase.
- Audit of Mixins and access widener for 26.3.
- Revisit all stat formulas and textures during actual playtests (temporarily inherited vanilla armor textures).
- Source-level namespace correction is still not an end-to-end import/API verifier.

**Do not install this development archive in the production Forever World.**

Reference: Fabric 26.3 example — https://github.com/FabricMC/fabric-example-mod/tree/26.3
