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

package net.lax1dude.eaglercraft.v1_8.update;

import net.lax1dude.eaglercraft.v1_8.opengl.EaglercraftGPU;
import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.lax1dude.eaglercraft.v1_8.sp.lan.LANServerController;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;

import static net.lax1dude.eaglercraft.v1_8.opengl.RealOpenGLEnums.*;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class GuiUpdateCheckerOverlay extends Gui {

	private static final ResourceLocation eaglerIcons = new ResourceLocation("eagler:gui/eagler_gui.png");

	private Minecraft mc;

	private int width;
	private int height;

	private int totalHeightOffset = 0;

	private boolean isIngame;
	private Screen backScreen;

	private net.minecraft.client.gui.components.Button checkForUpdatesButton;
	private net.minecraft.client.gui.components.Button startDownloadButton;
	private net.minecraft.client.gui.components.Button viewAllUpdatesButton;
	private net.minecraft.client.gui.components.Button dismissUpdatesButton;

	public GuiUpdateCheckerOverlay(boolean isIngame, Screen screen) {
		this.isIngame = isIngame;
		this.backScreen = screen;
	}

	public void setResolution(Minecraft mc, int w, int h) {
		if(!UpdateService.supported()) {
			return;
		}
		this.minecraft = mc;
		this.width = w;
		this.height = h;
		checkForUpdatesButton = new net.minecraft.client.gui.components.Button(0, 0, 0, 150, 20, I18n.get("update.button") + " " + I18n.get(mc.options.enableUpdateSvc ? "gui.yes" : "gui.no"));
		startDownloadButton = new net.minecraft.client.gui.components.Button(1, 1, 0, 115, 20, I18n.get("update.startDownload"));
		viewAllUpdatesButton = new net.minecraft.client.gui.components.Button(2, 1, 0, 115, 20, I18n.get("update.viewAll", 0));
		dismissUpdatesButton = new net.minecraft.client.gui.components.Button(3, 1, 0, 115, 20, I18n.get("update.dismiss"));
	}

	public void drawScreen(int mx, int my, float partialTicks) {
		if(!UpdateService.supported()) {
			return;
		}
		UpdateProgressStruct progressState = UpdateService.getUpdatingStatus();
		if(progressState.isBusy) {
			drawScreenBusy(mx, my, partialTicks, progressState);
			return;
		}
		
		checkForUpdatesButton.visible = isIngame;
		startDownloadButton.visible = false;
		viewAllUpdatesButton.visible = false;
		dismissUpdatesButton.visible = false;
		totalHeightOffset = 0;
		
		int i = UpdateService.getAvailableUpdates().size();
		boolean shownSP = i > 0 || !mc.isSingleplayer() || LANServerController.isHostingLAN();
		checkForUpdatesButton.visible &= shownSP;
		
		if(mc.options.enableUpdateSvc) {
			String str;
			UpdateCertificate cert = UpdateService.getLatestUpdateFound();
			if(cert != null) {
				startDownloadButton.visible = true;
				viewAllUpdatesButton.visible = true;
				dismissUpdatesButton.visible = true;
				viewAllUpdatesButton.displayString = I18n.get("update.viewAll", i);
				str = I18n.get("update.found");
				mc.font.drawStringWithShadow(str, 3, 22, 0xFFFFAA);
				
				int embedY = 35;
				int embedWidth = 115;
				int embedWidth2 = (int)(embedWidth / 0.75f);
				
				List<String> lst = cert.bundleVersionComment.length() == 0 ? null : mc.font.listFormattedStringToWidth(cert.bundleVersionComment, embedWidth2 - 14);
				
				int embedHeight = 44;
				if(lst != null) {
					embedHeight += 3 + lst.size() * 6;
				}
				
				GlStateManager.pushMatrix();
				GlStateManager.translate(1.0f, embedY, 0.0f);
				GlStateManager.scale(0.75f, 0.75f, 0.75f);
				
				int embedHeight2 = (int)(embedHeight / 0.75f);
				
				drawGradientRect(1, 1, embedWidth2 - 1, embedHeight2 - 1, 0xFFFFFFAA, 0xFFFFFFAA);
				drawGradientRect(0, 1, embedWidth2, 2, 0xFF000000, 0xFF000000);
				drawGradientRect(0, embedHeight2 - 1, embedWidth2, embedHeight2, 0xFF000000, 0xFF000000);
				drawGradientRect(0, 1, 1, embedHeight2 - 1, 0xFF000000, 0xFF000000);
				drawGradientRect(embedWidth2 - 1, 1, embedWidth2, embedHeight2 - 1, 0xFF000000, 0xFF000000);
				
				mc.getTextureManager().bindTexture(eaglerIcons);
				GlStateManager.pushMatrix();
				GlStateManager.scale(0.3f, 0.3f, 0.3f);
				drawGradientRect(23, 23, 127, 127, 0xFF000000, 0xFF000000);
				EaglercraftGPU.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR); // rip
				drawTexturedModalRect(25, 25, 156, 0, 100, 100);
				EaglercraftGPU.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
				GlStateManager.popMatrix();
	
				mc.font.drawString(ChatFormatting.UNDERLINE + cert.bundleDisplayName, 45, 11, 0x000000);
				mc.font.drawString(I18n.get("update.update") + " " + ChatFormatting.DARK_RED + cert.bundleDisplayVersion, 45, 25, 0x000000);
				
				if(lst != null) {
					for(int j = 0, l = lst.size(); j < l; ++j) {
						mc.font.drawString(lst.get(j), 5, 42 + j * 8, 0x000000);
					}
				}
				
				mc.font.drawString(I18n.get("update.author") + " " + cert.bundleAuthorName, 5, 44 + (lst == null ? 0 : (3 + lst.size() * 8)), 0x777777);
				
				startDownloadButton.yPosition = embedHeight + embedY + 5;
				viewAllUpdatesButton.yPosition = startDownloadButton.yPosition + 22;
				dismissUpdatesButton.yPosition = viewAllUpdatesButton.yPosition + 22;
				totalHeightOffset = dismissUpdatesButton.yPosition + 20;
				
				GlStateManager.popMatrix();
			}else if(isIngame) {
				if(shownSP) {
					str = I18n.get("update.noneNew");
					mc.font.drawString(str, 3, 22, 0xDDDDDD);
					if(i > 0) {
						viewAllUpdatesButton.yPosition = 40;
						viewAllUpdatesButton.visible = true;
						viewAllUpdatesButton.displayString = I18n.get("update.viewAll", i);
						totalHeightOffset = 60;
					}else {
						totalHeightOffset = 32;
					}
				}
			}
		}

		checkForUpdatesButton.drawButton(mc, mx, my);
		startDownloadButton.drawButton(mc, mx, my);
		viewAllUpdatesButton.drawButton(mc, mx, my);
		dismissUpdatesButton.drawButton(mc, mx, my);
	}

	public void drawScreenBusy(int mx, int my, float partialTicks, UpdateProgressStruct progressState) {
		if(!UpdateService.supported()) {
			return;
		}
		checkForUpdatesButton.visible = false;
		startDownloadButton.visible = false;
		viewAllUpdatesButton.visible = false;
		dismissUpdatesButton.visible = false;
		GlStateManager.pushMatrix();
		GlStateManager.translate(1.0f, isIngame ? 0.0f : 18.0f, 0.0f);
		String str = I18n.get("update.downloading");
		mc.font.drawStringWithShadow(str, 2, 2, 0xFFFFAA);
		GlStateManager.translate(0.0f, 14.0f, 0.0f);
		GlStateManager.scale(0.75f, 0.75f, 0.75f);
		if(!StringUtils.isAllBlank(progressState.statusString1)) {
			str = progressState.statusString1;
			mc.font.drawStringWithShadow(str, 3, 0, 0xFFFFFF);
		}
		int cc = isIngame ? 0xBBBBBB : 0xFFFFFF;
		if(!StringUtils.isAllBlank(progressState.statusString2)) {
			str = progressState.statusString2;
			mc.font.drawStringWithShadow(str, 3, 11, cc);
		}
		int progX1 = 3;
		int progY1 = 22;
		int progX2 = 135;
		int progY2 = 32;
		float prog = progressState.progressBar;
		if(prog >= 0.0f) {
			int bk = 0xFFBBBBBB;
			int fg = 0xFFDD0000;
			drawGradientRect(progX1 + 1, progY1 + 1, progX1 + (int)((progX2 - progX1 - 1) * prog), progY2 - 1, fg, fg);
			drawGradientRect(progX1 + (int)((progX2 - progX1 - 1) * prog), progY1 + 1, progX2 - 1, progY2 - 1, bk, bk);
			drawGradientRect(progX1, progY1, progX2, progY1 + 1, 0xFF000000, 0xFF000000);
			drawGradientRect(progX1, progY2 - 1, progX2, progY2, 0xFF000000, 0xFF000000);
			drawGradientRect(progX1, progY1 + 1, progX1 + 1, progY2 - 1, 0xFF000000, 0xFF000000);
			drawGradientRect(progX2 - 1, progY1 + 1, progX2, progY2 - 1, 0xFF000000, 0xFF000000);
		}
		totalHeightOffset = 32;
		if(!StringUtils.isAllBlank(progressState.statusString3)) {
			GlStateManager.translate(0.0f, progY2 + 2, 0.0f);
			GlStateManager.scale(0.66f, 0.66f, 0.66f);
			str = progressState.statusString3;
			List<String> wrappedURL = mc.font.listFormattedStringToWidth(str, (int)((progX2 - progX1) * 1.5f));
			for(int i = 0, l = wrappedURL.size(); i < l; ++i) {
				str = wrappedURL.get(i);
				mc.font.drawStringWithShadow(str, 5, i * 11, cc);
			}
			totalHeightOffset += (int)(wrappedURL.size() * 5.5f);
		}
		GlStateManager.popMatrix();

	}

	public void mouseClicked(int mx, int my, int btn) {
		if(!UpdateService.supported()) {
			return;
		}
		if (btn == 0) {
			if(checkForUpdatesButton.mousePressed(mc, mx, my)) {
				mc.options.enableUpdateSvc = !mc.options.enableUpdateSvc;
				mc.options.saveOptions();
				checkForUpdatesButton.displayString =  I18n.get("update.button") + " " + I18n.get(mc.options.enableUpdateSvc ? "gui.yes" : "gui.no");
			}
			if(startDownloadButton.mousePressed(mc, mx, my)) {
				if(!UpdateService.getUpdatingStatus().isBusy) {
					UpdateCertificate cert = UpdateService.getLatestUpdateFound();
					if(cert != null) {
						UpdateService.startClientUpdateFrom(cert);
					}
				}
			}
			if(viewAllUpdatesButton.mousePressed(mc, mx, my)) {
				mc.displayScreen(new GuiUpdateVersionList(backScreen));
			}
			if(dismissUpdatesButton.mousePressed(mc, mx, my)) {
				UpdateCertificate cert = UpdateService.getLatestUpdateFound();
				if(cert != null) {
					UpdateService.dismiss(cert);
				}
			}
		}
	}

	public int getSharedLevelDataYOffset() {
		return totalHeightOffset;
	}
}