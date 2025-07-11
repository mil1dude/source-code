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

package net.lax1dude.eaglercraft.v1_8.opengl.ext.deferred;

import static net.lax1dude.eaglercraft.v1_8.internal.PlatformOpenGL.*;
import static net.lax1dude.eaglercraft.v1_8.opengl.RealOpenGLEnums.*;
import static net.lax1dude.eaglercraft.v1_8.opengl.ext.deferred.ExtGLEnums.*;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import net.lax1dude.eaglercraft.v1_8.EagRuntime;
import net.lax1dude.eaglercraft.v1_8.internal.IVertexArrayGL;
import net.lax1dude.eaglercraft.v1_8.internal.IBufferGL;
import net.lax1dude.eaglercraft.v1_8.internal.buffer.ByteBuffer;
import net.lax1dude.eaglercraft.v1_8.opengl.EaglercraftGPU;
import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public class SkyboxRenderer {

	public final ResourceLocation skyboxLocation;

	private IBufferGL skyboxVBO = null;
	private IBufferGL skyboxIBO = null;
	private IVertexArrayGL skyboxVAO = null;
	private int normalsLUT = -1;
	private int atmosphereLUTWidth = -1;
	private int atmosphereLUTHeight = -1;

	private int skyboxIndexType = -1;
	private int skyboxIndexStride = -1;
	private int skyboxIndexCount = -1;

	private int skyboxTopIndexOffset = -1;
	private int skyboxTopIndexCount = -1;

	private int skyboxBottomIndexOffset = -1;
	private int skyboxBottomIndexCount = -1;

	public SkyboxRenderer(ResourceLocation is) {
		skyboxLocation = is;
	}

	public void load() throws IOException {
		destroy();
		try (DataInputStream is = new DataInputStream(
				Minecraft.getMinecraft().getResourceManager().getResource(skyboxLocation).getInputStream())) {
			if(is.read() != 0xEE || is.read() != 0xAA || is.read() != 0x66 || is.read() != '%') {
				throw new IOException("Bad file type for: " + skyboxLocation.toString());
			}
			byte[] bb = new byte[is.read()];
			is.read(bb);
			if(!Arrays.equals(bb, new byte[] { 's', 'k', 'y', 'b', 'o', 'x' })) {
				throw new IOException("Bad file type \"" + new String(bb, StandardCharsets.UTF_8) + "\" for: " + skyboxLocation.toString());
			}
			atmosphereLUTWidth = is.readUnsignedShort();
			atmosphereLUTHeight = is.readUnsignedShort();
			byte[] readBuffer = new byte[atmosphereLUTWidth * atmosphereLUTHeight * 4];
			is.read(readBuffer);
			
			ByteBuffer buf = EagRuntime.allocateByteBuffer(readBuffer.length);
			buf.put(readBuffer);
			buf.flip();
			
			normalsLUT = GlStateManager.generateTexture();
			GlStateManager.bindTexture(normalsLUT);
			_wglTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			_wglTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
			_wglTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
			_wglTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
			_wglTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, atmosphereLUTWidth, atmosphereLUTHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, buf);
			
			EagRuntime.freeByteBuffer(buf);

			skyboxTopIndexOffset = is.readInt();
			skyboxTopIndexCount = is.readInt();
			skyboxBottomIndexOffset = is.readInt();
			skyboxBottomIndexCount = is.readInt();
			
			int vboLength = is.readInt() * 8;
			readBuffer = new byte[vboLength];
			is.read(readBuffer);
			
			buf = EagRuntime.allocateByteBuffer(readBuffer.length);
			buf.put(readBuffer);
			buf.flip();
			
			skyboxVBO = _wglGenBuffers();
			EaglercraftGPU.bindGLArrayBuffer(skyboxVBO);
			_wglBufferData(GL_ARRAY_BUFFER, buf, GL_STATIC_DRAW);
			
			EagRuntime.freeByteBuffer(buf);
			
			int iboLength = skyboxIndexCount = is.readInt();
			int iboType = is.read();
			iboLength *= iboType;
			switch(iboType) {
			case 1:
				skyboxIndexType = GL_UNSIGNED_BYTE;
				break;
			case 2:
				skyboxIndexType = GL_UNSIGNED_SHORT;
				break;
			case 4:
				skyboxIndexType = GL_UNSIGNED_INT;
				break;
			default:
				throw new IOException("Unsupported index buffer type: " + iboType);
			}
			
			skyboxIndexStride = iboType;
			
			readBuffer = new byte[iboLength];
			is.read(readBuffer);
			
			buf = EagRuntime.allocateByteBuffer(readBuffer.length);
			buf.put(readBuffer);
			buf.flip();
			
			skyboxVAO = _wglGenVertexArrays();
			EaglercraftGPU.bindGLVertexArray(skyboxVAO);
			
			skyboxIBO = _wglGenBuffers();
			_wglBindBuffer(GL_ELEMENT_ARRAY_BUFFER, skyboxIBO);
			_wglBufferData(GL_ELEMENT_ARRAY_BUFFER, buf, GL_STATIC_DRAW);
			EagRuntime.freeByteBuffer(buf);
			
			EaglercraftGPU.bindGLArrayBuffer(skyboxVBO);
			
			_wglEnableVertexAttribArray(0);
			_wglVertexAttribPointer(0, 3, _GL_HALF_FLOAT, false, 8, 0);
			
			_wglEnableVertexAttribArray(1);
			_wglVertexAttribPointer(1, 2, GL_UNSIGNED_BYTE, true, 8, 6);
		}
	}

	public int getNormalsLUT() {
		return normalsLUT;
	}

	public int getAtmosLUTWidth() {
		return atmosphereLUTWidth;
	}

	public int getAtmosLUTHeight() {
		return atmosphereLUTHeight;
	}

	public void drawTop() {
		EaglercraftGPU.bindGLVertexArray(skyboxVAO);
		_wglDrawElements(GL_TRIANGLES, skyboxTopIndexCount, skyboxIndexType, skyboxTopIndexOffset * skyboxIndexStride);
	}

	public void drawBottom() {
		EaglercraftGPU.bindGLVertexArray(skyboxVAO);
		_wglDrawElements(GL_TRIANGLES, skyboxBottomIndexCount, skyboxIndexType, skyboxBottomIndexOffset * skyboxIndexStride);
	}

	public void drawFull() {
		EaglercraftGPU.bindGLVertexArray(skyboxVAO);
		_wglDrawElements(GL_TRIANGLES, skyboxIndexCount, skyboxIndexType, 0);
	}

	public void destroy() {
		if(skyboxVBO != null) {
			_wglDeleteBuffers(skyboxVBO);
			skyboxVBO = null;
		}
		if(skyboxIBO != null) {
			_wglDeleteBuffers(skyboxIBO);
			skyboxVBO = null;
		}
		if(skyboxVAO != null) {
			_wglDeleteVertexArrays(skyboxVAO);
			skyboxVBO = null;
		}
		if(normalsLUT != -1) {
			GlStateManager.deleteTexture(normalsLUT);
			normalsLUT = -1;
		}
	}

}