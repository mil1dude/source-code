package net.minecraft.world.level.block;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.level.ItemLike;

public interface SuspiciousEffectHolder {
    SuspiciousStewEffects getSuspiciousEffects();

    static List<SuspiciousEffectHolder> getAllEffectHolders() {
        return BuiltInRegistries.ITEM.stream().map(SuspiciousEffectHolder::tryGet).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Nullable
    static SuspiciousEffectHolder tryGet(ItemLike p_259322_) {
        if (p_259322_.asItem() instanceof BlockItem blockitem) {
            Block block = blockitem.getBlock();
            if (block instanceof SuspiciousEffectHolder) {
                return (SuspiciousEffectHolder)block;
            }
        }

        Item $$2 = p_259322_.asItem();
        return $$2 instanceof SuspiciousEffectHolder ? (SuspiciousEffectHolder)$$2 : null;
    }
}