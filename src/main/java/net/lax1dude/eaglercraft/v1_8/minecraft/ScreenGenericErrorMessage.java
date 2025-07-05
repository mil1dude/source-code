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

import org.apache.commons.lang3.StringUtils;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;

public class ScreenGenericErrorMessage extends Screen {

    private String str1;
    private String str2;
    private Screen cont;

    public ScreenGenericErrorMessage(String str1, String str2, Screen cont) {
        super(I18n.get("gui.error.title"));
        this.str1 = StringUtils.isAllEmpty(str1) ? "" : I18n.get(str1);
        this.str2 = StringUtils.isAllEmpty(str2) ? "" : I18n.get(str2);
        this.cont = cont;
    }

    @Override
    public void init() {
        this.clearWidgets();
        this.addRenderableWidget(new net.minecraft.client.gui.components.Button(this.width / 2 - 100, this.height / 6 + 96, 200, 20,
            I18n.get("gui.done"), btn -> this.minecraft.setScreen(cont)));
    }

    @Override
    public void render(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        drawCenteredString(graphics, this.font, str1, this.width / 2, 70, 11184810);
        drawCenteredString(graphics, this.font, str2, this.width / 2, 90, 16777215);
        super.render(graphics, mouseX, mouseY, partialTicks);
    }
}