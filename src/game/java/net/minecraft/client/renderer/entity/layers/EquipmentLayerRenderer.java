package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EquipmentLayerRenderer {
    private static final int NO_LAYER_COLOR = 0;
    private final EquipmentAssetManager equipmentAssets;
    private final Function<EquipmentLayerRenderer.LayerTextureKey, ResourceLocation> layerTextureLookup;
    private final Function<EquipmentLayerRenderer.TrimSpriteKey, TextureAtlasSprite> trimSpriteLookup;

    public EquipmentLayerRenderer(EquipmentAssetManager p_375597_, TextureAtlas p_363154_) {
        this.equipmentAssets = p_375597_;
        this.layerTextureLookup = Util.memoize(p_374656_ -> p_374656_.layer.getTextureLocation(p_374656_.layerType));
        this.trimSpriteLookup = Util.memoize(p_374658_ -> p_363154_.getSprite(p_374658_.textureId()));
    }

    public void renderLayers(
        EquipmentClientInfo.LayerType p_376060_,
        ResourceKey<EquipmentAsset> p_375841_,
        Model p_366052_,
        ItemStack p_368999_,
        PoseStack p_366797_,
        MultiBufferSource p_367071_,
        int p_365571_
    ) {
        this.renderLayers(p_376060_, p_375841_, p_366052_, p_368999_, p_366797_, p_367071_, p_365571_, null);
    }

    public void renderLayers(
        EquipmentClientInfo.LayerType p_377792_,
        ResourceKey<EquipmentAsset> p_377288_,
        Model p_366813_,
        ItemStack p_363462_,
        PoseStack p_361892_,
        MultiBufferSource p_369133_,
        int p_367241_,
        @Nullable ResourceLocation p_363056_
    ) {
        List<EquipmentClientInfo.Layer> list = this.equipmentAssets.get(p_377288_).getLayers(p_377792_);
        if (!list.isEmpty()) {
            int i = p_363462_.is(ItemTags.DYEABLE) ? DyedItemColor.getOrDefault(p_363462_, 0) : 0;
            boolean flag = p_363462_.hasFoil();

            for (EquipmentClientInfo.Layer equipmentclientinfo$layer : list) {
                int j = getColorForLayer(equipmentclientinfo$layer, i);
                if (j != 0) {
                    ResourceLocation resourcelocation = equipmentclientinfo$layer.usePlayerTexture() && p_363056_ != null
                        ? p_363056_
                        : this.layerTextureLookup.apply(new EquipmentLayerRenderer.LayerTextureKey(p_377792_, equipmentclientinfo$layer));
                    VertexConsumer vertexconsumer = ItemRenderer.getArmorFoilBuffer(p_369133_, RenderType.armorCutoutNoCull(resourcelocation), flag);
                    p_366813_.renderToBuffer(p_361892_, vertexconsumer, p_367241_, OverlayTexture.NO_OVERLAY, j);
                    flag = false;
                }
            }

            ArmorTrim armortrim = p_363462_.get(DataComponents.TRIM);
            if (armortrim != null) {
                TextureAtlasSprite textureatlassprite = this.trimSpriteLookup.apply(new EquipmentLayerRenderer.TrimSpriteKey(armortrim, p_377792_, p_377288_));
                VertexConsumer vertexconsumer1 = textureatlassprite.wrap(
                    p_369133_.getBuffer(Sheets.armorTrimsSheet(armortrim.pattern().value().decal()))
                );
                p_366813_.renderToBuffer(p_361892_, vertexconsumer1, p_367241_, OverlayTexture.NO_OVERLAY);
            }
        }
    }

    private static int getColorForLayer(EquipmentClientInfo.Layer p_376428_, int p_365160_) {
        Optional<EquipmentClientInfo.Dyeable> optional = p_376428_.dyeable();
        if (optional.isPresent()) {
            int i = optional.get().colorWhenUndyed().map(ARGB::opaque).orElse(0);
            return p_365160_ != 0 ? p_365160_ : i;
        } else {
            return -1;
        }
    }

    @OnlyIn(Dist.CLIENT)
    static record LayerTextureKey(EquipmentClientInfo.LayerType layerType, EquipmentClientInfo.Layer layer) {
    }

    @OnlyIn(Dist.CLIENT)
    static record TrimSpriteKey(ArmorTrim trim, EquipmentClientInfo.LayerType layerType, ResourceKey<EquipmentAsset> equipmentAssetId) {
        private static String getColorPaletteSuffix(Holder<TrimMaterial> p_376389_, ResourceKey<EquipmentAsset> p_378627_) {
            String s = p_376389_.value().overrideArmorAssets().get(p_378627_);
            return s != null ? s : p_376389_.value().assetName();
        }

        public ResourceLocation textureId() {
            ResourceLocation resourcelocation = this.trim.pattern().value().assetId();
            String s = getColorPaletteSuffix(this.trim.material(), this.equipmentAssetId);
            return resourcelocation.withPath(p_377681_ -> "trims/entity/" + this.layerType.getSerializedName() + "/" + p_377681_ + "_" + s);
        }
    }
}