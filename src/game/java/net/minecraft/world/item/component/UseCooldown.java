package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record UseCooldown(float seconds, Optional<ResourceLocation> cooldownGroup) {
    public static final Codec<UseCooldown> CODEC = RecordCodecBuilder.create(
        p_365460_ -> p_365460_.group(
                    ExtraCodecs.POSITIVE_FLOAT.fieldOf("seconds").forGetter(UseCooldown::seconds),
                    ResourceLocation.CODEC.optionalFieldOf("cooldown_group").forGetter(UseCooldown::cooldownGroup)
                )
                .apply(p_365460_, UseCooldown::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, UseCooldown> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.FLOAT,
        UseCooldown::seconds,
        ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs::optional),
        UseCooldown::cooldownGroup,
        UseCooldown::new
    );

    public UseCooldown(float p_362364_) {
        this(p_362364_, Optional.empty());
    }

    public int ticks() {
        return (int)(this.seconds * 20.0F);
    }

    public void apply(ItemStack p_360969_, LivingEntity p_366259_) {
        if (p_366259_ instanceof Player player) {
            player.getCooldowns().addCooldown(p_360969_, this.ticks());
        }
    }
}