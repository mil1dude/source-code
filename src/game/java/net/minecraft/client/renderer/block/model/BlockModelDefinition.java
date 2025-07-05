package net.minecraft.client.renderer.block.model;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.block.model.multipart.MultiPart;
import net.minecraft.client.renderer.block.model.multipart.Selector;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class BlockModelDefinition {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(BlockModelDefinition.class, new BlockModelDefinition.Deserializer())
        .registerTypeAdapter(Variant.class, new Variant.Deserializer())
        .registerTypeAdapter(MultiVariant.class, new MultiVariant.Deserializer())
        .registerTypeAdapter(MultiPart.Definition.class, new MultiPart.Deserializer())
        .registerTypeAdapter(Selector.class, new Selector.Deserializer())
        .create();
    private final Map<String, MultiVariant> variants;
    @Nullable
    private final MultiPart.Definition multiPart;

    public static BlockModelDefinition fromStream(Reader p_111542_) {
        return GsonHelper.fromJson(GSON, p_111542_, BlockModelDefinition.class);
    }

    public static BlockModelDefinition fromJsonElement(JsonElement p_250730_) {
        return GSON.fromJson(p_250730_, BlockModelDefinition.class);
    }

    public BlockModelDefinition(Map<String, MultiVariant> p_364442_, @Nullable MultiPart.Definition p_363348_) {
        this.multiPart = p_363348_;
        this.variants = p_364442_;
    }

    @VisibleForTesting
    public MultiVariant getVariant(String p_173429_) {
        MultiVariant multivariant = this.variants.get(p_173429_);
        if (multivariant == null) {
            throw new BlockModelDefinition.MissingVariantException();
        } else {
            return multivariant;
        }
    }

    @Override
    public boolean equals(Object p_111546_) {
        if (this == p_111546_) {
            return true;
        } else {
            return !(p_111546_ instanceof BlockModelDefinition blockmodeldefinition)
                ? false
                : this.variants.equals(blockmodeldefinition.variants) && Objects.equals(this.multiPart, blockmodeldefinition.multiPart);
        }
    }

    @Override
    public int hashCode() {
        return 31 * this.variants.hashCode() + (this.multiPart != null ? this.multiPart.hashCode() : 0);
    }

    @VisibleForTesting
    public Set<MultiVariant> getMultiVariants() {
        Set<MultiVariant> set = Sets.newHashSet(this.variants.values());
        if (this.multiPart != null) {
            set.addAll(this.multiPart.getMultiVariants());
        }

        return set;
    }

    @Nullable
    public MultiPart.Definition getMultiPart() {
        return this.multiPart;
    }

    public Map<BlockState, UnbakedBlockStateModel> instantiate(StateDefinition<Block, BlockState> p_361733_, String p_364653_) {
        Map<BlockState, UnbakedBlockStateModel> map = new IdentityHashMap<>();
        List<BlockState> list = p_361733_.getPossibleStates();
        MultiPart multipart;
        if (this.multiPart != null) {
            multipart = this.multiPart.instantiate(p_361733_);
            list.forEach(p_363978_ -> map.put(p_363978_, multipart));
        } else {
            multipart = null;
        }

        this.variants
            .forEach(
                (p_364884_, p_363250_) -> {
                    try {
                        list.stream()
                            .filter(VariantSelector.predicate(p_361733_, p_364884_))
                            .forEach(
                                p_374633_ -> {
                                    UnbakedBlockStateModel unbakedblockstatemodel = map.put(p_374633_, p_363250_);
                                    if (unbakedblockstatemodel != null && unbakedblockstatemodel != multipart) {
                                        String s = this.variants
                                            .entrySet()
                                            .stream()
                                            .filter(p_367129_ -> p_367129_.getValue() == unbakedblockstatemodel)
                                            .findFirst()
                                            .get()
                                            .getKey();
                                        throw new RuntimeException("Overlapping definition with: " + s);
                                    }
                                }
                            );
                    } catch (Exception exception) {
                        LOGGER.warn("Exception loading blockstate definition: '{}' for variant: '{}': {}", p_364653_, p_364884_, exception.getMessage());
                    }
                }
            );
        return map;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Deserializer implements JsonDeserializer<BlockModelDefinition> {
        public BlockModelDefinition deserialize(JsonElement p_111559_, Type p_111560_, JsonDeserializationContext p_111561_) throws JsonParseException {
            JsonObject jsonobject = p_111559_.getAsJsonObject();
            Map<String, MultiVariant> map = this.getVariants(p_111561_, jsonobject);
            MultiPart.Definition multipart$definition = this.getMultiPart(p_111561_, jsonobject);
            if (map.isEmpty() && multipart$definition == null) {
                throw new JsonParseException("Neither 'variants' nor 'multipart' found");
            } else {
                return new BlockModelDefinition(map, multipart$definition);
            }
        }

        protected Map<String, MultiVariant> getVariants(JsonDeserializationContext p_111556_, JsonObject p_111557_) {
            Map<String, MultiVariant> map = Maps.newHashMap();
            if (p_111557_.has("variants")) {
                JsonObject jsonobject = GsonHelper.getAsJsonObject(p_111557_, "variants");

                for (Entry<String, JsonElement> entry : jsonobject.entrySet()) {
                    map.put(entry.getKey(), p_111556_.deserialize(entry.getValue(), MultiVariant.class));
                }
            }

            return map;
        }

        @Nullable
        protected MultiPart.Definition getMultiPart(JsonDeserializationContext p_111563_, JsonObject p_111564_) {
            if (!p_111564_.has("multipart")) {
                return null;
            } else {
                JsonArray jsonarray = GsonHelper.getAsJsonArray(p_111564_, "multipart");
                return p_111563_.deserialize(jsonarray, MultiPart.Definition.class);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected static class MissingVariantException extends RuntimeException {
    }
}