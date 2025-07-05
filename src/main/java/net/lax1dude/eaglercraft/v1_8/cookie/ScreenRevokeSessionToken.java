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

package net.lax1dude.eaglercraft.v1_8.cookie;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ScreenRevokeSessionToken extends Screen {
    private static final Component TITLE = Component.translatable("revokeSessionToken.title");
    private static final Component NOTE_0 = Component.translatable("revokeSessionToken.note.0");
    private static final Component NOTE_1 = Component.translatable("revokeSessionToken.note.1");
    
    protected final Screen parentScreen;
    private ServerList list;
    private Button inspectButton;
    private Button revokeButton;
    private int selected = -1;

    public ScreenRevokeSessionToken(Screen parent) {
        super(Component.translatable("revokeSessionToken.title"));
        this.parentScreen = parent;
    }

    @Override
    public void init() {
        this.list = new ServerList(this.minecraft);
        this.addRenderableWidget(list);
        
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
            Objects.requireNonNull(this.minecraft).setScreen(parentScreen);
        }).bounds(this.width / 2 + 54, this.height - 38, 100, 20).build());
        
        this.revokeButton = this.addRenderableWidget(Button.builder(Component.translatable("revokeSessionToken.revoke"), button -> {
            String selected = list.getSelectedItem();
            if (selected != null) {
                ServerCookieDataStore.ServerCookie cookie = ServerCookieDataStore.loadCookie(selected);
                if (cookie != null) {
                    Objects.requireNonNull(this.minecraft).setScreen(new ScreenSendRevokeRequest(this, cookie));
                } else {
                    this.init(minecraft, width, height);
                }
            }
        }).bounds(this.width / 2 - 50, this.height - 38, 100, 20).build());
        
        this.inspectButton = this.addRenderableWidget(Button.builder(Component.translatable("revokeSessionToken.inspect"), button -> {
            String selected = list.getSelectedItem();
            if (selected != null) {
                ServerCookieDataStore.ServerCookie cookie = ServerCookieDataStore.loadCookie(selected);
                if (cookie != null) {
                    Objects.requireNonNull(this.minecraft).setScreen(new ScreenInspectSessionToken(this, cookie));
                } else {
                    this.init(minecraft, width, height);
                }
            }
        }).bounds(this.width / 2 - 154, this.height - 38, 100, 20).build());
        
        updateButtons();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.list.mouseClicked(mouseX, mouseY, button)) {
            this.setFocused(this.list);
            this.updateButtons();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void updateButtons() {
        boolean hasSelection = list.getSelectedItem() != null;
        this.inspectButton.active = hasSelection;
        this.revokeButton.active = hasSelection;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
        this.list.render(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.drawCenteredString(this.font, TITLE, this.width / 2, 16, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font, NOTE_0, this.width / 2, this.height - 66, 0x808080);
        guiGraphics.drawCenteredString(this.font, NOTE_1, this.width / 2, this.height - 56, 0x808080);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    class ServerList extends ObjectSelectionList<ServerList.Entry> {
        private final List<String> cookieNames;
        private int selected = -1;

        public ServerList(Minecraft minecraft) {
            super(minecraft, ScreenRevokeSessionToken.this.width, ScreenRevokeSessionToken.this.height, 32, 
                ScreenRevokeSessionToken.this.height - 75 + 4, 18);
            ServerCookieDataStore.flush();
            this.cookieNames = Lists.newArrayList(ServerCookieDataStore.getRevokableServers());
            Collections.sort(this.cookieNames);
            
            for (String name : cookieNames) {
                this.addEntry(new Entry(name));
            }
        }

        @Override
        protected int getScrollbarPosition() {
            return this.width / 2 + 140;
        }

        @Override
        public int getRowWidth() {
            return 260;
        }

        @Override
        protected boolean isSelectedItem(int index) {
            return index == selected;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            Entry entry = this.getEntryAtPosition(mouseX, mouseY);
            this.selected = entry != null ? this.children().indexOf(entry) : -1;
            if (this.selected >= 0) {
                ScreenRevokeSessionToken.this.updateButtons();
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        public String getSelectedItem() {
            return this.selected >= 0 && this.selected < this.cookieNames.size() ? 
                this.cookieNames.get(this.selected) : null;
        }

        class Entry extends ObjectSelectionList.Entry<Entry> {
            private final String name;

            public Entry(String name) {
                this.name = name;
            }

            @Override
            public void render(GuiGraphics guiGraphics, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean hovered, float delta) {
                int textX = (ScreenRevokeSessionToken.this.width - ScreenRevokeSessionToken.this.font.width(this.name)) / 2;
                guiGraphics.drawString(ScreenRevokeSessionToken.this.font, this.name, textX, y + 1, 0xFFFFFF, false);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                ServerList.this.setSelected(this);
                return false;
            }

            @Override
            public Component getNarration() {
                return Component.literal(this.name);
            }
        }
    }
}