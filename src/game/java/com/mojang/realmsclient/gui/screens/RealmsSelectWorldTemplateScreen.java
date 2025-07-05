package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.dto.WorldTemplatePaginatedList;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.util.RealmsTextureManager;
import com.mojang.realmsclient.util.TextRenderingUtils;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonLinks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsSelectWorldTemplateScreen extends RealmsScreen {
    static final Logger LOGGER = LogUtils.getLogger();
    static final ResourceLocation SLOT_FRAME_SPRITE = ResourceLocation.withDefaultNamespace("widget/slot_frame");
    private static final Component SELECT_BUTTON_NAME = Component.translatable("mco.template.button.select");
    private static final Component TRAILER_BUTTON_NAME = Component.translatable("mco.template.button.trailer");
    private static final Component PUBLISHER_BUTTON_NAME = Component.translatable("mco.template.button.publisher");
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_SPACING = 10;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    final Consumer<WorldTemplate> callback;
    RealmsSelectWorldTemplateScreen.WorldTemplateList worldTemplateList;
    private final RealmsServer.WorldType worldType;
    private Button selectButton;
    private Button trailerButton;
    private Button publisherButton;
    @Nullable
    WorldTemplate selectedTemplate = null;
    @Nullable
    String currentLink;
    @Nullable
    private Component[] warning;
    @Nullable
    List<TextRenderingUtils.Line> noTemplatesMessage;

    public RealmsSelectWorldTemplateScreen(Component p_167481_, Consumer<WorldTemplate> p_167482_, RealmsServer.WorldType p_167483_) {
        this(p_167481_, p_167482_, p_167483_, null);
    }

    public RealmsSelectWorldTemplateScreen(
        Component p_167485_, Consumer<WorldTemplate> p_167486_, RealmsServer.WorldType p_167487_, @Nullable WorldTemplatePaginatedList p_167488_
    ) {
        super(p_167485_);
        this.callback = p_167486_;
        this.worldType = p_167487_;
        if (p_167488_ == null) {
            this.worldTemplateList = new RealmsSelectWorldTemplateScreen.WorldTemplateList();
            this.fetchTemplatesAsync(new WorldTemplatePaginatedList(10));
        } else {
            this.worldTemplateList = new RealmsSelectWorldTemplateScreen.WorldTemplateList(Lists.newArrayList(p_167488_.templates));
            this.fetchTemplatesAsync(p_167488_);
        }
    }

    public void setWarning(Component... p_89683_) {
        this.warning = p_89683_;
    }

    @Override
    public void init() {
        this.layout.addTitleHeader(this.title, this.font);
        this.worldTemplateList = this.layout.addToContents(new RealmsSelectWorldTemplateScreen.WorldTemplateList(this.worldTemplateList.getTemplates()));
        LinearLayout linearlayout = this.layout.addToFooter(LinearLayout.horizontal().spacing(10));
        linearlayout.defaultCellSetting().alignHorizontallyCenter();
        this.trailerButton = linearlayout.addChild(Button.builder(TRAILER_BUTTON_NAME, p_89701_ -> this.onTrailer()).width(100).build());
        this.selectButton = linearlayout.addChild(Button.builder(SELECT_BUTTON_NAME, p_89696_ -> this.selectTemplate()).width(100).build());
        linearlayout.addChild(Button.builder(CommonComponents.GUI_CANCEL, p_89691_ -> this.onClose()).width(100).build());
        this.publisherButton = linearlayout.addChild(Button.builder(PUBLISHER_BUTTON_NAME, p_89679_ -> this.onPublish()).width(100).build());
        this.updateButtonStates();
        this.layout.visitWidgets(p_325159_ -> {
            AbstractWidget abstractwidget = this.addRenderableWidget(p_325159_);
        });
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.worldTemplateList.setSize(this.width, this.height - this.layout.getFooterHeight() - this.getHeaderHeight());
        this.layout.arrangeElements();
    }

    @Override
    public Component getNarrationMessage() {
        List<Component> list = Lists.newArrayListWithCapacity(2);
        list.add(this.title);
        if (this.warning != null) {
            list.addAll(Arrays.asList(this.warning));
        }

        return CommonComponents.joinLines(list);
    }

    void updateButtonStates() {
        this.publisherButton.visible = this.selectedTemplate != null && !this.selectedTemplate.link.isEmpty();
        this.trailerButton.visible = this.selectedTemplate != null && !this.selectedTemplate.trailer.isEmpty();
        this.selectButton.active = this.selectedTemplate != null;
    }

    @Override
    public void onClose() {
        this.callback.accept(null);
    }

    private void selectTemplate() {
        if (this.selectedTemplate != null) {
            this.callback.accept(this.selectedTemplate);
        }
    }

    private void onTrailer() {
        if (this.selectedTemplate != null && !this.selectedTemplate.trailer.isBlank()) {
            ConfirmLinkScreen.confirmLinkNow(this, this.selectedTemplate.trailer);
        }
    }

    private void onPublish() {
        if (this.selectedTemplate != null && !this.selectedTemplate.link.isBlank()) {
            ConfirmLinkScreen.confirmLinkNow(this, this.selectedTemplate.link);
        }
    }

    private void fetchTemplatesAsync(final WorldTemplatePaginatedList p_89654_) {
        (new Thread("realms-template-fetcher") {
                @Override
                public void run() {
                    WorldTemplatePaginatedList worldtemplatepaginatedlist = p_89654_;
                    RealmsClient realmsclient = RealmsClient.create();

                    while (worldtemplatepaginatedlist != null) {
                        Either<WorldTemplatePaginatedList, Exception> either = RealmsSelectWorldTemplateScreen.this.fetchTemplates(
                            worldtemplatepaginatedlist, realmsclient
                        );
                        worldtemplatepaginatedlist = RealmsSelectWorldTemplateScreen.this.minecraft
                            .submit(
                                () -> {
                                    if (either.right().isPresent()) {
                                        RealmsSelectWorldTemplateScreen.LOGGER.error("Couldn't fetch templates", either.right().get());
                                        if (RealmsSelectWorldTemplateScreen.this.worldTemplateList.isEmpty()) {
                                            RealmsSelectWorldTemplateScreen.this.noTemplatesMessage = TextRenderingUtils.decompose(
                                                I18n.get("mco.template.select.failure")
                                            );
                                        }

                                        return null;
                                    } else {
                                        WorldTemplatePaginatedList worldtemplatepaginatedlist1 = either.left().get();

                                        for (WorldTemplate worldtemplate : worldtemplatepaginatedlist1.templates) {
                                            RealmsSelectWorldTemplateScreen.this.worldTemplateList.addEntry(worldtemplate);
                                        }

                                        if (worldtemplatepaginatedlist1.templates.isEmpty()) {
                                            if (RealmsSelectWorldTemplateScreen.this.worldTemplateList.isEmpty()) {
                                                String s = I18n.get("mco.template.select.none", "%link");
                                                TextRenderingUtils.LineSegment textrenderingutils$linesegment = TextRenderingUtils.LineSegment.link(
                                                    I18n.get("mco.template.select.none.linkTitle"), CommonLinks.REALMS_CONTENT_CREATION.toString()
                                                );
                                                RealmsSelectWorldTemplateScreen.this.noTemplatesMessage = TextRenderingUtils.decompose(s, textrenderingutils$linesegment);
                                            }

                                            return null;
                                        } else {
                                            return worldtemplatepaginatedlist1;
                                        }
                                    }
                                }
                            )
                            .join();
                    }
                }
            })
            .start();
    }

    Either<WorldTemplatePaginatedList, Exception> fetchTemplates(WorldTemplatePaginatedList p_89656_, RealmsClient p_89657_) {
        try {
            return Either.left(p_89657_.fetchWorldTemplates(p_89656_.page + 1, p_89656_.size, this.worldType));
        } catch (RealmsServiceException realmsserviceexception) {
            return Either.right(realmsserviceexception);
        }
    }

    @Override
    public void render(GuiGraphics p_282162_, int p_89640_, int p_89641_, float p_89642_) {
        super.render(p_282162_, p_89640_, p_89641_, p_89642_);
        this.currentLink = null;
        if (this.noTemplatesMessage != null) {
            this.renderMultilineMessage(p_282162_, p_89640_, p_89641_, this.noTemplatesMessage);
        }

        if (this.warning != null) {
            for (int i = 0; i < this.warning.length; i++) {
                Component component = this.warning[i];
                p_282162_.drawCenteredString(this.font, component, this.width / 2, row(-1 + i), -6250336);
            }
        }
    }

    private void renderMultilineMessage(GuiGraphics p_282398_, int p_282163_, int p_282021_, List<TextRenderingUtils.Line> p_282203_) {
        for (int i = 0; i < p_282203_.size(); i++) {
            TextRenderingUtils.Line textrenderingutils$line = p_282203_.get(i);
            int j = row(4 + i);
            int k = textrenderingutils$line.segments.stream().mapToInt(p_280748_ -> this.font.width(p_280748_.renderedText())).sum();
            int l = this.width / 2 - k / 2;

            for (TextRenderingUtils.LineSegment textrenderingutils$linesegment : textrenderingutils$line.segments) {
                int i1 = textrenderingutils$linesegment.isLink() ? 3368635 : -1;
                int j1 = p_282398_.drawString(this.font, textrenderingutils$linesegment.renderedText(), l, j, i1);
                if (textrenderingutils$linesegment.isLink() && p_282163_ > l && p_282163_ < j1 && p_282021_ > j - 3 && p_282021_ < j + 8) {
                    this.setTooltipForNextRenderPass(Component.literal(textrenderingutils$linesegment.getLinkUrl()));
                    this.currentLink = textrenderingutils$linesegment.getLinkUrl();
                }

                l = j1;
            }
        }
    }

    int getHeaderHeight() {
        return this.warning != null ? row(1) : 33;
    }

    @OnlyIn(Dist.CLIENT)
    class Entry extends ObjectSelectionList.Entry<RealmsSelectWorldTemplateScreen.Entry> {
        private static final WidgetSprites WEBSITE_LINK_SPRITES = new WidgetSprites(
            ResourceLocation.withDefaultNamespace("icon/link"), ResourceLocation.withDefaultNamespace("icon/link_highlighted")
        );
        private static final WidgetSprites TRAILER_LINK_SPRITES = new WidgetSprites(
            ResourceLocation.withDefaultNamespace("icon/video_link"), ResourceLocation.withDefaultNamespace("icon/video_link_highlighted")
        );
        private static final Component PUBLISHER_LINK_TOOLTIP = Component.translatable("mco.template.info.tooltip");
        private static final Component TRAILER_LINK_TOOLTIP = Component.translatable("mco.template.trailer.tooltip");
        public final WorldTemplate template;
        private long lastClickTime;
        @Nullable
        private ImageButton websiteButton;
        @Nullable
        private ImageButton trailerButton;

        public Entry(final WorldTemplate p_89753_) {
            this.template = p_89753_;
            if (!p_89753_.link.isBlank()) {
                this.websiteButton = new ImageButton(
                    15, 15, WEBSITE_LINK_SPRITES, ConfirmLinkScreen.confirmLink(RealmsSelectWorldTemplateScreen.this, p_89753_.link), PUBLISHER_LINK_TOOLTIP
                );
                this.websiteButton.setTooltip(Tooltip.create(PUBLISHER_LINK_TOOLTIP));
            }

            if (!p_89753_.trailer.isBlank()) {
                this.trailerButton = new ImageButton(
                    15, 15, TRAILER_LINK_SPRITES, ConfirmLinkScreen.confirmLink(RealmsSelectWorldTemplateScreen.this, p_89753_.trailer), TRAILER_LINK_TOOLTIP
                );
                this.trailerButton.setTooltip(Tooltip.create(TRAILER_LINK_TOOLTIP));
            }
        }

        @Override
        public boolean mouseClicked(double p_299958_, double p_298696_, int p_299792_) {
            RealmsSelectWorldTemplateScreen.this.selectedTemplate = this.template;
            RealmsSelectWorldTemplateScreen.this.updateButtonStates();
            if (Util.getMillis() - this.lastClickTime < 250L && this.isFocused()) {
                RealmsSelectWorldTemplateScreen.this.callback.accept(this.template);
            }

            this.lastClickTime = Util.getMillis();
            if (this.websiteButton != null) {
                this.websiteButton.mouseClicked(p_299958_, p_298696_, p_299792_);
            }

            if (this.trailerButton != null) {
                this.trailerButton.mouseClicked(p_299958_, p_298696_, p_299792_);
            }

            return super.mouseClicked(p_299958_, p_298696_, p_299792_);
        }

        @Override
        public void render(
            GuiGraphics p_281796_,
            int p_282160_,
            int p_281759_,
            int p_282961_,
            int p_281497_,
            int p_282427_,
            int p_283550_,
            int p_282955_,
            boolean p_282866_,
            float p_281452_
        ) {
            p_281796_.blit(
                RenderType::guiTextured,
                RealmsTextureManager.worldTemplate(this.template.id, this.template.image),
                p_282961_ + 1,
                p_281759_ + 1 + 1,
                0.0F,
                0.0F,
                38,
                38,
                38,
                38
            );
            p_281796_.blitSprite(RenderType::guiTextured, RealmsSelectWorldTemplateScreen.SLOT_FRAME_SPRITE, p_282961_, p_281759_ + 1, 40, 40);
            int i = 5;
            int j = RealmsSelectWorldTemplateScreen.this.font.width(this.template.version);
            if (this.websiteButton != null) {
                this.websiteButton.setPosition(p_282961_ + p_281497_ - j - this.websiteButton.getWidth() - 10, p_281759_);
                this.websiteButton.render(p_281796_, p_283550_, p_282955_, p_281452_);
            }

            if (this.trailerButton != null) {
                this.trailerButton.setPosition(p_282961_ + p_281497_ - j - this.trailerButton.getWidth() * 2 - 15, p_281759_);
                this.trailerButton.render(p_281796_, p_283550_, p_282955_, p_281452_);
            }

            int k = p_282961_ + 45 + 20;
            int l = p_281759_ + 5;
            p_281796_.drawString(RealmsSelectWorldTemplateScreen.this.font, this.template.name, k, l, -1);
            p_281796_.drawString(RealmsSelectWorldTemplateScreen.this.font, this.template.version, p_282961_ + p_281497_ - j - 5, l, 7105644);
            p_281796_.drawString(RealmsSelectWorldTemplateScreen.this.font, this.template.author, k, l + 9 + 5, -6250336);
            if (!this.template.recommendedPlayers.isBlank()) {
                p_281796_.drawString(RealmsSelectWorldTemplateScreen.this.font, this.template.recommendedPlayers, k, p_281759_ + p_282427_ - 9 / 2 - 5, 5000268);
            }
        }

        @Override
        public Component getNarration() {
            Component component = CommonComponents.joinLines(
                Component.literal(this.template.name),
                Component.translatable("mco.template.select.narrate.authors", this.template.author),
                Component.literal(this.template.recommendedPlayers),
                Component.translatable("mco.template.select.narrate.version", this.template.version)
            );
            return Component.translatable("narrator.select", component);
        }
    }

    @OnlyIn(Dist.CLIENT)
    class WorldTemplateList extends ObjectSelectionList<RealmsSelectWorldTemplateScreen.Entry> {
        public WorldTemplateList() {
            this(Collections.emptyList());
        }

        public WorldTemplateList(final Iterable<WorldTemplate> p_89795_) {
            super(Minecraft.getInstance(), RealmsSelectWorldTemplateScreen.this.width, RealmsSelectWorldTemplateScreen.this.height - 33 - RealmsSelectWorldTemplateScreen.this.getHeaderHeight(), RealmsSelectWorldTemplateScreen.this.getHeaderHeight(), 46);
            p_89795_.forEach(this::addEntry);
        }

        public void addEntry(WorldTemplate p_89805_) {
            this.addEntry(RealmsSelectWorldTemplateScreen.this.new Entry(p_89805_));
        }

        @Override
        public boolean mouseClicked(double p_89797_, double p_89798_, int p_89799_) {
            if (RealmsSelectWorldTemplateScreen.this.currentLink != null) {
                ConfirmLinkScreen.confirmLinkNow(RealmsSelectWorldTemplateScreen.this, RealmsSelectWorldTemplateScreen.this.currentLink);
                return true;
            } else {
                return super.mouseClicked(p_89797_, p_89798_, p_89799_);
            }
        }

        public void setSelected(@Nullable RealmsSelectWorldTemplateScreen.Entry p_89807_) {
            super.setSelected(p_89807_);
            RealmsSelectWorldTemplateScreen.this.selectedTemplate = p_89807_ == null ? null : p_89807_.template;
            RealmsSelectWorldTemplateScreen.this.updateButtonStates();
        }

        @Override
        public int getRowWidth() {
            return 300;
        }

        public boolean isEmpty() {
            return this.getItemCount() == 0;
        }

        public List<WorldTemplate> getTemplates() {
            return this.children().stream().map(p_89814_ -> p_89814_.template).collect(Collectors.toList());
        }
    }
}
