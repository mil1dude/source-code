package net.minecraft.client.renderer.block.model.multipart;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.client.renderer.block.model.MultiVariant;
import net.minecraft.client.renderer.block.model.UnbakedBlockStateModel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.MultiPartBakedModel;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MultiPart implements UnbakedBlockStateModel {
    private final List<MultiPart.InstantiatedSelector> selectors;

    MultiPart(List<MultiPart.InstantiatedSelector> p_111966_) {
        this.selectors = p_111966_;
    }

    @Override
    public Object visualEqualityGroup(BlockState p_362300_) {
        IntList intlist = new IntArrayList();

        for (int i = 0; i < this.selectors.size(); i++) {
            if (this.selectors.get(i).predicate.test(p_362300_)) {
                intlist.add(i);
            }
        }

        @OnlyIn(Dist.CLIENT)
        record Key(MultiPart model, IntList selectors) {
            }

        return new Key(this, intlist);
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver p_377638_) {
        this.selectors.forEach(p_374637_ -> p_374637_.variant.resolveDependencies(p_377638_));
    }

    @Override
    public BakedModel bake(ModelBaker p_249988_) {
        List<MultiPartBakedModel.Selector> list = new ArrayList<>(this.selectors.size());

        for (MultiPart.InstantiatedSelector multipart$instantiatedselector : this.selectors) {
            BakedModel bakedmodel = multipart$instantiatedselector.variant.bake(p_249988_);
            list.add(new MultiPartBakedModel.Selector(multipart$instantiatedselector.predicate, bakedmodel));
        }

        return new MultiPartBakedModel(list);
    }

    @OnlyIn(Dist.CLIENT)
    public static record Definition(List<Selector> selectors) {
        public MultiPart instantiate(StateDefinition<Block, BlockState> p_365641_) {
            List<MultiPart.InstantiatedSelector> list = this.selectors
                .stream()
                .map(p_369060_ -> new MultiPart.InstantiatedSelector(p_369060_.getPredicate(p_365641_), p_369060_.getVariant()))
                .toList();
            return new MultiPart(list);
        }

        public Set<MultiVariant> getMultiVariants() {
            return this.selectors.stream().map(Selector::getVariant).collect(Collectors.toSet());
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Deserializer implements JsonDeserializer<MultiPart.Definition> {
        public MultiPart.Definition deserialize(JsonElement p_111994_, Type p_111995_, JsonDeserializationContext p_111996_) throws JsonParseException {
            return new MultiPart.Definition(this.getSelectors(p_111996_, p_111994_.getAsJsonArray()));
        }

        private List<Selector> getSelectors(JsonDeserializationContext p_111991_, JsonArray p_111992_) {
            List<Selector> list = new ArrayList<>();
            if (p_111992_.isEmpty()) {
                throw new JsonSyntaxException("Empty selector array");
            } else {
                for (JsonElement jsonelement : p_111992_) {
                    list.add(p_111991_.deserialize(jsonelement, Selector.class));
                }

                return list;
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    static record InstantiatedSelector(Predicate<BlockState> predicate, MultiVariant variant) {
    }
}
