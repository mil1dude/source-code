package net.minecraft.world.item;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class InstrumentItem extends Item {
    private final TagKey<Instrument> instruments;

    public InstrumentItem(TagKey<Instrument> p_220100_, Item.Properties p_220099_) {
        super(p_220099_);
        this.instruments = p_220100_;
    }

    @Override
    public void appendHoverText(ItemStack p_220115_, Item.TooltipContext p_328884_, List<Component> p_220117_, TooltipFlag p_220118_) {
        super.appendHoverText(p_220115_, p_328884_, p_220117_, p_220118_);
        HolderLookup.Provider holderlookup$provider = p_328884_.registries();
        if (holderlookup$provider != null) {
            Optional<Holder<Instrument>> optional = this.getInstrument(p_220115_, holderlookup$provider);
            if (optional.isPresent()) {
                MutableComponent mutablecomponent = optional.get().value().description().copy();
                ComponentUtils.mergeStyles(mutablecomponent, Style.EMPTY.withColor(ChatFormatting.GRAY));
                p_220117_.add(mutablecomponent);
            }
        }
    }

    public static ItemStack create(Item p_220108_, Holder<Instrument> p_220109_) {
        ItemStack itemstack = new ItemStack(p_220108_);
        itemstack.set(DataComponents.INSTRUMENT, p_220109_);
        return itemstack;
    }

    @Override
    public InteractionResult use(Level p_220123_, Player p_220124_, InteractionHand p_220125_) {
        ItemStack itemstack = p_220124_.getItemInHand(p_220125_);
        Optional<? extends Holder<Instrument>> optional = this.getInstrument(itemstack, p_220124_.registryAccess());
        if (optional.isPresent()) {
            Instrument instrument = optional.get().value();
            p_220124_.startUsingItem(p_220125_);
            play(p_220123_, p_220124_, instrument);
            p_220124_.getCooldowns().addCooldown(itemstack, Mth.floor(instrument.useDuration() * 20.0F));
            p_220124_.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResult.CONSUME;
        } else {
            return InteractionResult.FAIL;
        }
    }

    @Override
    public int getUseDuration(ItemStack p_220131_, LivingEntity p_345360_) {
        Optional<Holder<Instrument>> optional = this.getInstrument(p_220131_, p_345360_.registryAccess());
        return optional.<Integer>map(p_359409_ -> Mth.floor(p_359409_.value().useDuration() * 20.0F)).orElse(0);
    }

    private Optional<Holder<Instrument>> getInstrument(ItemStack p_220135_, HolderLookup.Provider p_365790_) {
        Holder<Instrument> holder = p_220135_.get(DataComponents.INSTRUMENT);
        if (holder != null) {
            return Optional.of(holder);
        } else {
            Optional<HolderSet.Named<Instrument>> optional = p_365790_.lookupOrThrow(Registries.INSTRUMENT).get(this.instruments);
            if (optional.isPresent()) {
                Iterator<Holder<Instrument>> iterator = optional.get().iterator();
                if (iterator.hasNext()) {
                    return Optional.of(iterator.next());
                }
            }

            return Optional.empty();
        }
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack p_220133_) {
        return ItemUseAnimation.TOOT_HORN;
    }

    private static void play(Level p_220127_, Player p_220128_, Instrument p_220129_) {
        SoundEvent soundevent = p_220129_.soundEvent().value();
        float f = p_220129_.range() / 16.0F;
        p_220127_.playSound(p_220128_, p_220128_, soundevent, SoundSource.RECORDS, f, 1.0F);
        p_220127_.gameEvent(GameEvent.INSTRUMENT_PLAY, p_220128_.position(), GameEvent.Context.of(p_220128_));
    }
}