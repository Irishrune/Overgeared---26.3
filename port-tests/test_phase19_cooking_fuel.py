"""26.3 cooking-fuel and fuel-remainder source regression checks.

These are deliberately narrower than an actual Minecraft compile or runtime test.
"""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
COMMON = ROOT / "common/src/main/java/net/stirdrem/overgeared"
FABRIC = ROOT / "fabric/src/main/java/net/stirdrem/overgeared/fabric"

SMELTERS = (
    ("AlloySmelterBlockEntity.java", "4"),
    ("CastingSmelterBlockEntity.java", "SLOT_FUEL"),
    ("NetherAlloySmelterBlockEntity.java", "9"),
)


class CookingFuelMigrationTests(unittest.TestCase):
    def test_platform_contract_is_context_aware(self):
        interface = (COMMON / "platform/IPlatformHelper.java").read_text(encoding="utf-8")
        self.assertIn("int getBurnTime(ItemStack stack, LootContext context);", interface)
        self.assertIn("boolean isCookingFuel(ItemStack stack);", interface)
        self.assertNotIn("int getBurnTime(ItemStack stack);", interface)

    def test_fabric_platform_uses_263_components(self):
        source = (FABRIC / "FabricPlatformHelper.java").read_text(encoding="utf-8")
        self.assertIn("ResolvableInt.getFromItem(", source)
        self.assertIn("DataComponents.COOKING_FUEL", source)
        self.assertIn("CookingFuel::burnTime", source)
        self.assertIn("getBurnTime(ItemStack stack, LootContext context)", source)
        self.assertIn("stack.has(DataComponents.COOKING_FUEL)", source)
        self.assertNotIn("FuelRegistry", source)

    def test_every_smelter_checks_fuel_and_resolves_time_at_its_own_context(self):
        for name, fuel_slot in SMELTERS:
            with self.subTest(name=name):
                source = (COMMON / "block/entity" / name).read_text(encoding="utf-8")
                self.assertIn("if (!(level instanceof ServerLevel serverLevel))", source)
                self.assertIn("Services.PLATFORM.getBurnTime(fuel, be.getLootContext(serverLevel))", source)
                self.assertIn("Services.PLATFORM.isCookingFuel(stack)", source)
                self.assertNotIn("Services.PLATFORM.getBurnTime(stack)", source)
                self.assertNotIn("Services.PLATFORM.getBurnTime(fuel)", source)

    def test_remainders_do_not_replace_unconsumed_fuel_or_disappear(self):
        for name, fuel_slot in SMELTERS:
            with self.subTest(name=name):
                source = (COMMON / "block/entity" / name).read_text(encoding="utf-8")
                self.assertIn("Services.PLATFORM.getCraftingRemainder(fuel)", source)
                self.assertIn("fuel.shrink(1);", source)
                self.assertIn("if (fuel.isEmpty())", source)
                self.assertIn("Containers.dropItemStack(serverLevel, pos.getX(), pos.getY(), pos.getZ(),", source)
                self.assertNotIn("getCraftingRemainingItem()", source)
                self.assertNotIn("new ItemStack(remainder)", source)


    def test_shift_click_fuel_slots_use_component_check(self):
        for name in ("AlloySmelterMenu.java", "CastingSmelterMenu.java",
                     "NetherAlloySmelterMenu.java"):
            with self.subTest(name=name):
                source = (COMMON / "screen" / name).read_text(encoding="utf-8")
                self.assertIn("Services.PLATFORM.isCookingFuel(", source)
                self.assertNotIn("Services.PLATFORM.getBurnTime(", source)

    def test_smelters_use_version_263_save_and_load_contract(self):
        for name, _ in SMELTERS:
            with self.subTest(name=name):
                source = (COMMON / "block/entity" / name).read_text(encoding="utf-8")
                self.assertIn("protected void saveAdditional(ValueOutput output)", source)
                self.assertIn("protected void loadAdditional(ValueInput input)", source)
                self.assertIn("ContainerHelper.saveAllItems(output,", source)
                self.assertIn("ContainerHelper.loadAllItems(input,", source)
                self.assertIn('output.putFloat("storedXp", storedExperience)', source)
                self.assertIn('storedExperience = input.getFloatOr("storedXp", 0.0F)', source)
                for key in ("burnTime", "maxBurnTime", "cookTime", "cookTimeTotal"):
                    self.assertIn(f'output.putInt("{key}", {key})', source)
                    self.assertIn(f'{key} = input.getIntOr("{key}", 0)', source)
                self.assertNotIn("HolderLookup.Provider registries", source)
                self.assertNotIn("tag.getInt(", source)

if __name__ == "__main__":
    unittest.main()
