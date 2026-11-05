"""Source-level guards for the Minecraft 26.3 forging HUD migration.

Verify the timing bar, quality zones and quality popups are still wired to
Fabric's 26.3 HUD extraction API. Visual and multiplayer smoke tests remain
necessary before claiming runtime correctness.
"""
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]
COMMON=ROOT/"common/src/main/java/net/stirdrem/overgeared/client"
FABRIC=ROOT/"fabric/src/main/java/net/stirdrem/overgeared/fabric/client/OvergearedFabricClient.java"
HUD=(COMMON/"AnvilMinigameOverlay.java").read_text(encoding="utf-8")
POP=(COMMON/"PopupOverlay.java").read_text(encoding="utf-8")
CLIENT=FABRIC.read_text(encoding="utf-8")

class ForgeHud26_3Tests(unittest.TestCase):
    def test_fabric_hud_elements_are_registered_with_stable_ids(self):
        self.assertIn("HudElementRegistry.addLast(AnvilMinigameOverlay.ID, AnvilMinigameOverlay.INSTANCE::render)",CLIENT)
        self.assertIn("HudElementRegistry.addLast(PopupOverlay.ID, PopupOverlay.INSTANCE::render)",CLIENT)
        self.assertNotIn("HudRenderCallback",CLIENT)
        self.assertNotIn("options.hideGui",CLIENT)

    def test_minigame_bar_preserves_zones_arrow_and_hits(self):
        self.assertIn("public void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker)",HUD)
        self.assertIn("Minecraft.getInstance().gui.hud.isHidden()",HUD)
        self.assertIn("RenderPipelines.GUI_TEXTURED",HUD)
        self.assertIn("getGoodZoneStart()",HUD)
        self.assertIn("getPerfectZoneStart()",HUD)
        self.assertIn("getHitsRemaining()",HUD)
        self.assertIn("getArrowPosition(deltaTracker.getGameTimeDeltaPartialTick(true))",HUD)
        self.assertIn("guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE",HUD)
        self.assertNotIn("LayeredDraw.Layer",HUD)
        self.assertNotIn("RenderSystem.",HUD)

    def test_popup_preserves_fade_float_and_scales_via_2d_matrix(self):
        self.assertIn("public void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker)",POP)
        self.assertIn("Minecraft.getInstance().gui.hud.isHidden()",POP)
        self.assertIn("POPUP_DURATION_MS",POP)
        self.assertIn("getGameTimeDeltaPartialTick(true)",POP)
        self.assertIn("guiGraphics.pose().pushMatrix()",POP)
        self.assertIn("guiGraphics.pose().translate(centerX, popupY)",POP)
        self.assertIn("guiGraphics.pose().scale(scale, scale)",POP)
        self.assertIn("guiGraphics.text(font, popup.text,",POP)
        self.assertIn("guiGraphics.pose().popMatrix()",POP)
        self.assertNotIn("LayeredDraw",POP)

if __name__=="__main__":
    unittest.main()
