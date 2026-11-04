"""Regression checks for the Minecraft 26.3 Fabric extended-menu API migration.

The source signatures are mirrored from Fabric API's 26.3
fabric-menu-api-v1 ExtendedMenuProvider and ExtendedMenuType.
These checks do not replace a real JDK 25 Minecraft build.
"""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
SOURCE = (ROOT / "fabric/src/main/java/net/stirdrem/overgeared/fabric/FabricPlatformHelper.java").read_text(encoding="utf-8")


class ExtendedMenuBridgeTests(unittest.TestCase):
    def test_uses_263_menu_api(self):
        self.assertIn("import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;", SOURCE)
        self.assertIn("import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;", SOURCE)
        self.assertIn("new ExtendedMenuType<>(", SOURCE)
        self.assertIn("new ExtendedMenuProvider<byte[]>()", SOURCE)
        self.assertNotIn("fabric.api.screenhandler.v1", SOURCE)
        self.assertNotIn("ExtendedScreenHandler", SOURCE)

    def test_preserves_extra_open_data_on_server(self):
        self.assertIn("public byte[] getScreenOpeningData(ServerPlayer serverPlayer)", SOURCE)
        self.assertIn("extraData.accept(buf);", SOURCE)
        self.assertIn("byte[] bytes = new byte[buf.readableBytes()];", SOURCE)
        self.assertIn("buf.readBytes(bytes);", SOURCE)
        self.assertIn("buf.release();", SOURCE)

    def test_decodes_payload_in_legacy_menu_factory(self):
        self.assertIn("ByteBufCodecs.BYTE_ARRAY", SOURCE)
        self.assertIn("new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(data), inventory.player.registryAccess())", SOURCE)


    def test_migrated_vanilla_263_signatures(self):
        self.assertIn("register.getRegistryKey().identifier()", SOURCE)
        self.assertIn("BuiltInRegistries.REGISTRY.getValue(", SOURCE)
        self.assertNotIn("BuiltInRegistries.REGISTRY.get(", SOURCE)
        self.assertNotIn("register.getRegistryKey().location()", SOURCE)
        self.assertIn("CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)", SOURCE)
        self.assertNotIn("CreativeModeTab.builder()", SOURCE)
        self.assertIn("MapCodec.assumeMapUnsafe(Ingredient.CODEC)", SOURCE)
        self.assertNotIn("Ingredient.CODEC_NONEMPTY", SOURCE)

if __name__ == "__main__":
    unittest.main()
