/*
 * Copyright (c) 2025 lax1dude. All Rights Reserved.
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

package net.lax1dude.eaglercraft.v1_8.sp.server;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

public class GenLayerEaglerRivers {
    private final long worldGenSeed;
    private final Object parent;
    private final Holder<Biome> riverBiome;

	private static final int[] pattern = new int[] {
			0b111000011100001110000111,
			0b111000111110011111000111,
			0b011100011100001110001110,
			0b011100000000000000001110,
			0b001110000000000000011100,
			0b001110000000000000011100,
			0b000111000000000000111000,
			0b000111000000000000111000,
			0b000011100000000001110000,
			0b000011100000000001110000,
			0b000001110000000011100000,
			0b000001110000000011100000,
			0b000000111000000111000000,
			0b000000111000000111000000,
			0b000000011100001110000000,
			0b000000011100001110000000,
			0b000000001110011100000000,
			0b000000001110011100000000,
			0b000000000111111000000000,
			0b000000000111111000000000,
			0b000000000011110000000000,
			0b000000000011110000000000,
			0b000000000001100000000000,
			0b000000000001100000000000,
	};

	private static final int patternSize = 24;

    public GenLayerEaglerRivers(long seed, Object parent) {
        this.worldGenSeed = seed;
        this.parent = parent;
        // Use the river biome directly
        this.riverBiome = Holder.direct(Biomes.RIVER);
    }

    public int[] getInts(int x, int y, int w, int h) {
        // Get the parent layer's biome data
        int[] aint;
        try {
            aint = (int[]) parent.getClass().getMethod("getInts", int.class, int.class, int.class, int.class)
                .invoke(parent, x, y, w, h);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get parent layer data", e);
        }
        
        int[] aint1 = new int[w * h];

		for (int yy = 0; yy < h; ++yy) {
			for (int xx = 0; xx < w; ++xx) {
				int i = xx + yy * w;
				int biomeId = aint[i];
				long a = this.worldGenSeed * 6364136223846793005L + 1442695040888963407L;
				long b = ((a & 112104L) == 0) ? (((a & 534L) == 0) ? 1L : 15L) : 746L;
				
				long xxx = (long)(x + xx) & 0xFFFFFFFFL;
				long yyy = (long)(y + yy) & 0xFFFFFFFFL;
				long hash = a + (xxx / patternSize);
				hash *= hash * 6364136223846793005L + 1442695040888963407L;
				hash += (yyy / patternSize);
				hash *= hash * 6364136223846793005L + 1442695040888963407L;
				hash += a;
				
				if ((hash & b) == 0L) {
					xxx %= (long)patternSize;
					yyy %= (long)patternSize;
					long tmp;
					switch((int)((hash >>> 16L) & 3L)) {
					case 1:
						tmp = xxx;
						xxx = yyy;
						yyy = (long)patternSize - tmp - 1L;
						break;
					case 2:
						tmp = xxx;
						xxx = (long)patternSize - yyy - 1L;
						yyy = tmp;
						break;
					case 3:
						tmp = xxx;
						xxx = (long)patternSize - yyy - 1L;
						yyy = (long)patternSize - tmp - 1L;
						break;
					}
					if((pattern[(int)yyy] & (1 << (int)xxx)) != 0) {
						// Use the river biome ID
						biomeId = this.riverBiome.unwrapKey().get().location().hashCode();
					}
				}
				aint1[i] = biomeId;
			}
		}

        return aint1;
    }

    // Helper method to get the biome ID
    private int getBiomeId(Holder<Biome> biome) {
        return biome.unwrapKey().get().location().hashCode();
    }

}