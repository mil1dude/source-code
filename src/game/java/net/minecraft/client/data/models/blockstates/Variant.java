package net.minecraft.client.data.models.blockstates;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Variant implements Supplier<JsonElement> {
    private final Map<VariantProperty<?>, VariantProperty<?>.Value> values = Maps.newLinkedHashMap();

    public <T> Variant with(VariantProperty<T> p_378303_, T p_377748_) {
        VariantProperty<?>.Value variantproperty = this.values.put(p_378303_, p_378303_.withValue(p_377748_));
        if (variantproperty != null) {
            throw new IllegalStateException("Replacing value of " + variantproperty + " with " + p_377748_);
        } else {
            return this;
        }
    }

    public static Variant variant() {
        return new Variant();
    }

    public static Variant merge(Variant p_376139_, Variant p_378642_) {
        Variant variant = new Variant();
        variant.values.putAll(p_376139_.values);
        variant.values.putAll(p_378642_.values);
        return variant;
    }

    public JsonElement get() {
        JsonObject jsonobject = new JsonObject();
        this.values.values().forEach(p_377837_ -> p_377837_.addToVariant(jsonobject));
        return jsonobject;
    }

    public static JsonElement convertList(List<Variant> p_378697_) {
        if (p_378697_.size() == 1) {
            return p_378697_.get(0).get();
        } else {
            JsonArray jsonarray = new JsonArray();
            p_378697_.forEach(p_375800_ -> jsonarray.add(p_375800_.get()));
            return jsonarray;
        }
    }
}