package net.minecraft.world.level.timers;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;

public class FunctionCallback implements TimerCallback<MinecraftServer> {
    final ResourceLocation functionId;

    public FunctionCallback(ResourceLocation p_82164_) {
        this.functionId = p_82164_;
    }

    public void handle(MinecraftServer p_82172_, TimerQueue<MinecraftServer> p_82173_, long p_82174_) {
        ServerFunctionManager serverfunctionmanager = p_82172_.getFunctions();
        serverfunctionmanager.get(this.functionId)
            .ifPresent(p_309355_ -> serverfunctionmanager.execute((CommandFunction<CommandSourceStack>)p_309355_, serverfunctionmanager.getGameLoopSender()));
    }

    public static class Serializer extends TimerCallback.Serializer<MinecraftServer, FunctionCallback> {
        public Serializer() {
            super(ResourceLocation.withDefaultNamespace("function"), FunctionCallback.class);
        }

        public void serialize(CompoundTag p_82182_, FunctionCallback p_82183_) {
            p_82182_.putString("Name", p_82183_.functionId.toString());
        }

        public FunctionCallback deserialize(CompoundTag p_82180_) {
            ResourceLocation resourcelocation = ResourceLocation.parse(p_82180_.getString("Name"));
            return new FunctionCallback(resourcelocation);
        }
    }
}