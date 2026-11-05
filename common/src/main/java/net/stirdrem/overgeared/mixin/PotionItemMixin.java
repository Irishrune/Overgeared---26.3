package net.stirdrem.overgeared.mixin;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.components.ModComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import java.util.function.Consumer;

/** 26.3 potions apply effects and build tooltips through their contents component. */
@Mixin(PotionContents.class)
public abstract class PotionItemMixin {
    @Unique private static float overgeared$scale(DataComponentGetter components) {
        Integer uses = components.get(ModComponents.TIPPED_USES.get());
        return uses == null ? 1f : Math.max(0.1f, 1f - uses / 8f);
    }
    @ModifyExpressionValue(method = "onConsume", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/item/ItemStack;getOrDefault(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;"))
    private Object overgeared$scaleEffects(Object duration, Level level, LivingEntity user, ItemStack stack, Consumable consumable) {
        return (Float)duration * overgeared$scale(stack);
    }
    @ModifyExpressionValue(method = "addToTooltip", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/core/component/DataComponentGetter;getOrDefault(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;"))
    private Object overgeared$scaleTooltip(Object duration, Item.TooltipContext context, Consumer<Component> consumer,
                                         TooltipFlag flag, DataComponentGetter components) {
        return (Float)duration * overgeared$scale(components);
    }
}
