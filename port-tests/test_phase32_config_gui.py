"""Source-level guards for 26.3 Overgeared configuration editor migration.

Every editable setting still uses its original config getter/setter; the
scrolling list must expose its buttons, sliders and text fields to extraction
and 26.3 mouse/keyboard events.
"""
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]
SRC=(ROOT/"common/src/main/java/net/stirdrem/overgeared/client/OvergearedConfigScreen.java").read_text(encoding="utf-8")

class ConfigGui26_3Tests(unittest.TestCase):
    def test_all_six_editing_entry_types_are_retained(self):
        for name in ("HeaderEntry", "BooleanEntry", "IntEntry", "DoubleEntry",
                     "ManualInputEntry", "BlacklistEntry"):
            self.assertIn("class "+name+" extends ConfigList.ConfigEntry",SRC)
        self.assertEqual(6,SRC.count("public void extractContent(GuiGraphicsExtractor gui"))
        self.assertIn("super.extractRenderState(gui, mouseX, mouseY, partialTicks)",SRC)
        self.assertIn("this.addRenderableWidget(configList)",SRC)
        self.assertNotIn("render(GuiGraphics",SRC)
        self.assertNotIn(".render(gui, mouseX, mouseY",SRC)

    def test_editors_preserve_changes_to_config_values(self):
        for symbol in ("setBoolean(", "setInt(", "setDouble(", "setStringList(",
                       "configFile.save()", "this.configList.scrollAmount()",
                       "this.configList.setScrollAmount("):
            if symbol=="this.configList.scrollAmount()":
                self.assertIn("configList.scrollAmount()",SRC)
            else:
                self.assertIn(symbol,SRC)
        self.assertIn("this.minecraft.setScreenAndShow(parent)",SRC)
        self.assertIn("mc.setScreenAndShow(editorScreen)",SRC)

    def test_modern_keyboard_and_mouse_events_are_forwarded_to_controls(self):
        self.assertIn("mouseClicked(MouseButtonEvent event, boolean doubleClick)",SRC)
        self.assertIn("mouseDragged(MouseButtonEvent event, double dx, double dy)",SRC)
        self.assertIn("mouseReleased(MouseButtonEvent event)",SRC)
        self.assertIn("keyPressed(KeyEvent event)",SRC)
        self.assertIn("charTyped(CharacterEvent event)",SRC)
        self.assertIn("editBox.keyPressed(event)",SRC)
        self.assertIn("editBox.charTyped(event)",SRC)
        for ctrl in ("toggle","slider","editBox","editButton"):
            self.assertIn(f"{ctrl}.mouseClicked(event, doubleClick)",SRC)
        self.assertNotIn("mouseClicked(double mouseX, double mouseY, int button)",SRC)

if __name__=="__main__":
    unittest.main()
