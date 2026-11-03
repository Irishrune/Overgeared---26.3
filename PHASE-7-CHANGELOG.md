# Phase 7 — Custom-workstation recipe contract and first immutable outputs

**Status: Work-in-progress SOURCE ONLY. No Java 25 Minecraft build, no playable JAR.**

Based on the user-supplied Overgeared 1.21.1-multiloader source, upstream commit `c898a0ce89b55a311ddf1b89079808a73f3604a1`. This archive includes the prior phases.

## Implemented

1. Added `OvergearedStationRecipe<T extends RecipeInput>` to implement the Minecraft 26.3 recipe contract for custom workstations: `group`, `showNotification`, `placementInfo`, `recipeBookCategory`, and `isSpecial`. Defaults intentionally bypass the *vanilla* recipe book until custom displays and placement are ported. This does **not** reimplement Overgeared's own forging recipe screens.
2. Updated eleven direct custom `Recipe` implementations (forge, casting, fletching, rock knapping, grinding, cooling, both kinds of alloy furnaces, shaped alloy furnace variants, and item-to-tool-type) to use that interface and the now-generic `RecipeSerializer<? extends Recipe<T>>` / `RecipeType<? extends Recipe<T>>` signatures. Retained existing recipe group information in six implementations and the forging notification override (`false` in the upstream file).
3. Ported **five basic immutable recipe outputs** to `ItemStackTemplate`: `CoolingRecipe`, `GrindingRecipe`, `RockKnappingRecipe`, `AlloySmeltingRecipe`, `NetherAlloySmeltingRecipe`. Their codecs now use `ItemStackTemplate.CODEC` / `.STREAM_CODEC`; `assemble` and legacy UI getters create a fresh `ItemStack` at runtime. Recipes with optional or dynamic results (including the core forging recipe) intentionally remain pending.
4. Removed the now-missing `Ingredient.CODEC_NONEMPTY` in the cooling and grinding codecs, using `Ingredient.CODEC` (already non-empty in Minecraft 26.3), and fixed cooling's obsolete/incorrect `NonNullList.of(ingredient)` use.
5. Replaced **49 source-file imports** of `net.minecraft.world.Level` with `net.minecraft.world.level.Level`, plus **15 imports** of `net.minecraft.util.collection.NonNullList` with `net.minecraft.core.NonNullList`. These are verified 26.3 package locations.
6. Added six targeted Python unit tests, including a standalone compilation/runtime test for the actual `OvergearedStationRecipe.java` against deliberately minimal 26.3-style stubs. Added a reproducible `tools/audit_recipe_263.py` scanner and `RECIPE-API-AUDIT.json` recording unresolved source occurrences.

## Verified locally

- `python -m unittest discover -s port-tests -p 'test_*.py' -q` — **22 tests passed**.
- `python port-tests/run.py` — **5 standalone Java regression programs passed** using the available JDK 21.
- `python tools/generate_263_assets.py --check` — **180 generated resource definitions** consistent with their generator.
- `python tools/check_263_namespaces.py` — no known stale Yarn-style package imports.
- `python tools/audit_recipe_263.py` — completed; audit contains locations of remaining legacy code.

**Not verified:** Actual Java 25 `:fabric:compileJava`; real codec parsing against Minecraft registries; client or dedicated-server startup; anvil GUI; gameplay; behavior under an installed modpack. Source tests are intentionally narrow.

## Known next blockers

The audit currently finds **17** `Ingredient.EMPTY` usages in recipe files; this sentinel no longer exists in 26.3, so forging and fletching blank slots need an explicit optional-ingredient representation and corresponding codec migration. There are also **14** remaining `ItemStack.CODEC` occurrences and **21** `ItemStack.STREAM_CODEC` occurrences in recipes, including optional-result paths. Some overlapping occurrences count more than one type. Recipe manager calls and other 1.21.1 methods still need porting. Some affected recipe classes also inherit old vanilla recipe superclass constructors.

The `OvergearedStationRecipe` defaults mark recipes as special/not placeable in the **vanilla** recipe book. That is a temporary compatibility bridge, not a final decision about custom UI or gameplay. It must be reevaluated during in-game testing.

## Primary references (as of 2026-11-03)

- https://www.fabricmc.net/2026/03/14/261.html
- https://mc-packet-reference.netlify.app/26.x/source/net/minecraft/world/item/crafting/recipe/
- https://mc-packet-reference.netlify.app/26.x/source/net/minecraft/world/item/crafting/ingredient/
- https://aldak.netlify.app/javadoc/26.3.x/net/minecraft/world/item/itemstacktemplate
