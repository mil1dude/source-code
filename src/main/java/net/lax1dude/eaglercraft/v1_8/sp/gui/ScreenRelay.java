/*
 * Copyright (c) 2022-2024 lax1dude, ayunami2000. All Rights Reserved.
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

package net.lax1dude.eaglercraft.v1_8.sp.gui;

import net.lax1dude.eaglercraft.v1_8.EagRuntime;
import net.lax1dude.eaglercraft.v1_8.sp.relay.RelayManager;
import net.lax1dude.eaglercraft.v1_8.sp.relay.RelayServer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.ChatFormatting;

import java.io.IOException;

public class ScreenRelay extends Screen {

	private final Screen parentScreen;
	private ContainerObjectSelectionListRelay slots;
	private boolean hasPinged;
	private boolean addingNew = false;
	private boolean deleting = false;
	int selected;

	private Button deleteRelay;

	private String tooltipString = null;

	private long lastRefresh = 0l;

	public ScreenRelay(Screen parentScreen) {
		super(new TextComponent("Relay Settings"));
		this.parentScreen = parentScreen;
	}

	@Override
	protected void init() {
		super.init();
		selected = -1;
		clearWidgets();
		this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> actionPerformed(0))
			.bounds(this.width / 2 + 54, this.height - 28, 100, 20).build());
		this.addRenderableWidget(Button.builder(Component.translatable("networkSettings.add"), button -> actionPerformed(1))
			.bounds(this.width / 2 - 154, this.height - 52, 100, 20).build());
		this.deleteRelay = this.addRenderableWidget(Button.builder(Component.translatable("networkSettings.delete"), button -> actionPerformed(2))
			.bounds(this.width / 2 - 50, this.height - 52, 100, 20).build());
		this.addRenderableWidget(Button.builder(Component.translatable("networkSettings.default"), button -> actionPerformed(3))
			.bounds(this.width / 2 + 54, this.height - 52, 100, 20).build());
		this.addRenderableWidget(Button.builder(Component.translatable("networkSettings.refresh"), button -> actionPerformed(4))
			.bounds(this.width / 2 - 50, this.height - 28, 100, 20).build());
		this.addRenderableWidget(Button.builder(Component.translatable("networkSettings.loadDefaults"), button -> actionPerformed(5))
			.bounds(this.width / 2 - 154, this.height - 28, 100, 20).build());
		this.addRenderableWidget(Button.builder(Component.translatable("networkSettings.downloadRelay"), button -> actionPerformed(6))
			.bounds(this.width - 100, 0, 100, 20).build());
		updateButtons();
		this.slots = new ContainerObjectSelectionListRelay(this);
		if(!hasPinged) {
			hasPinged = true;
			slots.relayManager.ping();
		}
	}

	void updateButtons() {
		if(selected < 0) {
			deleteRelay.active = false;
			setPrimary.active = false;
		}else {
			deleteRelay.active = true;
			setPrimary.active = true;
		}
	}

	private void actionPerformed(int buttonId) {
		switch(buttonId) {
			case 0:
				RelayManager.relayManager.save();
				this.minecraft.setScreen(parentScreen);
				break;
			case 1:
				addingNew = true;
				this.minecraft.setScreen(new ScreenAddRelay(this));
				break;
			case 2:
				if(selected >= 0) {
					RelayServer srv = RelayManager.relayManager.get(selected);
					this.minecraft.setScreen(new ConfirmScreen(confirmed -> {
						if(confirmed) {
							RelayManager.relayManager.remove(selected);
							selected = -1;
						}
						this.minecraft.setScreen(this);
					}, Component.translatable("networkSettings.delete"), 
					Component.literal(Component.translatable("addRelay.removeText1").getString() + " '" + srv.comment + "' (" + srv.address + ")")));
				}
				break;
			case 3:
				if(selected >= 0) {
					slots.relayManager.setPrimary(selected);
					selected = 0;
				}
				break;
			case 4:
				long millis = EagRuntime.steadyTimeMillis();
				if(millis - lastRefresh > 700l) {
					lastRefresh = millis;
					slots.relayManager.ping();
				}
				lastRefresh += 60l;
				break;
			case 5:
				slots.relayManager.loadDefaults();
				millis = EagRuntime.steadyTimeMillis();
				if(millis - lastRefresh > 700l) {
					lastRefresh = millis;
					slots.relayManager.ping();
				}
				lastRefresh += 60l;
				break;
			case 6:
				EagRuntime.downloadFileWithName("EaglerSPRelay.zip", EagRuntime.getRequiredResourceBytes("relay_download.zip"));
				break;
		}
	}

	@Override
	public void tick() {
		super.tick();
		slots.relayManager.update();
	}

	private int mx = 0;
	private int my = 0;

	int getFrameMouseX() {
		return mx;
	}

	int getFrameMouseY() {
		return my;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		mx = mouseX;
		my = mouseY;
		slots.render(guiGraphics, mouseX, mouseY, partialTicks);

		super.render(guiGraphics, mouseX, mouseY, partialTicks);

		if(tooltipString != null) {
			int ww = font.width(tooltipString);
			guiGraphics.fill(mouseX + 1, mouseY - 14, mouseX + ww + 7, mouseY - 2, 0xC0000000);
			guiGraphics.drawString(font, tooltipString, mouseX + 4, mouseY - 12, 0xFF999999);
			tooltipString = null;
		}

		guiGraphics.drawCenteredString(font, Component.translatable("networkSettings.title"), this.width / 2, 16, 0xFFFFFF);

		String str = Component.translatable("networkSettings.relayTimeout").getString() + " " + minecraft.options.relayTimeout();
		int w = font.width(str);
		guiGraphics.drawString(font, str, 3, 3, 0xDDDDDD, false);

		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(w + 7, 4, 0.0f);
		guiGraphics.pose().scale(0.75f, 0.75f, 0.75f);
		String changeText = Component.translatable("networkSettings.relayTimeoutChange").getString();
		int w2 = font.width(changeText);
		boolean hovered = mouseX > w + 5 && mouseX < w + 7 + w2 * 3 / 4 && mouseY > 3 && mouseY < 11;
		if(hovered) {
			minecraft.screen.setTooltipForNextRenderPass(Component.literal(""));
		}
		guiGraphics.drawString(font, ChatFormatting.UNDERLINE + changeText, 0, 0, hovered ? 0xCCCCCC : 0x999999, false);
		guiGraphics.pose().popPose();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if(super.mouseClicked(mouseX, mouseY, button)) {
			return true;
		}
		if(button == 0) {
			String str = Component.translatable("networkSettings.relayTimeout").getString() + " " + minecraft.options.relayTimeout();
			int w = font.width(str);
			String changeText = Component.translatable("networkSettings.relayTimeoutChange").getString();
			int w2 = font.width(changeText);
			if(mouseX > w + 5 && mouseX < w + 7 + w2 * 3 / 4 && mouseY > 3 && mouseY < 11) {
				this.minecraft.setScreen(new ScreenChangeRelayTimeout(this));
				this.minecraft.getSoundManager().play(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f);
				return true;
			}
		}
		return false;
	}

	void setToolTip(String str) {
		tooltipString = str;
	}

	String addNewName;
	String addNewAddr;
	boolean addNewPrimary;

	public void confirmClicked(boolean confirmed, int id) {
		if(confirmed) {
			if(addingNew) {
				RelayManager.relayManager.addNew(addNewAddr, addNewName, addNewPrimary);
				addNewAddr = null;
				addNewName = null;
				addNewPrimary = false;
				selected = -1;
				updateButtons();
			}else if(deleting) {
				RelayManager.relayManager.remove(id);
				selected = -1;
				updateButtons();
			}
		}
		addingNew = false;
		deleting = false;
		this.minecraft.setScreen(this);
	}

	static Minecraft getMinecraft(ScreenRelay screen) {
		return screen.minecraft;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
		if(super.mouseScrolled(mouseX, mouseY, deltaX, deltaY)) {
			return true;
		}
		return this.slots.mouseScrolled(mouseX, mouseY, deltaY);
	}

	@Override
	public void handleMouseInput() {
		super.handleMouseInput();
		this.slots.mouseMoved(mx, my);
	}

	@Override
	public void handleTouchInput() throws IOException {
		super.handleTouchInput();
		this.slots.handleTouchInput();
	}

}