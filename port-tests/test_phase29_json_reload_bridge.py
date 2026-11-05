"""Regression guards for the Minecraft 26.3 raw-JSON reload bridge.

These verify compatibility of the source interfaces, not actual datapack
loading or /reload behavior; those still need an in-game test.
"""
from pathlib import Path
import unittest

DIR=Path(__file__).resolve().parents[1]/"common/src/main/java/net/stirdrem/overgeared/datapack"
BRIDGE=(DIR/"LegacyJsonResourceReloadListener.java").read_text(encoding="utf-8")
LISTENERS=(
    "BlueprintTooltypesReloadListener",
    "BreakSystemBlacklistReloadListener",
    "CastingToolTypesReloadListener",
    "DurabilityBlacklistReloadListener",
    "GrindingBlacklistReloadListener",
    "KnappingResourceReloadListener",
    "MaterialSettingsReloadListener",
    "QualityAttributeReloadListener",
    "RockInteractionReloadListener",
)

class DatapackReload26_3Tests(unittest.TestCase):
    def test_raw_json_adapter_uses_codec_and_vanilla_file_converter(self):
        self.assertIn("extends SimpleJsonResourceReloadListener<JsonElement>",BRIDGE)
        self.assertIn("Codec.PASSTHROUGH.xmap(",BRIDGE)
        self.assertIn("dynamic.convert(JsonOps.INSTANCE).getValue()",BRIDGE)
        self.assertIn("new Dynamic<>(JsonOps.INSTANCE, json)",BRIDGE)
        self.assertIn("super(RAW_JSON, FileToIdConverter.json(directory))",BRIDGE)

    def test_all_nine_original_reloaders_keep_their_data_processors(self):
        for name in LISTENERS:
            with self.subTest(listener=name):
                s=(DIR/f"{name}.java").read_text(encoding="utf-8")
                self.assertIn("extends LegacyJsonResourceReloadListener",s)
                self.assertRegex(s, r"protected\s+(?:synchronized\s+)?void apply\(")
                self.assertIn("Map<Identifier, JsonElement>",s)
                self.assertNotIn("extends SimpleJsonResourceReloadListener",s)
                self.assertNotIn("import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;",s)
                self.assertIn('super(',s)

if __name__=="__main__":
    unittest.main()
