package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.resource.RenderTargetDescriptor;
import com.mojang.blaze3d.resource.ResourceHandle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class PostChain {
    public static final ResourceLocation MAIN_TARGET_ID = ResourceLocation.withDefaultNamespace("main");
    private final List<PostPass> passes;
    private final Map<ResourceLocation, PostChainConfig.InternalTarget> internalTargets;
    private final Set<ResourceLocation> externalTargets;

    private PostChain(List<PostPass> p_368125_, Map<ResourceLocation, PostChainConfig.InternalTarget> p_365904_, Set<ResourceLocation> p_364283_) {
        this.passes = p_368125_;
        this.internalTargets = p_365904_;
        this.externalTargets = p_364283_;
    }

    public static PostChain load(PostChainConfig p_361031_, TextureManager p_110034_, ShaderManager p_364428_, Set<ResourceLocation> p_370027_) throws ShaderManager.CompilationException {
        Stream<ResourceLocation> stream = p_361031_.passes()
            .stream()
            .flatMap(p_357873_ -> p_357873_.inputs().stream())
            .flatMap(p_357872_ -> p_357872_.referencedTargets().stream());
        Set<ResourceLocation> set = stream.filter(p_357871_ -> !p_361031_.internalTargets().containsKey(p_357871_)).collect(Collectors.toSet());
        Set<ResourceLocation> set1 = Sets.difference(set, p_370027_);
        if (!set1.isEmpty()) {
            throw new ShaderManager.CompilationException("Referenced external targets are not available in this context: " + set1);
        } else {
            Builder<PostPass> builder = ImmutableList.builder();

            for (PostChainConfig.Pass postchainconfig$pass : p_361031_.passes()) {
                builder.add(createPass(p_110034_, p_364428_, postchainconfig$pass));
            }

            return new PostChain(builder.build(), p_361031_.internalTargets(), set);
        }
    }

    private static PostPass createPass(TextureManager p_366006_, ShaderManager p_365441_, PostChainConfig.Pass p_368358_) throws ShaderManager.CompilationException {
        CompiledShaderProgram compiledshaderprogram = p_365441_.getProgramForLoading(p_368358_.program());

        for (PostChainConfig.Uniform postchainconfig$uniform : p_368358_.uniforms()) {
            String s = postchainconfig$uniform.name();
            if (compiledshaderprogram.getUniform(s) == null) {
                throw new ShaderManager.CompilationException("Uniform '" + s + "' does not exist for " + p_368358_.programId());
            }
        }

        String s2 = p_368358_.programId().toString();
        PostPass postpass = new PostPass(s2, compiledshaderprogram, p_368358_.outputTarget(), p_368358_.uniforms());

        for (PostChainConfig.Input postchainconfig$input : p_368358_.inputs()) {
            switch (postchainconfig$input) {
                case PostChainConfig.TextureInput texutre:
                    try {
                    String s3 = texutre.samplerName();
                    ResourceLocation resourcelocation = texutre.location();
                    int i = texutre.width();
                    int j = texutre.height();
                    boolean flag = texutre.bilinear();
                    AbstractTexture abstracttexture = p_366006_.getTexture(resourcelocation.withPath(p_357869_ -> "textures/effect/" + p_357869_ + ".png"));
                    abstracttexture.setFilter(flag, false);
                    postpass.addInput(new PostPass.TextureInput(s3, abstracttexture, i, j));
                    continue;
                    } catch (Throwable t) {
                        throw new MatchException(t.toString(), t);
                    }
                case PostChainConfig.TargetInput target:
                    try {
                    String s1 = target.samplerName();
                    ResourceLocation resourcelocation1 = target.targetId();
                    boolean flag1 = target.useDepthBuffer();
                    boolean flag2 = target.bilinear();
                    postpass.addInput(new PostPass.TargetInput(s1, resourcelocation1, flag1, flag2));
                    continue;
                    } catch (Throwable t) {
                        throw new MatchException(t.toString(), t);
                    }
                default:
                    throw new MatchException(null, null);
            }
        }

        return postpass;
    }

    public void addToFrame(FrameGraphBuilder p_362816_, int p_365028_, int p_368108_, PostChain.TargetBundle p_366403_) {
        Matrix4f matrix4f = new Matrix4f().setOrtho(0.0F, (float)p_365028_, 0.0F, (float)p_368108_, 0.1F, 1000.0F);
        Map<ResourceLocation, ResourceHandle<RenderTarget>> map = new HashMap<>(this.internalTargets.size() + this.externalTargets.size());

        for (ResourceLocation resourcelocation : this.externalTargets) {
            map.put(resourcelocation, p_366403_.getOrThrow(resourcelocation));
        }

        for (Entry<ResourceLocation, PostChainConfig.InternalTarget> entry : this.internalTargets.entrySet()) {
            ResourceLocation resourcelocation1 = entry.getKey();
            Objects.requireNonNull(entry.getValue());
            RenderTargetDescriptor rendertargetdescriptor = switch (entry.getValue()) {
                case PostChainConfig.FixedSizedTarget target -> new RenderTargetDescriptor(target.width(), target.height(), true);
                case PostChainConfig.FullScreenTarget target -> new RenderTargetDescriptor(p_365028_, p_368108_, true);
                default -> throw new MatchException(null, null);
            };
            map.put(resourcelocation1, p_362816_.createInternal(resourcelocation1.toString(), rendertargetdescriptor));
        }

        for (PostPass postpass : this.passes) {
            postpass.addToFrame(p_362816_, map, matrix4f);
        }

        for (ResourceLocation resourcelocation2 : this.externalTargets) {
            p_366403_.replace(resourcelocation2, map.get(resourcelocation2));
        }
    }

    @Deprecated
    public void process(RenderTarget p_367570_, GraphicsResourceAllocator p_362918_) {
        FrameGraphBuilder framegraphbuilder = new FrameGraphBuilder();
        PostChain.TargetBundle postchain$targetbundle = PostChain.TargetBundle.of(MAIN_TARGET_ID, framegraphbuilder.importExternal("main", p_367570_));
        this.addToFrame(framegraphbuilder, p_367570_.width, p_367570_.height, postchain$targetbundle);
        framegraphbuilder.execute(p_362918_);
    }

    public void setUniform(String p_327827_, float p_331223_) {
        for (PostPass postpass : this.passes) {
            postpass.getShader().safeGetUniform(p_327827_).set(p_331223_);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public interface TargetBundle {
        static PostChain.TargetBundle of(final ResourceLocation p_366117_, final ResourceHandle<RenderTarget> p_367685_) {
            return new PostChain.TargetBundle() {
                private ResourceHandle<RenderTarget> handle = p_367685_;

                @Override
                public void replace(ResourceLocation p_368607_, ResourceHandle<RenderTarget> p_369595_) {
                    if (p_368607_.equals(p_366117_)) {
                        this.handle = p_369595_;
                    } else {
                        throw new IllegalArgumentException("No target with id " + p_368607_);
                    }
                }

                @Nullable
                @Override
                public ResourceHandle<RenderTarget> get(ResourceLocation p_364302_) {
                    return p_364302_.equals(p_366117_) ? this.handle : null;
                }
            };
        }

        void replace(ResourceLocation p_369680_, ResourceHandle<RenderTarget> p_364990_);

        @Nullable
        ResourceHandle<RenderTarget> get(ResourceLocation p_365511_);

        default ResourceHandle<RenderTarget> getOrThrow(ResourceLocation p_364229_) {
            ResourceHandle<RenderTarget> resourcehandle = this.get(p_364229_);
            if (resourcehandle == null) {
                throw new IllegalArgumentException("Missing target with id " + p_364229_);
            } else {
                return resourcehandle;
            }
        }
    }
}
