package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Optionull;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.Style;
import net.minecraft.util.ARGB;
import net.minecraft.util.ArrayListDeque;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ChatComponent {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_CHAT_HISTORY = 100;
    private static final int MESSAGE_NOT_FOUND = -1;
    private static final int MESSAGE_INDENT = 4;
    private static final int MESSAGE_TAG_MARGIN_LEFT = 4;
    private static final int BOTTOM_MARGIN = 40;
    private static final int TIME_BEFORE_MESSAGE_DELETION = 60;
    private static final Component DELETED_CHAT_MESSAGE = Component.translatable("chat.deleted_marker").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
    private final Minecraft minecraft;
    private final ArrayListDeque<String> recentChat = new ArrayListDeque<>(100);
    private final List<GuiMessage> allMessages = Lists.newArrayList();
    private final List<GuiMessage.Line> trimmedMessages = Lists.newArrayList();
    private int chatScrollbarPos;
    private boolean newMessageSinceScroll;
    private final List<ChatComponent.DelayedMessageDeletion> messageDeletionQueue = new ArrayList<>();

    public ChatComponent(Minecraft p_93768_) {
        this.minecraft = p_93768_;
        this.recentChat.addAll(p_93768_.commandHistory().history());
    }

    public void tick() {
        if (!this.messageDeletionQueue.isEmpty()) {
            this.processMessageDeletionQueue();
        }
    }

    public void render(GuiGraphics p_282077_, int p_283491_, int p_282406_, int p_283111_, boolean p_328818_) {
        if (!this.isChatHidden()) {
            int i = this.getLinesPerPage();
            int j = this.trimmedMessages.size();
            if (j > 0) {
                ProfilerFiller profilerfiller = Profiler.get();
                profilerfiller.push("chat");
                float f = (float)this.getScale();
                int k = Mth.ceil((float)this.getWidth() / f);
                int l = p_282077_.guiHeight();
                p_282077_.pose().pushPose();
                p_282077_.pose().scale(f, f, 1.0F);
                p_282077_.pose().translate(4.0F, 0.0F, 0.0F);
                int i1 = Mth.floor((float)(l - 40) / f);
                int j1 = this.getMessageEndIndexAt(this.screenToChatX((double)p_282406_), this.screenToChatY((double)p_283111_));
                double d0 = this.minecraft.options.chatOpacity().get() * 0.9 + 0.1;
                double d1 = this.minecraft.options.textBackgroundOpacity().get();
                double d2 = this.minecraft.options.chatLineSpacing().get();
                int k1 = this.getLineHeight();
                int l1 = (int)Math.round(-8.0 * (d2 + 1.0) + 4.0 * d2);
                int i2 = 0;

                for (int j2 = 0; j2 + this.chatScrollbarPos < this.trimmedMessages.size() && j2 < i; j2++) {
                    int k2 = j2 + this.chatScrollbarPos;
                    GuiMessage.Line guimessage$line = this.trimmedMessages.get(k2);
                    if (guimessage$line != null) {
                        int l2 = p_283491_ - guimessage$line.addedTime();
                        if (l2 < 200 || p_328818_) {
                            double d3 = p_328818_ ? 1.0 : getTimeFactor(l2);
                            int j3 = (int)(255.0 * d3 * d0);
                            int k3 = (int)(255.0 * d3 * d1);
                            i2++;
                            if (j3 > 3) {
                                int l3 = 0;
                                int i4 = i1 - j2 * k1;
                                int j4 = i4 + l1;
                                p_282077_.fill(-4, i4 - k1, 0 + k + 4 + 4, i4, k3 << 24);
                                GuiMessageTag guimessagetag = guimessage$line.tag();
                                if (guimessagetag != null) {
                                    int k4 = guimessagetag.indicatorColor() | j3 << 24;
                                    p_282077_.fill(-4, i4 - k1, -2, i4, k4);
                                    if (k2 == j1 && guimessagetag.icon() != null) {
                                        int l4 = this.getTagIconLeft(guimessage$line);
                                        int i5 = j4 + 9;
                                        this.drawTagIcon(p_282077_, l4, i5, guimessagetag.icon());
                                    }
                                }

                                p_282077_.pose().pushPose();
                                p_282077_.pose().translate(0.0F, 0.0F, 50.0F);
                                p_282077_.drawString(this.minecraft.font, guimessage$line.content(), 0, j4, ARGB.color(j3, -1));
                                p_282077_.pose().popPose();
                            }
                        }
                    }
                }

                long j5 = this.minecraft.getChatListener().queueSize();
                if (j5 > 0L) {
                    int k5 = (int)(128.0 * d0);
                    int i6 = (int)(255.0 * d1);
                    p_282077_.pose().pushPose();
                    p_282077_.pose().translate(0.0F, (float)i1, 0.0F);
                    p_282077_.fill(-2, 0, k + 4, 9, i6 << 24);
                    p_282077_.pose().translate(0.0F, 0.0F, 50.0F);
                    p_282077_.drawString(this.minecraft.font, Component.translatable("chat.queue", j5), 0, 1, 16777215 + (k5 << 24));
                    p_282077_.pose().popPose();
                }

                if (p_328818_) {
                    int l5 = this.getLineHeight();
                    int j6 = j * l5;
                    int k6 = i2 * l5;
                    int i3 = this.chatScrollbarPos * k6 / j - i1;
                    int l6 = k6 * k6 / j6;
                    if (j6 != k6) {
                        int i7 = i3 > 0 ? 170 : 96;
                        int j7 = this.newMessageSinceScroll ? 13382451 : 3355562;
                        int k7 = k + 4;
                        p_282077_.fill(k7, -i3, k7 + 2, -i3 - l6, 100, j7 + (i7 << 24));
                        p_282077_.fill(k7 + 2, -i3, k7 + 1, -i3 - l6, 100, 13421772 + (i7 << 24));
                    }
                }

                p_282077_.pose().popPose();
                profilerfiller.pop();
            }
        }
    }

    private void drawTagIcon(GuiGraphics p_283206_, int p_281677_, int p_281878_, GuiMessageTag.Icon p_282783_) {
        int i = p_281878_ - p_282783_.height - 1;
        p_282783_.draw(p_283206_, p_281677_, i);
    }

    private int getTagIconLeft(GuiMessage.Line p_240622_) {
        return this.minecraft.font.width(p_240622_.content()) + 4;
    }

    private boolean isChatHidden() {
        return this.minecraft.options.chatVisibility().get() == ChatVisiblity.HIDDEN;
    }

    private static double getTimeFactor(int p_93776_) {
        double d0 = (double)p_93776_ / 200.0;
        d0 = 1.0 - d0;
        d0 *= 10.0;
        d0 = Mth.clamp(d0, 0.0, 1.0);
        return d0 * d0;
    }

    public void clearMessages(boolean p_93796_) {
        this.minecraft.getChatListener().clearQueue();
        this.messageDeletionQueue.clear();
        this.trimmedMessages.clear();
        this.allMessages.clear();
        if (p_93796_) {
            this.recentChat.clear();
            this.recentChat.addAll(this.minecraft.commandHistory().history());
        }
    }

    public void addMessage(Component p_93786_) {
        this.addMessage(p_93786_, null, this.minecraft.isSingleplayer() ? GuiMessageTag.systemSinglePlayer() : GuiMessageTag.system());
    }

    public void addMessage(Component p_241484_, @Nullable MessageSignature p_241323_, @Nullable GuiMessageTag p_241297_) {
        GuiMessage guimessage = new GuiMessage(this.minecraft.gui.getGuiTicks(), p_241484_, p_241323_, p_241297_);
        this.logChatMessage(guimessage);
        this.addMessageToDisplayQueue(guimessage);
        this.addMessageToQueue(guimessage);
    }

    private void logChatMessage(GuiMessage p_328461_) {
        String s = p_328461_.content().getString().replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n");
        String s1 = Optionull.map(p_328461_.tag(), GuiMessageTag::logTag);
        if (s1 != null) {
            LOGGER.info("[{}] [CHAT] {}", s1, s);
        } else {
            LOGGER.info("[CHAT] {}", s);
        }
    }

    private void addMessageToDisplayQueue(GuiMessage p_332173_) {
        int i = Mth.floor((double)this.getWidth() / this.getScale());
        GuiMessageTag.Icon guimessagetag$icon = p_332173_.icon();
        if (guimessagetag$icon != null) {
            i -= guimessagetag$icon.width + 4 + 2;
        }

        List<FormattedCharSequence> list = ComponentRenderUtils.wrapComponents(p_332173_.content(), i, this.minecraft.font);
        boolean flag = this.isChatFocused();

        for (int j = 0; j < list.size(); j++) {
            FormattedCharSequence formattedcharsequence = list.get(j);
            if (flag && this.chatScrollbarPos > 0) {
                this.newMessageSinceScroll = true;
                this.scrollChat(1);
            }

            boolean flag1 = j == list.size() - 1;
            this.trimmedMessages.add(0, new GuiMessage.Line(p_332173_.addedTime(), formattedcharsequence, p_332173_.tag(), flag1));
        }

        while (this.trimmedMessages.size() > 100) {
            this.trimmedMessages.remove(this.trimmedMessages.size() - 1);
        }
    }

    private void addMessageToQueue(GuiMessage p_330648_) {
        this.allMessages.add(0, p_330648_);

        while (this.allMessages.size() > 100) {
            this.allMessages.remove(this.allMessages.size() - 1);
        }
    }

    private void processMessageDeletionQueue() {
        int i = this.minecraft.gui.getGuiTicks();
        this.messageDeletionQueue.removeIf(p_250713_ -> i >= p_250713_.deletableAfter() ? this.deleteMessageOrDelay(p_250713_.signature()) == null : false);
    }

    public void deleteMessage(MessageSignature p_241324_) {
        ChatComponent.DelayedMessageDeletion chatcomponent$delayedmessagedeletion = this.deleteMessageOrDelay(p_241324_);
        if (chatcomponent$delayedmessagedeletion != null) {
            this.messageDeletionQueue.add(chatcomponent$delayedmessagedeletion);
        }
    }

    @Nullable
    private ChatComponent.DelayedMessageDeletion deleteMessageOrDelay(MessageSignature p_251812_) {
        int i = this.minecraft.gui.getGuiTicks();
        ListIterator<GuiMessage> listiterator = this.allMessages.listIterator();

        while (listiterator.hasNext()) {
            GuiMessage guimessage = listiterator.next();
            if (p_251812_.equals(guimessage.signature())) {
                int j = guimessage.addedTime() + 60;
                if (i >= j) {
                    listiterator.set(this.createDeletedMarker(guimessage));
                    this.refreshTrimmedMessages();
                    return null;
                }

                return new ChatComponent.DelayedMessageDeletion(p_251812_, j);
            }
        }

        return null;
    }

    private GuiMessage createDeletedMarker(GuiMessage p_249789_) {
        return new GuiMessage(p_249789_.addedTime(), DELETED_CHAT_MESSAGE, null, GuiMessageTag.system());
    }

    public void rescaleChat() {
        this.resetChatScroll();
        this.refreshTrimmedMessages();
    }

    private void refreshTrimmedMessages() {
        this.trimmedMessages.clear();

        for (GuiMessage guimessage : Lists.reverse(this.allMessages)) {
            this.addMessageToDisplayQueue(guimessage);
        }
    }

    public ArrayListDeque<String> getRecentChat() {
        return this.recentChat;
    }

    public void addRecentChat(String p_93784_) {
        if (!p_93784_.equals(this.recentChat.peekLast())) {
            if (this.recentChat.size() >= 100) {
                this.recentChat.removeFirst();
            }

            this.recentChat.addLast(p_93784_);
        }

        if (p_93784_.startsWith("/")) {
            this.minecraft.commandHistory().addCommand(p_93784_);
        }
    }

    public void resetChatScroll() {
        this.chatScrollbarPos = 0;
        this.newMessageSinceScroll = false;
    }

    public void scrollChat(int p_205361_) {
        this.chatScrollbarPos += p_205361_;
        int i = this.trimmedMessages.size();
        if (this.chatScrollbarPos > i - this.getLinesPerPage()) {
            this.chatScrollbarPos = i - this.getLinesPerPage();
        }

        if (this.chatScrollbarPos <= 0) {
            this.chatScrollbarPos = 0;
            this.newMessageSinceScroll = false;
        }
    }

    public boolean handleChatQueueClicked(double p_93773_, double p_93774_) {
        if (this.isChatFocused() && !this.minecraft.options.hideGui && !this.isChatHidden()) {
            ChatListener chatlistener = this.minecraft.getChatListener();
            if (chatlistener.queueSize() == 0L) {
                return false;
            } else {
                double d0 = p_93773_ - 2.0;
                double d1 = (double)this.minecraft.getWindow().getGuiScaledHeight() - p_93774_ - 40.0;
                if (d0 <= (double)Mth.floor((double)this.getWidth() / this.getScale()) && d1 < 0.0 && d1 > (double)Mth.floor(-9.0 * this.getScale())) {
                    chatlistener.acceptNextDelayedMessage();
                    return true;
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    @Nullable
    public Style getClickedComponentStyleAt(double p_93801_, double p_93802_) {
        double d0 = this.screenToChatX(p_93801_);
        double d1 = this.screenToChatY(p_93802_);
        int i = this.getMessageLineIndexAt(d0, d1);
        if (i >= 0 && i < this.trimmedMessages.size()) {
            GuiMessage.Line guimessage$line = this.trimmedMessages.get(i);
            return this.minecraft.font.getSplitter().componentStyleAtWidth(guimessage$line.content(), Mth.floor(d0));
        } else {
            return null;
        }
    }

    @Nullable
    public GuiMessageTag getMessageTagAt(double p_240576_, double p_240554_) {
        double d0 = this.screenToChatX(p_240576_);
        double d1 = this.screenToChatY(p_240554_);
        int i = this.getMessageEndIndexAt(d0, d1);
        if (i >= 0 && i < this.trimmedMessages.size()) {
            GuiMessage.Line guimessage$line = this.trimmedMessages.get(i);
            GuiMessageTag guimessagetag = guimessage$line.tag();
            if (guimessagetag != null && this.hasSelectedMessageTag(d0, guimessage$line, guimessagetag)) {
                return guimessagetag;
            }
        }

        return null;
    }

    private boolean hasSelectedMessageTag(double p_240619_, GuiMessage.Line p_240547_, GuiMessageTag p_240637_) {
        if (p_240619_ < 0.0) {
            return true;
        } else {
            GuiMessageTag.Icon guimessagetag$icon = p_240637_.icon();
            if (guimessagetag$icon == null) {
                return false;
            } else {
                int i = this.getTagIconLeft(p_240547_);
                int j = i + guimessagetag$icon.width;
                return p_240619_ >= (double)i && p_240619_ <= (double)j;
            }
        }
    }

    private double screenToChatX(double p_240580_) {
        return p_240580_ / this.getScale() - 4.0;
    }

    private double screenToChatY(double p_240548_) {
        double d0 = (double)this.minecraft.getWindow().getGuiScaledHeight() - p_240548_ - 40.0;
        return d0 / (this.getScale() * (double)this.getLineHeight());
    }

    private int getMessageEndIndexAt(double p_249245_, double p_252282_) {
        int i = this.getMessageLineIndexAt(p_249245_, p_252282_);
        if (i == -1) {
            return -1;
        } else {
            while (i >= 0) {
                if (this.trimmedMessages.get(i).endOfEntry()) {
                    return i;
                }

                i--;
            }

            return i;
        }
    }

    private int getMessageLineIndexAt(double p_249099_, double p_250008_) {
        if (this.isChatFocused() && !this.isChatHidden()) {
            if (!(p_249099_ < -4.0) && !(p_249099_ > (double)Mth.floor((double)this.getWidth() / this.getScale()))) {
                int i = Math.min(this.getLinesPerPage(), this.trimmedMessages.size());
                if (p_250008_ >= 0.0 && p_250008_ < (double)i) {
                    int j = Mth.floor(p_250008_ + (double)this.chatScrollbarPos);
                    if (j >= 0 && j < this.trimmedMessages.size()) {
                        return j;
                    }
                }

                return -1;
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

    public boolean isChatFocused() {
        return this.minecraft.screen instanceof ChatScreen;
    }

    public int getWidth() {
        return getWidth(this.minecraft.options.chatWidth().get());
    }

    public int getHeight() {
        return getHeight(this.isChatFocused() ? this.minecraft.options.chatHeightFocused().get() : this.minecraft.options.chatHeightUnfocused().get());
    }

    public double getScale() {
        return this.minecraft.options.chatScale().get();
    }

    public static int getWidth(double p_93799_) {
        int i = 320;
        int j = 40;
        return Mth.floor(p_93799_ * 280.0 + 40.0);
    }

    public static int getHeight(double p_93812_) {
        int i = 180;
        int j = 20;
        return Mth.floor(p_93812_ * 160.0 + 20.0);
    }

    public static double defaultUnfocusedPct() {
        int i = 180;
        int j = 20;
        return 70.0 / (double)(getHeight(1.0) - 20);
    }

    public int getLinesPerPage() {
        return this.getHeight() / this.getLineHeight();
    }

    private int getLineHeight() {
        return (int)(9.0 * (this.minecraft.options.chatLineSpacing().get() + 1.0));
    }

    public ChatComponent.State storeState() {
        return new ChatComponent.State(List.copyOf(this.allMessages), List.copyOf(this.recentChat), List.copyOf(this.messageDeletionQueue));
    }

    public void restoreState(ChatComponent.State p_330708_) {
        this.recentChat.clear();
        this.recentChat.addAll(p_330708_.history);
        this.messageDeletionQueue.clear();
        this.messageDeletionQueue.addAll(p_330708_.delayedMessageDeletions);
        this.allMessages.clear();
        this.allMessages.addAll(p_330708_.messages);
        this.refreshTrimmedMessages();
    }

    @OnlyIn(Dist.CLIENT)
    static record DelayedMessageDeletion(MessageSignature signature, int deletableAfter) {
    }

    @OnlyIn(Dist.CLIENT)
    public static class State {
        final List<GuiMessage> messages;
        final List<String> history;
        final List<ChatComponent.DelayedMessageDeletion> delayedMessageDeletions;

        public State(List<GuiMessage> p_334723_, List<String> p_331396_, List<ChatComponent.DelayedMessageDeletion> p_332733_) {
            this.messages = p_334723_;
            this.history = p_331396_;
            this.delayedMessageDeletions = p_332733_;
        }
    }
}