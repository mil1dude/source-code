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

package net.lax1dude.eaglercraft.v1_8.opengl.ext.dynamiclights;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.lax1dude.eaglercraft.v1_8.EagRuntime;
import net.lax1dude.eaglercraft.v1_8.opengl.EaglercraftGPU;
import net.lax1dude.eaglercraft.v1_8.opengl.FixedFunctionPipeline;
import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.lax1dude.eaglercraft.v1_8.vector.Matrix4f;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.util.Mth;

public class DynamicLightsStateManager {

	static final DynamicLightsPipelineCompiler deferredExtPipeline = new DynamicLightsPipelineCompiler();
	private static List<DynamicLightInstance> lightInstancePool = new ArrayList<>();
	private static int instancePoolIndex = 0;
	private static int maxListLengthTracker = 0;
	static final List<DynamicLightInstance> lightRenderList = new LinkedList<>();
	static final Matrix4f inverseViewMatrix = new Matrix4f();
	static int inverseViewMatrixSerial = 0;
	static DynamicLightBucketLoader bucketLoader = null;
	static DynamicLightsAcceleratedEffectRenderer accelParticleRenderer = null;
	static int lastTotal = 0;
	private static long lastTick = 0l;

	public static void enableDynamicLightsRender() {
		if(bucketLoader == null) {
			bucketLoader = new DynamicLightBucketLoader();
			bucketLoader.initialize();
			bucketLoader.bindLightSourceBucket(-999, -999, -999, 0);
			FixedFunctionPipeline.loadExtensionPipeline(deferredExtPipeline);
		}
		if(accelParticleRenderer == null) {
			accelParticleRenderer = new DynamicLightsAcceleratedEffectRenderer();
			accelParticleRenderer.initialize();
		}
		lightRenderList.clear();
		instancePoolIndex = 0;
		maxListLengthTracker = 0;
	}

	public static void bindAcceleratedEffectRenderer(ParticleEngine renderer) {
		renderer.acceleratedParticleRenderer = accelParticleRenderer;
	}

	public static void disableDynamicLightsRender(boolean unloadPipeline) {
		if(bucketLoader != null) {
			bucketLoader.destroy();
			bucketLoader = null;
			if(unloadPipeline) {
				FixedFunctionPipeline.loadExtensionPipeline(null);
			}
		}
		if(accelParticleRenderer != null) {
			accelParticleRenderer.destroy();
			accelParticleRenderer = null;
		}
		destroyAll();
		lightRenderList.clear();
		instancePoolIndex = 0;
		maxListLengthTracker = 0;
	}

	public static boolean isDynamicLightsRender() {
		return bucketLoader != null;
	}

	public static boolean isInDynamicLightsPass() {
		return GlStateManager.isExtensionPipeline() && bucketLoader != null;
	}

	public static void reportForwardRenderObjectPosition(int centerX, int centerY, int centerZ) {
		if(bucketLoader != null) {
			bucketLoader.bindLightSourceBucket(centerX, centerY, centerZ, 0);
		}
	}

	public static void reportForwardRenderObjectPosition2(float x, float y, float z) {
		if(bucketLoader != null) {
			float posX = (float)((x + BlockEntityRenderDispatcher.camera.getPosition().x) - (Mth.floor(BlockEntityRenderDispatcher.camera.getPosition().x / 16.0) << 4));
			float posY = (float)((y + BlockEntityRenderDispatcher.camera.getPosition().y) - (Mth.floor(BlockEntityRenderDispatcher.camera.getPosition().y / 16.0) << 4));
			float posZ = (float)((z + BlockEntityRenderDispatcher.camera.getPosition().z) - (Mth.floor(BlockEntityRenderDispatcher.camera.getPosition().z / 16.0) << 4));
			bucketLoader.bindLightSourceBucket((int)posX, (int)posY, (int)posZ, 0);
		}
	}

	public static void renderDynamicLight(String lightName, double posX, double posY, double posZ, float radius) {
		if(bucketLoader != null) {
			DynamicLightInstance dl;
			if(instancePoolIndex < lightInstancePool.size()) {
				dl = lightInstancePool.get(instancePoolIndex);
			}else {
				lightInstancePool.add(dl = new DynamicLightInstance());
			}
			++instancePoolIndex;
			dl.updateLight(posX, posY, posZ, radius);
			lightRenderList.add(dl);
		}
	}

	public static void clearRenderList() {
		if(instancePoolIndex > maxListLengthTracker) {
			maxListLengthTracker = instancePoolIndex;
		}
		lightRenderList.clear();
		instancePoolIndex = 0;
	}

	public static void commitLightSourceBuckets(double renderPosX, double renderPosY, double renderPosZ) {
		lastTotal = lightRenderList.size();
		if(bucketLoader != null) {
			bucketLoader.clearBuckets();
			int entityChunkOriginX = Mth.floor_double(renderPosX / 16.0) << 4;
			int entityChunkOriginY = Mth.floor_double(renderPosY / 16.0) << 4;
			int entityChunkOriginZ = Mth.floor_double(renderPosZ / 16.0) << 4;
			Iterator<DynamicLightInstance> itr = lightRenderList.iterator();
			while(itr.hasNext()) {
				DynamicLightInstance dl = itr.next();
				float lightChunkPosX = (float)(dl.getX() - entityChunkOriginX);
				float lightChunkPosY = (float)(dl.getY() - entityChunkOriginY);
				float lightChunkPosZ = (float)(dl.getZ() - entityChunkOriginZ);
				bucketLoader.bucketLightSource(lightChunkPosX, lightChunkPosY, lightChunkPosZ, dl);
			}
			bucketLoader.setRenderPos(renderPosX, renderPosY, renderPosZ);
			bucketLoader.truncateOverflowingBuffers();
		}
		updateTimers();
		clearRenderList();
	}

	public static void setupInverseViewMatrix() {
		Matrix4f.invert(GlStateManager.getModelViewReference(), inverseViewMatrix);
		inverseViewMatrixSerial = GlStateManager.getModelViewSerial();
	}

	private static void updateTimers() {
		long millis = EagRuntime.steadyTimeMillis();
		if(millis - lastTick > 5000l) {
			lastTick = millis;
			if(maxListLengthTracker < (lightInstancePool.size() >> 1)) {
				List<DynamicLightInstance> newPool = new ArrayList<>(Math.max(maxListLengthTracker, 16));
				for(int i = 0; i < maxListLengthTracker; ++i) {
					newPool.add(lightInstancePool.get(i));
				}
				lightInstancePool = newPool;
			}
			maxListLengthTracker = 0;
		}
	}

	public static void destroyAll() {
		lightInstancePool = new ArrayList<>();
	}

	public static String getF3String() {
		return "DynamicLightsTotal: " + lastTotal;
	}

	public static boolean isSupported() {
		return EaglercraftGPU.checkOpenGLESVersion() >= 300;
	}

}