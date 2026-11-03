package net.stirdrem.overgeared.item;

/** Preserve pre-port attack damage when switching to ToolMaterial+Properties. */
public final class EquipmentStatMath {
    private EquipmentStatMath() {}

    /** Tool material's bonus is added by Properties.{sword,pickaxe,...}. */
    public static float attackBaseline(float legacyTotalDamage, float materialBonus) {
        if (!Float.isFinite(legacyTotalDamage) || !Float.isFinite(materialBonus)) {
            throw new IllegalArgumentException("Attack damage inputs must be finite");
        }
        return legacyTotalDamage - materialBonus;
    }
}
