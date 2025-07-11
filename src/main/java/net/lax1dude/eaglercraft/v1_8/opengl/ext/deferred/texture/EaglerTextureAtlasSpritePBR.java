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

package net.lax1dude.eaglercraft.v1_8.opengl.ext.deferred.texture;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

import com.carrotsearch.hppc.cursors.IntCursor;
import com.google.common.collect.Lists;

import net.lax1dude.eaglercraft.v1_8.HString;
import net.lax1dude.eaglercraft.v1_8.log4j.LogManager;
import net.lax1dude.eaglercraft.v1_8.log4j.Logger;
import net.lax1dude.eaglercraft.v1_8.minecraft.EaglerTextureAtlasSprite;
import net.lax1dude.eaglercraft.v1_8.minecraft.TextureAnimationCache;
import net.lax1dude.eaglercraft.v1_8.opengl.ImageData;
// Using Eaglercraft's texture utilities instead of removed/restricted classes
import net.minecraft.client.resources.metadata.animation.AnimationFrame;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
// Crash reporting is now handled differently in 1.21.4
import net.minecraft.resources.ResourceLocation;

public class EaglerTextureAtlasSpritePBR extends EaglerTextureAtlasSprite {

	private static final Logger logger = LogManager.getLogger("EaglerTextureAtlasSpritePBR");

	protected List<int[][]>[] frameTextureDataPBR = new List[] { Lists.newArrayList(), Lists.newArrayList(), Lists.newArrayList() };
	protected TextureAnimationCache[] animationCachePBR = new TextureAnimationCache[3];

	public boolean dontAnimateNormals = true;
	public boolean dontAnimateMaterial = true;

	public static EaglerTextureAtlasSpritePBR makeAtlasSprite(ResourceLocation spriteResourceLocation) {
		String s = spriteResourceLocation.toString();
		return (EaglerTextureAtlasSpritePBR) (locationNameClock.equals(s) ? new TextureClockPBRImpl(s)
				: (locationNameCompass.equals(s) ? new TextureCompassPBRImpl(s) : new EaglerTextureAtlasSpritePBR(s)));
	}

	public EaglerTextureAtlasSpritePBR(String spriteName) {
		super(spriteName);
	}

	public void loadSpritePBR(ImageData[][] imageDatas, net.minecraft.client.resources.metadata.animation.AnimationMetadataSection meta,
			boolean dontAnimateNormals, boolean dontAnimateMaterial) {
		this.resetSprite();
		if(imageDatas.length != 3) {
			throw new IllegalArgumentException("loadSpritePBR required an array of 3 different textures (" + imageDatas.length + " given)");
		}
		this.dontAnimateNormals = dontAnimateNormals;
		this.dontAnimateMaterial = dontAnimateMaterial;
		int i = imageDatas[0][0].width;
		int j = imageDatas[0][0].height;
		this.width = i;
		this.height = j;
		int[][][] aint = new int[3][imageDatas[0].length][];

		for (int l = 0; l < imageDatas.length; ++l) {
			ImageData[] images = imageDatas[l];
			for (int k = 0; k < images.length; ++k) {
				ImageData bufferedimage = images[k];
				if (bufferedimage != null) {
					if (k > 0 && (bufferedimage.width) != i >> k || bufferedimage.height != j >> k) {
						throw new RuntimeException(
								HString.format("Unable to load miplevel: %d, image is size: %dx%d, expected %dx%d",
										new Object[] { Integer.valueOf(k), Integer.valueOf(bufferedimage.width),
												Integer.valueOf(bufferedimage.height), Integer.valueOf(i >> k),
												Integer.valueOf(j >> k) }));
					}
	
					aint[l][k] = new int[bufferedimage.width * bufferedimage.height];
					bufferedimage.getRGB(0, 0, bufferedimage.width, bufferedimage.height, aint[l][k], 0, bufferedimage.width);
				}
			}
		}

		if (meta == null) {
			if (j != i) {
				throw new RuntimeException("broken aspect ratio and not an animation");
			}

			this.frameTextureDataPBR[0].add(aint[0]);
			this.frameTextureDataPBR[1].add(aint[1]);
			this.frameTextureDataPBR[2].add(aint[2]);
		} else {
			int j1 = j / i;
			int k1 = i;
			int l = i;
			this.height = this.width;
			if (meta.getFrameCount() > 0) {
				for (IntCursor cur : meta.getFrameIndexSet()) {
					int i1 = cur.value;
					if (i1 >= j1) {
						throw new RuntimeException("invalid frameindex " + i1);
					}

					this.allocateFrameTextureData(i1);
					this.frameTextureDataPBR[0].set(i1, getFrameTextureData(aint[0], k1, l, i1));
					this.frameTextureDataPBR[1].set(i1, getFrameTextureData(aint[1], k1, l, i1));
					this.frameTextureDataPBR[2].set(i1, getFrameTextureData(aint[2], k1, l, i1));
				}

				this.animationMetadata = meta;
			} else {
				List<net.minecraft.client.resources.metadata.animation.AnimationFrame> arraylist = Lists.newArrayList();

				for (int l1 = 0; l1 < j1; ++l1) {
					this.frameTextureDataPBR[0].add(getFrameTextureData(aint[0], k1, l, l1));
					this.frameTextureDataPBR[1].add(getFrameTextureData(aint[1], k1, l, l1));
					this.frameTextureDataPBR[2].add(getFrameTextureData(aint[2], k1, l, l1));
					arraylist.add(new net.minecraft.client.resources.metadata.animation.AnimationFrame(l1, -1));
				}

				this.animationMetadata = new net.minecraft.client.resources.metadata.animation.AnimationMetadataSection(arraylist, this.width, this.height,
						meta.getFrameTime(), meta.isInterpolate());
			}
		}
	}

	public int[][][] getFramePBRTextureData(int index) {
		return new int[][][] { frameTextureDataPBR[0].get(index),
				frameTextureDataPBR[1].get(index),
				frameTextureDataPBR[2].get(index) };
	}

	public int[][] getFrameTextureData(int index) {
		return frameTextureDataPBR[0].get(index);
	}

	public int getFrameCount() {
		return frameTextureDataPBR[0].size();
	}

	public void setFramesTextureDataPBR(List<int[][]>[] newFramesTextureData) {
		frameTextureDataPBR = newFramesTextureData;
	}

	protected void allocateFrameTextureData(int index) {
		for(int j = 0; j < 3; ++j) {
			if (this.frameTextureDataPBR[j].size() <= index) {
				for (int i = this.frameTextureDataPBR[j].size(); i <= index; ++i) {
					this.frameTextureDataPBR[j].add((int[][]) null);
				}
			}
		}
	}

	public void generateMipmaps(int level) {
		List[] arraylist = new List[] { Lists.newArrayList(), Lists.newArrayList(), Lists.newArrayList() };

		for(int j = 0; j < 3; ++j) {
			for (int i = 0; i < this.frameTextureDataPBR[j].size(); ++i) {
				final int[][] aint = (int[][]) this.frameTextureDataPBR[j].get(i);
				if (aint != null) {
					try {
						if(j == 0) {
							arraylist[j].add(net.lax1dude.eaglercraft.v1_8.opengl.ImageData.mipmap(aint, level, this.width));
						}else {
							arraylist[j].add(PBRTextureMapUtils.generateMipmapDataIgnoreAlpha(level, this.width, aint));
						}
					} catch (Throwable throwable) {
						net.minecraft.CrashReport crashreport = net.minecraft.CrashReport.forThrowable(throwable, "Generating mipmaps for frame (pbr)");
						net.minecraft.CrashReportCategory crashreportcategory = crashreport.addCategory("Frame being iterated");
						crashreportcategory.addCrashSection("PBR Layer", Integer.valueOf(j));
						crashreportcategory.addCrashSection("Frame index", Integer.valueOf(i));
						crashreportcategory.addCrashSectionCallable("Frame sizes", new Callable<String>() {
							public String call() throws Exception {
								StringBuilder stringbuilder = new StringBuilder();
	
								for (int k = 0; k < aint.length; ++k) {
									if (stringbuilder.length() > 0) {
										stringbuilder.append(", ");
									}
	
									int[] aint1 = aint[k];
									stringbuilder.append(aint1 == null ? "null" : Integer.valueOf(aint1.length));
								}
	
								return stringbuilder.toString();
							}
						});
						throw new RuntimeException(crashreport.getFriendlyReport());
					}
				}
			}
		}

		this.setFramesTextureDataPBR(arraylist);
		this.bakeAnimationCache();
	}

	public void bakeAnimationCache() {
		if(animationMetadata != null) {
			for(int i = 0; i < 3; ++i) {
				if(dontAnimateNormals && i == 1) continue;
				if(dontAnimateMaterial && i == 2) continue;
				int mipLevels = frameTextureDataPBR[i].get(0).length;
				if(animationCachePBR[i] == null) {
					animationCachePBR[i] = new TextureAnimationCache(width, height, mipLevels);
				}
				animationCachePBR[i].initialize(frameTextureDataPBR[i]);
			}
		}
	}

	protected IAnimCopyFunction currentAnimUpdaterPBR = null;

	public void updateAnimationPBR() {
		if(animationCachePBR[0] == null || (!dontAnimateNormals && animationCachePBR[1] == null)
				|| (!dontAnimateMaterial && animationCachePBR[2] == null)) {
			throw new IllegalStateException("Animation cache for '" + this.iconName + "' was never baked!");
		}
		++this.tickCounter;
		if (this.tickCounter >= this.animationMetadata.getFrameTimeSingle(this.frameCounter)) {
			int i = this.animationMetadata.getFrameIndex(this.frameCounter);
			int j = this.animationMetadata.getFrameCount() == 0 ? this.frameTextureDataPBR[0].size()
					: this.animationMetadata.getFrameCount();
			this.frameCounter = (this.frameCounter + 1) % j;
			this.tickCounter = 0;
			int k = this.animationMetadata.getFrameIndex(this.frameCounter);
			if (i != k && k >= 0 && k < this.frameTextureDataPBR[0].size()) {
				currentAnimUpdater = (mapWidth, mapHeight, mapLevel) -> {
					animationCachePBR[0].copyFrameToTex2D(k, mapLevel, this.originX >> mapLevel,
							this.originY >> mapLevel, this.width >> mapLevel, this.height >> mapLevel, mapWidth,
							mapHeight);
				};
				if(!dontAnimateNormals || !dontAnimateMaterial) {
					currentAnimUpdaterPBR = (mapWidth, mapHeight, mapLevel) -> {
						if (!dontAnimateNormals)
							animationCachePBR[1].copyFrameToTex2D(k, mapLevel, this.originX >> mapLevel,
									this.originY >> mapLevel, this.width >> mapLevel, this.height >> mapLevel, mapWidth,
									mapHeight);
						if (!dontAnimateMaterial)
							animationCachePBR[2].copyFrameToTex2D(k, mapLevel, this.originX >> mapLevel,
									(this.originY >> mapLevel) + (mapHeight >> 1), this.width >> mapLevel,
									this.height >> mapLevel, mapWidth, mapHeight);
					};
				}else {
					currentAnimUpdaterPBR = null;
				}
			}else {
				currentAnimUpdater = null;
				currentAnimUpdaterPBR = null;
			}
		} else if (this.animationMetadata.isInterpolate()) {
			float f = 1.0f - (float) this.tickCounter / (float) this.animationMetadata.getFrameTimeSingle(this.frameCounter);
			int i = this.animationMetadata.getFrameIndex(this.frameCounter);
			int j = this.animationMetadata.getFrameCount() == 0 ? this.frameTextureDataPBR[0].size()
					: this.animationMetadata.getFrameCount();
			int k = this.animationMetadata.getFrameIndex((this.frameCounter + 1) % j);
			if (i != k && k >= 0 && k < this.frameTextureDataPBR[0].size()) {
				currentAnimUpdater = (mapWidth, mapHeight, mapLevel) -> {
					animationCachePBR[0].copyInterpolatedFrameToTex2D(i, k, f, mapLevel, this.originX >> mapLevel,
							this.originY >> mapLevel, this.width >> mapLevel, this.height >> mapLevel, mapWidth,
							mapHeight);
				};
				if(!dontAnimateNormals || !dontAnimateMaterial) {
					currentAnimUpdaterPBR = (mapWidth, mapHeight, mapLevel) -> {
						if (!dontAnimateNormals)
							animationCachePBR[1].copyInterpolatedFrameToTex2D(i, k, f, mapLevel,
									this.originX >> mapLevel, this.originY >> mapLevel, this.width >> mapLevel,
									this.height >> mapLevel, mapWidth, mapHeight);
						if (!dontAnimateMaterial)
							animationCachePBR[2].copyInterpolatedFrameToTex2D(i, k, f, mapLevel,
									this.originX >> mapLevel, (this.originY >> mapLevel) + (mapHeight >> 1),
									this.width >> mapLevel, this.height >> mapLevel, mapWidth, mapHeight);
					};
				}else {
					currentAnimUpdaterPBR = null;
				}
			}else {
				currentAnimUpdater = null;
				currentAnimUpdaterPBR = null;
			}
		}else {
			currentAnimUpdater = null;
			currentAnimUpdaterPBR = null;
		}
	}

	public void copyAnimationFramePBR(int pass, int mapWidth, int mapHeight, int mapLevel) {
		if(pass == 0) {
			if(currentAnimUpdater != null) {
				currentAnimUpdater.updateAnimation(mapWidth, mapHeight, mapLevel);
			}
		}else {
			if(currentAnimUpdaterPBR != null) {
				currentAnimUpdaterPBR.updateAnimation(mapWidth, mapHeight, mapLevel);
			}
		}
	}

	public void clearFramesTextureData() {
		for(int i = 0; i < 3; ++i) {
			this.frameTextureDataPBR[i].clear();
			if(this.animationCachePBR[i] != null) {
				this.animationCachePBR[i].free();
				this.animationCachePBR[i] = null;
			}
		}
	}

	public void loadSprite(ImageData[] images, net.minecraft.client.resources.metadata.animation.AnimationMetadataSection meta) throws IOException {
		Throwable t = new UnsupportedOperationException("Cannot call regular loadSprite in PBR mode, use loadSpritePBR");
		try {
			throw t;
		}catch(Throwable tt) {
			logger.error(t);
		}
	}

	public void setFramesTextureData(List<int[][]> newFramesTextureData) {
		Throwable t = new UnsupportedOperationException("Cannot call regular setFramesTextureData in PBR mode, use setFramesTextureDataPBR");
		try {
			throw t;
		}catch(Throwable tt) {
			logger.error(t);
		}
	}

	public void updateAnimation() {
		Throwable t = new UnsupportedOperationException("Cannot call regular updateAnimation in PBR mode, use updateAnimationPBR");
		try {
			throw t;
		}catch(Throwable tt) {
			logger.error(t);
		}
	}

	public void copyAnimationFrame(int mapWidth, int mapHeight, int mapLevel) {
		Throwable t = new UnsupportedOperationException("Cannot call regular copyAnimationFrame in PBR mode, use updateAnimationPBR");
		try {
			throw t;
		}catch(Throwable tt) {
			logger.error(t);
		}
	}

	protected void resetSprite() {
		this.animationMetadata = null;
		this.setFramesTextureDataPBR(new List[] { Lists.newArrayList(), Lists.newArrayList(), Lists.newArrayList() });
		this.frameCounter = 0;
		this.tickCounter = 0;
		for(int i = 0; i < 3; ++i) {
			if(this.animationCachePBR[i] != null) {
				this.animationCachePBR[i].free();
				this.animationCachePBR[i] = null;
			}
		}
	}

	public String toString() {
		return "EaglerTextureAtlasSpritePBR{name=\'" + this.iconName + '\'' + ", frameCount=" + this.framesTextureData.size()
				+ ", rotated=" + this.rotated + ", x=" + this.originX + ", y=" + this.originY + ", height="
				+ this.height + ", width=" + this.width + ", u0=" + this.minU + ", u1=" + this.maxU + ", v0="
				+ this.minV + ", v1=" + this.maxV + '}';
	}

}