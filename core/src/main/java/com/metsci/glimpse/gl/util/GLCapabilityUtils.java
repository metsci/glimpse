/*
 * Copyright (c) 2016, Metron, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Metron, Inc. nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL METRON, INC. BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.metsci.glimpse.gl.util;

import static com.metsci.glimpse.gl.util.GLUtils.queryGLBoolean;
import static com.metsci.glimpse.gl.util.GLUtils.queryGLInteger;
import static com.metsci.glimpse.util.logging.LoggerUtils.log;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;
import javax.media.opengl.GLContext;

/**
 * @author osborn
 */
public class GLCapabilityUtils
{
    // these come from the official OpenGL documentation
    public static final String glVendorDocumentation = "The company responsible for this GL implementation. This name does not change from release to release.";
    public static final String glRendererDocumentation = "The name of the renderer. This name is typically specific to a particular configuration of a hardware platform. It does not change from release to release.";
    public static final String glVersionDocumentation = "A version or release number.";
    public static final String glShaderVersionDocumentation = "A version or release number for the shading language.";
    public static final String glExtensionDocumentation = "A list of supported extensions to GL3.";

    public static void logGLBufferProperties( Logger logger, Level level, GLContext context, String prefix )
    {
        GL gl = context.getGL( );

        if ( prefix == null ) prefix = "";

        int rBits = queryGLInteger( GL.GL_RED_BITS, gl );
        int gBits = queryGLInteger( GL.GL_GREEN_BITS, gl );
        int bBits = queryGLInteger( GL.GL_BLUE_BITS, gl );
        int aBits = queryGLInteger( GL.GL_ALPHA_BITS, gl );
        int dBits = queryGLInteger( GL.GL_DEPTH_BITS, gl );
        int sBits = queryGLInteger( GL.GL_STENCIL_BITS, gl );
        int totalBits = rBits + gBits + bBits + aBits + dBits + sBits;

        log( logger, level, "%sbuffer-properties: %2d bits depth", prefix, dBits );
        log( logger, level, "%sbuffer-properties: %2d bits stencil", prefix, sBits );
        log( logger, level, "%sbuffer-properties: %2d bits red", prefix, rBits );
        log( logger, level, "%sbuffer-properties: %2d bits green", prefix, gBits );
        log( logger, level, "%sbuffer-properties: %2d bits blue", prefix, bBits );
        log( logger, level, "%sbuffer-properties: %2d bits alpha", prefix, aBits );
        log( logger, level, "%sbuffer-properties: %d bits TOTAL", prefix, totalBits );

        boolean isDoubleBuffered = queryGLBoolean( GL3.GL_DOUBLEBUFFER, gl );
        log( logger, level, "%sbuffer-properties: %s double buffered", prefix, isDoubleBuffered ? "is" : "is NOT" );

        boolean isStereo = queryGLBoolean( GL3.GL_STEREO, gl );
        log( logger, level, "%sbuffer-properties: %s stereo", prefix, isStereo ? "is" : "is NOT" );
    }

    public static void logGLVersionInfo( Logger logger, Level level, GLContext context )
    {
        logGLVersionInfo( logger, level, context, false );
    }

    public static void logGLVersionInfo( Logger logger, Level level, GLContext context, boolean includeDocumentations )
    {
        GL gl = context.getGL( );

        String vendor = getGLVendorString( gl );
        if ( vendor == null ) vendor = "unavailable";

        String renderer = getGLRendererString( gl );
        if ( renderer == null ) renderer = "unavailable";

        String glVersion = getGLVersionString( gl );
        if ( glVersion == null ) glVersion = "unavailable";

        String shaderVersion = getGLShaderVersionString( gl );
        if ( shaderVersion == null ) shaderVersion = "unavailable";

        log( logger, level, "OpenGL Vendor: %s", vendor );
        if ( includeDocumentations ) log( logger, level, "OpenGL Vendor Documentation: %s", glVendorDocumentation );

        log( logger, level, "OpenGL Renderer: %s", renderer );
        if ( includeDocumentations ) log( logger, level, "OpenGL Renderer Documentation: %s", glRendererDocumentation );

        log( logger, level, "OpenGL Version: %s", glVersion );
        if ( includeDocumentations ) log( logger, level, "OpenGL Version Documentation: %s", glVersionDocumentation );

        log( logger, level, "OpenGL Shader Version: %s", shaderVersion );
        if ( includeDocumentations ) log( logger, level, "OpenGL Shader Version Documentation: %s", glShaderVersionDocumentation );
    }

    public static void logGLExtensions( Logger logger, Level level, GLContext context, boolean separateLines )
    {
        GL gl = context.getGL( );

        String extString = getGLExtensions( gl );
        if ( extString == null )
        {
            log( logger, level, "No OpenGL extensions found." );
        }
        else
        {
            String[] exts = extString.split( " " );
            if ( separateLines )
            {
                log( logger, level, "%d OpenGL extenstions found.", exts.length );
                for ( int i = 0; i < exts.length; i++ )
                    log( logger, level, "OpenGL extension found: %s", exts[i] );
            }
            else
            {
                log( logger, level, "%d OpenGL extenstions found: %s", exts.length, extString.trim( ) );
            }
        }
    }

    /**
     * Logs a variety of capability limitations, such as maximum texture size.
     */
    public static void logGLMaximumValues( Logger logger, Level level, GLContext context )
    {
        GL gl = context.getGL( );

        int maxVertexAttributes = getGLMaxVertexAttributes( gl );
        int maxGeomShaderOutput = getGLMaxGeometryShaderOutput( gl );
        int maxTexEdge = getGLMaxTextureEdgeLength( gl );
        int maxTexBuffer = getGLMaxTextureBufferSize( gl );
        int maxIndices = GLCapabilityUtils.getGLMaxElementsIndices( gl );
        int maxVertices = GLCapabilityUtils.getGLMaxElementsVertices( gl );

        log( logger, level, "OpenGL Max Vertex Attributes: %d", maxVertexAttributes );
        log( logger, level, "OpenGL Max Geometry Outputs: %d", maxGeomShaderOutput );
        log( logger, level, "OpenGL Max Texture Edge Length: %d", maxTexEdge );
        log( logger, level, "OpenGL Max Texture Buffer Size: %d", maxTexBuffer );
        log( logger, level, "OpenGL Max Element Indices: %d", maxIndices );
        log( logger, level, "OpenGL Max Element Vertices: %d", maxVertices );
    }

    /**
     * Returns the maximum number of 4-component generic vertex attributes
     * accessible to a vertex shader. The value must be at least 16.
     */
    public static int getGLMaxVertexAttributes( GL gl )
    {
        return queryGLInteger( GL3.GL_MAX_VERTEX_ATTRIBS, gl );
    }

    /**
     * Returns the maximum number of components of outputs written by a geometry
     * shader, which must be at least 128
     */
    public static int getGLMaxGeometryShaderOutput( GL gl )
    {
        // TODO: Figure out this GLProfile business -- ttran17
        return queryGLInteger( GL3.GL_MAX_GEOMETRY_TOTAL_OUTPUT_COMPONENTS, gl );
    }

    /**
     * The value gives a rough estimate of the largest texture that the GL can
     * handle. The value must be at least 1024. Use a proxy texture target such
     * as GL_PROXY_TEXTURE_1D or GL_PROXY_TEXTURE_2D to determine if a texture
     * is too large.
     */
    public static int getGLMaxTextureEdgeLength( GL gl )
    {
        return queryGLInteger( GL3.GL_MAX_TEXTURE_SIZE, gl );
    }

    /**
     * The value gives the maximum number of texels allowed in the texel array
     * of a texture buffer object. Value must be at least 65536.
     */
    public static int getGLMaxTextureBufferSize( GL gl )
    {
        return queryGLInteger( GL3.GL_MAX_TEXTURE_BUFFER_SIZE, gl );
    }

    public static int getGLMaxElementsIndices( GL gl )
    {
        return queryGLInteger( GL3.GL_MAX_ELEMENTS_INDICES, gl );
    }

    public static int getGLMaxElementsVertices( GL gl )
    {
        return queryGLInteger( GL3.GL_MAX_ELEMENTS_VERTICES, gl );
    }

    /**
     * @return The company responsible for this GL implementation. This name
     *         does not change from release to release.
     */
    public static String getGLVendorString( GL gl )
    {
        return gl.glGetString( GL3.GL_VENDOR );
    }

    /**
     * @return The name of the renderer. This name is typically specific to a
     *         particular configuration of a hardware platform. It does not
     *         change from release to release.
     */
    public static String getGLRendererString( GL gl )
    {
        return gl.glGetString( GL3.GL_RENDERER );
    }

    /**
     * @return A version or release number.
     */
    public static String getGLVersionString( GL gl )
    {
        return gl.glGetString( GL3.GL_VERSION );
    }

    /**
     * @return A version or release number for the shading language.
     */
    public static String getGLShaderVersionString( GL gl )
    {
        return gl.glGetString( GL3.GL_SHADING_LANGUAGE_VERSION );
    }

    /**
     * @return A space-delimited list of supported extensions to GL3.
     */
    public static String getGLExtensions( GL gl )
    {
        return gl.glGetString( GL3.GL_EXTENSIONS );
    }
}
