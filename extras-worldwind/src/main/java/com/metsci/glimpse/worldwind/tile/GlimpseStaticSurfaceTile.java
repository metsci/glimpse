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
package com.metsci.glimpse.worldwind.tile;

import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;

import java.util.logging.Logger;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLContext;

import com.metsci.glimpse.canvas.GlimpseCanvas;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.context.TargetStackUtil;
import com.metsci.glimpse.gl.GLSimpleFrameBufferObject;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.worldwind.canvas.SimpleOffscreenCanvas;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.PreRenderable;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.util.OGLStackHandler;

/**
 * GlimpseSurfaceTile uses TextureSurfaceTile to display the output of Glimpse
 * offscreen rendering onto the surface of the Worldwind globe.
 *
 * @author ulman
 */
public class GlimpseStaticSurfaceTile extends AbstractLayer implements GlimpseSurfaceTile, Renderable, PreRenderable
{
    private static final Logger logger = Logger.getLogger( GlimpseStaticSurfaceTile.class.getSimpleName( ) );

    protected GlimpseLayout layout;
    protected int width, height;
    protected Iterable<? extends LatLon> corners;

    protected SimpleOffscreenCanvas offscreenCanvas;
    protected TextureSurfaceTile tile;
    protected GLContext context;

    public GlimpseStaticSurfaceTile( GlimpseLayout layout, int width, int height, Iterable<? extends LatLon> corners )
    {
        this.layout = layout;
        this.width = width;
        this.height = height;
        this.corners = corners;

        this.offscreenCanvas = new SimpleOffscreenCanvas( width, height, false, false );
        this.offscreenCanvas.addLayout( layout );
    }

    @Override
    public GlimpseLayout getGlimpseLayout( )
    {
        return this.layout;
    }

    @Override
    public GlimpseCanvas getGlimpseCanvas( )
    {
        return this.offscreenCanvas;
    }

    @Override
    public GlimpseTargetStack getTargetStack( )
    {
        return TargetStackUtil.newTargetStack( this.offscreenCanvas, this.layout );
    }

    @Override
    public void preRender( DrawContext dc )
    {
        if ( tile == null )
        {

            if ( context == null )
            {
                GLContext oldcontext = dc.getGLContext( );
                context = dc.getGLDrawable( ).createContext( oldcontext );
            }

            this.offscreenCanvas.initialize( context );
        }

        drawOffscreen( dc );

        if ( tile == null )
        {
            int textureHandle = offscreenCanvas.getFrameBuffer( ).getTextureId( );
            tile = newTextureSurfaceTile( textureHandle, corners );
        }
    }

    protected TextureSurfaceTile newTextureSurfaceTile( int textureHandle, Iterable<? extends LatLon> corners )
    {
        return new TextureSurfaceTile( textureHandle, corners );
    }

    @Override
    protected void doRender( DrawContext dc )
    {
        tile.render( dc );

    }

    protected void drawOffscreen( DrawContext dc )
    {
        context.makeCurrent( );
        try
        {
            drawOffscreen( dc.getGLContext( ) );
        }
        finally
        {
            dc.getGLContext( ).makeCurrent( );
        }
    }

    protected void drawOffscreen( GLContext glContext )
    {
        GLSimpleFrameBufferObject fbo = offscreenCanvas.getFrameBuffer( );
        OGLStackHandler stack = new OGLStackHandler( );
        GL2 gl = glContext.getGL( ).getGL2( );

        stack.pushAttrib( gl, GL2.GL_ALL_ATTRIB_BITS );
        stack.pushClientAttrib( gl, ( int ) GL2.GL_ALL_CLIENT_ATTRIB_BITS );
        stack.pushTexture( gl );
        stack.pushModelview( gl );
        stack.pushProjection( gl );

        fbo.bind( glContext );
        try
        {
            layout.paintTo( offscreenCanvas.getGlimpseContext( ) );
        }
        catch ( Exception e )
        {
            logWarning( logger, "Trouble drawing to offscreen buffer", e );
        }
        finally
        {
            fbo.unbind( glContext );
            stack.pop( gl );
        }
    }
}
