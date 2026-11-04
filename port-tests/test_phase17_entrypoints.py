"""26.3 actual networking, resource-reload, resource-key, and cooldown signatures."""
from pathlib import Path
import unittest
ROOT = Path(__file__).resolve().parents[1]
FABRIC = (ROOT / "fabric/src/main/java/net/stirdrem/overgeared/fabric/OvergearedFabric.java").read_text(encoding="utf-8")
class FabricEntryPointApiTests(unittest.TestCase):
    def test_packet_registry_directions(self):
        self.assertIn("PayloadTypeRegistry.clientboundPlay()",FABRIC)
        self.assertIn("PayloadTypeRegistry.serverboundPlay()",FABRIC)
        self.assertNotIn("PayloadTypeRegistry.playS2C()",FABRIC)
        self.assertNotIn("PayloadTypeRegistry.playC2S()",FABRIC)
    def test_reload_listener_passes_shared_state(self):
        self.assertIn("void prepareSharedState(SharedState state)",FABRIC)
        self.assertIn("listener.prepareSharedState(state);",FABRIC)
        self.assertIn("CompletableFuture<Void> reload(SharedState state, Executor backgroundExecutor,",FABRIC)
        self.assertIn("listener.reload(state, backgroundExecutor, barrier, gameExecutor)",FABRIC)
        self.assertNotIn("ProfilerFiller",FABRIC)
    def test_identifiers_and_cooldowns(self):
        self.assertIn("injection.table().equals(key.identifier())",FABRIC)
        self.assertIn("player.getCooldowns().isOnCooldown(stack)",FABRIC)
        self.assertNotIn("key.location()",FABRIC)
        self.assertNotIn("isOnCooldown(stack.getItem())",FABRIC)
if __name__ == "__main__":
    unittest.main()
