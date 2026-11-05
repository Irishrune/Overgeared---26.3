package net.stirdrem.overgeared.test;

import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Blocks;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.components.*;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.item.ToolTypeRegistry;
import net.stirdrem.overgeared.recipe.*;
import net.stirdrem.overgeared.util.SmithingTreatments;

public class TreatmentGameTests {
    private static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    private static void near(double a, double b, String message) { check(Math.abs(a-b)<.001, message+": "+a+" != "+b); }
    private static Recipe<?> recipe(GameTestHelper t, String name) {
        return t.getLevel().recipeAccess().getRecipes().stream().filter(r -> r.id().identifier().toString().equals(name))
                .findFirst().orElseThrow().value();
    }
    private static AbstractSmithingAnvilBlockEntity anvil(GameTestHelper t, boolean stone) {
        BlockPos pos=new BlockPos(1,2,1);
        t.setBlock(pos.below(), Blocks.STONE);
        t.setBlock(pos, Blocks.AIR);
        t.setBlock(pos, stone ? ModBlocks.STONE_SMITHING_ANVIL.get() : ModBlocks.SMITHING_ANVIL.get());
        return t.getBlockEntity(pos, AbstractSmithingAnvilBlockEntity.class);
    }
    private static void strikes(AbstractSmithingAnvilBlockEntity a, String sequence) {
        a.setMinigameOn(true);
        int p=0,g=0,m=0;
        for (char hit:sequence.toCharArray()) {
            if (hit=='P') p++; else if (hit=='G') g++; else m++;
            check(a.recordStrikeCounts(p,g,m), "Server rejected valid strike "+p+"/"+g+"/"+m);
            a.increaseForgingProgress(a.getLevel(),a.getBlockPos(),a.getBlockState());
        }
    }
    private static ItemStack namedSword() {
        ItemStack s=new ItemStack(Items.IRON_SWORD);
        s.set(ModComponents.FORGING_QUALITY.get(),ForgingQuality.EXPERT);
        s.set(ModComponents.CREATOR.get(),"Irishrune");
        s.set(DataComponents.CUSTOM_NAME,Component.literal("Jerry's Sword"));
        s.setDamageValue(32);
        return s;
    }

    @GameTest public void treatmentTimingConsumesMaterialsAndPreservesFailures(GameTestHelper t) {
        for (String timing:List.of("PPPP","PGPP","PPPM")) {
            var a=anvil(t,false); ItemStack original=namedSword();
            original.set(ModComponents.TREATMENT.get(),new TreatmentData("heavy",true));
            a.getItemHandler().setStackInSlot(0,original.copy());
            a.getItemHandler().setStackInSlot(1,new ItemStack(Items.COPPER_INGOT,2));
            check(a.hasRecipe(),"Treatment recipe unavailable");
            check(a.getCurrentRecipe().orElseThrow().isTreatment(),"Wrong recipe selected");
            strikes(a,timing);
            ItemStack result=a.getItemHandler().getStackInSlot(10);
            check(a.getItemHandler().getStackInSlot(0).isEmpty(),"Equipment duplicated");
            check(a.getItemHandler().getStackInSlot(1).getCount()==1,"Wrong treatment cost");
            if (timing.contains("M")) check(ItemStack.matches(original,result),"Failed treatment altered original");
            else {
                check(result.get(ModComponents.TREATMENT.get()).equals(new TreatmentData("balanced",timing.equals("PPPP"))),"Incorrect treatment result");
                check(result.getDamageValue()==32 && result.get(ModComponents.CREATOR.get()).equals("Irishrune"),"Lost wear/creator");
                check(result.get(ModComponents.FORGING_QUALITY.get())==ForgingQuality.EXPERT,"Rerolled quality");
                check(result.getHoverName().getString().equals("Jerry's Sword"),"Lost custom name");
            }
        }
        t.succeed();
    }

    @GameTest public void treatmentRestrictionsAndSessionValidation(GameTestHelper t) {
        var a=anvil(t,true);
        a.getItemHandler().setStackInSlot(0,new ItemStack(Items.STONE_SWORD));
        a.getItemHandler().setStackInSlot(1,new ItemStack(Items.COPPER_INGOT));
        check(!a.hasRecipe(),"Stone anvil allowed treatment");
        a=anvil(t,false);
        a.getItemHandler().setStackInSlot(0,new ItemStack(Items.STONE_SWORD));
        a.getItemHandler().setStackInSlot(1,new ItemStack(Items.COPPER_INGOT));
        check(a.hasRecipe(),"Stone equipment cannot be treated on iron");
        a.setMinigameOn(true);
        check(!a.recordStrikeCounts(4,0,0),"Skipped strikes accepted");
        check(a.recordStrikeCounts(1,0,0),"Valid strike rejected");
        check(!a.recordStrikeCounts(1,0,0),"Duplicate count accepted");
        a.increaseForgingProgress(a.getLevel(),a.getBlockPos(),a.getBlockState());
        ItemStack replacement=new ItemStack(Items.STONE_SWORD); replacement.setDamageValue(1);
        a.getItemHandler().setStackInSlot(0,replacement);
        check(!a.recordStrikeCounts(2,0,0),"Swapped input accepted mid-treatment");
        check(!SmithingTreatments.canTreat(new ItemStack(Items.DIAMOND_SWORD),"balanced"),"Diamond directly treatable");
        check(!SmithingTreatments.canTreat(new ItemStack(Items.NETHERITE_CHESTPLATE),"vitality"),"Netherite directly treatable");
        ItemStack flawless=SmithingTreatments.apply(namedSword(),new TreatmentData("balanced",true));
        check(ItemStack.matches(flawless,SmithingTreatments.apply(flawless,new TreatmentData("balanced",false))),"Flawless treatment downgraded");
        t.succeed();
    }

    @GameTest public void treatmentAttributesAreRealAndDoNotStack(GameTestHelper t) {
        ItemStack sword=new ItemStack(Items.IRON_SWORD);
        sword.set(ModComponents.TREATMENT.get(),new TreatmentData("balanced",false));
        for(int i=0;i<3;i++) {
            double[] stats={1,4};
            sword.forEachModifier(EquipmentSlot.MAINHAND,(attribute,m)->{
                if(attribute.equals(Attributes.ATTACK_DAMAGE))stats[0]+=m.amount();
                if(attribute.equals(Attributes.ATTACK_SPEED))stats[1]+=m.amount();
            });
            near(stats[0],6*.95,"Damage penalty"); near(stats[1],1.6*1.08,"Attack speed buff");
        }
        double[] offhand={0}; sword.forEachModifier(EquipmentSlot.OFFHAND,(a,m)->offhand[0]+=m.amount());
        near(offhand[0],0,"Offhand granted weapon buff");
        ItemStack pick=new ItemStack(Items.IRON_PICKAXE);
        pick.set(ModComponents.TREATMENT.get(),new TreatmentData("sturdy",false));
        check(pick.getMaxDamage()==288,"Sturdy durability missing");
        near(pick.getDestroySpeed(Blocks.STONE.defaultBlockState()),5.7,"Sturdy mining penalty missing");
        pick.set(ModComponents.TREATMENT.get(),new TreatmentData("sturdy",true));
        near(pick.getDestroySpeed(Blocks.STONE.defaultBlockState()),6,"Flawless still has penalty");
        t.succeed();
    }

    @GameTest public void vitalityEquipmentDoesNotHealOrStack(GameTestHelper t) {
        var mob=t.spawn(EntityTypes.HUSK,new BlockPos(1,2,1)); mob.setNoAi(true);
        mob.setHealth(10);
        ItemStack chest=new ItemStack(Items.IRON_CHESTPLATE);
        chest.set(ModComponents.TREATMENT.get(),new TreatmentData("vitality",true));
        mob.setItemSlot(EquipmentSlot.CHEST,chest);
        t.runAfterDelay(3,()->{
            near(mob.getMaxHealth(),24,"Vitality max health"); near(mob.getHealth(),10,"Equipping healed player");
            mob.setItemSlot(EquipmentSlot.CHEST,chest.copy());
            t.runAfterDelay(3,()->{
                near(mob.getMaxHealth(),24,"Repeated equip stacked health");
                mob.setHealth(24); mob.setItemSlot(EquipmentSlot.CHEST,ItemStack.EMPTY);
                t.runAfterDelay(3,()->{
                    near(mob.getMaxHealth(),20,"Unequip retained extra health");
                    near(mob.getHealth(),20,"Unequip did not clamp health"); t.succeed();
                });
            });
        });
    }

    @GameTest public void treatmentsSurviveAssemblySavingAndUpgrades(GameTestHelper t) {
        ItemStack blade=new ItemStack(ModItems.STEEL_SWORD_BLADE.get());
        blade.set(ModComponents.FORGING_QUALITY.get(),ForgingQuality.MASTER);
        blade.set(ModComponents.TREATMENT.get(),new TreatmentData("balanced",true));
        blade.set(ModComponents.POLISHED.get(),true);
        blade.set(ModComponents.CREATOR.get(),"Irishrune");
        var craft=(CraftingRecipe)recipe(t,"overgeared:steel_sword");
        ItemStack steel=craft.assemble(CraftingInput.of(2,1,List.of(blade,new ItemStack(Items.STICK))));
        check(steel.get(ModComponents.TREATMENT.get()).equals(blade.get(ModComponents.TREATMENT.get())),"Assembly lost treatment");
        var enchantments = new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
        enchantments.set(t.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                .getOrThrow(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING), 2);
        steel.set(DataComponents.ENCHANTMENTS, enchantments.toImmutable());
        steel.set(DataComponents.CUSTOM_NAME,Component.literal("Family Sword")); steel.setDamageValue(steel.getMaxDamage()/2);
        var ops=t.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
        var saved=ItemStack.CODEC.encodeStart(ops,steel).getOrThrow();
        steel=ItemStack.CODEC.parse(ops,saved).getOrThrow();
        var buffer=new RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),t.getLevel().registryAccess());
        ItemStack.STREAM_CODEC.encode(buffer,steel);
        check(ItemStack.matches(steel,ItemStack.STREAM_CODEC.decode(buffer)),"Network lost components"); buffer.release();
        var diamondRecipe=(SmithingTransformRecipe)recipe(t,"overgeared:steel_sword_to_diamond_sword");
        var input=new SmithingRecipeInput(new ItemStack(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get()),steel,new ItemStack(Items.DIAMOND));
        check(diamondRecipe.matches(input,t.getLevel()),"Diamond upgrade rejected steel");
        ItemStack diamond=diamondRecipe.assemble(input);
        check(diamond.is(Items.DIAMOND_SWORD),"Wrong upgrade output");
        int expected=(int)Math.ceil((double)steel.getDamageValue()*diamond.getMaxDamage()/steel.getMaxDamage());
        check(diamond.getDamageValue()==expected,"Diamond upgrade repaired wear");
        var netherRecipe=(SmithingTransformRecipe)recipe(t,"minecraft:netherite_sword_smithing");
        var netherInput=new SmithingRecipeInput(new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),diamond,new ItemStack(Items.NETHERITE_INGOT));
        check(netherRecipe.matches(netherInput,t.getLevel()),"Netherite upgrade invalid");
        ItemStack nether=netherRecipe.assemble(netherInput);
        check(nether.is(Items.NETHERITE_SWORD),"Wrong netherite output");
        for(ItemStack result:List.of(diamond,nether)) {
            check(result.get(ModComponents.TREATMENT.get()).equals(new TreatmentData("balanced",true)),"Upgrade lost flawless treatment");
            check(result.get(ModComponents.FORGING_QUALITY.get())==ForgingQuality.MASTER,"Upgrade lost quality");
            check(result.getHoverName().getString().equals("Family Sword"),"Upgrade lost name");
            check(result.get(ModComponents.CREATOR.get()).equals("Irishrune"),"Upgrade lost creator");
            check(result.get(DataComponents.ENCHANTMENTS).equals(enchantments.toImmutable()),"Upgrade lost enchantments");
        }
        t.succeed();
    }

    private static void armorInputs(AbstractSmithingAnvilBlockEntity a, Item plate, String quality, int uses) {
        for(int i=0;i<12;i++)a.getItemHandler().setStackInSlot(i,ItemStack.EMPTY);
        for(int i:new int[]{0,2,3,4,5,6,7,8})a.getItemHandler().setStackInSlot(i,new ItemStack(plate));
        if(quality!=null){
            ItemStack bp=new ItemStack(ModItems.BLUEPRINT.get());
            bp.set(ModComponents.BLUEPRINT_DATA.get(),new BlueprintData(quality,"chestplate",uses));
            a.getItemHandler().setStackInSlot(11,bp);
        }
    }
    @GameTest public void armorBlueprintsProgressToMasterworkAndStoneStaysPrimitive(GameTestHelper t) {
        for(String type:List.of("helmet","chestplate","leggings","boots"))check(ToolTypeRegistry.byId(type).isPresent(),"Armor blueprint absent: "+type);
        for(String quality:new String[]{null,"well","expert","master"}) {
            var a=anvil(t,false);armorInputs(a,ModItems.IRON_PLATE.get(),quality,0);
            check(a.hasRecipe(),"Armor recipe missing");strikes(a,"PPPPP");
            ForgingQuality expected=quality==null?ForgingQuality.WELL:ForgingQuality.fromString(quality);
            check(a.getItemHandler().getStackInSlot(10).get(ModComponents.FORGING_QUALITY.get())==expected,"Wrong armor cap: "+quality);
        }
        var a=anvil(t,false);armorInputs(a,ModItems.STEEL_PLATE.get(),"perfect",28);
        strikes(a,"PPPPP");
        check(a.getItemHandler().getStackInSlot(11).get(ModComponents.BLUEPRINT_DATA.get()).quality().equals("master"),"Perfect blueprint did not reach Masterwork");
        a=anvil(t,true);armorInputs(a,ModItems.COPPER_PLATE.get(),"master",0);
        strikes(a,"PPPPP");
        check(a.getItemHandler().getStackInSlot(10).get(ModComponents.FORGING_QUALITY.get())==ForgingQuality.WELL,"Stone bypassed cap");
        t.succeed();
    }

    @GameTest public void diamondTemplateAndTreatmentRecipeCodec(GameTestHelper t) {
        var craft=(CraftingRecipe)recipe(t,"overgeared:diamond_upgrade_smithing_template");
        ItemStack d=new ItemStack(Items.DIAMOND);
        var input=CraftingInput.of(3,3,List.of(d,d,d,d,new ItemStack(ModItems.EMPTY_BLUEPRINT.get()),d,
                ItemStack.EMPTY,new ItemStack(ModItems.STEEL_INGOT.get()),ItemStack.EMPTY));
        check(craft.matches(input,t.getLevel())&&craft.assemble(input).getCount()==1,"Template recipe wrong");
        var treatment=(ForgingRecipe)recipe(t,"overgeared:treatment/balanced/iron_sword");
        var buffer=new RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),t.getLevel().registryAccess());
        ForgingRecipe.Serializer.STREAM_CODEC.encode(buffer,treatment);
        var copy=ForgingRecipe.Serializer.STREAM_CODEC.decode(buffer); buffer.release();
        check(copy.treatment().equals("balanced")&&copy.getHammeringRequired()==4,"Treatment recipe sync lost flags");
        t.succeed();
    }
}
