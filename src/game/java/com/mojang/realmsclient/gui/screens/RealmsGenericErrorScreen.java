package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.client.RealmsError;
import com.mojang.realmsclient.exception.RealmsServiceException;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsGenericErrorScreen extends RealmsScreen {
    private final Screen nextScreen;
    private final RealmsGenericErrorScreen.ErrorMessage lines;
    private MultiLineLabel line2Split = MultiLineLabel.EMPTY;

    public RealmsGenericErrorScreen(RealmsServiceException p_88669_, Screen p_88670_) {
        super(GameNarrator.NO_TITLE);
        this.nextScreen = p_88670_;
        this.lines = errorMessage(p_88669_);
    }

    public RealmsGenericErrorScreen(Component p_88672_, Screen p_88673_) {
        super(GameNarrator.NO_TITLE);
        this.nextScreen = p_88673_;
        this.lines = errorMessage(p_88672_);
    }

    public RealmsGenericErrorScreen(Component p_88675_, Component p_88676_, Screen p_88677_) {
        super(GameNarrator.NO_TITLE);
        this.nextScreen = p_88677_;
        this.lines = errorMessage(p_88675_, p_88676_);
    }

    private static RealmsGenericErrorScreen.ErrorMessage errorMessage(RealmsServiceException p_288965_) {
        RealmsError realmserror = p_288965_.realmsError;
        return errorMessage(Component.translatable("mco.errorMessage.realmsService.realmsError", realmserror.errorCode()), realmserror.errorMessage());
    }

    private static RealmsGenericErrorScreen.ErrorMessage errorMessage(Component p_289003_) {
        return errorMessage(Component.translatable("mco.errorMessage.generic"), p_289003_);
    }

    private static RealmsGenericErrorScreen.ErrorMessage errorMessage(Component p_289010_, Component p_289015_) {
        return new RealmsGenericErrorScreen.ErrorMessage(p_289010_, p_289015_);
    }

    @Override
    public void init() {
        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_OK, p_325126_ -> this.onClose())
                .bounds(this.width / 2 - 100, this.height - 52, 200, 20)
                .build()
        );
        this.line2Split = MultiLineLabel.create(this.font, this.lines.detail, this.width * 3 / 4);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.nextScreen);
    }

    @Override
    public Component getNarrationMessage() {
        return Component.empty().append(this.lines.title).append(": ").append(this.lines.detail);
    }

    @Override
    public void render(GuiGraphics p_283497_, int p_88680_, int p_88681_, float p_88682_) {
        super.render(p_283497_, p_88680_, p_88681_, p_88682_);
        p_283497_.drawCenteredString(this.font, this.lines.title, this.width / 2, 80, -1);
        this.line2Split.renderCentered(p_283497_, this.width / 2, 100, 9, -2142128);
    }

    @OnlyIn(Dist.CLIENT)
    static record ErrorMessage(Component title, Component detail) {
    }
}