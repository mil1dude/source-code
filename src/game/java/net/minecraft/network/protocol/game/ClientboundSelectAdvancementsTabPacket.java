package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.ResourceLocation;

public class ClientboundSelectAdvancementsTabPacket implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundSelectAdvancementsTabPacket> STREAM_CODEC = Packet.codec(
        ClientboundSelectAdvancementsTabPacket::write, ClientboundSelectAdvancementsTabPacket::new
    );
    @Nullable
    private final ResourceLocation tab;

    public ClientboundSelectAdvancementsTabPacket(@Nullable ResourceLocation p_133006_) {
        this.tab = p_133006_;
    }

    private ClientboundSelectAdvancementsTabPacket(FriendlyByteBuf p_179198_) {
        this.tab = p_179198_.readNullable(FriendlyByteBuf::readResourceLocation);
    }

    private void write(FriendlyByteBuf p_133015_) {
        p_133015_.writeNullable(this.tab, FriendlyByteBuf::writeResourceLocation);
    }

    @Override
    public PacketType<ClientboundSelectAdvancementsTabPacket> type() {
        return GamePacketTypes.CLIENTBOUND_SELECT_ADVANCEMENTS_TAB;
    }

    public void handle(ClientGamePacketListener p_133012_) {
        p_133012_.handleSelectAdvancementsTab(this);
    }

    @Nullable
    public ResourceLocation getTab() {
        return this.tab;
    }
}