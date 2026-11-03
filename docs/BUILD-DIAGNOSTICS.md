# Overgeared Fabric 26.3 — first actual Java 25 compile

**Status:** compilation FAILED; this is a diagnostic development snapshot, not a playable JAR.

**Run:** https://github.com/Irishrune/Overgeared---26.3/actions/runs/41720775097

The GitHub-hosted runner installed Java 25 and Gradle 9.8.2, ran isolated tests successfully, then reached `:fabric:compileJava`. The compiler stopped after reporting its first 100 errors. More errors may be exposed as these are fixed.

## Compiler error concentration

| Class / source | Errors in the first compiler batch |
|---|---:|
| `fabric/src/main/java/net/stirdrem/overgeared/fabric/client/FabricRecipeCategories.java` | 9 |
| `common/src/main/java/net/stirdrem/overgeared/advancement/ModAdvancementTriggers.java` | 7 |
| `fabric/src/main/java/net/stirdrem/overgeared/fabric/FabricTrades.java` | 6 |
| `common/src/main/java/net/stirdrem/overgeared/event/ModEvents.java` | 6 |
| `common/src/main/java/net/stirdrem/overgeared/event/QualityWrappedTrade.java` | 6 |
| `fabric/src/main/java/net/stirdrem/overgeared/fabric/mixin/client/RecipeBookCategoriesMixin.java` | 5 |
| `common/src/main/java/net/stirdrem/overgeared/advancement/ForgingQualityTrigger.java` | 5 |
| `common/src/main/java/net/stirdrem/overgeared/advancement/MakeSmithingAnvilTrigger.java` | 5 |
| `common/src/main/java/net/stirdrem/overgeared/advancement/KnappingAdvancementTrigger.java` | 5 |
| `common/src/main/java/net/stirdrem/overgeared/advancement/BlueprintQualityTrigger.java` | 5 |
| `common/src/main/java/net/stirdrem/overgeared/advancement/MaxLevelBlueprintAdvancementTrigger.java` | 5 |
| `fabric/src/main/java/net/stirdrem/overgeared/fabric/OvergearedFabric.java` | 4 |
| `fabric/src/main/java/net/stirdrem/overgeared/fabric/FabricPlatformHelper.java` | 4 |
| `fabric/src/main/java/net/stirdrem/overgeared/fabric/client/OvergearedFabricClient.java` | 3 |
| `fabric/src/main/java/net/stirdrem/overgeared/fabric/mixin/client/ClientRecipeBookMixin.java` | 2 |
| `common/src/main/java/net/stirdrem/overgeared/event/BlueprintWanderingTrade.java` | 2 |
| `common/src/main/java/net/stirdrem/overgeared/event/ModItemInteractEvents.java` | 2 |
| `common/src/main/java/net/stirdrem/overgeared/event/ForgedItemTrade.java` | 2 |
| `common/src/main/java/net/stirdrem/overgeared/event/SteelEmeraldTrade.java` | 2 |
| `common/src/main/java/net/stirdrem/overgeared/util/TooltipButton.java` | 2 |
| `common/src/main/java/net/stirdrem/overgeared/entity/custom/LingeringArrowEntity.java` | 2 |
| `common/src/main/java/net/stirdrem/overgeared/entity/custom/UpgradeArrowEntity.java` | 2 |
| `fabric/src/main/java/net/stirdrem/overgeared/fabric/mixin/PotionBrewingMixin.java` | 1 |
| `common/src/main/java/net/stirdrem/overgeared/entity/renderer/LingeringArrowEntityRenderer.java` | 1 |
| `common/src/main/java/net/stirdrem/overgeared/entity/renderer/UpgradeArrowEntityRenderer.java` | 1 |
| `common/src/main/java/net/stirdrem/overgeared/block/entity/AbstractSmithingAnvilBlockEntity.java` | 1 |

## First actionable errors by file

### `fabric/src/main/java/net/stirdrem/overgeared/fabric/client/FabricRecipeCategories.java`
- L3: `cannot find symbol` — `import net.minecraft.client.RecipeBookCategories;`
- L15: `cannot find symbol` — `private static RecipeBookCategories search;`
- L16: `cannot find symbol` — `private static RecipeBookCategories tools;`
- L17: `cannot find symbol` — `private static RecipeBookCategories armors;`

### `common/src/main/java/net/stirdrem/overgeared/advancement/ModAdvancementTriggers.java`
- L3: `cannot find symbol` — `import net.minecraft.advancements.CriterionTrigger;`
- L11: `cannot find symbol` — `public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS = DeferredRegister`
- L14: `cannot find symbol` — `public static final DeferredHolder<CriterionTrigger<?>, MakeSmithingAnvilTrigger> MAKE_SMITHING_ANVIL = TRIGGERS`
- L17: `cannot find symbol` — `public static final DeferredHolder<CriterionTrigger<?>, KnappingAdvancementTrigger> KNAPPING = TRIGGERS`

### `fabric/src/main/java/net/stirdrem/overgeared/fabric/FabricTrades.java`
- L6: `cannot find symbol` — `import net.minecraft.world.entity.npc.VillagerProfession;`
- L7: `cannot find symbol` — `import net.minecraft.world.entity.npc.VillagerTrades;`
- L25: `cannot find symbol` — `private static Map<VillagerProfession, Int2ObjectMap<VillagerTrades.ItemListing[]>> originalTrades;`
- L25: `package VillagerTrades does not exist` — `private static Map<VillagerProfession, Int2ObjectMap<VillagerTrades.ItemListing[]>> originalTrades;`

### `common/src/main/java/net/stirdrem/overgeared/event/ModEvents.java`
- L17: `cannot find symbol` — `import net.minecraft.world.entity.npc.VillagerProfession;`
- L18: `cannot find symbol` — `import net.minecraft.world.entity.npc.VillagerTrades;`
- L285: `cannot find symbol` — `public static void onVillagerTrades(VillagerProfession type, Int2ObjectMap<List<VillagerTrades.ItemListing>> tradesByLevel) {`
- L285: `package VillagerTrades does not exist` — `public static void onVillagerTrades(VillagerProfession type, Int2ObjectMap<List<VillagerTrades.ItemListing>> tradesByLevel) {`

### `common/src/main/java/net/stirdrem/overgeared/event/QualityWrappedTrade.java`
- L5: `cannot find symbol` — `import net.minecraft.world.entity.npc.VillagerTrades;`
- L6: `cannot find symbol` — `import net.minecraft.world.item.ArmorItem;`
- L9: `cannot find symbol` — `import net.minecraft.world.item.TieredItem;`
- L16: `package VillagerTrades does not exist` — `public class QualityWrappedTrade implements VillagerTrades.ItemListing {`

### `fabric/src/main/java/net/stirdrem/overgeared/fabric/mixin/client/RecipeBookCategoriesMixin.java`
- L4: `cannot find symbol` — `import net.minecraft.client.RecipeBookCategories;`
- L35: `cannot find symbol` — `private static RecipeBookCategories[] $VALUES;`
- L40: `cannot find symbol` — `public static Map<RecipeBookCategories, List<RecipeBookCategories>> AGGREGATE_CATEGORIES;`
- L43: `cannot find symbol` — `private static RecipeBookCategories overgeared$create(String name, int ordinal, ItemStack... icons) {`

### `common/src/main/java/net/stirdrem/overgeared/advancement/ForgingQualityTrigger.java`
- L5: `package net.minecraft.advancements.critereon does not exist` — `import net.minecraft.advancements.critereon.ContextAwarePredicate;`
- L6: `package net.minecraft.advancements.critereon does not exist` — `import net.minecraft.advancements.critereon.SimpleCriterionTrigger;`
- L11: `cannot find symbol` — `public class ForgingQualityTrigger extends SimpleCriterionTrigger<ForgingQualityTrigger.TriggerInstance> {`
- L26: `package SimpleCriterionTrigger does not exist` — `implements SimpleCriterionTrigger.SimpleInstance {`

### `common/src/main/java/net/stirdrem/overgeared/advancement/MakeSmithingAnvilTrigger.java`
- L5: `package net.minecraft.advancements.critereon does not exist` — `import net.minecraft.advancements.critereon.ContextAwarePredicate;`
- L6: `package net.minecraft.advancements.critereon does not exist` — `import net.minecraft.advancements.critereon.SimpleCriterionTrigger;`
- L11: `cannot find symbol` — `public class MakeSmithingAnvilTrigger extends SimpleCriterionTrigger<MakeSmithingAnvilTrigger.TriggerInstance> {`
- L23: `package SimpleCriterionTrigger does not exist` — `implements SimpleCriterionTrigger.SimpleInstance {`

### `common/src/main/java/net/stirdrem/overgeared/advancement/KnappingAdvancementTrigger.java`
- L5: `package net.minecraft.advancements.critereon does not exist` — `import net.minecraft.advancements.critereon.ContextAwarePredicate;`
- L6: `package net.minecraft.advancements.critereon does not exist` — `import net.minecraft.advancements.critereon.SimpleCriterionTrigger;`
- L11: `cannot find symbol` — `public class KnappingAdvancementTrigger extends SimpleCriterionTrigger<KnappingAdvancementTrigger.TriggerInstance> {`
- L23: `package SimpleCriterionTrigger does not exist` — `implements SimpleCriterionTrigger.SimpleInstance {`

### `common/src/main/java/net/stirdrem/overgeared/advancement/BlueprintQualityTrigger.java`
- L5: `package net.minecraft.advancements.critereon does not exist` — `import net.minecraft.advancements.critereon.ContextAwarePredicate;`
- L6: `package net.minecraft.advancements.critereon does not exist` — `import net.minecraft.advancements.critereon.SimpleCriterionTrigger;`
- L11: `cannot find symbol` — `public class BlueprintQualityTrigger extends SimpleCriterionTrigger<BlueprintQualityTrigger.TriggerInstance> {`
- L26: `package SimpleCriterionTrigger does not exist` — `implements SimpleCriterionTrigger.SimpleInstance {`

### `common/src/main/java/net/stirdrem/overgeared/advancement/MaxLevelBlueprintAdvancementTrigger.java`
- L5: `package net.minecraft.advancements.critereon does not exist` — `import net.minecraft.advancements.critereon.ContextAwarePredicate;`
- L6: `package net.minecraft.advancements.critereon does not exist` — `import net.minecraft.advancements.critereon.SimpleCriterionTrigger;`
- L12: `cannot find symbol` — `extends SimpleCriterionTrigger<MaxLevelBlueprintAdvancementTrigger.TriggerInstance> {`
- L24: `package SimpleCriterionTrigger does not exist` — `implements SimpleCriterionTrigger.SimpleInstance {`

### `fabric/src/main/java/net/stirdrem/overgeared/fabric/OvergearedFabric.java`
- L3: `package fuzs.forgeconfigapiport.fabric.api.neoforge.v4 does not exist` — `import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;`
- L6: `cannot find symbol` — `import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;`
- L27: `cannot find symbol` — `import net.minecraft.world.InteractionResultHolder;`
- L34: `cannot find symbol` — `import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;`

### `fabric/src/main/java/net/stirdrem/overgeared/fabric/FabricPlatformHelper.java`
- L7: `package net.fabricmc.fabric.api.itemgroup.v1 does not exist` — `import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;`
- L9: `cannot find symbol` — `import net.fabricmc.fabric.api.registry.FuelRegistry;`
- L10: `package net.fabricmc.fabric.api.screenhandler.v1 does not exist` — `import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;`
- L11: `package net.fabricmc.fabric.api.screenhandler.v1 does not exist` — `import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;`

### `fabric/src/main/java/net/stirdrem/overgeared/fabric/client/OvergearedFabricClient.java`
- L7: `cannot find symbol` — `import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;`
- L8: `cannot find symbol` — `import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;`
- L10: `cannot find symbol` — `import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;`

### `fabric/src/main/java/net/stirdrem/overgeared/fabric/mixin/client/ClientRecipeBookMixin.java`
- L4: `cannot find symbol` — `import net.minecraft.client.RecipeBookCategories;`
- L16: `cannot find symbol` — `private static void overgeared$forgingCategory(RecipeHolder<?> recipe, CallbackInfoReturnable<RecipeBookCategories> cir) {`

### `common/src/main/java/net/stirdrem/overgeared/event/BlueprintWanderingTrade.java`
- L5: `cannot find symbol` — `import net.minecraft.world.entity.npc.VillagerTrades;`
- L19: `package VillagerTrades does not exist` — `public class BlueprintWanderingTrade implements VillagerTrades.ItemListing {`

### `common/src/main/java/net/stirdrem/overgeared/event/ModItemInteractEvents.java`
- L34: `cannot find symbol` — `import net.minecraft.world.level.GameRules;`
- L77: `package javax.annotation does not exist` — `import javax.annotation.Nullable;`

### `common/src/main/java/net/stirdrem/overgeared/event/ForgedItemTrade.java`
- L5: `cannot find symbol` — `import net.minecraft.world.entity.npc.VillagerTrades;`
- L14: `package VillagerTrades does not exist` — `public class ForgedItemTrade implements VillagerTrades.ItemListing {`

### `common/src/main/java/net/stirdrem/overgeared/event/SteelEmeraldTrade.java`
- L5: `cannot find symbol` — `import net.minecraft.world.entity.npc.VillagerTrades;`
- L12: `package VillagerTrades does not exist` — `public class SteelEmeraldTrade implements VillagerTrades.ItemListing {`

### `common/src/main/java/net/stirdrem/overgeared/util/TooltipButton.java`
- L6: `cannot find symbol` — `import net.minecraft.client.gui.GuiGraphics;`
- L21: `cannot find symbol` — `public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {`

### `common/src/main/java/net/stirdrem/overgeared/entity/custom/LingeringArrowEntity.java`
- L15: `cannot find symbol` — `import net.minecraft.world.entity.projectile.AbstractArrow;`
- L26: `cannot find symbol` — `public class LingeringArrowEntity extends AbstractArrow {`

### `common/src/main/java/net/stirdrem/overgeared/entity/custom/UpgradeArrowEntity.java`
- L17: `cannot find symbol` — `import net.minecraft.world.entity.projectile.AbstractArrow;`
- L33: `cannot find symbol` — `public class UpgradeArrowEntity extends AbstractArrow {`

### `fabric/src/main/java/net/stirdrem/overgeared/fabric/mixin/PotionBrewingMixin.java`
- L6: `cannot find symbol` — `import net.minecraft.world.item.alchemy.PotionBrewing;`

### `common/src/main/java/net/stirdrem/overgeared/entity/renderer/LingeringArrowEntityRenderer.java`
- L9: `wrong number of type arguments; required 2` — `public class LingeringArrowEntityRenderer extends ArrowRenderer<LingeringArrowEntity> {`

### `common/src/main/java/net/stirdrem/overgeared/entity/renderer/UpgradeArrowEntityRenderer.java`
- L10: `wrong number of type arguments; required 2` — `public class UpgradeArrowEntityRenderer extends ArrowRenderer<UpgradeArrowEntity> {`

### `common/src/main/java/net/stirdrem/overgeared/block/entity/AbstractSmithingAnvilBlockEntity.java`
- L18: `cannot find symbol` — `import net.minecraft.world.item.ArmorItem;`

## Recommended sequence

1. Reconcile Fabric API and removed vanilla class symbols in the Fabric entry point and platform helper.
2. Port or temporarily isolate the recipe-book extension and its Mixins without breaking forging recipe lookup.
3. Migrate villager trades and advancement triggers to the new registry/data APIs.
4. Re-run the full compiler with a larger `-Xmaxerrs` limit after the first symbol category is cleared.
5. Restore optional compatibility adapters (JEI/EMI) only after core compilation and runtime checks.

**Do not install into Forever World until client and dedicated-server startup plus forging gameplay pass.**


## Expanded Java 25 compile diagnostics (November 3, 2026)

- **Run:** https://github.com/Irishrune/Overgeared---26.3/actions/runs/41736205157
- **Source regression tests:** passed.
- **Minecraft/Fabric Java compilation:** failed with **899 reported errors** after increasing javac's error limit to 1,000 (previous runs only surfaced the first 100).
- The first wave centers on missing/changed Fabric APIs and vanilla 26.3 classes; substantial migrations remain for anvil menus, item interactions, rendering, custom arrows, villager trades and recipe-book screens.

Most affected classes in the compilation output include `UpgradeArrowEntity`, `AbstractSmithingAnvilMenu`, `OvergearedConfigScreen`, `LingeringArrowEntity` and `AbstractSmithingAnvilBlockEntity`. These counts are a compiler snapshot, **not** a progress estimate: fixing shared imports or interfaces may resolve many downstream errors.

The subsequent commits have migrated:
1. `UseItemCallback` to return `InteractionResult` instead of the removed `InteractionResultHolder`;
2. creative-tab factory invocation to `CreativeModeTab.builder()`;
3. Forge Config API Port registration from Fabric's obsolete v4 class to `fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry` (confirmed against the dependency's 26.3.x source);
4. javac settings to show up to 1,000 errors and added source regression tests.

The next actionable step is **compiler-driven, feature-preserving migrations**, not installing the JAR. No playable or tested release exists.
