package net.minecraft.client.gui.screens.options;

import java.util.Arrays;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SoundOptionsScreen extends OptionsSubScreen {
    private static final Component TITLE = Component.translatable("options.sounds.title");

    private static OptionInstance<?>[] buttonOptions(Options p_343195_) {
        return new OptionInstance[]{p_343195_.showSubtitles(), p_343195_.directionalAudio()};
    }

    public SoundOptionsScreen(Screen p_343471_, Options p_344842_) {
        super(p_343471_, p_344842_, TITLE);
    }

    @Override
    protected void addOptions() {
        this.list.addBig(this.options.getSoundSourceOptionInstance(SoundSource.MASTER));
        this.list.addSmall(this.getAllSoundOptionsExceptMaster());
        this.list.addBig(this.options.soundDevice());
        this.list.addSmall(buttonOptions(this.options));
    }

    private OptionInstance<?>[] getAllSoundOptionsExceptMaster() {
        return Arrays.stream(SoundSource.values())
            .filter(p_343395_ -> p_343395_ != SoundSource.MASTER)
            .map(p_344760_ -> this.options.getSoundSourceOptionInstance(p_344760_))
            .toArray(OptionInstance[]::new);
    }
}