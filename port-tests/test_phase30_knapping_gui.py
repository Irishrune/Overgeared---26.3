"""Preserve the playable 3×3 knapping grid while using the 26.3 GUI extractor."""
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]
SCREEN=(ROOT/"common/src/main/java/net/stirdrem/overgeared/screen/RockKnappingScreen.java").read_text(encoding="utf-8")

class KnappingGui26_3Tests(unittest.TestCase):
    def test_grid_buttons_and_server_packets_remain(self):
        self.assertIn("for (int i = 0; i < 9; i++)",SCREEN)
        self.assertIn("menu.isChipped(i)",SCREEN)
        self.assertIn("menu.setChip(index)",SCREEN)
        self.assertIn("new KnappingChipC2SPacket(index)",SCREEN)
        self.assertIn("btn.active = false",SCREEN)
        self.assertIn("getUnchippedTexture()",SCREEN)
        self.assertIn("CHIPPED_TEXTURE",SCREEN)

    def test_rendering_uses_extractor_and_button_content_hook(self):
        self.assertIn("super(menu, playerInventory, title, 176, 166)",SCREEN)
        self.assertIn("public void extractBackground(GuiGraphicsExtractor",SCREEN)
        self.assertIn("protected void extractLabels(GuiGraphicsExtractor",SCREEN)
        self.assertIn("public void extractContents(GuiGraphicsExtractor",SCREEN)
        self.assertIn("RenderPipelines.GUI_TEXTURED, resourceLocation",SCREEN)
        self.assertIn("RenderPipelines.GUI_TEXTURED, TEXTURE",SCREEN)
        self.assertNotIn("renderWidget(",SCREEN)
        self.assertNotIn("renderBg(",SCREEN)
        self.assertNotIn("RenderSystem.",SCREEN)
        self.assertNotIn("new GameRenderer",SCREEN)

    def test_knapping_sound_playback_remains(self):
        self.assertIn("public void playDownSound(SoundManager pHandler)",SCREEN)
        self.assertIn("minecraft.player.playSound(menu.getSound()",SCREEN)

if __name__=="__main__":
    unittest.main()
