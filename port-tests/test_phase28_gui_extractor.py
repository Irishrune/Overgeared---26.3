"""Minecraft 26.3 workstation GUI extraction guards.

These assertions protect layout/visual elements during API migration. They do
not substitute for live GUI rendering or interactive inventory tests.
"""
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]/"common/src/main/java/net/stirdrem/overgeared/screen"
SCREEN_NAMES=("AlloySmelterScreen","NetherAlloySmelterScreen","CastingSmelterScreen",
              "FletchingStationScreen","BlueprintWorkbenchScreen")
SCREENS={name:(ROOT/(name+".java")).read_text(encoding="utf-8") for name in SCREEN_NAMES}

class GuiExtractor26_3Tests(unittest.TestCase):
    def test_screens_use_gui_extractor_with_normal_texture_pipeline(self):
        for name,src in SCREENS.items():
            with self.subTest(screen=name):
                self.assertIn("import net.minecraft.client.gui.GuiGraphicsExtractor;",src)
                self.assertIn("import net.minecraft.client.renderer.RenderPipelines;",src)
                self.assertIn("public void extractBackground(GuiGraphicsExtractor ",src)
                self.assertIn("super.extractBackground(",src)
                self.assertIn("RenderPipelines.GUI_TEXTURED, TEXTURE",src)
                self.assertNotIn("GuiGraphics;",src)
                self.assertNotIn("RenderSystem.",src)
                self.assertNotIn("renderBg(",src)
                self.assertNotIn("guiGraphics.blit(TEXTURE",src)
                self.assertNotIn("this.imageWidth = ",src)
                self.assertNotIn("this.imageHeight = ",src)

    def test_furnace_flames_and_cook_arrows_are_preserved(self):
        for name in ("AlloySmelterScreen","NetherAlloySmelterScreen","CastingSmelterScreen"):
            with self.subTest(screen=name):
                src=SCREENS[name]
                self.assertIn("getCookProgress()",src)
                self.assertIn("RenderPipelines.GUI_TEXTURED, TEXTURE",src)
                if name=="CastingSmelterScreen":
                    self.assertIn("menu.isBurning()",src)
                    self.assertIn("menu.getBurnProgress()",src)
                else:
                    self.assertIn("menu.isLit()",src)
                    self.assertIn("menu.getLitProgress()",src)
                    self.assertIn("protected void extractLabels(GuiGraphicsExtractor",src)
                    self.assertIn("guiGraphics.text(",src)

    def test_fletching_and_blueprint_labels_and_selector_survive(self):
        fle=SCREENS["FletchingStationScreen"]
        bp=SCREENS["BlueprintWorkbenchScreen"]
        self.assertIn("protected void extractLabels(GuiGraphicsExtractor",fle)
        self.assertIn("graphics.text(this.font, this.playerInventoryTitle",fle)
        self.assertIn("new SelectToolTypeC2SPacket(",bp)
        self.assertIn("createButtons(x, y)",bp)
        self.assertIn("currentToolName",bp)
        self.assertIn("guiGraphics.text(this.font, currentToolName",bp)

if __name__=="__main__":
    unittest.main()
