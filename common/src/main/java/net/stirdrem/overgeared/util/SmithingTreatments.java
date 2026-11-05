package net.stirdrem.overgeared.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.components.TreatmentData;

public final class SmithingTreatments {
    public static final List<String> TRAITS = List.of("balanced", "heavy", "sturdy", "vitality");
    private SmithingTreatments() {}

    public static boolean canTreat(ItemStack stack, String trait) {
        return TRAITS.contains(trait) && stack.is(TagKey.create(Registries.ITEM,
                OvergearedMod.loc("treatable/" + trait)));
    }

    public static boolean isHead(ItemStack stack) {
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return path.endsWith("_head") || path.endsWith("_blade");
    }

    private static boolean isWeapon(ItemStack stack) {
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return path.endsWith("_sword") || path.endsWith("_sword_blade")
                || path.endsWith("_axe") || path.endsWith("_axe_head");
    }

    private static boolean isArmor(ItemStack stack) {
        var equippable = stack.get(DataComponents.EQUIPPABLE);
        return equippable != null && equippable.slot().isArmor();
    }

    /** Null means no misses-free completed treatment: preserve the original stack. */
    public static TreatmentData outcome(String trait, int perfect, int good, int missed, int required) {
        if (!TRAITS.contains(trait) || required < 1 || perfect < 0 || good < 0 || missed != 0
                || perfect + good != required) return null;
        return new TreatmentData(trait, good == 0);
    }

    public static ItemStack apply(ItemStack original, TreatmentData treatment) {
        ItemStack result = original.copyWithCount(1);
        if (treatment == null) return result;
        TreatmentData old = original.get(ModComponents.TREATMENT.get());
        if (old != null && old.trait().equals(treatment.trait()) && old.flawless()) return result;
        result.set(ModComponents.TREATMENT.get(), treatment);
        preserveWear(original, result);
        return result;
    }

    /** Keep wear fraction when max durability changes; treatments/upgrades are not repairs. */
    public static void preserveWear(ItemStack original, ItemStack result) {
        int max = original.getMaxDamage();
        if (max > 0 && result.getMaxDamage() > 0) {
            result.setDamageValue(Math.min(result.getMaxDamage(), (int)Math.ceil(
                    (double)original.getDamageValue() * result.getMaxDamage() / max)));
        }
    }

    public static float durabilityMultiplier(ItemStack stack) {
        TreatmentData t = stack.get(ModComponents.TREATMENT.get());
        if (t == null) return 1;
        return switch (t.trait()) {
            case "sturdy" -> 1.15f;
            case "vitality" -> t.flawless() ? 1 : .9f;
            default -> 1;
        };
    }

    public static float miningMultiplier(ItemStack stack) {
        TreatmentData t = stack.get(ModComponents.TREATMENT.get());
        return t != null && t.trait().equals("sturdy") && !t.flawless()
                && !isArmor(stack) && !isWeapon(stack) ? .95f : 1;
    }

    public static ItemAttributeModifiers attributes(ItemStack stack, ItemAttributeModifiers base) {
        TreatmentData t = stack.get(ModComponents.TREATMENT.get());
        if (t == null || !TRAITS.contains(t.trait()) || isHead(stack) || BrokenHelper.isBroken(stack)) return base;
        List<ItemAttributeModifiers.Entry> entries = new ArrayList<>(base.modifiers());
        double damage = 0, speed = 0;
        if (isWeapon(stack)) {
            if (t.trait().equals("balanced")) { speed = .08; damage = t.flawless() ? 0 : -.05; }
            if (t.trait().equals("heavy")) { damage = .10; speed = t.flawless() ? 0 : -.08; }
            if (t.trait().equals("sturdy") && !t.flawless()) speed = -.05;
        }
        // Weapon modifiers subtract from the player's base attack speed (4).
        // Scale the effective baseline, not the negative modifier itself.
        for (var attribute : List.of(Attributes.ATTACK_DAMAGE, Attributes.ATTACK_SPEED)) {
            double bonus = attribute.equals(Attributes.ATTACK_DAMAGE) ? damage : speed;
            if (bonus == 0) continue;
            double total = attribute.equals(Attributes.ATTACK_DAMAGE) ? 1 : 4;
            for (var e : base.modifiers())
                if (e.attribute().equals(attribute) && e.slot().test(EquipmentSlot.MAINHAND)
                        && e.modifier().operation() == AttributeModifier.Operation.ADD_VALUE) total += e.modifier().amount();
            entries.add(new ItemAttributeModifiers.Entry(attribute, new AttributeModifier(
                    OvergearedMod.loc("treatment/" + (attribute.equals(Attributes.ATTACK_DAMAGE) ? "damage" : "speed")),
                    total * bonus, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND));
        }
        if (isArmor(stack)) {
            var slot = stack.get(DataComponents.EQUIPPABLE).slot();
            EquipmentSlotGroup group = switch (slot) {
                case HEAD -> EquipmentSlotGroup.HEAD;
                case CHEST -> EquipmentSlotGroup.CHEST;
                case LEGS -> EquipmentSlotGroup.LEGS;
                default -> EquipmentSlotGroup.FEET;
            };
            if (t.trait().equals("vitality") && slot == EquipmentSlot.CHEST)
                entries.add(new ItemAttributeModifiers.Entry(Attributes.MAX_HEALTH, new AttributeModifier(
                        OvergearedMod.loc("treatment/vitality"), 4, AttributeModifier.Operation.ADD_VALUE), group));
            if (t.trait().equals("sturdy") && !t.flawless())
                entries.add(new ItemAttributeModifiers.Entry(Attributes.MOVEMENT_SPEED, new AttributeModifier(
                        OvergearedMod.loc("treatment/sturdy_" + slot.getName()), -.05,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE), group));
        }
        return new ItemAttributeModifiers(entries);
    }

    public static List<Component> tooltip(ItemStack stack) {
        TreatmentData t = stack.get(ModComponents.TREATMENT.get());
        if (t == null || !TRAITS.contains(t.trait())) return List.of();
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("tooltip.overgeared.treatment", Component.translatable("treatment.overgeared." + t.trait()),
                Component.translatable("treatment.overgeared." + (t.flawless() ? "flawless" : "standard"))).withStyle(ChatFormatting.AQUA));
        lines.add(Component.translatable("treatment.overgeared." + t.trait() + ".benefit").withStyle(ChatFormatting.GREEN));
        String penalty = t.trait().equals("sturdy") ? (isArmor(stack) ? "armor" : isWeapon(stack) ? "weapon" : "tool") : "penalty";
        lines.add(Component.translatable(t.flawless() ? "treatment.overgeared.no_penalty"
                : "treatment.overgeared." + t.trait() + "." + penalty).withStyle(t.flawless() ? ChatFormatting.GRAY : ChatFormatting.RED));
        if (isHead(stack)) lines.add(Component.translatable("treatment.overgeared.transfer").withStyle(ChatFormatting.GRAY));
        if (BrokenHelper.isBroken(stack)) lines.add(Component.translatable("treatment.overgeared.broken").withStyle(ChatFormatting.RED));
        return lines;
    }
}
