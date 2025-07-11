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

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

public class TextureClockPBRImpl extends EaglerTextureAtlasSpritePBR {
	private double smoothParam1;
	private double smoothParam2;

	public TextureClockPBRImpl(String spriteName) {
		super(spriteName);
	}

	public void updateAnimationPBR() {
		if (!this.frameTextureDataPBR[0].isEmpty()) {
			Minecraft minecraft = Minecraft.getMinecraft();
			double d0 = 0.0;
			if (minecraft.theLevel != null && minecraft.player != null) {
				d0 = (double) minecraft.theLevel.getCelestialAngle(1.0f);
				if (!minecraft.theLevel.provider.isSurfaceLevel()) {
					d0 = Math.random();
				}
			}

			double d1;
			for (d1 = d0 - this.smoothParam1; d1 < -0.5; ++d1) {
				;
			}

			while (d1 >= 0.5) {
				--d1;
			}

			d1 = Mth.clamp_double(d1, -1.0, 1.0);
			this.smoothParam2 += d1 * 0.1;
			this.smoothParam2 *= 0.8;
			this.smoothParam1 += this.smoothParam2;

			int i, frameCount = this.frameTextureDataPBR[0].size();
			for (i = (int) ((this.smoothParam1 + 1.0) * frameCount) % frameCount; i < 0; i = (i + frameCount) % frameCount) {
				;
			}

			if (i != this.frameCounter) {
				this.frameCounter = i;
				currentAnimUpdater = (mapWidth, mapHeight, mapLevel) -> {
					animationCachePBR[0].copyFrameToTex2D(this.frameCounter, mapLevel, this.originX >> mapLevel,
							this.originY >> mapLevel, this.width >> mapLevel, this.height >> mapLevel, mapWidth,
							mapHeight);
				};
				if(!dontAnimateNormals || !dontAnimateMaterial) {
					currentAnimUpdaterPBR = (mapWidth, mapHeight, mapLevel) -> {
						if (!dontAnimateNormals)
							animationCachePBR[1].copyFrameToTex2D(this.frameCounter, mapLevel, this.originX,
									this.originY, this.width, this.height, mapWidth, mapHeight);
						if (!dontAnimateMaterial)
							animationCachePBR[2].copyFrameToTex2D(this.frameCounter, mapLevel, this.originX >> mapLevel,
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

		}else {
			currentAnimUpdater = null;
			currentAnimUpdaterPBR = null;
		}
	}

}