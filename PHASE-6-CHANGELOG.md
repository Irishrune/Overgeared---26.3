# Phase 6 — Recipe serializer and assembly API migration

**Status: source port in progress. No playable mod.**

Original upstream: `phuccom000/Overgeared` `1.21.1-multiloader` commit `c898a0ce89b55a311ddf1b89079808a73f3604a1`.

## Changed

1. **20 recipe registrations** in `ModRecipeSerializers.java` now use `new RecipeSerializer<>(codec, streamCodec)`, as required since Minecraft 26.1 and confirmed for 26.3. The removed `SimpleCraftingRecipeSerializer` is no longer used.
2. **19 nested codec holders** were converted from obsolete `implements RecipeSerializer<...>` classes to ordinary `Serializer` utility classes, retaining their 1.21.1 codec expressions for subsequent adaptation. `ForgingRecipe` and `ShapedAlloySmeltingRecipe` previously generated codecs in accessor methods; they now expose static codec constants.
3. `DynamicToolCastRecipe` defines a `MapCodec.unit` and `StreamCodec.unit` around a singleton, matching the 26.3 representation for a special crafting recipe. Its remaining inheritance and result handling still need migration.
4. **20 recipe assembly overrides** now use the `assemble(input)` signature verified in the 26.3 `Recipe` interface. Two calls in `FletchingStationMenu` were updated. Legacy helpers like `getResultItem` remain for the current Overgeared UI code but no longer claim to override `Recipe`.
5. Added tests for duplicate codec fields, recipe registration coverage, new assembly signatures, and an isolated compilation and runtime smoke test that compiles the *real* `ModRecipeSerializers.java` against small record-shaped API stubs.

## Verification on the source snapshot

- `python port-tests/run.py` → five standalone Java programs passed.
- `python -m unittest discover -s port-tests -p 'test_*.py' -v` → 16 tests passed, including isolated record-shaped registry compilation.
- `python tools/generate_263_assets.py --check` → 180 generated resources consistent.
- `python tools/check_263_namespaces.py` → zero known stale Yarn-namespace references.

**Not verified:** Gradle `:fabric:compileJava` on JDK 25, actual recipe serialization/decoding, game initialization, rendering, multiplayer and smithing. Gradle resolution in this environment fails due to DNS/network restrictions. No claim is made that the mod can run.

## Remaining high-priority migrations

- Replace recipe-output codecs that still use the 1.21.1 `ItemStack` conventions with the 26.3 `ItemStackTemplate` and rework cooking/crafting constructors.
- Implement the remaining modern `Recipe` contract, placement metadata, and data-driven recipe loading, keeping recipes compatible with the original alloy and forge workstations.
- Validate Fabric mixins/access wideners, block-entity registration, and item components against the real 26.3 game libraries.
- Port client-side anvil renderer and minigame network/UI.
- Run the included GitHub Actions Java 25 build to generate the first complete compiler report, then iterate until compilation and dedicated-server start succeed.

**References:**
- https://www.fabricmc.net/2026/03/14/261.html
- https://aldak.netlify.app/javadoc/26.3.x/net/minecraft/world/item/crafting/recipeserializer
- https://aldak.netlify.app/javadoc/26.3.x/net/minecraft/world/item/crafting/recipe
