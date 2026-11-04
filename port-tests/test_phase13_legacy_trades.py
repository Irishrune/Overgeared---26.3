"""Guard against obsolete Java trade code leaking into Fabric 26.3."""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
COMMON = ROOT / "common/src/main/java/net/stirdrem/overgeared"
FABRIC = ROOT / "fabric/src/main/java/net/stirdrem/overgeared/fabric"

class DisabledLegacyTradeBridgeTests(unittest.TestCase):
    def test_legacy_java_classes_excluded(self):
        build = (ROOT / "fabric/build.gradle").read_text(encoding="utf-8")
        for path in (
            "net/stirdrem/overgeared/fabric/FabricTrades.java",
            "net/stirdrem/overgeared/event/QualityWrappedTrade.java",
            "net/stirdrem/overgeared/event/ForgedItemTrade.java",
            "net/stirdrem/overgeared/event/BlueprintWanderingTrade.java",
            "net/stirdrem/overgeared/event/SteelEmeraldTrade.java",
        ):
            self.assertIn(f"java.exclude '{path}'", build)

    def test_server_entrypoint_loads_no_removed_trade_bridge(self):
        src=(FABRIC / "OvergearedFabric.java").read_text(encoding="utf-8")
        self.assertNotIn("FabricTrades.register()",src)

    def test_mod_events_no_longer_compile_removed_trade_symbols(self):
        src=(COMMON / "event/ModEvents.java").read_text(encoding="utf-8")
        self.assertNotIn("VillagerTrades",src)
        self.assertNotIn("onVillagerTrades(",src)
        self.assertIn("onTooltip(",src)

    def test_legacy_trading_behavior_archived_for_future_port(self):
        text=(ROOT / "porting/legacy-trades/ModEvents-trade-methods.txt").read_text(encoding="utf-8")
        for item in ("onVillagerTrades", "onWanderingTraderTrades", "QualityWrappedTrade",
                     "BlueprintWanderingTrade", "addForgedTrades"):
            self.assertIn(item,text)

if __name__ == "__main__":
    unittest.main()
