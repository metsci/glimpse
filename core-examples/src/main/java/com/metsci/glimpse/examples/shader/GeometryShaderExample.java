/*
 * Copyright (c) 2012, Metron, Inc.
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
package com.metsci.glimpse.examples.shader;

import java.io.IOException;
import java.util.Date;
import java.util.EnumSet;
import java.util.Random;

import javax.media.opengl.GL;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.AxisUtil;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.event.mouse.GlimpseMouseListener;
import com.metsci.glimpse.event.mouse.MouseButton;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.gl.shader.Pipeline;
import com.metsci.glimpse.gl.shader.Shader;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.base.GlimpseDataPainter2D;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.painter.info.FpsPainter;
import com.metsci.glimpse.painter.info.SimpleTextPainter;
import com.metsci.glimpse.painter.info.SimpleTextPainter.HorizontalPosition;
import com.metsci.glimpse.painter.info.SimpleTextPainter.VerticalPosition;
import com.metsci.glimpse.support.color.RGBA;
import com.metsci.glimpse.support.color.WebColors;
import com.metsci.glimpse.support.shader.geometry.SimpleGeometryShader;
import com.metsci.glimpse.support.shader.geometry.SimpleShader;

/**
 * @author ellis
 */
public class GeometryShaderExample implements GlimpseLayoutProvider
{
    public static void main( String[] args ) throws Exception
    {
        Example.showWithSwing( new GeometryShaderExample( ) );
    }

    GeometryShaderExample( ) throws IOException
    {

        colors = new RGBA[] { WebColors.CornflowerBlue, WebColors.Salmon, WebColors.ForestGreen };

        //@formatter:off
        shaders = new SimpleGeometryShader[] { SimpleGeometryShader.passGeometry( GL.GL_LINES, GL.GL_LINE_STRIP, 2 ),
                                               SimpleGeometryShader.linesToXs( ),
                                               SimpleGeometryShader.pointsToFixedSizeNGons( false, 4, 0.0375f ),
                                               SimpleGeometryShader.linesToVariableSizeNGons( true, 6 ),
                                               SimpleGeometryShader.linesToVariableSizeNGons( false, 3 ),
                                               SimpleGeometryShader.pointsToFixedSizeNGons( true, 5, 0.075f ),
                                               SimpleGeometryShader.pointsToPixelSizedNGons( false, 3, 10 ) };

        pipelineNames = new String[] { "pass through lines",
                                       "lines into Xs",
                                       "points into fixed squares",
                                       "lines into solid, variable hexagons",
                                       "lines into variable triangles",
                                       "points into fixed, solid pentagons",
                                       "points into 10 pixel radius triangles" };
        //@formatter:on

        pipelines = new Pipeline[shaders.length];
        inputTypes = new int[shaders.length];
        Shader passVertices = SimpleShader.passVertex( );
        for ( int i = 0; i < shaders.length; i++ )
        {
            pipelines[i] = new Pipeline( pipelineNames[i], shaders[i], passVertices, null );
            inputTypes[i] = shaders[i].inType;
        }

        pipelineIndex1 = 0;
        pipelineIndex2 = 0;
        pipelineIndex3 = 0;
    }

    final SimpleGeometryShader[] shaders;
    final int[] inputTypes;
    final Pipeline[] pipelines;
    final String[] pipelineNames;
    int pipelineIndex1, pipelineIndex2, pipelineIndex3;
    private final SimpleTextPainter textLeft = new SimpleTextPainter( ), textRight = new SimpleTextPainter( ),
            textCenter = new SimpleTextPainter( );
    RGBA[] colors;

    private static final float z = 0.0f, w = 1.0f;

    protected void updateText( )
    {

        textLeft.setText( String.format( "Left click to change: %s", pipelineNames[pipelineIndex1] ) );
        textRight.setText( String.format( "Right click to change: %s", pipelineNames[pipelineIndex2] ) );
        textCenter.setText( String.format( "Center click to change: %s", pipelineNames[pipelineIndex3] ) );
    }

    @Override
    public GlimpseLayout getLayout( )
    {
        GlimpseAxisLayout2D backgroundLayout = new GlimpseAxisLayout2D( new Axis2D( ) );
        backgroundLayout.addPainter( new BackgroundPainter( ) );
        backgroundLayout.addPainter( new GlimpseDataPainter2D( )
        {
            float[][] xys = null;
            int nRandom = 1000;

            @Override
            public void paintTo( GL gl, GlimpseBounds bounds, Axis2D axes )
            {
                {
                    RGBA color = colors[2];

                    gl.glColor3f( color.r, color.g, color.b );
                    int ix3 = pipelineIndex3;
                    int t3 = inputTypes[ix3];
                    Pipeline p3 = pipelines[ix3];
                    p3.beginUse( gl );
                    gl.glBegin( t3 );
                    {
                        Random r = new Random( new Date( ).getTime( ) );
                        float deltaX = ( float ) ( axes.getMaxX( ) - axes.getMinX( ) );
                        float deltaY = ( float ) ( axes.getMaxX( ) - axes.getMinX( ) );
                        if ( xys == null )
                        {
                            xys = new float[nRandom][4];
                            for ( int i = 0; i < nRandom; i++ )
                            {
                                float x0 = r.nextFloat( ) * deltaX + ( float ) axes.getMinX( );
                                float y0 = r.nextFloat( ) * deltaY + ( float ) axes.getMinY( );
                                float angle = r.nextFloat( ) * ( float ) ( 2 * Math.PI );
                                float distance = ( float ) ( r.nextGaussian( ) / 2 );
                                float x1 = x0 + distance * ( float ) Math.cos( angle );
                                float y1 = y0 + distance * ( float ) Math.sin( angle );
                                xys[i][0] = x0;
                                xys[i][1] = y0;
                                xys[i][2] = x1;
                                xys[i][3] = y1;
                            }
                        }

                        for ( int i = 0; i < nRandom; i++ )
                        {

                            xys[i][0] += r.nextGaussian( ) * deltaX / 100;
                            xys[i][1] += r.nextGaussian( ) * deltaX / 100;
                            xys[i][0] = ( float ) Math.max( axes.getMinX( ), Math.min( axes.getMaxX( ), xys[i][0] ) );
                            xys[i][1] = ( float ) Math.max( axes.getMinY( ), Math.min( axes.getMaxY( ), xys[i][1] ) );
                            xys[i][2] = ( xys[i][0] / 5 + 4 * xys[i][2] / 5 ) + ( float ) ( Math.sqrt( deltaX ) * r.nextGaussian( ) / 200 );
                            xys[i][3] = ( xys[i][1] / 5 + 4 * xys[i][3] / 5 ) + ( float ) ( Math.sqrt( deltaY ) * r.nextGaussian( ) / 200 );

                            gl.glVertex4f( xys[i][0], xys[i][1], z, w );
                            gl.glVertex4f( xys[i][2], xys[i][3], z, w );
                        }
                    }
                    gl.glEnd( );
                    p3.endUse( gl );
                }

                {
                    RGBA color = colors[0];
                    gl.glColor3f( color.r, color.g, color.b );
                    int ix1 = pipelineIndex1;
                    int t1 = inputTypes[ix1];
                    Pipeline p1 = pipelines[ix1];

                    p1.beginUse( gl );
                    gl.glBegin( t1 );
                    for ( int i = 0; i < 100; i += 3 + 2 * Math.cos( i ) )
                    {

                        gl.glVertex4f( i / 10f, i / 10f, z, w );
                    }
                    gl.glEnd( );
                    p1.endUse( gl );
                }

                {
                    RGBA color = colors[1];
                    gl.glColor3f( color.r, color.g, color.b );
                    int ix2 = pipelineIndex2;
                    int t2 = inputTypes[ix2];
                    Pipeline p2 = pipelines[ix2];

                    p2.beginUse( gl );
                    gl.glBegin( t2 );
                    for ( int i = 0; i < 100; i += 5 )
                    {

                        gl.glVertex4f( ( 100 - i ) / 10f, i / 10f, z, w );
                    }
                    gl.glEnd( );
                    p2.endUse( gl );
                }
            }
        } );

        textLeft.setColor( colors[0].toFloat4( ) );
        textLeft.setHorizontalPosition( HorizontalPosition.Left );
        backgroundLayout.addPainter( textLeft );

        textRight.setColor( colors[1].toFloat4( ) );
        textRight.setHorizontalPosition( HorizontalPosition.Right );
        backgroundLayout.addPainter( textRight );

        textCenter.setColor( colors[2].toFloat4( ) );
        textCenter.setHorizontalPosition( HorizontalPosition.Center );
        textCenter.setVerticalPosition( VerticalPosition.Top );
        backgroundLayout.addPainter( textCenter );

        backgroundLayout.addPainter( new FpsPainter( ) );
        updateText( );

        backgroundLayout.addGlimpseMouseListener( new GlimpseMouseListener( )
        {

            public void mousePressed( GlimpseMouseEvent event )
            {
                EnumSet<MouseButton> buttons = event.getButtons( );
                if ( buttons.contains( MouseButton.Button1 ) )
                {
                    pipelineIndex1 = ( pipelineIndex1 + 1 ) % pipelines.length;
                }

                if ( buttons.contains( MouseButton.Button3 ) )
                {
                    pipelineIndex2 = ( pipelineIndex2 + 1 ) % pipelines.length;
                }

                if ( buttons.contains( MouseButton.Button2 ) )
                {
                    pipelineIndex3 = ( pipelineIndex3 + 1 ) % pipelines.length;
                }
                updateText( );
            }

            public void mouseReleased( GlimpseMouseEvent event )
            {
            }

            public void mouseExited( GlimpseMouseEvent event )
            {
            }

            public void mouseEntered( GlimpseMouseEvent event )
            {
            }
        } );

        AxisUtil.attachMouseListener( backgroundLayout );

        return backgroundLayout;
    }
}
