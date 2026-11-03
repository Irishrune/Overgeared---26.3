package net.minecraft.world.item;
import net.minecraft.tags.TagKey; import net.minecraft.world.level.block.Block;
public record ToolMaterial(TagKey<Block> incorrectBlocksForDrops, int durability, float speed,
    float attackDamageBonus, int enchantmentValue, TagKey<Item> repairItems) {}
