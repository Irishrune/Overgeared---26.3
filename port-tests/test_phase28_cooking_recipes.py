"""Source regressions for 26.3 cast smelting/blasting recipe compatibility.

Actual cooking output, tag reload, codecs and furnace behavior require runtime tests.
"""
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]/"common/src/main/java/net/stirdrem/overgeared"
SMELTING=(ROOT/"recipe/CastSmeltingRecipe.java").read_text(encoding="utf-8")
BLASTING=(ROOT/"recipe/CastBlastingRecipe.java").read_text(encoding="utf-8")
NBT_SMELTING=(ROOT/"recipe/NBTKeepingSmeltingRecipe.java").read_text(encoding="utf-8")
NBT_BLASTING=(ROOT/"recipe/NBTKeepingBlastingRecipe.java").read_text(encoding="utf-8")

class CastCookingRecipes26_3Tests(unittest.TestCase):
    def test_custom_serializers_use_abstract_cooking_recipe(self):
        for name, source, typ in [("CastSmeltingRecipe", SMELTING, "SMELTING"),
                                  ("CastBlastingRecipe", BLASTING, "BLASTING")]:
            with self.subTest(name=name):
                self.assertIn(f"class {name} extends AbstractCookingRecipe", source)
                self.assertIn(f"public RecipeSerializer<{name}> getSerializer()", source)
                self.assertIn("public RecipeType<? extends AbstractCookingRecipe> getType()",source)
                self.assertIn(f"return RecipeType.{typ};",source)
                self.assertNotIn(f"class {name} extends {typ.title()}Recipe",source)

    def test_tag_ingredient_and_component_contents_survive(self):
        for name, source in [("smelting",SMELTING),("blasting",BLASTING)]:
            with self.subTest(name=name):
                self.assertEqual(2,source.count("Ingredient.of(ModItems.CLAY_TOOL_CAST.get(), ModItems.NETHER_TOOL_CAST.get())"))
                self.assertIn("stack.is(ModTags.Items.USABLE_TOOL_CAST)",source)
                self.assertIn("castData.materials()",source)
                self.assertIn("for (var entry : requiredMaterials.entrySet())",source)
                self.assertIn("cast.set(ModComponents.CAST_DATA.get(), updatedData)",source)
                self.assertIn("result.set(ModComponents.FORGING_QUALITY.get(), forgingQuality)",source)
                self.assertIn("cast.set(ModComponents.HEATED_COMPONENT.get(), true)",source)

    def test_cooking_book_and_network_values_are_retained(self):
        self.assertIn("return Items.FURNACE;",SMELTING)
        self.assertIn("RecipeBookCategories.FURNACE_BLOCKS",SMELTING)
        self.assertIn("RecipeBookCategories.FURNACE_FOOD",SMELTING)
        self.assertIn("return Items.BLAST_FURNACE;",BLASTING)
        self.assertIn("RecipeBookCategories.BLAST_FURNACE_BLOCKS",BLASTING)
        for source in [SMELTING,BLASTING]:
            self.assertIn("buffer.writeFloat(recipe.experience())",source)
            self.assertIn("buffer.writeVarInt(recipe.cookingTime())",source)
            self.assertIn("CodecUtils.MATERIAL_INT_MAP_STREAM.encode",source)

    def test_component_preserving_cooking_recipes_keep_serializers(self):
        for name, src, typ, icon in [
            ("NBTKeepingSmeltingRecipe", NBT_SMELTING, "SMELTING", "FURNACE"),
            ("NBTKeepingBlastingRecipe", NBT_BLASTING, "BLASTING", "BLAST_FURNACE"),
        ]:
            with self.subTest(name=name):
                self.assertIn(f"class {name} extends AbstractCookingRecipe", src)
                self.assertIn(f"public RecipeSerializer<{name}> getSerializer()", src)
                self.assertIn(f"return RecipeType.{typ};", src)
                self.assertIn(f"return Items.{icon};", src)
                self.assertIn("output.applyComponents(input.getItem(0).getComponentsPatch())", src)
                self.assertIn("AbstractCookingRecipe.cookingMapCodec", src)
                self.assertIn("AbstractCookingRecipe.cookingStreamCodec", src)

if __name__=="__main__":
    unittest.main()
