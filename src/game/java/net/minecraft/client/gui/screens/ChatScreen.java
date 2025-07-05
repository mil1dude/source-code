package net.minecraft.client.gui.screens;

import javax.annotation.Nullable;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;

@OnlyIn(Dist.CLIENT)
public class ChatScreen extends Screen {
    public static final double MOUSE_SCROLL_SPEED = 7.0;
    private static final Component USAGE_TEXT = Component.translatable("chat_screen.usage");
    private static final int TOOLTIP_MAX_WIDTH = 210;
    private String historyBuffer = "";
    private int historyPos = -1;
    protected EditBox input;
    private String initial;
    CommandSuggestions commandSuggestions;

    public ChatScreen(String p_95579_) {
        super(Component.translatable("chat_screen.title"));
        this.initial = p_95579_;
    }

    @Override
    protected void init() {
        this.historyPos = this.minecraft.gui.getChat().getRecentChat().size();
        this.input = new EditBox(this.minecraft.fontFilterFishy, 4, this.height - 12, this.width - 4, 12, Component.translatable("chat.editBox")) {
            @Override
            protected MutableComponent createNarrationMessage() {
                return super.createNarrationMessage().append(ChatScreen.this.commandSuggestions.getNarrationMessage());
            }
        };
        this.input.setMaxLength(256);
        this.input.setBordered(false);
        this.input.setValue(this.initial);
        this.input.setResponder(this::onEdited);
        this.input.setCanLoseFocus(false);
        this.addWidget(this.input);
        this.commandSuggestions = new CommandSuggestions(this.minecraft, this, this.input, this.font, false, false, 1, 10, true, -805306368);
        this.commandSuggestions.setAllowHiding(false);
        this.commandSuggestions.updateCommandInfo();
    }

    @Override
    protected void setInitialFocus() {
        this.setInitialFocus(this.input);
    }

    @Override
    public void resize(Minecraft p_95600_, int p_95601_, int p_95602_) {
        String s = this.input.getValue();
        this.init(p_95600_, p_95601_, p_95602_);
        this.setChatLine(s);
        this.commandSuggestions.updateCommandInfo();
    }

    @Override
    public void removed() {
        this.minecraft.gui.getChat().resetChatScroll();
    }

    private void onEdited(String p_95611_) {
        String s = this.input.getValue();
        this.commandSuggestions.setAllowSuggestions(!s.equals(this.initial));
        this.commandSuggestions.updateCommandInfo();
    }

    @Override
    public boolean keyPressed(int p_95591_, int p_95592_, int p_95593_) {
        if (this.commandSuggestions.keyPressed(p_95591_, p_95592_, p_95593_)) {
            return true;
        } else if (super.keyPressed(p_95591_, p_95592_, p_95593_)) {
            return true;
        } else if (p_95591_ == 256) {
            this.minecraft.setScreen(null);
            return true;
        } else if (p_95591_ == 257 || p_95591_ == 335) {
            this.handleChatInput(this.input.getValue(), true);
            this.minecraft.setScreen(null);
            return true;
        } else if (p_95591_ == 265) {
            this.moveInHistory(-1);
            return true;
        } else if (p_95591_ == 264) {
            this.moveInHistory(1);
            return true;
        } else if (p_95591_ == 266) {
            this.minecraft.gui.getChat().scrollChat(this.minecraft.gui.getChat().getLinesPerPage() - 1);
            return true;
        } else if (p_95591_ == 267) {
            this.minecraft.gui.getChat().scrollChat(-this.minecraft.gui.getChat().getLinesPerPage() + 1);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseScrolled(double p_95581_, double p_95582_, double p_95583_, double p_300876_) {
        p_300876_ = Mth.clamp(p_300876_, -1.0, 1.0);
        if (this.commandSuggestions.mouseScrolled(p_300876_)) {
            return true;
        } else {
            if (!hasShiftDown()) {
                p_300876_ *= 7.0;
            }

            this.minecraft.gui.getChat().scrollChat((int)p_300876_);
            return true;
        }
    }

    @Override
    public boolean mouseClicked(double p_95585_, double p_95586_, int p_95587_) {
        if (this.commandSuggestions.mouseClicked((double)((int)p_95585_), (double)((int)p_95586_), p_95587_)) {
            return true;
        } else {
            if (p_95587_ == 0) {
                ChatComponent chatcomponent = this.minecraft.gui.getChat();
                if (chatcomponent.handleChatQueueClicked(p_95585_, p_95586_)) {
                    return true;
                }

                Style style = this.getComponentStyleAt(p_95585_, p_95586_);
                if (style != null && this.handleComponentClicked(style)) {
                    this.initial = this.input.getValue();
                    return true;
                }
            }

            return this.input.mouseClicked(p_95585_, p_95586_, p_95587_) ? true : super.mouseClicked(p_95585_, p_95586_, p_95587_);
        }
    }

    @Override
    protected void insertText(String p_95606_, boolean p_95607_) {
        if (p_95607_) {
            this.input.setValue(p_95606_);
        } else {
            this.input.insertText(p_95606_);
        }
    }

    public void moveInHistory(int p_95589_) {
        int i = this.historyPos + p_95589_;
        int j = this.minecraft.gui.getChat().getRecentChat().size();
        i = Mth.clamp(i, 0, j);
        if (i != this.historyPos) {
            if (i == j) {
                this.historyPos = j;
                this.input.setValue(this.historyBuffer);
            } else {
                if (this.historyPos == j) {
                    this.historyBuffer = this.input.getValue();
                }

                this.input.setValue(this.minecraft.gui.getChat().getRecentChat().get(i));
                this.commandSuggestions.setAllowSuggestions(false);
                this.historyPos = i;
            }
        }
    }

    @Override
    public void render(GuiGraphics p_282470_, int p_282674_, int p_282014_, float p_283132_) {
        this.minecraft.gui.getChat().render(p_282470_, this.minecraft.gui.getGuiTicks(), p_282674_, p_282014_, true);
        p_282470_.fill(2, this.height - 14, this.width - 2, this.height - 2, this.minecraft.options.getBackgroundColor(Integer.MIN_VALUE));
        this.input.render(p_282470_, p_282674_, p_282014_, p_283132_);
        super.render(p_282470_, p_282674_, p_282014_, p_283132_);
        p_282470_.pose().pushPose();
        p_282470_.pose().translate(0.0F, 0.0F, 200.0F);
        this.commandSuggestions.render(p_282470_, p_282674_, p_282014_);
        p_282470_.pose().popPose();
        GuiMessageTag guimessagetag = this.minecraft.gui.getChat().getMessageTagAt((double)p_282674_, (double)p_282014_);
        if (guimessagetag != null && guimessagetag.text() != null) {
            p_282470_.renderTooltip(this.font, this.font.split(guimessagetag.text(), 210), p_282674_, p_282014_);
        } else {
            Style style = this.getComponentStyleAt((double)p_282674_, (double)p_282014_);
            if (style != null && style.getHoverEvent() != null) {
                p_282470_.renderComponentHoverEffect(this.font, style, p_282674_, p_282014_);
            }
        }
    }

    @Override
    public void renderBackground(GuiGraphics p_298203_, int p_299897_, int p_297752_, float p_300216_) {
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void setChatLine(String p_95613_) {
        this.input.setValue(p_95613_);
    }

    @Override
    protected void updateNarrationState(NarrationElementOutput p_169238_) {
        p_169238_.add(NarratedElementType.TITLE, this.getTitle());
        p_169238_.add(NarratedElementType.USAGE, USAGE_TEXT);
        String s = this.input.getValue();
        if (!s.isEmpty()) {
            p_169238_.nest().add(NarratedElementType.TITLE, Component.translatable("chat_screen.message", s));
        }
    }

    @Nullable
    private Style getComponentStyleAt(double p_232702_, double p_232703_) {
        return this.minecraft.gui.getChat().getClickedComponentStyleAt(p_232702_, p_232703_);
    }

    public void handleChatInput(String p_242400_, boolean p_242161_) {
        p_242400_ = this.normalizeChatMessage(p_242400_);
        if (!p_242400_.isEmpty()) {
            if (p_242161_) {
                this.minecraft.gui.getChat().addRecentChat(p_242400_);
            }

            if (p_242400_.startsWith("/")) {
                this.minecraft.player.connection.sendCommand(p_242400_.substring(1));
            } else {
                this.minecraft.player.connection.sendChat(p_242400_);
            }
        }
    }

    public String normalizeChatMessage(String p_232707_) {
        return StringUtil.trimChatMessage(StringUtils.normalizeSpace(p_232707_.trim()));
    }
}