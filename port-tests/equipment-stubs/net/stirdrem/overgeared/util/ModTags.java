package net.stirdrem.overgeared.util;
import net.minecraft.tags.TagKey; import net.minecraft.world.item.Item; import net.minecraft.world.level.block.Block;
public final class ModTags {
public static final class Blocks { public static final TagKey<Block> INCORRECT_FOR_STEEL_TOOL = new TagKey<>("steel");
public static final TagKey<Block> INCORRECT_FOR_COPPER_TOOL = new TagKey<>("copper"); }
public static final class Items { public static final TagKey<Item> REPAIRS_STEEL_TOOLS = new TagKey<>("steel");
public static final TagKey<Item> REPAIRS_COPPER_TOOLS = new TagKey<>("copper"); } }
