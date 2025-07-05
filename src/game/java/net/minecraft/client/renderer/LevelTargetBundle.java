package net.minecraft.client.renderer;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.ResourceHandle;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LevelTargetBundle implements PostChain.TargetBundle {
    public static final ResourceLocation MAIN_TARGET_ID = PostChain.MAIN_TARGET_ID;
    public static final ResourceLocation TRANSLUCENT_TARGET_ID = ResourceLocation.withDefaultNamespace("translucent");
    public static final ResourceLocation ITEM_ENTITY_TARGET_ID = ResourceLocation.withDefaultNamespace("item_entity");
    public static final ResourceLocation PARTICLES_TARGET_ID = ResourceLocation.withDefaultNamespace("particles");
    public static final ResourceLocation WEATHER_TARGET_ID = ResourceLocation.withDefaultNamespace("weather");
    public static final ResourceLocation CLOUDS_TARGET_ID = ResourceLocation.withDefaultNamespace("clouds");
    public static final ResourceLocation ENTITY_OUTLINE_TARGET_ID = ResourceLocation.withDefaultNamespace("entity_outline");
    public static final Set<ResourceLocation> MAIN_TARGETS = Set.of(MAIN_TARGET_ID);
    public static final Set<ResourceLocation> OUTLINE_TARGETS = Set.of(MAIN_TARGET_ID, ENTITY_OUTLINE_TARGET_ID);
    public static final Set<ResourceLocation> SORTING_TARGETS = Set.of(MAIN_TARGET_ID, TRANSLUCENT_TARGET_ID, ITEM_ENTITY_TARGET_ID, PARTICLES_TARGET_ID, WEATHER_TARGET_ID, CLOUDS_TARGET_ID);
    public ResourceHandle<RenderTarget> main = ResourceHandle.invalid();
    @Nullable
    public ResourceHandle<RenderTarget> translucent;
    @Nullable
    public ResourceHandle<RenderTarget> itemEntity;
    @Nullable
    public ResourceHandle<RenderTarget> particles;
    @Nullable
    public ResourceHandle<RenderTarget> weather;
    @Nullable
    public ResourceHandle<RenderTarget> clouds;
    @Nullable
    public ResourceHandle<RenderTarget> entityOutline;

    @Override
    public void replace(ResourceLocation p_362668_, ResourceHandle<RenderTarget> p_364961_) {
        if (p_362668_.equals(MAIN_TARGET_ID)) {
            this.main = p_364961_;
        } else if (p_362668_.equals(TRANSLUCENT_TARGET_ID)) {
            this.translucent = p_364961_;
        } else if (p_362668_.equals(ITEM_ENTITY_TARGET_ID)) {
            this.itemEntity = p_364961_;
        } else if (p_362668_.equals(PARTICLES_TARGET_ID)) {
            this.particles = p_364961_;
        } else if (p_362668_.equals(WEATHER_TARGET_ID)) {
            this.weather = p_364961_;
        } else if (p_362668_.equals(CLOUDS_TARGET_ID)) {
            this.clouds = p_364961_;
        } else {
            if (!p_362668_.equals(ENTITY_OUTLINE_TARGET_ID)) {
                throw new IllegalArgumentException("No target with id " + p_362668_);
            }

            this.entityOutline = p_364961_;
        }
    }

    @Nullable
    @Override
    public ResourceHandle<RenderTarget> get(ResourceLocation p_368551_) {
        if (p_368551_.equals(MAIN_TARGET_ID)) {
            return this.main;
        } else if (p_368551_.equals(TRANSLUCENT_TARGET_ID)) {
            return this.translucent;
        } else if (p_368551_.equals(ITEM_ENTITY_TARGET_ID)) {
            return this.itemEntity;
        } else if (p_368551_.equals(PARTICLES_TARGET_ID)) {
            return this.particles;
        } else if (p_368551_.equals(WEATHER_TARGET_ID)) {
            return this.weather;
        } else if (p_368551_.equals(CLOUDS_TARGET_ID)) {
            return this.clouds;
        } else {
            return p_368551_.equals(ENTITY_OUTLINE_TARGET_ID) ? this.entityOutline : null;
        }
    }

    public void clear() {
        this.main = ResourceHandle.invalid();
        this.translucent = null;
        this.itemEntity = null;
        this.particles = null;
        this.weather = null;
        this.clouds = null;
        this.entityOutline = null;
    }
}