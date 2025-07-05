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
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import net.lax1dude.eaglercraft.v1_8.log4j.LogManager;
import net.lax1dude.eaglercraft.v1_8.log4j.Logger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class EmissiveItems extends SimplePreparableReloadListener<Void> {

    private static final Logger logger = LogManager.getLogger("EmissiveItemsCSV");
    private static final Map<String, float[]> entries = new HashMap<>();

    public static float[] getItemEmission(ItemStack itemStack) {
        return getItemEmission(itemStack.getItem(), itemStack.getDamageValue());
    }

    public static float[] getItemEmission(Item item, int damage) {
        return entries.get(Item.BY_NAME.getKey(item).toString() + "#" + damage);
    }

    @Override
    protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        entries.clear();
        try (Resource resource = resourceManager.getResource(new ResourceLocation("eagler:glsl/deferred/emissive_items.csv"))) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.open(), StandardCharsets.UTF_8))) {
                
                String line;
                boolean firstLine = true;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.length() > 0) {
                        if (firstLine) {
                            firstLine = false;
                            continue;
                        }
                        
                        String[] split = line.split(",");
                        if (split.length == 6) {
                            try {
                                int dmg = Integer.parseInt(split[1]);
                                float r = Float.parseFloat(split[2]);
                                float g = Float.parseFloat(split[3]);
                                float b = Float.parseFloat(split[4]);
                                float i = Float.parseFloat(split[5]);
                                r *= i;
                                g *= i;
                                b *= i;
                                entries.put(split[0] + "#" + dmg, new float[] { r, g, b });
                            } catch (NumberFormatException ex) {
                                logger.error("Invalid number format in emissive_items.csv: " + line);
                            }
                        } else {
                            logger.error("Invalid line format in emissive_items.csv: " + line);
                        }
                    }
                }
            }
        } catch (IOException ex) {
            logger.error("Could not load emissive_items.csv");
            logger.error(ex);
        }
        return null;
    }

    @Override
    protected void apply(Void prepared, ResourceManager resourceManager, ProfilerFiller profiler) {
        // No additional processing needed after loading
    }

    // For backward compatibility
    public void onResourceManagerReload(ResourceManager var1) {
        var1.reloadExecutor().execute(() -> {
            var profiler = new net.minecraft.util.profiling.metrics.MetricsRecorder();
            profiler.startTick();
            try {
                this.prepare(var1, profiler.getProfiler());
                this.apply(null, var1, profiler.getProfiler());
            } finally {
                profiler.endTick();
            }
        });
    }
}