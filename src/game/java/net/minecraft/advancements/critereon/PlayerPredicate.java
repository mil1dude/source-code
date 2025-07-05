package net.minecraft.advancements.critereon;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMaps;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap.Entry;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.StatsCounter;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public record PlayerPredicate(
    MinMaxBounds.Ints level,
    GameTypePredicate gameType,
    List<PlayerPredicate.StatMatcher<?>> stats,
    Object2BooleanMap<ResourceKey<Recipe<?>>> recipes,
    Map<ResourceLocation, PlayerPredicate.AdvancementPredicate> advancements,
    Optional<EntityPredicate> lookingAt,
    Optional<InputPredicate> input
) implements EntitySubPredicate {
    public static final int LOOKING_AT_RANGE = 100;
    public static final MapCodec<PlayerPredicate> CODEC = RecordCodecBuilder.mapCodec(
        p_296141_ -> p_296141_.group(
                    MinMaxBounds.Ints.CODEC.optionalFieldOf("level", MinMaxBounds.Ints.ANY).forGetter(PlayerPredicate::level),
                    GameTypePredicate.CODEC.optionalFieldOf("gamemode", GameTypePredicate.ANY).forGetter(PlayerPredicate::gameType),
                    PlayerPredicate.StatMatcher.CODEC.listOf().optionalFieldOf("stats", List.of()).forGetter(PlayerPredicate::stats),
                    ExtraCodecs.object2BooleanMap(ResourceKey.codec(Registries.RECIPE))
                        .optionalFieldOf("recipes", Object2BooleanMaps.emptyMap())
                        .forGetter(PlayerPredicate::recipes),
                    Codec.unboundedMap(ResourceLocation.CODEC, PlayerPredicate.AdvancementPredicate.CODEC)
                        .optionalFieldOf("advancements", Map.of())
                        .forGetter(PlayerPredicate::advancements),
                    EntityPredicate.CODEC.optionalFieldOf("looking_at").forGetter(PlayerPredicate::lookingAt),
                    InputPredicate.CODEC.optionalFieldOf("input").forGetter(PlayerPredicate::input)
                )
                .apply(p_296141_, PlayerPredicate::new)
    );

    @Override
    public boolean matches(Entity p_222484_, ServerLevel p_222485_, @Nullable Vec3 p_222486_) {
        if (!(p_222484_ instanceof ServerPlayer serverplayer)) {
            return false;
        } else if (!this.level.matches(serverplayer.experienceLevel)) {
            return false;
        } else if (!this.gameType.matches(serverplayer.gameMode.getGameModeForPlayer())) {
            return false;
        } else {
            StatsCounter statscounter = serverplayer.getStats();

            for (PlayerPredicate.StatMatcher<?> statmatcher : this.stats) {
                if (!statmatcher.matches(statscounter)) {
                    return false;
                }
            }

            ServerRecipeBook serverrecipebook = serverplayer.getRecipeBook();

            for (Entry<ResourceKey<Recipe<?>>> entry : this.recipes.object2BooleanEntrySet()) {
                if (serverrecipebook.contains(entry.getKey()) != entry.getBooleanValue()) {
                    return false;
                }
            }

            if (!this.advancements.isEmpty()) {
                PlayerAdvancements playeradvancements = serverplayer.getAdvancements();
                ServerAdvancementManager serveradvancementmanager = serverplayer.getServer().getAdvancements();

                for (java.util.Map.Entry<ResourceLocation, PlayerPredicate.AdvancementPredicate> entry1 : this.advancements.entrySet()) {
                    AdvancementHolder advancementholder = serveradvancementmanager.get(entry1.getKey());
                    if (advancementholder == null || !entry1.getValue().test(playeradvancements.getOrStartProgress(advancementholder))) {
                        return false;
                    }
                }
            }

            if (this.lookingAt.isPresent()) {
                Vec3 vec3 = serverplayer.getEyePosition();
                Vec3 vec31 = serverplayer.getViewVector(1.0F);
                Vec3 vec32 = vec3.add(vec31.x * 100.0, vec31.y * 100.0, vec31.z * 100.0);
                EntityHitResult entityhitresult = ProjectileUtil.getEntityHitResult(
                    serverplayer.level(), serverplayer, vec3, vec32, new AABB(vec3, vec32).inflate(1.0), p_156765_ -> !p_156765_.isSpectator(), 0.0F
                );
                if (entityhitresult == null || entityhitresult.getType() != HitResult.Type.ENTITY) {
                    return false;
                }

                Entity entity = entityhitresult.getEntity();
                if (!this.lookingAt.get().matches(serverplayer, entity) || !serverplayer.hasLineOfSight(entity)) {
                    return false;
                }
            }

            return !this.input.isPresent() || this.input.get().matches(serverplayer.getLastClientInput());
        }
    }

    @Override
    public MapCodec<PlayerPredicate> codec() {
        return EntitySubPredicates.PLAYER;
    }

    static record AdvancementCriterionsPredicate(Object2BooleanMap<String> criterions) implements PlayerPredicate.AdvancementPredicate {
        public static final Codec<PlayerPredicate.AdvancementCriterionsPredicate> CODEC = ExtraCodecs.object2BooleanMap(Codec.STRING)
            .xmap(PlayerPredicate.AdvancementCriterionsPredicate::new, PlayerPredicate.AdvancementCriterionsPredicate::criterions);

        public boolean test(AdvancementProgress p_62296_) {
            for (Entry<String> entry : this.criterions.object2BooleanEntrySet()) {
                CriterionProgress criterionprogress = p_62296_.getCriterion(entry.getKey());
                if (criterionprogress == null || criterionprogress.isDone() != entry.getBooleanValue()) {
                    return false;
                }
            }

            return true;
        }
    }

    static record AdvancementDonePredicate(boolean state) implements PlayerPredicate.AdvancementPredicate {
        public static final Codec<PlayerPredicate.AdvancementDonePredicate> CODEC = Codec.BOOL
            .xmap(PlayerPredicate.AdvancementDonePredicate::new, PlayerPredicate.AdvancementDonePredicate::state);

        public boolean test(AdvancementProgress p_62304_) {
            return p_62304_.isDone() == this.state;
        }
    }

    interface AdvancementPredicate extends Predicate<AdvancementProgress> {
        Codec<PlayerPredicate.AdvancementPredicate> CODEC = Codec.either(
                PlayerPredicate.AdvancementDonePredicate.CODEC, PlayerPredicate.AdvancementCriterionsPredicate.CODEC
            )
            .xmap(Either::unwrap, p_298131_ -> {
                if (p_298131_ instanceof PlayerPredicate.AdvancementDonePredicate playerpredicate$advancementdonepredicate) {
                    return Either.left(playerpredicate$advancementdonepredicate);
                } else if (p_298131_ instanceof PlayerPredicate.AdvancementCriterionsPredicate playerpredicate$advancementcriterionspredicate) {
                    return Either.right(playerpredicate$advancementcriterionspredicate);
                } else {
                    throw new UnsupportedOperationException();
                }
            });
    }

    public static class Builder {
        private MinMaxBounds.Ints level = MinMaxBounds.Ints.ANY;
        private GameTypePredicate gameType = GameTypePredicate.ANY;
        private final ImmutableList.Builder<PlayerPredicate.StatMatcher<?>> stats = ImmutableList.builder();
        private final Object2BooleanMap<ResourceKey<Recipe<?>>> recipes = new Object2BooleanOpenHashMap<>();
        private final Map<ResourceLocation, PlayerPredicate.AdvancementPredicate> advancements = Maps.newHashMap();
        private Optional<EntityPredicate> lookingAt = Optional.empty();
        private Optional<InputPredicate> input = Optional.empty();

        public static PlayerPredicate.Builder player() {
            return new PlayerPredicate.Builder();
        }

        public PlayerPredicate.Builder setLevel(MinMaxBounds.Ints p_156776_) {
            this.level = p_156776_;
            return this;
        }

        public <T> PlayerPredicate.Builder addStat(StatType<T> p_300081_, Holder.Reference<T> p_298048_, MinMaxBounds.Ints p_156770_) {
            this.stats.add(new PlayerPredicate.StatMatcher<>(p_300081_, p_298048_, p_156770_));
            return this;
        }

        public PlayerPredicate.Builder addRecipe(ResourceKey<Recipe<?>> p_367099_, boolean p_156782_) {
            this.recipes.put(p_367099_, p_156782_);
            return this;
        }

        public PlayerPredicate.Builder setGameType(GameTypePredicate p_345199_) {
            this.gameType = p_345199_;
            return this;
        }

        public PlayerPredicate.Builder setLookingAt(EntityPredicate.Builder p_299861_) {
            this.lookingAt = Optional.of(p_299861_.build());
            return this;
        }

        public PlayerPredicate.Builder checkAdvancementDone(ResourceLocation p_156784_, boolean p_156785_) {
            this.advancements.put(p_156784_, new PlayerPredicate.AdvancementDonePredicate(p_156785_));
            return this;
        }

        public PlayerPredicate.Builder checkAdvancementCriterions(ResourceLocation p_156778_, Map<String, Boolean> p_156779_) {
            this.advancements.put(p_156778_, new PlayerPredicate.AdvancementCriterionsPredicate(new Object2BooleanOpenHashMap<>(p_156779_)));
            return this;
        }

        public PlayerPredicate.Builder hasInput(InputPredicate p_362570_) {
            this.input = Optional.of(p_362570_);
            return this;
        }

        public PlayerPredicate build() {
            return new PlayerPredicate(this.level, this.gameType, this.stats.build(), this.recipes, this.advancements, this.lookingAt, this.input);
        }
    }

    static record StatMatcher<T>(StatType<T> type, Holder<T> value, MinMaxBounds.Ints range, Supplier<Stat<T>> stat) {
        public static final Codec<PlayerPredicate.StatMatcher<?>> CODEC = BuiltInRegistries.STAT_TYPE
            .byNameCodec()
            .dispatch(PlayerPredicate.StatMatcher::type, PlayerPredicate.StatMatcher::createTypedCodec);

        public StatMatcher(StatType<T> p_297612_, Holder<T> p_301288_, MinMaxBounds.Ints p_298901_) {
            this(p_297612_, p_301288_, p_298901_, Suppliers.memoize(() -> p_297612_.get(p_301288_.value())));
        }

        private static <T> MapCodec<PlayerPredicate.StatMatcher<T>> createTypedCodec(StatType<T> p_297243_) {
            return RecordCodecBuilder.mapCodec(
                p_325241_ -> p_325241_.group(
                            p_297243_.getRegistry()
                                .holderByNameCodec()
                                .fieldOf("stat")
                                .forGetter(PlayerPredicate.StatMatcher::value),
                            MinMaxBounds.Ints.CODEC
                                .optionalFieldOf("value", MinMaxBounds.Ints.ANY)
                                .forGetter(PlayerPredicate.StatMatcher::range)
                        )
                        .apply(p_325241_, (p_301267_, p_297932_) -> new PlayerPredicate.StatMatcher<>(p_297243_, p_301267_, p_297932_))
            );
        }

        public boolean matches(StatsCounter p_300296_) {
            return this.range.matches(p_300296_.getValue(this.stat.get()));
        }
    }
}
