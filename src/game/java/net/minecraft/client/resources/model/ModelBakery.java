package net.minecraft.client.resources.model;

import com.mojang.logging.LogUtils;
import com.mojang.math.Transformation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.UnbakedBlockStateModel;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.MissingItemModel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ModelBakery {
    public static final Material FIRE_0 = new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("block/fire_0"));
    public static final Material FIRE_1 = new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("block/fire_1"));
    public static final Material LAVA_FLOW = new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("block/lava_flow"));
    public static final Material WATER_FLOW = new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("block/water_flow"));
    public static final Material WATER_OVERLAY = new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("block/water_overlay"));
    public static final Material BANNER_BASE = new Material(Sheets.BANNER_SHEET, ResourceLocation.withDefaultNamespace("entity/banner_base"));
    public static final Material SHIELD_BASE = new Material(Sheets.SHIELD_SHEET, ResourceLocation.withDefaultNamespace("entity/shield_base"));
    public static final Material NO_PATTERN_SHIELD = new Material(Sheets.SHIELD_SHEET, ResourceLocation.withDefaultNamespace("entity/shield_base_nopattern"));
    public static final int DESTROY_STAGE_COUNT = 10;
    public static final List<ResourceLocation> DESTROY_STAGES = IntStream.range(0, 10)
        .mapToObj(p_340955_ -> ResourceLocation.withDefaultNamespace("block/destroy_stage_" + p_340955_))
        .collect(Collectors.toList());
    public static final List<ResourceLocation> BREAKING_LOCATIONS = DESTROY_STAGES.stream()
        .map(p_340960_ -> p_340960_.withPath(p_340956_ -> "textures/" + p_340956_ + ".png"))
        .collect(Collectors.toList());
    public static final List<RenderType> DESTROY_TYPES = BREAKING_LOCATIONS.stream().map(RenderType::crumbling).collect(Collectors.toList());
    static final Logger LOGGER = LogUtils.getLogger();
    private final EntityModelSet entityModelSet;
    final Map<ModelBakery.BakedCacheKey, BakedModel> bakedCache = new HashMap<>();
    private final Map<ModelResourceLocation, UnbakedBlockStateModel> unbakedBlockStateModels;
    private final Map<ResourceLocation, ClientItem> clientInfos;
    final Map<ResourceLocation, UnbakedModel> unbakedPlainModels;
    final UnbakedModel missingModel;

    public ModelBakery(
        EntityModelSet p_376026_,
        Map<ModelResourceLocation, UnbakedBlockStateModel> p_251087_,
        Map<ResourceLocation, ClientItem> p_250416_,
        Map<ResourceLocation, UnbakedModel> p_375852_,
        UnbakedModel p_361482_
    ) {
        this.entityModelSet = p_376026_;
        this.unbakedBlockStateModels = p_251087_;
        this.clientInfos = p_250416_;
        this.unbakedPlainModels = p_375852_;
        this.missingModel = p_361482_;
    }

    public ModelBakery.BakingResult bakeModels(ModelBakery.TextureGetter p_343407_) {
        BakedModel bakedmodel = UnbakedModel.bakeWithTopModelValues(this.missingModel, new ModelBakery.ModelBakerImpl(p_343407_, () -> "missing"), BlockModelRotation.X0_Y0);
        Map<ModelResourceLocation, BakedModel> map = new HashMap<>(this.unbakedBlockStateModels.size());
        this.unbakedBlockStateModels.forEach((p_374710_, p_374711_) -> {
            try {
                BakedModel bakedmodel1 = p_374711_.bake(new ModelBakery.ModelBakerImpl(p_343407_, p_374710_::toString));
                map.put(p_374710_, bakedmodel1);
            } catch (Exception exception) {
                LOGGER.warn("Unable to bake model: '{}': {}", p_374710_, exception);
            }
        });
        ItemModel itemmodel = new MissingItemModel(bakedmodel);
        Map<ResourceLocation, ItemModel> map1 = new HashMap<>(this.clientInfos.size());
        Map<ResourceLocation, ClientItem.Properties> map2 = new HashMap<>(this.clientInfos.size());
        this.clientInfos.forEach((p_374705_, p_374706_) -> {
            ModelDebugName modeldebugname = () -> p_374705_ + "#inventory";
            ModelBakery.ModelBakerImpl modelbakery$modelbakerimpl = new ModelBakery.ModelBakerImpl(p_343407_, modeldebugname);
            ItemModel.BakingContext itemmodel$bakingcontext = new ItemModel.BakingContext(modelbakery$modelbakerimpl, this.entityModelSet, itemmodel);

            try {
                ItemModel itemmodel1 = p_374706_.model().bake(itemmodel$bakingcontext);
                map1.put(p_374705_, itemmodel1);
                if (!p_374706_.properties().equals(ClientItem.Properties.DEFAULT)) {
                    map2.put(p_374705_, p_374706_.properties());
                }
            } catch (Exception exception) {
                LOGGER.warn("Unable to bake item model: '{}'", p_374705_, exception);
            }
        });
        return new ModelBakery.BakingResult(bakedmodel, map, itemmodel, map1, map2);
    }

    @OnlyIn(Dist.CLIENT)
    static record BakedCacheKey(ResourceLocation id, Transformation transformation, boolean isUvLocked) {
    }

    @OnlyIn(Dist.CLIENT)
    public static record BakingResult(
        BakedModel missingModel,
        Map<ModelResourceLocation, BakedModel> blockStateModels,
        ItemModel missingItemModel,
        Map<ResourceLocation, ItemModel> itemStackModels,
        Map<ResourceLocation, ClientItem.Properties> itemProperties
    ) {
    }

    @OnlyIn(Dist.CLIENT)
    class ModelBakerImpl implements ModelBaker {
        private final ModelDebugName rootName;
        private final SpriteGetter modelTextureGetter;

        ModelBakerImpl(final ModelBakery.TextureGetter p_342310_, final ModelDebugName p_378194_) {
            this.modelTextureGetter = p_342310_.bind(p_378194_);
            this.rootName = p_378194_;
        }

        @Override
        public SpriteGetter sprites() {
            return this.modelTextureGetter;
        }

        private UnbakedModel getModel(ResourceLocation p_248568_) {
            UnbakedModel unbakedmodel = ModelBakery.this.unbakedPlainModels.get(p_248568_);
            if (unbakedmodel == null) {
                ModelBakery.LOGGER.warn("Requested a model that was not discovered previously: {}", p_248568_);
                return ModelBakery.this.missingModel;
            } else {
                return unbakedmodel;
            }
        }

        @Override
        public BakedModel bake(ResourceLocation p_252176_, ModelState p_249765_) {
            ModelBakery.BakedCacheKey modelbakery$bakedcachekey = new ModelBakery.BakedCacheKey(p_252176_, p_249765_.getRotation(), p_249765_.isUvLocked());
            BakedModel bakedmodel = ModelBakery.this.bakedCache.get(modelbakery$bakedcachekey);
            if (bakedmodel != null) {
                return bakedmodel;
            } else {
                UnbakedModel unbakedmodel = this.getModel(p_252176_);
                BakedModel bakedmodel1 = UnbakedModel.bakeWithTopModelValues(unbakedmodel, this, p_249765_);
                ModelBakery.this.bakedCache.put(modelbakery$bakedcachekey, bakedmodel1);
                return bakedmodel1;
            }
        }

        @Override
        public ModelDebugName rootName() {
            return this.rootName;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public interface TextureGetter {
        TextureAtlasSprite get(ModelDebugName p_377683_, Material p_375611_);

        TextureAtlasSprite reportMissingReference(ModelDebugName p_375507_, String p_376568_);

        default SpriteGetter bind(final ModelDebugName p_377175_) {
            return new SpriteGetter() {
                @Override
                public TextureAtlasSprite get(Material p_376856_) {
                    return TextureGetter.this.get(p_377175_, p_376856_);
                }

                @Override
                public TextureAtlasSprite reportMissingReference(String p_375947_) {
                    return TextureGetter.this.reportMissingReference(p_377175_, p_375947_);
                }
            };
        }
    }
}