package net.stirdrem.overgeared.item;

import net.minecraft.world.item.ToolMaterial;
import net.stirdrem.overgeared.util.ModTags;

/**
 * 26.3 tool materials are records, not instances of the removed Tier interface.
 * Numeric values are taken from the 1.21.1 Overgeared definitions; keep them
 * unchanged during the compatibility port so rebalancing is deliberate later.
 */
public final class ModToolTiers {
    private ModToolTiers() {}

    public static final ToolMaterial COPPER = new ToolMaterial(
            ModTags.Blocks.INCORRECT_FOR_COPPER_TOOL,
            190, 5.0f, 1.0f, 12,
            ModTags.Items.REPAIRS_COPPER_TOOLS);

    public static final ToolMaterial STEEL = new ToolMaterial(
            ModTags.Blocks.INCORRECT_FOR_STEEL_TOOL,
            500, 7.0f, 3.0f, 12,
            ModTags.Items.REPAIRS_STEEL_TOOLS);
}
