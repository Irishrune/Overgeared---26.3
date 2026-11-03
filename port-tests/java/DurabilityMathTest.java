import net.stirdrem.overgeared.util.DurabilityMath;

public final class DurabilityMathTest {
    private static void check(int want, int actual, String note) {
        if (want != actual) throw new AssertionError(note + ": expected " + want + ", got " + actual);
    }
    public static void main(String[] args) {
        check(250, DurabilityMath.modifiedMaxDamage(250, false, 1f, 1f, 0, .05f), "unchanged");
        check(500, DurabilityMath.modifiedMaxDamage(250, false, 2f, 1f, 0, .05f), "base multiplier");
        check(250, DurabilityMath.modifiedMaxDamage(250, true, 2f, 1f, 0, .05f), "blacklist skips base bonus");
        check(750, DurabilityMath.modifiedMaxDamage(250, false, 1f, 3f, 0, .05f), "masterwork quality");
        check(375, DurabilityMath.modifiedMaxDamage(250, false, 2f, .75f, 0, .05f), "poor quality");
        check(225, DurabilityMath.modifiedMaxDamage(250, false, 1f, 1f, 2, .05f), "grind wear");
        check(25, DurabilityMath.modifiedMaxDamage(250, false, 1f, 1f, 100, .05f), "grind penalty floor");
        check(0, DurabilityMath.modifiedMaxDamage(0, false, 2f, 3f, 10, .05f), "unbreakable");
        check(175, DurabilityMath.modifiedMaxDamage(200, false, 1.25f, .75f, 1, .06f), "rounding order");
        System.out.println("DurabilityMath: all tests passed");
    }
}
