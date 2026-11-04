# Fabric 26.3: smithing anvil sessions across dimensions

The old `ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD` event is not
present in Fabric API 26.3. The Fabric entry point now tracks each online
player's last `Level` **on the server thread** and compares it at every server
tick. During a dimension transition, it releases the player's forging session
using the **origin world**, not the new dimension with potentially identical
block coordinates. This ensures anvil progress, ownership, and client state
reset. Respawn and disconnect have explicit cleanup paths; the map is cleared
on server shutdown.

This change fixes a source-level migration and a previously identified
cross-dimension ownership bug. Actual multiplayer validation is still needed
once the mod compiles, including simultaneous anvils at the same coordinates
in different dimensions.
