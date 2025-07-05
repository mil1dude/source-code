package com.mojang.blaze3d.platform;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.DontObfuscate;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

@OnlyIn(Dist.CLIENT)
@DontObfuscate
public class GLX {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static String cpuInfo;

    public static String getOpenGLVersionString() {
        RenderSystem.assertOnRenderThread();
        return GLFW.glfwGetCurrentContext() == 0L
            ? "NO CONTEXT"
            : GlStateManager._getString(7937) + " GL version " + GlStateManager._getString(7938) + ", " + GlStateManager._getString(7936);
    }

    public static int _getRefreshRate(Window p_69342_) {
        RenderSystem.assertOnRenderThread();
        long i = GLFW.glfwGetWindowMonitor(p_69342_.getWindow());
        if (i == 0L) {
            i = GLFW.glfwGetPrimaryMonitor();
        }

        GLFWVidMode glfwvidmode = i == 0L ? null : GLFW.glfwGetVideoMode(i);
        return glfwvidmode == null ? 0 : glfwvidmode.refreshRate();
    }

    public static String _getLWJGLVersion() {
        return Version.getVersion();
    }

    public static LongSupplier _initGlfw() {
        Window.checkGlfwError((p_242032_, p_242033_) -> {
            throw new IllegalStateException(String.format(Locale.ROOT, "GLFW error before init: [0x%X]%s", p_242032_, p_242033_));
        });
        List<String> list = Lists.newArrayList();
        GLFWErrorCallback glfwerrorcallback = GLFW.glfwSetErrorCallback((p_69365_, p_69366_) -> {
            String s1 = p_69366_ == 0L ? "" : MemoryUtil.memUTF8(p_69366_);
            list.add(String.format(Locale.ROOT, "GLFW error during init: [0x%X]%s", p_69365_, s1));
        });
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW, errors: " + Joiner.on(",").join(list));
        } else {
            LongSupplier longsupplier = () -> (long)(GLFW.glfwGetTime() * 1.0E9);

            for (String s : list) {
                LOGGER.error("GLFW error collected during initialization: {}", s);
            }

            RenderSystem.setErrorCallback(glfwerrorcallback);
            return longsupplier;
        }
    }

    public static void _setGlfwErrorCallback(GLFWErrorCallbackI p_69353_) {
        GLFWErrorCallback glfwerrorcallback = GLFW.glfwSetErrorCallback(p_69353_);
        if (glfwerrorcallback != null) {
            glfwerrorcallback.free();
        }
    }

    public static boolean _shouldClose(Window p_69356_) {
        return GLFW.glfwWindowShouldClose(p_69356_.getWindow());
    }

    public static void _init(int p_69344_, boolean p_69345_) {
        try {
            CentralProcessor centralprocessor = new SystemInfo().getHardware().getProcessor();
            cpuInfo = String.format(Locale.ROOT, "%dx %s", centralprocessor.getLogicalProcessorCount(), centralprocessor.getProcessorIdentifier().getName())
                .replaceAll("\\s+", " ");
        } catch (Throwable throwable) {
        }

        GlDebug.enableDebugCallback(p_69344_, p_69345_);
    }

    public static String _getCpuInfo() {
        return cpuInfo == null ? "<unknown>" : cpuInfo;
    }

    public static void _renderCrosshair(int p_69348_, boolean p_69349_, boolean p_69350_, boolean p_69351_) {
        if (p_69349_ || p_69350_ || p_69351_) {
            RenderSystem.assertOnRenderThread();
            GlStateManager._depthMask(false);
            GlStateManager._disableCull();
            RenderSystem.setShader(CoreShaders.RENDERTYPE_LINES);
            Tesselator tesselator = RenderSystem.renderThreadTesselator();
            BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
            RenderSystem.lineWidth(4.0F);
            if (p_69349_) {
                bufferbuilder.addVertex(0.0F, 0.0F, 0.0F).setColor(-16777216).setNormal(1.0F, 0.0F, 0.0F);
                bufferbuilder.addVertex((float)p_69348_, 0.0F, 0.0F).setColor(-16777216).setNormal(1.0F, 0.0F, 0.0F);
            }

            if (p_69350_) {
                bufferbuilder.addVertex(0.0F, 0.0F, 0.0F).setColor(-16777216).setNormal(0.0F, 1.0F, 0.0F);
                bufferbuilder.addVertex(0.0F, (float)p_69348_, 0.0F).setColor(-16777216).setNormal(0.0F, 1.0F, 0.0F);
            }

            if (p_69351_) {
                bufferbuilder.addVertex(0.0F, 0.0F, 0.0F).setColor(-16777216).setNormal(0.0F, 0.0F, 1.0F);
                bufferbuilder.addVertex(0.0F, 0.0F, (float)p_69348_).setColor(-16777216).setNormal(0.0F, 0.0F, 1.0F);
            }

            BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
            RenderSystem.lineWidth(2.0F);
            bufferbuilder = tesselator.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
            if (p_69349_) {
                bufferbuilder.addVertex(0.0F, 0.0F, 0.0F).setColor(-65536).setNormal(1.0F, 0.0F, 0.0F);
                bufferbuilder.addVertex((float)p_69348_, 0.0F, 0.0F).setColor(-65536).setNormal(1.0F, 0.0F, 0.0F);
            }

            if (p_69350_) {
                bufferbuilder.addVertex(0.0F, 0.0F, 0.0F).setColor(-16711936).setNormal(0.0F, 1.0F, 0.0F);
                bufferbuilder.addVertex(0.0F, (float)p_69348_, 0.0F).setColor(-16711936).setNormal(0.0F, 1.0F, 0.0F);
            }

            if (p_69351_) {
                bufferbuilder.addVertex(0.0F, 0.0F, 0.0F).setColor(-8421377).setNormal(0.0F, 0.0F, 1.0F);
                bufferbuilder.addVertex(0.0F, 0.0F, (float)p_69348_).setColor(-8421377).setNormal(0.0F, 0.0F, 1.0F);
            }

            BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
            RenderSystem.lineWidth(1.0F);
            GlStateManager._enableCull();
            GlStateManager._depthMask(true);
        }
    }

    public static <T> T make(Supplier<T> p_69374_) {
        return p_69374_.get();
    }

    public static <T> T make(T p_69371_, Consumer<T> p_69372_) {
        p_69372_.accept(p_69371_);
        return p_69371_;
    }
}