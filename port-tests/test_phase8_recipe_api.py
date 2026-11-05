"""Phase 8 source-level guards + isolated real helper compilation; NOT a Minecraft build."""
from pathlib import Path
import json
import re
import shutil
import subprocess
import tempfile
import unittest

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/'common/src/main/java/net/stirdrem/overgeared'
RECIPES=JAVA/'recipe'

class RecipePhase8Tests(unittest.TestCase):
    def read(self, filename):
        return (RECIPES/filename).read_text()

    def test_recipe_outputs_use_263_codecs(self):
        for p in RECIPES.glob('*.java'):
            with self.subTest(recipe=p.name):
                s=p.read_text()
                self.assertNotRegex(s,r'\bItemStack\.(?:CODEC|STREAM_CODEC|OPTIONAL_STREAM_CODEC)\b')
                self.assertNotRegex(s,r'(?<![a-zA-Z])Ingredient\.EMPTY\b')
        for n in ('ForgingRecipe','FletchingRecipe','ShapedAlloySmeltingRecipe',
                  'ShapedNetherAlloySmeltingRecipe','CastingRecipe'):
            with self.subTest(recipe=n):
                s=self.read(n+'.java')
                self.assertIn('ItemStackTemplate.CODEC',s)
                self.assertIn('ItemStackTemplate.STREAM_CODEC',s)

    def test_blank_slots_are_explicitly_empty_not_wildcards(self):
        for n in ('ShapedAlloySmeltingRecipe','ShapedNetherAlloySmeltingRecipe'):
            with self.subTest(recipe=n):
                s=self.read(n+'.java')
                for fragment in ('symbol == \' \'','Optional.empty()','Ingredient.testOptionalIngredient',
                                 'OPTIONAL_CONTENTS_STREAM_CODEC','patternRows','patternKey'):
                    self.assertIn(fragment,s)
        forging=self.read('ForgingRecipe.java')
        self.assertIn('new ForgingIngredient(Optional.empty(), false, false)',forging)
        self.assertIn('if (ingredient.ingredient().isEmpty())',forging)
        self.assertIn('if (!RecipeInput.getItem(invSlot).isEmpty()) return false;',forging)
        self.assertIn('Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC',forging)
        fletch=self.read('FletchingRecipe.java')
        self.assertIn('Ingredient.testOptionalIngredient(tip, input.getItem(0))',fletch)
        self.assertIn('Ingredient.testOptionalIngredient(shaft, input.getItem(1))',fletch)
        self.assertIn('Ingredient.testOptionalIngredient(feather, input.getItem(2))',fletch)
        self.assertIn('potion.map(value -> value.test(input.getItem(3))).orElse(true)',fletch)

    def test_recipe_manager_centralized_and_client_safe(self):
        common=list((ROOT/'common/src/main/java').rglob('*.java'))
        fabric=list((ROOT/'fabric/src/main/java').rglob('*.java'))
        for p in common+fabric:
            with self.subTest(path=p.name):
                self.assertNotIn('getAllRecipesFor(',p.read_text())
        lookups=self.read('RecipeLookups.java')
        self.assertIn('server.recipeAccess()',lookups)
        display=(JAVA/'util/IngredientDisplay.java').read_text()
        self.assertIn('ingredient.items()',display)
        self.assertIn('new ItemStack(holder.value())',display)
        self.assertIn('if (manager == null)',lookups)
        self.assertIn('return List.of();',lookups)
        self.assertIn('manager.getRecipes().stream()',lookups)
        self.assertIn('holder.value().getType() == type',lookups)
        for n in ('util/ConfigHelper.java','util/ItemUtils.java','event/ModItemInteractEvents.java'):
            with self.subTest(file=n):
                self.assertIn('RecipeLookups.all(level,', (JAVA/n).read_text())
        for n in ('CastingSmelterBlockEntity','AlloySmelterBlockEntity','NetherAlloySmelterBlockEntity'):
            with self.subTest(block_entity=n):
                s=(JAVA/'block/entity'/(n+'.java')).read_text()
                self.assertRegex(s,r'\b(?:rm|manager) == null\) return Optional.empty\(\)')

    def test_upstream_recipe_json_is_not_corrupted(self):
        data=ROOT/'common/src/main/resources/data'
        p=[f for f in data.rglob('*.json') if f.relative_to(data).parts[1] == 'recipe']
        self.assertGreater(len(p),30)
        for f in p:
            with self.subTest(recipe=f.name):
                d=json.loads(f.read_text())
                self.assertIsInstance(d,dict)
                self.assertIn('type',d)

    def test_recipe_lookups_compiles_against_263_shaped_stubs(self):
        if not (shutil.which('javac') and shutil.which('java')):
            self.skipTest('JDK unavailable')
        with tempfile.TemporaryDirectory() as d:
            base=Path(d)
            srcs=[]
            def add(package, cls, body):
                path=base/'src'/Path(package.replace('.','/'))/(cls+'.java')
                path.parent.mkdir(parents=True,exist_ok=True)
                path.write_text('package '+package+';\n'+body)
                srcs.append(path)
            c='net.minecraft.world.item.crafting'
            add(c,'RecipeInput','public interface RecipeInput {}')
            add(c,'Recipe','public interface Recipe<I extends RecipeInput> { RecipeType<?> getType(); }')
            add(c,'RecipeType','public interface RecipeType<T extends Recipe<?>> {}')
            add(c,'RecipeHolder','public record RecipeHolder<T extends Recipe<?>>(T value) {}')
            add(c,'RecipeManager','''import java.util.*;
                public interface RecipeManager { Collection<RecipeHolder<?>> getRecipes(); }''')
            add('net.minecraft.world.level','Level','public class Level {}')
            add('net.minecraft.server.level','ServerLevel','''import net.minecraft.world.level.Level;
               import net.minecraft.world.item.crafting.RecipeManager;
               public class ServerLevel extends Level {
                 private final RecipeManager manager;
                 public ServerLevel(RecipeManager manager) { this.manager=manager; }
                 public RecipeManager recipeAccess(){ return manager; }
               }''')
            add('net.stirdrem.overgeared.recipe','RecipeLookupsSmoke','''
                import net.minecraft.server.level.ServerLevel;
                import net.minecraft.world.level.Level;
                import net.minecraft.world.item.crafting.*;
                import java.util.*;
                public class RecipeLookupsSmoke {
                    static class Input implements RecipeInput {}
                    static class Dummy implements Recipe<Input> {
                        private final RecipeType<?> type;
                        Dummy(RecipeType<?> type){ this.type=type; }
                        @Override public RecipeType<?> getType() { return type; }
                    }
                    public static void main(String[] args) {
                        RecipeType<Dummy> type = new RecipeType<>() {};
                        RecipeHolder<Dummy> holder = new RecipeHolder<>(new Dummy(type));
                        RecipeManager manager = () -> List.<RecipeHolder<?>>of(holder);
                        Level client = new Level();
                        Level server = new ServerLevel(manager);
                        if (RecipeLookups.managerOrNull(client) != null) throw new AssertionError();
                        if (!RecipeLookups.all(client,type).isEmpty()) throw new AssertionError();
                        if (RecipeLookups.all(server,type).size() != 1) throw new AssertionError();
                        if (RecipeLookups.serverManager(server) != manager) throw new AssertionError();
                        try { RecipeLookups.serverManager(client); throw new AssertionError(); }
                        catch (IllegalStateException expected) { }
                        System.out.println("recipe lookup smoke passed");
                    }
                }''')
            classes=base/'classes';classes.mkdir()
            proc=subprocess.run(['javac','-d',str(classes),str(RECIPES/'RecipeLookups.java'),*map(str,srcs)],
                                  text=True,capture_output=True,timeout=25)
            self.assertEqual(proc.returncode,0,proc.stderr)
            proc=subprocess.run(['java','-cp',str(classes),'net.stirdrem.overgeared.recipe.RecipeLookupsSmoke'],
                                  text=True,capture_output=True,timeout=10)
            self.assertEqual(proc.returncode,0,proc.stderr)
            self.assertIn('recipe lookup smoke passed',proc.stdout)

if __name__ == '__main__': unittest.main()
