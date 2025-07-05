package net.minecraft.client.gui.screens.multiplayer;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.ServerLinks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ServerLinksScreen extends Screen {
    private static final int LINK_BUTTON_WIDTH = 310;
    private static final int DEFAULT_ITEM_HEIGHT = 25;
    private static final Component TITLE = Component.translatable("menu.server_links.title");
    private final Screen lastScreen;
    @Nullable
    private ServerLinksScreen.LinkList list;
    final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    final ServerLinks links;

    public ServerLinksScreen(Screen p_342231_, ServerLinks p_343353_) {
        super(TITLE);
        this.lastScreen = p_342231_;
        this.links = p_343353_;
    }

    @Override
    protected void init() {
        this.layout.addTitleHeader(this.title, this.font);
        this.list = this.layout.addToContents(new ServerLinksScreen.LinkList(this.minecraft, this.width, this));
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_BACK, p_344159_ -> this.onClose()).width(200).build());
        this.layout.visitWidgets(p_345150_ -> {
            AbstractWidget abstractwidget = this.addRenderableWidget(p_345150_);
        });
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        if (this.list != null) {
            this.list.updateSize(this.width, this.layout);
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    @OnlyIn(Dist.CLIENT)
    static class LinkList extends ContainerObjectSelectionList<ServerLinksScreen.LinkListEntry> {
        public LinkList(Minecraft p_343649_, int p_342240_, ServerLinksScreen p_344656_) {
            super(p_343649_, p_342240_, p_344656_.layout.getContentHeight(), p_344656_.layout.getHeaderHeight(), 25);
            p_344656_.links.entries().forEach(p_345105_ -> this.addEntry(new ServerLinksScreen.LinkListEntry(p_344656_, p_345105_)));
        }

        @Override
        public int getRowWidth() {
            return 310;
        }

        @Override
        public void updateSize(int p_342406_, HeaderAndFooterLayout p_343115_) {
            super.updateSize(p_342406_, p_343115_);
            int i = p_342406_ / 2 - 155;
            this.children().forEach(p_342392_ -> p_342392_.button.setX(i));
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class LinkListEntry extends ContainerObjectSelectionList.Entry<ServerLinksScreen.LinkListEntry> {
        final AbstractWidget button;

        LinkListEntry(Screen p_344819_, ServerLinks.Entry p_343451_) {
            this.button = Button.builder(p_343451_.displayName(), ConfirmLinkScreen.confirmLink(p_344819_, p_343451_.link(), false))
                .width(310)
                .build();
        }

        @Override
        public void render(
            GuiGraphics p_344940_,
            int p_343211_,
            int p_342411_,
            int p_344825_,
            int p_342491_,
            int p_342714_,
            int p_344661_,
            int p_344790_,
            boolean p_344345_,
            float p_343822_
        ) {
            this.button.setY(p_342411_);
            this.button.render(p_344940_, p_344661_, p_344790_, p_343822_);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(this.button);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of(this.button);
        }
    }
}