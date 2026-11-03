package net.stirdrem.overgeared.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.SmithingTemplateItem;

import java.util.List;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public class DiamondUpgradeTemplateItem extends SmithingTemplateItem {
  private static final Component DIAMOND_UPGRADE = Component.translatable("upgrade.overgeared.diamond_upgrade").withStyle(ChatFormatting.GRAY);
  private static final Component DIAMOND_UPGRADE_APPLIES_TO = Component.translatable("item.overgeared.smithing_template.diamond_upgrade.applies_to").withStyle(ChatFormatting.BLUE);
  private static final Component DIAMOND_UPGRADE_INGREDIENTS = Component.translatable("item.overgeared.smithing_template.diamond_upgrade.ingredients").withStyle(ChatFormatting.BLUE);
  private static final Component DIAMOND_UPGRADE_BASE_SLOT_DESCRIPTION = Component.translatable("item.overgeared.smithing_template.diamond_upgrade.base_slot_description");
  private static final Component DIAMOND_UPGRADE_ADDITIONS_SLOT_DESCRIPTION = Component.translatable("item.overgeared.smithing_template.diamond_upgrade.additions_slot_description");
  private static final Identifier EMPTY_SLOT_HELMET = Identifier.parse("item/empty_armor_slot_helmet");
  private static final Identifier EMPTY_SLOT_CHESTPLATE = Identifier.parse("item/empty_armor_slot_chestplate");
  private static final Identifier EMPTY_SLOT_LEGGINGS = Identifier.parse("item/empty_armor_slot_leggings");
  private static final Identifier EMPTY_SLOT_BOOTS = Identifier.parse("item/empty_armor_slot_boots");
  private static final Identifier EMPTY_SLOT_HOE = Identifier.parse("item/empty_slot_hoe");
  private static final Identifier EMPTY_SLOT_AXE = Identifier.parse("item/empty_slot_axe");
  private static final Identifier EMPTY_SLOT_SWORD = Identifier.parse("item/empty_slot_sword");
  private static final Identifier EMPTY_SLOT_SHOVEL = Identifier.parse("item/empty_slot_shovel");
  private static final Identifier EMPTY_SLOT_PICKAXE = Identifier.parse("item/empty_slot_pickaxe");
  private static final Identifier EMPTY_SLOT_DIAMOND = Identifier.parse("item/empty_slot_diamond");


  public DiamondUpgradeTemplateItem(Component appliesTo, Component ingredients,
                                    Component baseSlotDescription, Component additionsSlotDescription,
                                    List<Identifier> baseSlotIcons, List<Identifier> additionsSlotIcons,
                                    Item.Properties properties) {
    super(appliesTo, ingredients, baseSlotDescription, additionsSlotDescription,
            baseSlotIcons, additionsSlotIcons, properties);
  }

  public static SmithingTemplateItem createDiamondUpgradeTemplate(ResourceKey<Item> key) {
    return new SmithingTemplateItem(
            DIAMOND_UPGRADE_APPLIES_TO,
            DIAMOND_UPGRADE_INGREDIENTS,
            DIAMOND_UPGRADE_BASE_SLOT_DESCRIPTION,
            DIAMOND_UPGRADE_ADDITIONS_SLOT_DESCRIPTION,
            createDiamondUpgradeIconList(),
            createDiamondUpgradeMaterialList(),
            new Item.Properties().setId(key)
    );
  }

  private static List<Identifier> createDiamondUpgradeIconList() {
    return List.of(
            EMPTY_SLOT_HELMET,
            EMPTY_SLOT_SWORD,
            EMPTY_SLOT_CHESTPLATE,
            EMPTY_SLOT_PICKAXE,
            EMPTY_SLOT_LEGGINGS,
            EMPTY_SLOT_AXE,
            EMPTY_SLOT_BOOTS,
            EMPTY_SLOT_HOE,
            EMPTY_SLOT_SHOVEL
    );
  }

  private static List<Identifier> createDiamondUpgradeMaterialList() {
    return List.of(EMPTY_SLOT_DIAMOND);
  }
}
