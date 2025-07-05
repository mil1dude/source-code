package net.minecraft.world.level.storage.loot;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.context.ContextKey;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;

public class LootParams {
    private final ServerLevel level;
    private final ContextMap params;
    private final Map<ResourceLocation, LootParams.DynamicDrop> dynamicDrops;
    private final float luck;

    public LootParams(ServerLevel p_287766_, ContextMap p_364711_, Map<ResourceLocation, LootParams.DynamicDrop> p_287705_, float p_287671_) {
        this.level = p_287766_;
        this.params = p_364711_;
        this.dynamicDrops = p_287705_;
        this.luck = p_287671_;
    }

    public ServerLevel getLevel() {
        return this.level;
    }

    public ContextMap contextMap() {
        return this.params;
    }

    public void addDynamicDrops(ResourceLocation p_287768_, Consumer<ItemStack> p_287711_) {
        LootParams.DynamicDrop lootparams$dynamicdrop = this.dynamicDrops.get(p_287768_);
        if (lootparams$dynamicdrop != null) {
            lootparams$dynamicdrop.add(p_287711_);
        }
    }

    public float getLuck() {
        return this.luck;
    }

    public static class Builder {
        private final ServerLevel level;
        private final ContextMap.Builder params = new ContextMap.Builder();
        private final Map<ResourceLocation, LootParams.DynamicDrop> dynamicDrops = Maps.newHashMap();
        private float luck;

        public Builder(ServerLevel p_287594_) {
            this.level = p_287594_;
        }

        public ServerLevel getLevel() {
            return this.level;
        }

        public <T> LootParams.Builder withParameter(ContextKey<T> p_363960_, T p_287606_) {
            this.params.withParameter(p_363960_, p_287606_);
            return this;
        }

        public <T> LootParams.Builder withOptionalParameter(ContextKey<T> p_369471_, @Nullable T p_287630_) {
            this.params.withOptionalParameter(p_369471_, p_287630_);
            return this;
        }

        public <T> T getParameter(ContextKey<T> p_365868_) {
            return this.params.getParameter(p_365868_);
        }

        @Nullable
        public <T> T getOptionalParameter(ContextKey<T> p_361118_) {
            return this.params.getOptionalParameter(p_361118_);
        }

        public LootParams.Builder withDynamicDrop(ResourceLocation p_287734_, LootParams.DynamicDrop p_287724_) {
            LootParams.DynamicDrop lootparams$dynamicdrop = this.dynamicDrops.put(p_287734_, p_287724_);
            if (lootparams$dynamicdrop != null) {
                throw new IllegalStateException("Duplicated dynamic drop '" + this.dynamicDrops + "'");
            } else {
                return this;
            }
        }

        public LootParams.Builder withLuck(float p_287703_) {
            this.luck = p_287703_;
            return this;
        }

        public LootParams create(ContextKeySet p_367827_) {
            ContextMap contextmap = this.params.create(p_367827_);
            return new LootParams(this.level, contextmap, this.dynamicDrops, this.luck);
        }
    }

    @FunctionalInterface
    public interface DynamicDrop {
        void add(Consumer<ItemStack> p_287584_);
    }
}