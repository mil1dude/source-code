package net.minecraft.commands;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerFunctionManager;

public class CacheableFunction {
    public static final Codec<CacheableFunction> CODEC = ResourceLocation.CODEC.xmap(CacheableFunction::new, CacheableFunction::getId);
    private final ResourceLocation id;
    private boolean resolved;
    private Optional<CommandFunction<CommandSourceStack>> function = Optional.empty();

    public CacheableFunction(ResourceLocation p_312073_) {
        this.id = p_312073_;
    }

    public Optional<CommandFunction<CommandSourceStack>> get(ServerFunctionManager p_310125_) {
        if (!this.resolved) {
            this.function = p_310125_.get(this.id);
            this.resolved = true;
        }

        return this.function;
    }

    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public boolean equals(Object p_313210_) {
        if (p_313210_ == this) {
            return true;
        } else {
            if (p_313210_ instanceof CacheableFunction cacheablefunction && this.getId().equals(cacheablefunction.getId())) {
                return true;
            }

            return false;
        }
    }
}