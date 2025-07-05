package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WeatherEffectRenderer {
    private static final int RAIN_RADIUS = 10;
    private static final int RAIN_DIAMETER = 21;
    private static final ResourceLocation RAIN_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/rain.png");
    private static final ResourceLocation SNOW_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/snow.png");
    private static final int RAIN_TABLE_SIZE = 32;
    private static final int HALF_RAIN_TABLE_SIZE = 16;
    private int rainSoundTime;
    private final float[] columnSizeX = new float[1024];
    private final float[] columnSizeZ = new float[1024];

    public WeatherEffectRenderer() {
        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 32; j++) {
                float f = (float)(j - 16);
                float f1 = (float)(i - 16);
                float f2 = Mth.length(f, f1);
                this.columnSizeX[i * 32 + j] = -f1 / f2;
                this.columnSizeZ[i * 32 + j] = f / f2;
            }
        }
    }

    public void render(Level p_361632_, MultiBufferSource p_376033_, int p_365872_, float p_365795_, Vec3 p_361547_) {
        float f = p_361632_.getRainLevel(p_365795_);
        if (!(f <= 0.0F)) {
            int i = Minecraft.useFancyGraphics() ? 10 : 5;
            List<WeatherEffectRenderer.ColumnInstance> list = new ArrayList<>();
            List<WeatherEffectRenderer.ColumnInstance> list1 = new ArrayList<>();
            this.collectColumnInstances(p_361632_, p_365872_, p_365795_, p_361547_, i, list, list1);
            if (!list.isEmpty() || !list1.isEmpty()) {
                this.render(p_376033_, p_361547_, i, f, list, list1);
            }
        }
    }

    private void collectColumnInstances(
        Level p_363306_,
        int p_367248_,
        float p_368775_,
        Vec3 p_367550_,
        int p_361277_,
        List<WeatherEffectRenderer.ColumnInstance> p_364246_,
        List<WeatherEffectRenderer.ColumnInstance> p_362622_
    ) {
        int i = Mth.floor(p_367550_.x);
        int j = Mth.floor(p_367550_.y);
        int k = Mth.floor(p_367550_.z);
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        RandomSource randomsource = RandomSource.create();

        for (int l = k - p_361277_; l <= k + p_361277_; l++) {
            for (int i1 = i - p_361277_; i1 <= i + p_361277_; i1++) {
                int j1 = p_363306_.getHeight(Heightmap.Types.MOTION_BLOCKING, i1, l);
                int k1 = Math.max(j - p_361277_, j1);
                int l1 = Math.max(j + p_361277_, j1);
                if (l1 - k1 != 0) {
                    Biome.Precipitation biome$precipitation = this.getPrecipitationAt(p_363306_, blockpos$mutableblockpos.set(i1, j, l));
                    if (biome$precipitation != Biome.Precipitation.NONE) {
                        int i2 = i1 * i1 * 3121 + i1 * 45238971 ^ l * l * 418711 + l * 13761;
                        randomsource.setSeed((long)i2);
                        int j2 = Math.max(j, j1);
                        int k2 = LevelRenderer.getLightColor(p_363306_, blockpos$mutableblockpos.set(i1, j2, l));
                        if (biome$precipitation == Biome.Precipitation.RAIN) {
                            p_364246_.add(this.createRainColumnInstance(randomsource, p_367248_, i1, k1, l1, l, k2, p_368775_));
                        } else if (biome$precipitation == Biome.Precipitation.SNOW) {
                            p_362622_.add(this.createSnowColumnInstance(randomsource, p_367248_, i1, k1, l1, l, k2, p_368775_));
                        }
                    }
                }
            }
        }
    }

    private void render(
        MultiBufferSource p_375954_,
        Vec3 p_368504_,
        int p_362749_,
        float p_362074_,
        List<WeatherEffectRenderer.ColumnInstance> p_361651_,
        List<WeatherEffectRenderer.ColumnInstance> p_361059_
    ) {
        if (!p_361651_.isEmpty()) {
            RenderType rendertype = RenderType.weather(RAIN_LOCATION, Minecraft.useShaderTransparency());
            this.renderInstances(p_375954_.getBuffer(rendertype), p_361651_, p_368504_, 1.0F, p_362749_, p_362074_);
        }

        if (!p_361059_.isEmpty()) {
            RenderType rendertype1 = RenderType.weather(SNOW_LOCATION, Minecraft.useShaderTransparency());
            this.renderInstances(p_375954_.getBuffer(rendertype1), p_361059_, p_368504_, 0.8F, p_362749_, p_362074_);
        }
    }

    private WeatherEffectRenderer.ColumnInstance createRainColumnInstance(
        RandomSource p_369207_, int p_369418_, int p_368589_, int p_364560_, int p_362596_, int p_368571_, int p_362548_, float p_362995_
    ) {
        int i = p_369418_ & 131071;
        int j = p_368589_ * p_368589_ * 3121 + p_368589_ * 45238971 + p_368571_ * p_368571_ * 418711 + p_368571_ * 13761 & 0xFF;
        float f = 3.0F + p_369207_.nextFloat();
        float f1 = -((float)(i + j) + p_362995_) / 32.0F * f;
        float f2 = f1 % 32.0F;
        return new WeatherEffectRenderer.ColumnInstance(p_368589_, p_368571_, p_364560_, p_362596_, 0.0F, f2, p_362548_);
    }

    private WeatherEffectRenderer.ColumnInstance createSnowColumnInstance(
        RandomSource p_362287_, int p_363885_, int p_367897_, int p_362095_, int p_364648_, int p_366422_, int p_369864_, float p_367820_
    ) {
        float f = (float)p_363885_ + p_367820_;
        float f1 = (float)(p_362287_.nextDouble() + (double)(f * 0.01F * (float)p_362287_.nextGaussian()));
        float f2 = (float)(p_362287_.nextDouble() + (double)(f * (float)p_362287_.nextGaussian() * 0.001F));
        float f3 = -((float)(p_363885_ & 511) + p_367820_) / 512.0F;
        int i = LightTexture.pack((LightTexture.block(p_369864_) * 3 + 15) / 4, (LightTexture.sky(p_369864_) * 3 + 15) / 4);
        return new WeatherEffectRenderer.ColumnInstance(p_367897_, p_366422_, p_362095_, p_364648_, f1, f3 + f2, i);
    }

    private void renderInstances(
        VertexConsumer p_377303_, List<WeatherEffectRenderer.ColumnInstance> p_364835_, Vec3 p_367411_, float p_360961_, int p_369839_, float p_363459_
    ) {
        for (WeatherEffectRenderer.ColumnInstance weathereffectrenderer$columninstance : p_364835_) {
            float f = (float)((double)weathereffectrenderer$columninstance.x + 0.5 - p_367411_.x);
            float f1 = (float)((double)weathereffectrenderer$columninstance.z + 0.5 - p_367411_.z);
            float f2 = (float)Mth.lengthSquared((double)f, (double)f1);
            float f3 = Mth.lerp(f2 / (float)(p_369839_ * p_369839_), p_360961_, 0.5F) * p_363459_;
            int i = ARGB.white(f3);
            int j = (weathereffectrenderer$columninstance.z - Mth.floor(p_367411_.z) + 16) * 32
                + weathereffectrenderer$columninstance.x
                - Mth.floor(p_367411_.x)
                + 16;
            float f4 = this.columnSizeX[j] / 2.0F;
            float f5 = this.columnSizeZ[j] / 2.0F;
            float f6 = f - f4;
            float f7 = f + f4;
            float f8 = (float)((double)weathereffectrenderer$columninstance.topY - p_367411_.y);
            float f9 = (float)((double)weathereffectrenderer$columninstance.bottomY - p_367411_.y);
            float f10 = f1 - f5;
            float f11 = f1 + f5;
            float f12 = weathereffectrenderer$columninstance.uOffset + 0.0F;
            float f13 = weathereffectrenderer$columninstance.uOffset + 1.0F;
            float f14 = (float)weathereffectrenderer$columninstance.bottomY * 0.25F + weathereffectrenderer$columninstance.vOffset;
            float f15 = (float)weathereffectrenderer$columninstance.topY * 0.25F + weathereffectrenderer$columninstance.vOffset;
            p_377303_.addVertex(f6, f8, f10).setUv(f12, f14).setColor(i).setLight(weathereffectrenderer$columninstance.lightCoords);
            p_377303_.addVertex(f7, f8, f11).setUv(f13, f14).setColor(i).setLight(weathereffectrenderer$columninstance.lightCoords);
            p_377303_.addVertex(f7, f9, f11).setUv(f13, f15).setColor(i).setLight(weathereffectrenderer$columninstance.lightCoords);
            p_377303_.addVertex(f6, f9, f10).setUv(f12, f15).setColor(i).setLight(weathereffectrenderer$columninstance.lightCoords);
        }
    }

    public void tickRainParticles(ClientLevel p_365121_, Camera p_364267_, int p_360728_, ParticleStatus p_367686_) {
        float f = p_365121_.getRainLevel(1.0F) / (Minecraft.useFancyGraphics() ? 1.0F : 2.0F);
        if (!(f <= 0.0F)) {
            RandomSource randomsource = RandomSource.create((long)p_360728_ * 312987231L);
            BlockPos blockpos = BlockPos.containing(p_364267_.getPosition());
            BlockPos blockpos1 = null;
            int i = (int)(100.0F * f * f) / (p_367686_ == ParticleStatus.DECREASED ? 2 : 1);

            for (int j = 0; j < i; j++) {
                int k = randomsource.nextInt(21) - 10;
                int l = randomsource.nextInt(21) - 10;
                BlockPos blockpos2 = p_365121_.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockpos.offset(k, 0, l));
                if (blockpos2.getY() > p_365121_.getMinY()
                    && blockpos2.getY() <= blockpos.getY() + 10
                    && blockpos2.getY() >= blockpos.getY() - 10
                    && this.getPrecipitationAt(p_365121_, blockpos2) == Biome.Precipitation.RAIN) {
                    blockpos1 = blockpos2.below();
                    if (p_367686_ == ParticleStatus.MINIMAL) {
                        break;
                    }

                    double d0 = randomsource.nextDouble();
                    double d1 = randomsource.nextDouble();
                    BlockState blockstate = p_365121_.getBlockState(blockpos1);
                    FluidState fluidstate = p_365121_.getFluidState(blockpos1);
                    VoxelShape voxelshape = blockstate.getCollisionShape(p_365121_, blockpos1);
                    double d2 = voxelshape.max(Direction.Axis.Y, d0, d1);
                    double d3 = (double)fluidstate.getHeight(p_365121_, blockpos1);
                    double d4 = Math.max(d2, d3);
                    ParticleOptions particleoptions = !fluidstate.is(FluidTags.LAVA)
                            && !blockstate.is(Blocks.MAGMA_BLOCK)
                            && !CampfireBlock.isLitCampfire(blockstate)
                        ? ParticleTypes.RAIN
                        : ParticleTypes.SMOKE;
                    p_365121_.addParticle(
                        particleoptions,
                        (double)blockpos1.getX() + d0,
                        (double)blockpos1.getY() + d4,
                        (double)blockpos1.getZ() + d1,
                        0.0,
                        0.0,
                        0.0
                    );
                }
            }

            if (blockpos1 != null && randomsource.nextInt(3) < this.rainSoundTime++) {
                this.rainSoundTime = 0;
                if (blockpos1.getY() > blockpos.getY() + 1
                    && p_365121_.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockpos).getY() > Mth.floor((float)blockpos.getY())) {
                    p_365121_.playLocalSound(blockpos1, SoundEvents.WEATHER_RAIN_ABOVE, SoundSource.WEATHER, 0.1F, 0.5F, false);
                } else {
                    p_365121_.playLocalSound(blockpos1, SoundEvents.WEATHER_RAIN, SoundSource.WEATHER, 0.2F, 1.0F, false);
                }
            }
        }
    }

    private Biome.Precipitation getPrecipitationAt(Level p_360760_, BlockPos p_361577_) {
        if (!p_360760_.getChunkSource().hasChunk(SectionPos.blockToSectionCoord(p_361577_.getX()), SectionPos.blockToSectionCoord(p_361577_.getZ()))) {
            return Biome.Precipitation.NONE;
        } else {
            Biome biome = p_360760_.getBiome(p_361577_).value();
            return biome.getPrecipitationAt(p_361577_, p_360760_.getSeaLevel());
        }
    }

    @OnlyIn(Dist.CLIENT)
    static record ColumnInstance(int x, int z, int bottomY, int topY, float uOffset, float vOffset, int lightCoords) {
    }
}