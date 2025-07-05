package net.lax1dude.eaglercraft.v1_8.recording;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;

public class ContainerObjectSelectionListSelectCodec extends ContainerObjectSelectionList<ContainerObjectSelectionListSelectCodec.Entry> {
    private final ScreenSelectCodec parent;

    public ContainerObjectSelectionListSelectCodec(ScreenSelectCodec parent, int width, int height) {
        super(parent.minecraft, width, parent.height - 45, 32, 20);
        this.parent = parent;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    public static class Entry extends ContainerObjectSelectionList.Entry<Entry> {
        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
            // Render your entry here
        }
    }
}
