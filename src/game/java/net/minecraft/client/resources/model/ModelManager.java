package net.minecraft.client.resources.model;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SpecialBlockModelRenderer;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ModelManager implements PreparableReloadListener, AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final FileToIdConverter MODEL_LISTER = FileToIdConverter.json("models");
    private static final Map<ResourceLocation, ResourceLocation> VANILLA_ATLASES = Map.of(
        Sheets.BANNER_SHEET,
        ResourceLocation.withDefaultNamespace("banner_patterns"),
        Sheets.BED_SHEET,
        ResourceLocation.withDefaultNamespace("beds"),
        Sheets.CHEST_SHEET,
        ResourceLocation.withDefaultNamespace("chests"),
        Sheets.SHIELD_SHEET,
        ResourceLocation.withDefaultNamespace("shield_patterns"),
        Sheets.SIGN_SHEET,
        ResourceLocation.withDefaultNamespace("signs"),
        Sheets.SHULKER_SHEET,
        ResourceLocation.withDefaultNamespace("shulker_boxes"),
        Sheets.ARMOR_TRIMS_SHEET,
        ResourceLocation.withDefaultNamespace("armor_trims"),
        Sheets.DECORATED_POT_SHEET,
        ResourceLocation.withDefaultNamespace("decorated_pot"),
        TextureAtlas.LOCATION_BLOCKS,
        ResourceLocation.withDefaultNamespace("blocks")
    );
    private Map<ModelResourceLocation, BakedModel> bakedBlockStateModels = Map.of();
    private Map<ResourceLocation, ItemModel> bakedItemStackModels = Map.of();
    private Map<ResourceLocation, ClientItem.Properties> itemProperties = Map.of();
    private final AtlasSet atlases;
    private final BlockModelShaper blockModelShaper;
    private final BlockColors blockColors;
    private EntityModelSet entityModelSet = EntityModelSet.EMPTY;
    private SpecialBlockModelRenderer specialBlockModelRenderer = SpecialBlockModelRenderer.EMPTY;
    private int maxMipmapLevels;
    private BakedModel missingModel;
    private ItemModel missingItemModel;
    private Object2IntMap<BlockState> modelGroups = Object2IntMaps.emptyMap();

    public ModelManager(TextureManager p_119406_, BlockColors p_119407_, int p_119408_) {
        this.blockColors = p_119407_;
        this.maxMipmapLevels = p_119408_;
        this.blockModelShaper = new BlockModelShaper(this);
        this.atlases = new AtlasSet(VANILLA_ATLASES, p_119406_);
    }

    public BakedModel getModel(ModelResourceLocation p_119423_) {
        return this.bakedBlockStateModels.getOrDefault(p_119423_, this.missingModel);
    }

    public BakedModel getMissingModel() {
        return this.missingModel;
    }

    public ItemModel getItemModel(ResourceLocation p_376816_) {
        return this.bakedItemStackModels.getOrDefault(p_376816_, this.missingItemModel);
    }

    public ClientItem.Properties getItemProperties(ResourceLocation p_378319_) {
        return this.itemProperties.getOrDefault(p_378319_, ClientItem.Properties.DEFAULT);
    }

    public BlockModelShaper getBlockModelShaper() {
        return this.blockModelShaper;
    }

    @Override
    public final CompletableFuture<Void> reload(
        PreparableReloadListener.PreparationBarrier p_249079_, ResourceManager p_251134_, Executor p_250550_, Executor p_249221_
    ) {
        UnbakedModel unbakedmodel = MissingBlockModel.missingModel();
        CompletableFuture<EntityModelSet> completablefuture = CompletableFuture.supplyAsync(EntityModelSet::vanilla, p_250550_);
        CompletableFuture<SpecialBlockModelRenderer> completablefuture1 = completablefuture.thenApplyAsync(SpecialBlockModelRenderer::vanilla, p_250550_);
        CompletableFuture<Map<ResourceLocation, UnbakedModel>> completablefuture2 = loadBlockModels(p_251134_, p_250550_);
        CompletableFuture<BlockStateModelLoader.LoadedModels> completablefuture3 = BlockStateModelLoader.loadBlockStates(unbakedmodel, p_251134_, p_250550_);
        CompletableFuture<ClientItemInfoLoader.LoadedClientInfos> completablefuture4 = ClientItemInfoLoader.scheduleLoad(p_251134_, p_250550_);
        CompletableFuture<ModelDiscovery> completablefuture5 = CompletableFuture.allOf(completablefuture2, completablefuture3, completablefuture4)
            .thenApplyAsync(p_374722_ -> discoverModelDependencies(unbakedmodel, completablefuture2.join(), completablefuture3.join(), completablefuture4.join()), p_250550_);
        CompletableFuture<Object2IntMap<BlockState>> completablefuture6 = completablefuture3.thenApplyAsync(
            p_358038_ -> buildModelGroups(this.blockColors, p_358038_), p_250550_
        );
        Map<ResourceLocation, CompletableFuture<AtlasSet.StitchResult>> map = this.atlases.scheduleLoad(p_251134_, this.maxMipmapLevels, p_250550_);
        return CompletableFuture.allOf(
                Stream.concat(
                        map.values().stream(),
                        Stream.of(completablefuture5, completablefuture6, completablefuture3, completablefuture4, completablefuture, completablefuture1)
                    )
                    .toArray(CompletableFuture[]::new)
            )
            .thenApplyAsync(
                p_374732_ -> {
                    Map<ResourceLocation, AtlasSet.StitchResult> map1 = map.entrySet()
                        .stream()
                        .collect(Collectors.toMap(Entry::getKey, p_248988_ -> p_248988_.getValue().join()));
                    ModelDiscovery modeldiscovery = completablefuture5.join();
                    Object2IntMap<BlockState> object2intmap = completablefuture6.join();
                    Set<ResourceLocation> set = modeldiscovery.getUnreferencedModels();
                    if (!set.isEmpty()) {
                        LOGGER.debug(
                            "Unreferenced models: \n{}", set.stream().sorted().map(p_374723_ -> "\t" + p_374723_ + "\n").collect(Collectors.joining())
                        );
                    }

                    ModelBakery modelbakery = new ModelBakery(
                        completablefuture.join(),
                        completablefuture3.join().plainModels(),
                        completablefuture4.join().contents(),
                        modeldiscovery.getReferencedModels(),
                        unbakedmodel
                    );
                    return loadModels(Profiler.get(), map1, modelbakery, object2intmap, completablefuture.join(), completablefuture1.join());
                },
                p_250550_
            )
            .thenCompose(p_252255_ -> p_252255_.readyForUpload.thenApply(p_251581_ -> (ModelManager.ReloadState)p_252255_))
            .thenCompose(p_249079_::wait)
            .thenAcceptAsync(p_358039_ -> this.apply(p_358039_, Profiler.get()), p_249221_);
    }

    private static CompletableFuture<Map<ResourceLocation, UnbakedModel>> loadBlockModels(ResourceManager p_251361_, Executor p_252189_) {
        return CompletableFuture.<Map<ResourceLocation, Resource>>supplyAsync(() -> MODEL_LISTER.listMatchingResources(p_251361_), p_252189_)
            .thenCompose(
                p_250597_ -> {
                    List<CompletableFuture<Pair<ResourceLocation, BlockModel>>> list = new ArrayList<>(p_250597_.size());

                    for (Entry<ResourceLocation, Resource> entry : p_250597_.entrySet()) {
                        list.add(CompletableFuture.supplyAsync(() -> {
                            ResourceLocation resourcelocation = MODEL_LISTER.fileToId(entry.getKey());

                            try {
                                Pair pair;
                                try (Reader reader = entry.getValue().openAsReader()) {
                                    pair = Pair.of(resourcelocation, BlockModel.fromStream(reader));
                                }

                                return pair;
                            } catch (Exception exception) {
                                LOGGER.error("Failed to load model {}", entry.getKey(), exception);
                                return null;
                            }
                        }, p_252189_));
                    }

                    return Util.sequence(list)
                        .thenApply(
                            p_250813_ -> p_250813_.stream().filter(Objects::nonNull).collect(Collectors.toUnmodifiableMap(Pair::getFirst, Pair::getSecond))
                        );
                }
            );
    }

    private static ModelDiscovery discoverModelDependencies(
        UnbakedModel p_364387_,
        Map<ResourceLocation, UnbakedModel> p_360749_,
        BlockStateModelLoader.LoadedModels p_366446_,
        ClientItemInfoLoader.LoadedClientInfos p_378505_
    ) {
        ModelDiscovery modeldiscovery = new ModelDiscovery(p_360749_, p_364387_);
        p_366446_.forResolving().forEach(modeldiscovery::addRoot);
        p_378505_.contents().values().forEach(p_374734_ -> modeldiscovery.addRoot(p_374734_.model()));
        modeldiscovery.registerSpecialModels();
        modeldiscovery.discoverDependencies();
        return modeldiscovery;
    }

    private static ModelManager.ReloadState loadModels(
        ProfilerFiller p_252136_,
        final Map<ResourceLocation, AtlasSet.StitchResult> p_250646_,
        ModelBakery p_248945_,
        Object2IntMap<BlockState> p_361513_,
        EntityModelSet p_378097_,
        SpecialBlockModelRenderer p_377275_
    ) {
        p_252136_.push("baking");
        final Multimap<String, Material> multimap = HashMultimap.create();
        final Multimap<String, String> multimap1 = HashMultimap.create();
        final TextureAtlasSprite textureatlassprite = p_250646_.get(TextureAtlas.LOCATION_BLOCKS).missing();
        ModelBakery.BakingResult modelbakery$bakingresult = p_248945_.bakeModels(new ModelBakery.TextureGetter() {
            @Override
            public TextureAtlasSprite get(ModelDebugName p_375833_, Material p_375858_) {
                AtlasSet.StitchResult atlasset$stitchresult = p_250646_.get(p_375858_.atlasLocation());
                TextureAtlasSprite textureatlassprite1 = atlasset$stitchresult.getSprite(p_375858_.texture());
                if (textureatlassprite1 != null) {
                    return textureatlassprite1;
                } else {
                    multimap.put(p_375833_.get(), p_375858_);
                    return atlasset$stitchresult.missing();
                }
            }

            @Override
            public TextureAtlasSprite reportMissingReference(ModelDebugName p_377684_, String p_378821_) {
                multimap1.put(p_377684_.get(), p_378821_);
                return textureatlassprite;
            }
        });
        multimap.asMap()
            .forEach(
                (p_376688_, p_252017_) -> LOGGER.warn(
                        "Missing textures in model {}:\n{}",
                        p_376688_,
                        p_252017_.stream()
                            .sorted(Material.COMPARATOR)
                            .map(p_325574_ -> "    " + p_325574_.atlasLocation() + ":" + p_325574_.texture())
                            .collect(Collectors.joining("\n"))
                    )
            );
        multimap1.asMap()
            .forEach(
                (p_374739_, p_374740_) -> LOGGER.warn(
                        "Missing texture references in model {}:\n{}",
                        p_374739_,
                        p_374740_.stream().sorted().map(p_374742_ -> "    " + p_374742_).collect(Collectors.joining("\n"))
                    )
            );
        p_252136_.popPush("dispatch");
        Map<BlockState, BakedModel> map = createBlockStateToModelDispatch(modelbakery$bakingresult.blockStateModels(), modelbakery$bakingresult.missingModel());
        CompletableFuture<Void> completablefuture = CompletableFuture.allOf(
            p_250646_.values().stream().map(AtlasSet.StitchResult::readyForUpload).toArray(CompletableFuture[]::new)
        );
        p_252136_.pop();
        return new ModelManager.ReloadState(modelbakery$bakingresult, p_361513_, map, p_250646_, p_378097_, p_377275_, completablefuture);
    }

    private static Map<BlockState, BakedModel> createBlockStateToModelDispatch(Map<ModelResourceLocation, BakedModel> p_377857_, BakedModel p_378390_) {
        Map<BlockState, BakedModel> map = new IdentityHashMap<>();

        for (Block block : BuiltInRegistries.BLOCK) {
            block.getStateDefinition().getPossibleStates().forEach(p_374738_ -> {
                ResourceLocation resourcelocation = p_374738_.getBlock().builtInRegistryHolder().key().location();
                ModelResourceLocation modelresourcelocation = BlockModelShaper.stateToModelLocation(resourcelocation, p_374738_);
                BakedModel bakedmodel = p_377857_.get(modelresourcelocation);
                if (bakedmodel == null) {
                    LOGGER.warn("Missing model for variant: '{}'", modelresourcelocation);
                    map.putIfAbsent(p_374738_, p_378390_);
                } else {
                    map.put(p_374738_, bakedmodel);
                }
            });
        }

        return map;
    }

    private static Object2IntMap<BlockState> buildModelGroups(BlockColors p_369941_, BlockStateModelLoader.LoadedModels p_360724_) {
        return ModelGroupCollector.build(p_369941_, p_360724_);
    }

    private void apply(ModelManager.ReloadState p_248996_, ProfilerFiller p_251960_) {
        p_251960_.push("upload");
        p_248996_.atlasPreparations.values().forEach(AtlasSet.StitchResult::upload);
        ModelBakery.BakingResult modelbakery$bakingresult = p_248996_.bakedModels;
        this.bakedBlockStateModels = modelbakery$bakingresult.blockStateModels();
        this.bakedItemStackModels = modelbakery$bakingresult.itemStackModels();
        this.itemProperties = modelbakery$bakingresult.itemProperties();
        this.modelGroups = p_248996_.modelGroups;
        this.missingModel = modelbakery$bakingresult.missingModel();
        this.missingItemModel = modelbakery$bakingresult.missingItemModel();
        p_251960_.popPush("cache");
        this.blockModelShaper.replaceCache(p_248996_.modelCache);
        this.specialBlockModelRenderer = p_248996_.specialBlockModelRenderer;
        this.entityModelSet = p_248996_.entityModelSet;
        p_251960_.pop();
    }

    public boolean requiresRender(BlockState p_119416_, BlockState p_119417_) {
        if (p_119416_ == p_119417_) {
            return false;
        } else {
            int i = this.modelGroups.getInt(p_119416_);
            if (i != -1) {
                int j = this.modelGroups.getInt(p_119417_);
                if (i == j) {
                    FluidState fluidstate = p_119416_.getFluidState();
                    FluidState fluidstate1 = p_119417_.getFluidState();
                    return fluidstate != fluidstate1;
                }
            }

            return true;
        }
    }

    public TextureAtlas getAtlas(ResourceLocation p_119429_) {
        return this.atlases.getAtlas(p_119429_);
    }

    @Override
    public void close() {
        this.atlases.close();
    }

    public void updateMaxMipLevel(int p_119411_) {
        this.maxMipmapLevels = p_119411_;
    }

    public Supplier<SpecialBlockModelRenderer> specialBlockModelRenderer() {
        return () -> this.specialBlockModelRenderer;
    }

    public Supplier<EntityModelSet> entityModels() {
        return () -> this.entityModelSet;
    }

    @OnlyIn(Dist.CLIENT)
    static record ReloadState(
        ModelBakery.BakingResult bakedModels,
        Object2IntMap<BlockState> modelGroups,
        Map<BlockState, BakedModel> modelCache,
        Map<ResourceLocation, AtlasSet.StitchResult> atlasPreparations,
        EntityModelSet entityModelSet,
        SpecialBlockModelRenderer specialBlockModelRenderer,
        CompletableFuture<Void> readyForUpload
    ) {
    }
}