package net.lax1dude.eaglercraft.v1_8.minecraft;

import net.lax1dude.eaglercraft.v1_8.EagRuntime;
import net.lax1dude.eaglercraft.v1_8.log4j.LogManager;
import net.lax1dude.eaglercraft.v1_8.log4j.Logger;
import net.lax1dude.eaglercraft.v1_8.opengl.LevelRenderer;
import net.lax1dude.eaglercraft.v1_8.opengl.LevelVertexBufferUploader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
//import net.minecraft.client.renderer.chunk.SectionRenderDispatcher.RenderSection;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
//import net.minecraft.client.renderer.chunk.RenderSectionRenderRegion;
import net.minecraft.world.entity.Entity;
import java.util.LinkedList;
import java.util.List;

// EnumLevelBlockLayer wurde zu RenderType oder RenderLayer in neueren Versionen
// ...existing code...
public class ChunkUpdateManager {

	private static final Logger LOGGER = LogManager.getLogger();

	private final Object renderCache;

	private int chunkUpdatesTotal = 0;
	private int chunkUpdatesTotalLast = 0;
	private int chunkUpdatesTotalImmediate = 0;
	private int chunkUpdatesTotalImmediateLast = 0;
	private int chunkUpdatesQueued = 0;
	private int chunkUpdatesQueuedLast = 0;
	private long chunkUpdatesTotalLastUpdate = 0l;

	private final SectionRenderDispatcher sectionRenderDispatcher;
	private final RenderRegionCache renderRegionCache;
	private final List<SectionRenderDispatcher.RenderSection.CompileTask> queue = new LinkedList<>();

	public ChunkUpdateManager() {
		renderCache = null; // oder passende Initialisierung, falls du eine eigene Klasse hast
	}
	
	public static class EmptyBlockLayerException extends IllegalStateException {
	}
	
	private void runGenerator(SectionRenderDispatcher.RenderSection.CompileTask generator, Entity entity) {
		generator.setRenderRegionCache(renderCache);
		float f = (float) entity.getX();
		float f1 = (float) entity.getY() + entity.getEyeHeight();
		float f2 = (float) entity.getZ();
		SectionRenderDispatcher.RenderSection.CompileTask.Type chunkcompiletaskgenerator$type = generator.getType();
		generator.setStatus(SectionRenderDispatcher.RenderSection.CompileTask.Status.COMPILING);
		if (chunkcompiletaskgenerator$type == SectionRenderDispatcher.RenderSection.CompileTask.Type.REBUILD_CHUNK) {
			generator.getSectionRenderDispatcher.RenderSection().rebuildChunk(f, f1, f2, generator);
		} else if (chunkcompiletaskgenerator$type == SectionRenderDispatcher.RenderSection.CompileTask.Type.RESORT_TRANSPARENCY) {
			SectionRenderDispatcher.RenderSection r = generator.getSectionRenderDispatcher.RenderSection();
			try {
				r.resortTransparency(f, f1, f2, generator);
				CompiledChunk ch = generator.getCompiledChunk();
				if(ch.isLayerEmpty(RenderType.translucent()) && ch.isLayerEmpty(RenderType.waterMask())) {
					throw new EmptyBlockLayerException();
				}
			}catch(EmptyBlockLayerException ex) {
				LOGGER.error("SectionRenderDispatcher.RenderSection {} tried to update it's TRANSLUCENT layer with no proper initialization", r.blockPosition());
				generator.setStatus(SectionRenderDispatcher.RenderSection.CompileTask.Status.DONE);
				return; // rip
			}
		}

		generator.setStatus(SectionRenderDispatcher.RenderSection.CompileTask.Status.UPLOADING);

		final CompiledChunk compiledchunk = generator.getCompiledChunk();
		if (chunkcompiletaskgenerator$type == SectionRenderDispatcher.RenderSection.CompileTask.Type.REBUILD_CHUNK) {
			RenderType[] layers = new RenderType[] {
				RenderType.solid(),
				RenderType.cutout(),
				RenderType.translucent(),
				RenderType.waterMask()
			};
			for (RenderType layer : layers) {
				if (!compiledchunk.isLayerEmpty(layer)) {
					this.uploadChunk(layer,
							generator.getRenderRegionCache().getLevelRendererByLayer(layer),
							generator.getSectionRenderDispatcher.RenderSection(), compiledchunk);
					generator.setStatus(SectionRenderDispatcher.RenderSection.CompileTask.Status.DONE);
				}
			}
		} else if (chunkcompiletaskgenerator$type == SectionRenderDispatcher.RenderSection.CompileTask.Type.RESORT_TRANSPARENCY) {
			if(!compiledchunk.isLayerEmpty(RenderType.translucent())) {
				this.uploadChunk(RenderType.translucent(), generator.getRenderRegionCache()
								.getLevelRendererByLayer(RenderType.translucent()),
						generator.getSectionRenderDispatcher.RenderSection(), compiledchunk);
			}
			if(!compiledchunk.isLayerEmpty(RenderType.waterMask())) {
				this.uploadChunk(RenderType.waterMask(), generator.getRenderRegionCache()
								.getLevelRendererByLayer(RenderType.waterMask()),
						generator.getSectionRenderDispatcher.RenderSection(), compiledchunk);
			}
			generator.setStatus(SectionRenderDispatcher.RenderSection.CompileTask.Status.DONE);
		}
	}
	
	public boolean updateChunks(long timeout) {
		Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
		if (entity == null) {
			queue.clear();
			chunkUpdatesQueued = 0;
			return false;
		}else {
			boolean flag = false;
			long millis = EagRuntime.steadyTimeMillis();
			List<SectionRenderDispatcher.RenderSection.CompileTask> droppedUpdates = new LinkedList<>();
			while(!queue.isEmpty()) {
				SectionRenderDispatcher.RenderSection.CompileTask generator = queue.remove(0);
				
				if(!generator.canExecuteYet()) {
					if(millis - generator.goddamnFuckingTimeout < 60000l) {
						droppedUpdates.add(generator);
					}
					continue;
				}
				
				runGenerator(generator, entity);
				flag = true;
				
				++chunkUpdatesTotal;
				
				if(timeout < EagRuntime.nanoTime()) {
					break;
				}
			}
			queue.addAll(droppedUpdates);
			return flag;
		}
	}
	
	public boolean updateChunkLater(SectionRenderDispatcher.RenderSection section) {
		SectionRenderDispatcher.RenderSection.CompileTask task = section.createCompileTask(renderRegionCache);
		boolean flag = queue.size() < 100;
		if (!flag) {
			task.cancel();
		} else {
			queue.add(task);
			sectionRenderDispatcher.schedule(task);
		}
		return flag;
	}

	public boolean updateChunkNow(SectionRenderDispatcher.RenderSection section) {
		Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
		if (entity != null) {
			runGenerator(section.createCompileTask(renderRegionCache), entity);
			++chunkUpdatesTotalImmediate;
		}
		return true;
	}

	public void stopChunkUpdates() {
		queue.clear();
		chunkUpdatesQueued = 0;
	}

	public boolean updateTransparencyLater(SectionRenderDispatcher.RenderSection section) {
		if(isAlreadyQueued(section)) {
			return true;
		}
		final SectionRenderDispatcher.RenderSection.CompileTask chunkcompiletaskgenerator = section.createCompileTask(renderRegionCache);
		if (chunkcompiletaskgenerator == null) {
			return true;
		}
		chunkcompiletaskgenerator.goddamnFuckingTimeout = EagRuntime.steadyTimeMillis();
		if(queue.size() < 100) {
			chunkcompiletaskgenerator.addFinishRunnable(new Runnable() {
				@Override
				public void run() {
					if(queue.remove(chunkcompiletaskgenerator)) {
						++chunkUpdatesTotal;
					}
				}
			});
			queue.add(chunkcompiletaskgenerator);
			++chunkUpdatesQueued;
			return true;
		}else {
			return false;
		}
	}

	public void uploadChunk(RenderType layer, LevelRenderer chunkRenderer,
			SectionRenderDispatcher.RenderSection section, Object compiledSection) {
		this.uploadDisplayList(chunkRenderer,
				((ListedSectionRenderDispatcher.RenderSection) compiledSection).getDisplayList(layer, section), compiledSection);
		chunkRenderer.setTranslation(0.0D, 0.0D, 0.0D);
	}

	private void uploadDisplayList(LevelRenderer chunkRenderer, int parInt1, SectionRenderDispatcher.RenderSection section) {
		LevelVertexBufferUploader.uploadDisplayList(parInt1, chunkRenderer);
	}

	public boolean isAlreadyQueued(SectionRenderDispatcher.RenderSection section) {
		for(int i = 0, l = queue.size(); i < l; ++i) {
			if(queue.get(i).getSectionRenderDispatcher.RenderSection() == section) {
				return true;
			}
		}
		return false;
	}

	public String getDebugInfo() {
		long millis = EagRuntime.steadyTimeMillis();
		
		if(millis - chunkUpdatesTotalLastUpdate > 500l) {
			chunkUpdatesTotalLastUpdate = millis;
			chunkUpdatesTotalLast = chunkUpdatesTotal;
			chunkUpdatesTotalImmediateLast = chunkUpdatesTotalImmediate;
			chunkUpdatesTotalImmediate = 0;
			chunkUpdatesTotal = 0;
			chunkUpdatesQueuedLast = chunkUpdatesQueued;
			chunkUpdatesQueued -= chunkUpdatesTotalLast;
			if(chunkUpdatesQueued < 0) {
				chunkUpdatesQueued = 0;
			}
		}
		
		return "Uq: " + (chunkUpdatesTotalLast + chunkUpdatesTotalImmediateLast) + "/"
				+ (chunkUpdatesQueuedLast + chunkUpdatesTotalImmediateLast);
	}
	
}
