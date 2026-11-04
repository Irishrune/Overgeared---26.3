"""Server-owned dimension tracking for smithing anvil sessions."""
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]
FABRIC=(ROOT/"fabric/src/main/java/net/stirdrem/overgeared/fabric/OvergearedFabric.java").read_text(encoding="utf-8")
EVENTS=(ROOT/"common/src/main/java/net/stirdrem/overgeared/event/ModEvents.java").read_text(encoding="utf-8")
INTERACT=(ROOT/"common/src/main/java/net/stirdrem/overgeared/event/ModItemInteractEvents.java").read_text(encoding="utf-8")

class Phase16DimensionSessionTests(unittest.TestCase):
    def test_no_removed_fabric_dimension_event(self):
        self.assertNotIn("ServerEntityWorldChangeEvents",FABRIC)
        self.assertIn("ServerPlayerEvents.AFTER_RESPAWN.register",FABRIC)
        self.assertIn("LAST_PLAYER_WORLDS.remove(player.getUUID());",FABRIC)
        self.assertIn("LAST_PLAYER_WORLDS.clear();",FABRIC)

    def test_dimension_transition_releases_origin_world_anvil(self):
        self.assertIn("previousWorld != currentWorld",FABRIC)
        self.assertIn("resetMinigameForPlayer(player, previousWorld)",FABRIC)
        self.assertIn("BlockEntity be = anvilWorld.getBlockEntity(anvilPos);",EVENTS)
        self.assertIn("releaseAnvil(player, anvilPos, anvilWorld);",EVENTS)
        self.assertIn("BlockEntity be = anvilWorld.getBlockEntity(pos);",INTERACT)

    def test_orphaned_session_cleared_even_when_anvil_is_missing(self):
        self.assertIn("if (anvilPos != null)",EVENTS)
        self.assertIn("ModItemInteractEvents.playerAnvilPositions.remove(playerId);",EVENTS)
        self.assertIn("ModItemInteractEvents.playerMinigameVisibility.remove(playerId);",EVENTS)

if __name__=="__main__":
    unittest.main()
