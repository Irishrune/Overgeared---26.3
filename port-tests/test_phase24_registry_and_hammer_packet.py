"""Regression checks for Minecraft 26.3 registry, blueprint and hammer networking ports."""
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]/"common/src/main/java/net/stirdrem/overgeared"
HOLDER=(ROOT/"registry/DeferredHolder.java").read_text(encoding="utf-8")
REGISTER=(ROOT/"registry/DeferredRegister.java").read_text(encoding="utf-8")
PACKET=(ROOT/"networking/packet/PacketSendCounterC2SPacket.java").read_text(encoding="utf-8")
WORKBENCH=(ROOT/"block/custom/BlueprintWorkbenchBlock.java").read_text(encoding="utf-8")

class Phase24Tests(unittest.TestCase):
    def test_deferred_holder_uses_263_registry_apis(self):
        self.assertIn("BuiltInRegistries.REGISTRY.getValue(key.registry())", HOLDER)
        self.assertIn("holder = registry.get(key)", HOLDER)
        self.assertIn("key.identifier()", HOLDER)
        self.assertNotIn("key.location()", HOLDER)
        self.assertNotIn("registry.getHolder(key)", HOLDER)
        self.assertIn("registryKey.identifier()", REGISTER)
        self.assertNotIn("registryKey.location()", REGISTER)

    def test_hammer_packet_encodes_identifiers_with_stream_codec(self):
        self.assertIn("Identifier.STREAM_CODEC.encode(buffer, packet.hitSound)", PACKET)
        self.assertIn("Identifier.STREAM_CODEC.encode(buffer, packet.completeSound)", PACKET)
        self.assertIn("Identifier.STREAM_CODEC.encode(buffer, packet.failedSound)", PACKET)
        self.assertEqual(3, PACKET.count("Identifier.STREAM_CODEC.decode(buffer)"))
        self.assertNotIn("readResourceLocation()", PACKET)
        self.assertNotIn("writeResourceLocation(", PACKET)

    def test_hammer_packets_require_server_ownership_and_real_hammer(self):
        checks=[
            "player.isWithinBlockInteractionRange(pos, 0.0D)",
            "player.getUUID().equals(anvilBE.getOwnerUUID())",
            "anvilBE.isMinigameOn()",
            "mainHand.is(ModTags.Items.SMITHING_HAMMERS)",
            "mainHand.hurtAndBreak(1, player, EquipmentSlot.MAINHAND)",
        ]
        for check in checks:
            self.assertIn(check, PACKET)
        self.assertLess(PACKET.index("player.getUUID().equals(anvilBE.getOwnerUUID())"),
                        PACKET.index("anvilBlock.setQuality("))
        self.assertLess(PACKET.index("mainHand.is(ModTags.Items.SMITHING_HAMMERS)"),
                        PACKET.index("anvilBE.increaseForgingProgress("))
        for sound in ("FORGING_FAILED","FORGING_COMPLETE","ANVIL_HIT"):
            self.assertIn(f"ModSounds.{sound}.get()",PACKET)
        self.assertNotIn("BuiltInRegistries.SOUND_EVENT.get",PACKET)

    def test_blueprint_workbench_keeps_menu_and_facing(self):
        self.assertIn("EnumProperty<Direction> FACING",WORKBENCH)
        self.assertIn("new BlueprintWorkbenchMenu(containerId, inventory)",WORKBENCH)
        self.assertNotIn("DirectionProperty",WORKBENCH)

if __name__=="__main__":
    unittest.main()
