package net.minecraft.client.resources.model;

import javax.annotation.Nullable;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface UnbakedModel extends ResolvableModel {
    boolean DEFAULT_AMBIENT_OCCLUSION = true;
    UnbakedModel.GuiLight DEFAULT_GUI_LIGHT = UnbakedModel.GuiLight.SIDE;

    BakedModel bake(TextureSlots p_376037_, ModelBaker p_250133_, ModelState p_119536_, boolean p_378145_, boolean p_377763_, ItemTransforms p_375499_);

    @Nullable
    default Boolean getAmbientOcclusion() {
        return null;
    }

    @Nullable
    default UnbakedModel.GuiLight getGuiLight() {
        return null;
    }

    @Nullable
    default ItemTransforms getTransforms() {
        return null;
    }

    default TextureSlots.Data getTextureSlots() {
        return TextureSlots.Data.EMPTY;
    }

    @Nullable
    default UnbakedModel getParent() {
        return null;
    }

    static BakedModel bakeWithTopModelValues(UnbakedModel p_377580_, ModelBaker p_375760_, ModelState p_377199_) {
        TextureSlots textureslots = getTopTextureSlots(p_377580_, p_375760_.rootName());
        boolean flag = getTopAmbientOcclusion(p_377580_);
        boolean flag1 = getTopGuiLight(p_377580_).lightLikeBlock();
        ItemTransforms itemtransforms = getTopTransforms(p_377580_);
        return p_377580_.bake(textureslots, p_375760_, p_377199_, flag, flag1, itemtransforms);
    }

    static TextureSlots getTopTextureSlots(UnbakedModel p_375427_, ModelDebugName p_378400_) {
        TextureSlots.Resolver textureslots$resolver = new TextureSlots.Resolver();

        while (p_375427_ != null) {
            textureslots$resolver.addLast(p_375427_.getTextureSlots());
            p_375427_ = p_375427_.getParent();
        }

        return textureslots$resolver.resolve(p_378400_);
    }

    static boolean getTopAmbientOcclusion(UnbakedModel p_377158_) {
        while (p_377158_ != null) {
            Boolean obool = p_377158_.getAmbientOcclusion();
            if (obool != null) {
                return obool;
            }

            p_377158_ = p_377158_.getParent();
        }

        return true;
    }

    static UnbakedModel.GuiLight getTopGuiLight(UnbakedModel p_375581_) {
        while (p_375581_ != null) {
            UnbakedModel.GuiLight unbakedmodel$guilight = p_375581_.getGuiLight();
            if (unbakedmodel$guilight != null) {
                return unbakedmodel$guilight;
            }

            p_375581_ = p_375581_.getParent();
        }

        return DEFAULT_GUI_LIGHT;
    }

    static ItemTransform getTopTransform(UnbakedModel p_377137_, ItemDisplayContext p_376495_) {
        while (p_377137_ != null) {
            ItemTransforms itemtransforms = p_377137_.getTransforms();
            if (itemtransforms != null) {
                ItemTransform itemtransform = itemtransforms.getTransform(p_376495_);
                if (itemtransform != ItemTransform.NO_TRANSFORM) {
                    return itemtransform;
                }
            }

            p_377137_ = p_377137_.getParent();
        }

        return ItemTransform.NO_TRANSFORM;
    }

    static ItemTransforms getTopTransforms(UnbakedModel p_377896_) {
        ItemTransform itemtransform = getTopTransform(p_377896_, ItemDisplayContext.THIRD_PERSON_LEFT_HAND);
        ItemTransform itemtransform1 = getTopTransform(p_377896_, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND);
        ItemTransform itemtransform2 = getTopTransform(p_377896_, ItemDisplayContext.FIRST_PERSON_LEFT_HAND);
        ItemTransform itemtransform3 = getTopTransform(p_377896_, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND);
        ItemTransform itemtransform4 = getTopTransform(p_377896_, ItemDisplayContext.HEAD);
        ItemTransform itemtransform5 = getTopTransform(p_377896_, ItemDisplayContext.GUI);
        ItemTransform itemtransform6 = getTopTransform(p_377896_, ItemDisplayContext.GROUND);
        ItemTransform itemtransform7 = getTopTransform(p_377896_, ItemDisplayContext.FIXED);
        return new ItemTransforms(itemtransform, itemtransform1, itemtransform2, itemtransform3, itemtransform4, itemtransform5, itemtransform6, itemtransform7);
    }

    @OnlyIn(Dist.CLIENT)
    public static enum GuiLight {
        FRONT("front"),
        SIDE("side");

        private final String name;

        private GuiLight(final String p_377886_) {
            this.name = p_377886_;
        }

        public static UnbakedModel.GuiLight getByName(String p_378162_) {
            for (UnbakedModel.GuiLight unbakedmodel$guilight : values()) {
                if (unbakedmodel$guilight.name.equals(p_378162_)) {
                    return unbakedmodel$guilight;
                }
            }

            throw new IllegalArgumentException("Invalid gui light: " + p_378162_);
        }

        public boolean lightLikeBlock() {
            return this == SIDE;
        }
    }
}