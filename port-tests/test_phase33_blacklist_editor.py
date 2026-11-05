"""Minecraft 26.3 UI regression checks for the individual blacklist editor."""
from pathlib import Path
import unittest

SRC=(Path(__file__).resolve().parents[1]/
"common/src/main/java/net/stirdrem/overgeared/client/BlacklistEditorScreen.java").read_text(encoding="utf-8")

class BlacklistEditor26_3Tests(unittest.TestCase):
    def test_list_and_input_fields_remain_functional(self):
        self.assertIn("this.addRenderableWidget(itemList);",SRC)
        self.assertIn("this.addRenderableWidget(addItemField);",SRC)
        self.assertIn("this.addRenderableWidget(addButton);",SRC)
        self.assertIn("this.addRenderableWidget(doneButton);",SRC)
        self.assertIn("this.addRenderableWidget(cancelButton);",SRC)
        self.assertIn("itemList.clearBlacklistEntries();",SRC)
        self.assertIn("items.remove(item)",SRC)
        self.assertIn("items.add(newItem)",SRC)
        self.assertIn("setter.accept(items)",SRC)
        self.assertIn("configScreen.restoreScrollPosition()",SRC)

    def test_263_event_and_render_apis(self):
        self.assertIn("extractRenderState(GuiGraphicsExtractor gui",SRC)
        self.assertIn("extractContent(GuiGraphicsExtractor gui",SRC)
        self.assertIn("mouseClicked(MouseButtonEvent event, boolean doubleClick)",SRC)
        self.assertIn("mouseReleased(MouseButtonEvent event)",SRC)
        self.assertIn("keyPressed(KeyEvent event)",SRC)
        self.assertIn("removeButton.extractRenderState(gui, mouseX, mouseY, pt)",SRC)
        self.assertIn("this.minecraft.setScreenAndShow(parentScreen)",SRC)
        self.assertNotIn("render(GuiGraphics",SRC)
        self.assertNotIn(".setScreen(",SRC)
        self.assertNotIn("mouseClicked(double mouseX, double mouseY",SRC)

if __name__=="__main__":
    unittest.main()
