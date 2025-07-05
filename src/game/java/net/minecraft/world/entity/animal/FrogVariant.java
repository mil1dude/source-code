package net.minecraft.world.entity.animal;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public record FrogVariant(ResourceLocation texture) {
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<FrogVariant>> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.FROG_VARIANT);
    public static final ResourceKey<FrogVariant> TEMPERATE = createKey("temperate");
    public static final ResourceKey<FrogVariant> WARM = createKey("warm");
    public static final ResourceKey<FrogVariant> COLD = createKey("cold");

    private static ResourceKey<FrogVariant> createKey(String p_332326_) {
        return ResourceKey.create(Registries.FROG_VARIANT, ResourceLocation.withDefaultNamespace(p_332326_));
    }

    public static FrogVariant bootstrap(Registry<FrogVariant> p_331705_) {
        register(p_331705_, TEMPERATE, "textures/entity/frog/temperate_frog.png");
        register(p_331705_, WARM, "textures/entity/frog/warm_frog.png");
        return register(p_331705_, COLD, "textures/entity/frog/cold_frog.png");
    }

    private static FrogVariant register(Registry<FrogVariant> p_335641_, ResourceKey<FrogVariant> p_331676_, String p_218194_) {
        return Registry.register(p_335641_, p_331676_, new FrogVariant(ResourceLocation.withDefaultNamespace(p_218194_)));
    }
}