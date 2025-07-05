package net.minecraft.commands.execution;

import net.minecraft.resources.ResourceLocation;

public interface TraceCallbacks extends AutoCloseable {
    void onCommand(int p_312367_, String p_309424_);

    void onReturn(int p_309782_, String p_310974_, int p_309683_);

    void onError(String p_312377_);

    void onCall(int p_313011_, ResourceLocation p_309460_, int p_309874_);

    @Override
    void close();
}