"""26.3 server-side interaction, cooking and recipe lookup regression checks.

Source checks are not a substitute for a compile or Minecraft behavior test.
"""
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]/"common/src/main/java/net/stirdrem/overgeared"

def source(path):
    return (ROOT/path).read_text(encoding="utf-8")

class Phase31ServerAPITests(unittest.TestCase):
    def test_heated_items_and_knapping_drop_unclaimed_items_on_server(self):
        for name, wanted in [
            ("heateditem/HeatedItem.java", ("newStack", "cooledStack")),
            ("screen/RockKnappingMenu.java", ("result.copy()",)),
        ]:
            with self.subTest(name=name):
                s=source(name)
                self.assertIn("import net.minecraft.util.Prediction;",s)
                for item in wanted:
                    self.assertIn(f"player.drop({item}, false, Prediction.SERVER_ONLY)",s)

    def test_hammer_particles_use_263_rgb_constructor(self):
        s=source("util/ParticleUtils.java")
        self.assertIn("new DustParticleOptions(0xFF8000, 1.0f)",s)
        self.assertIn("serverLevel.sendParticles(ParticleTypes.CRIT",s)
        self.assertNotIn("new Vector3f(",s)

    def test_quality_break_preserves_hand_specific_equipment_slot(self):
        s=source("util/QualityBreak.java")
        self.assertIn("hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND",s)
        self.assertIn("player.onEquippedItemBroken(",s)
        self.assertIn("ForgingQuality.downgrade(stack)",s)
        self.assertNotIn("LivingEntity.getSlotForHand(",s)

    def test_anvil_variants_drop_removed_codec_overrides_not_custom_shapes(self):
        for name in ("SteelSmithingAnvil","StoneSmithingAnvil"):
            with self.subTest(name=name):
                s=source(f"block/custom/{name}.java")
                self.assertNotIn("protected MapCodec<? extends BaseEntityBlock> codec()",s)
                self.assertIn("VoxelShape getShape(",s)
                self.assertIn("getStateForPlacement(",s)

    def test_campfire_recipe_keeps_heat_and_custom_serializer(self):
        s=source("recipe/NBTCampfireRecipe.java")
        self.assertIn("extends AbstractCookingRecipe",s)
        self.assertIn("return Items.CAMPFIRE;",s)
        self.assertIn("return RecipeType.CAMPFIRE_COOKING;",s)
        self.assertIn("return RecipeBookCategories.CAMPFIRE;",s)
        self.assertIn("ModRecipeSerializers.CAMPFIRE_HEATING.get()",s)
        self.assertIn("output.set(ModComponents.HEATED_COMPONENT.get(), true)",s)
        self.assertIn("output.remove(ModComponents.FORGING_QUALITY.get())",s)

    def test_recipe_type_scans_use_public_server_recipe_collection(self):
        s=source("recipe/RecipeLookups.java")
        self.assertIn("level instanceof ServerLevel server ? server.recipeAccess() : null",s)
        self.assertIn("manager.getRecipes().stream()",s)
        self.assertIn("holder.value().getType() == type",s)
        self.assertNotIn("manager.recipeMap()",s)

    def test_permission_level_two_remains_required_for_commands(self):
        s=source("command/ModCommands.java")
        self.assertEqual(2,s.count("source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)"))
        self.assertNotIn("source.hasPermission(2)",s)

    def test_loot_quality_checks_damage_source_context(self):
        s=source("loot/LootQuality.java")
        self.assertIn("context.hasParameter(LootContextParams.DAMAGE_SOURCE)",s)
        self.assertNotIn("context.hasParam(",s)

    def test_tooltype_packet_uses_profile_record_name(self):
        s=source("networking/packet/SelectToolTypeC2SPacket.java")
        self.assertIn("player.getGameProfile().name()",s)
        self.assertNotIn("player.getGameProfile().getName()",s)

if __name__=="__main__":
    unittest.main()
