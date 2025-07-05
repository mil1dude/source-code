package net.minecraft.world.level.levelgen.structure.pools.alias;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

record RandomGroup(SimpleWeightedRandomList<List<PoolAliasBinding>> groups) implements PoolAliasBinding {
    static MapCodec<RandomGroup> CODEC = RecordCodecBuilder.mapCodec(
        p_311954_ -> p_311954_.group(
                    SimpleWeightedRandomList.wrappedCodec(Codec.list(PoolAliasBinding.CODEC)).fieldOf("groups").forGetter(RandomGroup::groups)
                )
                .apply(p_311954_, RandomGroup::new)
    );

    @Override
    public void forEachResolved(RandomSource p_309696_, BiConsumer<ResourceKey<StructureTemplatePool>, ResourceKey<StructureTemplatePool>> p_312789_) {
        this.groups.getRandom(p_309696_).ifPresent(p_327489_ -> p_327489_.data().forEach(p_313096_ -> p_313096_.forEachResolved(p_309696_, p_312789_)));
    }

    @Override
    public Stream<ResourceKey<StructureTemplatePool>> allTargets() {
        return this.groups.unwrap().stream().flatMap(p_327490_ -> p_327490_.data().stream()).flatMap(PoolAliasBinding::allTargets);
    }

    @Override
    public MapCodec<RandomGroup> codec() {
        return CODEC;
    }
}