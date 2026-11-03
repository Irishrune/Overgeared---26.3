package net.minecraft.world.item.equipment;
import java.util.Map; import net.minecraft.tags.TagKey; import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceKey; import net.minecraft.sounds.SoundEvents;
public record ArmorMaterial(int durability, Map<ArmorType,Integer> defense,int enchantmentValue,
    SoundEvents.DummySound equipSound,float toughness,float knockbackResistance,
    TagKey<Item> repairIngredient,ResourceKey<EquipmentAsset> assetId) {}
