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

package net.lax1dude.eaglercraft.v1_8.touch_gui;

import net.lax1dude.eaglercraft.v1_8.PointerInputAbstraction;
import net.lax1dude.eaglercraft.v1_8.Touch;
import net.lax1dude.eaglercraft.v1_8.opengl.GameOverlayFramebuffer;
import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.lax1dude.eaglercraft.v1_8.opengl.LevelRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;

import static net.lax1dude.eaglercraft.v1_8.opengl.RealOpenGLEnums.*;

import java.util.Set;

import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.google.common.collect.Sets;

public class TouchOverlayRenderer {

	public static final ResourceLocation spriteSheet = new ResourceLocation("eagler:gui/touch_gui.png");

	static final int[] _fuck = new int[2];

	private GameOverlayFramebuffer overlayFramebuffer;
	private final Minecraft mc;
	private boolean invalid = false;
	private boolean invalidDeep = false;
	private int currentWidth = -1;
	private int currentHeight = -1;

	public TouchOverlayRenderer(Minecraft mc) {
		this.minecraft = mc;
		this.overlayFramebuffer = new GameOverlayFramebuffer(false);
		EnumTouchControl.currentLayout = null;
		EnumTouchControl.setLayoutState(this, EnumTouchLayoutState.IN_GUI);
	}

	public void invalidate() {
		invalid = true;
	}

	public void invalidateDeep() {
		invalid = true;
		invalidDeep = true;
	}

	public void render(int w, int h, Minecraft mc) {
		if(PointerInputAbstraction.isTouchMode()) {
			render0(w, h, mc);
			if(EnumTouchControl.KEYBOARD.visible) {
				int[] pos = EnumTouchControl.KEYBOARD.getLocation(scaledResolution, _fuck);
				int scale = scaledResolution.getScaleFactor();
				int size = EnumTouchControl.KEYBOARD.size * scale;
				Touch.touchSetOpenKeyboardZone(pos[0] * scale,
						(scaledResolution.getScaledHeight() - pos[1] - 1) * scale - size, size, size);
			}else {
				Touch.touchSetOpenKeyboardZone(0, 0, 0, 0);
			}
		}else {
			Touch.touchSetOpenKeyboardZone(0, 0, 0, 0);
		}
	}

	private void render0(int w, int h, Minecraft mc) {
		EnumTouchControl.setLayoutState(this, hashLayoutState());
		int sw = mc.getWindow().getGuiScaledWidth();
		int sh = mc.getWindow().getGuiScaledHeight();
		if(currentWidth != sw || currentHeight != sh) {
			invalidateDeep();
		}
		GlStateManager.disableDepth();
		GlStateManager.disableBlend();
		GlStateManager.disableLighting();
		GlStateManager.enableAlpha();
		GlStateManager.depthMask(false);
		if(invalid) {
			GlStateManager.pushMatrix();
			invalidDeep |= overlayFramebuffer.beginRender(sw, sh);
			GlStateManager.viewport(0, 0, sw, sh);
			if(invalidDeep) {
				currentWidth = sw;
				currentHeight = sh;
				GlStateManager.clearColor(0.0f, 0.0f, 0.0f, 0.0f);
				GlStateManager.clear(GL_COLOR_BUFFER_BIT);
			}
			Set<EnumTouchControl> controls = Sets.newHashSet(EnumTouchControl._VALUES);
			for (ObjectCursor<TouchControlInput> input : TouchControls.touchControls.values()) {
				controls.remove(input.value.control);
			}
			for (EnumTouchControl control : controls) {
				if(invalidDeep || control.invalid) {
					if(control.visible) {
						GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
						control.getRender().call(control, 0, 0, false, mc, scaledResolution);
					}
					control.invalid = false;
				}
			}
			for (ObjectCursor<TouchControlInput> input_ : TouchControls.touchControls.values()) {
				TouchControlInput input = input_.value;
				EnumTouchControl control = input.control;
				if(invalidDeep || control.invalid) {
					if(control.visible) {
						GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
						control.getRender().call(control, input.x, input.y, true, mc, scaledResolution);
					}
					control.invalid = false;
				}
			}
			overlayFramebuffer.endRender();
			invalid = false;
			invalidDeep = false;
			GlStateManager.popMatrix();
			GlStateManager.viewport(0, 0, w, h);
		}
		GlStateManager.bindTexture(overlayFramebuffer.getTexture());
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableAlpha();
		GlStateManager.color(1.0f, 1.0f, 1.0f, Mth.clamp_float(mc.options.touchControlOpacity, 0.0f, 1.0f));
		Tesselator tessellator = Tesselator.getInstance();
		LevelRenderer worldrenderer = tessellator.getLevelRenderer();
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		worldrenderer.pos(0.0D, (double) sh, 500.0D).tex(0.0D, 0.0D).endVertex();
		worldrenderer.pos((double) sw, (double) sh, 500.0D).tex(1.0D, 0.0D).endVertex();
		worldrenderer.pos((double) sw, 0.0D, 500.0D).tex(1.0D, 1.0D).endVertex();
		worldrenderer.pos(0.0D, 0.0D, 500.0D).tex(0.0D, 1.0D).endVertex();
		tessellator.draw();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.enableDepth();
		GlStateManager.depthMask(true);
	}

	private EnumTouchLayoutState hashLayoutState() {
		if(mc.screen != null) {
			return mc.screen.showCopyPasteButtons() ? EnumTouchLayoutState.IN_GUI_TYPING
					: (mc.screen.canCloseGui() ? EnumTouchLayoutState.IN_GUI
							: EnumTouchLayoutState.IN_GUI_NO_BACK);
		}
		LocalPlayer player = mc.player;
		if(player != null) {
			if(player.capabilities.isFlying) {
				 return showDiagButtons() ? EnumTouchLayoutState.IN_GAME_WALK_FLYING : EnumTouchLayoutState.IN_GAME_FLYING;
			}else {
				if(player.capabilities.allowFlying) {
					return showDiagButtons() ? EnumTouchLayoutState.IN_GAME_WALK_CAN_FLY : EnumTouchLayoutState.IN_GAME_CAN_FLY;
				}else {
					return showDiagButtons() ? EnumTouchLayoutState.IN_GAME_WALK : EnumTouchLayoutState.IN_GAME;
				}
			}
		}else {
			return showDiagButtons() ? EnumTouchLayoutState.IN_GAME_WALK : EnumTouchLayoutState.IN_GAME;
		}
	}

	private boolean showDiagButtons() {
		return TouchControls.isPressed(EnumTouchControl.DPAD_UP)
				|| TouchControls.isPressed(EnumTouchControl.DPAD_UP_LEFT)
				|| TouchControls.isPressed(EnumTouchControl.DPAD_UP_RIGHT);
	}

	protected static void drawTexturedModalRect(float xCoord, float yCoord, int minU, int minV, int maxU, int maxV, int scaleFac) {
		float f = 0.00390625F;
		float f1 = 0.00390625F;
		Tesselator tessellator = Tesselator.getInstance();
		LevelRenderer worldrenderer = tessellator.getLevelRenderer();
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		worldrenderer.pos((double) (xCoord + 0.0F), (double) (yCoord + (float) maxV * scaleFac), 0.0)
				.tex((double) ((float) (minU + 0) * f), (double) ((float) (minV + maxV) * f1)).endVertex();
		worldrenderer.pos((double) (xCoord + (float) maxU * scaleFac), (double) (yCoord + (float) maxV * scaleFac), 0.0)
				.tex((double) ((float) (minU + maxU) * f), (double) ((float) (minV + maxV) * f1)).endVertex();
		worldrenderer.pos((double) (xCoord + (float) maxU * scaleFac), (double) (yCoord + 0.0F), 0.0)
				.tex((double) ((float) (minU + maxU) * f), (double) ((float) (minV + 0) * f1)).endVertex();
		worldrenderer.pos((double) (xCoord + 0.0F), (double) (yCoord + 0.0F), 0.0)
				.tex((double) ((float) (minU + 0) * f), (double) ((float) (minV + 0) * f1)).endVertex();
		tessellator.draw();
	}

}