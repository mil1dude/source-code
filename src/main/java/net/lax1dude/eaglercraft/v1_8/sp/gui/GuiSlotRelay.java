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

import net.lax1dude.eaglercraft.v1_8.sp.relay.RelayManager;
import net.lax1dude.eaglercraft.v1_8.sp.relay.RelayQuery;
import net.lax1dude.eaglercraft.v1_8.sp.relay.RelayServer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

class ContainerObjectSelectionListRelay extends ObjectSelectionList<ContainerObjectSelectionListRelay.RelayEntry> {

    private static final Component RELAY_LOADING = Component.translatable("relay.loading");
    private static final Component RELAY_CONNECTING = Component.translatable("relay.connecting");
    private static final Component RELAY_CONNECTED = Component.translatable("relay.connected");
    private static final Component RELAY_FAILED = Component.translatable("relay.failed");
    private static final Component RELAY_UNKNOWN = Component.translatable("relay.unknown");

    private static final ResourceLocation eaglerGuiTex = new ResourceLocation("eagler:gui/eagler_gui.png");

    final ScreenRelay screen;
    final RelayManager relayManager;

    public ContainerObjectSelectionListRelay(ScreenRelay screen) {
        super(screen.minecraft, screen.width, screen.height - 64, 32, 26);
        this.screen = screen;
        this.relayManager = RelayManager.relayManager;
        this.setRenderBackground(false);
        this.setRenderTopAndBottom(false);
        this.refreshEntries();
    }

    public void refreshEntries() {
        this.clearEntries();
        for (int i = 0; i < relayManager.count(); i++) {
            this.addEntry(new RelayEntry(relayManager.get(i), i));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button == 0) {
            int i = this.getRowWidth() / 2 - 140;
            int j = this.width / 2 - 140;
            int k = this.getRowTop(0);
            int l = mouseY - k;
            int m = this.getRowWidth();
            int n = this.itemHeight;
            if (mouseX >= j && mouseX <= j + m && l >= 0 && l < n * this.getItemCount()) {
                int o = this.getRowLeft();
                int p = (int) Math.floor(l / n);
                if (p >= 0 && p < this.getItemCount()) {
                    this.selectItem(p);
                    return true;
                }
            }
        }
        return false;
    }

    private void selectItem(int index) {
        screen.selected = index;
        screen.updateButtons();
    }

    @Override
    public int getRowWidth() {
        return 280;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.width / 2 + 140;
    }

    class RelayEntry extends ObjectSelectionList.Entry<RelayEntry> {
        private final RelayServer srv;
        private final int index;

        public RelayEntry(RelayServer srv, int index) {
            this.srv = srv;
            this.index = index;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTicks) {
            String comment = srv.comment;
            int xx = x + 2;
            int yy = y - 1;
            int w = getRowWidth() - 4;

            if (index == relayManager.getPrimary()) {
                guiGraphics.fill(xx, yy, xx + w, yy + height, 0x20FFFFFF);
            }

            if (screen.selected == index) {
                guiGraphics.fill(xx, yy, xx + w, yy + height, 0x40FFFFFF);
                if (screen.selected == index) {
                    guiGraphics.fill(xx, yy, xx + 1, yy + height, 0xFFFFFFFF);
                    guiGraphics.fill(xx + w - 1, yy, xx + w, yy + height, 0xFFFFFFFF);
                    guiGraphics.fill(xx, yy, xx + w, yy + 1, 0xFFFFFFFF);
                    guiGraphics.fill(xx, yy + height - 1, xx + w, yy + height, 0xFFFFFFFF);
                }
            }

            Component statusText = getStatusText(srv);

            if (statusText != null) {
                guiGraphics.drawString(screen.font, statusText, xx + 4, yy + height - 10, 0xFFFFFF, false);
            }

            if (srv.getStatus() == 2) {
                guiGraphics.blit(eaglerGuiTex, xx + 4, yy + 4, 48, 0, 16, 16, 256, 256);
            }

            guiGraphics.drawString(screen.font, comment, xx + 22, yy + 2, 0xFFFFFF, false);
            guiGraphics.drawString(screen.font, srv.address, xx + 22, yy + 12, 0xFF999999, false);

            if (statusText != null) {
                int rx = xx + 202;
                if (mouseX >= rx && mouseX < rx + 13 && mouseY >= yy - 1 && mouseY < yy + height) {
                    screen.setToolTip(statusText.getString());
                }
            }
        }

        private Component getStatusText(RelayServer srv) {
            switch (srv.getStatus()) {
                case 0:
                    return RELAY_LOADING;
                case 1:
                    return RELAY_CONNECTING;
                case 2:
                    if (srv.ping >= 0) {
                        return Component.translatable("relay.connected.ms", srv.ping);
                    }
                    return RELAY_CONNECTED;
                case 3:
                    return RELAY_FAILED;
                default:
                    return RELAY_UNKNOWN;
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                selectItem(index);
                return true;
            }
            return false;
        }

        @Override
        public Component getNarration() {
            return Component.literal(srv.comment + " " + srv.address);
        }
    }
}