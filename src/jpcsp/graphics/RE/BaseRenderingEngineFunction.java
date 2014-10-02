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
package jpcsp.graphics.RE;

import static java.lang.Math.abs;
import static jpcsp.graphics.GeCommands.TFLT_NEAREST;
import static jpcsp.graphics.GeCommands.TMAP_TEXTURE_MAP_MODE_TEXTURE_COORDIATES_UV;
import static jpcsp.graphics.GeCommands.TMAP_TEXTURE_PROJECTION_MODE_TEXTURE_COORDINATES;
import static jpcsp.graphics.GeCommands.TWRAP_WRAP_MODE_CLAMP;
import static jpcsp.graphics.RE.software.PixelColor.ONE;
import static jpcsp.graphics.RE.software.PixelColor.getBlue;
import static jpcsp.graphics.RE.software.PixelColor.getColorBGR;
import static jpcsp.graphics.RE.software.PixelColor.getGreen;
import static jpcsp.graphics.RE.software.PixelColor.getRed;
import static jpcsp.util.Utilities.matrixMult;

import java.nio.IntBuffer;

import jpcsp.graphics.GeCommands;
import jpcsp.graphics.VideoEngine;
import jpcsp.graphics.GeContext.EnableDisableFlag;
import jpcsp.settings.Settings;

/**
 * @author gid15
 *
 * Base class the RenderingEngine providing basic functionalities:
 * - generic clear mode handling: implementation matching the PSP behavior,
 *   but probably not the most efficient implementation.
 * - merge of View and Model matrix for OpenGL supporting
 *   only combined model-view matrix
 * - direct rendering mode
 * - implementation of the bounding box processing
 *   (using OpenGL Query with a partial software implementation)
 * - mapping of setColorMask(int, int, int, int) to setColorMask(bool, bool, bool, bool)
 * - bug fix for glMultiDrawArrays
 *
 * The partial software implementation for Bounding Boxes tries to find out
 * if a bounding box is visible or not without using an OpenGL Query.
 * The OpenGL queries have quite a large overhead to be setup and the software
 * implementation solves the following bounding box cases:
 * - if at least one bounding box vertex is visible,
 *   the complete bounding box is visible
 * - if all the bounding box vertices are not visible and are all placed on the
 *   same side of a frustum plane, then the complete bounding box is not visible.
 *   E.g.: all the vertices are hidden on the left side of the frustum.
 *
 *   If some vertices are hidden on different sides of the frustum (e.g. one on
 *   the left side and one on the right side), the implementation cannot determine
 *   if some pixels in between are visible or not. A complete intersection test is
 *   necessary in that case. Remark: this could be implemented in software too.
 *
 * In all other cases, the OpenGL query has to be used to determine if the bounding
 * box is visible or not.
 */
public class BaseRenderingEngineFunction extends BaseRenderingEngineProxy {

    protected static final boolean useWorkaroundForMultiDrawArrays = true;
    protected boolean useVertexArray;
    ClearModeContext clearModeContext = new ClearModeContext();
    protected boolean directMode;
    protected boolean directModeSetOrthoMatrix;
    protected static final boolean[] flagsValidInClearMode = new boolean[]{
        false, // GU_ALPHA_TEST
        false, // GU_DEPTH_TEST
        true,  // GU_SCISSOR_TEST
        false, // GU_STENCIL_TEST
        false, // GU_BLEND
        false, // GU_CULL_FACE
        true,  // GU_DITHER
        false, // GU_FOG
        true,  // GU_CLIP_PLANES
        false, // GU_TEXTURE_2D
        true,  // GU_LIGHTING
        true,  // GU_LIGHT0
        true,  // GU_LIGHT1
        true,  // GU_LIGHT2
        true,  // GU_LIGHT3
        true,  // GU_LINE_SMOOTH
        true,  // GU_PATCH_CULL_FACE
        false, // GU_COLOR_TEST
        false, // GU_COLOR_LOGIC_OP
        true,  // GU_FACE_NORMAL_REVERSE
        true,  // GU_PATCH_FACE
        true,  // GU_FRAGMENT_2X
        true,  // RE_COLOR_MATERIAL
        true,  // RE_TEXTURE_GEN_S
        true,  // RE_TEXTURE_GEN_T
    };

    protected static class ClearModeContext {

        public boolean color;
        public boolean stencil;
        public boolean depth;
        public int alphaFunc;
        public int depthFunc;
        public int textureFunc;
        public boolean textureAlphaUsed;
        public boolean textureColorDoubled;
        public int stencilFunc;
        public int stencilRef;
        public int stencilMask;
        public int stencilOpFail;
        public int stencilOpZFail;
        public int stencilOpZPass;
    };
    protected boolean viewMatrixLoaded = false;
    protected boolean modelMatrixLoaded = false;
    protected boolean bboxVisible;
    protected int activeTextureUnit = 0;
    private boolean[] colorMask = new boolean[4];

    public BaseRenderingEngineFunction(IRenderingEngine proxy) {
        super(proxy);
        init();
    }

    protected void init() {
        useVertexArray = Settings.getInstance().readBool("emu.enablevao") && super.isVertexArrayAvailable();
        if (useVertexArray) {
            log.info("Using VAO (Vertex Array Object)");
        }
    }

    @Override
    public void setRenderingEngine(IRenderingEngine re) {
        getBufferManager().setRenderingEngine(re);
        super.setRenderingEngine(re);
    }

    protected void setFlag(EnableDisableFlag flag) {
        if (flag.isEnabled()) {
            re.enableFlag(flag.getReFlag());
        } else {
            re.disableFlag(flag.getReFlag());
        }
    }

    protected void setClearModeSettings(boolean color, boolean stencil, boolean depth) {
        // Disable all the flags invalid in clear mode
        for (int i = 0; i < flagsValidInClearMode.length; i++) {
            if (!flagsValidInClearMode[i]) {
                re.disableFlag(i);
            }
        }

        if (stencil) {
        	re.enableFlag(GU_STENCIL_TEST);
        	re.setStencilFunc(GeCommands.STST_FUNCTION_ALWAYS_PASS_STENCIL_TEST, 0, 0);
        	re.setStencilOp(GeCommands.SOP_KEEP_STENCIL_VALUE, GeCommands.SOP_KEEP_STENCIL_VALUE, GeCommands.SOP_ZERO_STENCIL_VALUE);
    	}

        if (depth) {
        	re.enableFlag(GU_DEPTH_TEST);
        	context.depthFunc = GeCommands.ZTST_FUNCTION_ALWAYS_PASS_PIXEL;
        	re.setDepthFunc(context.depthFunc);
        }

        // Update color, stencil and depth masks.
        re.setDepthMask(depth);
        re.setColorMask(color, color, color, stencil);
        re.setTextureFunc(GeCommands.TFUNC_FRAGMENT_DOUBLE_TEXTURE_EFECT_REPLACE, true, false);
        re.setBones(0, null);
    }

    @Override
    public void startClearMode(boolean color, boolean stencil, boolean depth) {
		// Clear mode flags.
        clearModeContext.color = color;
        clearModeContext.stencil = stencil;
        clearModeContext.depth = depth;
        
        // Save depth.
        clearModeContext.depthFunc = context.depthFunc;
        
        // Save texture.     
        clearModeContext.textureFunc = context.textureFunc;
        clearModeContext.textureAlphaUsed = context.textureAlphaUsed;
        clearModeContext.textureColorDoubled = context.textureColorDoubled;
        
        // Save stencil.
        clearModeContext.stencilFunc = context.stencilFunc;
        clearModeContext.stencilRef = context.stencilRef;
        clearModeContext.stencilMask = context.stencilMask;
        clearModeContext.stencilOpFail = context.stencilOpFail;
        clearModeContext.stencilOpZFail = context.stencilOpZFail;
        clearModeContext.stencilOpZPass = context.stencilOpZPass;

        setClearModeSettings(color, stencil, depth);
        super.startClearMode(color, stencil, depth);
    }

    @Override
    public void endClearMode() {
        // Reset all the flags disabled in CLEAR mode
        for (EnableDisableFlag flag : context.flags) {
            if (!flagsValidInClearMode[flag.getReFlag()]) {
                setFlag(flag);
            }
        }

        // Restore depth.
        context.depthFunc = clearModeContext.depthFunc;
        re.setDepthFunc(context.depthFunc);

        // Restore texture.
        context.textureFunc = clearModeContext.textureFunc;
        context.textureAlphaUsed = clearModeContext.textureAlphaUsed;
        context.textureColorDoubled = clearModeContext.textureColorDoubled;
        re.setTextureFunc(context.textureFunc, context.textureAlphaUsed, context.textureColorDoubled);

        // Restore stencil.
        context.stencilFunc = clearModeContext.stencilFunc;
        context.stencilRef = clearModeContext.stencilRef;
        context.stencilMask = clearModeContext.stencilMask;
        re.setStencilFunc(context.stencilFunc, context.stencilRef, context.stencilRef);
        
        context.stencilOpFail = clearModeContext.stencilOpFail;
        context.stencilOpZFail = clearModeContext.stencilOpZFail;
        context.stencilOpZPass = clearModeContext.stencilOpZPass;
        re.setStencilOp(context.stencilOpFail, context.stencilOpZFail, context.stencilOpZPass);

        re.setDepthMask(context.depthMask);
        re.setColorMask(true, true, true, context.stencilTestFlag.isEnabled());
        re.setColorMask(context.colorMask[0], context.colorMask[1], context.colorMask[2], context.colorMask[3]);

        super.endClearMode();
    }

    protected boolean isClearMode() {
        return context.clearMode;
    }

    protected boolean canUpdateFlag(int flag) {
        return !context.clearMode || directMode || flagsValidInClearMode[flag];
    }

    protected boolean canUpdate() {
        return !context.clearMode || directMode;
    }

    protected static boolean getBooleanColorMask(String name, int bitMask) {
        if (bitMask == 0xFF) {
            return false;
        } else if (bitMask != 0x00) {
            log.warn(String.format("Unimplemented %s 0x%02X", name, bitMask));
        }

        return true;
    }

    @Override
    public void setColorMask(boolean redWriteEnabled, boolean greenWriteEnabled, boolean blueWriteEnabled, boolean alphaWriteEnabled) {
        colorMask[0] = redWriteEnabled;
        colorMask[1] = greenWriteEnabled;
        colorMask[2] = blueWriteEnabled;
        colorMask[3] = alphaWriteEnabled;
        super.setColorMask(redWriteEnabled, greenWriteEnabled, blueWriteEnabled, alphaWriteEnabled);
    }

    @Override
    public void setColorMask(int redMask, int greenMask, int blueMask, int alphaMask) {
        boolean redWriteEnabled = getBooleanColorMask("Red color mask", redMask);
        boolean greenWriteEnabled = getBooleanColorMask("Green color mask", greenMask);
        boolean blueWriteEnabled = getBooleanColorMask("Blue color mask", blueMask);
        // boolean alphaWriteEnabled = getBooleanColorMask("Alpha mask", alphaMask);
        re.setColorMask(redWriteEnabled, greenWriteEnabled, blueWriteEnabled, colorMask[3]);
        super.setColorMask(redMask, greenMask, blueMask, alphaMask);
    }

    @Override
    public void setDepthMask(boolean depthWriteEnabled) {
        if (canUpdate()) {
            super.setDepthMask(depthWriteEnabled);
        }
    }

    @Override
    public void setViewMatrix(float[] values) {
        super.setViewMatrix(values);
        // Reload the Model matrix if it was loaded before the View matrix (wrong order)
        if (modelMatrixLoaded) {
            re.setModelMatrix(context.model_uploaded_matrix);
        }
        viewMatrixLoaded = true;
    }

    @Override
    public void setModelMatrix(float[] values) {
        if (!viewMatrixLoaded) {
            re.setViewMatrix(context.view_uploaded_matrix);
        }
        super.setModelMatrix(values);
        modelMatrixLoaded = true;
    }

    @Override
    public void endModelViewMatrixUpdate() {
        if (!viewMatrixLoaded) {
            re.setViewMatrix(context.view_uploaded_matrix);
        }
        if (!modelMatrixLoaded) {
            re.setModelMatrix(context.model_uploaded_matrix);
        }
        super.endModelViewMatrixUpdate();
        viewMatrixLoaded = false;
        modelMatrixLoaded = false;
    }

    @Override
    public void startDirectRendering(boolean textureEnabled, boolean depthWriteEnabled, boolean colorWriteEnabled, boolean setOrthoMatrix, boolean orthoInverted, int width, int height) {
        directMode = true;

        re.disableFlag(GU_DEPTH_TEST);
        re.disableFlag(GU_BLEND);
        re.disableFlag(GU_ALPHA_TEST);
        re.disableFlag(GU_FOG);
        re.disableFlag(GU_LIGHTING);
        re.disableFlag(GU_COLOR_LOGIC_OP);
        re.disableFlag(GU_STENCIL_TEST);
        re.disableFlag(GU_CULL_FACE);
        re.disableFlag(GU_SCISSOR_TEST);
        if (textureEnabled) {
            re.enableFlag(GU_TEXTURE_2D);
        } else {
            re.disableFlag(GU_TEXTURE_2D);
        }
        re.setTextureMipmapMinFilter(TFLT_NEAREST);
        re.setTextureMipmapMagFilter(TFLT_NEAREST);
        re.setTextureMipmapMinLevel(0);
        re.setTextureMipmapMaxLevel(0);
        re.setTextureWrapMode(TWRAP_WRAP_MODE_CLAMP, TWRAP_WRAP_MODE_CLAMP);
        int colorMask = colorWriteEnabled ? 0x00 : 0xFF;
        re.setColorMask(colorMask, colorMask, colorMask, colorMask);
        re.setColorMask(colorWriteEnabled, colorWriteEnabled, colorWriteEnabled, colorWriteEnabled);
        re.setDepthMask(depthWriteEnabled);
        re.setTextureFunc(RE_TEXENV_REPLACE, true, false);
        re.setTextureMapMode(TMAP_TEXTURE_MAP_MODE_TEXTURE_COORDIATES_UV, TMAP_TEXTURE_PROJECTION_MODE_TEXTURE_COORDINATES);
        re.setFrontFace(true);
        re.setBones(0, null);

        directModeSetOrthoMatrix = setOrthoMatrix;
        if (setOrthoMatrix) {
            float[] orthoMatrix;
            if (orthoInverted) {
                orthoMatrix = VideoEngine.getOrthoMatrix(0, width, 0, height, -1, 1);
            } else {
                orthoMatrix = VideoEngine.getOrthoMatrix(0, width, height, 0, -1, 1);
            }
            re.setProjectionMatrix(orthoMatrix);
            re.setModelViewMatrix(null);
            re.setTextureMatrix(null);
        }

        super.startDirectRendering(textureEnabled, depthWriteEnabled, colorWriteEnabled, setOrthoMatrix, orthoInverted, width, height);
    }

    @Override
    public void endDirectRendering() {
        // Restore all the values according to the context or the clearMode
        re.setColorMask(context.colorMask[0], context.colorMask[1], context.colorMask[2], context.colorMask[3]);
        if (context.clearMode) {
            setClearModeSettings(clearModeContext.color, clearModeContext.stencil, clearModeContext.depth);
        } else {
            context.depthTestFlag.updateEnabled();
            context.blendFlag.updateEnabled();
            context.alphaTestFlag.updateEnabled();
            context.fogFlag.updateEnabled();
            context.colorLogicOpFlag.updateEnabled();
            context.stencilTestFlag.updateEnabled();
            context.cullFaceFlag.updateEnabled();
            context.textureFlag.update();
            re.setDepthMask(context.depthMask);
            re.setTextureFunc(context.textureFunc, context.textureAlphaUsed, context.textureColorDoubled);
        }
        re.setTextureMapMode(context.tex_map_mode, context.tex_proj_map_mode);
        context.scissorTestFlag.updateEnabled();
        context.lightingFlag.updateEnabled();
        re.setTextureMipmapMagFilter(context.tex_mag_filter);
        re.setTextureMipmapMinFilter(context.tex_min_filter);
        re.setTextureWrapMode(context.tex_wrap_s, context.tex_wrap_t);
        re.setFrontFace(context.frontFaceCw);

        if (directModeSetOrthoMatrix) {
            VideoEngine videoEngine = VideoEngine.getInstance();
            videoEngine.projectionMatrixUpload.setChanged(true);
            videoEngine.viewMatrixUpload.setChanged(true);
            videoEngine.modelMatrixUpload.setChanged(true);
            videoEngine.textureMatrixUpload.setChanged(true);
        }

        super.endDirectRendering();

        directMode = false;
    }

    @Override
    public void beginBoundingBox(int numberOfVertexBoundingBox) {
        bboxVisible = true;

        super.beginBoundingBox(numberOfVertexBoundingBox);
    }

    @Override
    public void drawBoundingBox(float[][] values) {
        if (bboxVisible) {
			// Pre-compute the Model-View-Projection matrix
        	final float[] modelViewMatrix = new float[16];
        	final float[] modelViewProjectionMatrix = new float[16];
			matrixMult(modelViewMatrix, context.view_uploaded_matrix, context.model_uploaded_matrix);
			matrixMult(modelViewProjectionMatrix, context.proj_uploaded_matrix, modelViewMatrix);

            final float viewportX = context.viewport_cx - context.offset_x;
            final float viewportY = context.viewport_cy - context.offset_y;
            final float viewportWidth = context.viewport_width;
            final float viewportHeight = context.viewport_height;
            final float[] mvpVertex = new float[4];
            float minX = 0f;
            float minY = 0f;
            float maxX = 0f;
            float maxY = 0f;
            float minW = 0f;
            float maxW = 0f;

            for (int i = 0; i < values.length; i++) {
                multMatrix44(mvpVertex, modelViewProjectionMatrix, values[i]);

                float w = mvpVertex[3];
                float x = minX;
                float y = minY;
                if (w != 0.f) {
                    x = mvpVertex[0] / w * viewportWidth + viewportX;
                    y = mvpVertex[1] / w * viewportHeight + viewportY;
                }
                if (i == 0) {
                	minX = maxX = x;
                	minY = maxY = y;
                	minW = maxW = w;
                } else {
                	minX = Math.min(minX, x);
                	maxX = Math.max(maxX, x);
                	minY = Math.min(minY, y);
                	maxY = Math.max(maxY, y);
                	minW = Math.min(minW, w);
                	maxW = Math.max(maxW, w);
                }

                if (log.isDebugEnabled()) {
                	log.debug(String.format("drawBoundingBox vertex#%d x=%f, y=%f, w=%f", i, x, y, w));
                }
            }

            // The Bounding Box is not visible when all vertices are outside the drawing region.
            if (maxX < context.region_x1 ||
                maxY < context.region_y1 ||
                minX > context.region_x2 ||
                minY > context.region_y2) {
        		// When the bounding box is partially before and behind the viewpoint,
            	// assume the bounding box is visible. Rejecting the bounding box in
            	// such cases is leading to incorrect results.
            	if (minW >= 0f || maxW <= 0f) {
            		bboxVisible = false;
            	}
            }
        }

        if (log.isDebugEnabled()) {
        	log.debug(String.format("drawBoundingBox visible=%b", bboxVisible));
        }

        super.drawBoundingBox(values);
    }

    @Override
    public boolean isBoundingBoxVisible() {
    	if (!bboxVisible) {
    		return false;
    	}
        return super.isBoundingBoxVisible();
    }

    @Override
    public void setTextureFunc(int func, boolean alphaUsed, boolean colorDoubled) {
        re.setTexEnv(RE_TEXENV_RGB_SCALE, colorDoubled ? 2.0f : 1.0f);
        re.setTexEnv(RE_TEXENV_ENV_MODE, func);
        super.setTextureFunc(func, alphaUsed, colorDoubled);
    }

    protected static void multMatrix44(float[] result4, float[] matrix44, float[] vector4) {
        float x = vector4[0];
        float y = vector4[1];
        float z = vector4[2];
        float w = vector4.length < 4 ? 1.f : vector4[3];
        result4[0] = matrix44[0] * x + matrix44[4] * y + matrix44[ 8] * z + matrix44[12] * w;
        result4[1] = matrix44[1] * x + matrix44[5] * y + matrix44[ 9] * z + matrix44[13] * w;
        result4[2] = matrix44[2] * x + matrix44[6] * y + matrix44[10] * z + matrix44[14] * w;
        result4[3] = matrix44[3] * x + matrix44[7] * y + matrix44[11] * z + matrix44[15] * w;
    }

    @Override
    public void startDisplay() {
        for (int light = 0; light < context.lightFlags.length; light++) {
            context.lightFlags[light].update();
        }
        super.startDisplay();
    }

    @Override
    public boolean isVertexArrayAvailable() {
        return useVertexArray;
    }

    @Override
    public void multiDrawArrays(int primitive, IntBuffer first, IntBuffer count) {
        // (gid15) I don't know why, but glMultiDrawArrays doesn't seem to work
        // as expected... is it a bug in LWJGL or did I misunderstood the effect
        // of the function?
        // Workaround using glDrawArrays provided.
        if (useWorkaroundForMultiDrawArrays) {
            int primitiveCount = first.remaining();
            int positionFirst = first.position();
            int positionCount = count.position();
            if (primitive == GeCommands.PRIM_POINT || primitive == GeCommands.PRIM_LINE || primitive == GeCommands.PRIM_TRIANGLE || primitive == IRenderingEngine.RE_QUADS) {
                // Independent elements can be rendered in one drawArrays call
                // if all the elements are sequentially defined
                boolean sequential = true;
                int firstIndex = first.get(positionFirst);
                int currentIndex = firstIndex;
                for (int i = 1; i < primitiveCount; i++) {
                    currentIndex += count.get(positionCount + i - 1);
                    if (currentIndex != first.get(positionFirst + i)) {
                        sequential = false;
                        break;
                    }
                }

                if (sequential) {
                    re.drawArrays(primitive, firstIndex, currentIndex - firstIndex + count.get(positionCount + primitiveCount - 1));
                    return;
                }
            }

            // Implement multiDrawArrays using multiple drawArrays.
            // The first call is using drawArrays and the subsequent calls,
            // drawArraysBurstMode (allowing a faster implementation).
            re.drawArrays(primitive, first.get(positionFirst), count.get(positionCount));
            for (int i = 1; i < primitiveCount; i++) {
                re.drawArraysBurstMode(primitive, first.get(positionFirst + i), count.get(positionCount + i));
            }
        } else {
            super.multiDrawArrays(primitive, first, count);
        }
    }

    @Override
    public void bindActiveTexture(int index, int texture) {
        int previousActiveTextureUnit = activeTextureUnit;
        re.setActiveTexture(index);
        re.bindTexture(texture);
        re.setActiveTexture(previousActiveTextureUnit);
    }

    @Override
    public void setActiveTexture(int index) {
        activeTextureUnit = index;
        super.setActiveTexture(index);
    }

    protected void setAlphaMask(boolean alphaWriteEnabled) {
        if (colorMask[3] != alphaWriteEnabled) {
            colorMask[3] = alphaWriteEnabled;
            re.setColorMask(colorMask[0], colorMask[1], colorMask[2], colorMask[3]);
        }
    }

    @Override
    public void disableFlag(int flag) {
        if (flag == IRenderingEngine.GU_STENCIL_TEST) {
            setAlphaMask(false);
        }
        super.disableFlag(flag);
    }

    @Override
    public void enableFlag(int flag) {
        if (flag == IRenderingEngine.GU_STENCIL_TEST) {
            setAlphaMask(true);
        }
        super.enableFlag(flag);
    }

    private int getBlendFix(int fixColor) {
        if (fixColor == 0x000000) {
            return IRenderingEngine.GU_FIX_BLACK;
        } else if (fixColor == 0xFFFFFF) {
            return IRenderingEngine.GU_FIX_WHITE;
        } else {
            return IRenderingEngine.GU_FIX_BLEND_COLOR;
        }
    }

    /**
     * Return the distance between 2 colors.
     * The distance is the sum of the color component differences.
     * 
     * @param color1
     * @param color2
     * @return
     */
    private int colorDistance(int color1, int color2) {
    	int blueDistance = abs(getBlue(color1) - getBlue(color2));
    	int greenDistance = abs(getGreen(color1) - getGreen(color2));
    	int redDistance = abs(getRed(color1) - getRed(color2));

    	return redDistance + greenDistance + blueDistance;
    }

    private int oneMinusColor(int color) {
    	int b = ONE - getBlue(color);
    	int g = ONE - getGreen(color);
    	int r = ONE - getRed(color);
    	return getColorBGR(b, g, r);
    }

    /**
     * Return the best distance that could be used with a blend factor
     * for a given color and blend color.
     * The possible blend factors are
     * - BLACK
     * - WHITE
     * - the blend color
     * - one minus the blend color
     * 
     * @param blendColor
     * @param color
     * @return
     */
    private int getBestColorDistance(int blendColor, int color) {
    	int bestDistance = colorDistance(color, blendColor);
    	bestDistance = Math.min(bestDistance, colorDistance(color, oneMinusColor(blendColor)));
    	bestDistance = Math.min(bestDistance, colorDistance(color, 0x000000));
    	bestDistance = Math.min(bestDistance, colorDistance(color, 0xFFFFFF));

    	return bestDistance;
    }

    /**
     * Find the best blend factor for a color given a blend color.
     * 
     * @param blendColor
     * @param oneMinusBlendColor
     * @param color
     * @return
     */
    private int getBestBlend(int blendColor, int oneMinusBlendColor, int color) {
    	// Simple cases...
    	if (blendColor == 0x000000) {
    		return IRenderingEngine.GU_FIX_BLACK;
    	}
    	if (blendColor == 0xFFFFFF) {
    		return IRenderingEngine.GU_FIX_WHITE;
    	}
    	if (blendColor == color) {
    		return IRenderingEngine.GU_FIX_BLEND_COLOR;
    	}
    	if (blendColor == oneMinusBlendColor) {
    		return IRenderingEngine.GU_FIX_BLEND_ONE_MINUS_COLOR;
    	}

    	// Complex case: test which blend would be the closest to the given color
    	int bestDistance = colorDistance(color, blendColor);
    	int bestBlend = IRenderingEngine.GU_FIX_BLEND_COLOR;

    	int distance = colorDistance(color, oneMinusBlendColor);
    	if (distance < bestDistance) {
    		bestDistance = distance;
    		bestBlend = IRenderingEngine.GU_FIX_BLEND_ONE_MINUS_COLOR;
    	}

    	distance = colorDistance(color, 0x000000);
    	if (distance < bestDistance) {
    		bestDistance = distance;
    		bestBlend = IRenderingEngine.GU_FIX_BLACK;
    	}

    	distance = colorDistance(color, 0xFFFFFF);
    	if (distance < bestDistance) {
    		bestDistance = distance;
    		bestBlend = IRenderingEngine.GU_FIX_WHITE;
    	}

    	return bestBlend;
    }

    private int getColorFromBlend(int blend, int blendColor, int oneMinusBlendColor) {
    	switch (blend) {
    		case IRenderingEngine.GU_FIX_BLACK: return 0x000000;
    		case IRenderingEngine.GU_FIX_WHITE: return 0xFFFFFF;
    		case IRenderingEngine.GU_FIX_BLEND_COLOR: return blendColor;
    		case IRenderingEngine.GU_FIX_BLEND_ONE_MINUS_COLOR: return oneMinusBlendColor;
    	}

    	// Unknown blend...
    	return -1;
    }

    @Override
    public void setBlendFunc(int src, int dst) {
        if (src == 10) { // GU_FIX
            src = getBlendFix(context.sfix);
        }

        if (dst == 10) { // GU_FIX
            if (src == IRenderingEngine.GU_FIX_BLEND_COLOR && (context.sfix + context.dfix == 0xFFFFFF)) {
                dst = IRenderingEngine.GU_FIX_BLEND_ONE_MINUS_COLOR;
            } else {
                dst = getBlendFix(context.dfix);
            }
        }

        float[] blend_color = null;
        if (src == IRenderingEngine.GU_FIX_BLEND_COLOR) {
            blend_color = context.sfix_color;
            if (dst == IRenderingEngine.GU_FIX_BLEND_COLOR) {
                if (context.sfix != context.dfix) {
                	// We cannot set the correct FIX blend colors.
                	// Try to find the best approximation...
                	int blendColor;
                	// Check which blend color, sfix or dfix, would give the best results
                	// (i.e. would have the smallest distance)
                	if (getBestColorDistance(context.sfix, context.dfix) <= getBestColorDistance(context.dfix, context.sfix)) {
                		// Taking sfix as the blend color leads to the best results
                		blendColor = context.sfix;
                		blend_color = context.sfix_color;
                	} else {
                		// Taking dfix as the blend color leads to the best results
                		blendColor = context.dfix;
                		blend_color = context.dfix_color;
                	}
                	int oneMinusBlendColor = oneMinusColor(blendColor);

                	// Now that we have decided which blend color to take,
                	// find the optimum blend factor for both the source and destination
                	src = getBestBlend(blendColor, oneMinusBlendColor, context.sfix);
                	dst = getBestBlend(blendColor, oneMinusBlendColor, context.dfix);

                	if (log.isInfoEnabled()) {
                		log.warn(String.format("UNSUPPORTED: Both different SFIX (0x%06X) and DFIX (0x%06X) are not supported (blend equation=%d), approximating with 0x%06X/0x%06X", context.sfix, context.dfix, context.blendEquation, getColorFromBlend(src, blendColor, oneMinusBlendColor), getColorFromBlend(dst, blendColor, oneMinusBlendColor)));
                	}
                }
            }
        } else if (dst == IRenderingEngine.GU_FIX_BLEND_COLOR) {
            blend_color = context.dfix_color;
        }

        if (blend_color != null) {
            re.setBlendColor(blend_color);
        }

        super.setBlendFunc(src, dst);
    }

    @Override
    public void setBlendDFix(int sfix, float[] color) {
        // Update the blend color and functions when the DFIX is changing
        setBlendFunc(context.blend_src, context.blend_dst);

        super.setBlendDFix(sfix, color);
    }

    @Override
    public void setBlendSFix(int dfix, float[] color) {
        // Update the blend color and functions when the SFIX is changing
        setBlendFunc(context.blend_src, context.blend_dst);

        super.setBlendSFix(dfix, color);
    }

	@Override
	public void multiDrawElements(int primitive, IntBuffer first, IntBuffer count, int indexType, long indicesOffset) {
        int primitiveCount = first.remaining();
        int positionFirst = first.position();
        int positionCount = count.position();
        if (primitive == GeCommands.PRIM_POINT || primitive == GeCommands.PRIM_LINE || primitive == GeCommands.PRIM_TRIANGLE || primitive == IRenderingEngine.RE_QUADS) {
            // Independent elements can be rendered in one drawElements call
            // if all the elements are sequentially defined and the first
        	// index is 0
            boolean sequential = true;
            int firstIndex = first.get(positionFirst);
            int currentIndex = firstIndex;
            if (firstIndex != 0) {
            	sequential = false;
            } else {
	            for (int i = 1; i < primitiveCount; i++) {
	                currentIndex += count.get(positionCount + i - 1);
	                if (currentIndex != first.get(positionFirst + i)) {
	                    sequential = false;
	                    break;
	                }
	            }
            }

            if (sequential) {
                re.drawElements(primitive, currentIndex - firstIndex + count.get(positionCount + primitiveCount - 1), indexType, indicesOffset);
                return;
            }
        }

        // Implement multiDrawElements using multiple drawElements.
        // The first call is using drawElements and the subsequent calls,
        // drawElementsBurstMode (allowing a faster implementation).
        int bytesPerIndex = sizeOfType[indexType];
        re.drawElements(primitive, count.get(positionCount), indexType, indicesOffset + first.get(positionFirst) * bytesPerIndex);
        for (int i = 1; i < primitiveCount; i++) {
            re.drawElementsBurstMode(primitive, count.get(positionCount + i), indexType, indicesOffset + first.get(positionFirst + i) * bytesPerIndex);
        }
	}
}