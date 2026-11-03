package net.stirdrem.overgeared.item;

import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.util.ModTags;

import java.util.Map;

/**
 * 26.3 armor uses material records and item components instead of ArmorItem.
 * Preserve Overgeared's existing defense / durability / toughness values.
 */
public final class ModArmorMaterials {
    private ModArmorMaterials() {}

    public static final ResourceKey<EquipmentAsset> STEEL_ASSET = ResourceKey.create(
            EquipmentAssets.ROOT_ID, OvergearedMod.loc("steel"));
    public static final ResourceKey<EquipmentAsset> COPPER_ASSET = ResourceKey.create(
            EquipmentAssets.ROOT_ID, OvergearedMod.loc("copper"));

    public static final ArmorMaterial STEEL = new ArmorMaterial(
            26,
            Map.of(ArmorType.HELMET, 3, ArmorType.CHESTPLATE, 7,
                   ArmorType.LEGGINGS, 5, ArmorType.BOOTS, 2),
            12, SoundEvents.ARMOR_EQUIP_IRON,
            1.0f, 0.0f,
            ModTags.Items.REPAIRS_STEEL_TOOLS, STEEL_ASSET);

    public static final ArmorMaterial COPPER = new ArmorMaterial(
            10,
            Map.of(ArmorType.HELMET, 1, ArmorType.CHESTPLATE, 4,
                   ArmorType.LEGGINGS, 3, ArmorType.BOOTS, 1),
            15, SoundEvents.ARMOR_EQUIP_IRON,
            0.0f, 0.0f,
            ModTags.Items.REPAIRS_COPPER_TOOLS, COPPER_ASSET);

    public static int getDurabilityForType(ArmorMaterial material, ArmorType type) {
        return type.getDurability(material.durability());
    }
}
