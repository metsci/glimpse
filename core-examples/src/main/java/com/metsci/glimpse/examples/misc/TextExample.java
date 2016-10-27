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
package com.metsci.glimpse.examples.misc;

import java.io.IOException;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;
import javax.media.opengl.fixedfunc.GLMatrixFunc;

import com.jogamp.graph.curve.Region;
import com.jogamp.graph.curve.opengl.RegionRenderer;
import com.jogamp.graph.curve.opengl.RenderState;
import com.jogamp.graph.curve.opengl.TextRegionUtil;
import com.jogamp.graph.font.Font;
import com.jogamp.graph.font.FontFactory;
import com.jogamp.graph.geom.SVertex;
import com.jogamp.opengl.util.PMVMatrix;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.gl.util.GLUtils;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.base.GlimpsePainter;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.plot.EmptyPlot2D;
import com.metsci.glimpse.support.color.GlimpseColor;

public class TextExample implements GlimpseLayoutProvider
{
    public static void main( String[] args ) throws Exception
    {
        Example.showWithSwing( new TextExample( ) );
    }

    @Override
    public GlimpseLayout getLayout( ) throws Exception
    {
        EmptyPlot2D plot = new EmptyPlot2D( );

        Font loadFont = null;
        try
        {
            loadFont = FontFactory.get( getClass( ), "fonts/bitstream/Vera.ttf", false );
        }
        catch ( IOException e )
        {
            System.err.println( "Couldn't open font!" );
            e.printStackTrace( );
        }

        RenderState renderState = RenderState.createRenderState( SVertex.factory( ) );
        renderState.setColorStatic( 0, 0, 0, 1 );
        renderState.setHintMask( RenderState.BITHINT_GLOBAL_DEPTH_TEST_ENABLED );

        final RegionRenderer renderer = RegionRenderer.create( renderState, RegionRenderer.defaultBlendEnable, RegionRenderer.defaultBlendDisable );
        final TextRegionUtil util = new TextRegionUtil( Region.DEFAULT_TWO_PASS_TEXTURE_UNIT | Region.COLORCHANNEL_RENDERING_BIT );
        final Font font = loadFont;

        GlimpsePainter painter = new GlimpsePainterBase( )
        {
            protected boolean initialized = false;

            @Override
            public void doPaintTo( GlimpseContext context )
            {
                Axis2D axis = requireAxis2D( context );
                GlimpseBounds bounds = getBounds( context );
                GL3 gl = context.getGL( ).getGL3( );

                if ( !initialized )
                {
                    renderer.init( gl, Region.DEFAULT_TWO_PASS_TEXTURE_UNIT | Region.COLORCHANNEL_RENDERING_BIT );
                    initialized = true;
                }

                GLUtils.enableStandardBlending( gl );
                gl.glEnable( GL.GL_CULL_FACE );
                try
                {
                    PMVMatrix m = renderer.getMatrix( );
                    m.glMatrixMode( GLMatrixFunc.GL_MODELVIEW );
                    m.glLoadIdentity( );
                    m.glTranslatef( 5, 7, 0 );

                    m.glMatrixMode( GLMatrixFunc.GL_PROJECTION );
                    m.glLoadIdentity( );
                    m.glOrthof( ( float ) axis.getMinX( ), ( float ) axis.getMaxX( ), ( float ) axis.getMinY( ), ( float ) axis.getMaxY( ), -1f, 1f );

                    float pixelSize = 1.0f; //font.getPixelSize( 32, 96 );
                    int[] samples = new int[1];
                    //String text = "Test";
                    String text = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
                    util.drawString3D( gl, renderer, font, pixelSize, text, GlimpseColor.getRed( ), samples );

                    m.glMatrixMode( GLMatrixFunc.GL_PROJECTION );
                    m.glLoadIdentity( );
                    m.glOrthof( 0, bounds.getWidth( ), 0, bounds.getHeight( ), -1f, 1f );

                    m.glMatrixMode( GLMatrixFunc.GL_MODELVIEW );
                    m.glLoadIdentity( );
                    m.glTranslatef( ( float ) axis.getAxisX( ).valueToScreenPixelUnits( 5 ), ( float ) axis.getAxisY( ).valueToScreenPixelUnits( 5 ), 0f );

                    pixelSize = font.getPixelSize( 32, 96 );
                    util.drawString3D( gl, renderer, font, pixelSize, text, GlimpseColor.getRed( ), samples );

                    renderer.enable( gl, false );
                }
                finally
                {
                    gl.glDisable( GL.GL_CULL_FACE );
                    GLUtils.disableBlending( gl );
                }
            }

            @Override
            protected void doDispose( GlimpseContext context )
            {
                // do nothing
            }
        };

        plot.addPainter( painter );

        return plot;
    }
}
