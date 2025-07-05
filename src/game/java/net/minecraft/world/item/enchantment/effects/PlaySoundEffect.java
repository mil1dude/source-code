package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.phys.Vec3;

public record PlaySoundEffect(Holder<SoundEvent> soundEvent, FloatProvider volume, FloatProvider pitch) implements EnchantmentEntityEffect {
    public static final MapCodec<PlaySoundEffect> CODEC = RecordCodecBuilder.mapCodec(
        p_342111_ -> p_342111_.group(
                    SoundEvent.CODEC.fieldOf("sound").forGetter(PlaySoundEffect::soundEvent),
                    FloatProvider.codec(1.0E-5F, 10.0F).fieldOf("volume").forGetter(PlaySoundEffect::volume),
                    FloatProvider.codec(1.0E-5F, 2.0F).fieldOf("pitch").forGetter(PlaySoundEffect::pitch)
                )
                .apply(p_342111_, PlaySoundEffect::new)
    );

    @Override
    public void apply(ServerLevel p_342634_, int p_344633_, EnchantedItemInUse p_344196_, Entity p_344269_, Vec3 p_343993_) {
        RandomSource randomsource = p_344269_.getRandom();
        if (!p_344269_.isSilent()) {
            p_342634_.playSound(
                null,
                p_343993_.x(),
                p_343993_.y(),
                p_343993_.z(),
                this.soundEvent,
                p_344269_.getSoundSource(),
                this.volume.sample(randomsource),
                this.pitch.sample(randomsource)
            );
        }
    }

    @Override
    public MapCodec<PlaySoundEffect> codec() {
        return CODEC;
    }
}