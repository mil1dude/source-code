package net.minecraft.network.protocol.game;

import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.item.ItemStack;

public class ClientboundContainerSetContentPacket implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundContainerSetContentPacket> STREAM_CODEC = Packet.codec(
        ClientboundContainerSetContentPacket::write, ClientboundContainerSetContentPacket::new
    );
    private final int containerId;
    private final int stateId;
    private final List<ItemStack> items;
    private final ItemStack carriedItem;

    public ClientboundContainerSetContentPacket(int p_182704_, int p_182705_, NonNullList<ItemStack> p_182706_, ItemStack p_182707_) {
        this.containerId = p_182704_;
        this.stateId = p_182705_;
        this.items = NonNullList.withSize(p_182706_.size(), ItemStack.EMPTY);

        for (int i = 0; i < p_182706_.size(); i++) {
            this.items.set(i, p_182706_.get(i).copy());
        }

        this.carriedItem = p_182707_.copy();
    }

    private ClientboundContainerSetContentPacket(RegistryFriendlyByteBuf p_332879_) {
        this.containerId = p_332879_.readContainerId();
        this.stateId = p_332879_.readVarInt();
        this.items = ItemStack.OPTIONAL_LIST_STREAM_CODEC.decode(p_332879_);
        this.carriedItem = ItemStack.OPTIONAL_STREAM_CODEC.decode(p_332879_);
    }

    private void write(RegistryFriendlyByteBuf p_330970_) {
        p_330970_.writeContainerId(this.containerId);
        p_330970_.writeVarInt(this.stateId);
        ItemStack.OPTIONAL_LIST_STREAM_CODEC.encode(p_330970_, this.items);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(p_330970_, this.carriedItem);
    }

    @Override
    public PacketType<ClientboundContainerSetContentPacket> type() {
        return GamePacketTypes.CLIENTBOUND_CONTAINER_SET_CONTENT;
    }

    public void handle(ClientGamePacketListener p_131953_) {
        p_131953_.handleContainerContent(this);
    }

    public int getContainerId() {
        return this.containerId;
    }

    public List<ItemStack> getItems() {
        return this.items;
    }

    public ItemStack getCarriedItem() {
        return this.carriedItem;
    }

    public int getStateId() {
        return this.stateId;
    }
}