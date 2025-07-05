package net.minecraft.world.scores;

import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.NumberFormatTypes;

public class Score implements ReadOnlyScoreInfo {
    private static final String TAG_SCORE = "Score";
    private static final String TAG_LOCKED = "Locked";
    private static final String TAG_DISPLAY = "display";
    private static final String TAG_FORMAT = "format";
    private int value;
    private boolean locked = true;
    @Nullable
    private Component display;
    @Nullable
    private NumberFormat numberFormat;

    @Override
    public int value() {
        return this.value;
    }

    public void value(int p_313056_) {
        this.value = p_313056_;
    }

    @Override
    public boolean isLocked() {
        return this.locked;
    }

    public void setLocked(boolean p_83399_) {
        this.locked = p_83399_;
    }

    @Nullable
    public Component display() {
        return this.display;
    }

    public void display(@Nullable Component p_312952_) {
        this.display = p_312952_;
    }

    @Nullable
    @Override
    public NumberFormat numberFormat() {
        return this.numberFormat;
    }

    public void numberFormat(@Nullable NumberFormat p_310093_) {
        this.numberFormat = p_310093_;
    }

    public CompoundTag write(HolderLookup.Provider p_334001_) {
        CompoundTag compoundtag = new CompoundTag();
        compoundtag.putInt("Score", this.value);
        compoundtag.putBoolean("Locked", this.locked);
        if (this.display != null) {
            compoundtag.putString("display", Component.Serializer.toJson(this.display, p_334001_));
        }

        if (this.numberFormat != null) {
            NumberFormatTypes.CODEC
                .encodeStart(p_334001_.createSerializationContext(NbtOps.INSTANCE), this.numberFormat)
                .ifSuccess(p_309357_ -> compoundtag.put("format", p_309357_));
        }

        return compoundtag;
    }

    public static Score read(CompoundTag p_313199_, HolderLookup.Provider p_329343_) {
        Score score = new Score();
        score.value = p_313199_.getInt("Score");
        score.locked = p_313199_.getBoolean("Locked");
        if (p_313199_.contains("display", 8)) {
            score.display = Component.Serializer.fromJson(p_313199_.getString("display"), p_329343_);
        }

        if (p_313199_.contains("format", 10)) {
            NumberFormatTypes.CODEC
                .parse(p_329343_.createSerializationContext(NbtOps.INSTANCE), p_313199_.get("format"))
                .ifSuccess(p_309359_ -> score.numberFormat = p_309359_);
        }

        return score;
    }
}