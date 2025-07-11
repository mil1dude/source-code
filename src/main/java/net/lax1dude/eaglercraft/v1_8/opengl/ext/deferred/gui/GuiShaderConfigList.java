/*
 * Copyright (c) 2023 lax1dude. All Rights Reserved.
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

package net.lax1dude.eaglercraft.v1_8.opengl.ext.deferred.gui;

import java.util.ArrayList;
import java.util.List;

import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.lax1dude.eaglercraft.v1_8.opengl.ext.deferred.EaglerDeferredConfig;
import net.lax1dude.eaglercraft.v1_8.opengl.ext.deferred.ShaderPackInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;

public class GuiShaderConfigList extends ContainerObjectSelectionList<GuiShaderConfigList.Entry> {

    public abstract static class Entry extends ContainerObjectSelectionList.Entry<Entry> {
        public void drawEntry(int entryID, int x, int y, int getListWidth, int var5, int var6, int var7, boolean var8) {}
        public boolean mousePressed(int var1, int var2, int var3, int var4, int var5, int var6) { return false; }
        public void mouseReleased(int var1, int var2, int var3, int var4, int var5, int var6) {}
        public void setSelected(int var1, int var2, int var3) {}
    }

	public static final ResourceLocation shaderPackIcon = new ResourceLocation("eagler:glsl/deferred/shader_pack_icon.png");

	private final GuiShaderConfig screen;

	private final List<Entry> list = new ArrayList<>();

	private static abstract class ShaderOption {

		private final String label;
		private final List<String> desc;

		private ShaderOption(String label, List<String> desc) {
			this.label = label;
			this.desc = desc;
		}

		protected abstract String getDisplayValue();

		protected abstract void toggleOption(net.minecraft.client.gui.components.Button button, int dir);

		protected abstract boolean getDirty();

	}

	private static List<String> loadDescription(String key) {
		List<String> ret = new ArrayList<>();
		String msg;
		int i = 0;
		while(true) {
			if((msg = I18n.get(key + '.' + i)).equals(key + '.' + i)) {
				if(!I18n.get(key + '.' + (i + 1)).equals(key + '.' + (i + 1))) {
					msg = "";
				}else {
					break;
				}
			}
			ret.add(msg);
			++i;
		}
		if(ret.size() == 0) {
			ret.add("" + ChatFormatting.GRAY + ChatFormatting.ITALIC + "(no description found)");
		}
		return ret;
	}

	private static String loadShaderLbl(String key) {
		return I18n.get("shaders.gui.option." + key + ".label");
	}

	private static List<String> loadShaderDesc(String key) {
		return loadDescription("shaders.gui.option." + key + ".desc");
	}

	private static String getColoredOnOff(boolean state, ChatFormatting on, ChatFormatting off) {
		return state ? "" + on + I18n.get("options.on") : "" + off + I18n.get("options.off");
	}

	private void addAllOptions(List<ShaderOption> opts) {
		for(int i = 0, l = opts.size(); i < l; ++i) {
			ShaderOption opt1 = opts.get(i);
			if(++i >= l) {
				list.add(new ListEntryButtonRow(opt1, null, null));
				break;
			}
			ShaderOption opt2 = opts.get(i);
			if(++i >= l) {
				list.add(new ListEntryButtonRow(opt1, opt2, null));
				break;
			}
			list.add(new ListEntryButtonRow(opt1, opt2, opts.get(i)));
		}
	}

	public GuiShaderConfigList(GuiShaderConfig screen, Minecraft mcIn) {
		super(mcIn, screen.width, screen.height, 32, screen.height - 40, 30);
		this.screen = screen;
		this.list.add(new ListEntryHeader("Current Shader Pack:"));
		this.list.add(new ListEntryPackInfo());
		this.list.add(new ListEntrySpacing());
		this.list.add(new ListEntrySpacing());
		this.list.add(new ListEntryHeader(I18n.get("shaders.gui.headerTier1")));
		List<ShaderOption> opts = new ArrayList<>();
		EaglerDeferredConfig conf = mcIn.options.deferredShaderConf;
		if(conf.shaderPackInfo.WAVING_BLOCKS) {
			opts.add(new ShaderOption(loadShaderLbl("WAVING_BLOCKS"), loadShaderDesc("WAVING_BLOCKS")) {
				private final boolean originalValue = conf.wavingBlocks;
				@Override
				protected String getDisplayValue() {
					return getColoredOnOff(conf.wavingBlocks, ChatFormatting.GREEN, ChatFormatting.RED);
				}
				@Override
				protected void toggleOption(net.minecraft.client.gui.components.Button button, int dir) {
					conf.wavingBlocks = !conf.wavingBlocks;
				}
				@Override
				protected boolean getDirty() {
					return conf.wavingBlocks != originalValue;
				}
			});
		}
		if(conf.shaderPackInfo.DYNAMIC_LIGHTS) {
			opts.add(new ShaderOption(loadShaderLbl("DYNAMIC_LIGHTS"), loadShaderDesc("DYNAMIC_LIGHTS")) {
				private final boolean originalValue = conf.dynamicLights;
				@Override
				protected String getDisplayValue() {
					return getColoredOnOff(conf.dynamicLights, ChatFormatting.GREEN, ChatFormatting.RED);
				}
				@Override
				protected void toggleOption(net.minecraft.client.gui.components.Button button, int dir) {
					conf.dynamicLights = !conf.dynamicLights;
				}
				@Override
				protected boolean getDirty() {
					return conf.dynamicLights != originalValue;
				}
			});
		}
		if(conf.shaderPackInfo.GLOBAL_AMBIENT_OCCLUSION) {
			opts.add(new ShaderOption(loadShaderLbl("GLOBAL_AMBIENT_OCCLUSION"), loadShaderDesc("GLOBAL_AMBIENT_OCCLUSION")) {
				private final boolean originalValue = conf.ssao;
				@Override
				protected String getDisplayValue() {
					return getColoredOnOff(conf.ssao, ChatFormatting.GREEN, ChatFormatting.RED);
				}
				@Override
				protected void toggleOption(net.minecraft.client.gui.components.Button button, int dir) {
					conf.ssao = !conf.ssao;
				}
				@Override
				protected boolean getDirty() {
					return conf.ssao != originalValue;
				}
			});
		}
		if(conf.shaderPackInfo.SHADOWS_SUN) {
			opts.add(new ShaderOption(loadShaderLbl("SHADOWS_SUN"), loadShaderDesc("SHADOWS_SUN")) {
				private final int originalValue = conf.shadowsSun;
				@Override
				protected String getDisplayValue() {
					return conf.shadowsSun == 0 ? "" + ChatFormatting.RED + "0" : "" + ChatFormatting.YELLOW + (1 << (conf.shadowsSun + 3));
				}
				@Override
				protected void toggleOption(net.minecraft.client.gui.components.Button button, int dir) {
					conf.shadowsSun = (conf.shadowsSun + dir + 5) % 5;
				}
				@Override
				protected boolean getDirty() {
					return conf.shadowsSun != originalValue;
				}
			});
		}
		if(conf.shaderPackInfo.REFLECTIONS_PARABOLOID) {
			opts.add(new ShaderOption(loadShaderLbl("REFLECTIONS_PARABOLOID"), loadShaderDesc("REFLECTIONS_PARABOLOID")) {
				private final boolean originalValue = conf.useEnvMap;
				@Override
				protected String getDisplayValue() {
					return getColoredOnOff(conf.useEnvMap, ChatFormatting.GREEN, ChatFormatting.RED);
				}
				@Override
				protected void toggleOption(net.minecraft.client.gui.components.Button button, int dir) {
					conf.useEnvMap = !conf.useEnvMap;
				}
				@Override
				protected boolean getDirty() {
					return conf.useEnvMap != originalValue;
				}
			});
		}
		if(conf.shaderPackInfo.POST_LENS_DISTORION) {
			opts.add(new ShaderOption(loadShaderLbl("POST_LENS_DISTORION"), loadShaderDesc("POST_LENS_DISTORION")) {
				private final boolean originalValue = conf.lensDistortion;
				@Override
				protected String getDisplayValue() {
					return getColoredOnOff(conf.lensDistortion, ChatFormatting.GREEN, ChatFormatting.RED);
				}
				@Override
				protected void toggleOption(net.minecraft.client.gui.components.Button button, int dir) {
					conf.lensDistortion = !conf.lensDistortion;
				}
				@Override
				protected boolean getDirty() {
					return conf.lensDistortion != originalValue;
				}
			});
		}
		if(conf.shaderPackInfo.SUBSURFACE_SCATTERING) {
			opts.add(new ShaderOption(loadShaderLbl("SUBSURFACE_SCATTERING"), loadShaderDesc("SUBSURFACE_SCATTERING")) {
				private final boolean originalValue = conf.subsurfaceScattering;
				@Override
				protected String getDisplayValue() {
					return getColoredOnOff(conf.subsurfaceScattering, ChatFormatting.GREEN, ChatFormatting.RED);
				}
				@Override
				protected void toggleOption(net.minecraft.client.gui.components.Button button, int dir) {
					conf.subsurfaceScattering = !conf.subsurfaceScattering;
				}
				@Override
				protected boolean getDirty() {
					return conf.subsurfaceScattering != originalValue;
				}
			});
		}
		if(conf.shaderPackInfo.POST_LENS_FLARES) {
			opts.add(new ShaderOption(loadShaderLbl("POST_LENS_FLARES"), loadShaderDesc("POST_LENS_FLARES")) {
				private final boolean originalValue = conf.lensFlares;
				@Override
				protected String getDisplayValue() {
					return getColoredOnOff(conf.lensFlares, ChatFormatting.GREEN, ChatFormatting.RED);
				}
				@Override
				protected void toggleOption(net.minecraft.client.gui.components.Button button, int dir) {
					conf.lensFlares = !conf.lensFlares;
				}
				@Override
				protected boolean getDirty() {
					return conf.lensFlares != originalValue;
				}
			});
		}
		if(conf.shaderPackInfo.POST_FXAA) {
			opts.add(new ShaderOption(loadShaderLbl("POST_FXAA"), loadShaderDesc("POST_FXAA")) {
				private final boolean originalValue = conf.fxaa;
				@Override
				protected String getDisplayValue() {
					return getColoredOnOff(conf.fxaa, ChatFormatting.GREEN, ChatFormatting.RED);
				}
				@Override
				protected void toggleOption(net.minecraft.client.gui.components.Button button, int dir) {
					conf.fxaa = !conf.fxaa;
				}
				@Override
				protected boolean getDirty() {
					return conf.fxaa != originalValue;
				}
			});
		}
		this.addAllOptions(opts);
		opts.clear();
		this.list.add(new ListEntryHeader(I18n.get("shaders.gui.headerTier2")));
		if(conf.shaderPackInfo.SHADOWS_COLORED) {
			opts.add(new ShaderOption(loadShaderLbl("SHADOWS_COLORED"), loadShaderDesc("SHADOWS_COLORED")) {
				private final boolean originalValue = conf.shadowsColored;
				@Override
				protected String getDisplayValue() {
					return getColoredOnOff(conf.shadowsColored, ChatFormatting.GREEN, ChatFormatting.RED);
				}
				@Override
				protected void toggleOption(net.minecraft.client.gui.components.Button button, int dir) {
					conf.shadowsColored = !conf.shadowsColored;
				}
				@Override
				protected boolean getDirty() {
					return conf.shadowsColored != originalValue;
				}
			});
		}
		if(conf.shaderPackInfo.SHADOWS_SMOOTHED) {
			opts.add(new ShaderOption(loadShaderLbl("SHADOWS_SMOOTHED"), loadShaderDesc("SHADOWS_SMOOTHED")) {
				private final boolean originalValue = conf.shadowsSmoothed;
				@Override
				protected String getDisplayValue() {
					return getColoredOnOff(conf.shadowsSmoothed, ChatFormatting.GREEN, ChatFormatting.RED);
				}
				@Override
				protected void toggleOption(net.minecraft.client.gui.components.Button button, int dir) {
					conf.shadowsSmoothed = !conf.shadowsSmoothed;
				}
				@Override
				protected boolean getDirty() {
					return conf.shadowsSmoothed != originalValue;
				}
			});
		}
		if(conf.shaderPackInfo.REALISTIC_WATER) {
			opts.add(new ShaderOption(loadShaderLbl("REALISTIC_WATER"), loadShaderDesc("REALISTIC_WATER")) {
				private final boolean originalValue = conf.realisticWater;
				@Override
				protected String getDisplayValue() {
					return getColoredOnOff(conf.realisticWater, ChatFormatting.GREEN, ChatFormatting.RED);
				}
				@Override
				protected void toggleOption(net.minecraft.client.gui.components.Button button, int dir) {
					conf.realisticWater = !conf.realisticWater;
				}
				@Override
				protected boolean getDirty() {
					return conf.realisticWater != originalValue;
				}
			});
		}
		if(conf.shaderPackInfo.POST_BLOOM) {
			opts.add(new ShaderOption(loadShaderLbl("POST_BLOOM"), loadShaderDesc("POST_BLOOM")) {
				private final boolean originalValue = conf.bloom;
				@Override
				protected String getDisplayValue() {
					return getColoredOnOff(conf.bloom, ChatFormatting.GREEN, ChatFormatting.RED);
				}
				@Override
				protected void toggleOption(net.minecraft.client.gui.components.Button button, int dir) {
					conf.bloom = !conf.bloom;
				}
				@Override
				protected boolean getDirty() {
					return conf.bloom != originalValue;
				}
			});
		}
		if(conf.shaderPackInfo.LIGHT_SHAFTS) {
			opts.add(new ShaderOption(loadShaderLbl("LIGHT_SHAFTS"), loadShaderDesc("LIGHT_SHAFTS")) {
				private final boolean originalValue = conf.lightShafts;
				@Override
				protected String getDisplayValue() {
					return getColoredOnOff(conf.lightShafts, ChatFormatting.GREEN, ChatFormatting.RED);
				}
				@Override
				protected void toggleOption(net.minecraft.client.gui.components.Button button, int dir) {
					conf.lightShafts = !conf.lightShafts;
				}
				@Override
				protected boolean getDirty() {
					return conf.lightShafts != originalValue;
				}
			});
		}
		if(conf.shaderPackInfo.SCREEN_SPACE_REFLECTIONS) {
			opts.add(new ShaderOption(loadShaderLbl("SCREEN_SPACE_REFLECTIONS"), loadShaderDesc("SCREEN_SPACE_REFLECTIONS")) {
				private final boolean originalValue = conf.raytracing;
				@Override
				protected String getDisplayValue() {
					return getColoredOnOff(conf.raytracing, ChatFormatting.GREEN, ChatFormatting.RED);
				}
				@Override
				protected void toggleOption(net.minecraft.client.gui.components.Button button, int dir) {
					conf.raytracing = !conf.raytracing;
				}
				@Override
				protected boolean getDirty() {
					return conf.raytracing != originalValue;
				}
			});
		}
		this.addAllOptions(opts);
		setAllDisabled(!mcIn.options.shaders);
	}

	public void setAllDisabled(boolean disable) {
		for(int i = 0, l = list.size(); i < l; ++i) {
			IGuiListEntry etr = list.get(i);
			if(etr instanceof ListEntryButtonRow) {
				ListEntryButtonRow etr2 = (ListEntryButtonRow)etr;
				if(etr2.button1 != null) {
					etr2.button1.enabled = !disable;
				}
				if(etr2.button2 != null) {
					etr2.button2.enabled = !disable;
				}
				if(etr2.button3 != null) {
					etr2.button3.enabled = !disable;
				}
			}
		}
	}

	@Override
	public Entry getEntry(int var1) {
		return list.get(var1);
	}

	@Override
	protected int getSize() {
		return list.size();
	}

	@Override
	public int getListWidth() {
		return 225;
	}

	private class ListEntryPackInfo extends Entry {
		@Override
		public void render(PoseStack poseStack, int entryID, int y, int x, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
			drawEntry(entryID, x, y, width, height, 0, 0, isMouseOver);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			return mousePressed(0, (int)mouseX, (int)mouseY, 0, 0, button);
		}

		@Override
		public void mouseReleased(double mouseX, double mouseY, int button) {
			mouseReleased(0, (int)mouseX, (int)mouseY, 0, 0, button);
		}

		@Override
		public void drawEntry(int entryID, int x, int y, int getListWidth, int var5, int var6, int var7, boolean var8) {
			Minecraft mc = Minecraft.getMinecraft();
			ShaderPackInfo info = mc.options.deferredShaderConf.shaderPackInfo;
			String packNameString = info.name;
			int strWidth = mc.font.getStringWidth(packNameString) + 40;
			if(strWidth < 210) {
				strWidth = 210;
			}
			int x2 = strWidth > getListWidth * 2 ? x : x + (getListWidth - strWidth) / 2;
			screen.drawString(mc.font, packNameString, x2 + 38, y + 3, 0xFFFFFF);
			screen.drawString(mc.font, "Author: " + info.author, x2 + 38, y + 14, 0xBBBBBB);
			screen.drawString(mc.font, "Version: " + info.vers, x2 + 38, y + 25, 0x888888);
			List<String> descLines = mc.font.listFormattedStringToWidth(info.desc, strWidth);
			for(int i = 0, l = descLines.size(); i < l; ++i) {
				screen.drawString(mc.font, descLines.get(i), x2, y + 43 + i * 9, 0xBBBBBB);
			}
			mc.getTextureManager().bindTexture(shaderPackIcon);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			Gui.drawModalRectWithCustomSizedTexture(x2, y + 2, 0, 0, 32, 32, 32, 32);
		}

		@Override
		public void setSelected(int var1, int var2, int var3) {
			
		}

		@Override
		public boolean mousePressed(int var1, int var2, int var3, int var4, int var5, int var6) {
			return false;
		}

		@Override
		public void mouseReleased(int var1, int var2, int var3, int var4, int var5, int var6) {
			
		}
		
	}

	private class ListEntrySpacing extends Entry {
		@Override
		public void render(PoseStack poseStack, int entryID, int y, int x, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
			drawEntry(entryID, x, y, width, height, 0, 0, isMouseOver);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			return mousePressed(0, (int)mouseX, (int)mouseY, 0, 0, button);
		}

		@Override
		public void mouseReleased(double mouseX, double mouseY, int button) {
			mouseReleased(0, (int)mouseX, (int)mouseY, 0, 0, button);
		}

		@Override
		public void setSelected(int var1, int var2, int var3) {
			
		}

		@Override
		public void drawEntry(int var1, int var2, int var3, int var4, int var5, int var6, int var7, boolean var8) {
			
		}

		@Override
		public boolean mousePressed(int var1, int var2, int var3, int var4, int var5, int var6) {
			return false;
		}

		@Override
		public void mouseReleased(int var1, int var2, int var3, int var4, int var5, int var6) {
			
		}
		
	}

	private class ListEntryHeader extends Entry {
		@Override
		public void render(PoseStack poseStack, int entryID, int y, int x, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
			drawEntry(entryID, x, y, width, height, 0, 0, isMouseOver);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			return mousePressed(0, (int)mouseX, (int)mouseY, 0, 0, button);
		}

		@Override
		public void mouseReleased(double mouseX, double mouseY, int button) {
			mouseReleased(0, (int)mouseX, (int)mouseY, 0, 0, button);
		}

		private final String text;

		private ListEntryHeader(String text) {
			this.text = text;
		}

		@Override
		public void setSelected(int var1, int var2, int var3) {
			
		}

		@Override
		public void drawEntry(int entryID, int x, int y, int getListWidth, int var5, int var6, int var7, boolean var8) {
			screen.drawString(screen.getFont(), text, x, y + 10, 0xFFFFFF);
		}

		@Override
		public boolean mousePressed(int var1, int var2, int var3, int var4, int var5, int var6) {
			return false;
		}

		@Override
		public void mouseReleased(int var1, int var2, int var3, int var4, int var5, int var6) {
			
		}
		
	}

	private class ListEntryButtonRow extends Entry {
		@Override
		public void render(PoseStack poseStack, int entryID, int y, int x, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
			drawEntry(entryID, x, y, width, height, 0, 0, isMouseOver);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			return mousePressed(0, (int)mouseX, (int)mouseY, 0, 0, button);
		}

		@Override
		public void mouseReleased(double mouseX, double mouseY, int button) {
			mouseReleased(0, (int)mouseX, (int)mouseY, 0, 0, button);
		}

		private final ShaderOption opt1;
		private final ShaderOption opt2;
		private final ShaderOption opt3;

		private net.minecraft.client.gui.components.Button button1;
		private net.minecraft.client.gui.components.Button button2;
		private net.minecraft.client.gui.components.Button button3;

		private ListEntryButtonRow(ShaderOption opt1, ShaderOption opt2, ShaderOption opt3) {
			this.opt1 = opt1;
			this.opt2 = opt2;
			this.opt3 = opt3;
			if(this.opt1 != null) {
				this.button1 = new net.minecraft.client.gui.components.Button(0, 0, 0, 73, 20, this.opt1.label + ": " + this.opt1.getDisplayValue());
				this.button1.fontScale = 0.78f - (this.opt1.label.length() * 0.01f);
			}
			if(this.opt2 != null) {
				this.button2 = new net.minecraft.client.gui.components.Button(0, 0, 0, 73, 20, this.opt2.label + ": " + this.opt2.getDisplayValue());
				this.button2.fontScale = 0.78f - (this.opt2.label.length() * 0.01f);
			}
			if(this.opt3 != null) {
				this.button3 = new net.minecraft.client.gui.components.Button(0, 0, 0, 73, 20, this.opt3.label + ": " + this.opt3.getDisplayValue());
				this.button3.fontScale = 0.78f - (this.opt3.label.length() * 0.01f);
			}
		}

		@Override
		public void setSelected(int var1, int var2, int var3) {
			
		}

		@Override
		public void drawEntry(int entryID, int x, int y, int getListWidth, int var5, int var6, int var7, boolean var8) {
			if(this.button1 != null) {
				this.button1.xPosition = x;
				this.button1.yPosition = y;
				this.button1.drawButton(mc, var6, var7);
				if(this.button1.isMouseOver() && this.button1.yPosition + 10 < bottom && this.button1.yPosition + 10 > top) {
					renderTooltip(var6, var7 + 15, this.opt1.desc);
				}
			}
			if(this.button2 != null) {
				this.button2.xPosition = x + 75;
				this.button2.yPosition = y;
				this.button2.drawButton(mc, var6, var7);
				if(this.button2.isMouseOver() && this.button2.yPosition + 10 < bottom && this.button2.yPosition + 10 > top) {
					renderTooltip(var6, var7 + 15, this.opt2.desc);
				}
			}
			if(this.button3 != null) {
				this.button3.xPosition = x + 150;
				this.button3.yPosition = y;
				this.button3.drawButton(mc, var6, var7);
				if(this.button3.isMouseOver() && this.button3.yPosition + 10 < bottom && this.button3.yPosition + 10 > top) {
					renderTooltip(var6, var7 + 15, this.opt3.desc);
				}
			}
		}

		@Override
		public boolean mousePressed(int var1, int var2, int var3, int var4, int var5, int var6) {
			if(var4 != 0) return false;
			if(this.button1 != null) {
				if(this.button1.yPosition + 15 < bottom && this.button1.yPosition + 5 > top) {
					if(this.button1.mousePressed(mc, var2, var3)) {
						this.opt1.toggleOption(this.button1, var4 == 1 ? -1 : 1);
						this.button1.displayString = (this.opt1.getDirty() ? "*" : "") + this.opt1.label + ": " + this.opt1.getDisplayValue();
						this.button1.playPressSound(mc.getSoundManager());
					}
				}
			}
			if(this.button2 != null) {
				if(this.button2.yPosition + 15 < bottom && this.button2.yPosition + 5 > top) {
					if(this.button2.mousePressed(mc, var2, var3)) {
						this.opt2.toggleOption(this.button2, var4 == 1 ? -1 : 1);
						this.button2.displayString = (this.opt2.getDirty() ? "*" : "") + this.opt2.label + ": " + this.opt2.getDisplayValue();
						this.button2.playPressSound(mc.getSoundManager());
					}
				}
			}
			if(this.button3 != null) {
				if(this.button3.yPosition + 15 < bottom && this.button3.yPosition + 5 > top) {
					if(this.button3.mousePressed(mc, var2, var3)) {
						this.opt3.toggleOption(this.button3, var4 == 1 ? -1 : 1);
						this.button3.displayString = (this.opt3.getDirty() ? "*" : "") + this.opt3.label + ": " + this.opt3.getDisplayValue();
						this.button3.playPressSound(mc.getSoundManager());
					}
				}
			}
			return false;
		}

		@Override
		public void mouseReleased(int var1, int var2, int var3, int var4, int var5, int var6) {
			
		}
		
	}

	private List<String> tooltipToShow = null;
	private int tooltipToShowX = 0;
	private int tooltipToShowY = 0;

	public void postRender(int mx, int my, float partialTicks) {
		if(tooltipToShow != null) {
			screen.width *= 2;
			screen.height *= 2;
			GlStateManager.pushMatrix();
			GlStateManager.scale(0.5f, 0.5f, 0.5f);
			screen.renderTooltip(tooltipToShow, tooltipToShowX * 2, tooltipToShowY * 2);
			GlStateManager.popMatrix();
			screen.width /= 2;
			screen.height /= 2;
			tooltipToShow = null;
		}
	}

	private void renderTooltip(int x, int y, List<String> msg) {
		renderTooltip(x, y, 250, msg);
	}

	private void renderTooltip(int x, int y, int width, List<String> msg) {
		List<String> tooltipList = new ArrayList<>(msg.size() * 2);
		for(int i = 0, l = msg.size(); i < l; ++i) {
			String s = msg.get(i);
			if(s.length() > 0) {
				tooltipList.addAll(screen.getFont().listFormattedStringToWidth(s, width));
			}else {
				tooltipList.add("");
			}
		}
		tooltipToShow = tooltipList;
		tooltipToShowX = x;
		tooltipToShowY = y;
	}

	public boolean isDirty() {
		for(int i = 0, l = list.size(); i < l; ++i) {
			IGuiListEntry etr = list.get(i);
			if(etr instanceof ListEntryButtonRow) {
				ListEntryButtonRow etr2 = (ListEntryButtonRow)etr;
				if(etr2.opt1 != null) {
					if(etr2.opt1.getDirty()) {
						return true;
					}
				}
				if(etr2.opt2 != null) {
					if(etr2.opt2.getDirty()) {
						return true;
					}
				}
				if(etr2.opt3 != null) {
					if(etr2.opt3.getDirty()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public void resize() {
		width = screen.width;
		height = screen.height;
		top = 32;
		bottom = screen.height - 40;
	}

}