package com.mojang.realmsclient.gui.screens;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.CommonLayouts;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.util.StringUtil;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsInviteScreen extends RealmsScreen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component TITLE = Component.translatable("mco.configure.world.buttons.invite");
    private static final Component NAME_LABEL = Component.translatable("mco.configure.world.invite.profile.name").withColor(-6250336);
    private static final Component INVITING_PLAYER_TEXT = Component.translatable("mco.configure.world.players.inviting").withColor(-6250336);
    private static final Component NO_SUCH_PLAYER_ERROR_TEXT = Component.translatable("mco.configure.world.players.error").withColor(-65536);
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private EditBox profileName;
    private Button inviteButton;
    private final RealmsServer serverData;
    private final RealmsConfigureWorldScreen configureScreen;
    private final Screen lastScreen;
    @Nullable
    private Component message;

    public RealmsInviteScreen(RealmsConfigureWorldScreen p_88703_, Screen p_88704_, RealmsServer p_88705_) {
        super(TITLE);
        this.configureScreen = p_88703_;
        this.lastScreen = p_88704_;
        this.serverData = p_88705_;
    }

    @Override
    public void init() {
        this.layout.addTitleHeader(TITLE, this.font);
        LinearLayout linearlayout = this.layout.addToContents(LinearLayout.vertical().spacing(8));
        this.profileName = new EditBox(this.minecraft.font, 200, 20, Component.translatable("mco.configure.world.invite.profile.name"));
        linearlayout.addChild(CommonLayouts.labeledElement(this.font, this.profileName, NAME_LABEL));
        this.inviteButton = linearlayout.addChild(Button.builder(TITLE, p_88721_ -> this.onInvite()).width(200).build());
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_BACK, p_325127_ -> this.onClose()).width(200).build());
        this.layout.visitWidgets(p_325129_ -> {
            AbstractWidget abstractwidget = this.addRenderableWidget(p_325129_);
        });
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
    }

    @Override
    protected void setInitialFocus() {
        this.setInitialFocus(this.profileName);
    }

    private void onInvite() {
        if (StringUtil.isBlank(this.profileName.getValue())) {
            this.showMessage(NO_SUCH_PLAYER_ERROR_TEXT);
        } else {
            long i = this.serverData.id;
            String s = this.profileName.getValue().trim();
            this.inviteButton.active = false;
            this.profileName.setEditable(false);
            this.showMessage(INVITING_PLAYER_TEXT);
            CompletableFuture.<RealmsServer>supplyAsync(() -> {
                try {
                    return RealmsClient.create().invite(i, s);
                } catch (Exception exception) {
                    LOGGER.error("Couldn't invite user");
                    return null;
                }
            }, Util.ioPool()).thenAcceptAsync(p_289618_ -> {
                if (p_289618_ != null) {
                    this.serverData.players = p_289618_.players;
                    this.minecraft.setScreen(new RealmsPlayerScreen(this.configureScreen, this.serverData));
                } else {
                    this.showMessage(NO_SUCH_PLAYER_ERROR_TEXT);
                }

                this.profileName.setEditable(true);
                this.inviteButton.active = true;
            }, this.screenExecutor);
        }
    }

    private void showMessage(Component p_289685_) {
        this.message = p_289685_;
        this.minecraft.getNarrator().sayNow(p_289685_);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    @Override
    public void render(GuiGraphics p_282206_, int p_283415_, int p_282016_, float p_283011_) {
        super.render(p_282206_, p_283415_, p_282016_, p_283011_);
        if (this.message != null) {
            p_282206_.drawCenteredString(this.font, this.message, this.width / 2, this.inviteButton.getY() + this.inviteButton.getHeight() + 8, -1);
        }
    }
}