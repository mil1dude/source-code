package net.minecraft.client.data.models;

import com.google.gson.JsonElement;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.client.data.models.blockstates.BlockStateGenerator;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelProvider implements DataProvider {
    private final PackOutput.PathProvider blockStatePathProvider;
    private final PackOutput.PathProvider itemInfoPathProvider;
    private final PackOutput.PathProvider modelPathProvider;

    public ModelProvider(PackOutput p_378149_) {
        this.blockStatePathProvider = p_378149_.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates");
        this.itemInfoPathProvider = p_378149_.createPathProvider(PackOutput.Target.RESOURCE_PACK, "items");
        this.modelPathProvider = p_378149_.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput p_376268_) {
        ModelProvider.ItemInfoCollector modelprovider$iteminfocollector = new ModelProvider.ItemInfoCollector();
        ModelProvider.BlockStateGeneratorCollector modelprovider$blockstategeneratorcollector = new ModelProvider.BlockStateGeneratorCollector();
        ModelProvider.SimpleModelCollector modelprovider$simplemodelcollector = new ModelProvider.SimpleModelCollector();
        new BlockModelGenerators(modelprovider$blockstategeneratorcollector, modelprovider$iteminfocollector, modelprovider$simplemodelcollector).run();
        new ItemModelGenerators(modelprovider$iteminfocollector, modelprovider$simplemodelcollector).run();
        modelprovider$blockstategeneratorcollector.validate();
        modelprovider$iteminfocollector.finalizeAndValidate();
        return CompletableFuture.allOf(
            modelprovider$blockstategeneratorcollector.save(p_376268_, this.blockStatePathProvider),
            modelprovider$simplemodelcollector.save(p_376268_, this.modelPathProvider),
            modelprovider$iteminfocollector.save(p_376268_, this.itemInfoPathProvider)
        );
    }

    static <T> CompletableFuture<?> saveAll(CachedOutput p_378816_, Function<T, Path> p_377914_, Map<T, ? extends Supplier<JsonElement>> p_377836_) {
        return DataProvider.saveAll(p_378816_, Supplier::get, p_377914_, p_377836_);
    }

    @Override
    public final String getName() {
        return "Model Definitions";
    }

    @OnlyIn(Dist.CLIENT)
    static class BlockStateGeneratorCollector implements Consumer<BlockStateGenerator> {
        private final Map<Block, BlockStateGenerator> generators = new HashMap<>();

        public void accept(BlockStateGenerator p_378147_) {
            Block block = p_378147_.getBlock();
            BlockStateGenerator blockstategenerator = this.generators.put(block, p_378147_);
            if (blockstategenerator != null) {
                throw new IllegalStateException("Duplicate blockstate definition for " + block);
            }
        }

        public void validate() {
            Stream<Holder.Reference<Block>> stream = BuiltInRegistries.BLOCK.listElements().filter(p_376480_ -> true);
            List<ResourceLocation> list = stream.filter(p_378423_ -> !this.generators.containsKey(p_378423_.value()))
                .map(p_378200_ -> p_378200_.key().location())
                .toList();
            if (!list.isEmpty()) {
                throw new IllegalStateException("Missing blockstate definitions for: " + list);
            }
        }

        public CompletableFuture<?> save(CachedOutput p_377986_, PackOutput.PathProvider p_377969_) {
            return ModelProvider.saveAll(p_377986_, p_378541_ -> p_377969_.json(p_378541_.builtInRegistryHolder().key().location()), this.generators);
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class ItemInfoCollector implements ItemModelOutput {
        private final Map<Item, ClientItem> itemInfos = new HashMap<>();
        private final Map<Item, Item> copies = new HashMap<>();

        @Override
        public void accept(Item p_376450_, ItemModel.Unbaked p_378513_) {
            this.register(p_376450_, new ClientItem(p_378513_, ClientItem.Properties.DEFAULT));
        }

        private void register(Item p_378050_, ClientItem p_376323_) {
            ClientItem clientitem = this.itemInfos.put(p_378050_, p_376323_);
            if (clientitem != null) {
                throw new IllegalStateException("Duplicate item model definition for " + p_378050_);
            }
        }

        @Override
        public void copy(Item p_377438_, Item p_376965_) {
            this.copies.put(p_376965_, p_377438_);
        }

        public void finalizeAndValidate() {
            BuiltInRegistries.ITEM.forEach(p_378629_ -> {
                if (!this.copies.containsKey(p_378629_)) {
                    if (p_378629_ instanceof BlockItem blockitem && !this.itemInfos.containsKey(blockitem)) {
                        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(blockitem.getBlock());
                        this.accept(blockitem, ItemModelUtils.plainModel(resourcelocation));
                    }
                }
            });
            this.copies.forEach((p_376289_, p_375718_) -> {
                ClientItem clientitem = this.itemInfos.get(p_375718_);
                if (clientitem == null) {
                    throw new IllegalStateException("Missing donor: " + p_375718_ + " -> " + p_376289_);
                } else {
                    this.register(p_376289_, clientitem);
                }
            });
            List<ResourceLocation> list = BuiltInRegistries.ITEM
                .listElements()
                .filter(p_377225_ -> !this.itemInfos.containsKey(p_377225_.value()))
                .map(p_378591_ -> p_378591_.key().location())
                .toList();
            if (!list.isEmpty()) {
                throw new IllegalStateException("Missing item model definitions for: " + list);
            }
        }

        public CompletableFuture<?> save(CachedOutput p_378568_, PackOutput.PathProvider p_377933_) {
            return DataProvider.saveAll(
                p_378568_, ClientItem.CODEC, p_377091_ -> p_377933_.json(p_377091_.builtInRegistryHolder().key().location()), this.itemInfos
            );
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class SimpleModelCollector implements BiConsumer<ResourceLocation, ModelInstance> {
        private final Map<ResourceLocation, ModelInstance> models = new HashMap<>();

        public void accept(ResourceLocation p_376394_, ModelInstance p_376914_) {
            Supplier<JsonElement> supplier = this.models.put(p_376394_, p_376914_);
            if (supplier != null) {
                throw new IllegalStateException("Duplicate model definition for " + p_376394_);
            }
        }

        public CompletableFuture<?> save(CachedOutput p_377109_, PackOutput.PathProvider p_378055_) {
            return ModelProvider.saveAll(p_377109_, p_378055_::json, this.models);
        }
    }
}