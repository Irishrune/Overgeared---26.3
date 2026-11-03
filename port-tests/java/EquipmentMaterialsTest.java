import net.stirdrem.overgeared.item.ModToolTiers;
import net.stirdrem.overgeared.item.ModArmorMaterials;
import net.stirdrem.overgeared.item.EquipmentStatMath;
import net.minecraft.world.item.equipment.ArmorType;

public final class EquipmentMaterialsTest {
    private static void eq(float actual, float expected) {
        if (Math.abs(actual - expected)>0.00001f) throw new AssertionError("expected "+expected+", got "+actual);
    }
    private static void eq(int actual, int expected) {
        if(actual!=expected) throw new AssertionError("expected "+expected+", got "+actual);
    }
    public static void main(String[] args) {
        eq(ModToolTiers.STEEL.durability(),500);
        eq(ModToolTiers.STEEL.speed(),7f);
        eq(ModToolTiers.STEEL.attackDamageBonus(),3f);
        eq(ModToolTiers.COPPER.durability(),190);
        eq(ModToolTiers.COPPER.speed(),5f);
        eq(ModToolTiers.COPPER.attackDamageBonus(),1f);
        eq(ModArmorMaterials.STEEL.durability(),26);
        eq(ModArmorMaterials.COPPER.durability(),10);
        eq(ModArmorMaterials.STEEL.defense().get(ArmorType.HELMET),3);
        eq(ModArmorMaterials.STEEL.defense().get(ArmorType.CHESTPLATE),7);
        eq(ModArmorMaterials.STEEL.defense().get(ArmorType.LEGGINGS),5);
        eq(ModArmorMaterials.STEEL.defense().get(ArmorType.BOOTS),2);
        eq(ModArmorMaterials.COPPER.defense().get(ArmorType.HELMET),1);
        eq(ModArmorMaterials.COPPER.defense().get(ArmorType.CHESTPLATE),4);
        eq(ModArmorMaterials.COPPER.defense().get(ArmorType.LEGGINGS),3);
        eq(ModArmorMaterials.COPPER.defense().get(ArmorType.BOOTS),1);
        eq(ModArmorMaterials.getDurabilityForType(ModArmorMaterials.STEEL,ArmorType.HELMET),286);
        eq(ModArmorMaterials.getDurabilityForType(ModArmorMaterials.COPPER,ArmorType.BOOTS),130);
        eq(ModArmorMaterials.STEEL.toughness(),1.0f);
        eq(ModArmorMaterials.COPPER.toughness(),0.0f);
        eq(ModToolTiers.STEEL.enchantmentValue(),12);
        eq(ModToolTiers.COPPER.enchantmentValue(),12);
        float[] damageSteel = {6f,4f,8f,0f,4f};
        float[] damageCopper = {4f,2f,6f,0f,2.5f};
        for(float dmg:damageSteel) eq(EquipmentStatMath.attackBaseline(dmg,3f)+3f,dmg);
        for(float dmg:damageCopper) eq(EquipmentStatMath.attackBaseline(dmg,1f)+1f,dmg);
        boolean rejected = false;
        try { EquipmentStatMath.attackBaseline(Float.NaN, 1f); } catch(IllegalArgumentException e){rejected=true;}
        if(!rejected) throw new AssertionError("Nonfinite damage must be rejected");
        System.out.println("EquipmentMaterialsTest passed");
    }
}
