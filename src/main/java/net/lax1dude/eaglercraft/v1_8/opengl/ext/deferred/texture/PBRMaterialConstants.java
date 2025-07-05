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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import net.lax1dude.eaglercraft.v1_8.log4j.LogManager;
import net.lax1dude.eaglercraft.v1_8.log4j.Logger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;

public class PBRMaterialConstants implements PreparableReloadListener {

	public static final Logger logger = LogManager.getLogger("PBRMaterialConstants");

	public final ResourceLocation resourceLocation;
	public final Map<String,Integer> spriteNameToMaterialConstants = new HashMap<>();

	public int defaultMaterial = 0xFF000A77;

	public PBRMaterialConstants(ResourceLocation resourceLocation) {
		this.resourceLocation = resourceLocation;
	}

	@Override
	public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager var1,
			ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor,
			Executor gameExecutor) {
		return var1.getResource(resourceLocation).thenCompose(resource -> CompletableFuture.supplyAsync(() -> {
			spriteNameToMaterialConstants.clear();
			try (InputStream is = resource.open()) {
				BufferedReader bf = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
				String line;
				while((line = bf.readLine()) != null) {
					line = line.trim();
					if(line.isEmpty() || line.startsWith("#")) {
						continue;
					}
					String[] split = line.split(",");
					if(split.length == 2) {
						try {
							spriteNameToMaterialConstants.put(split[0].trim(), Integer.parseUnsignedInt(split[1].trim().substring(2), 16));
						}catch(NumberFormatException ex) {
							logger.error("Could not parse material constant: {}", line);
						}
						continue;
					}
					if(split.length == 3 && split[0].isEmpty()) {
						try {
							defaultMaterial = Integer.parseUnsignedInt(split[2].trim().substring(2), 16);
						}catch(NumberFormatException ex) {
							logger.error("Could not parse default material constant: {}", line);
						}
						continue;
					}
					logger.error("Skipping bad material constant entry: {}", line);
				}
			} catch (IOException e) {
				logger.error("Could not parse material constants from: {}", resourceLocation, e);
			}
			return Unit.INSTANCE;
		}, backgroundExecutor)).thenCompose(preparationBarrier::wait).thenAcceptAsync((unit) -> {
			// Reload complete
		}, gameExecutor);
	}

	// For backward compatibility
	public void onResourceManagerReload(ResourceManager var1) {
		reload(null, var1, null, null, Runnable::run, Runnable::run);
	}
}