package net.minecraft.client.renderer;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class DimensionSpecialEffects {
    private static final Object2ObjectMap<ResourceLocation, DimensionSpecialEffects> EFFECTS = Util.make(new Object2ObjectArrayMap<>(), p_108881_ -> {
        DimensionSpecialEffects.OverworldEffects dimensionspecialeffects$overworldeffects = new DimensionSpecialEffects.OverworldEffects();
        p_108881_.defaultReturnValue(dimensionspecialeffects$overworldeffects);
        p_108881_.put(BuiltinDimensionTypes.OVERWORLD_EFFECTS, dimensionspecialeffects$overworldeffects);
        p_108881_.put(BuiltinDimensionTypes.NETHER_EFFECTS, new DimensionSpecialEffects.NetherEffects());
        p_108881_.put(BuiltinDimensionTypes.END_EFFECTS, new DimensionSpecialEffects.EndEffects());
    });
    private final float cloudLevel;
    private final boolean hasGround;
    private final DimensionSpecialEffects.SkyType skyType;
    private final boolean forceBrightLightmap;
    private final boolean constantAmbientLight;

    public DimensionSpecialEffects(float p_108866_, boolean p_108867_, DimensionSpecialEffects.SkyType p_108868_, boolean p_108869_, boolean p_108870_) {
        this.cloudLevel = p_108866_;
        this.hasGround = p_108867_;
        this.skyType = p_108868_;
        this.forceBrightLightmap = p_108869_;
        this.constantAmbientLight = p_108870_;
    }

    public static DimensionSpecialEffects forType(DimensionType p_108877_) {
        return EFFECTS.get(p_108877_.effectsLocation());
    }

    public boolean isSunriseOrSunset(float p_361903_) {
        return false;
    }

    public int getSunriseOrSunsetColor(float p_367777_) {
        return 0;
    }

    public float getCloudHeight() {
        return this.cloudLevel;
    }

    public boolean hasGround() {
        return this.hasGround;
    }

    public abstract Vec3 getBrightnessDependentFogColor(Vec3 p_108878_, float p_108879_);

    public abstract boolean isFoggyAt(int p_108874_, int p_108875_);

    public DimensionSpecialEffects.SkyType skyType() {
        return this.skyType;
    }

    public boolean forceBrightLightmap() {
        return this.forceBrightLightmap;
    }

    public boolean constantAmbientLight() {
        return this.constantAmbientLight;
    }

    @OnlyIn(Dist.CLIENT)
    public static class EndEffects extends DimensionSpecialEffects {
        public EndEffects() {
            super(Float.NaN, false, DimensionSpecialEffects.SkyType.END, true, false);
        }

        @Override
        public Vec3 getBrightnessDependentFogColor(Vec3 p_108894_, float p_108895_) {
            return p_108894_.scale(0.15F);
        }

        @Override
        public boolean isFoggyAt(int p_108891_, int p_108892_) {
            return false;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class NetherEffects extends DimensionSpecialEffects {
        public NetherEffects() {
            super(Float.NaN, true, DimensionSpecialEffects.SkyType.NONE, false, true);
        }

        @Override
        public Vec3 getBrightnessDependentFogColor(Vec3 p_108901_, float p_108902_) {
            return p_108901_;
        }

        @Override
        public boolean isFoggyAt(int p_108898_, int p_108899_) {
            return true;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class OverworldEffects extends DimensionSpecialEffects {
        public static final int CLOUD_LEVEL = 192;
        private static final float SUNRISE_AND_SUNSET_TIMESPAN = 0.4F;

        public OverworldEffects() {
            super(192.0F, true, DimensionSpecialEffects.SkyType.OVERWORLD, false, false);
        }

        @Override
        public boolean isSunriseOrSunset(float p_364112_) {
            float f = Mth.cos(p_364112_ * (float) (Math.PI * 2));
            return f >= -0.4F && f <= 0.4F;
        }

        @Override
        public int getSunriseOrSunsetColor(float p_362545_) {
            float f = Mth.cos(p_362545_ * (float) (Math.PI * 2));
            float f1 = f / 0.4F * 0.5F + 0.5F;
            float f2 = Mth.square(1.0F - (1.0F - Mth.sin(f1 * (float) Math.PI)) * 0.99F);
            return ARGB.colorFromFloat(f2, f1 * 0.3F + 0.7F, f1 * f1 * 0.7F + 0.2F, 0.2F);
        }

        @Override
        public Vec3 getBrightnessDependentFogColor(Vec3 p_108908_, float p_108909_) {
            return p_108908_.multiply((double)(p_108909_ * 0.94F + 0.06F), (double)(p_108909_ * 0.94F + 0.06F), (double)(p_108909_ * 0.91F + 0.09F));
        }

        @Override
        public boolean isFoggyAt(int p_108905_, int p_108906_) {
            return false;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static enum SkyType {
        NONE,
        OVERWORLD,
        END;
    }
}