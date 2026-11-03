package net.minecraft.world.item.equipment;
public enum ArmorType {HELMET(11), CHESTPLATE(16), LEGGINGS(15), BOOTS(13);
private final int factor; ArmorType(int factor){this.factor=factor;} public int getDurability(int amount){return factor*amount;} }
