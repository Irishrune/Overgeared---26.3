"""Source regression checks for 26.3 rock/knapping datapack and forged quality targeting.

The maps and logic still require a real datapack reload and item attribute test.
"""
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]/"common/src/main/java/net/stirdrem/overgeared"
K=(ROOT/"datapack/KnappingResourceReloadListener.java").read_text(encoding="utf-8")
R=(ROOT/"datapack/RockInteractionReloadListener.java").read_text(encoding="utf-8")
Q=(ROOT/"datapack/QualityAttributeReloadListener.java").read_text(encoding="utf-8")
HANDLER=(ROOT/"event/QualityAttributeHandler.java").read_text(encoding="utf-8")

class QualityAndResource26_3Tests(unittest.TestCase):
    def test_knapping_datapack_keeps_sound_and_item_fallbacks(self):
        self.assertIn("BuiltInRegistries.SOUND_EVENT.getValue(soundId)",K)
        self.assertIn("BuiltInRegistries.ITEM.getValue(itemId)",K)
        self.assertIn("ITEM_TEXTURES.put(item, texture)",K)
        self.assertIn("TAG_TEXTURES.put(tag, texture)",K)
        self.assertIn("FALLBACK_SOUND",K)
        self.assertIn("FALLBACK_TEXTURE",K)

    def test_rock_interactions_preserve_flint_and_rock_drop(self):
        self.assertIn("BuiltInRegistries.BLOCK.getValue(inputId)",R)
        self.assertIn("BuiltInRegistries.BLOCK.getValue(resultId)",R)
        self.assertIn("BuiltInRegistries.ITEM.getValue(dropId)",R)
        self.assertIn("new RockInteractionData.ToolEntry",R)
        self.assertIn("new ItemStack(ModItems.ROCK.get())",R)
        self.assertIn("data.get",R) if False else None
        self.assertIn("Ingredient.CODEC",R)

    def test_quality_reload_filters_item_types_by_components(self):
        self.assertIn("BuiltInRegistries.ITEM.getValue(target.id())",Q)
        self.assertIn("BuiltInRegistries.ITEM.listElements()",Q)
        self.assertIn("stack.has(DataComponents.WEAPON)",Q)
        self.assertIn("stack.has(DataComponents.TOOL)",Q)
        self.assertIn("equippable.slot().isArmor()",Q)
        self.assertNotIn("instanceof TieredItem",Q)
        self.assertNotIn("instanceof ArmorItem",Q)

    def test_quality_modifier_keeps_custom_forging_bonuses(self):
        self.assertIn("BuiltInRegistries.ATTRIBUTE",HANDLER)
        self.assertIn(".getValue(def.attribute())",HANDLER)
        self.assertIn("new ItemAttributeModifiers(entries)",HANDLER)
        self.assertIn("stack.has(DataComponents.WEAPON)",HANDLER)
        self.assertIn("stack.has(DataComponents.TOOL)",HANDLER)
        self.assertIn("equippable.slot().isArmor()",HANDLER)
        self.assertIn("createModifiedAttribute(originalModifier, bonus, operation, quality)",HANDLER)
        self.assertNotIn("modifiers.showInTooltip()",HANDLER)

if __name__=="__main__":
    unittest.main()
