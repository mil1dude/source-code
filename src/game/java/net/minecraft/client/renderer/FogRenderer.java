package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.shaders.FogShape;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.ARGB;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;
import org.joml.Vector4f;

@OnlyIn(Dist.CLIENT)
public class FogRenderer {
    private static final int WATER_FOG_DISTANCE = 96;
    private static final List<FogRenderer.MobEffectFogFunction> MOB_EFFECT_FOG = Lists.newArrayList(
        new FogRenderer.BlindnessFogFunction(), new FogRenderer.DarknessFogFunction()
    );
    public static final float BIOME_FOG_TRANSITION_TIME = 5000.0F;
    private static int targetBiomeFog = -1;
    private static int previousBiomeFog = -1;
    private static long biomeChangedTime = -1L;
    private static boolean fogEnabled = true;

    public static Vector4f computeFogColor(Camera p_362477_, float p_364035_, ClientLevel p_361507_, int p_361512_, float p_367602_) {
        FogType fogtype = p_362477_.getFluidInCamera();
        Entity entity = p_362477_.getEntity();
        float f;
        float f1;
        float f2;
        if (fogtype == FogType.WATER) {
            long i = Util.getMillis();
            int k = p_361507_.getBiome(BlockPos.containing(p_362477_.getPosition())).value().getWaterFogColor();
            if (biomeChangedTime < 0L) {
                targetBiomeFog = k;
                previousBiomeFog = k;
                biomeChangedTime = i;
            }

            int l = targetBiomeFog >> 16 & 0xFF;
            int i1 = targetBiomeFog >> 8 & 0xFF;
            int j1 = targetBiomeFog & 0xFF;
            int k1 = previousBiomeFog >> 16 & 0xFF;
            int l1 = previousBiomeFog >> 8 & 0xFF;
            int i2 = previousBiomeFog & 0xFF;
            float f3 = Mth.clamp((float)(i - biomeChangedTime) / 5000.0F, 0.0F, 1.0F);
            float f4 = Mth.lerp(f3, (float)k1, (float)l);
            float f5 = Mth.lerp(f3, (float)l1, (float)i1);
            float f6 = Mth.lerp(f3, (float)i2, (float)j1);
            f = f4 / 255.0F;
            f1 = f5 / 255.0F;
            f2 = f6 / 255.0F;
            if (targetBiomeFog != k) {
                targetBiomeFog = k;
                previousBiomeFog = Mth.floor(f4) << 16 | Mth.floor(f5) << 8 | Mth.floor(f6);
                biomeChangedTime = i;
            }
        } else if (fogtype == FogType.LAVA) {
            f = 0.6F;
            f1 = 0.1F;
            f2 = 0.0F;
            biomeChangedTime = -1L;
        } else if (fogtype == FogType.POWDER_SNOW) {
            f = 0.623F;
            f1 = 0.734F;
            f2 = 0.785F;
            biomeChangedTime = -1L;
        } else {
            float f7 = 0.25F + 0.75F * (float)p_361512_ / 32.0F;
            f7 = 1.0F - (float)Math.pow((double)f7, 0.25);
            int j = p_361507_.getSkyColor(p_362477_.getPosition(), p_364035_);
            float f9 = ARGB.redFloat(j);
            float f11 = ARGB.greenFloat(j);
            float f13 = ARGB.blueFloat(j);
            float f14 = Mth.clamp(Mth.cos(p_361507_.getTimeOfDay(p_364035_) * (float) (Math.PI * 2)) * 2.0F + 0.5F, 0.0F, 1.0F);
            BiomeManager biomemanager = p_361507_.getBiomeManager();
            Vec3 vec3 = p_362477_.getPosition().subtract(2.0, 2.0, 2.0).scale(0.25);
            Vec3 vec31 = CubicSampler.gaussianSampleVec3(
                vec3,
                (p_109033_, p_109034_, p_109035_) -> p_361507_.effects()
                        .getBrightnessDependentFogColor(Vec3.fromRGB24(biomemanager.getNoiseBiomeAtQuart(p_109033_, p_109034_, p_109035_).value().getFogColor()), f14)
            );
            f = (float)vec31.x();
            f1 = (float)vec31.y();
            f2 = (float)vec31.z();
            if (p_361512_ >= 4) {
                float f15 = Mth.sin(p_361507_.getSunAngle(p_364035_)) > 0.0F ? -1.0F : 1.0F;
                Vector3f vector3f = new Vector3f(f15, 0.0F, 0.0F);
                float f19 = p_362477_.getLookVector().dot(vector3f);
                if (f19 < 0.0F) {
                    f19 = 0.0F;
                }

                if (f19 > 0.0F && p_361507_.effects().isSunriseOrSunset(p_361507_.getTimeOfDay(p_364035_))) {
                    int j2 = p_361507_.effects().getSunriseOrSunsetColor(p_361507_.getTimeOfDay(p_364035_));
                    f19 *= ARGB.alphaFloat(j2);
                    f = f * (1.0F - f19) + ARGB.redFloat(j2) * f19;
                    f1 = f1 * (1.0F - f19) + ARGB.greenFloat(j2) * f19;
                    f2 = f2 * (1.0F - f19) + ARGB.blueFloat(j2) * f19;
                }
            }

            f += (f9 - f) * f7;
            f1 += (f11 - f1) * f7;
            f2 += (f13 - f2) * f7;
            float f16 = p_361507_.getRainLevel(p_364035_);
            if (f16 > 0.0F) {
                float f17 = 1.0F - f16 * 0.5F;
                float f20 = 1.0F - f16 * 0.4F;
                f *= f17;
                f1 *= f17;
                f2 *= f20;
            }

            float f18 = p_361507_.getThunderLevel(p_364035_);
            if (f18 > 0.0F) {
                float f21 = 1.0F - f18 * 0.5F;
                f *= f21;
                f1 *= f21;
                f2 *= f21;
            }

            biomeChangedTime = -1L;
        }

        float f8 = ((float)p_362477_.getPosition().y - (float)p_361507_.getMinY()) * p_361507_.getLevelData().getClearColorScale();
        FogRenderer.MobEffectFogFunction fogrenderer$mobeffectfogfunction = getPriorityFogFunction(entity, p_364035_);
        if (fogrenderer$mobeffectfogfunction != null) {
            LivingEntity livingentity = (LivingEntity)entity;
            f8 = fogrenderer$mobeffectfogfunction.getModifiedVoidDarkness(livingentity, livingentity.getEffect(fogrenderer$mobeffectfogfunction.getMobEffect()), f8, p_364035_);
        }

        if (f8 < 1.0F && fogtype != FogType.LAVA && fogtype != FogType.POWDER_SNOW) {
            if (f8 < 0.0F) {
                f8 = 0.0F;
            }

            f8 *= f8;
            f *= f8;
            f1 *= f8;
            f2 *= f8;
        }

        if (p_367602_ > 0.0F) {
            f = f * (1.0F - p_367602_) + f * 0.7F * p_367602_;
            f1 = f1 * (1.0F - p_367602_) + f1 * 0.6F * p_367602_;
            f2 = f2 * (1.0F - p_367602_) + f2 * 0.6F * p_367602_;
        }

        float f10;
        if (fogtype == FogType.WATER) {
            if (entity instanceof LocalPlayer) {
                f10 = ((LocalPlayer)entity).getWaterVision();
            } else {
                f10 = 1.0F;
            }
        } else {
            label86: {
                if (entity instanceof LivingEntity livingentity1
                    && livingentity1.hasEffect(MobEffects.NIGHT_VISION)
                    && !livingentity1.hasEffect(MobEffects.DARKNESS)) {
                    f10 = GameRenderer.getNightVisionScale(livingentity1, p_364035_);
                    break label86;
                }

                f10 = 0.0F;
            }
        }

        if (f != 0.0F && f1 != 0.0F && f2 != 0.0F) {
            float f12 = Math.min(1.0F / f, Math.min(1.0F / f1, 1.0F / f2));
            f = f * (1.0F - f10) + f * f12 * f10;
            f1 = f1 * (1.0F - f10) + f1 * f12 * f10;
            f2 = f2 * (1.0F - f10) + f2 * f12 * f10;
        }

        return new Vector4f(f, f1, f2, 1.0F);
    }

    public static boolean toggleFog() {
        return fogEnabled = !fogEnabled;
    }

    @Nullable
    private static FogRenderer.MobEffectFogFunction getPriorityFogFunction(Entity p_234166_, float p_234167_) {
        return p_234166_ instanceof LivingEntity livingentity
            ? MOB_EFFECT_FOG.stream().filter(p_234171_ -> p_234171_.isEnabled(livingentity, p_234167_)).findFirst().orElse(null)
            : null;
    }

    public static FogParameters setupFog(
        Camera p_234173_, FogRenderer.FogMode p_234174_, Vector4f p_365589_, float p_234175_, boolean p_234176_, float p_234177_
    ) {
        if (!fogEnabled) {
            return FogParameters.NO_FOG;
        } else {
            FogType fogtype = p_234173_.getFluidInCamera();
            Entity entity = p_234173_.getEntity();
            FogRenderer.FogData fogrenderer$fogdata = new FogRenderer.FogData(p_234174_);
            FogRenderer.MobEffectFogFunction fogrenderer$mobeffectfogfunction = getPriorityFogFunction(entity, p_234177_);
            if (fogtype == FogType.LAVA) {
                if (entity.isSpectator()) {
                    fogrenderer$fogdata.start = -8.0F;
                    fogrenderer$fogdata.end = p_234175_ * 0.5F;
                } else if (entity instanceof LivingEntity && ((LivingEntity)entity).hasEffect(MobEffects.FIRE_RESISTANCE)) {
                    fogrenderer$fogdata.start = 0.0F;
                    fogrenderer$fogdata.end = 5.0F;
                } else {
                    fogrenderer$fogdata.start = 0.25F;
                    fogrenderer$fogdata.end = 1.0F;
                }
            } else if (fogtype == FogType.POWDER_SNOW) {
                if (entity.isSpectator()) {
                    fogrenderer$fogdata.start = -8.0F;
                    fogrenderer$fogdata.end = p_234175_ * 0.5F;
                } else {
                    fogrenderer$fogdata.start = 0.0F;
                    fogrenderer$fogdata.end = 2.0F;
                }
            } else if (fogrenderer$mobeffectfogfunction != null) {
                LivingEntity livingentity = (LivingEntity)entity;
                MobEffectInstance mobeffectinstance = livingentity.getEffect(fogrenderer$mobeffectfogfunction.getMobEffect());
                if (mobeffectinstance != null) {
                    fogrenderer$mobeffectfogfunction.setupFog(fogrenderer$fogdata, livingentity, mobeffectinstance, p_234175_, p_234177_);
                }
            } else if (fogtype == FogType.WATER) {
                fogrenderer$fogdata.start = -8.0F;
                fogrenderer$fogdata.end = 96.0F;
                if (entity instanceof LocalPlayer localplayer) {
                    fogrenderer$fogdata.end = fogrenderer$fogdata.end * Math.max(0.25F, localplayer.getWaterVision());
                    Holder<Biome> holder = localplayer.level().getBiome(localplayer.blockPosition());
                    if (holder.is(BiomeTags.HAS_CLOSER_WATER_FOG)) {
                        fogrenderer$fogdata.end *= 0.85F;
                    }
                }

                if (fogrenderer$fogdata.end > p_234175_) {
                    fogrenderer$fogdata.end = p_234175_;
                    fogrenderer$fogdata.shape = FogShape.CYLINDER;
                }
            } else if (p_234176_) {
                fogrenderer$fogdata.start = p_234175_ * 0.05F;
                fogrenderer$fogdata.end = Math.min(p_234175_, 192.0F) * 0.5F;
            } else if (p_234174_ == FogRenderer.FogMode.FOG_SKY) {
                fogrenderer$fogdata.start = 0.0F;
                fogrenderer$fogdata.end = p_234175_;
                fogrenderer$fogdata.shape = FogShape.CYLINDER;
            } else if (p_234174_ == FogRenderer.FogMode.FOG_TERRAIN) {
                float f = Mth.clamp(p_234175_ / 10.0F, 4.0F, 64.0F);
                fogrenderer$fogdata.start = p_234175_ - f;
                fogrenderer$fogdata.end = p_234175_;
                fogrenderer$fogdata.shape = FogShape.CYLINDER;
            }

            return new FogParameters(
                fogrenderer$fogdata.start, fogrenderer$fogdata.end, fogrenderer$fogdata.shape, p_365589_.x, p_365589_.y, p_365589_.z, p_365589_.w
            );
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class BlindnessFogFunction implements FogRenderer.MobEffectFogFunction {
        @Override
        public Holder<MobEffect> getMobEffect() {
            return MobEffects.BLINDNESS;
        }

        @Override
        public void setupFog(FogRenderer.FogData p_234181_, LivingEntity p_234182_, MobEffectInstance p_234183_, float p_234184_, float p_234185_) {
            float f = p_234183_.isInfiniteDuration() ? 5.0F : Mth.lerp(Math.min(1.0F, (float)p_234183_.getDuration() / 20.0F), p_234184_, 5.0F);
            if (p_234181_.mode == FogRenderer.FogMode.FOG_SKY) {
                p_234181_.start = 0.0F;
                p_234181_.end = f * 0.8F;
            } else if (p_234181_.mode == FogRenderer.FogMode.FOG_TERRAIN) {
                p_234181_.start = f * 0.25F;
                p_234181_.end = f;
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class DarknessFogFunction implements FogRenderer.MobEffectFogFunction {
        @Override
        public Holder<MobEffect> getMobEffect() {
            return MobEffects.DARKNESS;
        }

        @Override
        public void setupFog(FogRenderer.FogData p_234194_, LivingEntity p_234195_, MobEffectInstance p_234196_, float p_234197_, float p_234198_) {
            float f = Mth.lerp(p_234196_.getBlendFactor(p_234195_, p_234198_), p_234197_, 15.0F);

            p_234194_.start = switch (p_234194_.mode) {
                case FOG_SKY -> 0.0F;
                case FOG_TERRAIN -> f * 0.75F;
            };
            p_234194_.end = f;
        }

        @Override
        public float getModifiedVoidDarkness(LivingEntity p_234189_, MobEffectInstance p_234190_, float p_234191_, float p_234192_) {
            return 1.0F - p_234190_.getBlendFactor(p_234189_, p_234192_);
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class FogData {
        public final FogRenderer.FogMode mode;
        public float start;
        public float end;
        public FogShape shape = FogShape.SPHERE;

        public FogData(FogRenderer.FogMode p_234204_) {
            this.mode = p_234204_;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static enum FogMode {
        FOG_SKY,
        FOG_TERRAIN;
    }

    @OnlyIn(Dist.CLIENT)
    interface MobEffectFogFunction {
        Holder<MobEffect> getMobEffect();

        void setupFog(FogRenderer.FogData p_234212_, LivingEntity p_234213_, MobEffectInstance p_234214_, float p_234215_, float p_234216_);

        default boolean isEnabled(LivingEntity p_234206_, float p_234207_) {
            return p_234206_.hasEffect(this.getMobEffect());
        }

        default float getModifiedVoidDarkness(LivingEntity p_234208_, MobEffectInstance p_234209_, float p_234210_, float p_234211_) {
            MobEffectInstance mobeffectinstance = p_234208_.getEffect(this.getMobEffect());
            if (mobeffectinstance != null) {
                if (mobeffectinstance.endsWithin(19)) {
                    p_234210_ = 1.0F - (float)mobeffectinstance.getDuration() / 20.0F;
                } else {
                    p_234210_ = 0.0F;
                }
            }

            return p_234210_;
        }
    }
}