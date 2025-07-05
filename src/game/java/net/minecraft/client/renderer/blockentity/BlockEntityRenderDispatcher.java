package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlockEntityRenderDispatcher implements ResourceManagerReloadListener {
    private Map<BlockEntityType<?>, BlockEntityRenderer<?>> renderers = ImmutableMap.of();
    private final Font font;
    private final Supplier<EntityModelSet> entityModelSet;
    public Level level;
    public Camera camera;
    public HitResult cameraHitResult;
    private final BlockRenderDispatcher blockRenderDispatcher;
    private final ItemModelResolver itemModelResolver;
    private final ItemRenderer itemRenderer;
    private final EntityRenderDispatcher entityRenderer;

    public BlockEntityRenderDispatcher(
        Font p_234432_,
        Supplier<EntityModelSet> p_234434_,
        BlockRenderDispatcher p_377332_,
        ItemModelResolver p_376400_,
        ItemRenderer p_378208_,
        EntityRenderDispatcher p_375551_
    ) {
        this.itemRenderer = p_378208_;
        this.itemModelResolver = p_376400_;
        this.entityRenderer = p_375551_;
        this.font = p_234432_;
        this.entityModelSet = p_234434_;
        this.blockRenderDispatcher = p_377332_;
    }

    @Nullable
    public <E extends BlockEntity> BlockEntityRenderer<E> getRenderer(E p_112266_) {
        return (BlockEntityRenderer<E>)this.renderers.get(p_112266_.getType());
    }

    public void prepare(Level p_173565_, Camera p_173566_, HitResult p_173567_) {
        if (this.level != p_173565_) {
            this.setLevel(p_173565_);
        }

        this.camera = p_173566_;
        this.cameraHitResult = p_173567_;
    }

    public <E extends BlockEntity> void render(E p_112268_, float p_112269_, PoseStack p_112270_, MultiBufferSource p_112271_) {
        BlockEntityRenderer<E> blockentityrenderer = this.getRenderer(p_112268_);
        if (blockentityrenderer != null) {
            if (p_112268_.hasLevel() && p_112268_.getType().isValid(p_112268_.getBlockState())) {
                if (blockentityrenderer.shouldRender(p_112268_, this.camera.getPosition())) {
                    try {
                        setupAndRender(blockentityrenderer, p_112268_, p_112269_, p_112270_, p_112271_);
                    } catch (Throwable throwable) {
                        CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering Block Entity");
                        CrashReportCategory crashreportcategory = crashreport.addCategory("Block Entity Details");
                        p_112268_.fillCrashReportCategory(crashreportcategory);
                        throw new ReportedException(crashreport);
                    }
                }
            }
        }
    }

    private static <T extends BlockEntity> void setupAndRender(
        BlockEntityRenderer<T> p_112285_, T p_112286_, float p_112287_, PoseStack p_112288_, MultiBufferSource p_112289_
    ) {
        Level level = p_112286_.getLevel();
        int i;
        if (level != null) {
            i = LevelRenderer.getLightColor(level, p_112286_.getBlockPos());
        } else {
            i = 15728880;
        }

        p_112285_.render(p_112286_, p_112287_, p_112288_, p_112289_, i, OverlayTexture.NO_OVERLAY);
    }

    public void setLevel(@Nullable Level p_112258_) {
        this.level = p_112258_;
        if (p_112258_ == null) {
            this.camera = null;
        }
    }

    @Override
    public void onResourceManagerReload(ResourceManager p_173563_) {
        BlockEntityRendererProvider.Context blockentityrendererprovider$context = new BlockEntityRendererProvider.Context(
            this, this.blockRenderDispatcher, this.itemModelResolver, this.itemRenderer, this.entityRenderer, this.entityModelSet.get(), this.font
        );
        this.renderers = BlockEntityRenderers.createEntityRenderers(blockentityrendererprovider$context);
    }
}