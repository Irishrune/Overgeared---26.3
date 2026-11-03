import net.stirdrem.overgeared.util.ForgingScore;

public final class ForgingScoreTest {
    static void eq(Object expected, Object actual) {
        if (!expected.equals(actual)) throw new AssertionError(expected + " != " + actual);
    }
    static void close(float expected, float actual) {
        if (Math.abs(expected - actual) > 0.00001f) throw new AssertionError(expected + " != " + actual);
    }
    static void illegal(Runnable action) {
        try { action.run(); }
        catch (IllegalArgumentException expected) { return; }
        throw new AssertionError("Expected IllegalArgumentException");
    }
    public static void main(String[] args) {
        close(0f, ForgingScore.calculate(0,0,0));
        close(1f, ForgingScore.calculate(12,0,0));
        close(0.6f, ForgingScore.calculate(0,10,0));
        close(0f, ForgingScore.calculate(0,0,10));
        close(0.4f, ForgingScore.calculate(1, 3, 3)); // (1 + 1.8) / 7
        eq("perfect", ForgingScore.rank(10,0,0, 0.9, (double) 0.6f,0.3));
        eq("expert", ForgingScore.rank(0,10,0, 0.9,0.5,0.3));
        eq("well", ForgingScore.rank(0,5,5, 0.9,0.5,0.2));
        eq("poor", ForgingScore.rank(0,0,10, 0.9,0.5,0.2));
        // Strict comparisons at equality: 0.6 is not above 0.6
        eq("well", ForgingScore.rank(0,10,0, 0.9, (double) 0.6f,0.3));
        illegal(() -> ForgingScore.calculate(-1,0,0));
        illegal(() -> ForgingScore.calculate(0,-1,0));
        illegal(() -> ForgingScore.calculate(0,0,-1));
        System.out.println("ForgingScore: all tests passed");
    }
}
