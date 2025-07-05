package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;

public record ItemTrimPredicate(Optional<HolderSet<TrimMaterial>> material, Optional<HolderSet<TrimPattern>> pattern)
    implements SingleComponentItemPredicate<ArmorTrim> {
    public static final Codec<ItemTrimPredicate> CODEC = RecordCodecBuilder.create(
        p_334329_ -> p_334329_.group(
                    RegistryCodecs.homogeneousList(Registries.TRIM_MATERIAL).optionalFieldOf("material").forGetter(ItemTrimPredicate::material),
                    RegistryCodecs.homogeneousList(Registries.TRIM_PATTERN).optionalFieldOf("pattern").forGetter(ItemTrimPredicate::pattern)
                )
                .apply(p_334329_, ItemTrimPredicate::new)
    );

    @Override
    public DataComponentType<ArmorTrim> componentType() {
        return DataComponents.TRIM;
    }

    public boolean matches(ItemStack p_336368_, ArmorTrim p_370011_) {
        return this.material.isPresent() && !this.material.get().contains(p_370011_.material())
            ? false
            : !this.pattern.isPresent() || this.pattern.get().contains(p_370011_.pattern());
    }
}