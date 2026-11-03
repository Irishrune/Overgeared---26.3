package net.stirdrem.overgeared.recipe;

import java.util.List;
import java.util.Set;

/**
 * Coordinate helpers for a movable forging pattern in the 3 x 3 anvil grid.
 * No Minecraft runtime dependencies, allowing direct regression tests.
 */
public final class ForgingGridSlots {
    private static final int GRID = 3;

    private ForgingGridSlots() { }

    /** A packed pattern index is not necessarily the input grid slot. */
    public static int slotForPatternIndex(int index, int width, int originX, int originY) {
        if (width < 1 || width > GRID || index < 0 || index >= width * GRID)
            throw new IllegalArgumentException("Invalid pattern index or width");
        int x = originX + index % width;
        int y = originY + index / width;
        if (x < 0 || y < 0 || x >= GRID || y >= GRID)
            throw new IllegalArgumentException("Pattern cell outside anvil grid");
        return y * GRID + x;
    }

    /** Reject malformed datapack grids and undefined nonblank keys. */
    public static void validatePattern(List<String> rows, Set<String> keys) {
        if (rows == null || rows.isEmpty() || rows.size() > GRID)
            throw new IllegalArgumentException("Forging pattern requires 1–3 rows");
        if (keys == null) throw new IllegalArgumentException("Null forging keys");
        int width = rows.getFirst() == null ? 0 : rows.getFirst().length();
        if (width < 1 || width > GRID)
            throw new IllegalArgumentException("Forging pattern width must be 1–3");
        boolean material = false;
        for (String row : rows) {
            if (row == null || row.length() != width)
                throw new IllegalArgumentException("Forging pattern row width mismatch");
            for (int x = 0; x < width; x++) {
                char symbol = row.charAt(x);
                if (symbol == ' ') continue;
                material = true;
                if (!keys.contains(String.valueOf(symbol)))
                    throw new IllegalArgumentException("Undefined forging key: " + symbol);
            }
        }
        if (!material) throw new IllegalArgumentException("Forging pattern has no ingredients");
    }
}
