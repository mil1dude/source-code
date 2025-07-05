package net.minecraft.world;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public record LockCode(ItemPredicate predicate) {
    public static final LockCode NO_LOCK = new LockCode(ItemPredicate.Builder.item().build());
    public static final Codec<LockCode> CODEC = ItemPredicate.CODEC.xmap(LockCode::new, LockCode::predicate);
    public static final String TAG_LOCK = "lock";

    public boolean unlocksWith(ItemStack p_19108_) {
        return this.predicate.test(p_19108_);
    }

    public void addToTag(CompoundTag p_19110_, HolderLookup.Provider p_367767_) {
        if (this != NO_LOCK) {
            DataResult<Tag> dataresult = CODEC.encode(this, p_367767_.createSerializationContext(NbtOps.INSTANCE), new CompoundTag());
            dataresult.result().ifPresent(p_362847_ -> p_19110_.put("lock", p_362847_));
        }
    }

    public static LockCode fromTag(CompoundTag p_19112_, HolderLookup.Provider p_361968_) {
        if (p_19112_.contains("lock", 10)) {
            DataResult<Pair<LockCode, Tag>> dataresult = CODEC.decode(p_361968_.createSerializationContext(NbtOps.INSTANCE), p_19112_.get("lock"));
            if (dataresult.isSuccess()) {
                return dataresult.getOrThrow().getFirst();
            }
        }

        return NO_LOCK;
    }
}