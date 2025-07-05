package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.List;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.WeightedBakedModel;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record MultiVariant(List<Variant> variants) implements UnbakedBlockStateModel {
    public MultiVariant(List<Variant> variants) {
        if (variants.isEmpty()) {
            throw new IllegalArgumentException("Variant list must contain at least one element");
        } else {
            this.variants = variants;
        }
    }

    @Override
    public Object visualEqualityGroup(BlockState p_364633_) {
        return this;
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver p_378076_) {
        this.variants.forEach(p_374635_ -> p_378076_.resolve(p_374635_.modelLocation()));
    }

    @Override
    public BakedModel bake(ModelBaker p_249016_) {
        if (this.variants.size() == 1) {
            Variant variant1 = this.variants.getFirst();
            return p_249016_.bake(variant1.modelLocation(), variant1);
        } else {
            SimpleWeightedRandomList.Builder<BakedModel> builder = SimpleWeightedRandomList.builder();

            for (Variant variant : this.variants) {
                BakedModel bakedmodel = p_249016_.bake(variant.modelLocation(), variant);
                builder.add(bakedmodel, variant.weight());
            }

            return new WeightedBakedModel(builder.build());
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Deserializer implements JsonDeserializer<MultiVariant> {
        public MultiVariant deserialize(JsonElement p_111867_, Type p_111868_, JsonDeserializationContext p_111869_) throws JsonParseException {
            List<Variant> list = Lists.newArrayList();
            if (p_111867_.isJsonArray()) {
                JsonArray jsonarray = p_111867_.getAsJsonArray();
                if (jsonarray.isEmpty()) {
                    throw new JsonParseException("Empty variant array");
                }

                for (JsonElement jsonelement : jsonarray) {
                    list.add(p_111869_.deserialize(jsonelement, Variant.class));
                }
            } else {
                list.add(p_111869_.deserialize(p_111867_, Variant.class));
            }

            return new MultiVariant(list);
        }
    }
}