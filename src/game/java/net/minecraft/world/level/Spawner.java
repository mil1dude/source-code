package net.minecraft.world.level;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public interface Spawner {
    void setEntityId(EntityType<?> p_312533_, RandomSource p_311601_);

    static void appendHoverText(ItemStack p_311346_, List<Component> p_309883_, String p_310819_) {
        Component component = getSpawnEntityDisplayName(p_311346_, p_310819_);
        if (component != null) {
            p_309883_.add(component);
        } else {
            p_309883_.add(CommonComponents.EMPTY);
            p_309883_.add(Component.translatable("block.minecraft.spawner.desc1").withStyle(ChatFormatting.GRAY));
            p_309883_.add(CommonComponents.space().append(Component.translatable("block.minecraft.spawner.desc2").withStyle(ChatFormatting.BLUE)));
        }
    }

    @Nullable
    static Component getSpawnEntityDisplayName(ItemStack p_312162_, String p_309907_) {
        CompoundTag compoundtag = p_312162_.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY).getUnsafe();
        ResourceLocation resourcelocation = getEntityKey(compoundtag, p_309907_);
        return resourcelocation != null
            ? BuiltInRegistries.ENTITY_TYPE
                .getOptional(resourcelocation)
                .map(p_311493_ -> Component.translatable(p_311493_.getDescriptionId()).withStyle(ChatFormatting.GRAY))
                .orElse(null)
            : null;
    }

    @Nullable
    private static ResourceLocation getEntityKey(CompoundTag p_313110_, String p_312914_) {
        if (p_313110_.contains(p_312914_, 10)) {
            String s = p_313110_.getCompound(p_312914_).getCompound("entity").getString("id");
            return ResourceLocation.tryParse(s);
        } else {
            return null;
        }
    }
}