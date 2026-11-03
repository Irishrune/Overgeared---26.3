# Phase 4 — Registry-key migration (2026-11-03)

Status: **source-only work in progress; not a playable mod.**

## Changes implemented

- Added `registerWithKey` variants for item and block deferred factories. Their factories now receive a `ResourceKey<Item>` or `ResourceKey<Block>` at construction time. Existing generic registration for non-item registries was not removed.
- Made default item factories pass `.setId(key)` to `Item.Properties` **before** creating an `Item`, as required for Minecraft 26.3.
- Migrated 32 custom `ModItems` item factories (for equipment, tools, armor, arrows, and the diamond upgrade template) to the key-aware factory contract. 31 directly set the key on their Properties; the diamond template does it in `DiamondUpgradeTemplateItem`.
- Migrated all 9 `ModBlocks` factories to set their ID on `BlockBehaviour.Properties` before block creation.
- Migrated block-item creation to use `.useBlockDescriptionPrefix().setId(key)`.
- Updated the Fabric registration bridge to register each value under its typed `ResourceKey` rather than a bare identifier.
- Migrated the diamond upgrade smithing template to the **documented Minecraft 26.3** seven-argument constructor that ends with `Item.Properties`. This preserves base and addition slot descriptions, but the old third title argument no longer exists.
- Added `RegistryKeyRegistrationTest.java`: compiles the **actual** `DeferredRegister.java` source against explicit fake Minecraft API classes in `port-tests/stubs/` to verify deferred registration order, key assignment, duplicate detection and block-item association.
- Added four source-level regression tests protecting ID injection across all 9 registered blocks and 32 custom items.

## Validation completed

- `python port-tests/run.py`: forging score + durability regressions + isolated registry test **passed** (using JDK 21; no game classes).
- `python -m unittest discover -s port-tests -p 'test_*.py' -v`: **8 tests passed** (4 prior resource tests + 4 new registration tests).
- `python tools/generate_263_assets.py --check`: all **180 generated resource definitions current**.
- `python tools/port_263_mapped_symbols.py --check`: **no pending mapped replacements**.

## Important limitations

- Real Fabric 26.3 compile and game launch **not verified**. The current execution container has JDK 21 and cannot fetch Gradle 9.8.2 or Minecraft dependencies.
- The isolated test uses purpose-built *stubs*, not real Minecraft, and only verifies the internal registration algorithm and factory contracts. Source checks are likewise static.
- Major 1.21.1 game changes remain unresolved in tool/armor constructors (`SwordItem`, `PickaxeItem`, `ArmorItem`, legacy `Tier`), the legacy forge and recipe systems, mixins, network packets, and rendering.
- Client animations, server startup, world persistence, and multiplayer gameplay are **not tested**.
- Do not put any artifact from this archive on the user's production server.

## API references

- https://docs.fabricmc.net/develop/items/first-item
- https://docs.fabricmc.net/develop/blocks/first-block
- https://mappings.relativitymc.org/26.3/net/minecraft/item/SmithingTemplateItem.html
- https://mappings.relativitymc.org/26.3/net/minecraft/item/Item$Settings.html
