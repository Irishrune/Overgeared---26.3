"""Phase 9 regression checks. These are source-contract and standalone Java tests."""
from pathlib import Path
import json
import re
import unittest

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT/'common/src/main/java/net/stirdrem/overgeared'
FABRIC_GRADLE = (ROOT/'fabric/build.gradle').read_text()

class Phase9ForgingAndCompat(unittest.TestCase):
    def test_real_forging_uses_matched_grid_coordinates_for_component_copy(self):
        s=(SOURCE/'recipe/ForgingRecipe.java').read_text()
        self.assertIn('findMatchOrigin(recipeInput)',s)
        self.assertIn('int inputSlot = ForgingGridSlots.slotForPatternIndex(i, width, originX, originY);',s)
        self.assertIn('ItemStack inputStack = recipeInput.getItem(inputSlot);',s)
        self.assertNotIn('ItemStack input = RecipeInput.getItem(i);',s)
        self.assertRegex(s,r'input\.size\(\) <= BLUEPRINT_SLOT')
        self.assertIn('ForgingGridSlots.validatePattern(pattern, key.keySet())',s)
        smithing=(SOURCE/'block/entity/AbstractSmithingAnvilBlockEntity.java').read_text()
        craft=smithing.split('public void craftItem()',1)[1].split('public boolean',1)[0]
        self.assertIn('recipe.assemble(new RecipeWrapper(itemHandler))',craft)
        self.assertNotIn('recipe.getResultItem(getLevel().registryAccess())',craft)
        self.assertIn("c == ' ' ? ForgingIngredient.EMPTY : key.get(keyStr)",s)

    def test_forging_recipes_have_valid_grid_shapes_and_keys(self):
        recipes=list((ROOT/'common/src/main/resources/data').rglob('*.json'))
        forged=[]
        for file in recipes:
            try:d=json.loads(file.read_text())
            except Exception: continue
            if d.get('type') != 'overgeared:forging':continue
            forged.append(file)
            pat=d['pattern']
            self.assertTrue(1 <= len(pat) <= 3, file)
            width=len(pat[0])
            self.assertTrue(1<=width<=3,file)
            self.assertTrue(all(len(x)==width for x in pat),file)
            self.assertTrue(any(c!=' ' for line in pat for c in line),file)
            self.assertTrue(all(c==' ' or c in d['key'] for line in pat for c in line),file)
        self.assertTrue(forged, 'No forging recipes found to validate')

    def test_unported_optional_adapters_are_not_compiled(self):
        for name in ('jei','emi','sable'):
            self.assertIn(f"java.exclude 'net/stirdrem/overgeared/compat/{name}/**'",FABRIC_GRADLE)
        metadata=json.loads((ROOT/'fabric/src/main/resources/fabric.mod.json').read_text())
        self.assertNotIn('jei_mod_plugin',metadata['entrypoints'])
        self.assertNotIn('emi',metadata['entrypoints'])
        # Only code under the excluded directories may import those APIs.
        external=('mezz.jei','dev.emi','dev.ryanhcode.sable','compat.sable.SableCompat')
        for base in (ROOT/'common/src/main/java',ROOT/'fabric/src/main/java'):
            for p in base.rglob('*.java'):
                if any(f'/compat/{name}/' in str(p) for name in ('jei','emi','sable')):continue
                for line in p.read_text().splitlines():
                    if not line.startswith('import '):continue
                    self.assertFalse(any(x in line for x in external), f'{p}: {line}')

    def test_anvil_distance_is_only_checked_once_per_tick(self):
        code=(SOURCE/'event/ModEvents.java').read_text()
        section=code.split('public static void onPlayerTick(Player entity)',1)[1].split('private static void handleAnvilDistance',1)[0]
        self.assertEqual(section.count('handleAnvilDistance(player, level);'),1)
        self.assertNotIn('SableCompat', code)
        self.assertNotIn('AnvilMinigameEvents.hasAnvilPosition',code)
        self.assertIn('ModItemInteractEvents.playerAnvilPositions.get(player.getUUID())',code)

if __name__=='__main__':unittest.main()
