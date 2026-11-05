package net.stirdrem.overgeared.mixin;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.BrewingRecipe;
import net.minecraft.world.item.crafting.BrewingInput;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.stirdrem.overgeared.components.ModComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BrewingStandBlockEntity.class)
public class BrewingStandBlockEntityMixin {
    @WrapOperation(method = "doBrew", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/item/crafting/BrewingRecipe;assemble(Lnet/minecraft/world/item/crafting/BrewingInput;)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack overgeared$keepTippingUses(BrewingRecipe recipe, BrewingInput input, Operation<ItemStack> original) {
        ItemStack result = original.call(recipe, input);
        Integer uses = input.input().get(ModComponents.TIPPED_USES.get());
        if (uses != null && !result.isEmpty()) result.set(ModComponents.TIPPED_USES.get(), uses);
        return result;
    }
}
