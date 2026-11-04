# Dynamic forged villager-trade migration — Minecraft 26.3

The old `VillagerTrades.ItemListing` approach is no longer compatible with
Minecraft 26.3. This phase adds a custom **loot item modifier**
(`overgeared:set_forged_trade_quality`) backed by the original
`ForgingQualityHelper.rollQuality` and `priceForQuality` logic.

- The trade JSON starts with a baseline emerald cost of **7**.
- The modifier rolls an original level-dependent quality **once** and applies
  `overgeared:forging_quality` to the resulting item.
- It sets Minecraft's transient `minecraft:additional_trade_cost` to the
  difference between the rolled-quality price and baseline (7).
- For original forged gear at villager levels 1–3 the roll cannot produce
  Master quality, so the old one-use-only Masterwork branch does not apply.
- First migrated offer: Copper Sword, novice Weaponsmith, with 12 uses and 2 XP.

**Not completed:** other equipment's JSON offers, vanilla trade quality wrapping,
wandering blueprint trade generation, and removal/port of obsolete
`VillagerTrades` Java code. This code is not playable until the mod compiles.

Minecraft 26.3 renamed `given_item_modifiers` to `given_item_modifier`;
loot item functions now identify type using the `type` key.
See official 26.3 release notes.
