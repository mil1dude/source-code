/*
 * Copyright (c) 2022-2025 lax1dude. All Rights Reserved.
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

package net.lax1dude.eaglercraft.v1_8.opengl;

import net.lax1dude.eaglercraft.v1_8.internal.buffer.ByteBuffer;

public class LevelVertexBufferUploader {

	public static void func_181679_a(LevelRenderer parLevelRenderer) {
		int cunt = parLevelRenderer.getVertexCount();
		if (cunt > 0) {
			VertexFormat fmt = parLevelRenderer.getVertexFormat();
			ByteBuffer buf = parLevelRenderer.getByteBuffer();
			buf.position(0).limit(cunt * fmt.attribStride);
			EaglercraftGPU.renderBuffer(buf, fmt.eaglercraftAttribBits,
					parLevelRenderer.getDrawMode(), cunt);
			parLevelRenderer.reset();
		}
	}

	public static void uploadDisplayList(int displayList, LevelRenderer worldRenderer) {
		int cunt = worldRenderer.getVertexCount();
		if (cunt > 0) {
			VertexFormat fmt = worldRenderer.getVertexFormat();
			ByteBuffer buf = worldRenderer.getByteBuffer();
			buf.position(0).limit(cunt * fmt.attribStride);
			EaglercraftGPU.uploadListDirect(displayList, buf, fmt.eaglercraftAttribBits, worldRenderer.getDrawMode(), cunt);
			worldRenderer.reset();
		}else {
			EaglercraftGPU.flushDisplayList(displayList);
		}
	}

}