package net.minecraft.client.resources.model;

import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ModelDiscovery {
    static final Logger LOGGER = LogUtils.getLogger();
    private final Map<ResourceLocation, UnbakedModel> inputModels;
    final UnbakedModel missingModel;
    private final List<ResolvableModel> topModels = new ArrayList<>();
    private final Map<ResourceLocation, UnbakedModel> referencedModels = new HashMap<>();

    public ModelDiscovery(Map<ResourceLocation, UnbakedModel> p_362964_, UnbakedModel p_367385_) {
        this.inputModels = p_362964_;
        this.missingModel = p_367385_;
        this.referencedModels.put(MissingBlockModel.LOCATION, p_367385_);
    }

    public void registerSpecialModels() {
        this.referencedModels.put(ItemModelGenerator.GENERATED_ITEM_MODEL_ID, new ItemModelGenerator());
    }

    public void addRoot(ResolvableModel p_376215_) {
        this.topModels.add(p_376215_);
    }

    public void discoverDependencies() {
        this.topModels.forEach(p_374712_ -> p_374712_.resolveDependencies(new ModelDiscovery.ResolverImpl()));
    }

    public Map<ResourceLocation, UnbakedModel> getReferencedModels() {
        return this.referencedModels;
    }

    public Set<ResourceLocation> getUnreferencedModels() {
        return Sets.difference(this.inputModels.keySet(), this.referencedModels.keySet());
    }

    UnbakedModel getBlockModel(ResourceLocation p_363667_) {
        return this.referencedModels.computeIfAbsent(p_363667_, this::loadBlockModel);
    }

    private UnbakedModel loadBlockModel(ResourceLocation p_368910_) {
        UnbakedModel unbakedmodel = this.inputModels.get(p_368910_);
        if (unbakedmodel == null) {
            LOGGER.warn("Missing block model: '{}'", p_368910_);
            return this.missingModel;
        } else {
            return unbakedmodel;
        }
    }

    @OnlyIn(Dist.CLIENT)
    class ResolverImpl implements ResolvableModel.Resolver {
        private final List<ResourceLocation> stack = new ArrayList<>();
        private final Set<ResourceLocation> resolvedModels = new HashSet<>();

        @Override
        public UnbakedModel resolve(ResourceLocation p_360973_) {
            if (this.stack.contains(p_360973_)) {
                ModelDiscovery.LOGGER.warn("Detected model loading loop: {}->{}", this.stacktraceToString(), p_360973_);
                return ModelDiscovery.this.missingModel;
            } else {
                UnbakedModel unbakedmodel = ModelDiscovery.this.getBlockModel(p_360973_);
                if (this.resolvedModels.add(p_360973_)) {
                    this.stack.add(p_360973_);
                    unbakedmodel.resolveDependencies(this);
                    this.stack.remove(p_360973_);
                }

                return unbakedmodel;
            }
        }

        private String stacktraceToString() {
            return this.stack.stream().map(ResourceLocation::toString).collect(Collectors.joining("->"));
        }
    }
}