package net.minecraft.world.item.enchantment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

public class ItemEnchantments implements TooltipProvider {
    public static final ItemEnchantments EMPTY = new ItemEnchantments(new Object2IntOpenHashMap<>(), true);
    private static final Codec<Integer> LEVEL_CODEC = Codec.intRange(1, 255);
    private static final Codec<Object2IntOpenHashMap<Holder<Enchantment>>> LEVELS_CODEC = Codec.unboundedMap(Enchantment.CODEC, LEVEL_CODEC)
        .xmap(Object2IntOpenHashMap::new, Function.identity());
    private static final Codec<ItemEnchantments> FULL_CODEC = RecordCodecBuilder.create(
        p_330315_ -> p_330315_.group(
                    LEVELS_CODEC.fieldOf("levels").forGetter(p_334450_ -> p_334450_.enchantments),
                    Codec.BOOL.optionalFieldOf("show_in_tooltip", Boolean.valueOf(true)).forGetter(p_330292_ -> p_330292_.showInTooltip)
                )
                .apply(p_330315_, ItemEnchantments::new)
    );
    public static final Codec<ItemEnchantments> CODEC = Codec.withAlternative(FULL_CODEC, LEVELS_CODEC, p_330983_ -> new ItemEnchantments(p_330983_, true));
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemEnchantments> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.map(Object2IntOpenHashMap::new, Enchantment.STREAM_CODEC, ByteBufCodecs.VAR_INT),
        p_334569_ -> p_334569_.enchantments,
        ByteBufCodecs.BOOL,
        p_328412_ -> p_328412_.showInTooltip,
        ItemEnchantments::new
    );
    final Object2IntOpenHashMap<Holder<Enchantment>> enchantments;
    final boolean showInTooltip;

    ItemEnchantments(Object2IntOpenHashMap<Holder<Enchantment>> p_329796_, boolean p_331323_) {
        this.enchantments = p_329796_;
        this.showInTooltip = p_331323_;

        for (Entry<Holder<Enchantment>> entry : p_329796_.object2IntEntrySet()) {
            int i = entry.getIntValue();
            if (i < 0 || i > 255) {
                throw new IllegalArgumentException("Enchantment " + entry.getKey() + " has invalid level " + i);
            }
        }
    }

    public int getLevel(Holder<Enchantment> p_343517_) {
        return this.enchantments.getInt(p_343517_);
    }

    @Override
    public void addToTooltip(Item.TooltipContext p_332503_, Consumer<Component> p_333731_, TooltipFlag p_332196_) {
        if (this.showInTooltip) {
            HolderLookup.Provider holderlookup$provider = p_332503_.registries();
            HolderSet<Enchantment> holderset = getTagOrEmpty(holderlookup$provider, Registries.ENCHANTMENT, EnchantmentTags.TOOLTIP_ORDER);

            for (Holder<Enchantment> holder : holderset) {
                int i = this.enchantments.getInt(holder);
                if (i > 0) {
                    p_333731_.accept(Enchantment.getFullname(holder, i));
                }
            }

            for (Entry<Holder<Enchantment>> entry : this.enchantments.object2IntEntrySet()) {
                Holder<Enchantment> holder1 = entry.getKey();
                if (!holderset.contains(holder1)) {
                    p_333731_.accept(Enchantment.getFullname(entry.getKey(), entry.getIntValue()));
                }
            }
        }
    }

    private static <T> HolderSet<T> getTagOrEmpty(@Nullable HolderLookup.Provider p_327799_, ResourceKey<Registry<T>> p_330565_, TagKey<T> p_327764_) {
        if (p_327799_ != null) {
            Optional<HolderSet.Named<T>> optional = p_327799_.lookupOrThrow(p_330565_).get(p_327764_);
            if (optional.isPresent()) {
                return optional.get();
            }
        }

        return HolderSet.direct();
    }

    public ItemEnchantments withTooltip(boolean p_333031_) {
        return new ItemEnchantments(this.enchantments, p_333031_);
    }

    public Set<Holder<Enchantment>> keySet() {
        return Collections.unmodifiableSet(this.enchantments.keySet());
    }

    public Set<Entry<Holder<Enchantment>>> entrySet() {
        return Collections.unmodifiableSet(this.enchantments.object2IntEntrySet());
    }

    public int size() {
        return this.enchantments.size();
    }

    public boolean isEmpty() {
        return this.enchantments.isEmpty();
    }

    @Override
    public boolean equals(Object p_328229_) {
        if (this == p_328229_) {
            return true;
        } else {
            return !(p_328229_ instanceof ItemEnchantments itemenchantments)
                ? false
                : this.showInTooltip == itemenchantments.showInTooltip && this.enchantments.equals(itemenchantments.enchantments);
        }
    }

    @Override
    public int hashCode() {
        int i = this.enchantments.hashCode();
        return 31 * i + (this.showInTooltip ? 1 : 0);
    }

    @Override
    public String toString() {
        return "ItemEnchantments{enchantments=" + this.enchantments + ", showInTooltip=" + this.showInTooltip + "}";
    }

    public static class Mutable {
        private final Object2IntOpenHashMap<Holder<Enchantment>> enchantments = new Object2IntOpenHashMap<>();
        private final boolean showInTooltip;

        public Mutable(ItemEnchantments p_328128_) {
            this.enchantments.putAll(p_328128_.enchantments);
            this.showInTooltip = p_328128_.showInTooltip;
        }

        public void set(Holder<Enchantment> p_343732_, int p_330613_) {
            if (p_330613_ <= 0) {
                this.enchantments.removeInt(p_343732_);
            } else {
                this.enchantments.put(p_343732_, Math.min(p_330613_, 255));
            }
        }

        public void upgrade(Holder<Enchantment> p_345245_, int p_332549_) {
            if (p_332549_ > 0) {
                this.enchantments.merge(p_345245_, Math.min(p_332549_, 255), Integer::max);
            }
        }

        public void removeIf(Predicate<Holder<Enchantment>> p_330896_) {
            this.enchantments.keySet().removeIf(p_330896_);
        }

        public int getLevel(Holder<Enchantment> p_342208_) {
            return this.enchantments.getOrDefault(p_342208_, 0);
        }

        public Set<Holder<Enchantment>> keySet() {
            return this.enchantments.keySet();
        }

        public ItemEnchantments toImmutable() {
            return new ItemEnchantments(this.enchantments, this.showInTooltip);
        }
    }
}