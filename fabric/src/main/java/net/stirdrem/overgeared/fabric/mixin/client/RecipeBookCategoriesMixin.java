package net.stirdrem.overgeared.fabric.mixin.client;

import net.minecraft.client.gui.screens.recipebook.SearchRecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.stirdrem.overgeared.recipe.ForgingRecipeCategories;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Arrays;

@Mixin(SearchRecipeBookCategory.class)
public abstract class RecipeBookCategoriesMixin {
    @Shadow @Final @Mutable private static SearchRecipeBookCategory[] $VALUES;
    @Invoker("<init>")
    private static SearchRecipeBookCategory overgeared$create(String name, int ordinal, RecipeBookCategory... categories) {
        throw new AssertionError();
    }
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void overgeared$addForgingCategories(CallbackInfo ci) {
        var values = Arrays.copyOf($VALUES, $VALUES.length + 1);
        values[$VALUES.length] = overgeared$create("OVERGEARED_FORGING", $VALUES.length,
            ForgingRecipeCategories.TOOLS, ForgingRecipeCategories.ARMORS, ForgingRecipeCategories.MISC);
        $VALUES = values;
    }
}
