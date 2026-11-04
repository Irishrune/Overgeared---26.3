package net.stirdrem.overgeared.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.util.ForgingQualityHelper;

/**
 * Creates the original per-villager-level forging qualities in data-driven
 * Minecraft 26.3 trades, while preserving the matching emerald price.
 *
 * The first cost stack is configured with base_price; the transient additional
 * trade cost adjusts it to the quality-specific final price. Unlike static
 * vanilla item modifiers, this keeps quality and price on the same random roll.
 *
 * Only used for original forged offers at levels 1-3, where MASTER cannot roll,
 * so the legacy MASTER-only one-use cap is not relevant to these offers.
 */
public final class ForgedTradeQualityFunction extends LootItemConditionalFunction {
    public static final MapCodec<ForgedTradeQualityFunction> MAP_CODEC =
            RecordCodecBuilder.mapCodec(instance ->
                    commonFields(instance).and(instance.group(
                            Codec.intRange(1, 5).fieldOf("villager_level").forGetter(f -> f.villagerLevel),
                            Codec.intRange(1, 64).fieldOf("base_price").forGetter(f -> f.basePrice)
                    )).apply(instance, ForgedTradeQualityFunction::new));

    private final int villagerLevel;
    private final int basePrice;

    private ForgedTradeQualityFunction(Optional<Holder<LootItemCondition>> condition, int villagerLevel, int basePrice) {
        super(condition);
        this.villagerLevel = villagerLevel;
        this.basePrice = basePrice;
    }

    @Override
    public MapCodec<ForgedTradeQualityFunction> codec() {
        return MAP_CODEC;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        if (stack.isEmpty()) {
            return stack;
        }
        ForgingQuality quality = ForgingQualityHelper.rollQuality(context.getRandom(), villagerLevel);
        ForgingQualityHelper.applyQuality(stack, quality);
        int finalPrice = ForgingQualityHelper.priceForQuality(quality);
        stack.set(DataComponents.ADDITIONAL_TRADE_COST, finalPrice - basePrice);
        return stack;
    }

    public static void register() {
        Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE,
                OvergearedMod.loc("set_forged_trade_quality"), MAP_CODEC);
    }
}
