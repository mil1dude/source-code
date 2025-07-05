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

package net.lax1dude.eaglercraft.v1_8.minecraft;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class ScreenVideoSettingsWarning extends Screen {

    private final Screen cont;
    private final int mask;
    private final List<String> messages = new ArrayList<>();
    private int top = 0;

    public static final int WARNING_VSYNC = 1;
    public static final int WARNING_RENDER_DISTANCE = 2;
    public static final int WARNING_FRAME_LIMIT = 4;

    public ScreenVideoSettingsWarning(Screen cont, int mask) {
        super(Component.translatable("options.badVideoSettingsDetected.title"));
        this.cont = cont;
        this.mask = mask;
    }

    @Override
    public void init() {
        messages.clear();
        messages.add(ChatFormatting.RED + I18n.get("options.badVideoSettingsDetected.title"));
        messages.add(null);
        messages.add(ChatFormatting.GRAY + I18n.get("options.badVideoSettingsDetected.0"));
        messages.add(ChatFormatting.GRAY + I18n.get("options.badVideoSettingsDetected.1"));
        if((mask & WARNING_VSYNC) != 0) {
            messages.add(null);
            messages.add(I18n.get("options.badVideoSettingsDetected.vsync.0"));
            messages.add(I18n.get("options.badVideoSettingsDetected.vsync.1"));
            messages.add(I18n.get("options.badVideoSettingsDetected.vsync.2"));
            messages.add(I18n.get("options.badVideoSettingsDetected.vsync.3"));
            messages.add(I18n.get("options.badVideoSettingsDetected.vsync.4"));
        }
        // Die folgenden Zeilen müssen ggf. an dein neues Options-System angepasst werden!
        // messages.add(I18n.get("options.badVideoSettingsDetected.renderDistance.0", ...));
        // messages.add(I18n.get("options.badVideoSettingsDetected.frameLimit.0", ...));
        int j = 0;
        for(int i = 0, l = messages.size(); i < l; ++i) {
            if(messages.get(i) != null) {
                j += 9;
            }else {
                j += 5;
            }
        }
        top = this.height / 6 + j / -12;
        j += top;
        this.clearWidgets();
        this.addRenderableWidget(new net.minecraft.client.gui.components.Button(this.width / 2 - 100, j + 16, 200, 20,
            Component.translatable("options.badVideoSettingsDetected.fixSettings"), btn -> this.onFixSettings()));
        this.addRenderableWidget(new net.minecraft.client.gui.components.Button(this.width / 2 - 100, j + 40, 200, 20,
            Component.translatable("options.badVideoSettingsDetected.continueAnyway"), btn -> this.minecraft.setScreen(cont)));
        this.addRenderableWidget(new net.minecraft.client.gui.components.Button(this.width / 2 - 100, j + 64, 200, 20,
            Component.translatable("options.badVideoSettingsDetected.doNotShowAgain"), btn -> this.onDoNotShowAgain()));
    }

    @Override
    public void render(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        int j = 0;
        for(int i = 0, l = messages.size(); i < l; ++i) {
            String str = messages.get(i);
            if(str != null) {
                drawCenteredString(graphics, this.font, str, this.width / 2, top + j, 16777215);
                j += 9;
            }else {
                j += 5;
            }
        }
        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    private void onFixSettings() {
        // Passe das an dein neues Options-System an!
        // this.minecraft.options.fixBadVideoSettings();
        // this.minecraft.options.save();
        this.minecraft.setScreen(cont);
    }

    private void onDoNotShowAgain() {
        // Passe das an dein neues Options-System an!
        // this.minecraft.options.hideVideoSettingsWarning = true;
        // this.minecraft.options.save();
        this.minecraft.setScreen(cont);
    }
}