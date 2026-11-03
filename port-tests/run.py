#!/usr/bin/env python3
"""Run Minecraft-independent regression tests with the installed JDK."""
from pathlib import Path
import subprocess
import tempfile

root = Path(__file__).resolve().parents[1]
score = root/'common/src/main/java/net/stirdrem/overgeared/util/ForgingScore.java'
durability = root/'common/src/main/java/net/stirdrem/overgeared/util/DurabilityMath.java'
tests = root/'port-tests/java/ForgingScoreTest.java'
durability_tests = root/'port-tests/java/DurabilityMathTest.java'
with tempfile.TemporaryDirectory(prefix='overgeared-tests-') as d:
    subprocess.run(['javac', '-d', d, str(score), str(tests), str(durability), str(durability_tests)], check=True)
    subprocess.run(['java', '-cp', d, 'ForgingScoreTest'], check=True)
    subprocess.run(['java', '-cp', d, 'DurabilityMathTest'], check=True)

# Verify the exact 3x3 input-grid indexing used by forging component transfer.
# This test compiles actual production source, not a rewritten test equivalent.
grid_source = root/'common/src/main/java/net/stirdrem/overgeared/recipe/ForgingGridSlots.java'
grid_test = root/'port-tests/java/ForgingGridSlotsTest.java'
with tempfile.TemporaryDirectory(prefix='overgeared-forging-grid-tests-') as d:
    subprocess.run(['javac', '-d', d, str(grid_source), str(grid_test)], check=True)
    subprocess.run(['java', '-cp', d, 'ForgingGridSlotsTest'], check=True)

# Compile the actual registry compatibility helper against minimal 26.3-shaped
# Minecraft stubs. This verifies key injection and deferred-registry semantics,
# but does not substitute for real Minecraft/Fabric compilation.
stubs = sorted((root/'port-tests/stubs').rglob('*.java'))
registrar = root/'common/src/main/java/net/stirdrem/overgeared/registry/DeferredRegister.java'
registry_test = root/'port-tests/java/RegistryKeyRegistrationTest.java'
with tempfile.TemporaryDirectory(prefix='overgeared-registry-tests-') as d:
    subprocess.run(['javac', '-d', d, *map(str, stubs), str(registrar), str(registry_test)], check=True)
    subprocess.run(['java', '-cp', d, 'RegistryKeyRegistrationTest'], check=True)

# Actual Phase 5 equipment classes against 26.3-shaped minimal API stubs.
# Contract-only verification; not a real Minecraft Gradle compile.
equipment_classes = [
    root/'common/src/main/java/net/stirdrem/overgeared/item/ModToolTiers.java',
    root/'common/src/main/java/net/stirdrem/overgeared/item/ModArmorMaterials.java',
    root/'common/src/main/java/net/stirdrem/overgeared/item/EquipmentStatMath.java',
    root/'port-tests/java/EquipmentMaterialsTest.java',
    root/'common/src/main/java/net/stirdrem/overgeared/item/custom/SmithingHammer.java',
    root/'common/src/main/java/net/stirdrem/overgeared/item/custom/Tongs.java',
    root/'port-tests/java/SmithingToolsTest.java',
]
equipment_stubs = sorted((root/'port-tests/equipment-stubs').rglob('*.java'))
with tempfile.TemporaryDirectory(prefix='overgeared-equipment-tests-') as d:
    subprocess.run(['javac','-d',d,*map(str,equipment_stubs+equipment_classes)],check=True)
    subprocess.run(['java','-cp',d,'EquipmentMaterialsTest'],check=True)
    subprocess.run(['java','-cp',d,'SmithingToolsTest'],check=True)
