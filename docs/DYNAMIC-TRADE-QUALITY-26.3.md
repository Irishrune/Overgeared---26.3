# Quality-aware villager trades — Minecraft 26.3 (source-level migration)

This phase defines **all 38 original extra forged-equipment trade offers** through
Minecraft 26.3 data-driven `villager_trade` entries and additive
`minecraft:weaponsmith`, `minecraft:toolsmith`, and `minecraft:armorer`
trade tags.

## Preserved offer rules

- **Copper (level 1):** 12 uses, 2 XP, base emerald price 8
- **Iron (level 2):** 10 uses, 5 XP, base emerald price 14
- **Golden (level 2):** 10 uses, 5 XP, base emerald price 16
- **Steel (level 3):** 6 uses, 10 XP, base emerald price 24
- Reputation multiplier: 0.05 throughout.
- Base prices are adjusted *once*, using the same quality-dependent material
  multiplier as the former `QualityWrappedTrade`. The new
  `overgeared:set_forged_trade_quality` loot modifier applies the
  `overgeared:forging_quality` component and sets Minecraft's temporary
  `minecraft:additional_trade_cost` component to the price difference.
- Levels 1–2 roll **Poor 22%, Well 70%, Expert 8%**; level 3 rolls
  **Well 64%, Expert 30%, Perfect 6%**, preserving the old helper's thresholds.
- The legacy `ForgedItemTrade` constructor also assigned a preliminary quality
  and price, but the normal `QualityWrappedTrade` wrapper replaced these on
  forgeable equipment; the data-driven modifier models the final offer.

These additions are additive (`replace: false`) and do not overwrite vanilla
trade definitions. Minecraft still chooses offers by its existing trade sets,
so adding 38 possibilities does not make villagers stock all 38 simultaneously.

## Important remaining work

1. This is **not a successful compile or runtime test**: existing old
   `VillagerTrades.ItemListing` Java code still blocks full compilation,
   and it must only be retired once its remaining features are migrated.
2. Vanilla equipment trades previously wrapped with randomly rolled forging quality
   are not yet migrated.
3. Wandering trader blueprints still require randomized quality and tool type
   with accurate emerald prices.
4. Trade prices and components need a disposable-world test once the mod starts,
   especially around item-component registration and trade set reload.

Minecraft 26.3 changed the inline trade field name to
`given_item_modifier` and the item function discriminator to `type`;
the older plural and `function` keys would be incorrect here.


## Obsolete Java bridge isolated from Fabric compilation

After adding the 38 dynamic forged offers and two steel barters, the obsolete
`FabricTrades.register()` startup call is no longer used. Its old
`VillagerTrades.ItemListing` classes are excluded from active Fabric compilation;
they remain in Git source history. The `ModEvents` trade hooks were moved intact
to `porting/legacy-trades/ModEvents-trade-methods.txt` as a migration reference.

**Deliberately not yet feature-complete:** the old behavior that modifies
existing vanilla sword/tool/armor trades and the Wandering Trader's random
blueprints is not active in the replacement. Neither is being silently marked
as ported. Both must be implemented on the new trade data/item-modifier system
and validated in-game before release. This temporary isolation reduces dead-API
compile errors while we migrate the rest of the mod.
