package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.dto.RealmsServer;
import java.util.Objects;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.PopupScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.util.StringUtil;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsSettingsScreen extends RealmsScreen {
    private static final int COMPONENT_WIDTH = 212;
    private static final Component NAME_LABEL = Component.translatable("mco.configure.world.name");
    private static final Component DESCRIPTION_LABEL = Component.translatable("mco.configure.world.description");
    private final RealmsConfigureWorldScreen configureWorldScreen;
    private final RealmsServer serverData;
    private EditBox descEdit;
    private EditBox nameEdit;

    public RealmsSettingsScreen(RealmsConfigureWorldScreen p_89829_, RealmsServer p_89830_) {
        super(Component.translatable("mco.configure.world.settings.title"));
        this.configureWorldScreen = p_89829_;
        this.serverData = p_89830_;
    }

    @Override
    public void init() {
        int i = this.width / 2 - 106;
        String s = this.serverData.state == RealmsServer.State.OPEN ? "mco.configure.world.buttons.close" : "mco.configure.world.buttons.open";
        Button button = Button.builder(
                Component.translatable(s),
                p_340722_ -> {
                    if (this.serverData.state == RealmsServer.State.OPEN) {
                        this.minecraft
                            .setScreen(
                                RealmsPopups.infoPopupScreen(
                                    this, Component.translatable("mco.configure.world.close.question.line1"), p_340721_ -> this.configureWorldScreen.closeTheWorld()
                                )
                            );
                    } else {
                        this.configureWorldScreen.openTheWorld(false);
                    }
                }
            )
            .bounds(this.width / 2 - 53, row(0), 106, 20)
            .build();
        this.addRenderableWidget(button);
        this.nameEdit = new EditBox(this.minecraft.font, i, row(4), 212, 20, Component.translatable("mco.configure.world.name"));
        this.nameEdit.setMaxLength(32);
        this.nameEdit.setValue(Objects.requireNonNullElse(this.serverData.getName(), ""));
        this.addRenderableWidget(this.nameEdit);
        this.descEdit = new EditBox(this.minecraft.font, i, row(8), 212, 20, Component.translatable("mco.configure.world.description"));
        this.descEdit.setMaxLength(32);
        this.descEdit.setValue(this.serverData.getDescription());
        this.addRenderableWidget(this.descEdit);
        Button button1 = this.addRenderableWidget(
            Button.builder(Component.translatable("mco.configure.world.buttons.done"), p_89847_ -> this.save())
                .bounds(i - 2, row(12), 106, 20)
                .build()
        );
        this.nameEdit.setResponder(p_325161_ -> button1.active = !StringUtil.isBlank(p_325161_));
        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_CANCEL, p_308066_ -> this.onClose()).bounds(this.width / 2 + 2, row(12), 106, 20).build()
        );
    }

    @Override
    protected void setInitialFocus() {
        this.setInitialFocus(this.nameEdit);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.configureWorldScreen);
    }

    @Override
    public void render(GuiGraphics p_283580_, int p_281307_, int p_282074_, float p_282669_) {
        super.render(p_283580_, p_281307_, p_282074_, p_282669_);
        p_283580_.drawCenteredString(this.font, this.title, this.width / 2, 17, -1);
        p_283580_.drawString(this.font, NAME_LABEL, this.width / 2 - 106, row(3), -1);
        p_283580_.drawString(this.font, DESCRIPTION_LABEL, this.width / 2 - 106, row(7), -1);
    }

    public void save() {
        this.configureWorldScreen.saveSettings(this.nameEdit.getValue(), this.descEdit.getValue());
    }
}