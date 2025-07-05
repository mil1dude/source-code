package net.minecraft.network.chat;

import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface ChatDecorator {
    ChatDecorator PLAIN = (p_296388_, p_296389_) -> p_296389_;

    Component decorate(@Nullable ServerPlayer p_236962_, Component p_236963_);
}