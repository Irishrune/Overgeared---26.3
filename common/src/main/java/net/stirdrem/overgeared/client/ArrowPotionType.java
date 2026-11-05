package net.stirdrem.overgeared.client;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.item.ModItems;

public record ArrowPotionType() implements RangeSelectItemModelProperty {
    public static final MapCodec<ArrowPotionType> CODEC = MapCodec.unit(new ArrowPotionType());
    @Override public MapCodec<ArrowPotionType> type() { return CODEC; }
    @Override public float get(ItemStack stack, ClientLevel level, ItemOwner owner, int seed) {
        PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        if (contents.potion().isEmpty() && !contents.hasEffects()) return 0;
        return stack.is(ModItems.LINGERING_ARROW.get()) || stack.getOrDefault(ModComponents.LINGERING_STATUS.get(), false) ? 2 : 1;
    }
}
