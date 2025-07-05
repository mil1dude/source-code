package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.ResourceLocation;

public class ServerboundSeenAdvancementsPacket implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ServerboundSeenAdvancementsPacket> STREAM_CODEC = Packet.codec(
        ServerboundSeenAdvancementsPacket::write, ServerboundSeenAdvancementsPacket::new
    );
    private final ServerboundSeenAdvancementsPacket.Action action;
    @Nullable
    private final ResourceLocation tab;

    public ServerboundSeenAdvancementsPacket(ServerboundSeenAdvancementsPacket.Action p_134434_, @Nullable ResourceLocation p_134435_) {
        this.action = p_134434_;
        this.tab = p_134435_;
    }

    public static ServerboundSeenAdvancementsPacket openedTab(AdvancementHolder p_300057_) {
        return new ServerboundSeenAdvancementsPacket(ServerboundSeenAdvancementsPacket.Action.OPENED_TAB, p_300057_.id());
    }

    public static ServerboundSeenAdvancementsPacket closedScreen() {
        return new ServerboundSeenAdvancementsPacket(ServerboundSeenAdvancementsPacket.Action.CLOSED_SCREEN, null);
    }

    private ServerboundSeenAdvancementsPacket(FriendlyByteBuf p_179744_) {
        this.action = p_179744_.readEnum(ServerboundSeenAdvancementsPacket.Action.class);
        if (this.action == ServerboundSeenAdvancementsPacket.Action.OPENED_TAB) {
            this.tab = p_179744_.readResourceLocation();
        } else {
            this.tab = null;
        }
    }

    private void write(FriendlyByteBuf p_134446_) {
        p_134446_.writeEnum(this.action);
        if (this.action == ServerboundSeenAdvancementsPacket.Action.OPENED_TAB) {
            p_134446_.writeResourceLocation(this.tab);
        }
    }

    @Override
    public PacketType<ServerboundSeenAdvancementsPacket> type() {
        return GamePacketTypes.SERVERBOUND_SEEN_ADVANCEMENTS;
    }

    public void handle(ServerGamePacketListener p_134441_) {
        p_134441_.handleSeenAdvancements(this);
    }

    public ServerboundSeenAdvancementsPacket.Action getAction() {
        return this.action;
    }

    @Nullable
    public ResourceLocation getTab() {
        return this.tab;
    }

    public static enum Action {
        OPENED_TAB,
        CLOSED_SCREEN;
    }
}