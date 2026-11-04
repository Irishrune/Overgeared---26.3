"""Targeted source guards for Fabric 26.3 callback/builder port fixes.
These are source-level assertions, not a Minecraft runtime or compile test.
"""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
FABRIC = ROOT / "fabric/src/main/java/net/stirdrem/overgeared/fabric"

class Fabric263CallbackMigrationTests(unittest.TestCase):
    def test_item_callback_uses_interaction_result(self):
        source = (FABRIC / "OvergearedFabric.java").read_text(encoding="utf-8")
        self.assertNotIn("InteractionResultHolder", source)
        self.assertIn("UseItemCallback.EVENT.register", source)
        self.assertIn("return event.getCancellationResult();", source)
        self.assertGreaterEqual(source.count("return InteractionResult.PASS;"), 2)

    def test_creative_group_uses_vanilla_builder(self):
        source = (FABRIC / "FabricPlatformHelper.java").read_text(encoding="utf-8")
        self.assertNotIn("FabricItemGroup", source)
        self.assertIn("return CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0);", source)

    def test_config_registry_uses_26_3_v5_api(self):
        source = (FABRIC / "OvergearedFabric.java").read_text(encoding="utf-8")
        self.assertIn("fabric.api.v5.ConfigRegistry", source)
        self.assertIn("ConfigRegistry.INSTANCE.register(", source)
        self.assertNotIn("NeoForgeConfigRegistry", source)

    def test_compiler_reports_more_than_first_100_errors(self):
        source = (ROOT / "fabric/build.gradle").read_text(encoding="utf-8")
        self.assertIn("'1000'", source)
        self.assertIn("-Xmaxerrs", source)

if __name__ == "__main__":
    unittest.main()
