package net.minecraft.client.gui;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector2ic;

@OnlyIn(Dist.CLIENT)
public class GuiGraphics {
    public static final float MAX_GUI_Z = 10000.0F;
    public static final float MIN_GUI_Z = -10000.0F;
    private static final int EXTRA_SPACE_AFTER_FIRST_TOOLTIP_LINE = 2;
    private final Minecraft minecraft;
    private final PoseStack pose;
    private final MultiBufferSource.BufferSource bufferSource;
    private final GuiGraphics.ScissorStack scissorStack = new GuiGraphics.ScissorStack();
    private final GuiSpriteManager sprites;
    private final ItemStackRenderState scratchItemStackRenderState = new ItemStackRenderState();

    private GuiGraphics(Minecraft p_282144_, PoseStack p_281551_, MultiBufferSource.BufferSource p_281460_) {
        this.minecraft = p_282144_;
        this.pose = p_281551_;
        this.bufferSource = p_281460_;
        this.sprites = p_282144_.getGuiSprites();
    }

    public GuiGraphics(Minecraft p_283406_, MultiBufferSource.BufferSource p_282238_) {
        this(p_283406_, new PoseStack(), p_282238_);
    }

    public int guiWidth() {
        return this.minecraft.getWindow().getGuiScaledWidth();
    }

    public int guiHeight() {
        return this.minecraft.getWindow().getGuiScaledHeight();
    }

    public PoseStack pose() {
        return this.pose;
    }

    public void flush() {
        this.bufferSource.endBatch();
    }

    public void hLine(int p_283318_, int p_281662_, int p_281346_, int p_281672_) {
        this.hLine(RenderType.gui(), p_283318_, p_281662_, p_281346_, p_281672_);
    }

    public void hLine(RenderType p_286630_, int p_286453_, int p_286247_, int p_286814_, int p_286623_) {
        if (p_286247_ < p_286453_) {
            int i = p_286453_;
            p_286453_ = p_286247_;
            p_286247_ = i;
        }

        this.fill(p_286630_, p_286453_, p_286814_, p_286247_ + 1, p_286814_ + 1, p_286623_);
    }

    public void vLine(int p_282951_, int p_281591_, int p_281568_, int p_282718_) {
        this.vLine(RenderType.gui(), p_282951_, p_281591_, p_281568_, p_282718_);
    }

    public void vLine(RenderType p_286607_, int p_286309_, int p_286480_, int p_286707_, int p_286855_) {
        if (p_286707_ < p_286480_) {
            int i = p_286480_;
            p_286480_ = p_286707_;
            p_286707_ = i;
        }

        this.fill(p_286607_, p_286309_, p_286480_ + 1, p_286309_ + 1, p_286707_, p_286855_);
    }

    public void enableScissor(int p_281479_, int p_282788_, int p_282924_, int p_282826_) {
        ScreenRectangle screenrectangle = new ScreenRectangle(p_281479_, p_282788_, p_282924_ - p_281479_, p_282826_ - p_282788_)
            .transformAxisAligned(this.pose.last().pose());
        this.applyScissor(this.scissorStack.push(screenrectangle));
    }

    public void disableScissor() {
        this.applyScissor(this.scissorStack.pop());
    }

    public boolean containsPointInScissor(int p_334767_, int p_334338_) {
        return this.scissorStack.containsPoint(p_334767_, p_334338_);
    }

    private void applyScissor(@Nullable ScreenRectangle p_281569_) {
        this.flush();
        if (p_281569_ != null) {
            Window window = Minecraft.getInstance().getWindow();
            int i = window.getHeight();
            double d0 = window.getGuiScale();
            double d1 = (double)p_281569_.left() * d0;
            double d2 = (double)i - (double)p_281569_.bottom() * d0;
            double d3 = (double)p_281569_.width() * d0;
            double d4 = (double)p_281569_.height() * d0;
            RenderSystem.enableScissor((int)d1, (int)d2, Math.max(0, (int)d3), Math.max(0, (int)d4));
        } else {
            RenderSystem.disableScissor();
        }
    }

    public void fill(int p_282988_, int p_282861_, int p_281278_, int p_281710_, int p_281470_) {
        this.fill(p_282988_, p_282861_, p_281278_, p_281710_, 0, p_281470_);
    }

    public void fill(int p_281437_, int p_283660_, int p_282606_, int p_283413_, int p_283428_, int p_283253_) {
        this.fill(RenderType.gui(), p_281437_, p_283660_, p_282606_, p_283413_, p_283428_, p_283253_);
    }

    public void fill(RenderType p_286602_, int p_286738_, int p_286614_, int p_286741_, int p_286610_, int p_286560_) {
        this.fill(p_286602_, p_286738_, p_286614_, p_286741_, p_286610_, 0, p_286560_);
    }

    public void fill(RenderType p_286711_, int p_286234_, int p_286444_, int p_286244_, int p_286411_, int p_286671_, int p_286599_) {
        Matrix4f matrix4f = this.pose.last().pose();
        if (p_286234_ < p_286244_) {
            int i = p_286234_;
            p_286234_ = p_286244_;
            p_286244_ = i;
        }

        if (p_286444_ < p_286411_) {
            int j = p_286444_;
            p_286444_ = p_286411_;
            p_286411_ = j;
        }

        VertexConsumer vertexconsumer = this.bufferSource.getBuffer(p_286711_);
        vertexconsumer.addVertex(matrix4f, (float)p_286234_, (float)p_286444_, (float)p_286671_).setColor(p_286599_);
        vertexconsumer.addVertex(matrix4f, (float)p_286234_, (float)p_286411_, (float)p_286671_).setColor(p_286599_);
        vertexconsumer.addVertex(matrix4f, (float)p_286244_, (float)p_286411_, (float)p_286671_).setColor(p_286599_);
        vertexconsumer.addVertex(matrix4f, (float)p_286244_, (float)p_286444_, (float)p_286671_).setColor(p_286599_);
    }

    public void fillGradient(int p_283290_, int p_283278_, int p_282670_, int p_281698_, int p_283374_, int p_283076_) {
        this.fillGradient(p_283290_, p_283278_, p_282670_, p_281698_, 0, p_283374_, p_283076_);
    }

    public void fillGradient(int p_282702_, int p_282331_, int p_281415_, int p_283118_, int p_282419_, int p_281954_, int p_282607_) {
        this.fillGradient(RenderType.gui(), p_282702_, p_282331_, p_281415_, p_283118_, p_281954_, p_282607_, p_282419_);
    }

    public void fillGradient(RenderType p_286522_, int p_286535_, int p_286839_, int p_286242_, int p_286856_, int p_286809_, int p_286833_, int p_286706_) {
        VertexConsumer vertexconsumer = this.bufferSource.getBuffer(p_286522_);
        this.fillGradient(vertexconsumer, p_286535_, p_286839_, p_286242_, p_286856_, p_286706_, p_286809_, p_286833_);
    }

    private void fillGradient(VertexConsumer p_286862_, int p_283414_, int p_281397_, int p_283587_, int p_281521_, int p_283505_, int p_283131_, int p_282949_) {
        Matrix4f matrix4f = this.pose.last().pose();
        p_286862_.addVertex(matrix4f, (float)p_283414_, (float)p_281397_, (float)p_283505_).setColor(p_283131_);
        p_286862_.addVertex(matrix4f, (float)p_283414_, (float)p_281521_, (float)p_283505_).setColor(p_282949_);
        p_286862_.addVertex(matrix4f, (float)p_283587_, (float)p_281521_, (float)p_283505_).setColor(p_282949_);
        p_286862_.addVertex(matrix4f, (float)p_283587_, (float)p_281397_, (float)p_283505_).setColor(p_283131_);
    }

    public void fillRenderType(RenderType p_327925_, int p_328209_, int p_335424_, int p_329528_, int p_336385_, int p_332231_) {
        Matrix4f matrix4f = this.pose.last().pose();
        VertexConsumer vertexconsumer = this.bufferSource.getBuffer(p_327925_);
        vertexconsumer.addVertex(matrix4f, (float)p_328209_, (float)p_335424_, (float)p_332231_);
        vertexconsumer.addVertex(matrix4f, (float)p_328209_, (float)p_336385_, (float)p_332231_);
        vertexconsumer.addVertex(matrix4f, (float)p_329528_, (float)p_336385_, (float)p_332231_);
        vertexconsumer.addVertex(matrix4f, (float)p_329528_, (float)p_335424_, (float)p_332231_);
    }

    public void drawCenteredString(Font p_282122_, String p_282898_, int p_281490_, int p_282853_, int p_281258_) {
        this.drawString(p_282122_, p_282898_, p_281490_ - p_282122_.width(p_282898_) / 2, p_282853_, p_281258_);
    }

    public void drawCenteredString(Font p_282901_, Component p_282456_, int p_283083_, int p_282276_, int p_281457_) {
        FormattedCharSequence formattedcharsequence = p_282456_.getVisualOrderText();
        this.drawString(p_282901_, formattedcharsequence, p_283083_ - p_282901_.width(formattedcharsequence) / 2, p_282276_, p_281457_);
    }

    public void drawCenteredString(Font p_282592_, FormattedCharSequence p_281854_, int p_281573_, int p_283511_, int p_282577_) {
        this.drawString(p_282592_, p_281854_, p_281573_ - p_282592_.width(p_281854_) / 2, p_283511_, p_282577_);
    }

    public int drawString(Font p_282003_, @Nullable String p_281403_, int p_282714_, int p_282041_, int p_281908_) {
        return this.drawString(p_282003_, p_281403_, p_282714_, p_282041_, p_281908_, true);
    }

    public int drawString(Font p_283343_, @Nullable String p_281896_, int p_283569_, int p_283418_, int p_281560_, boolean p_282130_) {
        return p_281896_ == null
            ? 0
            : p_283343_.drawInBatch(
                p_281896_,
                (float)p_283569_,
                (float)p_283418_,
                p_281560_,
                p_282130_,
                this.pose.last().pose(),
                this.bufferSource,
                Font.DisplayMode.NORMAL,
                0,
                15728880
            );
    }

    public int drawString(Font p_283019_, FormattedCharSequence p_283376_, int p_283379_, int p_283346_, int p_282119_) {
        return this.drawString(p_283019_, p_283376_, p_283379_, p_283346_, p_282119_, true);
    }

    public int drawString(Font p_282636_, FormattedCharSequence p_281596_, int p_281586_, int p_282816_, int p_281743_, boolean p_282394_) {
        return p_282636_.drawInBatch(
            p_281596_,
            (float)p_281586_,
            (float)p_282816_,
            p_281743_,
            p_282394_,
            this.pose.last().pose(),
            this.bufferSource,
            Font.DisplayMode.NORMAL,
            0,
            15728880
        );
    }

    public int drawString(Font p_281653_, Component p_283140_, int p_283102_, int p_282347_, int p_281429_) {
        return this.drawString(p_281653_, p_283140_, p_283102_, p_282347_, p_281429_, true);
    }

    public int drawString(Font p_281547_, Component p_282131_, int p_282857_, int p_281250_, int p_282195_, boolean p_282791_) {
        return this.drawString(p_281547_, p_282131_.getVisualOrderText(), p_282857_, p_281250_, p_282195_, p_282791_);
    }

    public void drawWordWrap(Font p_281494_, FormattedText p_283463_, int p_282183_, int p_283250_, int p_282564_, int p_282629_) {
        this.drawWordWrap(p_281494_, p_283463_, p_282183_, p_283250_, p_282564_, p_282629_, true);
    }

    public void drawWordWrap(Font p_378519_, FormattedText p_378432_, int p_377858_, int p_376136_, int p_378596_, int p_378166_, boolean p_376508_) {
        for (FormattedCharSequence formattedcharsequence : p_378519_.split(p_378432_, p_378596_)) {
            this.drawString(p_378519_, formattedcharsequence, p_377858_, p_376136_, p_378166_, p_376508_);
            p_376136_ += 9;
        }
    }

    public int drawStringWithBackdrop(Font p_344926_, Component p_342324_, int p_342814_, int p_345075_, int p_343565_, int p_342787_) {
        int i = this.minecraft.options.getBackgroundColor(0.0F);
        if (i != 0) {
            int j = 2;
            this.fill(p_342814_ - 2, p_345075_ - 2, p_342814_ + p_343565_ + 2, p_345075_ + 9 + 2, ARGB.multiply(i, p_342787_));
        }

        return this.drawString(p_344926_, p_342324_, p_342814_, p_345075_, p_342787_, true);
    }

    public void renderOutline(int p_281496_, int p_282076_, int p_281334_, int p_283576_, int p_283618_) {
        this.fill(p_281496_, p_282076_, p_281496_ + p_281334_, p_282076_ + 1, p_283618_);
        this.fill(p_281496_, p_282076_ + p_283576_ - 1, p_281496_ + p_281334_, p_282076_ + p_283576_, p_283618_);
        this.fill(p_281496_, p_282076_ + 1, p_281496_ + 1, p_282076_ + p_283576_ - 1, p_283618_);
        this.fill(p_281496_ + p_281334_ - 1, p_282076_ + 1, p_281496_ + p_281334_, p_282076_ + p_283576_ - 1, p_283618_);
    }

    public void blitSprite(
        Function<ResourceLocation, RenderType> p_365180_, ResourceLocation p_298820_, int p_300417_, int p_298256_, int p_299965_, int p_300008_
    ) {
        this.blitSprite(p_365180_, p_298820_, p_300417_, p_298256_, p_299965_, p_300008_, -1);
    }

    public void blitSprite(
        Function<ResourceLocation, RenderType> p_367608_, ResourceLocation p_300860_, int p_298718_, int p_298541_, int p_300996_, int p_298426_, int p_364958_
    ) {
        TextureAtlasSprite textureatlassprite = this.sprites.getSprite(p_300860_);
        GuiSpriteScaling guispritescaling = this.sprites.getSpriteScaling(textureatlassprite);
        if (guispritescaling instanceof GuiSpriteScaling.Stretch) {
            this.blitSprite(p_367608_, textureatlassprite, p_298718_, p_298541_, p_300996_, p_298426_, p_364958_);
        } else if (guispritescaling instanceof GuiSpriteScaling.Tile guispritescaling$tile) {
            this.blitTiledSprite(
                p_367608_,
                textureatlassprite,
                p_298718_,
                p_298541_,
                p_300996_,
                p_298426_,
                0,
                0,
                guispritescaling$tile.width(),
                guispritescaling$tile.height(),
                guispritescaling$tile.width(),
                guispritescaling$tile.height(),
                p_364958_
            );
        } else if (guispritescaling instanceof GuiSpriteScaling.NineSlice guispritescaling$nineslice) {
            this.blitNineSlicedSprite(p_367608_, textureatlassprite, guispritescaling$nineslice, p_298718_, p_298541_, p_300996_, p_298426_, p_364958_);
        }
    }

    public void blitSprite(
        Function<ResourceLocation, RenderType> p_364730_,
        ResourceLocation p_364214_,
        int p_300402_,
        int p_300310_,
        int p_300994_,
        int p_297577_,
        int p_299466_,
        int p_301260_,
        int p_298369_,
        int p_300819_
    ) {
        TextureAtlasSprite textureatlassprite = this.sprites.getSprite(p_364214_);
        GuiSpriteScaling guispritescaling = this.sprites.getSpriteScaling(textureatlassprite);
        if (guispritescaling instanceof GuiSpriteScaling.Stretch) {
            this.blitSprite(p_364730_, textureatlassprite, p_300402_, p_300310_, p_300994_, p_297577_, p_299466_, p_301260_, p_298369_, p_300819_, -1);
        } else {
            this.enableScissor(p_299466_, p_301260_, p_299466_ + p_298369_, p_301260_ + p_300819_);
            this.blitSprite(p_364730_, p_364214_, p_299466_ - p_300994_, p_301260_ - p_297577_, p_300402_, p_300310_, -1);
            this.disableScissor();
        }
    }

    public void blitSprite(
        Function<ResourceLocation, RenderType> p_369326_, TextureAtlasSprite p_369444_, int p_297264_, int p_301178_, int p_297744_, int p_299331_
    ) {
        this.blitSprite(p_369326_, p_369444_, p_297264_, p_301178_, p_297744_, p_299331_, -1);
    }

    public void blitSprite(
        Function<ResourceLocation, RenderType> p_364852_,
        TextureAtlasSprite p_363987_,
        int p_301241_,
        int p_298760_,
        int p_299400_,
        int p_299966_,
        int p_298806_
    ) {
        if (p_299400_ != 0 && p_299966_ != 0) {
            this.innerBlit(
                p_364852_,
                p_363987_.atlasLocation(),
                p_301241_,
                p_301241_ + p_299400_,
                p_298760_,
                p_298760_ + p_299966_,
                p_363987_.getU0(),
                p_363987_.getU1(),
                p_363987_.getV0(),
                p_363987_.getV1(),
                p_298806_
            );
        }
    }

    private void blitSprite(
        Function<ResourceLocation, RenderType> p_365820_,
        TextureAtlasSprite p_299484_,
        int p_297573_,
        int p_300435_,
        int p_299725_,
        int p_300673_,
        int p_301130_,
        int p_362878_,
        int p_362501_,
        int p_362210_,
        int p_363944_
    ) {
        if (p_362501_ != 0 && p_362210_ != 0) {
            this.innerBlit(
                p_365820_,
                p_299484_.atlasLocation(),
                p_301130_,
                p_301130_ + p_362501_,
                p_362878_,
                p_362878_ + p_362210_,
                p_299484_.getU((float)p_299725_ / (float)p_297573_),
                p_299484_.getU((float)(p_299725_ + p_362501_) / (float)p_297573_),
                p_299484_.getV((float)p_300673_ / (float)p_300435_),
                p_299484_.getV((float)(p_300673_ + p_362210_) / (float)p_300435_),
                p_363944_
            );
        }
    }

    private void blitNineSlicedSprite(
        Function<ResourceLocation, RenderType> p_364789_,
        TextureAtlasSprite p_300154_,
        GuiSpriteScaling.NineSlice p_300599_,
        int p_297486_,
        int p_298301_,
        int p_299602_,
        int p_299587_,
        int p_299827_
    ) {
        GuiSpriteScaling.NineSlice.Border guispritescaling$nineslice$border = p_300599_.border();
        int i = Math.min(guispritescaling$nineslice$border.left(), p_299602_ / 2);
        int j = Math.min(guispritescaling$nineslice$border.right(), p_299602_ / 2);
        int k = Math.min(guispritescaling$nineslice$border.top(), p_299587_ / 2);
        int l = Math.min(guispritescaling$nineslice$border.bottom(), p_299587_ / 2);
        if (p_299602_ == p_300599_.width() && p_299587_ == p_300599_.height()) {
            this.blitSprite(p_364789_, p_300154_, p_300599_.width(), p_300599_.height(), 0, 0, p_297486_, p_298301_, p_299602_, p_299587_, p_299827_);
        } else if (p_299587_ == p_300599_.height()) {
            this.blitSprite(p_364789_, p_300154_, p_300599_.width(), p_300599_.height(), 0, 0, p_297486_, p_298301_, i, p_299587_, p_299827_);
            this.blitNineSliceInnerSegment(
                p_364789_,
                p_300599_,
                p_300154_,
                p_297486_ + i,
                p_298301_,
                p_299602_ - j - i,
                p_299587_,
                i,
                0,
                p_300599_.width() - j - i,
                p_300599_.height(),
                p_300599_.width(),
                p_300599_.height(),
                p_299827_
            );
            this.blitSprite(
                p_364789_,
                p_300154_,
                p_300599_.width(),
                p_300599_.height(),
                p_300599_.width() - j,
                0,
                p_297486_ + p_299602_ - j,
                p_298301_,
                j,
                p_299587_,
                p_299827_
            );
        } else if (p_299602_ == p_300599_.width()) {
            this.blitSprite(p_364789_, p_300154_, p_300599_.width(), p_300599_.height(), 0, 0, p_297486_, p_298301_, p_299602_, k, p_299827_);
            this.blitNineSliceInnerSegment(
                p_364789_,
                p_300599_,
                p_300154_,
                p_297486_,
                p_298301_ + k,
                p_299602_,
                p_299587_ - l - k,
                0,
                k,
                p_300599_.width(),
                p_300599_.height() - l - k,
                p_300599_.width(),
                p_300599_.height(),
                p_299827_
            );
            this.blitSprite(
                p_364789_,
                p_300154_,
                p_300599_.width(),
                p_300599_.height(),
                0,
                p_300599_.height() - l,
                p_297486_,
                p_298301_ + p_299587_ - l,
                p_299602_,
                l,
                p_299827_
            );
        } else {
            this.blitSprite(p_364789_, p_300154_, p_300599_.width(), p_300599_.height(), 0, 0, p_297486_, p_298301_, i, k, p_299827_);
            this.blitNineSliceInnerSegment(
                p_364789_,
                p_300599_,
                p_300154_,
                p_297486_ + i,
                p_298301_,
                p_299602_ - j - i,
                k,
                i,
                0,
                p_300599_.width() - j - i,
                k,
                p_300599_.width(),
                p_300599_.height(),
                p_299827_
            );
            this.blitSprite(
                p_364789_,
                p_300154_,
                p_300599_.width(),
                p_300599_.height(),
                p_300599_.width() - j,
                0,
                p_297486_ + p_299602_ - j,
                p_298301_,
                j,
                k,
                p_299827_
            );
            this.blitSprite(
                p_364789_,
                p_300154_,
                p_300599_.width(),
                p_300599_.height(),
                0,
                p_300599_.height() - l,
                p_297486_,
                p_298301_ + p_299587_ - l,
                i,
                l,
                p_299827_
            );
            this.blitNineSliceInnerSegment(
                p_364789_,
                p_300599_,
                p_300154_,
                p_297486_ + i,
                p_298301_ + p_299587_ - l,
                p_299602_ - j - i,
                l,
                i,
                p_300599_.height() - l,
                p_300599_.width() - j - i,
                l,
                p_300599_.width(),
                p_300599_.height(),
                p_299827_
            );
            this.blitSprite(
                p_364789_,
                p_300154_,
                p_300599_.width(),
                p_300599_.height(),
                p_300599_.width() - j,
                p_300599_.height() - l,
                p_297486_ + p_299602_ - j,
                p_298301_ + p_299587_ - l,
                j,
                l,
                p_299827_
            );
            this.blitNineSliceInnerSegment(
                p_364789_,
                p_300599_,
                p_300154_,
                p_297486_,
                p_298301_ + k,
                i,
                p_299587_ - l - k,
                0,
                k,
                i,
                p_300599_.height() - l - k,
                p_300599_.width(),
                p_300599_.height(),
                p_299827_
            );
            this.blitNineSliceInnerSegment(
                p_364789_,
                p_300599_,
                p_300154_,
                p_297486_ + i,
                p_298301_ + k,
                p_299602_ - j - i,
                p_299587_ - l - k,
                i,
                k,
                p_300599_.width() - j - i,
                p_300599_.height() - l - k,
                p_300599_.width(),
                p_300599_.height(),
                p_299827_
            );
            this.blitNineSliceInnerSegment(
                p_364789_,
                p_300599_,
                p_300154_,
                p_297486_ + p_299602_ - j,
                p_298301_ + k,
                j,
                p_299587_ - l - k,
                p_300599_.width() - j,
                k,
                j,
                p_300599_.height() - l - k,
                p_300599_.width(),
                p_300599_.height(),
                p_299827_
            );
        }
    }

    private void blitNineSliceInnerSegment(
        Function<ResourceLocation, RenderType> p_362420_,
        GuiSpriteScaling.NineSlice p_361460_,
        TextureAtlasSprite p_364978_,
        int p_364957_,
        int p_367994_,
        int p_362572_,
        int p_366826_,
        int p_365488_,
        int p_366188_,
        int p_369698_,
        int p_362666_,
        int p_367341_,
        int p_362743_,
        int p_364128_
    ) {
        if (p_362572_ > 0 && p_366826_ > 0) {
            if (p_361460_.stretchInner()) {
                this.innerBlit(
                    p_362420_,
                    p_364978_.atlasLocation(),
                    p_364957_,
                    p_364957_ + p_362572_,
                    p_367994_,
                    p_367994_ + p_366826_,
                    p_364978_.getU((float)p_365488_ / (float)p_367341_),
                    p_364978_.getU((float)(p_365488_ + p_369698_) / (float)p_367341_),
                    p_364978_.getV((float)p_366188_ / (float)p_362743_),
                    p_364978_.getV((float)(p_366188_ + p_362666_) / (float)p_362743_),
                    p_364128_
                );
            } else {
                this.blitTiledSprite(
                    p_362420_,
                    p_364978_,
                    p_364957_,
                    p_367994_,
                    p_362572_,
                    p_366826_,
                    p_365488_,
                    p_366188_,
                    p_369698_,
                    p_362666_,
                    p_367341_,
                    p_362743_,
                    p_364128_
                );
            }
        }
    }

    private void blitTiledSprite(
        Function<ResourceLocation, RenderType> p_364593_,
        TextureAtlasSprite p_298835_,
        int p_297456_,
        int p_300732_,
        int p_297241_,
        int p_300646_,
        int p_299561_,
        int p_298797_,
        int p_299557_,
        int p_297684_,
        int p_299756_,
        int p_297303_,
        int p_299619_
    ) {
        if (p_297241_ > 0 && p_300646_ > 0) {
            if (p_299557_ > 0 && p_297684_ > 0) {
                for (int i = 0; i < p_297241_; i += p_299557_) {
                    int j = Math.min(p_299557_, p_297241_ - i);

                    for (int k = 0; k < p_300646_; k += p_297684_) {
                        int l = Math.min(p_297684_, p_300646_ - k);
                        this.blitSprite(p_364593_, p_298835_, p_299756_, p_297303_, p_299561_, p_298797_, p_297456_ + i, p_300732_ + k, j, l, p_299619_);
                    }
                }
            } else {
                throw new IllegalArgumentException("Tiled sprite texture size must be positive, got " + p_299557_ + "x" + p_297684_);
            }
        }
    }

    public void blit(
        Function<ResourceLocation, RenderType> p_367278_,
        ResourceLocation p_282639_,
        int p_282732_,
        int p_283541_,
        float p_282660_,
        float p_281522_,
        int p_281760_,
        int p_283298_,
        int p_283429_,
        int p_282193_,
        int p_281980_
    ) {
        this.blit(
            p_367278_, p_282639_, p_282732_, p_283541_, p_282660_, p_281522_, p_281760_, p_283298_, p_281760_, p_283298_, p_283429_, p_282193_, p_281980_
        );
    }

    public void blit(
        Function<ResourceLocation, RenderType> p_365997_,
        ResourceLocation p_361724_,
        int p_282225_,
        int p_281487_,
        float p_365061_,
        float p_368643_,
        int p_281985_,
        int p_281329_,
        int p_283035_,
        int p_366975_
    ) {
        this.blit(p_365997_, p_361724_, p_282225_, p_281487_, p_365061_, p_368643_, p_281985_, p_281329_, p_281985_, p_281329_, p_283035_, p_366975_);
    }

    public void blit(
        Function<ResourceLocation, RenderType> p_366246_,
        ResourceLocation p_283377_,
        int p_281970_,
        int p_282111_,
        float p_367108_,
        float p_362374_,
        int p_283134_,
        int p_282778_,
        int p_281478_,
        int p_281821_,
        int p_361382_,
        int p_365327_
    ) {
        this.blit(p_366246_, p_283377_, p_281970_, p_282111_, p_367108_, p_362374_, p_283134_, p_282778_, p_281478_, p_281821_, p_361382_, p_365327_, -1);
    }

    public void blit(
        Function<ResourceLocation, RenderType> p_363581_,
        ResourceLocation p_283573_,
        int p_283574_,
        int p_283670_,
        float p_283029_,
        float p_283061_,
        int p_283545_,
        int p_282845_,
        int p_282558_,
        int p_282832_,
        int p_281851_,
        int p_366628_,
        int p_364363_
    ) {
        this.innerBlit(
            p_363581_,
            p_283573_,
            p_283574_,
            p_283574_ + p_283545_,
            p_283670_,
            p_283670_ + p_282845_,
            (p_283029_ + 0.0F) / (float)p_281851_,
            (p_283029_ + (float)p_282558_) / (float)p_281851_,
            (p_283061_ + 0.0F) / (float)p_366628_,
            (p_283061_ + (float)p_282832_) / (float)p_366628_,
            p_364363_
        );
    }

    private void innerBlit(
        Function<ResourceLocation, RenderType> p_368273_,
        ResourceLocation p_283254_,
        int p_283092_,
        int p_281930_,
        int p_282113_,
        int p_281388_,
        float p_281327_,
        float p_281676_,
        float p_283166_,
        float p_282630_,
        int p_283583_
    ) {
        RenderType rendertype = p_368273_.apply(p_283254_);
        Matrix4f matrix4f = this.pose.last().pose();
        VertexConsumer vertexconsumer = this.bufferSource.getBuffer(rendertype);
        vertexconsumer.addVertex(matrix4f, (float)p_283092_, (float)p_282113_, 0.0F).setUv(p_281327_, p_283166_).setColor(p_283583_);
        vertexconsumer.addVertex(matrix4f, (float)p_283092_, (float)p_281388_, 0.0F).setUv(p_281327_, p_282630_).setColor(p_283583_);
        vertexconsumer.addVertex(matrix4f, (float)p_281930_, (float)p_281388_, 0.0F).setUv(p_281676_, p_282630_).setColor(p_283583_);
        vertexconsumer.addVertex(matrix4f, (float)p_281930_, (float)p_282113_, 0.0F).setUv(p_281676_, p_283166_).setColor(p_283583_);
    }

    public void renderItem(ItemStack p_281978_, int p_282647_, int p_281944_) {
        this.renderItem(this.minecraft.player, this.minecraft.level, p_281978_, p_282647_, p_281944_, 0);
    }

    public void renderItem(ItemStack p_282262_, int p_283221_, int p_283496_, int p_283435_) {
        this.renderItem(this.minecraft.player, this.minecraft.level, p_282262_, p_283221_, p_283496_, p_283435_);
    }

    public void renderItem(ItemStack p_282786_, int p_282502_, int p_282976_, int p_281592_, int p_282314_) {
        this.renderItem(this.minecraft.player, this.minecraft.level, p_282786_, p_282502_, p_282976_, p_281592_, p_282314_);
    }

    public void renderFakeItem(ItemStack p_281946_, int p_283299_, int p_283674_) {
        this.renderFakeItem(p_281946_, p_283299_, p_283674_, 0);
    }

    public void renderFakeItem(ItemStack p_309605_, int p_310104_, int p_309448_, int p_310674_) {
        this.renderItem(null, this.minecraft.level, p_309605_, p_310104_, p_309448_, p_310674_);
    }

    public void renderItem(LivingEntity p_282154_, ItemStack p_282777_, int p_282110_, int p_281371_, int p_283572_) {
        this.renderItem(p_282154_, p_282154_.level(), p_282777_, p_282110_, p_281371_, p_283572_);
    }

    private void renderItem(@Nullable LivingEntity p_283524_, @Nullable Level p_282461_, ItemStack p_283653_, int p_283141_, int p_282560_, int p_282425_) {
        this.renderItem(p_283524_, p_282461_, p_283653_, p_283141_, p_282560_, p_282425_, 0);
    }

    private void renderItem(
        @Nullable LivingEntity p_282619_, @Nullable Level p_281754_, ItemStack p_281675_, int p_281271_, int p_282210_, int p_283260_, int p_281995_
    ) {
        if (!p_281675_.isEmpty()) {
            this.minecraft.getItemModelResolver().updateForTopItem(this.scratchItemStackRenderState, p_281675_, ItemDisplayContext.GUI, false, p_281754_, p_282619_, p_283260_);
            this.pose.pushPose();
            this.pose.translate((float)(p_281271_ + 8), (float)(p_282210_ + 8), (float)(150 + (this.scratchItemStackRenderState.isGui3d() ? p_281995_ : 0)));

            try {
                this.pose.scale(16.0F, -16.0F, 16.0F);
                boolean flag = !this.scratchItemStackRenderState.usesBlockLight();
                if (flag) {
                    this.flush();
                    Lighting.setupForFlatItems();
                }

                this.scratchItemStackRenderState.render(this.pose, this.bufferSource, 15728880, OverlayTexture.NO_OVERLAY);
                this.flush();
                if (flag) {
                    Lighting.setupFor3DItems();
                }
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering item");
                CrashReportCategory crashreportcategory = crashreport.addCategory("Item being rendered");
                crashreportcategory.setDetail("Item Type", () -> String.valueOf(p_281675_.getItem()));
                crashreportcategory.setDetail("Item Components", () -> String.valueOf(p_281675_.getComponents()));
                crashreportcategory.setDetail("Item Foil", () -> String.valueOf(p_281675_.hasFoil()));
                throw new ReportedException(crashreport);
            }

            this.pose.popPose();
        }
    }

    public void renderItemDecorations(Font p_281721_, ItemStack p_281514_, int p_282056_, int p_282683_) {
        this.renderItemDecorations(p_281721_, p_281514_, p_282056_, p_282683_, null);
    }

    public void renderItemDecorations(Font p_282005_, ItemStack p_283349_, int p_282641_, int p_282146_, @Nullable String p_282803_) {
        if (!p_283349_.isEmpty()) {
            this.pose.pushPose();
            this.renderItemBar(p_283349_, p_282641_, p_282146_);
            this.renderItemCount(p_282005_, p_283349_, p_282641_, p_282146_, p_282803_);
            this.renderItemCooldown(p_283349_, p_282641_, p_282146_);
            this.pose.popPose();
        }
    }

    public void renderTooltip(Font p_282308_, ItemStack p_282781_, int p_282687_, int p_282292_) {
        this.renderTooltip(
            p_282308_, Screen.getTooltipFromItem(this.minecraft, p_282781_), p_282781_.getTooltipImage(), p_282687_, p_282292_, p_282781_.get(DataComponents.TOOLTIP_STYLE)
        );
    }

    public void renderTooltip(Font p_283128_, List<Component> p_282716_, Optional<TooltipComponent> p_281682_, int p_283678_, int p_281696_) {
        this.renderTooltip(p_283128_, p_282716_, p_281682_, p_283678_, p_281696_, null);
    }

    public void renderTooltip(
        Font p_362491_, List<Component> p_368544_, Optional<TooltipComponent> p_362815_, int p_366300_, int p_368952_, @Nullable ResourceLocation p_368469_
    ) {
        List<ClientTooltipComponent> list = p_368544_.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).collect(Util.toMutableList());
        p_362815_.ifPresent(p_325321_ -> list.add(list.isEmpty() ? 0 : 1, ClientTooltipComponent.create(p_325321_)));
        this.renderTooltipInternal(p_362491_, list, p_366300_, p_368952_, DefaultTooltipPositioner.INSTANCE, p_368469_);
    }

    public void renderTooltip(Font p_282269_, Component p_282572_, int p_282044_, int p_282545_) {
        this.renderTooltip(p_282269_, p_282572_, p_282044_, p_282545_, null);
    }

    public void renderTooltip(Font p_366149_, Component p_366436_, int p_364277_, int p_361100_, @Nullable ResourceLocation p_361238_) {
        this.renderTooltip(p_366149_, List.of(p_366436_.getVisualOrderText()), p_364277_, p_361100_, p_361238_);
    }

    public void renderComponentTooltip(Font p_282739_, List<Component> p_281832_, int p_282191_, int p_282446_) {
        this.renderComponentTooltip(p_282739_, p_281832_, p_282191_, p_282446_, null);
    }

    public void renderComponentTooltip(Font p_369090_, List<Component> p_365405_, int p_368143_, int p_366244_, @Nullable ResourceLocation p_364763_) {
        this.renderTooltipInternal(
            p_369090_,
            p_365405_.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).toList(),
            p_368143_,
            p_366244_,
            DefaultTooltipPositioner.INSTANCE,
            p_364763_
        );
    }

    public void renderTooltip(Font p_282192_, List<? extends FormattedCharSequence> p_282297_, int p_281680_, int p_283325_) {
        this.renderTooltip(p_282192_, p_282297_, p_281680_, p_283325_, null);
    }

    public void renderTooltip(Font p_368879_, List<? extends FormattedCharSequence> p_368774_, int p_369836_, int p_368099_, @Nullable ResourceLocation p_362582_) {
        this.renderTooltipInternal(
            p_368879_,
            p_368774_.stream().map(ClientTooltipComponent::create).collect(Collectors.toList()),
            p_369836_,
            p_368099_,
            DefaultTooltipPositioner.INSTANCE,
            p_362582_
        );
    }

    public void renderTooltip(Font p_281627_, List<FormattedCharSequence> p_283313_, ClientTooltipPositioner p_283571_, int p_282367_, int p_282806_) {
        this.renderTooltipInternal(p_281627_, p_283313_.stream().map(ClientTooltipComponent::create).collect(Collectors.toList()), p_282367_, p_282806_, p_283571_, null);
    }

    private void renderTooltipInternal(
        Font p_282675_,
        List<ClientTooltipComponent> p_282615_,
        int p_283230_,
        int p_283417_,
        ClientTooltipPositioner p_282442_,
        @Nullable ResourceLocation p_368234_
    ) {
        if (!p_282615_.isEmpty()) {
            int i = 0;
            int j = p_282615_.size() == 1 ? -2 : 0;

            for (ClientTooltipComponent clienttooltipcomponent : p_282615_) {
                int k = clienttooltipcomponent.getWidth(p_282675_);
                if (k > i) {
                    i = k;
                }

                j += clienttooltipcomponent.getHeight(p_282675_);
            }

            int i2 = i;
            int j2 = j;
            Vector2ic vector2ic = p_282442_.positionTooltip(this.guiWidth(), this.guiHeight(), p_283230_, p_283417_, i, j);
            int l = vector2ic.x();
            int i1 = vector2ic.y();
            this.pose.pushPose();
            int j1 = 400;
            TooltipRenderUtil.renderTooltipBackground(this, l, i1, i, j, 400, p_368234_);
            this.pose.translate(0.0F, 0.0F, 400.0F);
            int k1 = i1;

            for (int l1 = 0; l1 < p_282615_.size(); l1++) {
                ClientTooltipComponent clienttooltipcomponent1 = p_282615_.get(l1);
                clienttooltipcomponent1.renderText(p_282675_, l, k1, this.pose.last().pose(), this.bufferSource);
                k1 += clienttooltipcomponent1.getHeight(p_282675_) + (l1 == 0 ? 2 : 0);
            }

            k1 = i1;

            for (int k2 = 0; k2 < p_282615_.size(); k2++) {
                ClientTooltipComponent clienttooltipcomponent2 = p_282615_.get(k2);
                clienttooltipcomponent2.renderImage(p_282675_, l, k1, i2, j2, this);
                k1 += clienttooltipcomponent2.getHeight(p_282675_) + (k2 == 0 ? 2 : 0);
            }

            this.pose.popPose();
        }
    }

    private void renderItemBar(ItemStack p_367359_, int p_362139_, int p_368464_) {
        if (p_367359_.isBarVisible()) {
            int i = p_362139_ + 2;
            int j = p_368464_ + 13;
            this.fill(RenderType.gui(), i, j, i + 13, j + 2, 200, -16777216);
            this.fill(RenderType.gui(), i, j, i + p_367359_.getBarWidth(), j + 1, 200, ARGB.opaque(p_367359_.getBarColor()));
        }
    }

    private void renderItemCount(Font p_363240_, ItemStack p_367163_, int p_369299_, int p_364530_, @Nullable String p_368187_) {
        if (p_367163_.getCount() != 1 || p_368187_ != null) {
            String s = p_368187_ == null ? String.valueOf(p_367163_.getCount()) : p_368187_;
            this.pose.pushPose();
            this.pose.translate(0.0F, 0.0F, 200.0F);
            this.drawString(p_363240_, s, p_369299_ + 19 - 2 - p_363240_.width(s), p_364530_ + 6 + 3, -1, true);
            this.pose.popPose();
        }
    }

    private void renderItemCooldown(ItemStack p_365241_, int p_364235_, int p_369346_) {
        LocalPlayer localplayer = this.minecraft.player;
        float f = localplayer == null ? 0.0F : localplayer.getCooldowns().getCooldownPercent(p_365241_, this.minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(true));
        if (f > 0.0F) {
            int i = p_369346_ + Mth.floor(16.0F * (1.0F - f));
            int j = i + Mth.ceil(16.0F * f);
            this.fill(RenderType.gui(), p_364235_, i, p_364235_ + 16, j, 200, Integer.MAX_VALUE);
        }
    }

    public void renderComponentHoverEffect(Font p_282584_, @Nullable Style p_282156_, int p_283623_, int p_282114_) {
        if (p_282156_ != null && p_282156_.getHoverEvent() != null) {
            HoverEvent hoverevent = p_282156_.getHoverEvent();
            HoverEvent.ItemStackInfo hoverevent$itemstackinfo = hoverevent.getValue(HoverEvent.Action.SHOW_ITEM);
            if (hoverevent$itemstackinfo != null) {
                this.renderTooltip(p_282584_, hoverevent$itemstackinfo.getItemStack(), p_283623_, p_282114_);
            } else {
                HoverEvent.EntityTooltipInfo hoverevent$entitytooltipinfo = hoverevent.getValue(HoverEvent.Action.SHOW_ENTITY);
                if (hoverevent$entitytooltipinfo != null) {
                    if (this.minecraft.options.advancedItemTooltips) {
                        this.renderComponentTooltip(p_282584_, hoverevent$entitytooltipinfo.getTooltipLines(), p_283623_, p_282114_);
                    }
                } else {
                    Component component = hoverevent.getValue(HoverEvent.Action.SHOW_TEXT);
                    if (component != null) {
                        this.renderTooltip(p_282584_, p_282584_.split(component, Math.max(this.guiWidth() / 2, 200)), p_283623_, p_282114_);
                    }
                }
            }
        }
    }

    public void drawSpecial(Consumer<MultiBufferSource> p_367429_) {
        p_367429_.accept(this.bufferSource);
        this.bufferSource.endBatch();
    }

    @OnlyIn(Dist.CLIENT)
    static class ScissorStack {
        private final Deque<ScreenRectangle> stack = new ArrayDeque<>();

        public ScreenRectangle push(ScreenRectangle p_281812_) {
            ScreenRectangle screenrectangle = this.stack.peekLast();
            if (screenrectangle != null) {
                ScreenRectangle screenrectangle1 = Objects.requireNonNullElse(p_281812_.intersection(screenrectangle), ScreenRectangle.empty());
                this.stack.addLast(screenrectangle1);
                return screenrectangle1;
            } else {
                this.stack.addLast(p_281812_);
                return p_281812_;
            }
        }

        @Nullable
        public ScreenRectangle pop() {
            if (this.stack.isEmpty()) {
                throw new IllegalStateException("Scissor stack underflow");
            } else {
                this.stack.removeLast();
                return this.stack.peekLast();
            }
        }

        public boolean containsPoint(int p_329411_, int p_333404_) {
            return this.stack.isEmpty() ? true : this.stack.peek().containsPoint(p_329411_, p_333404_);
        }
    }
}