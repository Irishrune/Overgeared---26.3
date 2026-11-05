"""Verify 26.3 recipe serializer registration against a deliberately small API stub.

This compiles and runs the REAL ModRecipeSerializers.java with Java 21 stubs for
26.3's RecipeSerializer record. It does not prove recipes compile in Minecraft.
"""
from pathlib import Path
import re
import shutil
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]
RECIPE_DIR = ROOT/'common/src/main/java/net/stirdrem/overgeared/recipe'
REGISTER = RECIPE_DIR/'ModRecipeSerializers.java'


def write_java(dir, qualified, source):
    dst = dir/'src'/Path(qualified.replace('.', '/')+'.java')
    dst.parent.mkdir(parents=True,exist_ok=True)
    dst.write_text(source)
    return dst

class SerializerPortTests(unittest.TestCase):
    def test_all_recipe_serializers_use_record_construction(self):
        source=REGISTER.read_text()
        declarations=re.findall(r'public static final Supplier<RecipeSerializer<([A-Za-z]+Recipe)>>\s+([A-Z_]+)',source)
        self.assertEqual(21,len(declarations))
        self.assertEqual(21,len(set(x[0] for x in declarations)))
        self.assertEqual(21,source.count('() -> recipe('))
        self.assertIn('return new RecipeSerializer<>(codec, networkCodec);',source)
        self.assertNotIn('SimpleCraftingRecipeSerializer',source)
        self.assertNotIn('.Serializer::new',source)
        self.assertNotIn('Serializer.INSTANCE',source)
        for name, field in declarations:
            if name == 'DynamicToolCastRecipe':
                content=(RECIPE_DIR/f'{name}.java').read_text()
                self.assertIn('MapCodec.unit(INSTANCE)',content)
                self.assertIn('StreamCodec.unit(INSTANCE)',content)
                continue
            content=(RECIPE_DIR/f'{name}.java').read_text()
            self.assertRegex(content,rf'public static final class Serializer\s*\{{')
            self.assertRegex(content,rf'public static final MapCodec<{name}> CODEC\s*=')
            self.assertRegex(content,rf'public static final StreamCodec<RegistryFriendlyByteBuf,\s*{name}> STREAM_CODEC\s*=')
            self.assertNotIn('implements RecipeSerializer<',content)
            # Accidental field duplications can be hidden by source-string checks.
            self.assertEqual(1,len(re.findall(rf'public static final MapCodec<{name}> CODEC\s*=',content)))
            self.assertEqual(1,len(re.findall(rf'public static final StreamCodec<RegistryFriendlyByteBuf,\s*{name}> STREAM_CODEC\s*=',content)))
            self.assertNotIn('STREAM_CODEC = STREAM_CODEC',content)

    def test_modern_assemble_signature_is_used(self):
        sources=list(RECIPE_DIR.glob('*.java'))
        count=0
        for source in sources:
            text=source.read_text()
            if 'public ItemStack assemble(' not in text:continue
            self.assertRegex(text,r'public ItemStack assemble\(\w+\s+\w+\)')
            self.assertNotRegex(text,r'public ItemStack assemble\([^)]*HolderLookup\.Provider')
            count+=1
        self.assertEqual(21,count)
        menu=(ROOT/'common/src/main/java/net/stirdrem/overgeared/screen/FletchingStationMenu.java').read_text()
        self.assertEqual(2,menu.count('.assemble(recipeInput)'))
        self.assertNotIn('.assemble(recipeInput, level.registryAccess())',menu)

    def test_real_registry_code_compiles_and_initializes_with_record_api_stubs(self):
        if not shutil.which('javac') or not shutil.which('java'):
            self.skipTest('Java compiler not installed')
        with tempfile.TemporaryDirectory() as dir:
            root=Path(dir)
            java=[]
            def stub(klass,src): java.append(str(write_java(root,klass,src)))
            stub('com.mojang.serialization.MapCodec','package com.mojang.serialization; public class MapCodec<T> {}')
            stub('net.minecraft.network.RegistryFriendlyByteBuf','package net.minecraft.network; public class RegistryFriendlyByteBuf {}')
            stub('net.minecraft.network.codec.StreamCodec','package net.minecraft.network.codec; public class StreamCodec<B,T> {}')
            stub('net.minecraft.core.registries.Registries','package net.minecraft.core.registries; public class Registries { public static final Object RECIPE_SERIALIZER = new Object(); }')
            stub('net.minecraft.world.item.crafting.Recipe','package net.minecraft.world.item.crafting; public interface Recipe<T> {}')
            stub('net.minecraft.world.item.crafting.RecipeSerializer','''package net.minecraft.world.item.crafting;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
public record RecipeSerializer<T extends Recipe<?>>(MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf,T> streamCodec) {}''')
            stub('net.stirdrem.overgeared.OvergearedMod','package net.stirdrem.overgeared; public class OvergearedMod { public static final String MOD_ID="overgeared"; }')
            stub('net.stirdrem.overgeared.registry.DeferredRegister','''package net.stirdrem.overgeared.registry;
import java.util.function.Supplier;
public class DeferredRegister<T> {
 public static <T> DeferredRegister<T> create(Object registry, String modId) {return new DeferredRegister<>();}
 public <R extends T> Supplier<R> register(String id,Supplier<R> factory){return factory;}
 public void register(){}
}''')
            registered=re.findall(r'public static final Supplier<RecipeSerializer<([A-Za-z]+Recipe)>>\s+([A-Z_]+)',REGISTER.read_text())
            classes=[]
            for klass,field in registered:
                if klass != 'DynamicToolCastRecipe':
                    classes.append(f'''class {klass} implements Recipe<Object> {{
 public static class Serializer {{
  public static final MapCodec<{klass}> CODEC = new MapCodec<>();
  public static final StreamCodec<RegistryFriendlyByteBuf, {klass}> STREAM_CODEC = new StreamCodec<>();
 }}
}}''')
                else:
                    classes.append(f'''class {klass} implements Recipe<Object> {{
 public static final MapCodec<{klass}> CODEC = new MapCodec<>();
 public static final StreamCodec<RegistryFriendlyByteBuf, {klass}> STREAM_CODEC = new StreamCodec<>();
}}''')
            stub('net.stirdrem.overgeared.recipe.RecipeStubs','''package net.stirdrem.overgeared.recipe;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Recipe;
'''+'\n'.join(classes))
            check='\n'.join(f'''if (ModRecipeSerializers.{field}.get().codec() == null ||
 ModRecipeSerializers.{field}.get().streamCodec() == null)
  throw new AssertionError("{field} missing codec");''' for name,field in registered)
            stub('net.stirdrem.overgeared.recipe.SerializerSmoke',f'''package net.stirdrem.overgeared.recipe;
public class SerializerSmoke {{ public static void main(String[] args){{
{check}
System.out.println("{len(registered)} recipe serializers initialized with 26.3 record API");
}}}}''')
            classesDir=root/'classes';classesDir.mkdir()
            cmd=['javac','-encoding','UTF-8','-d',str(classesDir),str(REGISTER),*java]
            result=subprocess.run(cmd,capture_output=True,text=True,timeout=30)
            self.assertEqual(result.returncode,0,result.stderr)
            result=subprocess.run(['java','-cp',str(classesDir),'net.stirdrem.overgeared.recipe.SerializerSmoke'],capture_output=True,text=True,timeout=10)
            self.assertEqual(result.returncode,0,result.stderr)
            self.assertIn('21 recipe serializers initialized',result.stdout)

if __name__=='__main__':unittest.main()
