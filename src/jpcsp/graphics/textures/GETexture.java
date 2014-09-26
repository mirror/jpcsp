/*
This file is part of jpcsp.

Jpcsp is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Jpcsp is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Jpcsp.  If not, see <http://www.gnu.org/licenses/>.
 */
package jpcsp.graphics.textures;

import static jpcsp.graphics.GeCommands.TFLT_NEAREST;
import static jpcsp.graphics.GeCommands.TWRAP_WRAP_MODE_CLAMP;
import static jpcsp.graphics.VideoEngine.SIZEOF_FLOAT;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.apache.log4j.Logger;

import jpcsp.Memory;
import jpcsp.HLE.Modules;
import jpcsp.HLE.modules.sceDisplay;
import jpcsp.graphics.GeCommands;
import jpcsp.graphics.VideoEngine;
import jpcsp.graphics.RE.IRenderingEngine;
import jpcsp.graphics.RE.buffer.IREBufferManager;
import jpcsp.util.Utilities;

/**
 * @author gid15
 *
 */
public class GETexture {
	protected static Logger log = VideoEngine.log;
	protected int address;
	protected int length;
	protected int bufferWidth;
	protected int width;
	protected int height;
	protected int widthPow2;
	protected int heightPow2;
	protected int pixelFormat;
	protected int bytesPerPixel;
	protected int textureId = -1;
	protected int drawBufferId = -1;
	protected float texS;
	protected float texT;
	private boolean changed;
	protected int bufferLength;
	protected Buffer buffer;
	protected boolean useViewportResize;
	protected float resizeScale;
	// For copying Stencil to texture Alpha
	private int stencilFboId = -1;
	private int stencilTextureId = -1;
	private static final int stencilPixelFormat = IRenderingEngine.RE_DEPTH_STENCIL;

	public GETexture(int address, int bufferWidth, int width, int height, int pixelFormat, boolean useViewportResize) {
		this.address = address;
		this.bufferWidth = bufferWidth;
		this.width = width;
		this.height = height;
		this.pixelFormat = pixelFormat;
		bytesPerPixel = sceDisplay.getPixelFormatBytes(pixelFormat);
		length = bufferWidth * height * bytesPerPixel;
		widthPow2 = Utilities.makePow2(width);
		heightPow2 = Utilities.makePow2(height);
		this.useViewportResize = useViewportResize;
		changed = true;
		resizeScale = getViewportResizeScaleFactor();
	}

	private int getTextureBufferLength() {
		return getTexImageWidth() * getTexImageHeight() * bytesPerPixel;
	}

	private float getViewportResizeScaleFactor() {
		if (!useViewportResize) {
			return 1;
		}

		return Modules.sceDisplayModule.getViewportResizeScaleFactor();
	}

	public void bind(IRenderingEngine re, boolean forDrawing) {
		float viewportResizeScaleFactor = getViewportResizeScaleFactor();
		// Create the texture if not yet created or
		// re-create it if the viewport resize factor has been changed dynamically.
		if (textureId == -1 || viewportResizeScaleFactor != resizeScale) {
			// The Jpcsp window has been resized. Recreate all the textures using the new size.
			if (textureId != -1) {
				re.deleteTexture(textureId);
				textureId = -1;
			}
			if (stencilTextureId != -1) {
				re.deleteTexture(stencilTextureId);
				stencilTextureId = -1;
			}
			if (stencilFboId != -1) {
				re.deleteFramebuffer(stencilFboId);
				stencilFboId = -1;
			}

			resizeScale = viewportResizeScaleFactor;

			if (useViewportResize) {
				texS = sceDisplay.getResizedWidth(width) / (float) getTexImageWidth();
				texT = sceDisplay.getResizedHeight(height) / (float) getTexImageHeight();
			} else {
				texS = width / (float) bufferWidth;
				texT = height / (float) heightPow2;
			}

			textureId = re.genTexture();
			re.bindTexture(textureId);
    		re.setTexImage(0, pixelFormat, getTexImageWidth(), getTexImageHeight(), pixelFormat, pixelFormat, 0, null);
            re.setTextureMipmapMinFilter(TFLT_NEAREST);
            re.setTextureMipmapMagFilter(TFLT_NEAREST);
            re.setTextureMipmapMinLevel(0);
            re.setTextureMipmapMaxLevel(0);
            re.setTextureWrapMode(TWRAP_WRAP_MODE_CLAMP, TWRAP_WRAP_MODE_CLAMP);
            if (drawBufferId == -1) {
            	drawBufferId = re.getBufferManager().genBuffer(IRenderingEngine.RE_ARRAY_BUFFER, IRenderingEngine.RE_FLOAT, 16, IRenderingEngine.RE_DYNAMIC_DRAW);
            }
		} else {
			re.bindTexture(textureId);
		}

		if (forDrawing) {
			re.setTextureFormat(pixelFormat, false);
		}
	}

	public int getBufferWidth() {
		return bufferWidth;
	}

	public int getTexImageWidth() {
		return useViewportResize ? sceDisplay.getResizedWidthPow2(bufferWidth) : bufferWidth;
	}

	public int getTexImageHeight() {
		return useViewportResize ? sceDisplay.getResizedHeightPow2(heightPow2) : heightPow2;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getResizedWidth() {
		return useViewportResize ? sceDisplay.getResizedWidth(width) : width;
	}

	public int getResizedHeight() {
		return useViewportResize ? sceDisplay.getResizedHeight(height) : height;
	}

	public int getWidthPow2() {
		return widthPow2;
	}

	public int getHeightPow2() {
		return heightPow2;
	}

	public int getPixelFormat() {
		return pixelFormat;
	}

	public void copyScreenToTexture(IRenderingEngine re) {
		if (log.isDebugEnabled()) {
			log.debug(String.format("GETexture.copyScreenToTexture %s", toString()));
		}

		bind(re, false);

		int texWidth = Math.min(bufferWidth, width);
		int texHeight = height;
		if (useViewportResize) {
			texWidth = sceDisplay.getResizedWidth(texWidth);
			texHeight = sceDisplay.getResizedHeight(texHeight);
		}
		re.copyTexSubImage(0, 0, 0, 0, 0, texWidth, texHeight);

		if (Modules.sceDisplayModule.isSaveStencilToMemory()) {
			if (!copyStencilToTextureAlpha(re, texWidth, texHeight)) {
				Modules.sceDisplayModule.setSaveStencilToMemory(false);
			}
		}

		setChanged(true);
	}

	public void copyTextureToScreen(IRenderingEngine re) {
		copyTextureToScreen(re, 0, 0, width, height, true, true, true, true, true);
	}

	protected void copyTextureToScreen(IRenderingEngine re, int x, int y, int projectionWidth, int projectionHeight, boolean scaleToCanvas, boolean redWriteEnabled, boolean greenWriteEnabled, boolean blueWriteEnabled, boolean alphaWriteEnabled) {
		if (log.isDebugEnabled()) {
			log.debug(String.format("GETexture.copyTextureToScreen %s at %dx%d", toString(), x, y));
		}

		bind(re, true);

		drawTexture(re, x, y, projectionWidth, projectionHeight, scaleToCanvas, redWriteEnabled, greenWriteEnabled, blueWriteEnabled, alphaWriteEnabled);
	}

	private void drawTexture(IRenderingEngine re, int x, int y, int projectionWidth, int projectionHeight, boolean scaleToCanvas, boolean redWriteEnabled, boolean greenWriteEnabled, boolean blueWriteEnabled, boolean alphaWriteEnabled) {
		re.startDirectRendering(true, false, true, true, true, projectionWidth, projectionHeight);
		re.setColorMask(redWriteEnabled, greenWriteEnabled, blueWriteEnabled, alphaWriteEnabled);
		if (scaleToCanvas) {
			re.setViewport(0, 0, Modules.sceDisplayModule.getCanvasWidth(), Modules.sceDisplayModule.getCanvasHeight());
		} else {
			re.setViewport(0, 0, projectionWidth, projectionHeight);
		}

        IREBufferManager bufferManager = re.getBufferManager();
        ByteBuffer drawByteBuffer = bufferManager.getBuffer(drawBufferId);
        drawByteBuffer.clear();
        FloatBuffer drawFloatBuffer = drawByteBuffer.asFloatBuffer();
        drawFloatBuffer.clear();
        drawFloatBuffer.put(texS);
        drawFloatBuffer.put(texT);
        drawFloatBuffer.put(x + width);
        drawFloatBuffer.put(y + height);

        drawFloatBuffer.put(0.f);
        drawFloatBuffer.put(texT);
        drawFloatBuffer.put(x);
        drawFloatBuffer.put(y + height);

        drawFloatBuffer.put(0.f);
        drawFloatBuffer.put(0.f);
        drawFloatBuffer.put(x);
        drawFloatBuffer.put(y);

        drawFloatBuffer.put(texS);
        drawFloatBuffer.put(0.f);
        drawFloatBuffer.put(x + width);
        drawFloatBuffer.put(y);

        if (re.isVertexArrayAvailable()) {
        	re.bindVertexArray(0);
        }
        re.setVertexInfo(null, false, false, true, -1);
        re.enableClientState(IRenderingEngine.RE_TEXTURE);
        re.disableClientState(IRenderingEngine.RE_COLOR);
        re.disableClientState(IRenderingEngine.RE_NORMAL);
        re.enableClientState(IRenderingEngine.RE_VERTEX);
        bufferManager.setTexCoordPointer(drawBufferId, 2, IRenderingEngine.RE_FLOAT, 4 * SIZEOF_FLOAT, 0);
        bufferManager.setVertexPointer(drawBufferId, 2, IRenderingEngine.RE_FLOAT, 4 * SIZEOF_FLOAT, 2 * SIZEOF_FLOAT);
        bufferManager.setBufferData(IRenderingEngine.RE_ARRAY_BUFFER, drawBufferId, drawFloatBuffer.position() * SIZEOF_FLOAT, drawByteBuffer.rewind(), IRenderingEngine.RE_DYNAMIC_DRAW);
        re.drawArrays(IRenderingEngine.RE_QUADS, 0, 4);

        re.endDirectRendering();
	}

	protected void setChanged(boolean changed) {
		this.changed = changed;
	}

	protected boolean hasChanged() {
		return changed;
	}

	private void prepareBuffer() {
		// Is the current buffer large enough?
		if (buffer != null && bufferLength < getTextureBufferLength()) {
			// Reallocate a new larger buffer
			buffer = null;
		}

		if (buffer == null) {
			bufferLength = getTextureBufferLength();
			ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bufferLength).order(ByteOrder.LITTLE_ENDIAN);
			if (Memory.getInstance().getMainMemoryByteBuffer() instanceof IntBuffer) {
				buffer = byteBuffer.asIntBuffer();
			} else {
				buffer = byteBuffer;
			}
		} else {
			buffer.clear();
		}
	}

	public void copyTextureToMemory(IRenderingEngine re) {
		if (textureId == -1) {
			// Texture not yet created... nothing to copy
			return;
		}

		if (!hasChanged()) {
			// Texture unchanged... don't copy again
			return;
		}

		if (log.isDebugEnabled()) {
			log.debug(String.format("GETexture.copyTextureToMemory %s", toString()));
		}

		Buffer memoryBuffer = Memory.getInstance().getBuffer(address, length);
    	prepareBuffer();
        re.bindTexture(textureId);
		re.setTextureFormat(pixelFormat, false);
        re.setPixelStore(bufferWidth, sceDisplay.getPixelFormatBytes(pixelFormat));
        re.getTexImage(0, pixelFormat, pixelFormat, buffer);

    	buffer.clear();
    	if (buffer instanceof IntBuffer) {
    		IntBuffer src = (IntBuffer) buffer;
    		IntBuffer dst = (IntBuffer) memoryBuffer;
    		int pixelsPerElement = 4 / bytesPerPixel;
    		int copyWidth = Math.min(width, bufferWidth);
    		int widthLimit = (copyWidth + pixelsPerElement - 1) / pixelsPerElement;
    		int step = bufferWidth / pixelsPerElement;
    		int srcOffset = 0;
    		int dstOffset = (height - 1) * step;
    		// We have received the texture data upside-down, invert it
    		for (int y = 0; y < height; y++, srcOffset += step, dstOffset -= step) {
    			src.limit(srcOffset + widthLimit);
    			src.position(srcOffset);
    			dst.position(dstOffset);
    			dst.put(src);
    		}
    	} else {
    		ByteBuffer src = (ByteBuffer) buffer;
    		ByteBuffer dst = (ByteBuffer) memoryBuffer;
    		int copyWidth = Math.min(width, bufferWidth);
    		int widthLimit = copyWidth * bytesPerPixel;
    		int step = bufferWidth * bytesPerPixel;
    		int srcOffset = 0;
    		int dstOffset = (height - 1) * step;
    		// We have received the texture data upside-down, invert it
    		for (int y = 0; y < height; y++, srcOffset += step, dstOffset -= step) {
    			src.limit(srcOffset + widthLimit);
    			src.position(srcOffset);
    			dst.position(dstOffset);
    			dst.put(src);
    		}
    	}

    	setChanged(false);
	}

	public void delete(IRenderingEngine re) {
		if (drawBufferId != -1) {
			re.getBufferManager().deleteBuffer(drawBufferId);
			drawBufferId = -1;
		}
		if (textureId != -1) {
			re.deleteTexture(textureId);
			textureId = -1;
		}
	}

	public int getTextureId() {
		return textureId;
	}

	public boolean isCompatible(int width, int height, int bufferWidth, int pixelFormat) {
		if (width != this.width || height != this.height || bufferWidth != this.bufferWidth) {
			return false;
		}

		if (useViewportResize) {
			if (resizeScale != getViewportResizeScaleFactor()) {
				return false;
			}
		}

		return true;
	}

	protected boolean copyStencilToTextureAlpha(IRenderingEngine re, int texWidth, int texHeight) {
		re.checkAndLogErrors(null);

		if (stencilFboId == -1) {
			// Create a FBO
			stencilFboId = re.genFramebuffer();
			re.bindFramebuffer(IRenderingEngine.RE_DRAW_FRAMEBUFFER, stencilFboId);

			// Create stencil texture and attach it to the FBO
			stencilTextureId = re.genTexture();
			re.bindTexture(stencilTextureId);
			re.checkAndLogErrors("bindTexture");
    		re.setTexImage(0, stencilPixelFormat, getTexImageWidth(), getTexImageHeight(), stencilPixelFormat, stencilPixelFormat, 0, null);
    		if (re.checkAndLogErrors("setTexImage")) {
    			return false;
    		}
            re.setTextureMipmapMinFilter(TFLT_NEAREST);
            re.setTextureMipmapMagFilter(TFLT_NEAREST);
            re.setTextureMipmapMinLevel(0);
            re.setTextureMipmapMaxLevel(0);
            re.setTextureWrapMode(TWRAP_WRAP_MODE_CLAMP, TWRAP_WRAP_MODE_CLAMP);
			re.setFramebufferTexture(IRenderingEngine.RE_DRAW_FRAMEBUFFER, IRenderingEngine.RE_DEPTH_STENCIL_ATTACHMENT, stencilTextureId, 0);
    		if (re.checkAndLogErrors("setFramebufferTexture RE_STENCIL_ATTACHMENT")) {
    			return false;
    		}

			// Attach the GE texture to the FBO as well
			re.setFramebufferTexture(IRenderingEngine.RE_DRAW_FRAMEBUFFER, IRenderingEngine.RE_COLOR_ATTACHMENT0, textureId, 0);
    		if (re.checkAndLogErrors("setFramebufferTexture RE_COLOR_ATTACHMENT0")) {
    			return false;
    		}
		} else {
			re.bindFramebuffer(IRenderingEngine.RE_DRAW_FRAMEBUFFER, stencilFboId);
		}

		// Copy screen stencil buffer to stencil texture:
		// - read framebuffer is screen (0)
		// - draw/write framebuffer is our stencil FBO (stencilFboId)
		re.blitFramebuffer(0, 0, texWidth, texHeight, 0, 0, texWidth, texHeight, IRenderingEngine.RE_STENCIL_BUFFER_BIT, GeCommands.TFLT_NEAREST);
		if (re.checkAndLogErrors("blitFramebuffer")) {
			return false;
		}

		re.bindTexture(stencilTextureId);

		if (!re.setCopyRedToAlpha(true)) {
			return false;
		}

		// Draw the stencil texture and update only the alpha channel of the GE texture
		drawTexture(re, 0, 0, width, height, true, false, false, false, true);
		re.checkAndLogErrors("drawTexture");

		// Reset the framebuffer to the default one
		re.bindFramebuffer(IRenderingEngine.RE_DRAW_FRAMEBUFFER, 0);

		re.setCopyRedToAlpha(false);

		// Success
		return true;
	}

	@Override
	public String toString() {
		return String.format("GETexture[0x%08X-0x%08X, %dx%d (texture %dx%d), bufferWidth=%d, pixelFormat=%d(%s)]", address, address + length, width, height, getTexImageWidth(), getTexImageHeight(), bufferWidth, pixelFormat, VideoEngine.getPsmName(pixelFormat));
	}
}
