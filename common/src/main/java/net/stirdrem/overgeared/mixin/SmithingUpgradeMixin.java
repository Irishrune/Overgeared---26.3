package net.stirdrem.overgeared.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.util.SmithingTreatments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SmithingTransformRecipe.class)
public abstract class SmithingUpgradeMixin {
    @Inject(method = "assemble(Lnet/minecraft/world/item/crafting/SmithingRecipeInput;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("RETURN"))
    private void overgeared$preserveWear(SmithingRecipeInput input, CallbackInfoReturnable<ItemStack> cir) {
        if (input.template().is(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get())
                || (input.template().is(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)
                    && (input.base().has(ModComponents.FORGING_QUALITY.get()) || input.base().has(ModComponents.TREATMENT.get()))))
            SmithingTreatments.preserveWear(input.base(), cir.getReturnValue());
    }
}
