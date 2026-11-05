"""Minecraft 26.3 block-entity registration regression guards.

The seven existing workstation IDs must remain stable while switching from
the removed vanilla builder to Fabric's 26.3 object-builder API.
"""
from pathlib import Path
import re
import unittest

ROOT=Path(__file__).resolve().parents[1]
SRC=(ROOT / "common/src/main/java/net/stirdrem/overgeared/block/entity/ModBlockEntities.java").read_text(encoding="utf-8")
EXPECTED={
    "smithing_table_be",
    "tier_a_smithing_table_be",
    "tier_b_smithing_table_be",
    "stone_smithing_table_be",
    "alloy_furnace_be",
    "nether_alloy_furnace_be",
    "casting_furnace_be",
}

class BlockEntityRegistrationTests(unittest.TestCase):
    def test_seven_fabric_263_types_retain_identifiers(self):
        found=set(re.findall(r'BLOCK_ENTITIES.register\("([^"]+)"',SRC))
        self.assertEqual(EXPECTED,found)
        self.assertIn("import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;",SRC)
        self.assertEqual(7,SRC.count("FabricBlockEntityTypeBuilder.create("))
        self.assertNotIn("BlockEntityType.Builder.of(",SRC)
        self.assertNotIn(".build(null)",SRC)
        self.assertEqual(7,len(re.findall(r"\.\s*build\(\)", SRC)))

if __name__=="__main__":
    unittest.main()
