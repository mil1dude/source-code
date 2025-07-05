package net.minecraft.world.item;

import net.minecraft.network.protocol.game.ClientboundCooldownPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class ServerItemCooldowns extends ItemCooldowns {
    private final ServerPlayer player;

    public ServerItemCooldowns(ServerPlayer p_43067_) {
        this.player = p_43067_;
    }

    @Override
    protected void onCooldownStarted(ResourceLocation p_365314_, int p_43070_) {
        super.onCooldownStarted(p_365314_, p_43070_);
        this.player.connection.send(new ClientboundCooldownPacket(p_365314_, p_43070_));
    }

    @Override
    protected void onCooldownEnded(ResourceLocation p_361119_) {
        super.onCooldownEnded(p_361119_);
        this.player.connection.send(new ClientboundCooldownPacket(p_361119_, 0));
    }
}