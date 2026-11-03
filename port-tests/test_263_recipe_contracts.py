"""Phase 7 source migration regression tests, not a Minecraft compile."""
import re
import shutil
import subprocess
import tempfile
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DIR = ROOT/'common/src/main/java/net/stirdrem/overgeared/recipe'

DIRECT = {
    'ForgingRecipe':'RecipeInput',
    'CastingRecipe':'RecipeInput',
    'FletchingRecipe':'RecipeInput',
    'RockKnappingRecipe':'RecipeInput',
    'GrindingRecipe':'SingleRecipeInput',
    'CoolingRecipe':'SingleRecipeInput',
    'AlloySmeltingRecipe':'RecipeInput',
    'NetherAlloySmeltingRecipe':'RecipeInput',
    'ShapedAlloySmeltingRecipe':'RecipeInput',
    'ShapedNetherAlloySmeltingRecipe':'RecipeInput',
    'ItemToToolTypeRecipe':'SingleRecipeInput',
}
TEMPLATED = {
    'RockKnappingRecipe':'result',
    'GrindingRecipe':'result',
    'CoolingRecipe':'result',
    'AlloySmeltingRecipe':'output',
    'NetherAlloySmeltingRecipe':'result',
}

class Phase7RecipePortTests(unittest.TestCase):
    def test_station_recipes_implement_modern_generic_contract(self):
        for name, generic in DIRECT.items():
            with self.subTest(recipe=name):
                text=(DIR/(name+'.java')).read_text()
                self.assertIn(f'implements OvergearedStationRecipe<{generic}>',text)
                self.assertRegex(text, rf'public RecipeSerializer<\? extends Recipe<{generic}>> getSerializer\(')
                self.assertRegex(text, rf'public RecipeType<\? extends Recipe<{generic}>> getType\(')
                self.assertNotIn('import net.minecraft.world.Level;',text)
                self.assertNotIn('import net.minecraft.util.collection.NonNullList;',text)

    def test_forging_notification_and_recipe_group_are_not_discarded(self):
        for name in ['ForgingRecipe','CastingRecipe','AlloySmeltingRecipe',
                     'NetherAlloySmeltingRecipe','ShapedAlloySmeltingRecipe','ShapedNetherAlloySmeltingRecipe']:
            with self.subTest(recipe=name):
                text=(DIR/(name+'.java')).read_text()
                self.assertRegex(text,r'public String group\(\) \{\s*return getGroup\(\);')
        text=(DIR/'ForgingRecipe.java').read_text()
        self.assertRegex(text,r'public boolean showNotification\(\) \{\s*return false;')

    def test_static_results_use_immutable_templates_and_create_at_assembly(self):
        for name, field in TEMPLATED.items():
            with self.subTest(recipe=name):
                text=(DIR/(name+'.java')).read_text()
                self.assertRegex(text,rf'\bItemStackTemplate {field};')
                self.assertIn('import net.minecraft.world.item.ItemStackTemplate;',text)
                self.assertIn('ItemStackTemplate.CODEC.fieldOf(',text)
                self.assertIn('ItemStackTemplate.STREAM_CODEC',text)
                self.assertNotIn('ItemStack.CODEC.fieldOf(',text)
                self.assertNotIn('ItemStack.STREAM_CODEC',text)
                self.assertRegex(text,rf'public ItemStack assemble\(\w+ \w+\) \{{\s*return {field}\.create\(\);')
                self.assertRegex(text,rf'public ItemStack getResultItem\(HolderLookup.Provider \w+\) \{{\s*return {field}\.create\(\);')
                self.assertNotRegex(text,rf'\bItemStack {field};')

    def test_ingredient_codec_symbol_is_26_3_compatible(self):
        for name in ['CoolingRecipe','GrindingRecipe']:
            text=(DIR/(name+'.java')).read_text()
            self.assertNotIn('Ingredient.CODEC_NONEMPTY',text)
            self.assertIn('Ingredient.CODEC.fieldOf("input")',text)

    def test_verified_package_imports(self):
        sources=list((ROOT/'common/src/main/java').rglob('*.java')) + list((ROOT/'fabric/src/main/java').rglob('*.java'))
        for bad in ['import net.minecraft.world.Level;', 'import net.minecraft.util.collection.NonNullList;']:
            offenders=[p.relative_to(ROOT).as_posix() for p in sources if bad in p.read_text()]
            self.assertFalse(offenders,f'{bad}: {offenders}')

    def test_actual_station_interface_compiles_and_uses_defaults(self):
        if not shutil.which('javac') or not shutil.which('java'):
            self.skipTest('JDK unavailable')
        with tempfile.TemporaryDirectory() as d:
            base=Path(d)
            def add(pkg,name,contents):
                p=base/'src'/Path(pkg.replace('.','/'))/(name+'.java')
                p.parent.mkdir(parents=True,exist_ok=True)
                p.write_text('package '+pkg+';\n'+contents)
                return p
            srcs=[]
            p='net.minecraft.world.item.crafting'
            srcs.append(add(p,'RecipeInput','public interface RecipeInput {}'))
            srcs.append(add(p,'PlacementInfo','public enum PlacementInfo { NOT_PLACEABLE, PLACEABLE }'))
            srcs.append(add(p,'RecipeBookCategory','public interface RecipeBookCategory {}'))
            srcs.append(add(p,'Recipe','''public interface Recipe<T extends RecipeInput> {
                String group(); boolean showNotification(); PlacementInfo placementInfo();
                RecipeBookCategory recipeBookCategory(); default boolean isSpecial(){return false;}
            }'''))
            srcs.append(add('net.stirdrem.overgeared.recipe','ContractSmoke','''
import net.minecraft.world.item.crafting.*;
public class ContractSmoke {
 public static final class Station implements OvergearedStationRecipe<RecipeInput> {}
 public static final class Forge implements OvergearedStationRecipe<RecipeInput> {
     @Override public boolean showNotification(){return false;}
     @Override public String group(){return "forged";}
 }
 public static void main(String[] args) {
     Station s=new Station(); Forge f=new Forge();
     if(!s.isSpecial() || !s.showNotification() || !s.group().equals("") ||
        s.placementInfo()!=PlacementInfo.NOT_PLACEABLE || s.recipeBookCategory()!=null)
        throw new AssertionError("station defaults");
     if (f.showNotification() || !f.group().equals("forged"))
        throw new AssertionError("per-recipe override lost");
     System.out.println("recipe contract smoke passed");
 }
}'''))
            classes=base/'classes';classes.mkdir()
            result=subprocess.run(['javac','-d',str(classes),str(DIR/'OvergearedStationRecipe.java'),*map(str,srcs)],
                                  capture_output=True,text=True,timeout=20)
            self.assertEqual(0,result.returncode,result.stderr)
            result=subprocess.run(['java','-cp',str(classes),'net.stirdrem.overgeared.recipe.ContractSmoke'],
                                  capture_output=True,text=True,timeout=10)
            self.assertEqual(0,result.returncode,result.stderr)
            self.assertIn('recipe contract smoke passed',result.stdout)

if __name__=='__main__': unittest.main()
