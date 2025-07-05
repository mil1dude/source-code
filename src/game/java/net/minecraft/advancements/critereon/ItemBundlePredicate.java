package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;

public record ItemBundlePredicate(Optional<CollectionPredicate<ItemStack, ItemPredicate>> items) implements SingleComponentItemPredicate<BundleContents> {
    public static final Codec<ItemBundlePredicate> CODEC = RecordCodecBuilder.create(
        p_333649_ -> p_333649_.group(
                    CollectionPredicate.<ItemStack, ItemPredicate>codec(ItemPredicate.CODEC)
                        .optionalFieldOf("items")
                        .forGetter(ItemBundlePredicate::items)
                )
                .apply(p_333649_, ItemBundlePredicate::new)
    );

    @Override
    public DataComponentType<BundleContents> componentType() {
        return DataComponents.BUNDLE_CONTENTS;
    }

    public boolean matches(ItemStack p_327929_, BundleContents p_336290_) {
        return !this.items.isPresent() || this.items.get().test(p_336290_.items());
    }
}