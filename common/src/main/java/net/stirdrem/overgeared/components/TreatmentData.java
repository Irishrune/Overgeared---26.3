package net.stirdrem.overgeared.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** One persistent treatment; replacement never accumulates modifiers. */
public record TreatmentData(String trait, boolean flawless) {
    public static final Codec<TreatmentData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("trait").forGetter(TreatmentData::trait),
            Codec.BOOL.fieldOf("flawless").forGetter(TreatmentData::flawless)
    ).apply(i, TreatmentData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, TreatmentData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, TreatmentData::trait,
            ByteBufCodecs.BOOL, TreatmentData::flawless, TreatmentData::new);
}
