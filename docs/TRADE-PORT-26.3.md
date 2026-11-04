# Minecraft 26.3 villager-trade migration — first step

The Fabric 26.3 trade API is data-driven. The old Java `VillagerTrades.ItemListing` tables no longer exist and **must not be used** for runtime trade registration. The official Fabric 26.1 migration notes prescribe definitions under `data/<namespace>/villager_trade` and additions to tags under `data/minecraft/tags/villager_trade`.

## Completed: static steel barters

Two original Overgeared novice trades are now defined as datapack JSONs, without overwriting vanilla trade sets:

- Sell **1 steel ingot** for **2 emeralds**
- Buy **1 steel ingot** for **2 emeralds**

Both preserve the old maximum 12 uses, villager XP 5, and reputation discount 0.05. The vanilla `minecraft:common_smith/level_1` tag is extended with `replace: false` so Armorer, Toolsmith and Weaponsmith remain eligible. Minecraft chooses offers from that pool according to the existing trade-set selection rules; inclusion does **not** guarantee each trade appears.

## Not yet complete: dynamic forged equipment and blueprint offers

The legacy Java trade classes still require a 26.3 redesign. In particular:

- `ForgedItemTrade` and `QualityWrappedTrade` randomly assign item quality, change prices, and limit Masterwork offer stock. Plain static trade JSON would silently remove that behavior.
- Equipment and head offers by profession and rank need definitions and quality-aware item generation, most likely a custom item modifier or a new trade-generation hook, validated against the actual 26.3 API.
- `BlueprintWanderingTrade` selects random blueprint tool types and quality. Its 128-emerald Masterwork cost cannot be represented by a single ordinary 64-stack cost; migration must preserve intent without generating an impossible trade.
- `ModEvents.onVillagerTrades`, `FabricTrades`, and the old `VillagerTrades.ItemListing` classes are **not yet compilable**. Do not remove the quality implementation to obtain a misleading green build.

This is source-level JSON migration, **not** proof that the mod starts or that villagers offer these trades. Verify with a successful Java 25 build and a disposable 26.3 test world before distributing the JAR.

References:
- https://www.fabricmc.net/2026/03/14/261.html
- https://www.minecraft.net/en-us/article/minecraft-java-edition-26-3
