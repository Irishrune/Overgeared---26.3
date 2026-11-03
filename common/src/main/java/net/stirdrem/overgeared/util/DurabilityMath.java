package net.stirdrem.overgeared.util;

/**
 * Pure-Java version of Overgeared's existing durability calculation.
 *
 * This intentionally preserves the order and truncation behavior of the
 * 1.21.1 ItemStackMixin: base multiplier -> forging quality -> grind penalty.
 * Keeping this calculation separate lets it be regression-tested while the
 * version-specific ItemStack hook is updated for Fabric 26.3.
 */
public final class DurabilityMath {
    private DurabilityMath() {}

    public static int modifiedMaxDamage(int original, boolean baseMultiplierBlacklisted,
                                        float baseMultiplier, float qualityMultiplier,
                                        int reducedGrindCount, float penaltyPerGrind) {
        if (original <= 0) return original;
        int result = baseMultiplierBlacklisted
                ? original
                : (int) (original * baseMultiplier);
        result = (int) (result * qualityMultiplier);
        if (reducedGrindCount > 0) {
            float penalty = Math.max(0.1f, 1.0f - reducedGrindCount * penaltyPerGrind);
            result = (int) (result * penalty);
        }
        return result;
    }
}
