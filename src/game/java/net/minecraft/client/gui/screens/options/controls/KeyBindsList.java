package net.minecraft.client.gui.screens.options.controls;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.ArrayUtils;

@OnlyIn(Dist.CLIENT)
public class KeyBindsList extends ContainerObjectSelectionList<KeyBindsList.Entry> {
    private static final int ITEM_HEIGHT = 20;
    final KeyBindsScreen keyBindsScreen;
    private int maxNameWidth;

    public KeyBindsList(KeyBindsScreen p_344272_, Minecraft p_345192_) {
        super(p_345192_, p_344272_.width, p_344272_.layout.getContentHeight(), p_344272_.layout.getHeaderHeight(), 20);
        this.keyBindsScreen = p_344272_;
        KeyMapping[] akeymapping = ArrayUtils.clone((KeyMapping[])p_345192_.options.keyMappings);
        Arrays.sort((Object[])akeymapping);
        String s = null;

        for (KeyMapping keymapping : akeymapping) {
            String s1 = keymapping.getCategory();
            if (!s1.equals(s)) {
                s = s1;
                this.addEntry(new KeyBindsList.CategoryEntry(Component.translatable(s1)));
            }

            Component component = Component.translatable(keymapping.getName());
            int i = p_345192_.font.width(component);
            if (i > this.maxNameWidth) {
                this.maxNameWidth = i;
            }

            this.addEntry(new KeyBindsList.KeyEntry(keymapping, component));
        }
    }

    public void resetMappingAndUpdateButtons() {
        KeyMapping.resetMapping();
        this.refreshEntries();
    }

    public void refreshEntries() {
        this.children().forEach(KeyBindsList.Entry::refreshEntry);
    }

    @Override
    public int getRowWidth() {
        return 340;
    }

    @OnlyIn(Dist.CLIENT)
    public class CategoryEntry extends KeyBindsList.Entry {
        final Component name;
        private final int width;

        public CategoryEntry(final Component p_344163_) {
            this.name = p_344163_;
            this.width = KeyBindsList.this.minecraft.font.width(this.name);
        }

        @Override
        public void render(
            GuiGraphics p_344696_,
            int p_342756_,
            int p_342574_,
            int p_345326_,
            int p_342513_,
            int p_342110_,
            int p_343674_,
            int p_345502_,
            boolean p_343416_,
            float p_344286_
        ) {
            p_344696_.drawString(
                KeyBindsList.this.minecraft.font, this.name, KeyBindsList.this.width / 2 - this.width / 2, p_342574_ + p_342110_ - 9 - 1, -1
            );
        }

        @Nullable
        @Override
        public ComponentPath nextFocusPath(FocusNavigationEvent p_344683_) {
            return null;
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return Collections.emptyList();
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(new NarratableEntry() {
                @Override
                public NarratableEntry.NarrationPriority narrationPriority() {
                    return NarratableEntry.NarrationPriority.HOVERED;
                }

                @Override
                public void updateNarration(NarrationElementOutput p_343060_) {
                    p_343060_.add(NarratedElementType.TITLE, CategoryEntry.this.name);
                }
            });
        }

        @Override
        protected void refreshEntry() {
        }
    }

    @OnlyIn(Dist.CLIENT)
    public abstract static class Entry extends ContainerObjectSelectionList.Entry<KeyBindsList.Entry> {
        abstract void refreshEntry();
    }

    @OnlyIn(Dist.CLIENT)
    public class KeyEntry extends KeyBindsList.Entry {
        private static final Component RESET_BUTTON_TITLE = Component.translatable("controls.reset");
        private static final int PADDING = 10;
        private final KeyMapping key;
        private final Component name;
        private final Button changeButton;
        private final Button resetButton;
        private boolean hasCollision = false;

        KeyEntry(final KeyMapping p_343088_, final Component p_343976_) {
            this.key = p_343088_;
            this.name = p_343976_;
            this.changeButton = Button.builder(p_343976_, p_342196_ -> {
                    KeyBindsList.this.keyBindsScreen.selectedKey = p_343088_;
                    KeyBindsList.this.resetMappingAndUpdateButtons();
                })
                .bounds(0, 0, 75, 20)
                .createNarration(
                    p_342179_ -> p_343088_.isUnbound()
                            ? Component.translatable("narrator.controls.unbound", p_343976_)
                            : Component.translatable("narrator.controls.bound", p_343976_, p_342179_.get())
                )
                .build();
            this.resetButton = Button.builder(RESET_BUTTON_TITLE, p_357685_ -> {
                p_343088_.setKey(p_343088_.getDefaultKey());
                KeyBindsList.this.resetMappingAndUpdateButtons();
            }).bounds(0, 0, 50, 20).createNarration(p_344192_ -> Component.translatable("narrator.controls.reset", p_343976_)).build();
            this.refreshEntry();
        }

        @Override
        public void render(
            GuiGraphics p_342188_,
            int p_342251_,
            int p_342953_,
            int p_343435_,
            int p_344535_,
            int p_343807_,
            int p_342827_,
            int p_342128_,
            boolean p_345345_,
            float p_344073_
        ) {
            int i = KeyBindsList.this.scrollBarX() - this.resetButton.getWidth() - 10;
            int j = p_342953_ - 2;
            this.resetButton.setPosition(i, j);
            this.resetButton.render(p_342188_, p_342827_, p_342128_, p_344073_);
            int k = i - 5 - this.changeButton.getWidth();
            this.changeButton.setPosition(k, j);
            this.changeButton.render(p_342188_, p_342827_, p_342128_, p_344073_);
            p_342188_.drawString(KeyBindsList.this.minecraft.font, this.name, p_343435_, p_342953_ + p_343807_ / 2 - 9 / 2, -1);
            if (this.hasCollision) {
                int l = 3;
                int i1 = this.changeButton.getX() - 6;
                p_342188_.fill(i1, p_342953_ - 1, i1 + 3, p_342953_ + p_343807_, -65536);
            }
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.changeButton, this.resetButton);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(this.changeButton, this.resetButton);
        }

        @Override
        protected void refreshEntry() {
            this.changeButton.setMessage(this.key.getTranslatedKeyMessage());
            this.resetButton.active = !this.key.isDefault();
            this.hasCollision = false;
            MutableComponent mutablecomponent = Component.empty();
            if (!this.key.isUnbound()) {
                for (KeyMapping keymapping : KeyBindsList.this.minecraft.options.keyMappings) {
                    if (keymapping != this.key && this.key.same(keymapping)) {
                        if (this.hasCollision) {
                            mutablecomponent.append(", ");
                        }

                        this.hasCollision = true;
                        mutablecomponent.append(Component.translatable(keymapping.getName()));
                    }
                }
            }

            if (this.hasCollision) {
                this.changeButton
                    .setMessage(
                        Component.literal("[ ")
                            .append(this.changeButton.getMessage().copy().withStyle(ChatFormatting.WHITE))
                            .append(" ]")
                            .withStyle(ChatFormatting.RED)
                    );
                this.changeButton.setTooltip(Tooltip.create(Component.translatable("controls.keybinds.duplicateKeybinds", mutablecomponent)));
            } else {
                this.changeButton.setTooltip(null);
            }

            if (KeyBindsList.this.keyBindsScreen.selectedKey == this.key) {
                this.changeButton
                    .setMessage(
                        Component.literal("> ")
                            .append(this.changeButton.getMessage().copy().withStyle(ChatFormatting.WHITE, ChatFormatting.UNDERLINE))
                            .append(" <")
                            .withStyle(ChatFormatting.YELLOW)
                    );
            }
        }
    }
}