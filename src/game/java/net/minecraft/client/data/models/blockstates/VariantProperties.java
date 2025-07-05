package net.minecraft.client.data.models.blockstates;

import com.google.gson.JsonPrimitive;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VariantProperties {
    public static final VariantProperty<VariantProperties.Rotation> X_ROT = new VariantProperty<>("x", p_378021_ -> new JsonPrimitive(p_378021_.value));
    public static final VariantProperty<VariantProperties.Rotation> Y_ROT = new VariantProperty<>("y", p_375721_ -> new JsonPrimitive(p_375721_.value));
    public static final VariantProperty<ResourceLocation> MODEL = new VariantProperty<>("model", p_375977_ -> new JsonPrimitive(p_375977_.toString()));
    public static final VariantProperty<Boolean> UV_LOCK = new VariantProperty<>("uvlock", JsonPrimitive::new);
    public static final VariantProperty<Integer> WEIGHT = new VariantProperty<>("weight", JsonPrimitive::new);

    @OnlyIn(Dist.CLIENT)
    public static enum Rotation {
        R0(0),
        R90(90),
        R180(180),
        R270(270);

        final int value;

        private Rotation(final int p_377553_) {
            this.value = p_377553_;
        }
    }
}