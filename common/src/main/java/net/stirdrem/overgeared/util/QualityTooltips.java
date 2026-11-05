package net.stirdrem.overgeared.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.event.QualityAttributeHandler;

/** Values come from the same stack/config/datapack calculations used in gameplay. */
public final class QualityTooltips {
    private QualityTooltips() {}
    public static List<Component> lines(ItemStack stack) {
        List<Component> lines = new ArrayList<>();
        ForgingQuality quality = stack.get(ModComponents.FORGING_QUALITY.get());
        if (quality == null || quality == ForgingQuality.NONE) return lines;
        if (BrokenHelper.isBroken(stack)) {
            lines.add(Component.translatable("tooltip.overgeared.quality.inactive").withStyle(ChatFormatting.GRAY));
            return lines;
        }
        if (stack.isDamageableItem()) {
            lines.add(percent("durability", quality.getDurabilityMultiplier() - 1.0));
            lines.add(Component.translatable("tooltip.overgeared.quality.uses",
                    Math.max(0, stack.getMaxDamage() - stack.getDamageValue()), stack.getMaxDamage()).withStyle(ChatFormatting.GRAY));
        }
        if (stack.has(DataComponents.TOOL)) lines.add(percent("mining", quality.getMiningSpeedMultiplier() - 1.0));
        ItemAttributeModifiers base = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        ItemAttributeModifiers modified = QualityAttributeHandler.applyQualityModifiers(stack, base);
        for (var original : base.modifiers()) {
            var changed = modified.modifiers().stream().filter(e -> e.attribute().equals(original.attribute())
                    && e.slot().equals(original.slot()) && e.modifier().id().equals(original.modifier().id())).findFirst();
            if (changed.isEmpty()) continue;
            double difference = changed.get().modifier().amount() - original.modifier().amount();
            if (Math.abs(difference) < 0.00001) continue;
            String amount = String.format(Locale.ROOT, "%+.2f", difference);
            lines.add(Component.translatable("tooltip.overgeared.quality.attribute", amount,
                    Component.translatable(original.attribute().value().getDescriptionId()))
                    .withStyle(difference < 0 ? ChatFormatting.DARK_RED : ChatFormatting.GREEN));
        }
        if (lines.isEmpty()) lines.add(Component.translatable("tooltip.overgeared.quality.part").withStyle(ChatFormatting.GRAY));
        return lines;
    }
    private static Component percent(String stat, double fraction) {
        long amount = Math.round(fraction * 100);
        return Component.translatable("tooltip.overgeared.quality." + stat, (amount > 0 ? "+" : "") + amount + "%")
                .withStyle(amount < 0 ? ChatFormatting.DARK_RED : amount > 0 ? ChatFormatting.GREEN : ChatFormatting.GRAY);
    }
}
