package net.stirdrem.overgeared.fabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static net.stirdrem.overgeared.util.BrokenHelper.isBroken;

/**
 * Broken swords cannot sweep. Vanilla uses {@code isSweepAttack} where NeoForge
 * checks {@code ItemAbilities.SWORD_SWEEP} (see the NeoForge {@code PlayerMixin}).
 */
@Mixin(Player.class)
public abstract class PlayerSweepMixin {
    @ModifyReturnValue(method = "isSweepAttack", at = @At("RETURN"))
    private boolean overgeared$noSweepWhenBroken(boolean isSword) {
        return isSword && !isBroken(((Player) (Object) this).getMainHandItem());
    }
}
