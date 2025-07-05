package net.minecraft.client.gui.screens.options;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LanguageSelectScreen extends OptionsSubScreen {
    private static final Component WARNING_LABEL = Component.translatable("options.languageAccuracyWarning").withColor(-4539718);
    private static final int FOOTER_HEIGHT = 53;
    private LanguageSelectScreen.LanguageSelectionList languageSelectionList;
    final LanguageManager languageManager;

    public LanguageSelectScreen(Screen p_344210_, Options p_342264_, LanguageManager p_343432_) {
        super(p_344210_, p_342264_, Component.translatable("options.language.title"));
        this.languageManager = p_343432_;
        this.layout.setFooterHeight(53);
    }

    @Override
    protected void addContents() {
        this.languageSelectionList = this.layout.addToContents(new LanguageSelectScreen.LanguageSelectionList(this.minecraft));
    }

    @Override
    protected void addOptions() {
    }

    @Override
    protected void addFooter() {
        LinearLayout linearlayout = this.layout.addToFooter(LinearLayout.vertical()).spacing(8);
        linearlayout.defaultCellSetting().alignHorizontallyCenter();
        linearlayout.addChild(new StringWidget(WARNING_LABEL, this.font));
        LinearLayout linearlayout1 = linearlayout.addChild(LinearLayout.horizontal().spacing(8));
        linearlayout1.addChild(
            Button.builder(Component.translatable("options.font"), p_343010_ -> this.minecraft.setScreen(new FontOptionsScreen(this, this.options))).build()
        );
        linearlayout1.addChild(Button.builder(CommonComponents.GUI_DONE, p_343186_ -> this.onDone()).build());
    }

    @Override
    protected void repositionElements() {
        super.repositionElements();
        this.languageSelectionList.updateSize(this.width, this.layout);
    }

    void onDone() {
        LanguageSelectScreen.LanguageSelectionList.Entry languageselectscreen$languageselectionlist$entry = this.languageSelectionList.getSelected();
        if (languageselectscreen$languageselectionlist$entry != null
            && !languageselectscreen$languageselectionlist$entry.code.equals(this.languageManager.getSelected())) {
            this.languageManager.setSelected(languageselectscreen$languageselectionlist$entry.code);
            this.options.languageCode = languageselectscreen$languageselectionlist$entry.code;
            this.minecraft.reloadResourcePacks();
        }

        this.minecraft.setScreen(this.lastScreen);
    }

    @OnlyIn(Dist.CLIENT)
    class LanguageSelectionList extends ObjectSelectionList<LanguageSelectScreen.LanguageSelectionList.Entry> {
        public LanguageSelectionList(final Minecraft p_343433_) {
            super(p_343433_, LanguageSelectScreen.this.width, LanguageSelectScreen.this.height - 33 - 53, 33, 18);
            String s = LanguageSelectScreen.this.languageManager.getSelected();
            LanguageSelectScreen.this.languageManager
                .getLanguages()
                .forEach(
                    (p_342614_, p_342581_) -> {
                        LanguageSelectScreen.LanguageSelectionList.Entry languageselectscreen$languageselectionlist$entry = new LanguageSelectScreen.LanguageSelectionList.Entry(
                            p_342614_, p_342581_
                        );
                        this.addEntry(languageselectscreen$languageselectionlist$entry);
                        if (s.equals(p_342614_)) {
                            this.setSelected(languageselectscreen$languageselectionlist$entry);
                        }
                    }
                );
            if (this.getSelected() != null) {
                this.centerScrollOn(this.getSelected());
            }
        }

        @Override
        public int getRowWidth() {
            return super.getRowWidth() + 50;
        }

        @OnlyIn(Dist.CLIENT)
        public class Entry extends ObjectSelectionList.Entry<LanguageSelectScreen.LanguageSelectionList.Entry> {
            final String code;
            private final Component language;
            private long lastClickTime;

            public Entry(final String p_344457_, final LanguageInfo p_342261_) {
                this.code = p_344457_;
                this.language = p_342261_.toComponent();
            }

            @Override
            public void render(
                GuiGraphics p_344847_,
                int p_342936_,
                int p_344271_,
                int p_343473_,
                int p_343669_,
                int p_343479_,
                int p_342702_,
                int p_343137_,
                boolean p_344510_,
                float p_345266_
            ) {
                p_344847_.drawCenteredString(
                    LanguageSelectScreen.this.font, this.language, LanguageSelectionList.this.width / 2, p_344271_ + p_343479_ / 2 - 9 / 2, -1
                );
            }

            @Override
            public boolean keyPressed(int p_343262_, int p_344349_, int p_345020_) {
                if (CommonInputs.selected(p_343262_)) {
                    this.select();
                    LanguageSelectScreen.this.onDone();
                    return true;
                } else {
                    return super.keyPressed(p_343262_, p_344349_, p_345020_);
                }
            }

            @Override
            public boolean mouseClicked(double p_342624_, double p_342099_, int p_344982_) {
                this.select();
                if (Util.getMillis() - this.lastClickTime < 250L) {
                    LanguageSelectScreen.this.onDone();
                }

                this.lastClickTime = Util.getMillis();
                return super.mouseClicked(p_342624_, p_342099_, p_344982_);
            }

            private void select() {
                LanguageSelectionList.this.setSelected(this);
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", this.language);
            }
        }
    }
}