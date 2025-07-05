package net.minecraft.client.renderer.item.properties.select;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.SelectItemModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface SelectItemModelProperty<T> {
    @Nullable
    T get(ItemStack p_378017_, @Nullable ClientLevel p_375849_, @Nullable LivingEntity p_377186_, int p_375524_, ItemDisplayContext p_378106_);

    SelectItemModelProperty.Type<? extends SelectItemModelProperty<T>, T> type();

    @OnlyIn(Dist.CLIENT)
    public static record Type<P extends SelectItemModelProperty<T>, T>(MapCodec<SelectItemModel.UnbakedSwitch<P, T>> switchCodec) {
        public static <P extends SelectItemModelProperty<T>, T> SelectItemModelProperty.Type<P, T> create(MapCodec<P> p_376943_, Codec<T> p_376938_) {
            Codec<List<SelectItemModel.SwitchCase<T>>> codec = SelectItemModel.SwitchCase.codec(p_376938_)
                .listOf()
                .validate(
                    p_375867_ -> {
                        if (p_375867_.isEmpty()) {
                            return DataResult.error(() -> "Empty case list");
                        } else {
                            Multiset<T> multiset = HashMultiset.create();

                            for (SelectItemModel.SwitchCase<T> switchcase : p_375867_) {
                                multiset.addAll(switchcase.values());
                            }

                            return multiset.size() != multiset.entrySet().size()
                                ? DataResult.error(
                                    () -> "Duplicate case conditions: "
                                            + multiset.entrySet()
                                                .stream()
                                                .filter(p_378495_ -> p_378495_.getCount() > 1)
                                                .map(p_378161_ -> p_378161_.getElement().toString())
                                                .collect(Collectors.joining(", "))
                                )
                                : DataResult.success(p_375867_);
                        }
                    }
                );
            MapCodec<SelectItemModel.UnbakedSwitch<P, T>> mapcodec = RecordCodecBuilder.mapCodec(
                p_378451_ -> p_378451_.group(
                            p_376943_.forGetter(SelectItemModel.UnbakedSwitch::property),
                            codec.fieldOf("cases").forGetter(SelectItemModel.UnbakedSwitch::cases)
                        )
                        .apply(p_378451_, SelectItemModel.UnbakedSwitch::new)
            );
            return new SelectItemModelProperty.Type<>(mapcodec);
        }
    }
}