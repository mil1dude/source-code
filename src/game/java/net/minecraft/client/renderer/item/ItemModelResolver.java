package net.minecraft.client.renderer.item;

import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemModelResolver {
    private final Function<ResourceLocation, ItemModel> modelGetter;
    private final Function<ResourceLocation, ClientItem.Properties> clientProperties;

    public ItemModelResolver(ModelManager p_377509_) {
        this.modelGetter = p_377509_::getItemModel;
        this.clientProperties = p_377509_::getItemProperties;
    }

    public void updateForLiving(ItemStackRenderState p_375741_, ItemStack p_375493_, ItemDisplayContext p_375922_, boolean p_377322_, LivingEntity p_376619_) {
        this.updateForTopItem(p_375741_, p_375493_, p_375922_, p_377322_, p_376619_.level(), p_376619_, p_376619_.getId() + p_375922_.ordinal());
    }

    public void updateForNonLiving(ItemStackRenderState p_375853_, ItemStack p_378808_, ItemDisplayContext p_378112_, Entity p_376596_) {
        this.updateForTopItem(p_375853_, p_378808_, p_378112_, false, p_376596_.level(), null, p_376596_.getId());
    }

    public void updateForTopItem(
        ItemStackRenderState p_376095_,
        ItemStack p_376083_,
        ItemDisplayContext p_378127_,
        boolean p_377828_,
        @Nullable Level p_378324_,
        @Nullable LivingEntity p_377728_,
        int p_377306_
    ) {
        p_376095_.clear();
        if (!p_376083_.isEmpty()) {
            p_376095_.displayContext = p_378127_;
            p_376095_.isLeftHand = p_377828_;
            this.appendItemLayers(p_376095_, p_376083_, p_378127_, p_378324_, p_377728_, p_377306_);
        }
    }

    private static void fixupSkullProfile(ItemStack p_378393_) {
        if (p_378393_.getItem() instanceof BlockItem blockitem && blockitem.getBlock() instanceof AbstractSkullBlock) {
            ResolvableProfile resolvableprofile = p_378393_.get(DataComponents.PROFILE);
            if (resolvableprofile != null && !resolvableprofile.isResolved()) {
                p_378393_.remove(DataComponents.PROFILE);
                resolvableprofile.resolve().thenAcceptAsync(p_376419_ -> p_378393_.set(DataComponents.PROFILE, p_376419_), Minecraft.getInstance());
            }
        }
    }

    public void appendItemLayers(
        ItemStackRenderState p_376475_,
        ItemStack p_375988_,
        ItemDisplayContext p_377575_,
        @Nullable Level p_376809_,
        @Nullable LivingEntity p_376742_,
        int p_377982_
    ) {
        fixupSkullProfile(p_375988_);
        ResourceLocation resourcelocation = p_375988_.get(DataComponents.ITEM_MODEL);
        if (resourcelocation != null) {
            this.modelGetter
                .apply(resourcelocation)
                .update(p_376475_, p_375988_, this, p_377575_, p_376809_ instanceof ClientLevel clientlevel ? clientlevel : null, p_376742_, p_377982_);
        }
    }

    public boolean shouldPlaySwapAnimation(ItemStack p_375787_) {
        ResourceLocation resourcelocation = p_375787_.get(DataComponents.ITEM_MODEL);
        return resourcelocation == null ? true : this.clientProperties.apply(resourcelocation).handAnimationOnSwap();
    }
}