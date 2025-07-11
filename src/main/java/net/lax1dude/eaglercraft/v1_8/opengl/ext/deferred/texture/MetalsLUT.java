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

import java.io.BufferedReader;
import java.util.concurrent.Executor;
import java.util.concurrent.CompletableFuture;
import java.nio.charset.StandardCharsets;

import net.lax1dude.eaglercraft.v1_8.EagRuntime;
import net.lax1dude.eaglercraft.v1_8.internal.buffer.ByteBuffer;
import net.lax1dude.eaglercraft.v1_8.log4j.LogManager;
import net.lax1dude.eaglercraft.v1_8.log4j.Logger;
import net.lax1dude.eaglercraft.v1_8.opengl.EaglercraftGPU;
import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import static net.lax1dude.eaglercraft.v1_8.opengl.RealOpenGLEnums.*;
import static net.lax1dude.eaglercraft.v1_8.internal.PlatformOpenGL.*;

public class MetalsLUT extends SimplePreparableReloadListener<Void> {

	private static final Logger logger = LogManager.getLogger("MetalsLUT");

	private static int glTexture = -1;

	public static int getGLTexture() {
		if(glTexture == -1) {
			float[] lut = new float[128];
			for(int i = 0, j; i < 16; ++i) {
				j = i << 3;
				lut[i] = 1.0f;
				lut[i + 1] = 1.0f;
				lut[i + 2] = 1.0f;
				lut[i + 3] = 0.0f;
				lut[i + 4] = 1.0f;
				lut[i + 5] = 1.0f;
				lut[i + 6] = 1.0f;
				lut[i + 7] = 0.0f;
			}
			try (Resource metalsCsv = Minecraft.getInstance().getResourceManager()
					.getResource(new ResourceLocation("eagler:glsl/deferred/metals.csv"));
				BufferedReader reader = new BufferedReader(
					new InputStreamReader(metalsCsv.open(), StandardCharsets.UTF_8))) {
				String line;
				int cnt = 0;
				boolean firstLine = true;
				while((line = reader.readLine()) != null) {
					if((line = line.trim()).length() > 0) {
						if(firstLine) {
							firstLine = false;
							continue;
						}
						String[] split = line.split(",");
						if(split.length == 8) {
							try {
								int id = Integer.parseInt(split[1]);
								float nr = Float.parseFloat(split[2]);
								float ng = Float.parseFloat(split[3]);
								float nb = Float.parseFloat(split[4]);
								float kr = Float.parseFloat(split[5]);
								float kg = Float.parseFloat(split[6]);
								float kb = Float.parseFloat(split[7]);
								if(id < 230 || id > 245) {
									logger.error("Error, only metal IDs 230 to 245 are configurable!");
								}else {
									int i = (id - 230) << 3;
									lut[i] = nr;
									lut[i + 1] = ng;
									lut[i + 2] = nb;
									lut[i + 4] = kr;
									lut[i + 5] = kg;
									lut[i + 6] = kb;
								}
								++cnt;
								continue;
							}catch(NumberFormatException ex) {
							}
						}
						logger.error("Skipping bad metal constant entry: {}", line);
					}
				}
				logger.info("Loaded {} metal definitions", cnt);
			} catch (IOException e) {
				logger.error("Failed to load PBR metal lookup table!");
				logger.error(e);
			}
			if(EaglercraftGPU.checkHDRFramebufferSupport(16)) {
				ByteBuffer pixels = EagRuntime.allocateByteBuffer(256);
				for(int i = 0; i < 128; ++i) {
					pixels.putShort((short)IEEE754.encodeHalfFloat(lut[i]));
				}
				pixels.flip();
				glTexture = GlStateManager.generateTexture();
				GlStateManager.bindTexture(glTexture);
				setupFiltering();
				EaglercraftGPU.createFramebufferHDR16FTexture(GL_TEXTURE_2D, 0, 2, 16, GL_RGBA, pixels);
				EagRuntime.freeByteBuffer(pixels);
			}else if(EaglercraftGPU.checkHDRFramebufferSupport(32)) {
				logger.warn("16-bit HDR textures are not supported, using 32-bit fallback format");
				ByteBuffer pixels = EagRuntime.allocateByteBuffer(512);
				for(int i = 0; i < 128; ++i) {
					pixels.putFloat(lut[i]);
				}
				pixels.flip();
				glTexture = GlStateManager.generateTexture();
				GlStateManager.bindTexture(glTexture);
				setupFiltering();
				EaglercraftGPU.createFramebufferHDR32FTexture(GL_TEXTURE_2D, 0, 2, 16, GL_RGBA, pixels);
				EagRuntime.freeByteBuffer(pixels);
			}else {
				throw new UnsupportedOperationException("HDR textures are unavailable, could not create PBR metal definition LUT!");
			}
		}
		return glTexture;
	}

	@Override
	protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
		if(glTexture != -1) {
			EaglercraftGPU.deleteTextures(new int[] { glTexture });
			glTexture = -1;
		}
		return null;
	}

	@Override
	protected void apply(Void prepared, ResourceManager resourceManager, ProfilerFiller profiler) {
		// Texture will be recreated on next getGLTexture() call
	}

	// For backward compatibility
	public void onResourceManagerReload(ResourceManager var1) {
		this.prepare(var1, new net.minecraft.util.profiling.metrics.MetricsRecorder().getProfiler());
	}

	private static void setupFiltering() {
		_wglTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		_wglTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		_wglTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		_wglTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
	}
}