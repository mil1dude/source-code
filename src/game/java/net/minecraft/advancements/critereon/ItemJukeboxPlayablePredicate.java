package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.item.JukeboxSong;

public record ItemJukeboxPlayablePredicate(Optional<HolderSet<JukeboxSong>> song) implements SingleComponentItemPredicate<JukeboxPlayable> {
    public static final Codec<ItemJukeboxPlayablePredicate> CODEC = RecordCodecBuilder.create(
        p_344203_ -> p_344203_.group(RegistryCodecs.homogeneousList(Registries.JUKEBOX_SONG).optionalFieldOf("song").forGetter(ItemJukeboxPlayablePredicate::song))
                .apply(p_344203_, ItemJukeboxPlayablePredicate::new)
    );

    @Override
    public DataComponentType<JukeboxPlayable> componentType() {
        return DataComponents.JUKEBOX_PLAYABLE;
    }

    public boolean matches(ItemStack p_345238_, JukeboxPlayable p_343683_) {
        if (!this.song.isPresent()) {
            return true;
        } else {
            boolean flag = false;

            for (Holder<JukeboxSong> holder : this.song.get()) {
                Optional<ResourceKey<JukeboxSong>> optional = holder.unwrapKey();
                if (!optional.isEmpty() && optional.get() == p_343683_.song().key()) {
                    flag = true;
                    break;
                }
            }

            return flag;
        }
    }

    public static ItemJukeboxPlayablePredicate any() {
        return new ItemJukeboxPlayablePredicate(Optional.empty());
    }
}