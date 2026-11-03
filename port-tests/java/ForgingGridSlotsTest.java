import net.stirdrem.overgeared.recipe.ForgingGridSlots;
import java.util.List;
import java.util.Set;

public class ForgingGridSlotsTest {
    private static int checks = 0;
    private static void equal(int actual, int expected) {
        if (actual != expected) throw new AssertionError("Expected slot " + expected + ", got " + actual);
        checks++;
    }
    private static void fails(Runnable action) {
        try { action.run(); throw new AssertionError("Expected IllegalArgumentException"); }
        catch (IllegalArgumentException expected) { checks++; }
    }
    public static void main(String[] args) {
        for (int gridSlot=0;gridSlot<9;gridSlot++) {
            equal(ForgingGridSlots.slotForPatternIndex(0, 1, gridSlot % 3, gridSlot / 3), gridSlot);
        }
        // 2x2 recipe shifted one cell right and down, including lower-right cell.
        equal(ForgingGridSlots.slotForPatternIndex(0,2,1,1),4);
        equal(ForgingGridSlots.slotForPatternIndex(1,2,1,1),5);
        equal(ForgingGridSlots.slotForPatternIndex(2,2,1,1),7);
        equal(ForgingGridSlots.slotForPatternIndex(3,2,1,1),8);
        for (int i=0;i<9;i++) equal(ForgingGridSlots.slotForPatternIndex(i,3,0,0),i);
        fails(() -> ForgingGridSlots.slotForPatternIndex(1,2,2,0));
        fails(() -> ForgingGridSlots.slotForPatternIndex(2,1,0,2));
        fails(() -> ForgingGridSlots.slotForPatternIndex(-1,1,0,0));
        fails(() -> ForgingGridSlots.slotForPatternIndex(0,0,0,0));
        ForgingGridSlots.validatePattern(List.of("#"),Set.of("#"));
        ForgingGridSlots.validatePattern(List.of(" A","B "),Set.of("A","B"));
        checks+=2;
        fails(() -> ForgingGridSlots.validatePattern(List.of(),Set.of("#")));
        fails(() -> ForgingGridSlots.validatePattern(List.of(""),Set.of("#")));
        fails(() -> ForgingGridSlots.validatePattern(List.of("   "),Set.of("#")));
        fails(() -> ForgingGridSlots.validatePattern(List.of("#","##"),Set.of("#")));
        fails(() -> ForgingGridSlots.validatePattern(List.of("####"),Set.of("#")));
        fails(() -> ForgingGridSlots.validatePattern(List.of("#"),Set.of("A")));
        fails(() -> ForgingGridSlots.validatePattern(List.of("#","#","#","#"),Set.of("#")));
        System.out.println("ForgingGridSlotsTest passed (" + checks + " assertions)");
    }
}
