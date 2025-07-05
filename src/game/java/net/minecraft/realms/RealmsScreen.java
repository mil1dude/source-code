package net.minecraft.realms;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class RealmsScreen extends Screen {
    protected static final int TITLE_HEIGHT = 17;
    protected static final int EXPIRATION_NOTIFICATION_DAYS = 7;
    protected static final long SIZE_LIMIT = 5368709120L;
    protected static final int COLOR_DARK_GRAY = 5000268;
    protected static final int COLOR_MEDIUM_GRAY = 7105644;
    protected static final int COLOR_GREEN = 8388479;
    protected static final int COLOR_LINK = 3368635;
    protected static final int COLOR_LINK_HOVER = 7107012;
    protected static final int SKIN_FACE_SIZE = 32;
    protected static final int HARDCORE_HEART_SIZE = 8;
    private final List<RealmsLabel> labels = Lists.newArrayList();

    public RealmsScreen(Component p_175072_) {
        super(p_175072_);
    }

    protected static int row(int p_120775_) {
        return 40 + p_120775_ * 13;
    }

    protected RealmsLabel addLabel(RealmsLabel p_175074_) {
        this.labels.add(p_175074_);
        return this.addRenderableOnly(p_175074_);
    }

    public Component createLabelNarration() {
        return CommonComponents.joinLines(this.labels.stream().map(RealmsLabel::getText).collect(Collectors.toList()));
    }
}