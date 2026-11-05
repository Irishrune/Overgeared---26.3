"""Source regression checks for Minecraft 26.3 anvil inventory and forging sessions.

These guards validate serialization keys and the absence of duplicate owner
fields. They are not a runtime save/reload or multiplayer validation.
"""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1] / "common/src/main/java/net/stirdrem/overgeared"
BASE = (ROOT / "block/entity/BaseSmithingAnvilBlockEntity.java").read_text(encoding="utf-8")
ANVIL = (ROOT / "block/entity/AbstractSmithingAnvilBlockEntity.java").read_text(encoding="utf-8")
HANDLER = (ROOT / "util/items/ItemStackHandler.java").read_text(encoding="utf-8")


class SmithingPersistenceTests(unittest.TestCase):
    def test_inventory_uses_legacy_compatible_value_io(self):
        self.assertIn('output.putInt("Size", stacks.size())', HANDLER)
        self.assertIn('output.childrenList("Items")', HANDLER)
        self.assertIn('entry.putInt("Slot", i)', HANDLER)
        self.assertIn("entry.store(ItemStack.MAP_CODEC, stack)", HANDLER)
        self.assertIn('input.childrenListOrEmpty("Items")', HANDLER)
        self.assertIn('entry.read(ItemStack.MAP_CODEC)', HANDLER)
        self.assertNotIn("serializeNBT(", HANDLER)
        self.assertNotIn("deserializeNBT(", HANDLER)

    def test_base_controls_inventory_and_forging_session_owner(self):
        self.assertIn("protected void saveAdditional(ValueOutput output)", BASE)
        self.assertIn("itemHandler.write(output.child(\"inventory\"))", BASE)
        self.assertIn("input.child(\"inventory\").ifPresent(itemHandler::read)", BASE)
        self.assertIn('output.store("ownerUUID", UUIDUtil.CODEC, ownerUUID)', BASE)
        self.assertIn('input.read("ownerUUID", UUIDUtil.CODEC).orElse(null)', BASE)
        self.assertIn("return saveWithoutMetadata(provider);", BASE)

    def test_anvil_no_longer_shadows_owner_or_writes_inventory_twice(self):
        self.assertNotIn("protected UUID ownerUUID =", ANVIL)
        self.assertNotIn("protected long sessionStartTime =", ANVIL)
        self.assertNotIn("protected Map<BlockPos, UUID> occupiedAnvils =", ANVIL)
        self.assertNotIn("itemHandler.write(", ANVIL)
        self.assertNotIn("itemHandler.serializeNBT(", ANVIL)
        self.assertIn("super.saveAdditional(output);", ANVIL)
        self.assertIn("super.loadAdditional(input);", ANVIL)

    def test_progress_and_occupied_session_fields_are_preserved(self):
        for field in ("hitRemains", "progress", "maxProgress"):
            self.assertIn(f'output.putInt("{field}", {field})', ANVIL)
            self.assertIn(f'{field} = input.getIntOr("{field}", 0)', ANVIL)
        self.assertIn('output.childrenList("occupiedAnvils")', ANVIL)
        self.assertIn('input.childrenListOrEmpty("occupiedAnvils")', ANVIL)
        self.assertIn('child.store("uuid", UUIDUtil.CODEC, entry.getValue())', ANVIL)
        self.assertIn('child.read("uuid", UUIDUtil.CODEC)', ANVIL)

    def test_armor_polishing_check_is_component_based(self):
        self.assertIn("result.get(DataComponents.EQUIPPABLE)", ANVIL)
        self.assertIn("equippable.slot().isArmor()", ANVIL)
        self.assertNotIn("instanceof ArmorItem", ANVIL)


if __name__ == "__main__":
    unittest.main()
