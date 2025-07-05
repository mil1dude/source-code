package net.minecraft.client.gui.components;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OptionsList extends ContainerObjectSelectionList<OptionsList.Entry> {
    private static final int BIG_BUTTON_WIDTH = 310;
    private static final int DEFAULT_ITEM_HEIGHT = 25;
    private final OptionsSubScreen screen;

    public OptionsList(Minecraft p_94465_, int p_94466_, OptionsSubScreen p_342734_) {
        super(p_94465_, p_94466_, p_342734_.layout.getContentHeight(), p_342734_.layout.getHeaderHeight(), 25);
        this.centerListVertically = false;
        this.screen = p_342734_;
    }

    public void addBig(OptionInstance<?> p_232529_) {
        this.addEntry(OptionsList.OptionEntry.big(this.minecraft.options, p_232529_, this.screen));
    }

    public void addSmall(OptionInstance<?>... p_232534_) {
        for (int i = 0; i < p_232534_.length; i += 2) {
            OptionInstance<?> optioninstance = i < p_232534_.length - 1 ? p_232534_[i + 1] : null;
            this.addEntry(OptionsList.OptionEntry.small(this.minecraft.options, p_232534_[i], optioninstance, this.screen));
        }
    }

    public void addSmall(List<AbstractWidget> p_334237_) {
        for (int i = 0; i < p_334237_.size(); i += 2) {
            this.addSmall(p_334237_.get(i), i < p_334237_.size() - 1 ? p_334237_.get(i + 1) : null);
        }
    }

    public void addSmall(AbstractWidget p_330860_, @Nullable AbstractWidget p_333864_) {
        this.addEntry(OptionsList.Entry.small(p_330860_, p_333864_, this.screen));
    }

    @Override
    public int getRowWidth() {
        return 310;
    }

    @Nullable
    public AbstractWidget findOption(OptionInstance<?> p_232536_) {
        for (OptionsList.Entry optionslist$entry : this.children()) {
            if (optionslist$entry instanceof OptionsList.OptionEntry optionslist$optionentry) {
                AbstractWidget abstractwidget = optionslist$optionentry.options.get(p_232536_);
                if (abstractwidget != null) {
                    return abstractwidget;
                }
            }
        }

        return null;
    }

    public void applyUnsavedChanges() {
        for (OptionsList.Entry optionslist$entry : this.children()) {
            if (optionslist$entry instanceof OptionsList.OptionEntry) {
                OptionsList.OptionEntry optionslist$optionentry = (OptionsList.OptionEntry)optionslist$entry;

                for (AbstractWidget abstractwidget : optionslist$optionentry.options.values()) {
                    if (abstractwidget instanceof OptionInstance.OptionInstanceSliderButton<?> optioninstancesliderbutton) {
                        optioninstancesliderbutton.applyUnsavedValue();
                    }
                }
            }
        }
    }

    public Optional<GuiEventListener> getMouseOver(double p_94481_, double p_94482_) {
        for (OptionsList.Entry optionslist$entry : this.children()) {
            for (GuiEventListener guieventlistener : optionslist$entry.children()) {
                if (guieventlistener.isMouseOver(p_94481_, p_94482_)) {
                    return Optional.of(guieventlistener);
                }
            }
        }

        return Optional.empty();
    }

    @OnlyIn(Dist.CLIENT)
    protected static class Entry extends ContainerObjectSelectionList.Entry<OptionsList.Entry> {
        private final List<AbstractWidget> children;
        private final Screen screen;
        private static final int X_OFFSET = 160;

        Entry(List<AbstractWidget> p_328739_, Screen p_332963_) {
            this.children = ImmutableList.copyOf(p_328739_);
            this.screen = p_332963_;
        }

        public static OptionsList.Entry big(List<AbstractWidget> p_331607_, Screen p_332678_) {
            return new OptionsList.Entry(p_331607_, p_332678_);
        }

        public static OptionsList.Entry small(AbstractWidget p_332778_, @Nullable AbstractWidget p_330638_, Screen p_328012_) {
            return p_330638_ == null
                ? new OptionsList.Entry(ImmutableList.of(p_332778_), p_328012_)
                : new OptionsList.Entry(ImmutableList.of(p_332778_, p_330638_), p_328012_);
        }

        @Override
        public void render(
            GuiGraphics p_281311_,
            int p_94497_,
            int p_94498_,
            int p_94499_,
            int p_94500_,
            int p_94501_,
            int p_94502_,
            int p_94503_,
            boolean p_94504_,
            float p_94505_
        ) {
            int i = 0;
            int j = this.screen.width / 2 - 155;

            for (AbstractWidget abstractwidget : this.children) {
                abstractwidget.setPosition(j + i, p_94498_);
                abstractwidget.render(p_281311_, p_94502_, p_94503_, p_94505_);
                i += 160;
            }
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return this.children;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return this.children;
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected static class OptionEntry extends OptionsList.Entry {
        final Map<OptionInstance<?>, AbstractWidget> options;

        private OptionEntry(Map<OptionInstance<?>, AbstractWidget> p_331348_, OptionsSubScreen p_345262_) {
            super(ImmutableList.copyOf(p_331348_.values()), p_345262_);
            this.options = p_331348_;
        }

        public static OptionsList.OptionEntry big(Options p_335438_, OptionInstance<?> p_329713_, OptionsSubScreen p_342690_) {
            return new OptionsList.OptionEntry(ImmutableMap.of(p_329713_, p_329713_.createButton(p_335438_, 0, 0, 310)), p_342690_);
        }

        public static OptionsList.OptionEntry small(
            Options p_330617_, OptionInstance<?> p_330233_, @Nullable OptionInstance<?> p_331704_, OptionsSubScreen p_342280_
        ) {
            AbstractWidget abstractwidget = p_330233_.createButton(p_330617_);
            return p_331704_ == null
                ? new OptionsList.OptionEntry(ImmutableMap.of(p_330233_, abstractwidget), p_342280_)
                : new OptionsList.OptionEntry(ImmutableMap.of(p_330233_, abstractwidget, p_331704_, p_331704_.createButton(p_330617_)), p_342280_);
        }
    }
}