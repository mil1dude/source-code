package net.minecraft.world.level.storage.loot.providers.nbt;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.nbt.Tag;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class ContextNbtProvider implements NbtProvider {
    private static final String BLOCK_ENTITY_ID = "block_entity";
    private static final ContextNbtProvider.Getter BLOCK_ENTITY_PROVIDER = new ContextNbtProvider.Getter() {
        @Override
        public Tag get(LootContext p_165582_) {
            BlockEntity blockentity = p_165582_.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
            return blockentity != null ? blockentity.saveWithFullMetadata(blockentity.getLevel().registryAccess()) : null;
        }

        @Override
        public String getId() {
            return "block_entity";
        }

        @Override
        public Set<ContextKey<?>> getReferencedContextParams() {
            return Set.of(LootContextParams.BLOCK_ENTITY);
        }
    };
    public static final ContextNbtProvider BLOCK_ENTITY = new ContextNbtProvider(BLOCK_ENTITY_PROVIDER);
    private static final Codec<ContextNbtProvider.Getter> GETTER_CODEC = Codec.STRING.xmap(p_298021_ -> {
        if (p_298021_.equals("block_entity")) {
            return BLOCK_ENTITY_PROVIDER;
        } else {
            LootContext.EntityTarget lootcontext$entitytarget = LootContext.EntityTarget.getByName(p_298021_);
            return forEntity(lootcontext$entitytarget);
        }
    }, ContextNbtProvider.Getter::getId);
    public static final MapCodec<ContextNbtProvider> CODEC = RecordCodecBuilder.mapCodec(
        p_300408_ -> p_300408_.group(GETTER_CODEC.fieldOf("target").forGetter(p_300339_ -> p_300339_.getter)).apply(p_300408_, ContextNbtProvider::new)
    );
    public static final Codec<ContextNbtProvider> INLINE_CODEC = GETTER_CODEC.xmap(ContextNbtProvider::new, p_298349_ -> p_298349_.getter);
    private final ContextNbtProvider.Getter getter;

    private static ContextNbtProvider.Getter forEntity(final LootContext.EntityTarget p_165578_) {
        return new ContextNbtProvider.Getter() {
            @Nullable
            @Override
            public Tag get(LootContext p_165589_) {
                Entity entity = p_165589_.getOptionalParameter(p_165578_.getParam());
                return entity != null ? NbtPredicate.getEntityTagToCompare(entity) : null;
            }

            @Override
            public String getId() {
                return p_165578_.name();
            }

            @Override
            public Set<ContextKey<?>> getReferencedContextParams() {
                return Set.of(p_165578_.getParam());
            }
        };
    }

    private ContextNbtProvider(ContextNbtProvider.Getter p_165568_) {
        this.getter = p_165568_;
    }

    @Override
    public LootNbtProviderType getType() {
        return NbtProviders.CONTEXT;
    }

    @Nullable
    @Override
    public Tag get(LootContext p_165573_) {
        return this.getter.get(p_165573_);
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return this.getter.getReferencedContextParams();
    }

    public static NbtProvider forContextEntity(LootContext.EntityTarget p_165571_) {
        return new ContextNbtProvider(forEntity(p_165571_));
    }

    interface Getter {
        @Nullable
        Tag get(LootContext p_165591_);

        String getId();

        Set<ContextKey<?>> getReferencedContextParams();
    }
}