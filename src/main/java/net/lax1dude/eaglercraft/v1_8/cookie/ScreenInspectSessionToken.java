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

import java.text.SimpleDateFormat;
import java.util.Date;

import net.lax1dude.eaglercraft.v1_8.cookie.ServerCookieDataStore.ServerCookie;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
//import net.minecraft.network.chat.Component;

public class ScreenInspectSessionToken extends Screen {

    private final Screen parent;
    private final ServerCookie cookie;

    public ScreenInspectSessionToken(ScreenRevokeSessionToken parent, ServerCookie cookie) {
        super(Component.translatable("inspectSessionToken.title"));
        this.parent = parent;
        this.cookie = cookie;
    }

    @Override
    protected void init() {
        this.clearWidgets();
        this.addRenderableWidget(new net.minecraft.client.gui.components.Button(this.width / 2 - 100, this.height / 6 + 106, 200, 20, Component.translatable("gui.done"), (btn) -> {
            this.minecraft.setScreen(parent);
        }));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        
        String[][] toDraw = new String[][] {
            {
                "Server:",
                "Expires:",
                "Length:"
            },
            {
                cookie.server.length() > 32 ? cookie.server.substring(0, 30) + "..." : cookie.server,
                (new SimpleDateFormat("M/d/yyyy h:mm aa")).format(new Date(cookie.expires)),
                Integer.toString(cookie.cookie.length)
            }
        };
        
        int[] maxWidth = new int[2];
        for(int i = 0; i < 2; ++i) {
            String[] strs = toDraw[i];
            int w = 0;
            for(int j = 0; j < strs.length; ++j) {
                int k = this.fontRendererObj.getStringWidth(strs[j]);
                if(k > w) {
                    w = k;
                }
            }
            maxWidth[i] = w + 10;
        }
        
        int totalWidth = maxWidth[0] + maxWidth[1];
        
        this.drawCenteredString(this.fontRendererObj, "Session Token Details", this.width / 2, 30, 0xFFFFFF);
        
        int y = 60;
        for (int i = 0; i < 3; i++) {
            this.drawString(this.fontRendererObj, toDraw[0][i], (this.width - totalWidth) / 2, y, 0xAAAAAA);
            this.drawString(this.fontRendererObj, toDraw[1][i], (this.width - totalWidth) / 2 + maxWidth[0], y, 0xFFFFFF);
            y += 14;
        }
        
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}