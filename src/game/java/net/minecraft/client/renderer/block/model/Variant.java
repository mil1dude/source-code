package net.minecraft.client.renderer.block.model;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.math.Transformation;
import java.lang.reflect.Type;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record Variant(ResourceLocation modelLocation, Transformation rotation, boolean uvLock, int weight) implements ModelState {
    @Override
    public Transformation getRotation() {
        return this.rotation;
    }

    @Override
    public boolean isUvLocked() {
        return this.uvLock;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Deserializer implements JsonDeserializer<Variant> {
        @VisibleForTesting
        static final boolean DEFAULT_UVLOCK = false;
        @VisibleForTesting
        static final int DEFAULT_WEIGHT = 1;
        @VisibleForTesting
        static final int DEFAULT_X_ROTATION = 0;
        @VisibleForTesting
        static final int DEFAULT_Y_ROTATION = 0;

        public Variant deserialize(JsonElement p_111893_, Type p_111894_, JsonDeserializationContext p_111895_) throws JsonParseException {
            JsonObject jsonobject = p_111893_.getAsJsonObject();
            ResourceLocation resourcelocation = this.getModel(jsonobject);
            BlockModelRotation blockmodelrotation = this.getBlockRotation(jsonobject);
            boolean flag = this.getUvLock(jsonobject);
            int i = this.getWeight(jsonobject);
            return new Variant(resourcelocation, blockmodelrotation.getRotation(), flag, i);
        }

        private boolean getUvLock(JsonObject p_111903_) {
            return GsonHelper.getAsBoolean(p_111903_, "uvlock", false);
        }

        protected BlockModelRotation getBlockRotation(JsonObject p_111897_) {
            int i = GsonHelper.getAsInt(p_111897_, "x", 0);
            int j = GsonHelper.getAsInt(p_111897_, "y", 0);
            BlockModelRotation blockmodelrotation = BlockModelRotation.by(i, j);
            if (blockmodelrotation == null) {
                throw new JsonParseException("Invalid BlockModelRotation x: " + i + ", y: " + j);
            } else {
                return blockmodelrotation;
            }
        }

        protected ResourceLocation getModel(JsonObject p_111899_) {
            return ResourceLocation.parse(GsonHelper.getAsString(p_111899_, "model"));
        }

        protected int getWeight(JsonObject p_111901_) {
            int i = GsonHelper.getAsInt(p_111901_, "weight", 1);
            if (i < 1) {
                throw new JsonParseException("Invalid weight " + i + " found, expected integer >= 1");
            } else {
                return i;
            }
        }
    }
}