package net.minecraft.util.debugchart;

import com.google.common.collect.Maps;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Map.Entry;
import net.minecraft.Util;
import net.minecraft.network.protocol.game.ClientboundDebugSamplePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

public class DebugSampleSubscriptionTracker {
    public static final int STOP_SENDING_AFTER_TICKS = 200;
    public static final int STOP_SENDING_AFTER_MS = 10000;
    private final PlayerList playerList;
    private final EnumMap<RemoteDebugSampleType, Map<ServerPlayer, DebugSampleSubscriptionTracker.SubscriptionStartedAt>> subscriptions;
    private final Queue<DebugSampleSubscriptionTracker.SubscriptionRequest> subscriptionRequestQueue = new LinkedList<>();

    public DebugSampleSubscriptionTracker(PlayerList p_332100_) {
        this.playerList = p_332100_;
        this.subscriptions = new EnumMap<>(RemoteDebugSampleType.class);

        for (RemoteDebugSampleType remotedebugsampletype : RemoteDebugSampleType.values()) {
            this.subscriptions.put(remotedebugsampletype, Maps.newHashMap());
        }
    }

    public boolean shouldLogSamples(RemoteDebugSampleType p_328402_) {
        return !this.subscriptions.get(p_328402_).isEmpty();
    }

    public void broadcast(ClientboundDebugSamplePacket p_331964_) {
        for (ServerPlayer serverplayer : this.subscriptions.get(p_331964_.debugSampleType()).keySet()) {
            serverplayer.connection.send(p_331964_);
        }
    }

    public void subscribe(ServerPlayer p_328157_, RemoteDebugSampleType p_336058_) {
        if (this.playerList.isOp(p_328157_.getGameProfile())) {
            this.subscriptionRequestQueue.add(new DebugSampleSubscriptionTracker.SubscriptionRequest(p_328157_, p_336058_));
        }
    }

    public void tick(int p_335345_) {
        long i = Util.getMillis();
        this.handleSubscriptions(i, p_335345_);
        this.handleUnsubscriptions(i, p_335345_);
    }

    private void handleSubscriptions(long p_331878_, int p_331066_) {
        for (DebugSampleSubscriptionTracker.SubscriptionRequest debugsamplesubscriptiontracker$subscriptionrequest : this.subscriptionRequestQueue) {
            this.subscriptions
                .get(debugsamplesubscriptiontracker$subscriptionrequest.sampleType())
                .put(
                    debugsamplesubscriptiontracker$subscriptionrequest.player(),
                    new DebugSampleSubscriptionTracker.SubscriptionStartedAt(p_331878_, p_331066_)
                );
        }
    }

    private void handleUnsubscriptions(long p_335801_, int p_335929_) {
        for (Map<ServerPlayer, DebugSampleSubscriptionTracker.SubscriptionStartedAt> map : this.subscriptions.values()) {
            map.entrySet()
                .removeIf(
                    p_336353_ -> {
                        boolean flag = !this.playerList.isOp(p_336353_.getKey().getGameProfile());
                        DebugSampleSubscriptionTracker.SubscriptionStartedAt debugsamplesubscriptiontracker$subscriptionstartedat = p_336353_.getValue();
                        return flag
                            || p_335929_ > debugsamplesubscriptiontracker$subscriptionstartedat.tick() + 200
                                && p_335801_ > debugsamplesubscriptiontracker$subscriptionstartedat.millis() + 10000L;
                    }
                );
        }
    }

    static record SubscriptionRequest(ServerPlayer player, RemoteDebugSampleType sampleType) {
    }

    static record SubscriptionStartedAt(long millis, int tick) {
    }
}