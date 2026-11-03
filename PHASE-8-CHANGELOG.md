# Phase 8 — custom recipe migration, source-only

**Status:** All recipe blockers identified in Phase 7 have been addressed at the source level, but this is **NOT a buildable or playable mod proven against Minecraft 26.3**. A complete `:fabric:compileJava`, real codec parsing, client/server startup and world gameplay are all unverified. Do not install on the Forever World.

This revision is cumulative from Phase 7 and preserves the user's uploaded upstream `1.21.1-multiloader` project and prior phase changes.

## Implemented in this phase

1. **Empty slots:** Replaced removed `Ingredient.EMPTY` references inside the core recipe implementations with `Optional<Ingredient>` / `ForgingIngredient.EMPTY`. Shaped alloy recipes use `Ingredient.testOptionalIngredient` on each cell, so a blank requires an empty slot (not a wildcard). Forging patterns still require blank surrounding slots and record `requires_heated`/`transfer_nbt` attributes. Fletching's missing tip/shaft/feather require empty slots; a missing potion is intentionally unrestricted to preserve the original optional-potion behavior.
2. **Immutable results:** Migrated forge, fletching, both shaped alloy furnaces, casting, custom shapeless crafting and five cooking subclasses to `ItemStackTemplate` with modern record/stream codecs. Optional failure/tipped/lingering outputs remain optional and are turned into fresh stacks on assembly. Cast smelting/blasting retains heated flags, creator metadata, cast contents and quality transfer code. Custom shapeless crafting now extends `NormalCraftingRecipe` to accommodate the 26.3 superclass API.
3. **Recipe manager:** Introduced `RecipeLookups` for server-side `ServerLevel.recipeAccess()` and `RecipeManager.recipeMap().byType(...)`. Removed all uses of `getAllRecipesFor(...)` across common + Fabric Java sources. Added null-safe enumeration and client menu guards in affected furnace, knapping and fletching paths. The JEI client recipe list now returns early rather than dereferencing the absent server recipe manager; viewer integration is **not finished** and must be redesigned for 26.3.
4. **Ingredient preview migration:** Introduced `IngredientDisplay` to convert the new `Ingredient.items()` holder stream to preview stacks. Updated thirteen usages across recipe metadata, blacklists and viewer code. This helper is **for display only**, never for recipe matching.
5. **Miscellaneous:** Preserved recipe group/notification metadata, corrected some stale `isClientSide` field access and old method references, preserved optional ingredient visualizations in the JEI/EMI adapters (the viewer adapters themselves are not yet ported), and updated cast recipe display ingredients.

## Verification executed here

- `python -m unittest discover -s port-tests -p 'test_*.py' -q`: **27 Python tests passed** (includes Java 21 compilation/runtime of isolated recipe contract and recipe-manager helpers against small API-shaped stubs).
- `python port-tests/run.py`: **5 other isolated Java tests passed**.
- `python tools/generate_263_assets.py --check`: **180 generated assets consistent**.
- `python tools/check_263_namespaces.py`: **0 known stale Yarn-style imports**.
- `python tools/audit_recipe_263.py`: **0** uses of `Ingredient.EMPTY`, `ItemStack.CODEC`, `ItemStack.STREAM_CODEC`, `ItemStack.OPTIONAL_STREAM_CODEC`, or `getAllRecipesFor` inside **core recipe Java files**. The audit was corrected to avoid counting `ForgingIngredient.EMPTY` as an old sentinel. `RecipeHolder` remains valid and is not a failure.
- All JSON files under `common/src/main/resources/data/**/recipe(s)/**/*.json` parsed as JSON by structural tests. **They were not decoded with Minecraft's real codecs.**
- Attempted `./gradlew :fabric:compileJava --offline --console=plain`: Gradle wrapper stopped before compilation at `UnknownHostException: services.gradle.org`; this runtime has Java **21**, not 25, and cannot download Gradle/dependencies.

## Important remaining blockers and known compromises

- **Actual compile is still missing.** Source-based tests and simplified stub compilation do not prove correct type signatures, API behavior or runtime compatibility. Many legacy methods/imports remain outside the scope of this pass, especially mixins, platform events, config, client screens and rendering.
- **26.3 client recipe access** no longer exposes the server recipe manager. JEI/EMI adapters and the local anvil minigame's client recipe knowledge require explicit design work (packet synchronization/client displays). Guarding null prevents one class of crash but does not make the recipe viewers functional.
- **Viewer-only old crafting constructs:** `compat/jei/ToolCastRecipeMaker.java` still uses obsolete `Ingredient.EMPTY`, old `ShapedRecipePattern` and `ShapedRecipe` constructors. This is NOT core forge casting but WILL be a compile blocker if this adapter is compiled. JEI/EMI source integrations are not yet migrated to 26.3.
- The cast-recipe viewer ingredient is temporarily a generic usable-cast item/tag rather than a dynamic cast with specific components; custom matching logic is unchanged.
- Existing upstream recipes, item components, serializers, network mixins, server lifecycle, access wideners, resource-pack formats, texture rendering, and vanilla recipe formats all require a full compile/launch/data-reload validation. Some external mods/libraries may not yet offer 26.3 artifacts.
- `IngredientDisplay` only creates visual item previews. It may not preserve complex custom-ingredient component filters; these still need viewer-specific validation.

## Next verifiable milestone

On a Java 25 machine with network and dependency access: run `./gradlew :fabric:compileJava --console=plain --stacktrace` and supply the **first real compiler report**. Fix compiler errors in dependency/registry/recipe layers before attempting a dedicated server and test world. Maintain the user's existing Forever World untouched.

Source references: Minecraft 26.3 `RecipeManager`, `RecipeMap`, `ItemStackTemplate`, `Ingredient`, `ShapedRecipe`, `ShapedRecipePattern` API documentation; upstream Overgeared GitHub MIT-license declaration. See `PORTING-STATUS.md` and prior phase changelogs for history.
