package net.minecraft.util.debugchart;

import net.minecraft.network.protocol.game.ClientboundDebugSamplePacket;

public class RemoteSampleLogger extends AbstractSampleLogger {
    private final DebugSampleSubscriptionTracker subscriptionTracker;
    private final RemoteDebugSampleType sampleType;

    public RemoteSampleLogger(int p_329489_, DebugSampleSubscriptionTracker p_332606_, RemoteDebugSampleType p_331596_) {
        this(p_329489_, p_332606_, p_331596_, new long[p_329489_]);
    }

    public RemoteSampleLogger(int p_334352_, DebugSampleSubscriptionTracker p_334313_, RemoteDebugSampleType p_332243_, long[] p_333261_) {
        super(p_334352_, p_333261_);
        this.subscriptionTracker = p_334313_;
        this.sampleType = p_332243_;
    }

    @Override
    protected void useSample() {
        this.subscriptionTracker.broadcast(new ClientboundDebugSamplePacket((long[])this.sample.clone(), this.sampleType));
    }
}