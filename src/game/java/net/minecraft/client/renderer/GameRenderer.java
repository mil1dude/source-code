package net.minecraft.client.renderer;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.Zone;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class GameRenderer implements AutoCloseable {
    private static final ResourceLocation BLUR_POST_CHAIN_ID = ResourceLocation.withDefaultNamespace("blur");
    public static final int MAX_BLUR_RADIUS = 10;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final boolean DEPTH_BUFFER_DEBUG = false;
    public static final float PROJECTION_Z_NEAR = 0.05F;
    private static final float GUI_Z_NEAR = 1000.0F;
    private final Minecraft minecraft;
    private final ResourceManager resourceManager;
    private final RandomSource random = RandomSource.create();
    private float renderDistance;
    public final ItemInHandRenderer itemInHandRenderer;
    private final RenderBuffers renderBuffers;
    private int confusionAnimationTick;
    private float fovModifier;
    private float oldFovModifier;
    private float darkenWorldAmount;
    private float darkenWorldAmountO;
    private boolean renderHand = true;
    private boolean renderBlockOutline = true;
    private long lastScreenshotAttempt;
    private boolean hasWorldScreenshot;
    private long lastActiveTime = Util.getMillis();
    private final LightTexture lightTexture;
    private final OverlayTexture overlayTexture = new OverlayTexture();
    private boolean panoramicMode;
    private float zoom = 1.0F;
    private float zoomX;
    private float zoomY;
    public static final int ITEM_ACTIVATION_ANIMATION_LENGTH = 40;
    @Nullable
    private ItemStack itemActivationItem;
    private int itemActivationTicks;
    private float itemActivationOffX;
    private float itemActivationOffY;
    private final CrossFrameResourcePool resourcePool = new CrossFrameResourcePool(3);
    @Nullable
    private ResourceLocation postEffectId;
    private boolean effectActive;
    private final Camera mainCamera = new Camera();

    public GameRenderer(Minecraft p_234219_, ItemInHandRenderer p_234220_, ResourceManager p_234221_, RenderBuffers p_234222_) {
        this.minecraft = p_234219_;
        this.resourceManager = p_234221_;
        this.itemInHandRenderer = p_234220_;
        this.lightTexture = new LightTexture(this, p_234219_);
        this.renderBuffers = p_234222_;
    }

    @Override
    public void close() {
        this.lightTexture.close();
        this.overlayTexture.close();
        this.resourcePool.close();
    }

    public void setRenderHand(boolean p_172737_) {
        this.renderHand = p_172737_;
    }

    public void setRenderBlockOutline(boolean p_172776_) {
        this.renderBlockOutline = p_172776_;
    }

    public void setPanoramicMode(boolean p_172780_) {
        this.panoramicMode = p_172780_;
    }

    public boolean isPanoramicMode() {
        return this.panoramicMode;
    }

    public void clearPostEffect() {
        this.postEffectId = null;
    }

    public void togglePostEffect() {
        this.effectActive = !this.effectActive;
    }

    public void checkEntityPostEffect(@Nullable Entity p_109107_) {
        this.postEffectId = null;
        if (p_109107_ instanceof Creeper) {
            this.setPostEffect(ResourceLocation.withDefaultNamespace("creeper"));
        } else if (p_109107_ instanceof Spider) {
            this.setPostEffect(ResourceLocation.withDefaultNamespace("spider"));
        } else if (p_109107_ instanceof EnderMan) {
            this.setPostEffect(ResourceLocation.withDefaultNamespace("invert"));
        }
    }

    private void setPostEffect(ResourceLocation p_367260_) {
        this.postEffectId = p_367260_;
        this.effectActive = true;
    }

    public void processBlurEffect() {
        float f = (float)this.minecraft.options.getMenuBackgroundBlurriness();
        if (!(f < 1.0F)) {
            PostChain postchain = this.minecraft.getShaderManager().getPostChain(BLUR_POST_CHAIN_ID, LevelTargetBundle.MAIN_TARGETS);
            if (postchain != null) {
                postchain.setUniform("Radius", f);
                postchain.process(this.minecraft.getMainRenderTarget(), this.resourcePool);
            }
        }
    }

    public void preloadUiShader(ResourceProvider p_172723_) {
        try {
            this.minecraft.getShaderManager().preloadForStartup(p_172723_, CoreShaders.RENDERTYPE_GUI, CoreShaders.RENDERTYPE_GUI_OVERLAY, CoreShaders.POSITION_TEX_COLOR);
        } catch (ShaderManager.CompilationException | IOException ioexception) {
            throw new RuntimeException("Could not preload shaders for loading UI", ioexception);
        }
    }

    public void tick() {
        this.tickFov();
        this.lightTexture.tick();
        if (this.minecraft.getCameraEntity() == null) {
            this.minecraft.setCameraEntity(this.minecraft.player);
        }

        this.mainCamera.tick();
        this.itemInHandRenderer.tick();
        this.confusionAnimationTick++;
        if (this.minecraft.level.tickRateManager().runsNormally()) {
            this.minecraft.levelRenderer.tickParticles(this.mainCamera);
            this.darkenWorldAmountO = this.darkenWorldAmount;
            if (this.minecraft.gui.getBossOverlay().shouldDarkenScreen()) {
                this.darkenWorldAmount += 0.05F;
                if (this.darkenWorldAmount > 1.0F) {
                    this.darkenWorldAmount = 1.0F;
                }
            } else if (this.darkenWorldAmount > 0.0F) {
                this.darkenWorldAmount -= 0.0125F;
            }

            if (this.itemActivationTicks > 0) {
                this.itemActivationTicks--;
                if (this.itemActivationTicks == 0) {
                    this.itemActivationItem = null;
                }
            }
        }
    }

    @Nullable
    public ResourceLocation currentPostEffect() {
        return this.postEffectId;
    }

    public void resize(int p_109098_, int p_109099_) {
        this.resourcePool.clear();
        this.minecraft.levelRenderer.resize(p_109098_, p_109099_);
    }

    public void pick(float p_109088_) {
        Entity entity = this.minecraft.getCameraEntity();
        if (entity != null) {
            if (this.minecraft.level != null && this.minecraft.player != null) {
                Profiler.get().push("pick");
                double d0 = this.minecraft.player.blockInteractionRange();
                double d1 = this.minecraft.player.entityInteractionRange();
                HitResult hitresult = this.pick(entity, d0, d1, p_109088_);
                this.minecraft.hitResult = hitresult;
                this.minecraft.crosshairPickEntity = hitresult instanceof EntityHitResult entityhitresult ? entityhitresult.getEntity() : null;
                Profiler.get().pop();
            }
        }
    }

    private HitResult pick(Entity p_328026_, double p_328198_, double p_332188_, float p_336363_) {
        double d0 = Math.max(p_328198_, p_332188_);
        double d1 = Mth.square(d0);
        Vec3 vec3 = p_328026_.getEyePosition(p_336363_);
        HitResult hitresult = p_328026_.pick(d0, p_336363_, false);
        double d2 = hitresult.getLocation().distanceToSqr(vec3);
        if (hitresult.getType() != HitResult.Type.MISS) {
            d1 = d2;
            d0 = Math.sqrt(d2);
        }

        Vec3 vec31 = p_328026_.getViewVector(p_336363_);
        Vec3 vec32 = vec3.add(vec31.x * d0, vec31.y * d0, vec31.z * d0);
        float f = 1.0F;
        AABB aabb = p_328026_.getBoundingBox().expandTowards(vec31.scale(d0)).inflate(1.0, 1.0, 1.0);
        EntityHitResult entityhitresult = ProjectileUtil.getEntityHitResult(p_328026_, vec3, vec32, aabb, EntitySelector.CAN_BE_PICKED, d1);
        return entityhitresult != null && entityhitresult.getLocation().distanceToSqr(vec3) < d2
            ? filterHitResult(entityhitresult, vec3, p_332188_)
            : filterHitResult(hitresult, vec3, p_328198_);
    }

    private static HitResult filterHitResult(HitResult p_333216_, Vec3 p_336005_, double p_334240_) {
        Vec3 vec3 = p_333216_.getLocation();
        if (!vec3.closerThan(p_336005_, p_334240_)) {
            Vec3 vec31 = p_333216_.getLocation();
            Direction direction = Direction.getApproximateNearest(
                vec31.x - p_336005_.x, vec31.y - p_336005_.y, vec31.z - p_336005_.z
            );
            return BlockHitResult.miss(vec31, direction, BlockPos.containing(vec31));
        } else {
            return p_333216_;
        }
    }

    private void tickFov() {
        float f;
        if (this.minecraft.getCameraEntity() instanceof AbstractClientPlayer abstractclientplayer) {
            Options options = this.minecraft.options;
            boolean flag = options.getCameraType().isFirstPerson();
            float f1 = options.fovEffectScale().get().floatValue();
            f = abstractclientplayer.getFieldOfViewModifier(flag, f1);
        } else {
            f = 1.0F;
        }

        this.oldFovModifier = this.fovModifier;
        this.fovModifier = this.fovModifier + (f - this.fovModifier) * 0.5F;
        this.fovModifier = Mth.clamp(this.fovModifier, 0.1F, 1.5F);
    }

    private float getFov(Camera p_109142_, float p_109143_, boolean p_109144_) {
        if (this.panoramicMode) {
            return 90.0F;
        } else {
            float f = 70.0F;
            if (p_109144_) {
                f = (float)this.minecraft.options.fov().get().intValue();
                f *= Mth.lerp(p_109143_, this.oldFovModifier, this.fovModifier);
            }

            if (p_109142_.getEntity() instanceof LivingEntity livingentity && livingentity.isDeadOrDying()) {
                float f1 = Math.min((float)livingentity.deathTime + p_109143_, 20.0F);
                f /= (1.0F - 500.0F / (f1 + 500.0F)) * 2.0F + 1.0F;
            }

            FogType fogtype = p_109142_.getFluidInCamera();
            if (fogtype == FogType.LAVA || fogtype == FogType.WATER) {
                float f2 = this.minecraft.options.fovEffectScale().get().floatValue();
                f *= Mth.lerp(f2, 1.0F, 0.85714287F);
            }

            return f;
        }
    }

    private void bobHurt(PoseStack p_109118_, float p_109119_) {
        if (this.minecraft.getCameraEntity() instanceof LivingEntity livingentity) {
            float f2 = (float)livingentity.hurtTime - p_109119_;
            if (livingentity.isDeadOrDying()) {
                float f = Math.min((float)livingentity.deathTime + p_109119_, 20.0F);
                p_109118_.mulPose(Axis.ZP.rotationDegrees(40.0F - 8000.0F / (f + 200.0F)));
            }

            if (f2 < 0.0F) {
                return;
            }

            f2 /= (float)livingentity.hurtDuration;
            f2 = Mth.sin(f2 * f2 * f2 * f2 * (float) Math.PI);
            float f3 = livingentity.getHurtDir();
            p_109118_.mulPose(Axis.YP.rotationDegrees(-f3));
            float f1 = (float)((double)(-f2) * 14.0 * this.minecraft.options.damageTiltStrength().get());
            p_109118_.mulPose(Axis.ZP.rotationDegrees(f1));
            p_109118_.mulPose(Axis.YP.rotationDegrees(f3));
        }
    }

    private void bobView(PoseStack p_109139_, float p_109140_) {
        if (this.minecraft.getCameraEntity() instanceof AbstractClientPlayer abstractclientplayer) {
            float f2 = abstractclientplayer.walkDist - abstractclientplayer.walkDistO;
            float $$5 = -(abstractclientplayer.walkDist + f2 * p_109140_);
            float $$6 = Mth.lerp(p_109140_, abstractclientplayer.oBob, abstractclientplayer.bob);
            p_109139_.translate(Mth.sin($$5 * (float) Math.PI) * $$6 * 0.5F, -Math.abs(Mth.cos($$5 * (float) Math.PI) * $$6), 0.0F);
            p_109139_.mulPose(Axis.ZP.rotationDegrees(Mth.sin($$5 * (float) Math.PI) * $$6 * 3.0F));
            p_109139_.mulPose(Axis.XP.rotationDegrees(Math.abs(Mth.cos($$5 * (float) Math.PI - 0.2F) * $$6) * 5.0F));
        }
    }

    public void renderZoomed(float p_172719_, float p_172720_, float p_172721_) {
        this.zoom = p_172719_;
        this.zoomX = p_172720_;
        this.zoomY = p_172721_;
        this.setRenderBlockOutline(false);
        this.setRenderHand(false);
        this.renderLevel(DeltaTracker.ZERO);
        this.zoom = 1.0F;
    }

    private void renderItemInHand(Camera p_109122_, float p_109123_, Matrix4f p_331664_) {
        if (!this.panoramicMode) {
            Matrix4f matrix4f = this.getProjectionMatrix(this.getFov(p_109122_, p_109123_, false));
            RenderSystem.setProjectionMatrix(matrix4f, ProjectionType.PERSPECTIVE);
            PoseStack posestack = new PoseStack();
            posestack.pushPose();
            posestack.mulPose(p_331664_.invert(new Matrix4f()));
            Matrix4fStack matrix4fstack = RenderSystem.getModelViewStack();
            matrix4fstack.pushMatrix().mul(p_331664_);
            this.bobHurt(posestack, p_109123_);
            if (this.minecraft.options.bobView().get()) {
                this.bobView(posestack, p_109123_);
            }

            boolean flag = this.minecraft.getCameraEntity() instanceof LivingEntity && ((LivingEntity)this.minecraft.getCameraEntity()).isSleeping();
            if (this.minecraft.options.getCameraType().isFirstPerson()
                && !flag
                && !this.minecraft.options.hideGui
                && this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR) {
                this.lightTexture.turnOnLightLayer();
                this.itemInHandRenderer
                    .renderHandsWithItems(
                        p_109123_,
                        posestack,
                        this.renderBuffers.bufferSource(),
                        this.minecraft.player,
                        this.minecraft.getEntityRenderDispatcher().getPackedLightCoords(this.minecraft.player, p_109123_)
                    );
                this.lightTexture.turnOffLightLayer();
            }

            matrix4fstack.popMatrix();
            posestack.popPose();
            if (this.minecraft.options.getCameraType().isFirstPerson() && !flag) {
                MultiBufferSource.BufferSource multibuffersource$buffersource = this.renderBuffers.bufferSource();
                ScreenEffectRenderer.renderScreenEffect(this.minecraft, posestack, multibuffersource$buffersource);
                multibuffersource$buffersource.endBatch();
            }
        }
    }

    public Matrix4f getProjectionMatrix(float p_364788_) {
        Matrix4f matrix4f = new Matrix4f();
        if (this.zoom != 1.0F) {
            matrix4f.translate(this.zoomX, -this.zoomY, 0.0F);
            matrix4f.scale(this.zoom, this.zoom, 1.0F);
        }

        return matrix4f.perspective(
            p_364788_ * (float) (Math.PI / 180.0),
            (float)this.minecraft.getWindow().getWidth() / (float)this.minecraft.getWindow().getHeight(),
            0.05F,
            this.getDepthFar()
        );
    }

    public float getDepthFar() {
        return this.renderDistance * 4.0F;
    }

    public static float getNightVisionScale(LivingEntity p_109109_, float p_109110_) {
        MobEffectInstance mobeffectinstance = p_109109_.getEffect(MobEffects.NIGHT_VISION);
        return !mobeffectinstance.endsWithin(200)
            ? 1.0F
            : 0.7F + Mth.sin(((float)mobeffectinstance.getDuration() - p_109110_) * (float) Math.PI * 0.2F) * 0.3F;
    }

    public void render(DeltaTracker p_343467_, boolean p_109096_) {
        if (!this.minecraft.isWindowActive()
            && this.minecraft.options.pauseOnLostFocus
            && (!this.minecraft.options.touchscreen().get() || !this.minecraft.mouseHandler.isRightPressed())) {
            if (Util.getMillis() - this.lastActiveTime > 500L) {
                this.minecraft.pauseGame(false);
            }
        } else {
            this.lastActiveTime = Util.getMillis();
        }

        if (!this.minecraft.noRender) {
            ProfilerFiller profilerfiller = Profiler.get();
            boolean flag = this.minecraft.isGameLoadFinished();
            int i = (int)(this.minecraft.mouseHandler.xpos() * (double)this.minecraft.getWindow().getGuiScaledWidth() / (double)this.minecraft.getWindow().getScreenWidth());
            int j = (int)(this.minecraft.mouseHandler.ypos() * (double)this.minecraft.getWindow().getGuiScaledHeight() / (double)this.minecraft.getWindow().getScreenHeight());
            RenderSystem.viewport(0, 0, this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
            if (flag && p_109096_ && this.minecraft.level != null) {
                profilerfiller.push("level");
                this.renderLevel(p_343467_);
                this.tryTakeScreenshotIfNeeded();
                this.minecraft.levelRenderer.doEntityOutline();
                if (this.postEffectId != null && this.effectActive) {
                    RenderSystem.disableBlend();
                    RenderSystem.disableDepthTest();
                    RenderSystem.resetTextureMatrix();
                    PostChain postchain = this.minecraft.getShaderManager().getPostChain(this.postEffectId, LevelTargetBundle.MAIN_TARGETS);
                    if (postchain != null) {
                        postchain.process(this.minecraft.getMainRenderTarget(), this.resourcePool);
                    }
                }

                this.minecraft.getMainRenderTarget().bindWrite(true);
            }

            Window window = this.minecraft.getWindow();
            RenderSystem.clear(256);
            Matrix4f matrix4f = new Matrix4f()
                .setOrtho(
                    0.0F,
                    (float)((double)window.getWidth() / window.getGuiScale()),
                    (float)((double)window.getHeight() / window.getGuiScale()),
                    0.0F,
                    1000.0F,
                    21000.0F
                );
            RenderSystem.setProjectionMatrix(matrix4f, ProjectionType.ORTHOGRAPHIC);
            Matrix4fStack matrix4fstack = RenderSystem.getModelViewStack();
            matrix4fstack.pushMatrix();
            matrix4fstack.translation(0.0F, 0.0F, -11000.0F);
            Lighting.setupFor3DItems();
            GuiGraphics guigraphics = new GuiGraphics(this.minecraft, this.renderBuffers.bufferSource());
            if (flag && p_109096_ && this.minecraft.level != null) {
                profilerfiller.popPush("gui");
                if (!this.minecraft.options.hideGui) {
                    this.renderItemActivationAnimation(guigraphics, p_343467_.getGameTimeDeltaPartialTick(false));
                }

                this.minecraft.gui.render(guigraphics, p_343467_);
                guigraphics.flush();
                RenderSystem.clear(256);
                profilerfiller.pop();
            }

            if (this.minecraft.getOverlay() != null) {
                try {
                    this.minecraft.getOverlay().render(guigraphics, i, j, p_343467_.getGameTimeDeltaTicks());
                } catch (Throwable throwable2) {
                    CrashReport crashreport = CrashReport.forThrowable(throwable2, "Rendering overlay");
                    CrashReportCategory crashreportcategory = crashreport.addCategory("Overlay render details");
                    crashreportcategory.setDetail("Overlay name", () -> this.minecraft.getOverlay().getClass().getCanonicalName());
                    throw new ReportedException(crashreport);
                }
            } else if (flag && this.minecraft.screen != null) {
                try {
                    this.minecraft.screen.renderWithTooltip(guigraphics, i, j, p_343467_.getGameTimeDeltaTicks());
                } catch (Throwable throwable1) {
                    CrashReport crashreport1 = CrashReport.forThrowable(throwable1, "Rendering screen");
                    CrashReportCategory crashreportcategory1 = crashreport1.addCategory("Screen render details");
                    crashreportcategory1.setDetail("Screen name", () -> this.minecraft.screen.getClass().getCanonicalName());
                    crashreportcategory1.setDetail(
                        "Mouse location",
                        () -> String.format(
                                Locale.ROOT,
                                "Scaled: (%d, %d). Absolute: (%f, %f)",
                                i,
                                j,
                                this.minecraft.mouseHandler.xpos(),
                                this.minecraft.mouseHandler.ypos()
                            )
                    );
                    crashreportcategory1.setDetail(
                        "Screen size",
                        () -> String.format(
                                Locale.ROOT,
                                "Scaled: (%d, %d). Absolute: (%d, %d). Scale factor of %f",
                                this.minecraft.getWindow().getGuiScaledWidth(),
                                this.minecraft.getWindow().getGuiScaledHeight(),
                                this.minecraft.getWindow().getWidth(),
                                this.minecraft.getWindow().getHeight(),
                                this.minecraft.getWindow().getGuiScale()
                            )
                    );
                    throw new ReportedException(crashreport1);
                }

                try {
                    if (this.minecraft.screen != null) {
                        this.minecraft.screen.handleDelayedNarration();
                    }
                } catch (Throwable throwable) {
                    CrashReport crashreport2 = CrashReport.forThrowable(throwable, "Narrating screen");
                    CrashReportCategory crashreportcategory2 = crashreport2.addCategory("Screen details");
                    crashreportcategory2.setDetail("Screen name", () -> this.minecraft.screen.getClass().getCanonicalName());
                    throw new ReportedException(crashreport2);
                }
            }

            if (flag && p_109096_ && this.minecraft.level != null) {
                this.minecraft.gui.renderSavingIndicator(guigraphics, p_343467_);
            }

            if (flag) {
                try (Zone zone = profilerfiller.zone("toasts")) {
                    this.minecraft.getToastManager().render(guigraphics);
                }
            }

            guigraphics.flush();
            matrix4fstack.popMatrix();
            this.resourcePool.endFrame();
        }
    }

    private void tryTakeScreenshotIfNeeded() {
        if (!this.hasWorldScreenshot && this.minecraft.isLocalServer()) {
            long i = Util.getMillis();
            if (i - this.lastScreenshotAttempt >= 1000L) {
                this.lastScreenshotAttempt = i;
                IntegratedServer integratedserver = this.minecraft.getSingleplayerServer();
                if (integratedserver != null && !integratedserver.isStopped()) {
                    integratedserver.getWorldScreenshotFile().ifPresent(p_234239_ -> {
                        if (Files.isRegularFile(p_234239_)) {
                            this.hasWorldScreenshot = true;
                        } else {
                            this.takeAutoScreenshot(p_234239_);
                        }
                    });
                }
            }
        }
    }

    private void takeAutoScreenshot(Path p_182643_) {
        if (this.minecraft.levelRenderer.countRenderedSections() > 10 && this.minecraft.levelRenderer.hasRenderedAllSections()) {
            NativeImage nativeimage = Screenshot.takeScreenshot(this.minecraft.getMainRenderTarget());
            Util.ioPool().execute(() -> {
                int i = nativeimage.getWidth();
                int j = nativeimage.getHeight();
                int k = 0;
                int l = 0;
                if (i > j) {
                    k = (i - j) / 2;
                    i = j;
                } else {
                    l = (j - i) / 2;
                    j = i;
                }

                try (NativeImage nativeimage1 = new NativeImage(64, 64, false)) {
                    nativeimage.resizeSubRectTo(k, l, i, j, nativeimage1);
                    nativeimage1.writeToFile(p_182643_);
                } catch (IOException ioexception) {
                    LOGGER.warn("Couldn't save auto screenshot", (Throwable)ioexception);
                } finally {
                    nativeimage.close();
                }
            });
        }
    }

    private boolean shouldRenderBlockOutline() {
        if (!this.renderBlockOutline) {
            return false;
        } else {
            Entity entity = this.minecraft.getCameraEntity();
            boolean flag = entity instanceof Player && !this.minecraft.options.hideGui;
            if (flag && !((Player)entity).getAbilities().mayBuild) {
                ItemStack itemstack = ((LivingEntity)entity).getMainHandItem();
                HitResult hitresult = this.minecraft.hitResult;
                if (hitresult != null && hitresult.getType() == HitResult.Type.BLOCK) {
                    BlockPos blockpos = ((BlockHitResult)hitresult).getBlockPos();
                    BlockState blockstate = this.minecraft.level.getBlockState(blockpos);
                    if (this.minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR) {
                        flag = blockstate.getMenuProvider(this.minecraft.level, blockpos) != null;
                    } else {
                        BlockInWorld blockinworld = new BlockInWorld(this.minecraft.level, blockpos, false);
                        Registry<Block> registry = this.minecraft.level.registryAccess().lookupOrThrow(Registries.BLOCK);
                        flag = !itemstack.isEmpty() && (itemstack.canBreakBlockInAdventureMode(blockinworld) || itemstack.canPlaceOnBlockInAdventureMode(blockinworld));
                    }
                }
            }

            return flag;
        }
    }

    public void renderLevel(DeltaTracker p_342230_) {
        float f = p_342230_.getGameTimeDeltaPartialTick(true);
        this.lightTexture.updateLightTexture(f);
        if (this.minecraft.getCameraEntity() == null) {
            this.minecraft.setCameraEntity(this.minecraft.player);
        }

        this.pick(f);
        ProfilerFiller profilerfiller = Profiler.get();
        profilerfiller.push("center");
        boolean flag = this.shouldRenderBlockOutline();
        profilerfiller.popPush("camera");
        Camera camera = this.mainCamera;
        Entity entity = (Entity)(this.minecraft.getCameraEntity() == null ? this.minecraft.player : this.minecraft.getCameraEntity());
        float f1 = this.minecraft.level.tickRateManager().isEntityFrozen(entity) ? 1.0F : f;
        camera.setup(this.minecraft.level, entity, !this.minecraft.options.getCameraType().isFirstPerson(), this.minecraft.options.getCameraType().isMirrored(), f1);
        this.renderDistance = (float)(this.minecraft.options.getEffectiveRenderDistance() * 16);
        float f2 = this.getFov(camera, f, true);
        Matrix4f matrix4f = this.getProjectionMatrix(f2);
        PoseStack posestack = new PoseStack();
        this.bobHurt(posestack, camera.getPartialTickTime());
        if (this.minecraft.options.bobView().get()) {
            this.bobView(posestack, camera.getPartialTickTime());
        }

        matrix4f.mul(posestack.last().pose());
        float f3 = this.minecraft.options.screenEffectScale().get().floatValue();
        float f4 = Mth.lerp(f, this.minecraft.player.oSpinningEffectIntensity, this.minecraft.player.spinningEffectIntensity) * f3 * f3;
        if (f4 > 0.0F) {
            int i = this.minecraft.player.hasEffect(MobEffects.CONFUSION) ? 7 : 20;
            float f5 = 5.0F / (f4 * f4 + 5.0F) - f4 * 0.04F;
            f5 *= f5;
            Vector3f vector3f = new Vector3f(0.0F, Mth.SQRT_OF_TWO / 2.0F, Mth.SQRT_OF_TWO / 2.0F);
            float f6 = ((float)this.confusionAnimationTick + f) * (float)i * (float) (Math.PI / 180.0);
            matrix4f.rotate(f6, vector3f);
            matrix4f.scale(1.0F / f5, 1.0F, 1.0F);
            matrix4f.rotate(-f6, vector3f);
        }

        float f7 = Math.max(f2, (float)this.minecraft.options.fov().get().intValue());
        Matrix4f matrix4f1 = this.getProjectionMatrix(f7);
        RenderSystem.setProjectionMatrix(matrix4f, ProjectionType.PERSPECTIVE);
        Quaternionf quaternionf = camera.rotation().conjugate(new Quaternionf());
        Matrix4f matrix4f2 = new Matrix4f().rotation(quaternionf);
        this.minecraft.levelRenderer.prepareCullFrustum(camera.getPosition(), matrix4f2, matrix4f1);
        this.minecraft.getMainRenderTarget().bindWrite(true);
        this.minecraft.levelRenderer.renderLevel(this.resourcePool, p_342230_, flag, camera, this, matrix4f2, matrix4f);
        profilerfiller.popPush("hand");
        if (this.renderHand) {
            RenderSystem.clear(256);
            this.renderItemInHand(camera, f, matrix4f2);
        }

        profilerfiller.pop();
    }

    public void resetData() {
        this.itemActivationItem = null;
        this.minecraft.getMapTextureManager().resetData();
        this.mainCamera.reset();
        this.hasWorldScreenshot = false;
    }

    public void displayItemActivation(ItemStack p_109114_) {
        this.itemActivationItem = p_109114_;
        this.itemActivationTicks = 40;
        this.itemActivationOffX = this.random.nextFloat() * 2.0F - 1.0F;
        this.itemActivationOffY = this.random.nextFloat() * 2.0F - 1.0F;
    }

    private void renderItemActivationAnimation(GuiGraphics p_342383_, float p_109103_) {
        if (this.itemActivationItem != null && this.itemActivationTicks > 0) {
            int i = 40 - this.itemActivationTicks;
            float f = ((float)i + p_109103_) / 40.0F;
            float f1 = f * f;
            float f2 = f * f1;
            float f3 = 10.25F * f2 * f1 - 24.95F * f1 * f1 + 25.5F * f2 - 13.8F * f1 + 4.0F * f;
            float f4 = f3 * (float) Math.PI;
            float f5 = this.itemActivationOffX * (float)(p_342383_.guiWidth() / 4);
            float f6 = this.itemActivationOffY * (float)(p_342383_.guiHeight() / 4);
            PoseStack posestack = p_342383_.pose();
            posestack.pushPose();
            posestack.translate(
                (float)(p_342383_.guiWidth() / 2) + f5 * Mth.abs(Mth.sin(f4 * 2.0F)),
                (float)(p_342383_.guiHeight() / 2) + f6 * Mth.abs(Mth.sin(f4 * 2.0F)),
                -50.0F
            );
            float f7 = 50.0F + 175.0F * Mth.sin(f4);
            posestack.scale(f7, -f7, f7);
            posestack.mulPose(Axis.YP.rotationDegrees(900.0F * Mth.abs(Mth.sin(f4))));
            posestack.mulPose(Axis.XP.rotationDegrees(6.0F * Mth.cos(f * 8.0F)));
            posestack.mulPose(Axis.ZP.rotationDegrees(6.0F * Mth.cos(f * 8.0F)));
            p_342383_.drawSpecial(
                p_357824_ -> this.minecraft
                        .getItemRenderer()
                        .renderStatic(
                            this.itemActivationItem, ItemDisplayContext.FIXED, 15728880, OverlayTexture.NO_OVERLAY, posestack, p_357824_, this.minecraft.level, 0
                        )
            );
            posestack.popPose();
        }
    }

    public Minecraft getMinecraft() {
        return this.minecraft;
    }

    public float getDarkenWorldAmount(float p_109132_) {
        return Mth.lerp(p_109132_, this.darkenWorldAmountO, this.darkenWorldAmount);
    }

    public float getRenderDistance() {
        return this.renderDistance;
    }

    public Camera getMainCamera() {
        return this.mainCamera;
    }

    public LightTexture lightTexture() {
        return this.lightTexture;
    }

    public OverlayTexture overlayTexture() {
        return this.overlayTexture;
    }
}