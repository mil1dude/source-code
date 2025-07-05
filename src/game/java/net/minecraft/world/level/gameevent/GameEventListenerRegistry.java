package net.minecraft.world.level.gameevent;

import net.minecraft.core.Holder;
import net.minecraft.world.phys.Vec3;

public interface GameEventListenerRegistry {
    GameEventListenerRegistry NOOP = new GameEventListenerRegistry() {
        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public void register(GameEventListener p_251092_) {
        }

        @Override
        public void unregister(GameEventListener p_251937_) {
        }

        @Override
        public boolean visitInRangeListeners(Holder<GameEvent> p_332426_, Vec3 p_249086_, GameEvent.Context p_249012_, GameEventListenerRegistry.ListenerVisitor p_252106_) {
            return false;
        }
    };

    boolean isEmpty();

    void register(GameEventListener p_249257_);

    void unregister(GameEventListener p_248758_);

    boolean visitInRangeListeners(Holder<GameEvent> p_328591_, Vec3 p_249144_, GameEvent.Context p_249328_, GameEventListenerRegistry.ListenerVisitor p_250123_);

    @FunctionalInterface
    public interface ListenerVisitor {
        void visit(GameEventListener p_250787_, Vec3 p_251603_);
    }
}