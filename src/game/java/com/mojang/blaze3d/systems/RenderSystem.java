package com.mojang.blaze3d.systems;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.DontObfuscate;
import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.TracyFrameCapture;
import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderCall;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CompiledShaderProgram;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.ShaderProgram;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.TimeSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
@DontObfuscate
public class RenderSystem {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final ConcurrentLinkedQueue<RenderCall> recordingQueue = Queues.newConcurrentLinkedQueue();
    private static final Tesselator RENDER_THREAD_TESSELATOR = new Tesselator(1536);
    private static final int MINIMUM_ATLAS_TEXTURE_SIZE = 1024;
    @Nullable
    private static Thread renderThread;
    private static int MAX_SUPPORTED_TEXTURE_SIZE = -1;
    private static boolean isInInit;
    private static double lastDrawTime = Double.MIN_VALUE;
    private static final RenderSystem.AutoStorageIndexBuffer sharedSequential = new RenderSystem.AutoStorageIndexBuffer(1, 1, IntConsumer::accept);
    private static final RenderSystem.AutoStorageIndexBuffer sharedSequentialQuad = new RenderSystem.AutoStorageIndexBuffer(4, 6, (p_157398_, p_157399_) -> {
        p_157398_.accept(p_157399_ + 0);
        p_157398_.accept(p_157399_ + 1);
        p_157398_.accept(p_157399_ + 2);
        p_157398_.accept(p_157399_ + 2);
        p_157398_.accept(p_157399_ + 3);
        p_157398_.accept(p_157399_ + 0);
    });
    private static final RenderSystem.AutoStorageIndexBuffer sharedSequentialLines = new RenderSystem.AutoStorageIndexBuffer(4, 6, (p_157401_, p_157402_) -> {
        p_157401_.accept(p_157402_ + 0);
        p_157401_.accept(p_157402_ + 1);
        p_157401_.accept(p_157402_ + 2);
        p_157401_.accept(p_157402_ + 3);
        p_157401_.accept(p_157402_ + 2);
        p_157401_.accept(p_157402_ + 1);
    });
    private static Matrix4f projectionMatrix = new Matrix4f();
    private static Matrix4f savedProjectionMatrix = new Matrix4f();
    private static ProjectionType projectionType = ProjectionType.PERSPECTIVE;
    private static ProjectionType savedProjectionType = ProjectionType.PERSPECTIVE;
    private static final Matrix4fStack modelViewStack = new Matrix4fStack(16);
    private static Matrix4f textureMatrix = new Matrix4f();
    private static final int[] shaderTextures = new int[12];
    private static final float[] shaderColor = new float[]{1.0F, 1.0F, 1.0F, 1.0F};
    private static float shaderGlintAlpha = 1.0F;
    private static FogParameters shaderFog = FogParameters.NO_FOG;
    private static final Vector3f[] shaderLightDirections = new Vector3f[2];
    private static float shaderGameTime;
    private static float shaderLineWidth = 1.0F;
    private static String apiDescription = "Unknown";
    @Nullable
    private static CompiledShaderProgram shader;
    private static final AtomicLong pollEventsWaitStart = new AtomicLong();
    private static final AtomicBoolean pollingEvents = new AtomicBoolean(false);

    public static void initRenderThread() {
        if (renderThread != null) {
            throw new IllegalStateException("Could not initialize render thread");
        } else {
            renderThread = Thread.currentThread();
        }
    }

    public static boolean isOnRenderThread() {
        return Thread.currentThread() == renderThread;
    }

    public static boolean isOnRenderThreadOrInit() {
        return isInInit || isOnRenderThread();
    }

    public static void assertOnRenderThreadOrInit() {
        if (!isInInit && !isOnRenderThread()) {
            throw constructThreadException();
        }
    }

    public static void assertOnRenderThread() {
        if (!isOnRenderThread()) {
            throw constructThreadException();
        }
    }

    private static IllegalStateException constructThreadException() {
        return new IllegalStateException("Rendersystem called from wrong thread");
    }

    public static void recordRenderCall(RenderCall p_69880_) {
        recordingQueue.add(p_69880_);
    }

    private static void pollEvents() {
        pollEventsWaitStart.set(Util.getMillis());
        pollingEvents.set(true);
        GLFW.glfwPollEvents();
        pollingEvents.set(false);
    }

    public static boolean isFrozenAtPollEvents() {
        return pollingEvents.get() && Util.getMillis() - pollEventsWaitStart.get() > 200L;
    }

    public static void flipFrame(long p_69496_, @Nullable TracyFrameCapture p_365037_) {
        pollEvents();
        replayQueue();
        Tesselator.getInstance().clear();
        GLFW.glfwSwapBuffers(p_69496_);
        if (p_365037_ != null) {
            p_365037_.endFrame();
        }

        pollEvents();
    }

    public static void replayQueue() {
        while (!recordingQueue.isEmpty()) {
            RenderCall rendercall = recordingQueue.poll();
            rendercall.execute();
        }
    }

    public static void limitDisplayFPS(int p_69831_) {
        double d0 = lastDrawTime + 1.0 / (double)p_69831_;

        double d1;
        for (d1 = GLFW.glfwGetTime(); d1 < d0; d1 = GLFW.glfwGetTime()) {
            GLFW.glfwWaitEventsTimeout(d0 - d1);
        }

        lastDrawTime = d1;
    }

    public static void disableDepthTest() {
        assertOnRenderThread();
        GlStateManager._disableDepthTest();
    }

    public static void enableDepthTest() {
        GlStateManager._enableDepthTest();
    }

    public static void enableScissor(int p_69489_, int p_69490_, int p_69491_, int p_69492_) {
        GlStateManager._enableScissorTest();
        GlStateManager._scissorBox(p_69489_, p_69490_, p_69491_, p_69492_);
    }

    public static void disableScissor() {
        GlStateManager._disableScissorTest();
    }

    public static void depthFunc(int p_69457_) {
        assertOnRenderThread();
        GlStateManager._depthFunc(p_69457_);
    }

    public static void depthMask(boolean p_69459_) {
        assertOnRenderThread();
        GlStateManager._depthMask(p_69459_);
    }

    public static void enableBlend() {
        assertOnRenderThread();
        GlStateManager._enableBlend();
    }

    public static void disableBlend() {
        assertOnRenderThread();
        GlStateManager._disableBlend();
    }

    public static void blendFunc(GlStateManager.SourceFactor p_69409_, GlStateManager.DestFactor p_69410_) {
        assertOnRenderThread();
        GlStateManager._blendFunc(p_69409_.value, p_69410_.value);
    }

    public static void blendFunc(int p_69406_, int p_69407_) {
        assertOnRenderThread();
        GlStateManager._blendFunc(p_69406_, p_69407_);
    }

    public static void blendFuncSeparate(
        GlStateManager.SourceFactor p_69417_, GlStateManager.DestFactor p_69418_, GlStateManager.SourceFactor p_69419_, GlStateManager.DestFactor p_69420_
    ) {
        assertOnRenderThread();
        GlStateManager._blendFuncSeparate(p_69417_.value, p_69418_.value, p_69419_.value, p_69420_.value);
    }

    public static void blendFuncSeparate(int p_69412_, int p_69413_, int p_69414_, int p_69415_) {
        assertOnRenderThread();
        GlStateManager._blendFuncSeparate(p_69412_, p_69413_, p_69414_, p_69415_);
    }

    public static void blendEquation(int p_69404_) {
        assertOnRenderThread();
        GlStateManager._blendEquation(p_69404_);
    }

    public static void enableCull() {
        assertOnRenderThread();
        GlStateManager._enableCull();
    }

    public static void disableCull() {
        assertOnRenderThread();
        GlStateManager._disableCull();
    }

    public static void polygonMode(int p_69861_, int p_69862_) {
        assertOnRenderThread();
        GlStateManager._polygonMode(p_69861_, p_69862_);
    }

    public static void enablePolygonOffset() {
        assertOnRenderThread();
        GlStateManager._enablePolygonOffset();
    }

    public static void disablePolygonOffset() {
        assertOnRenderThread();
        GlStateManager._disablePolygonOffset();
    }

    public static void polygonOffset(float p_69864_, float p_69865_) {
        assertOnRenderThread();
        GlStateManager._polygonOffset(p_69864_, p_69865_);
    }

    public static void enableColorLogicOp() {
        assertOnRenderThread();
        GlStateManager._enableColorLogicOp();
    }

    public static void disableColorLogicOp() {
        assertOnRenderThread();
        GlStateManager._disableColorLogicOp();
    }

    public static void logicOp(GlStateManager.LogicOp p_69836_) {
        assertOnRenderThread();
        GlStateManager._logicOp(p_69836_.value);
    }

    public static void activeTexture(int p_69389_) {
        assertOnRenderThread();
        GlStateManager._activeTexture(p_69389_);
    }

    public static void texParameter(int p_69938_, int p_69939_, int p_69940_) {
        GlStateManager._texParameter(p_69938_, p_69939_, p_69940_);
    }

    public static void deleteTexture(int p_69455_) {
        GlStateManager._deleteTexture(p_69455_);
    }

    public static void bindTextureForSetup(int p_157185_) {
        bindTexture(p_157185_);
    }

    public static void bindTexture(int p_69397_) {
        GlStateManager._bindTexture(p_69397_);
    }

    public static void viewport(int p_69950_, int p_69951_, int p_69952_, int p_69953_) {
        GlStateManager._viewport(p_69950_, p_69951_, p_69952_, p_69953_);
    }

    public static void colorMask(boolean p_69445_, boolean p_69446_, boolean p_69447_, boolean p_69448_) {
        assertOnRenderThread();
        GlStateManager._colorMask(p_69445_, p_69446_, p_69447_, p_69448_);
    }

    public static void stencilFunc(int p_69926_, int p_69927_, int p_69928_) {
        assertOnRenderThread();
        GlStateManager._stencilFunc(p_69926_, p_69927_, p_69928_);
    }

    public static void stencilMask(int p_69930_) {
        assertOnRenderThread();
        GlStateManager._stencilMask(p_69930_);
    }

    public static void stencilOp(int p_69932_, int p_69933_, int p_69934_) {
        assertOnRenderThread();
        GlStateManager._stencilOp(p_69932_, p_69933_, p_69934_);
    }

    public static void clearDepth(double p_69431_) {
        GlStateManager._clearDepth(p_69431_);
    }

    public static void clearColor(float p_69425_, float p_69426_, float p_69427_, float p_69428_) {
        GlStateManager._clearColor(p_69425_, p_69426_, p_69427_, p_69428_);
    }

    public static void clearStencil(int p_69433_) {
        assertOnRenderThread();
        GlStateManager._clearStencil(p_69433_);
    }

    public static void clear(int p_69422_) {
        GlStateManager._clear(p_69422_);
    }

    public static void setShaderFog(FogParameters p_366203_) {
        assertOnRenderThread();
        shaderFog = p_366203_;
    }

    public static FogParameters getShaderFog() {
        assertOnRenderThread();
        return shaderFog;
    }

    public static void setShaderGlintAlpha(double p_268332_) {
        setShaderGlintAlpha((float)p_268332_);
    }

    public static void setShaderGlintAlpha(float p_268329_) {
        assertOnRenderThread();
        shaderGlintAlpha = p_268329_;
    }

    public static float getShaderGlintAlpha() {
        assertOnRenderThread();
        return shaderGlintAlpha;
    }

    public static void setShaderLights(Vector3f p_254155_, Vector3f p_254006_) {
        assertOnRenderThread();
        shaderLightDirections[0] = p_254155_;
        shaderLightDirections[1] = p_254006_;
    }

    public static void setupShaderLights(CompiledShaderProgram p_362948_) {
        assertOnRenderThread();
        if (p_362948_.LIGHT0_DIRECTION != null) {
            p_362948_.LIGHT0_DIRECTION.set(shaderLightDirections[0]);
        }

        if (p_362948_.LIGHT1_DIRECTION != null) {
            p_362948_.LIGHT1_DIRECTION.set(shaderLightDirections[1]);
        }
    }

    public static void setShaderColor(float p_157430_, float p_157431_, float p_157432_, float p_157433_) {
        assertOnRenderThread();
        shaderColor[0] = p_157430_;
        shaderColor[1] = p_157431_;
        shaderColor[2] = p_157432_;
        shaderColor[3] = p_157433_;
    }

    public static float[] getShaderColor() {
        assertOnRenderThread();
        return shaderColor;
    }

    public static void drawElements(int p_157187_, int p_157188_, int p_157189_) {
        assertOnRenderThread();
        GlStateManager._drawElements(p_157187_, p_157188_, p_157189_, 0L);
    }

    public static void lineWidth(float p_69833_) {
        assertOnRenderThread();
        shaderLineWidth = p_69833_;
    }

    public static float getShaderLineWidth() {
        assertOnRenderThread();
        return shaderLineWidth;
    }

    public static void pixelStore(int p_69855_, int p_69856_) {
        GlStateManager._pixelStore(p_69855_, p_69856_);
    }

    public static void readPixels(int p_69872_, int p_69873_, int p_69874_, int p_69875_, int p_69876_, int p_69877_, ByteBuffer p_69878_) {
        assertOnRenderThread();
        GlStateManager._readPixels(p_69872_, p_69873_, p_69874_, p_69875_, p_69876_, p_69877_, p_69878_);
    }

    public static void getString(int p_69520_, Consumer<String> p_69521_) {
        assertOnRenderThread();
        p_69521_.accept(GlStateManager._getString(p_69520_));
    }

    public static String getBackendDescription() {
        return String.format(Locale.ROOT, "LWJGL version %s", GLX._getLWJGLVersion());
    }

    public static String getApiDescription() {
        return apiDescription;
    }

    public static TimeSource.NanoTimeSource initBackendSystem() {
        return GLX._initGlfw()::getAsLong;
    }

    public static void initRenderer(int p_69581_, boolean p_69582_) {
        GLX._init(p_69581_, p_69582_);
        apiDescription = GLX.getOpenGLVersionString();
    }

    public static void setErrorCallback(GLFWErrorCallbackI p_69901_) {
        GLX._setGlfwErrorCallback(p_69901_);
    }

    public static void renderCrosshair(int p_69882_) {
        assertOnRenderThread();
        GLX._renderCrosshair(p_69882_, true, true, true);
    }

    public static String getCapsString() {
        assertOnRenderThread();
        return "Using framebuffer using OpenGL 3.2";
    }

    public static void setupDefaultState(int p_69903_, int p_69904_, int p_69905_, int p_69906_) {
        GlStateManager._clearDepth(1.0);
        GlStateManager._enableDepthTest();
        GlStateManager._depthFunc(515);
        projectionMatrix.identity();
        savedProjectionMatrix.identity();
        modelViewStack.clear();
        textureMatrix.identity();
        GlStateManager._viewport(p_69903_, p_69904_, p_69905_, p_69906_);
    }

    public static int maxSupportedTextureSize() {
        if (MAX_SUPPORTED_TEXTURE_SIZE == -1) {
            assertOnRenderThreadOrInit();
            int i = GlStateManager._getInteger(3379);

            for (int j = Math.max(32768, i); j >= 1024; j >>= 1) {
                GlStateManager._texImage2D(32868, 0, 6408, j, j, 0, 6408, 5121, null);
                int k = GlStateManager._getTexLevelParameter(32868, 0, 4096);
                if (k != 0) {
                    MAX_SUPPORTED_TEXTURE_SIZE = j;
                    return j;
                }
            }

            MAX_SUPPORTED_TEXTURE_SIZE = Math.max(i, 1024);
            LOGGER.info("Failed to determine maximum texture size by probing, trying GL_MAX_TEXTURE_SIZE = {}", MAX_SUPPORTED_TEXTURE_SIZE);
        }

        return MAX_SUPPORTED_TEXTURE_SIZE;
    }

    public static void glBindBuffer(int p_157209_, int p_344603_) {
        GlStateManager._glBindBuffer(p_157209_, p_344603_);
    }

    public static void glBindVertexArray(int p_344671_) {
        GlStateManager._glBindVertexArray(p_344671_);
    }

    public static void glBufferData(int p_69526_, ByteBuffer p_69527_, int p_69528_) {
        assertOnRenderThreadOrInit();
        GlStateManager._glBufferData(p_69526_, p_69527_, p_69528_);
    }

    public static void glDeleteBuffers(int p_69530_) {
        assertOnRenderThread();
        GlStateManager._glDeleteBuffers(p_69530_);
    }

    public static void glDeleteVertexArrays(int p_157214_) {
        assertOnRenderThread();
        GlStateManager._glDeleteVertexArrays(p_157214_);
    }

    public static void glUniform1i(int p_69544_, int p_69545_) {
        assertOnRenderThread();
        GlStateManager._glUniform1i(p_69544_, p_69545_);
    }

    public static void glUniform1(int p_69541_, IntBuffer p_69542_) {
        assertOnRenderThread();
        GlStateManager._glUniform1(p_69541_, p_69542_);
    }

    public static void glUniform2(int p_69550_, IntBuffer p_69551_) {
        assertOnRenderThread();
        GlStateManager._glUniform2(p_69550_, p_69551_);
    }

    public static void glUniform3(int p_69556_, IntBuffer p_69557_) {
        assertOnRenderThread();
        GlStateManager._glUniform3(p_69556_, p_69557_);
    }

    public static void glUniform4(int p_69562_, IntBuffer p_69563_) {
        assertOnRenderThread();
        GlStateManager._glUniform4(p_69562_, p_69563_);
    }

    public static void glUniform1(int p_69538_, FloatBuffer p_69539_) {
        assertOnRenderThread();
        GlStateManager._glUniform1(p_69538_, p_69539_);
    }

    public static void glUniform2(int p_69547_, FloatBuffer p_69548_) {
        assertOnRenderThread();
        GlStateManager._glUniform2(p_69547_, p_69548_);
    }

    public static void glUniform3(int p_69553_, FloatBuffer p_69554_) {
        assertOnRenderThread();
        GlStateManager._glUniform3(p_69553_, p_69554_);
    }

    public static void glUniform4(int p_69559_, FloatBuffer p_69560_) {
        assertOnRenderThread();
        GlStateManager._glUniform4(p_69559_, p_69560_);
    }

    public static void glUniformMatrix2(int p_69565_, boolean p_69566_, FloatBuffer p_69567_) {
        assertOnRenderThread();
        GlStateManager._glUniformMatrix2(p_69565_, p_69566_, p_69567_);
    }

    public static void glUniformMatrix3(int p_69569_, boolean p_69570_, FloatBuffer p_69571_) {
        assertOnRenderThread();
        GlStateManager._glUniformMatrix3(p_69569_, p_69570_, p_69571_);
    }

    public static void glUniformMatrix4(int p_69573_, boolean p_69574_, FloatBuffer p_69575_) {
        assertOnRenderThread();
        GlStateManager._glUniformMatrix4(p_69573_, p_69574_, p_69575_);
    }

    public static void setupOverlayColor(int p_69922_, int p_342657_) {
        assertOnRenderThread();
        setShaderTexture(1, p_69922_);
    }

    public static void teardownOverlayColor() {
        assertOnRenderThread();
        setShaderTexture(1, 0);
    }

    public static void setupLevelDiffuseLighting(Vector3f p_254489_, Vector3f p_254541_) {
        assertOnRenderThread();
        setShaderLights(p_254489_, p_254541_);
    }

    public static void setupGuiFlatDiffuseLighting(Vector3f p_254419_, Vector3f p_254483_) {
        assertOnRenderThread();
        GlStateManager.setupGuiFlatDiffuseLighting(p_254419_, p_254483_);
    }

    public static void setupGui3DDiffuseLighting(Vector3f p_253859_, Vector3f p_253890_) {
        assertOnRenderThread();
        GlStateManager.setupGui3DDiffuseLighting(p_253859_, p_253890_);
    }

    public static void beginInitialization() {
        isInInit = true;
    }

    public static void finishInitialization() {
        isInInit = false;
        if (!recordingQueue.isEmpty()) {
            replayQueue();
        }

        if (!recordingQueue.isEmpty()) {
            throw new IllegalStateException("Recorded to render queue during initialization");
        }
    }

    public static Tesselator renderThreadTesselator() {
        assertOnRenderThread();
        return RENDER_THREAD_TESSELATOR;
    }

    public static void defaultBlendFunc() {
        blendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
        );
    }

    @Nullable
    public static CompiledShaderProgram setShader(ShaderProgram p_364012_) {
        assertOnRenderThread();
        CompiledShaderProgram compiledshaderprogram = Minecraft.getInstance().getShaderManager().getProgram(p_364012_);
        shader = compiledshaderprogram;
        return compiledshaderprogram;
    }

    public static void setShader(CompiledShaderProgram p_362982_) {
        assertOnRenderThread();
        shader = p_362982_;
    }

    public static void clearShader() {
        assertOnRenderThread();
        shader = null;
    }

    @Nullable
    public static CompiledShaderProgram getShader() {
        assertOnRenderThread();
        return shader;
    }

    public static void setShaderTexture(int p_157457_, ResourceLocation p_157458_) {
        assertOnRenderThread();
        if (p_157457_ >= 0 && p_157457_ < shaderTextures.length) {
            TextureManager texturemanager = Minecraft.getInstance().getTextureManager();
            AbstractTexture abstracttexture = texturemanager.getTexture(p_157458_);
            shaderTextures[p_157457_] = abstracttexture.getId();
        }
    }

    public static void setShaderTexture(int p_157454_, int p_157455_) {
        assertOnRenderThread();
        if (p_157454_ >= 0 && p_157454_ < shaderTextures.length) {
            shaderTextures[p_157454_] = p_157455_;
        }
    }

    public static int getShaderTexture(int p_157204_) {
        assertOnRenderThread();
        return p_157204_ >= 0 && p_157204_ < shaderTextures.length ? shaderTextures[p_157204_] : 0;
    }

    public static void setProjectionMatrix(Matrix4f p_277884_, ProjectionType p_362578_) {
        assertOnRenderThread();
        projectionMatrix = new Matrix4f(p_277884_);
        projectionType = p_362578_;
    }

    public static void setTextureMatrix(Matrix4f p_254081_) {
        assertOnRenderThread();
        textureMatrix = new Matrix4f(p_254081_);
    }

    public static void resetTextureMatrix() {
        assertOnRenderThread();
        textureMatrix.identity();
    }

    public static void backupProjectionMatrix() {
        assertOnRenderThread();
        savedProjectionMatrix = projectionMatrix;
        savedProjectionType = projectionType;
    }

    public static void restoreProjectionMatrix() {
        assertOnRenderThread();
        projectionMatrix = savedProjectionMatrix;
        projectionType = savedProjectionType;
    }

    public static Matrix4f getProjectionMatrix() {
        assertOnRenderThread();
        return projectionMatrix;
    }

    public static Matrix4f getModelViewMatrix() {
        assertOnRenderThread();
        return modelViewStack;
    }

    public static Matrix4fStack getModelViewStack() {
        assertOnRenderThread();
        return modelViewStack;
    }

    public static Matrix4f getTextureMatrix() {
        assertOnRenderThread();
        return textureMatrix;
    }

    public static RenderSystem.AutoStorageIndexBuffer getSequentialBuffer(VertexFormat.Mode p_221942_) {
        assertOnRenderThread();

        return switch (p_221942_) {
            case QUADS -> sharedSequentialQuad;
            case LINES -> sharedSequentialLines;
            default -> sharedSequential;
        };
    }

    public static void setShaderGameTime(long p_157448_, float p_157449_) {
        assertOnRenderThread();
        shaderGameTime = ((float)(p_157448_ % 24000L) + p_157449_) / 24000.0F;
    }

    public static float getShaderGameTime() {
        assertOnRenderThread();
        return shaderGameTime;
    }

    public static ProjectionType getProjectionType() {
        assertOnRenderThread();
        return projectionType;
    }

    @OnlyIn(Dist.CLIENT)
    public static final class AutoStorageIndexBuffer {
        private final int vertexStride;
        private final int indexStride;
        private final RenderSystem.AutoStorageIndexBuffer.IndexGenerator generator;
        @Nullable
        private GpuBuffer buffer;
        private VertexFormat.IndexType type = VertexFormat.IndexType.SHORT;
        private int indexCount;

        AutoStorageIndexBuffer(int p_157472_, int p_157473_, RenderSystem.AutoStorageIndexBuffer.IndexGenerator p_157474_) {
            this.vertexStride = p_157472_;
            this.indexStride = p_157473_;
            this.generator = p_157474_;
        }

        public boolean hasStorage(int p_221945_) {
            return p_221945_ <= this.indexCount;
        }

        public void bind(int p_221947_) {
            if (this.buffer == null) {
                this.buffer = new GpuBuffer(BufferType.INDICES, BufferUsage.DYNAMIC_WRITE, 0);
            }

            this.buffer.bind();
            this.ensureStorage(p_221947_);
        }

        private void ensureStorage(int p_157477_) {
            if (!this.hasStorage(p_157477_)) {
                p_157477_ = Mth.roundToward(p_157477_ * 2, this.indexStride);
                RenderSystem.LOGGER.debug("Growing IndexBuffer: Old limit {}, new limit {}.", this.indexCount, p_157477_);
                int i = p_157477_ / this.indexStride;
                int j = i * this.vertexStride;
                VertexFormat.IndexType vertexformat$indextype = VertexFormat.IndexType.least(j);
                int k = Mth.roundToward(p_157477_ * vertexformat$indextype.bytes, 4);
                ByteBuffer bytebuffer = MemoryUtil.memAlloc(k);

                try {
                    this.type = vertexformat$indextype;
                    it.unimi.dsi.fastutil.ints.IntConsumer intconsumer = this.intConsumer(bytebuffer);

                    for (int l = 0; l < p_157477_; l += this.indexStride) {
                        this.generator.accept(intconsumer, l * this.vertexStride / this.indexStride);
                    }

                    bytebuffer.flip();
                    this.buffer.resize(k);
                    this.buffer.write(bytebuffer, 0);
                } finally {
                    MemoryUtil.memFree(bytebuffer);
                }

                this.indexCount = p_157477_;
            }
        }

        private it.unimi.dsi.fastutil.ints.IntConsumer intConsumer(ByteBuffer p_157479_) {
            switch (this.type) {
                case SHORT:
                    return p_157482_ -> p_157479_.putShort((short)p_157482_);
                case INT:
                default:
                    return p_157479_::putInt;
            }
        }

        public VertexFormat.IndexType type() {
            return this.type;
        }

        @OnlyIn(Dist.CLIENT)
        interface IndexGenerator {
            void accept(it.unimi.dsi.fastutil.ints.IntConsumer p_157488_, int p_157489_);
        }
    }
}