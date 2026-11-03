# Overgeared Fabric 26.3 static port audit

**Not a compilation report.** These are risk indicators from source scanning only.

- Java source files scanned (common + fabric): **252**
- Total category-matches: **1164**

The first real verification is the Java 25 Gradle compilation; fix compiler errors before runtime testing.

| Review area | Matching lines |
|---|---:|
| rendering / UI (26.x renderer refactor) | 149 |
| SDL input migration | 0 |
| recipe API / dynamic registries | 350 |
| config bridging (Forge Config API Port) | 133 |
| item stacks and data components | 424 |
| worldgen and loot | 8 |
| Fabric event and networking API | 37 |
| optional integrations lacking verified 26.3 builds | 63 |

## rendering / UI (26.x renderer refactor)

Examples (10 of 149):

- `common/src/main/java/net/stirdrem/overgeared/block/entity/renderer/SmithingAnvilBlockEntityRenderer.java:3` — `import com.mojang.blaze3d.vertex.PoseStack;`
- `common/src/main/java/net/stirdrem/overgeared/block/entity/renderer/SmithingAnvilBlockEntityRenderer.java:6` — `import net.minecraft.client.renderer.LightTexture;`
- `common/src/main/java/net/stirdrem/overgeared/block/entity/renderer/SmithingAnvilBlockEntityRenderer.java:7` — `import net.minecraft.client.renderer.MultiBufferSource;`
- `common/src/main/java/net/stirdrem/overgeared/block/entity/renderer/SmithingAnvilBlockEntityRenderer.java:8` — `import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;`
- `common/src/main/java/net/stirdrem/overgeared/block/entity/renderer/SmithingAnvilBlockEntityRenderer.java:9` — `import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;`
- `common/src/main/java/net/stirdrem/overgeared/block/entity/renderer/SmithingAnvilBlockEntityRenderer.java:10` — `import net.minecraft.client.renderer.entity.ItemRenderer;`
- `common/src/main/java/net/stirdrem/overgeared/block/entity/renderer/SmithingAnvilBlockEntityRenderer.java:11` — `import net.minecraft.client.renderer.texture.OverlayTexture;`
- `common/src/main/java/net/stirdrem/overgeared/client/AnvilMinigameOverlay.java:3` — `import com.mojang.blaze3d.systems.RenderSystem;`
- `common/src/main/java/net/stirdrem/overgeared/client/AnvilMinigameOverlay.java:5` — `import net.minecraft.client.gui.GuiGraphics;`
- `common/src/main/java/net/stirdrem/overgeared/client/AnvilMinigameOverlay.java:6` — `import net.minecraft.client.gui.LayeredDraw;`

## recipe API / dynamic registries

Examples (10 of 350):

- `common/src/main/java/net/stirdrem/overgeared/ForgingQuality.java:13` — `import net.stirdrem.overgeared.datapack.QualityAttributeReloadListener;`
- `common/src/main/java/net/stirdrem/overgeared/ForgingQuality.java:197` — `boolean affected = QualityAttributeReloadListener.INSTANCE`
- `common/src/main/java/net/stirdrem/overgeared/OvergearedMod.java:5` — `import net.minecraft.world.inventory.RecipeBookType;`
- `common/src/main/java/net/stirdrem/overgeared/OvergearedMod.java:17` — `import net.stirdrem.overgeared.recipe.ModRecipeSerializers;`
- `common/src/main/java/net/stirdrem/overgeared/OvergearedMod.java:18` — `import net.stirdrem.overgeared.recipe.ModRecipeTypes;`
- `common/src/main/java/net/stirdrem/overgeared/OvergearedMod.java:33` — `public static final RecipeBookType FORGING = RecipeBookType.valueOf("OVERGEARED_FORGING");`
- `common/src/main/java/net/stirdrem/overgeared/OvergearedMod.java:47` — `ModRecipeTypes.register();`
- `common/src/main/java/net/stirdrem/overgeared/OvergearedMod.java:48` — `ModRecipeSerializers.register();`
- `common/src/main/java/net/stirdrem/overgeared/block/entity/AlloySmelterBlockEntity.java:26` — `import net.minecraft.world.item.crafting.RecipeType;`
- `common/src/main/java/net/stirdrem/overgeared/block/entity/AlloySmelterBlockEntity.java:35` — `import net.stirdrem.overgeared.recipe.ModRecipeTypes;`

## config bridging (Forge Config API Port)

Examples (10 of 133):

- `common/src/main/java/net/stirdrem/overgeared/ForgingQuality.java:10` — `import net.neoforged.neoforge.common.ModConfigSpec;`
- `common/src/main/java/net/stirdrem/overgeared/ForgingQuality.java:30` — `private final Supplier<ModConfigSpec.IntValue> blueprintUsesConfig;`
- `common/src/main/java/net/stirdrem/overgeared/ForgingQuality.java:45` — `Supplier<ModConfigSpec.IntValue> blueprintUsesConfig) {`
- `common/src/main/java/net/stirdrem/overgeared/client/OvergearedConfigScreen.java:13` — `import net.neoforged.neoforge.common.ModConfigSpec;`
- `common/src/main/java/net/stirdrem/overgeared/client/OvergearedConfigScreen.java:26` — `private ModConfigSpec configSpec;`
- `common/src/main/java/net/stirdrem/overgeared/config/ClientConfig.java:3` — `import net.neoforged.neoforge.common.ModConfigSpec;`
- `common/src/main/java/net/stirdrem/overgeared/config/ClientConfig.java:7` — `public static final ModConfigSpec CLIENT_CONFIG;`
- `common/src/main/java/net/stirdrem/overgeared/config/ClientConfig.java:9` — `public static final ModConfigSpec.IntValue MINIGAME_OVERLAY_HEIGHT;`
- `common/src/main/java/net/stirdrem/overgeared/config/ClientConfig.java:10` — `public static final ModConfigSpec.BooleanValue POP_UP_TOGGLE;`
- `common/src/main/java/net/stirdrem/overgeared/config/ClientConfig.java:11` — `public static final ModConfigSpec.BooleanValue ENABLE_ANVIL_RECIPE_BOOK;`

## item stacks and data components

Examples (10 of 424):

- `common/src/main/java/net/stirdrem/overgeared/block/custom/StoneSmithingAnvil.java:137` — `Block.popResource(level, pos, new ItemStack(net.minecraft.world.item.Items.COBBLESTONE));`
- `common/src/main/java/net/stirdrem/overgeared/block/entity/AbstractSmithingAnvilBlockEntity.java:64` — `protected ItemStack lastBlueprint = ItemStack.EMPTY;`
- `common/src/main/java/net/stirdrem/overgeared/block/entity/AbstractSmithingAnvilBlockEntity.java:347` — `} else if (ItemStack.isSameItemSameComponents(existing, result)) {`
- `common/src/main/java/net/stirdrem/overgeared/block/entity/AbstractSmithingAnvilBlockEntity.java:432` — `return ItemStack.isSameItem(result, failedResult);`
- `common/src/main/java/net/stirdrem/overgeared/block/entity/AbstractSmithingAnvilBlockEntity.java:541` — `return (existing.isEmpty() \|\| ItemStack.isSameItemSameComponents(existing, stackToInsert));`
- `common/src/main/java/net/stirdrem/overgeared/block/entity/AbstractSmithingAnvilBlockEntity.java:588` — `if (!ItemStack.isSameItemSameComponents(currentBlueprint, lastBlueprint)) {`
- `common/src/main/java/net/stirdrem/overgeared/block/entity/AbstractSmithingAnvilBlockEntity.java:615` — `recipeChanged = !ItemStack.isSameItemSameComponents(currentResult, lastResult);`
- `common/src/main/java/net/stirdrem/overgeared/block/entity/AbstractSmithingAnvilBlockEntity.java:1062` — `itemHandler.setStackInSlot(slot, ItemStack.EMPTY);`
- `common/src/main/java/net/stirdrem/overgeared/block/entity/AbstractSmithingAnvilBlockEntity.java:1089` — `itemHandler.setStackInSlot(i, ItemStack.EMPTY);`
- `common/src/main/java/net/stirdrem/overgeared/block/entity/AlloySmelterBlockEntity.java:45` — `private final NonNullList<ItemStack> inventory = NonNullList.withSize(inventorySize, ItemStack.EMPTY);`

## worldgen and loot

Examples (8 of 8):

- `fabric/src/main/java/net/stirdrem/overgeared/fabric/OvergearedFabric.java:13` — `import net.fabricmc.fabric.api.loot.v3.LootTableEvents;`
- `fabric/src/main/java/net/stirdrem/overgeared/fabric/OvergearedFabric.java:31` — `import net.minecraft.world.level.storage.loot.LootPool;`
- `fabric/src/main/java/net/stirdrem/overgeared/fabric/OvergearedFabric.java:134` — `// NeoForge adds these through global loot modifiers; the forging quality is applied in LootTableMixin.`
- `fabric/src/main/java/net/stirdrem/overgeared/fabric/OvergearedFabric.java:136` — `LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {`
- `fabric/src/main/java/net/stirdrem/overgeared/fabric/OvergearedFabric.java:139` — `tableBuilder.withPool(LootPool.lootPool()`
- `fabric/src/main/java/net/stirdrem/overgeared/fabric/mixin/LootTableMixin.java:7` — `import net.minecraft.world.level.storage.loot.LootTable;`
- `fabric/src/main/java/net/stirdrem/overgeared/fabric/mixin/LootTableMixin.java:15` — `@Mixin(LootTable.class)`
- `fabric/src/main/java/net/stirdrem/overgeared/fabric/mixin/LootTableMixin.java:16` — `public abstract class LootTableMixin {`

## Fabric event and networking API

Examples (10 of 37):

- `fabric/src/main/java/net/stirdrem/overgeared/fabric/FabricPlatformHelper.java:6` — `import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;`
- `fabric/src/main/java/net/stirdrem/overgeared/fabric/FabricPlatformHelper.java:7` — `import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;`
- `fabric/src/main/java/net/stirdrem/overgeared/fabric/FabricPlatformHelper.java:8` — `import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;`
- `fabric/src/main/java/net/stirdrem/overgeared/fabric/FabricPlatformHelper.java:9` — `import net.fabricmc.fabric.api.registry.FuelRegistry;`
- `fabric/src/main/java/net/stirdrem/overgeared/fabric/FabricPlatformHelper.java:10` — `import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;`
- `fabric/src/main/java/net/stirdrem/overgeared/fabric/FabricPlatformHelper.java:11` — `import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;`
- `fabric/src/main/java/net/stirdrem/overgeared/fabric/FabricPlatformHelper.java:110` — `ClientPlayNetworking.send(payload);`
- `fabric/src/main/java/net/stirdrem/overgeared/fabric/FabricPlatformHelper.java:115` — `ServerPlayNetworking.send(player, payload);`
- `fabric/src/main/java/net/stirdrem/overgeared/fabric/FabricPlatformHelper.java:125` — `ServerPlayNetworking.send(player, payload);`
- `fabric/src/main/java/net/stirdrem/overgeared/fabric/FabricTrades.java:5` — `import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;`

## optional integrations lacking verified 26.3 builds

Examples (10 of 63):

- `common/src/main/java/net/stirdrem/overgeared/block/entity/AbstractSmithingAnvilBlockEntity.java:35` — `import net.stirdrem.overgeared.compat.polymorph.Polymorph;`
- `common/src/main/java/net/stirdrem/overgeared/block/entity/AbstractSmithingAnvilBlockEntity.java:525` — `Optional<RecipeHolder<ForgingRecipe>> polymorphRecipe = Polymorph.getSelectedRecipe(this, recipeInput);`
- `common/src/main/java/net/stirdrem/overgeared/block/entity/AbstractSmithingAnvilBlockEntity.java:526` — `if (polymorphRecipe.isPresent()) {`
- `common/src/main/java/net/stirdrem/overgeared/block/entity/AbstractSmithingAnvilBlockEntity.java:527` — `return polymorphRecipe.filter(holder -> matchesRecipeExactly(holder.value()));`
- `common/src/main/java/net/stirdrem/overgeared/compat/ModCompat.java:3` — `import net.stirdrem.overgeared.compat.polymorph.Polymorph;`
- `common/src/main/java/net/stirdrem/overgeared/compat/emi/AbstractAlloySmeltingEmiRecipe.java:1` — `package net.stirdrem.overgeared.compat.emi;`
- `common/src/main/java/net/stirdrem/overgeared/compat/emi/AlloySmeltingEmiRecipe.java:1` — `package net.stirdrem.overgeared.compat.emi;`
- `common/src/main/java/net/stirdrem/overgeared/compat/emi/BlueprintCloningEmiRecipe.java:1` — `package net.stirdrem.overgeared.compat.emi;`
- `common/src/main/java/net/stirdrem/overgeared/compat/emi/CastingEmiRecipe.java:1` — `package net.stirdrem.overgeared.compat.emi;`
- `common/src/main/java/net/stirdrem/overgeared/compat/emi/CoolingEmiRecipe.java:1` — `package net.stirdrem.overgeared.compat.emi;`

## Resources

- Upstream recipe JSON files present: **69**
- **Manual verification required:** recipe formats, loot tables, item models and tooltips in 26.3.
- **Manual verification required:** Mixins and access widener targets; compile may not catch all runtime issues.
- **Manual verification required:** SDL input, recipe-book extensions and all screen renderers.
- **Manual verification required:** multiplayer item sync and forging minigame packet validation.
