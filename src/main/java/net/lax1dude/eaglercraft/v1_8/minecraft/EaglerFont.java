/*
 * Copyright (c) 2022 lax1dude. All Rights Reserved.
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

import net.lax1dude.eaglercraft.v1_8.opengl.EaglercraftGPU;
import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.lax1dude.eaglercraft.v1_8.opengl.InstancedFont;
import net.lax1dude.eaglercraft.v1_8.opengl.LevelRenderer;
import net.minecraft.client.gui.Font;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.Options;

public class EaglerFont extends Font {

	private final int[] temporaryCodepointArray = new int[6553];

	public static Font createSupportedFont(Options gameSettingsIn, ResourceLocation location,
			TextureManager textureManagerIn, boolean unicode) {
		if(EaglercraftGPU.checkInstancingCapable()) {
			return new EaglerFont(gameSettingsIn, location, textureManagerIn, unicode);
		}else {
			return new Font(gameSettingsIn, location, textureManagerIn, unicode);
		}
	}

	public EaglerFont(Options gameSettingsIn, ResourceLocation location, TextureManager textureManagerIn,
			boolean unicode) {
		super(gameSettingsIn, location, textureManagerIn, unicode);
	}

	public int drawString(String text, float x, float y, int color, boolean dropShadow) {
		if (text == null || text.length() == 0) {
			this.getX() = x + (dropShadow ? 1 : 0);
			this.getY() = y;
		} else {
			if(this.unicodeFlag || !decodeASCIICodepointsAndValidate(text)) {
				return super.drawString(text, x, y, color, dropShadow);
			}
			this.resetStyles();
			if ((color & 0xFC000000) == 0) {
				color |= 0xFF000000;
			}
			this.red = (float) (color >>> 16 & 255) / 255.0F;
			this.blue = (float) (color >>> 8 & 255) / 255.0F;
			this.green = (float) (color & 255) / 255.0F;
			this.alpha = (float) (color >>> 24 & 255) / 255.0F;
			this.getX() = x;
			this.getY() = y;
			this.textColor = color;
			this.renderStringAtPos0(text, dropShadow);
		}
		return (int) this.getX();
	}

	protected void renderStringAtPos(String parString1, boolean parFlag) {
		if(parString1 == null) return;
		if(this.unicodeFlag || !decodeASCIICodepointsAndValidate(parString1)) {
			super.renderStringAtPos(parString1, parFlag);
		}else {
			renderStringAtPos0(parString1, false);
		}
	}

	private void renderStringAtPos0(String parString1, boolean parFlag) {
		renderEngine.bindTexture(locationFontTexture);
		InstancedFont.begin();
		
		Tesselator tessellator = Tesselator.getInstance();
		LevelRenderer worldrenderer = tessellator.getLevelRenderer();
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
		
		boolean hasStrike = false;
		
		for (int i = 0; i < parString1.length(); ++i) {
			char c0 = parString1.charAt(i);
			if (c0 == 167 && i + 1 < parString1.length()) {
				int i1 = "0123456789abcdefklmnor".indexOf(Character.toLowerCase(parString1.charAt(i + 1)));
				if (i1 < 16) {
					this.randomStyle = false;
					this.boldStyle = false;
					this.strikethroughStyle = false;
					this.underlineStyle = false;
					this.italicStyle = false;
					if (i1 < 0 || i1 > 15) {
						i1 = 15;
					}
					int j1 = this.colorCode[i1];
					this.textColor = j1 | (this.textColor & 0xFF000000);
				} else if (i1 == 16) {
					this.randomStyle = true;
				} else if (i1 == 17) {
					this.boldStyle = true;
				} else if (i1 == 18) {
					this.strikethroughStyle = true;
				} else if (i1 == 19) {
					this.underlineStyle = true;
				} else if (i1 == 20) {
					this.italicStyle = true;
				} else if (i1 == 21) {
					this.randomStyle = false;
					this.boldStyle = false;
					this.strikethroughStyle = false;
					this.underlineStyle = false;
					this.italicStyle = false;
					this.textColor = ((int) (this.alpha * 255.0f) << 24) | ((int) (this.red * 255.0f) << 16)
							| ((int) (this.green * 255.0f) << 8) | (int) (this.blue * 255.0f);
				}

				++i;
			} else {
				int j = temporaryCodepointArray[i];
				if(j > 255) continue;
				
				if (this.randomStyle && j != -1) {
					int k = this.getCharWidth(c0);
					char[] chars = Font.codepointLookup;

					char c1;
					while (true) {
						j = this.fontRandom.nextInt(chars.length);
						c1 = chars[j];
						if (k == this.getCharWidth(c1)) {
							break;
						}
					}

					c0 = c1;
				}

				float f = this.appendCharToBuffer(j, this.textColor, this.boldStyle, this.italicStyle);

				if (this.strikethroughStyle) {
					hasStrike = true;
					worldrenderer.pos((double) this.getX(), (double) (this.getY() + (float) (this.FONT_HEIGHT / 2)), 0.0D)
							.endVertex();
					worldrenderer
							.pos((double) (this.getX() + f), (double) (this.getY() + (float) (this.FONT_HEIGHT / 2)), 0.0D)
							.endVertex();
					worldrenderer.pos((double) (this.getX() + f),
							(double) (this.getY() + (float) (this.FONT_HEIGHT / 2) - 1.0F), 0.0D).endVertex();
					worldrenderer
							.pos((double) this.getX(), (double) (this.getY() + (float) (this.FONT_HEIGHT / 2) - 1.0F), 0.0D)
							.endVertex();
					worldrenderer.putColor4(this.textColor);
				}

				if (this.underlineStyle) {
					hasStrike = true;
					int l = this.underlineStyle ? -1 : 0;
					worldrenderer.pos((double) (this.getX() + (float) l),
							(double) (this.getY() + (float) this.FONT_HEIGHT), 0.0D).endVertex();
					worldrenderer.pos((double) (this.getX() + f), (double) (this.getY() + (float) this.FONT_HEIGHT), 0.0D)
							.endVertex();
					worldrenderer
							.pos((double) (this.getX() + f), (double) (this.getY() + (float) this.FONT_HEIGHT - 1.0F), 0.0D)
							.endVertex();
					worldrenderer.pos((double) (this.getX() + (float) l),
							(double) (this.getY() + (float) this.FONT_HEIGHT - 1.0F), 0.0D).endVertex();
					worldrenderer.putColor4(this.textColor);
				}

				this.getX() += (float) ((int) f);
			}
		}
		
		float texScale = 0.0625f;
		
		if(!hasStrike) {
			worldrenderer.finishDrawing();
		}
		
		if(parFlag) {
			if(hasStrike) {
				GlStateManager.color(0.25f, 0.25f, 0.25f, 1.0f);
				GlStateManager.translate(1.0f, 1.0f, 0.0f);
				GlStateManager.disableTexture2D();
				tessellator.draw();
				GlStateManager.translate(-1.0f, -1.0f, 0.0f);
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
				GlStateManager.enableTexture2D();
				InstancedFont.render(8, 8, texScale, texScale, true);
				GlStateManager.disableTexture2D();
				EaglercraftGPU.renderAgain();
				GlStateManager.enableTexture2D();
			}else {
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
				InstancedFont.render(8, 8, texScale, texScale, true);
			}
		}else {
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			if(hasStrike) {
				GlStateManager.disableTexture2D();
				tessellator.draw();
				GlStateManager.enableTexture2D();
			}
			InstancedFont.render(8, 8, texScale, texScale, false);
		}
		
		if(parFlag) {
			this.getX() += 1.0f;
		}
	}

	private float appendCharToBuffer(int parInt1, int color, boolean boldStyle, boolean italicStyle) {
		if (parInt1 == 32) {
			return 4.0f;
		}else {
			int i = parInt1 % 16;
			int j = parInt1 / 16;
			float w = this.charWidth[parInt1];
			if(boldStyle) {
				InstancedFont.appendBoldQuad((int)this.getX(), (int)this.getY(), i, j, color, italicStyle);
				++w;
			}else {
				InstancedFont.appendQuad((int)this.getX(), (int)this.getY(), i, j, color, italicStyle);
			}
			return w;
		}
	}

	private boolean decodeASCIICodepointsAndValidate(String str) {
		for(int i = 0, l = str.length(); i < l; ++i) {
			int j = FontMappingHelper.lookupChar(str.charAt(i), true);
			if(j != -1) {
				temporaryCodepointArray[i] = j;
			}else {
				return false;
			}
		}
		return true;
	}
}