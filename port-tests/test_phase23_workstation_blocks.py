"""Minecraft 26.3 workstation block lifecycle and interaction regression guards.

These check source-level invariants, not actual block breaking in a Minecraft world.
"""
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]/"common/src/main/java/net/stirdrem/overgeared"
BLOCK_DIR=ROOT/"block/custom"
ENTITY_DIR=ROOT/"block/entity"
SMELTERS=("AlloySmelter","NetherAlloySmelter","CastingSmelter")

class Workstation26_3Tests(unittest.TestCase):
    def test_smelter_block_interactions_and_neighbor_updates(self):
        for name in SMELTERS:
            with self.subTest(name=name):
                source=(BLOCK_DIR/f"{name}Block.java").read_text(encoding="utf-8")
                self.assertIn("EnumProperty<Direction> FACING",source)
                self.assertIn("protected void affectNeighborsAfterRemoval(",source)
                self.assertIn("Containers.updateNeighboursAfterDestroy(state, level, pos)",source)
                self.assertIn("return InteractionResult.SUCCESS;",source)
                self.assertIn("Menus.openMenu(serverPlayer, be, pos)",source)
                self.assertNotIn("onRemove(",source)
                self.assertNotIn("void preRemoveSideEffects(",source)
                self.assertNotIn("MapCodec<",source)
                self.assertNotIn("simpleCodec(",source)
                self.assertNotIn("sidedSuccess(",source)

    def test_smelter_items_drop_via_vanilla_and_extra_xp_via_pre_removal(self):
        for name in SMELTERS:
            with self.subTest(name=name):
                source=(ENTITY_DIR/f"{name}BlockEntity.java").read_text(encoding="utf-8")
                self.assertIn("public void preRemoveSideEffects(BlockPos pos, BlockState state)",source)
                self.assertIn("super.preRemoveSideEffects(pos, state);",source)
                self.assertIn("spawnExperience(storedExperience);",source)
                self.assertIn("storedExperience = 0.0F;",source)

    def test_anvil_drops_once_and_releases_session(self):
        base=(BLOCK_DIR/"BaseSmithingAnvilBlock.java").read_text(encoding="utf-8")
        anvil=(BLOCK_DIR/"AbstractSmithingAnvil.java").read_text(encoding="utf-8")
        entitybase=(ENTITY_DIR/"BaseSmithingAnvilBlockEntity.java").read_text(encoding="utf-8")
        entity=(ENTITY_DIR/"AbstractSmithingAnvilBlockEntity.java").read_text(encoding="utf-8")
        self.assertIn("EnumProperty<Direction> FACING",base)
        self.assertIn("new DustParticleOptions(0xFF8000, 1.0F)",base)
        self.assertIn("protected void affectNeighborsAfterRemoval(",base)
        self.assertNotIn("void onRemove(",base)
        self.assertNotIn("void onRemove(",anvil)
        self.assertIn("void preRemoveSideEffects(BlockPos pos, BlockState state)",entitybase)
        self.assertIn("drops();",entitybase)
        self.assertIn("void preRemoveSideEffects(BlockPos pos, BlockState state)",entity)
        self.assertIn("ModEvents.resetMinigameForAnvil(level, pos)",entity)
        self.assertIn("super.preRemoveSideEffects(pos, state)",entity)

    def test_anvil_hammer_and_falling_survive_api_migration(self):
        anvil=(BLOCK_DIR/"AbstractSmithingAnvil.java").read_text(encoding="utf-8")
        self.assertIn("protected InteractionResult useItemOn(",anvil)
        self.assertIn("AnvilMinigameEvents.handleHit()",anvil)
        self.assertIn("PacketDistributor.sendToServer(new PacketSendCounterC2SPacket(",anvil)
        self.assertIn("anvil.increaseForgingProgress(level, pos, state)",anvil)
        self.assertIn("held.hurtAndBreak(1, player,",anvil)
        self.assertIn("protected void onExplosionHit(BlockState state, ServerLevel level,",anvil)
        self.assertIn("protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,",anvil)
        self.assertIn("ticks.scheduleTick(pos, this, 2)",anvil)
        self.assertIn("FallingBlockEntity.fall(level, pos, state)",anvil)
        self.assertNotIn("ItemInteractionResult",anvil)
        self.assertNotIn("InteractionResult.sidedSuccess",anvil)

if __name__=="__main__":
    unittest.main()
