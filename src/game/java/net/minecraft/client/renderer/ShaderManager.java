package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.shaders.CompiledShader;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ShaderManager extends SimplePreparableReloadListener<ShaderManager.Configs> implements AutoCloseable {
    static final Logger LOGGER = LogUtils.getLogger();
    public static final String SHADER_PATH = "shaders";
    public static final String SHADER_INCLUDE_PATH = "shaders/include/";
    private static final FileToIdConverter PROGRAM_ID_CONVERTER = FileToIdConverter.json("shaders");
    private static final FileToIdConverter POST_CHAIN_ID_CONVERTER = FileToIdConverter.json("post_effect");
    public static final int MAX_LOG_LENGTH = 32768;
    final TextureManager textureManager;
    private final Consumer<Exception> recoveryHandler;
    private ShaderManager.CompilationCache compilationCache = new ShaderManager.CompilationCache(ShaderManager.Configs.EMPTY);

    public ShaderManager(TextureManager p_360733_, Consumer<Exception> p_367243_) {
        this.textureManager = p_360733_;
        this.recoveryHandler = p_367243_;
    }

    protected ShaderManager.Configs prepare(ResourceManager p_363890_, ProfilerFiller p_362646_) {
        Builder<ResourceLocation, ShaderProgramConfig> builder = ImmutableMap.builder();
        Builder<ShaderManager.ShaderSourceKey, String> builder1 = ImmutableMap.builder();
        Map<ResourceLocation, Resource> map = p_363890_.listResources("shaders", p_362430_ -> isProgram(p_362430_) || isShader(p_362430_));

        for (Entry<ResourceLocation, Resource> entry : map.entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            CompiledShader.Type compiledshader$type = CompiledShader.Type.byLocation(resourcelocation);
            if (compiledshader$type != null) {
                loadShader(resourcelocation, entry.getValue(), compiledshader$type, map, builder1);
            } else if (isProgram(resourcelocation)) {
                loadProgram(resourcelocation, entry.getValue(), builder);
            }
        }

        Builder<ResourceLocation, PostChainConfig> builder2 = ImmutableMap.builder();

        for (Entry<ResourceLocation, Resource> entry1 : POST_CHAIN_ID_CONVERTER.listMatchingResources(p_363890_).entrySet()) {
            loadPostChain(entry1.getKey(), entry1.getValue(), builder2);
        }

        return new ShaderManager.Configs(builder.build(), builder1.build(), builder2.build());
    }

    private static void loadShader(
        ResourceLocation p_369261_,
        Resource p_361062_,
        CompiledShader.Type p_365455_,
        Map<ResourceLocation, Resource> p_367069_,
        Builder<ShaderManager.ShaderSourceKey, String> p_365134_
    ) {
        ResourceLocation resourcelocation = p_365455_.idConverter().fileToId(p_369261_);
        GlslPreprocessor glslpreprocessor = createPreprocessor(p_367069_, p_369261_);

        try (Reader reader = p_361062_.openAsReader()) {
            String s = IOUtils.toString(reader);
            p_365134_.put(new ShaderManager.ShaderSourceKey(resourcelocation, p_365455_), String.join("", glslpreprocessor.process(s)));
        } catch (IOException ioexception) {
            LOGGER.error("Failed to load shader source at {}", p_369261_, ioexception);
        }
    }

    private static GlslPreprocessor createPreprocessor(final Map<ResourceLocation, Resource> p_367930_, ResourceLocation p_369394_) {
        final ResourceLocation resourcelocation = p_369394_.withPath(FileUtil::getFullResourcePath);
        return new GlslPreprocessor() {
            private final Set<ResourceLocation> importedLocations = new ObjectArraySet<>();

            @Override
            public String applyImport(boolean p_365562_, String p_361440_) {
                ResourceLocation resourcelocation1;
                try {
                    if (p_365562_) {
                        resourcelocation1 = resourcelocation.withPath(p_366909_ -> FileUtil.normalizeResourcePath(p_366909_ + p_361440_));
                    } else {
                        resourcelocation1 = ResourceLocation.parse(p_361440_).withPrefix("shaders/include/");
                    }
                } catch (ResourceLocationException resourcelocationexception) {
                    ShaderManager.LOGGER.error("Malformed GLSL import {}: {}", p_361440_, resourcelocationexception.getMessage());
                    return "#error " + resourcelocationexception.getMessage();
                }

                if (!this.importedLocations.add(resourcelocation1)) {
                    return null;
                } else {
                    try {
                        String s;
                        try (Reader reader = p_367930_.get(resourcelocation1).openAsReader()) {
                            s = IOUtils.toString(reader);
                        }

                        return s;
                    } catch (IOException ioexception) {
                        ShaderManager.LOGGER.error("Could not open GLSL import {}: {}", resourcelocation1, ioexception.getMessage());
                        return "#error " + ioexception.getMessage();
                    }
                }
            }
        };
    }

    private static void loadProgram(ResourceLocation p_365990_, Resource p_366934_, Builder<ResourceLocation, ShaderProgramConfig> p_366842_) {
        ResourceLocation resourcelocation = PROGRAM_ID_CONVERTER.fileToId(p_365990_);

        try (Reader reader = p_366934_.openAsReader()) {
            JsonElement jsonelement = JsonParser.parseReader(reader);
            ShaderProgramConfig shaderprogramconfig = ShaderProgramConfig.CODEC.parse(JsonOps.INSTANCE, jsonelement).getOrThrow(JsonSyntaxException::new);
            p_366842_.put(resourcelocation, shaderprogramconfig);
        } catch (JsonParseException | IOException ioexception) {
            LOGGER.error("Failed to parse shader config at {}", p_365990_, ioexception);
        }
    }

    private static void loadPostChain(ResourceLocation p_365599_, Resource p_365135_, Builder<ResourceLocation, PostChainConfig> p_362996_) {
        ResourceLocation resourcelocation = POST_CHAIN_ID_CONVERTER.fileToId(p_365599_);

        try (Reader reader = p_365135_.openAsReader()) {
            JsonElement jsonelement = JsonParser.parseReader(reader);
            p_362996_.put(resourcelocation, PostChainConfig.CODEC.parse(JsonOps.INSTANCE, jsonelement).getOrThrow(JsonSyntaxException::new));
        } catch (JsonParseException | IOException ioexception) {
            LOGGER.error("Failed to parse post chain at {}", p_365599_, ioexception);
        }
    }

    private static boolean isProgram(ResourceLocation p_368414_) {
        return p_368414_.getPath().endsWith(".json");
    }

    private static boolean isShader(ResourceLocation p_368473_) {
        return CompiledShader.Type.byLocation(p_368473_) != null || p_368473_.getPath().endsWith(".glsl");
    }

    protected void apply(ShaderManager.Configs p_360858_, ResourceManager p_369986_, ProfilerFiller p_364135_) {
        ShaderManager.CompilationCache shadermanager$compilationcache = new ShaderManager.CompilationCache(p_360858_);
        Map<ShaderProgram, ShaderManager.CompilationException> map = new HashMap<>();
        Set<ShaderProgram> set = new HashSet<>(CoreShaders.getProgramsToPreload());

        for (PostChainConfig postchainconfig : p_360858_.postChains.values()) {
            for (PostChainConfig.Pass postchainconfig$pass : postchainconfig.passes()) {
                set.add(postchainconfig$pass.program());
            }
        }

        for (ShaderProgram shaderprogram : set) {
            try {
                shadermanager$compilationcache.programs.put(shaderprogram, Optional.of(shadermanager$compilationcache.compileProgram(shaderprogram)));
            } catch (ShaderManager.CompilationException shadermanager$compilationexception) {
                map.put(shaderprogram, shadermanager$compilationexception);
            }
        }

        if (!map.isEmpty()) {
            shadermanager$compilationcache.close();
            throw new RuntimeException(
                "Failed to load required shader programs:\n"
                    + map.entrySet()
                        .stream()
                        .map(p_366321_ -> " - " + p_366321_.getKey() + ": " + p_366321_.getValue().getMessage())
                        .collect(Collectors.joining("\n"))
            );
        } else {
            this.compilationCache.close();
            this.compilationCache = shadermanager$compilationcache;
        }
    }

    @Override
    public String getName() {
        return "Shader Loader";
    }

    private void tryTriggerRecovery(Exception p_378248_) {
        if (!this.compilationCache.triggeredRecovery) {
            this.recoveryHandler.accept(p_378248_);
            this.compilationCache.triggeredRecovery = true;
        }
    }

    public void preloadForStartup(ResourceProvider p_367540_, ShaderProgram... p_362777_) throws IOException, ShaderManager.CompilationException {
        for (ShaderProgram shaderprogram : p_362777_) {
            Resource resource = p_367540_.getResourceOrThrow(PROGRAM_ID_CONVERTER.idToFile(shaderprogram.configId()));

            try (Reader reader = resource.openAsReader()) {
                JsonElement jsonelement = JsonParser.parseReader(reader);
                ShaderProgramConfig shaderprogramconfig = ShaderProgramConfig.CODEC
                    .parse(JsonOps.INSTANCE, jsonelement)
                    .getOrThrow(JsonSyntaxException::new);
                ShaderDefines shaderdefines = shaderprogramconfig.defines().withOverrides(shaderprogram.defines());
                CompiledShader compiledshader = this.preloadShader(p_367540_, shaderprogramconfig.vertex(), CompiledShader.Type.VERTEX, shaderdefines);
                CompiledShader compiledshader1 = this.preloadShader(p_367540_, shaderprogramconfig.fragment(), CompiledShader.Type.FRAGMENT, shaderdefines);
                CompiledShaderProgram compiledshaderprogram = linkProgram(shaderprogram, shaderprogramconfig, compiledshader, compiledshader1);
                this.compilationCache.programs.put(shaderprogram, Optional.of(compiledshaderprogram));
            }
        }
    }

    private CompiledShader preloadShader(ResourceProvider p_363994_, ResourceLocation p_360916_, CompiledShader.Type p_362265_, ShaderDefines p_368503_) throws IOException, ShaderManager.CompilationException {
        ResourceLocation resourcelocation = p_362265_.idConverter().idToFile(p_360916_);

        CompiledShader compiledshader1;
        try (Reader reader = p_363994_.getResourceOrThrow(resourcelocation).openAsReader()) {
            String s = IOUtils.toString(reader);
            String s1 = GlslPreprocessor.injectDefines(s, p_368503_);
            CompiledShader compiledshader = CompiledShader.compile(p_360916_, p_362265_, s1);
            this.compilationCache.shaders.put(new ShaderManager.ShaderCompilationKey(p_360916_, p_362265_, p_368503_), compiledshader);
            compiledshader1 = compiledshader;
        }

        return compiledshader1;
    }

    @Nullable
    public CompiledShaderProgram getProgram(ShaderProgram p_362106_) {
        try {
            return this.compilationCache.getOrCompileProgram(p_362106_);
        } catch (ShaderManager.CompilationException shadermanager$compilationexception) {
            LOGGER.error("Failed to load shader program: {}", p_362106_, shadermanager$compilationexception);
            this.compilationCache.programs.put(p_362106_, Optional.empty());
            this.tryTriggerRecovery(shadermanager$compilationexception);
            return null;
        }
    }

    public CompiledShaderProgram getProgramForLoading(ShaderProgram p_365077_) throws ShaderManager.CompilationException {
        CompiledShaderProgram compiledshaderprogram = this.compilationCache.getOrCompileProgram(p_365077_);
        if (compiledshaderprogram == null) {
            throw new ShaderManager.CompilationException("Shader '" + p_365077_ + "' could not be found");
        } else {
            return compiledshaderprogram;
        }
    }

    static CompiledShaderProgram linkProgram(ShaderProgram p_368435_, ShaderProgramConfig p_369334_, CompiledShader p_361097_, CompiledShader p_364151_) throws ShaderManager.CompilationException {
        CompiledShaderProgram compiledshaderprogram = CompiledShaderProgram.link(p_361097_, p_364151_, p_368435_.vertexFormat());
        compiledshaderprogram.setupUniforms(p_369334_.uniforms(), p_369334_.samplers());
        return compiledshaderprogram;
    }

    @Nullable
    public PostChain getPostChain(ResourceLocation p_370004_, Set<ResourceLocation> p_362698_) {
        try {
            return this.compilationCache.getOrLoadPostChain(p_370004_, p_362698_);
        } catch (ShaderManager.CompilationException shadermanager$compilationexception) {
            LOGGER.error("Failed to load post chain: {}", p_370004_, shadermanager$compilationexception);
            this.compilationCache.postChains.put(p_370004_, Optional.empty());
            this.tryTriggerRecovery(shadermanager$compilationexception);
            return null;
        }
    }

    @Override
    public void close() {
        this.compilationCache.close();
    }

    @OnlyIn(Dist.CLIENT)
    class CompilationCache implements AutoCloseable {
        private final ShaderManager.Configs configs;
        final Map<ShaderProgram, Optional<CompiledShaderProgram>> programs = new HashMap<>();
        final Map<ShaderManager.ShaderCompilationKey, CompiledShader> shaders = new HashMap<>();
        final Map<ResourceLocation, Optional<PostChain>> postChains = new HashMap<>();
        boolean triggeredRecovery;

        CompilationCache(final ShaderManager.Configs p_369367_) {
            this.configs = p_369367_;
        }

        @Nullable
        public CompiledShaderProgram getOrCompileProgram(ShaderProgram p_362727_) throws ShaderManager.CompilationException {
            Optional<CompiledShaderProgram> optional = this.programs.get(p_362727_);
            if (optional != null) {
                return optional.orElse(null);
            } else {
                CompiledShaderProgram compiledshaderprogram = this.compileProgram(p_362727_);
                this.programs.put(p_362727_, Optional.of(compiledshaderprogram));
                return compiledshaderprogram;
            }
        }

        CompiledShaderProgram compileProgram(ShaderProgram p_362724_) throws ShaderManager.CompilationException {
            ShaderProgramConfig shaderprogramconfig = this.configs.programs.get(p_362724_.configId());
            if (shaderprogramconfig == null) {
                throw new ShaderManager.CompilationException("Could not find program with id: " + p_362724_.configId());
            } else {
                ShaderDefines shaderdefines = shaderprogramconfig.defines().withOverrides(p_362724_.defines());
                CompiledShader compiledshader = this.getOrCompileShader(shaderprogramconfig.vertex(), CompiledShader.Type.VERTEX, shaderdefines);
                CompiledShader compiledshader1 = this.getOrCompileShader(shaderprogramconfig.fragment(), CompiledShader.Type.FRAGMENT, shaderdefines);
                return ShaderManager.linkProgram(p_362724_, shaderprogramconfig, compiledshader, compiledshader1);
            }
        }

        private CompiledShader getOrCompileShader(ResourceLocation p_362898_, CompiledShader.Type p_364365_, ShaderDefines p_369295_) throws ShaderManager.CompilationException {
            ShaderManager.ShaderCompilationKey shadermanager$shadercompilationkey = new ShaderManager.ShaderCompilationKey(p_362898_, p_364365_, p_369295_);
            CompiledShader compiledshader = this.shaders.get(shadermanager$shadercompilationkey);
            if (compiledshader == null) {
                compiledshader = this.compileShader(shadermanager$shadercompilationkey);
                this.shaders.put(shadermanager$shadercompilationkey, compiledshader);
            }

            return compiledshader;
        }

        private CompiledShader compileShader(ShaderManager.ShaderCompilationKey p_369853_) throws ShaderManager.CompilationException {
            String s = this.configs.shaderSources.get(new ShaderManager.ShaderSourceKey(p_369853_.id, p_369853_.type));
            if (s == null) {
                throw new ShaderManager.CompilationException("Could not find shader: " + p_369853_);
            } else {
                String s1 = GlslPreprocessor.injectDefines(s, p_369853_.defines);
                return CompiledShader.compile(p_369853_.id, p_369853_.type, s1);
            }
        }

        @Nullable
        public PostChain getOrLoadPostChain(ResourceLocation p_362197_, Set<ResourceLocation> p_368742_) throws ShaderManager.CompilationException {
            Optional<PostChain> optional = this.postChains.get(p_362197_);
            if (optional != null) {
                return optional.orElse(null);
            } else {
                PostChain postchain = this.loadPostChain(p_362197_, p_368742_);
                this.postChains.put(p_362197_, Optional.of(postchain));
                return postchain;
            }
        }

        private PostChain loadPostChain(ResourceLocation p_366740_, Set<ResourceLocation> p_366419_) throws ShaderManager.CompilationException {
            PostChainConfig postchainconfig = this.configs.postChains.get(p_366740_);
            if (postchainconfig == null) {
                throw new ShaderManager.CompilationException("Could not find post chain with id: " + p_366740_);
            } else {
                return PostChain.load(postchainconfig, ShaderManager.this.textureManager, ShaderManager.this, p_366419_);
            }
        }

        @Override
        public void close() {
            RenderSystem.assertOnRenderThread();
            this.programs.values().forEach(p_365427_ -> p_365427_.ifPresent(CompiledShaderProgram::close));
            this.shaders.values().forEach(CompiledShader::close);
            this.programs.clear();
            this.shaders.clear();
            this.postChains.clear();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class CompilationException extends Exception {
        public CompilationException(String p_366142_) {
            super(p_366142_);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static record Configs(
        Map<ResourceLocation, ShaderProgramConfig> programs,
        Map<ShaderManager.ShaderSourceKey, String> shaderSources,
        Map<ResourceLocation, PostChainConfig> postChains
    ) {
        public static final ShaderManager.Configs EMPTY = new ShaderManager.Configs(Map.of(), Map.of(), Map.of());
    }

    @OnlyIn(Dist.CLIENT)
    static record ShaderCompilationKey(ResourceLocation id, CompiledShader.Type type, ShaderDefines defines) {
        @Override
        public String toString() {
            String s = this.id + " (" + this.type + ")";
            return !this.defines.isEmpty() ? s + " with " + this.defines : s;
        }
    }

    @OnlyIn(Dist.CLIENT)
    static record ShaderSourceKey(ResourceLocation id, CompiledShader.Type type) {
        @Override
        public String toString() {
            return this.id + " (" + this.type + ")";
        }
    }
}