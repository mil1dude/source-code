package net.minecraft.client.gui.screens.worldselection;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class SelectWorldScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final WorldOptions TEST_OPTIONS = new WorldOptions((long)"test1".hashCode(), true, false);
    protected final Screen lastScreen;
    private Button deleteButton;
    private Button selectButton;
    private Button renameButton;
    private Button copyButton;
    protected EditBox searchBox;
    private WorldSelectionList list;

    public SelectWorldScreen(Screen p_101338_) {
        super(Component.translatable("selectWorld.title"));
        this.lastScreen = p_101338_;
    }

    @Override
    protected void init() {
        this.searchBox = new EditBox(this.font, this.width / 2 - 100, 22, 200, 20, this.searchBox, Component.translatable("selectWorld.search"));
        this.searchBox.setResponder(p_232980_ -> this.list.updateFilter(p_232980_));
        this.addWidget(this.searchBox);
        this.list = this.addRenderableWidget(
            new WorldSelectionList(this, this.minecraft, this.width, this.height - 112, 48, 36, this.searchBox.getValue(), this.list)
        );
        this.selectButton = this.addRenderableWidget(
            Button.builder(LevelSummary.PLAY_WORLD, p_232984_ -> this.list.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::joinWorld))
                .bounds(this.width / 2 - 154, this.height - 52, 150, 20)
                .build()
        );
        this.addRenderableWidget(
            Button.builder(Component.translatable("selectWorld.create"), p_280918_ -> CreateWorldScreen.openFresh(this.minecraft, this))
                .bounds(this.width / 2 + 4, this.height - 52, 150, 20)
                .build()
        );
        this.renameButton = this.addRenderableWidget(
            Button.builder(
                    Component.translatable("selectWorld.edit"), p_101378_ -> this.list.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::editWorld)
                )
                .bounds(this.width / 2 - 154, this.height - 28, 72, 20)
                .build()
        );
        this.deleteButton = this.addRenderableWidget(
            Button.builder(
                    Component.translatable("selectWorld.delete"), p_101376_ -> this.list.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::deleteWorld)
                )
                .bounds(this.width / 2 - 76, this.height - 28, 72, 20)
                .build()
        );
        this.copyButton = this.addRenderableWidget(
            Button.builder(
                    Component.translatable("selectWorld.recreate"),
                    p_101373_ -> this.list.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::recreateWorld)
                )
                .bounds(this.width / 2 + 4, this.height - 28, 72, 20)
                .build()
        );
        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_BACK, p_280917_ -> this.minecraft.setScreen(this.lastScreen))
                .bounds(this.width / 2 + 82, this.height - 28, 72, 20)
                .build()
        );
        this.updateButtonStatus(null);
    }

    @Override
    protected void setInitialFocus() {
        this.setInitialFocus(this.searchBox);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    @Override
    public void render(GuiGraphics p_282382_, int p_281534_, int p_281859_, float p_283289_) {
        super.render(p_282382_, p_281534_, p_281859_, p_283289_);
        this.searchBox.render(p_282382_, p_281534_, p_281859_, p_283289_);
        p_282382_.drawCenteredString(this.font, this.title, this.width / 2, 8, 16777215);
    }

    public void updateButtonStatus(@Nullable LevelSummary p_309997_) {
        if (p_309997_ == null) {
            this.selectButton.setMessage(LevelSummary.PLAY_WORLD);
            this.selectButton.active = false;
            this.renameButton.active = false;
            this.copyButton.active = false;
            this.deleteButton.active = false;
        } else {
            this.selectButton.setMessage(p_309997_.primaryActionMessage());
            this.selectButton.active = p_309997_.primaryActionActive();
            this.renameButton.active = p_309997_.canEdit();
            this.copyButton.active = p_309997_.canRecreate();
            this.deleteButton.active = p_309997_.canDelete();
        }
    }

    @Override
    public void removed() {
        if (this.list != null) {
            this.list.children().forEach(WorldSelectionList.Entry::close);
        }
    }
}