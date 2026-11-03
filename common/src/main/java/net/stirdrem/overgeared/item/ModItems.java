package net.stirdrem.overgeared.item;

import net.minecraft.world.item.*;
import net.minecraft.world.item.equipment.ArmorType;
import net.stirdrem.overgeared.registry.DeferredItem;
import net.stirdrem.overgeared.registry.DeferredRegister;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.entity.ArrowTier;
import net.stirdrem.overgeared.util.ModTags;
import net.stirdrem.overgeared.item.custom.*;
import net.stirdrem.overgeared.item.EquipmentStatMath;

import java.util.function.Function;
import net.minecraft.resources.ResourceKey;

public class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(OvergearedMod.MOD_ID);

    public static final DeferredItem<Item> CRUDE_STEEL = registerSimpleItem("crude_steel");
    public static final DeferredItem<Item> HEATED_CRUDE_STEEL = registerSimpleItem("heated_crude_steel");

    public static final DeferredItem<Item> ROCK = registerSimpleItem("knappable_rock");

    public static final DeferredItem<Item> UNFIRED_TOOL_CAST = registerItem("unfired_tool_cast",
            key -> new ClayToolCastItem(new Item.Properties().setId(key)));

    public static final DeferredItem<Item> CLAY_TOOL_CAST = registerItem("clay_tool_cast",
            key -> new ToolCastItem(true, new Item.Properties().setId(key).durability(1)));

    public static final DeferredItem<Item> NETHER_TOOL_CAST = registerItem("nether_tool_cast",
            key -> new ToolCastItem(false, new Item.Properties().setId(key).stacksTo(1)));

    public static final DeferredItem<Item> STEEL_NUGGET = registerSimpleItem("steel_nugget");
    public static final DeferredItem<Item> COPPER_NUGGET = registerSimpleItem("copper_nugget");

    public static final DeferredItem<Item> STEEL_INGOT = registerSimpleItem("steel_ingot");

    public static final DeferredItem<Item> IRON_ARROW_HEAD = registerSimpleItem("iron_arrow_head");
    public static final DeferredItem<Item> STEEL_ARROW_HEAD = registerSimpleItem("steel_arrow_head");

    public static final DeferredItem<Item> DIAMOND_SHARD = registerSimpleItem("diamond_shard");

    public static final DeferredItem<Item> NETHERITE_ALLOY = registerSimpleItem("netherite_alloy");

    public static final DeferredItem<Item> HEATED_IRON_INGOT = registerSimpleItem("heated_iron_ingot");
    public static final DeferredItem<Item> HEATED_COPPER_INGOT = registerSimpleItem("heated_copper_ingot");
    public static final DeferredItem<Item> HEATED_STEEL_INGOT = registerSimpleItem("heated_steel_ingot");
    public static final DeferredItem<Item> HEATED_SILVER_INGOT = registerSimpleItem("heated_silver_ingot");
    public static final DeferredItem<Item> HEATED_NETHERITE_ALLOY = registerSimpleItem("heated_netherite_alloy");

    public static final DeferredItem<Item> COPPER_PLATE = registerSimpleItem("copper_plate");
    public static final DeferredItem<Item> IRON_PLATE = registerSimpleItem("iron_plate");
    public static final DeferredItem<Item> STEEL_PLATE = registerSimpleItem("steel_plate");

    public static final DeferredItem<Item> STEEL_TONG = registerSimpleItem("steel_tong");
    public static final DeferredItem<Item> IRON_TONG = registerSimpleItem("iron_tong");

    public static final DeferredItem<Item> WOODEN_TONGS = registerItem("wooden_tongs",
            key -> new Tongs(new Item.Properties().setId(key)
                    .tool(ToolMaterial.WOOD, ModTags.Blocks.SMITHING, -1f, -2f, 0f).durability(120)));
    public static final DeferredItem<Item> IRON_TONGS = registerItem("iron_tongs",
            key -> new Tongs(new Item.Properties().setId(key)
                    .tool(ToolMaterial.IRON, ModTags.Blocks.SMITHING, -1f, -2f, 0f).durability(512)));
    public static final DeferredItem<Item> STEEL_TONGS = registerItem("steel_tongs",
            key -> new Tongs(new Item.Properties().setId(key)
                    .tool(ModToolTiers.STEEL, ModTags.Blocks.SMITHING, -1f, -2f, 0f).durability(1024)));

    public static final DeferredItem<Item> STONE_HAMMER_HEAD = registerSimpleItem("stone_hammer_head");
    public static final DeferredItem<Item> COPPER_HAMMER_HEAD = registerSimpleItem("copper_hammer_head");
    public static final DeferredItem<Item> STEEL_HAMMER_HEAD = registerSimpleItem("steel_hammer_head");

    public static final DeferredItem<Item> SMITHING_HAMMER = registerItem("smithing_hammer",
            key -> new SmithingHammer(new Item.Properties().setId(key)
                    .tool(ModToolTiers.STEEL, ModTags.Blocks.SMITHING, -1f, -2.8f, 0f)));

    public static final DeferredItem<Item> COPPER_SMITHING_HAMMER = registerItem("copper_smithing_hammer",
            key -> new SmithingHammer(new Item.Properties().setId(key)
                    .tool(ModToolTiers.COPPER, ModTags.Blocks.SMITHING, -1f, -2.8f, 0f)));

    public static final DeferredItem<Item> DIAMOND_UPGRADE_SMITHING_TEMPLATE = registerItem("diamond_upgrade_smithing_template",
            key -> DiamondUpgradeTemplateItem.createDiamondUpgradeTemplate(key));

    public static final DeferredItem<Item> EMPTY_BLUEPRINT = registerSimpleItem("empty_blueprint");

    public static final DeferredItem<Item> BLUEPRINT = registerItem("blueprint",
            key -> new BlueprintItem(new Item.Properties().setId(key)));

    public static final DeferredItem<Item> STONE_SWORD_BLADE = registerSimpleItem("stone_sword_blade");
    public static final DeferredItem<Item> IRON_SWORD_BLADE = registerSimpleItem("iron_sword_blade");
    public static final DeferredItem<Item> GOLDEN_SWORD_BLADE = registerSimpleItem("golden_sword_blade");
    public static final DeferredItem<Item> STEEL_SWORD_BLADE = registerSimpleItem("steel_sword_blade");
    public static final DeferredItem<Item> COPPER_SWORD_BLADE = registerSimpleItem("copper_sword_blade");

    public static final DeferredItem<Item> STONE_PICKAXE_HEAD = registerSimpleItem("stone_pickaxe_head");
    public static final DeferredItem<Item> IRON_PICKAXE_HEAD = registerSimpleItem("iron_pickaxe_head");
    public static final DeferredItem<Item> GOLDEN_PICKAXE_HEAD = registerSimpleItem("golden_pickaxe_head");
    public static final DeferredItem<Item> STEEL_PICKAXE_HEAD = registerSimpleItem("steel_pickaxe_head");
    public static final DeferredItem<Item> COPPER_PICKAXE_HEAD = registerSimpleItem("copper_pickaxe_head");

    public static final DeferredItem<Item> STONE_AXE_HEAD = registerSimpleItem("stone_axe_head");
    public static final DeferredItem<Item> IRON_AXE_HEAD = registerSimpleItem("iron_axe_head");
    public static final DeferredItem<Item> GOLDEN_AXE_HEAD = registerSimpleItem("golden_axe_head");
    public static final DeferredItem<Item> STEEL_AXE_HEAD = registerSimpleItem("steel_axe_head");
    public static final DeferredItem<Item> COPPER_AXE_HEAD = registerSimpleItem("copper_axe_head");

    public static final DeferredItem<Item> STONE_SHOVEL_HEAD = registerSimpleItem("stone_shovel_head");
    public static final DeferredItem<Item> IRON_SHOVEL_HEAD = registerSimpleItem("iron_shovel_head");
    public static final DeferredItem<Item> GOLDEN_SHOVEL_HEAD = registerSimpleItem("golden_shovel_head");
    public static final DeferredItem<Item> STEEL_SHOVEL_HEAD = registerSimpleItem("steel_shovel_head");
    public static final DeferredItem<Item> COPPER_SHOVEL_HEAD = registerSimpleItem("copper_shovel_head");

    public static final DeferredItem<Item> STONE_HOE_HEAD = registerSimpleItem("stone_hoe_head");
    public static final DeferredItem<Item> IRON_HOE_HEAD = registerSimpleItem("iron_hoe_head");
    public static final DeferredItem<Item> GOLDEN_HOE_HEAD = registerSimpleItem("golden_hoe_head");
    public static final DeferredItem<Item> STEEL_HOE_HEAD = registerSimpleItem("steel_hoe_head");
    public static final DeferredItem<Item> COPPER_HOE_HEAD = registerSimpleItem("copper_hoe_head");

    public static final DeferredItem<Item> STEEL_SWORD = registerItem("steel_sword",
            key -> new Item(new Item.Properties().setId(key).sword(ModToolTiers.STEEL,
                    EquipmentStatMath.attackBaseline(6f, ModToolTiers.STEEL.attackDamageBonus()), -2.4f)));
    public static final DeferredItem<Item> STEEL_PICKAXE = registerItem("steel_pickaxe",
            key -> new Item(new Item.Properties().setId(key).pickaxe(ModToolTiers.STEEL,
                    EquipmentStatMath.attackBaseline(4f, ModToolTiers.STEEL.attackDamageBonus()), -2.8f)));
    public static final DeferredItem<Item> STEEL_AXE = registerItem("steel_axe",
            key -> new Item(new Item.Properties().setId(key).axe(ModToolTiers.STEEL,
                    EquipmentStatMath.attackBaseline(8f, ModToolTiers.STEEL.attackDamageBonus()), -3.0f)));
    public static final DeferredItem<Item> STEEL_HOE = registerItem("steel_hoe",
            key -> new Item(new Item.Properties().setId(key).hoe(ModToolTiers.STEEL,
                    EquipmentStatMath.attackBaseline(0f, ModToolTiers.STEEL.attackDamageBonus()), -0.5f)));
    public static final DeferredItem<Item> STEEL_SHOVEL = registerItem("steel_shovel",
            key -> new Item(new Item.Properties().setId(key).shovel(ModToolTiers.STEEL,
                    EquipmentStatMath.attackBaseline(4f, ModToolTiers.STEEL.attackDamageBonus()), -3.0f)));

    public static final DeferredItem<Item> STEEL_HELMET = registerItem("steel_helmet",
            key -> new Item(new Item.Properties().setId(key)
                    .humanoidArmor(ModArmorMaterials.STEEL, ArmorType.HELMET)));
    public static final DeferredItem<Item> STEEL_CHESTPLATE = registerItem("steel_chestplate",
            key -> new Item(new Item.Properties().setId(key)
                    .humanoidArmor(ModArmorMaterials.STEEL, ArmorType.CHESTPLATE)));
    public static final DeferredItem<Item> STEEL_LEGGINGS = registerItem("steel_leggings",
            key -> new Item(new Item.Properties().setId(key)
                    .humanoidArmor(ModArmorMaterials.STEEL, ArmorType.LEGGINGS)));
    public static final DeferredItem<Item> STEEL_BOOTS = registerItem("steel_boots",
            key -> new Item(new Item.Properties().setId(key)
                    .humanoidArmor(ModArmorMaterials.STEEL, ArmorType.BOOTS)));

    public static final DeferredItem<Item> COPPER_HELMET = registerItem("copper_helmet",
            key -> new Item(new Item.Properties().setId(key)
                    .humanoidArmor(ModArmorMaterials.COPPER, ArmorType.HELMET)));
    public static final DeferredItem<Item> COPPER_CHESTPLATE = registerItem("copper_chestplate",
            key -> new Item(new Item.Properties().setId(key)
                    .humanoidArmor(ModArmorMaterials.COPPER, ArmorType.CHESTPLATE)));
    public static final DeferredItem<Item> COPPER_LEGGINGS = registerItem("copper_leggings",
            key -> new Item(new Item.Properties().setId(key)
                    .humanoidArmor(ModArmorMaterials.COPPER, ArmorType.LEGGINGS)));
    public static final DeferredItem<Item> COPPER_BOOTS = registerItem("copper_boots",
            key -> new Item(new Item.Properties().setId(key)
                    .humanoidArmor(ModArmorMaterials.COPPER, ArmorType.BOOTS)));

    public static final DeferredItem<Item> COPPER_SWORD = registerItem("copper_sword",
            key -> new Item(new Item.Properties().setId(key).sword(ModToolTiers.COPPER,
                    EquipmentStatMath.attackBaseline(4f, ModToolTiers.COPPER.attackDamageBonus()), -2.4f)));
    public static final DeferredItem<Item> COPPER_PICKAXE = registerItem("copper_pickaxe",
            key -> new Item(new Item.Properties().setId(key).pickaxe(ModToolTiers.COPPER,
                    EquipmentStatMath.attackBaseline(2f, ModToolTiers.COPPER.attackDamageBonus()), -2.8f)));
    public static final DeferredItem<Item> COPPER_AXE = registerItem("copper_axe",
            key -> new Item(new Item.Properties().setId(key).axe(ModToolTiers.COPPER,
                    EquipmentStatMath.attackBaseline(6f, ModToolTiers.COPPER.attackDamageBonus()), -3.0f)));
    public static final DeferredItem<Item> COPPER_HOE = registerItem("copper_hoe",
            key -> new Item(new Item.Properties().setId(key).hoe(ModToolTiers.COPPER,
                    EquipmentStatMath.attackBaseline(0f, ModToolTiers.COPPER.attackDamageBonus()), -1.5f)));
    public static final DeferredItem<Item> COPPER_SHOVEL = registerItem("copper_shovel",
            key -> new Item(new Item.Properties().setId(key).shovel(ModToolTiers.COPPER,
                    EquipmentStatMath.attackBaseline(2.5f, ModToolTiers.COPPER.attackDamageBonus()), -3.0f)));

    public static final DeferredItem<Item> LINGERING_ARROW = registerItem("lingering_arrow",
            key -> new LingeringArrowItem(new Item.Properties().setId(key), ArrowTier.FLINT));

    public static final DeferredItem<Item> IRON_UPGRADE_ARROW = registerItem("iron_arrow",
            key -> new UpgradeArrowItem(new Item.Properties().setId(key), ArrowTier.IRON));
    public static final DeferredItem<Item> STEEL_UPGRADE_ARROW = registerItem("steel_arrow",
            key -> new UpgradeArrowItem(new Item.Properties().setId(key), ArrowTier.STEEL));
    public static final DeferredItem<Item> DIAMOND_UPGRADE_ARROW = registerItem("diamond_arrow",
            key -> new UpgradeArrowItem(new Item.Properties().setId(key), ArrowTier.DIAMOND));

    public static <T extends Item> DeferredItem<T> registerItem(
            String name, Function<ResourceKey<Item>, T> itemFactory) {
        return ITEMS.registerWithKey(name, itemFactory);
    }

    public static DeferredItem<Item> registerSimpleItem(String name) {
        return ITEMS.registerSimpleItem(name);
    }

    public static void register() {
        ITEMS.register();
    }
}
