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

package net.lax1dude.eaglercraft.v1_8.notifications;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import net.lax1dude.eaglercraft.v1_8.EaglercraftUUID;
import net.lax1dude.eaglercraft.v1_8.socket.protocol.pkt.server.SPacketNotifBadgeShowV4EAG.EnumBadgePriority;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;

public class ScreenNotifications extends Screen {
    
    private static final Component TITLE = Component.translatable("notifications.title");

	private static final String[] priorityLangKeys = new String[] {
			"notifications.priority.low",
			"notifications.priority.normal",
			"notifications.priority.higher",
			"notifications.priority.highest"
	};

	private static final int[] priorityOrder = new int[] {
			0, 3, 2, 1
	};

	Screen parent;
	int selected;
	GuiSlotNotifications slots;
	Button clearAllButton;
	Button priorityButton;
	int showPriority = 0;
	EnumBadgePriority selectedMaxPriority = EnumBadgePriority.LOW;
	int lastUpdate = -1;

	public ScreenNotifications(Screen parent) {
		this.parent = parent;
	}

    @Override
    protected void init() {
        selected = -1;
        this.clearWidgets();
        
        // Done button
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
            this.onClose();
        }).bounds(this.width / 2 + 54, this.height - 32, 100, 20).build());
        
        // Clear All button
        this.clearAllButton = this.addRenderableWidget(Button.builder(
            Component.translatable("notifications.clearAll"), 
            button -> clearAllNotifications()
        ).bounds(this.width / 2 - 154, this.height - 32, 100, 20).build());
        
        // Priority button
        int i = priorityOrder[showPriority];
        this.priorityButton = this.addRenderableWidget(Button.builder(
            Component.translatable("notifications.priority", 
                Component.translatable(priorityLangKeys[i])
            ),
            button -> cyclePriority()
        ).bounds(this.width / 2 - 50, this.height - 32, 100, 20).build());
        
        selectedMaxPriority = EnumBadgePriority.getByID(i);
        slots = new GuiSlotNotifications(this);
        lastUpdate = -69420;
        updateList();
        updateButtons();
    }

    void updateButtons() {
        if (clearAllButton != null) {
            clearAllButton.active = !slots.currentDisplayNotifs.isEmpty();
        }
    }

    void updateList() {
        if (minecraft.player == null) return;
        ServerNotificationManager mgr = minecraft.player.connection.getNotifManager();
        int verHash = showPriority | (mgr.getNotifListUpdateCount() << 2);
        if (verHash != lastUpdate) {
            lastUpdate = verHash;
            EaglercraftUUID selectedUUID = null;
            List<GuiSlotNotifications.NotifBadgeSlot> lst = slots.currentDisplayNotifs;
            int oldSelectedId = selected;
            if (oldSelectedId >= 0 && oldSelectedId < lst.size()) {
                selectedUUID = lst.get(oldSelectedId).badge.badgeUUID;
            }
            lst.clear();
            mgr.getNotifLongHistory().stream()
                .filter((input) -> input.priority.priority >= priorityOrder[showPriority])
                .map(GuiSlotNotifications.NotifBadgeSlot::new)
                .forEach(lst::add);
            selected = -1;
            if (selectedUUID != null) {
                for (int i = 0, l = lst.size(); i < l; ++i) {
                    if (selectedUUID.equals(lst.get(i).badge.badgeUUID)) {
                        selected = i;
                        break;
                    }
                }
            }
            if (selected != -1) {
                if (oldSelectedId != selected) {
                    slots.scrollBy((selected - oldSelectedId) * slots.getItemHeight());
                }
            }
            updateButtons();
        }
    }

    @Override
    public void tick() {
        if (minecraft.player == null) {
            onClose();
            return;
        }
        updateList();
    }

	static Minecraft getMinecraft(ScreenNotifications screen) {
		return screen.minecraft;
	}

    private void clearAllNotifications() {
        if (minecraft.player != null) {
            ServerNotificationManager mgr = minecraft.player.connection.getNotifManager();
            mgr.removeAllNotifFromActiveList(mgr.getNotifLongHistory());
            updateButtons();
        }
    }
    
    private void cyclePriority() {
        showPriority = (showPriority + 1) & 3;
        int i = priorityOrder[showPriority];
        priorityButton.setMessage(Component.translatable("notifications.priority", 
            Component.translatable(priorityLangKeys[i])));
        selectedMaxPriority = EnumBadgePriority.getByID(i);
        updateList();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (minecraft.player == null) return;
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.drawCenteredString(this.font, TITLE, this.width / 2, 16, 0xFFFFFF);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (slots.mouseScrolled(mouseX, mouseY, delta)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.connection.getNotifManager().commitUnreadFlag();
        }
        if (parent != null) {
            minecraft.setScreen(parent);
        } else {
            super.onClose();
        }
    }
}