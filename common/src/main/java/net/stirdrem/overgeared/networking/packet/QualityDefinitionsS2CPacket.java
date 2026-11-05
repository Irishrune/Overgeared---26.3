package net.stirdrem.overgeared.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.datapack.QualityAttributeReloadListener;
import net.stirdrem.overgeared.networking.PacketContext;

/** Keeps item attribute calculations and tooltips aligned with server datapacks. */
public record QualityDefinitionsS2CPacket(String json) implements CustomPacketPayload {
    public static final Type<QualityDefinitionsS2CPacket> TYPE = new Type<>(OvergearedMod.loc("quality_definitions"));
    public static final StreamCodec<FriendlyByteBuf, QualityDefinitionsS2CPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeUtf(packet.json(), 1_048_576),
            buf -> new QualityDefinitionsS2CPacket(buf.readUtf(1_048_576)));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    public static void handle(QualityDefinitionsS2CPacket packet, PacketContext context) {
        context.enqueueWork(() -> QualityAttributeReloadListener.acceptSync(packet.json()));
    }
}
