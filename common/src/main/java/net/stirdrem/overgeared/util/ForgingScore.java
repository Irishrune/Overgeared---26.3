package net.stirdrem.overgeared.util;

/**
 * Loader- and Minecraft-version-independent quality scoring extracted from
 * the original Overgeared anvil minigame. A Perfect strike contributes 1.0,
 * Good contributes 0.6, and Miss contributes 0.0 to the final average.
 * Strict '>' comparisons intentionally preserve the upstream game's tiers.
 */
public final class ForgingScore {
    private ForgingScore() {}

    public static float calculate(int perfect, int good, int missed) {
        if (perfect < 0 || good < 0 || missed < 0) {
            throw new IllegalArgumentException("Strike counts must be nonnegative");
        }
        long total = (long) perfect + good + missed;
        if (total == 0L) return 0f;
        return (perfect + good * 0.6f) / (float) total;
    }

    /**
     * Returns the same machine-readable quality strings as upstream.
     * Configuration thresholds are passed in, rather than loading Minecraft.
     */
    public static String rank(int perfect, int good, int missed,
                              double perfectThreshold, double expertThreshold,
                              double wellThreshold) {
        float score = calculate(perfect, good, missed);
        if (score > perfectThreshold) return "perfect";
        if (score > expertThreshold) return "expert";
        if (score > wellThreshold) return "well";
        return "poor";
    }
}
