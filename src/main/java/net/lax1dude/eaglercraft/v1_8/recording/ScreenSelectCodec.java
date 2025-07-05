/*
 * Copyright (c) 2024 lax1dude. All Rights Reserved.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 */

package net.lax1dude.eaglercraft.v1_8.recording;

import java.io.IOException;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.locale.Language;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.gui.components.events.GuiEventListener;

public class ScreenSelectCodec extends Screen implements GuiEventListener {
	private static final Component TITLE = Component.translatable("options.recordingCodec.title");

	protected final ScreenRecordingSettings parent;
	protected List<EnumScreenRecordingCodec> codecs;
	protected int selectedCodec;
	protected net.lax1dude.eaglercraft.v1_8.recording.ContainerObjectSelectionListSelectCodec slots;
	protected Button showAllButton;
	protected boolean showAll;
	protected EnumScreenRecordingCodec codec;

	public ScreenSelectCodec(ScreenRecordingSettings parent, EnumScreenRecordingCodec codec) {
		super(TITLE);
		this.parent = parent;
		this.codec = codec;
	}

	@Override
	protected void init() {
		showAll = codec.advanced;
		codecs = showAll ? ScreenRecordingController.advancedCodecsOrdered : ScreenRecordingController.simpleCodecsOrdered;
		selectedCodec = codecs.indexOf(codec);
		
		this.clearWidgets();
		
		// Add show all button
		this.showAllButton = this.addRenderableWidget(Button.builder(
			Component.translatable("options.recordingCodec.showAdvancedCodecs", I18n.get(showAll ? "gui.yes" : "gui.no")),
			button -> actionPerformed(0, button))
			.bounds(this.width / 2 - 154, this.height - 38, 150, 20)
			.build());
		
		// Add done button
		this.addRenderableWidget(Button.builder(
			Component.translatable("gui.done"),
			button -> actionPerformed(1, button))
			.bounds(this.width / 2 + 4, this.height - 38, 150, 20)
			.build());
		
		// Add codec list
		slots = new ContainerObjectSelectionListSelectCodec(this, 32, this.height - 45);
		this.addRenderableWidget(slots);
		this.setInitialFocus(slots);
	}

	protected void actionPerformed(int id, Button parButton) {
		if(id == 0) {
			changeStateShowAll(!showAll);
			showAllButton.setMessage(Component.translatable("options.recordingCodec.showAdvancedCodecs", 
				I18n.get(showAll ? "gui.yes" : "gui.no")));
			init();
		} else if(id == 1) {
			if(selectedCodec >= 0 && selectedCodec < codecs.size()) {
				parent.handleCodecCallback(codecs.get(selectedCodec));
			}
			minecraft.setScreen(parent);
		}
	}

	protected void changeStateShowAll(boolean newShowAll) {
		if(newShowAll == showAll) return;
		EnumScreenRecordingCodec oldCodec = codecs.get(selectedCodec >= 0 && selectedCodec < codecs.size() ? selectedCodec : 0);
		codecs = newShowAll ? ScreenRecordingController.advancedCodecsOrdered : ScreenRecordingController.simpleCodecsOrdered;
		showAll = newShowAll;
		selectedCodec = codecs.indexOf(oldCodec);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		renderBackground(guiGraphics);
		slots.render(guiGraphics, mouseX, mouseY, partialTicks);
		guiGraphics.drawCenteredString(this.font, 
			TITLE, 
			this.width / 2, 8, 0xFFFFFF);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		slots.mouseClicked(mouseX, mouseY, button);
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		slots.mouseReleased(mouseX, mouseY, button);
		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		slots.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		return slots.mouseScrolled(mouseX, mouseY, delta) || super.mouseScrolled(mouseX, mouseY, delta);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == 256) { // ESC key
			minecraft.setScreen(parent);
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers) || slots.keyPressed(keyCode, scanCode, modifiers);
	}

	static Minecraft getMC(ScreenSelectCodec screen) {
		return screen.minecraft;
	}

}