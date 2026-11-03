# Import migration inventory — Overgeared Fabric 26.3

This is a **source import inventory**, not an error count or a successful compile.
Some imports are counted in more than one conceptual area in regular discussions; this
inventory assigns each import to the first matching group. It does NOT rewrite source.

- Java files inspected: **253**
- Count of direct Minecraft imports with old package paths in selected groups: **618**

| Group | Imports |
|---|---:|
| items / crafting | 151 |
| worlds / entity APIs | 131 |
| registries / resources / positions | 125 |
| blocks / block entities | 86 |
| rendering / client UI | 86 |
| text / NBT / network | 39 |

## Examples for porting

### items / crafting

- `common/src/main/java/net/stirdrem/overgeared/OvergearedMod.java` — `net.minecraft.world.inventory.RecipeBookType`
- `common/src/main/java/net/stirdrem/overgeared/client/ForgingRecipeBookComponent.java` — `net.minecraft.world.inventory.Slot`
- `common/src/main/java/net/stirdrem/overgeared/client/OvergearedClient.java` — `net.minecraft.world.item.alchemy.PotionContents`
- `common/src/main/java/net/stirdrem/overgeared/platform/IPlatformHelper.java` — `net.minecraft.world.inventory.AbstractContainerMenu`
- `common/src/main/java/net/stirdrem/overgeared/platform/IPlatformHelper.java` — `net.minecraft.world.inventory.MenuType`

### worlds / entity APIs

- `common/src/main/java/net/stirdrem/overgeared/advancement/ForgingQualityTrigger.java` — `net.minecraft.server.level.ServerPlayer`
- `common/src/main/java/net/stirdrem/overgeared/advancement/MakeSmithingAnvilTrigger.java` — `net.minecraft.server.level.ServerPlayer`
- `common/src/main/java/net/stirdrem/overgeared/advancement/KnappingAdvancementTrigger.java` — `net.minecraft.server.level.ServerPlayer`
- `common/src/main/java/net/stirdrem/overgeared/advancement/BlueprintQualityTrigger.java` — `net.minecraft.server.level.ServerPlayer`
- `common/src/main/java/net/stirdrem/overgeared/advancement/MaxLevelBlueprintAdvancementTrigger.java` — `net.minecraft.server.level.ServerPlayer`

### registries / resources / positions

- `common/src/main/java/net/stirdrem/overgeared/advancement/ModAdvancementTriggers.java` — `net.minecraft.core.registries.Registries`
- `common/src/main/java/net/stirdrem/overgeared/block/UpgradeArrowDispenseBehavior.java` — `net.minecraft.core.Position`
- `common/src/main/java/net/stirdrem/overgeared/block/UpgradeArrowDispenseBehavior.java` — `net.minecraft.core.dispenser.BlockSource`
- `common/src/main/java/net/stirdrem/overgeared/block/UpgradeArrowDispenseBehavior.java` — `net.minecraft.core.dispenser.DefaultDispenseItemBehavior`
- `common/src/main/java/net/stirdrem/overgeared/sound/ModSounds.java` — `net.minecraft.core.registries.Registries`

### blocks / block entities

- `common/src/main/java/net/stirdrem/overgeared/OvergearedMod.java` — `net.minecraft.world.level.block.DispenserBlock`
- `common/src/main/java/net/stirdrem/overgeared/block/ModBlocks.java` — `net.minecraft.world.level.block.state.BlockBehaviour`
- `common/src/main/java/net/stirdrem/overgeared/block/ModBlocks.java` — `net.minecraft.world.level.block.state.properties.BlockStateProperties`
- `common/src/main/java/net/stirdrem/overgeared/block/UpgradeArrowDispenseBehavior.java` — `net.minecraft.world.level.block.DispenserBlock`
- `common/src/main/java/net/stirdrem/overgeared/mixin/ContainerCoolingMixin.java` — `net.minecraft.world.level.block.entity.BlockEntity`

### rendering / client UI

- `common/src/main/java/net/stirdrem/overgeared/client/OvergearedConfigScreen.java` — `net.minecraft.client.gui.GuiGraphics`
- `common/src/main/java/net/stirdrem/overgeared/client/OvergearedConfigScreen.java` — `net.minecraft.client.gui.components.AbstractSliderButton`
- `common/src/main/java/net/stirdrem/overgeared/client/OvergearedConfigScreen.java` — `net.minecraft.client.gui.components.Button`
- `common/src/main/java/net/stirdrem/overgeared/client/OvergearedConfigScreen.java` — `net.minecraft.client.gui.components.EditBox`
- `common/src/main/java/net/stirdrem/overgeared/client/OvergearedConfigScreen.java` — `net.minecraft.client.gui.components.ObjectSelectionList`

### text / NBT / network

- `common/src/main/java/net/stirdrem/overgeared/networking/ModPackets.java` — `net.minecraft.network.protocol.common.custom.CustomPacketPayload`
- `common/src/main/java/net/stirdrem/overgeared/networking/PacketDistributor.java` — `net.minecraft.network.protocol.common.custom.CustomPacketPayload`
- `common/src/main/java/net/stirdrem/overgeared/platform/IPlatformHelper.java` — `net.minecraft.network.protocol.common.custom.CustomPacketPayload`
- `common/src/main/java/net/stirdrem/overgeared/item/ToolType.java` — `net.minecraft.network.chat.MutableComponent`
- `common/src/main/java/net/stirdrem/overgeared/event/ModItemInteractEvents.java` — `net.minecraft.nbt.CompoundTag`

## Most repeated Minecraft imports

- 127 × `net.minecraft.item.ItemStack`
- 78 × `net.minecraft.util.Identifier`
- 62 × `net.minecraft.world.World`
- 61 × `net.minecraft.text.Text`
- 40 × `net.minecraft.entity.player.PlayerEntity`
- 39 × `net.minecraft.network.codec.PacketCodec`
- 39 × `net.minecraft.item.Item`
- 38 × `net.minecraft.util.math.BlockPos`
- 33 × `net.minecraft.recipe.Ingredient`
- 33 × `net.minecraft.entity.player.PlayerInventory`
- 32 × `net.minecraft.network.RegistryByteBuf`
- 30 × `net.minecraft.core.HolderLookup`
- 29 × `net.minecraft.block.BlockState`
- 28 × `net.minecraft.server.level.ServerPlayer`
- 27 × `net.minecraft.recipe.RecipeEntry`
- 27 × `net.minecraft.item.Items`
- 23 × `net.minecraft.core.registries.BuiltInRegistries`
- 20 × `net.minecraft.util.collection.DefaultedList`
- 19 × `net.minecraft.core.registries.Registries`
- 19 × `net.minecraft.client.gui.GuiGraphics`

Do **not** replace packages globally without validating renamed types and methods.
Minecraft 26.3 uses new unobfuscated names and different packages.
See `https://mappings.relativitymc.org/26.3/index.html` and Fabric docs.
