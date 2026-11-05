"""Source regression checks for 26.3 forging/knapping advancement triggers.

They guard codec fields and trigger invocation but not in-game advancement awards.
"""
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]/"common/src/main/java/net/stirdrem/overgeared/advancement"
TRIGGERS={
    "ForgingQualityTrigger": "quality",
    "MakeSmithingAnvilTrigger": "tier",
    "KnappingAdvancementTrigger": None,
    "BlueprintQualityTrigger": "quality",
    "MaxLevelBlueprintAdvancementTrigger": None,
}

class Advancement26_3Tests(unittest.TestCase):
    def test_every_trigger_uses_migrated_package_and_holder_condition_codec(self):
        for name,field in TRIGGERS.items():
            with self.subTest(name=name):
                s=(ROOT/(name+".java")).read_text(encoding="utf-8")
                self.assertIn("import net.minecraft.advancements.triggers.SimpleCriterionTrigger;",s)
                self.assertIn("import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;",s)
                self.assertIn("import net.minecraft.core.Holder;",s)
                self.assertIn("Optional<Holder<LootItemCondition>> player",s)
                self.assertIn('LootItemCondition.CODEC.optionalFieldOf("player")',s)
                self.assertIn("implements SimpleCriterionTrigger.SimpleInstance",s)
                self.assertIn("public Codec<TriggerInstance> codec()",s)
                self.assertNotIn("ContextAwarePredicate",s)
                self.assertNotIn("net.minecraft.advancements.critereon",s)
                if field:
                    self.assertIn(f'Codec.STRING.optionalFieldOf("{field}")',s)

    def test_forging_and_blueprint_advancements_keep_quality_filters(self):
        for name in ("ForgingQualityTrigger","BlueprintQualityTrigger"):
            with self.subTest(name=name):
                s=(ROOT/(name+".java")).read_text(encoding="utf-8")
                self.assertIn("requiredQuality.isEmpty()",s)
                self.assertIn("requiredQuality.get().equals(",s)

    def test_knapping_and_max_level_advancements_still_fire(self):
        for name in ("KnappingAdvancementTrigger","MaxLevelBlueprintAdvancementTrigger"):
            with self.subTest(name=name):
                s=(ROOT/(name+".java")).read_text(encoding="utf-8")
                self.assertIn("this.trigger(player, instance -> true)",s)

    def test_original_registry_identifiers_retained(self):
        s=(ROOT/"ModAdvancementTriggers.java").read_text(encoding="utf-8")
        self.assertIn("import net.minecraft.advancements.triggers.CriterionTrigger;",s)
        for key in ("make_smithing_anvil","finished_knapping","forging_quality",
                    "blueprint_quality","max_level_blueprint"):
            self.assertIn(f'.register("{key}"',s)

if __name__=="__main__":
    unittest.main()
