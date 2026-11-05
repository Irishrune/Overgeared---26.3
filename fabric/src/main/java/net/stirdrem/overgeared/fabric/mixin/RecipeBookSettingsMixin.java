package net.stirdrem.overgeared.fabric.mixin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.stats.RecipeBookSettings;
import net.minecraft.world.inventory.RecipeBookType;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import java.util.function.UnaryOperator;

/** Extends lookup, mutation, copy, saved data and network synchronization. */
@Mixin(RecipeBookSettings.class)
public abstract class RecipeBookSettingsMixin {
    @Shadow @Final @Mutable public static MapCodec<RecipeBookSettings> MAP_CODEC;
    @Shadow @Final @Mutable public static StreamCodec<FriendlyByteBuf, RecipeBookSettings> STREAM_CODEC;
    @Unique private RecipeBookSettings.TypeSettings overgeared$forging = RecipeBookSettings.TypeSettings.DEFAULT;
    @Unique private static RecipeBookSettingsMixin overgeared$access(RecipeBookSettings value) {
        return (RecipeBookSettingsMixin)(Object)value;
    }
    @Inject(method = "getSettings", at = @At("HEAD"), cancellable = true)
    private void overgeared$get(RecipeBookType type, CallbackInfoReturnable<RecipeBookSettings.TypeSettings> cir) {
        if (type.name().equals("OVERGEARED_FORGING")) cir.setReturnValue(overgeared$forging);
    }
    @Inject(method = "updateSettings", at = @At("HEAD"), cancellable = true)
    private void overgeared$update(RecipeBookType type, UnaryOperator<RecipeBookSettings.TypeSettings> update, CallbackInfo ci) {
        if (type.name().equals("OVERGEARED_FORGING")) {
            overgeared$forging = update.apply(overgeared$forging);
            ci.cancel();
        }
    }
    @Inject(method = "copy", at = @At("RETURN"))
    private void overgeared$copy(CallbackInfoReturnable<RecipeBookSettings> cir) {
        overgeared$access(cir.getReturnValue()).overgeared$forging = overgeared$forging;
    }
    @Inject(method = "replaceFrom", at = @At("TAIL"))
    private void overgeared$replace(RecipeBookSettings other, CallbackInfo ci) {
        overgeared$forging = overgeared$access(other).overgeared$forging;
    }
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void overgeared$codecs(CallbackInfo ci) {
        MapCodec<RecipeBookSettings> vanilla = MAP_CODEC;
        MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            vanilla.forGetter((RecipeBookSettings s) -> s),
            Codec.BOOL.optionalFieldOf("is_overgeared_forging_gui_open", false)
                .forGetter((RecipeBookSettings s) -> overgeared$access(s).overgeared$forging.open()),
            Codec.BOOL.optionalFieldOf("is_overgeared_forging_filtering_craftable", false)
                .forGetter((RecipeBookSettings s) -> overgeared$access(s).overgeared$forging.filtering())
        ).apply(i, (s, open, filtering) -> {
            overgeared$access(s).overgeared$forging = new RecipeBookSettings.TypeSettings(open, filtering);
            return s;
        }));
        StreamCodec<FriendlyByteBuf, RecipeBookSettings> vanillaNetwork = STREAM_CODEC;
        STREAM_CODEC = StreamCodec.composite(vanillaNetwork, s -> s,
            RecipeBookSettings.TypeSettings.STREAM_CODEC, s -> overgeared$access(s).overgeared$forging,
            (s, forging) -> { overgeared$access(s).overgeared$forging = forging; return s; });
    }
}
