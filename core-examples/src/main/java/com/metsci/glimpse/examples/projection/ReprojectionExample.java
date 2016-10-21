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
package com.metsci.glimpse.examples.projection;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.GLContext;
import javax.media.opengl.GLOffscreenAutoDrawable;
import javax.media.opengl.GLProfile;
import javax.swing.JFrame;

import com.jogamp.opengl.util.FPSAnimator;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.AxisUtil;
import com.metsci.glimpse.canvas.FBOGlimpseCanvas;
import com.metsci.glimpse.canvas.NewtSwingGlimpseCanvas;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.examples.heatmap.HeatMapExample;
import com.metsci.glimpse.gl.util.GLUtils;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.painter.texture.ShadedTexturePainter;
import com.metsci.glimpse.plot.ColorAxisPlot2D;
import com.metsci.glimpse.support.projection.PolarProjection;
import com.metsci.glimpse.support.settings.SwingLookAndFeel;
import com.metsci.glimpse.support.shader.triangle.ColorTexture2DProgram;
import com.metsci.glimpse.support.texture.TextureProjected2D;
import com.metsci.glimpse.util.geo.projection.TangentPlane;

/**
 * Demonstrates using Glimpse offscreen rendering to distort an existing Glimpse plot.
 * This is a rather silly example, but this capability is used by
 * {@link com.metsci.glimpse.examples.worldwind.BathymetryTileExample}
 * to reproject Glimpse rendering performed using a {@link TangentPlane} onto the
 * WorldWind globe, which expects a {@link PlateCarreeProjection}.
 *
 * @author ulman
 */
public class ReprojectionExample
{
    public static void main( String[] args ) throws Exception
    {
        GLProfile glProfile = GLUtils.getDefaultGLProfile( );

        GLOffscreenAutoDrawable glDrawable = GLUtils.newOffscreenDrawable( glProfile );
        GLContext glContext = glDrawable.getContext( );

        final NewtSwingGlimpseCanvas canvas = new NewtSwingGlimpseCanvas( glContext );
        ColorAxisPlot2D layout = new HeatMapExample( ).getLayout( );
        canvas.addLayout( layout );
        canvas.setLookAndFeel( new SwingLookAndFeel( ) );

        final FBOGlimpseCanvas offscreenCanvas = new FBOGlimpseCanvas( glContext, 800, 800 );
        offscreenCanvas.addLayout( layout );

        final NewtSwingGlimpseCanvas canvas2 = new NewtSwingGlimpseCanvas( glContext );
        canvas2.addLayout( new ReprojectionExample( ).getLayout( offscreenCanvas ) );
        canvas2.setLookAndFeel( new SwingLookAndFeel( ) );

        // attach a repaint manager which repaints the canvas in a loop
        FPSAnimator animator = new FPSAnimator( 120 );
        animator.add( offscreenCanvas.getGLDrawable( ) );
        animator.add( canvas2.getGLDrawable( ) );
        animator.add( canvas.getGLDrawable( ) );
        animator.start( );

        createFrame( "Original", canvas );
        createFrame( "Reprojected", canvas2 );

        Runtime.getRuntime( ).addShutdownHook( new Thread( )
        {
            @Override
            public void run( )
            {
                offscreenCanvas.disposeAttached( );
            }
        } );
    }

    public static JFrame createFrame( String name, final NewtSwingGlimpseCanvas canvas )
    {
        final JFrame frame = new JFrame( name );

        frame.addWindowListener( new WindowAdapter( )
        {
            @Override
            public void windowClosing( WindowEvent e )
            {
                // dispose of resources associated with the canvas
                canvas.disposeAttached( );

                // remove the canvas from the frame
                frame.remove( canvas );
            }
        } );

        frame.add( canvas );

        frame.pack( );
        frame.setSize( 800, 800 );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.setVisible( true );

        return frame;
    }

    public GlimpseLayout getLayout( final FBOGlimpseCanvas offscreenCanvas ) throws Exception
    {
        Axis2D axis = new Axis2D( );
        axis.set( -10, 10, -10, 10 );
        GlimpseAxisLayout2D layout2 = new GlimpseAxisLayout2D( axis );
        AxisUtil.attachMouseListener( layout2 );

        ShadedTexturePainter painter = new ShadedTexturePainter( )
        {
            boolean initialized = false;

            @Override
            public void doPaintTo( GlimpseContext context )
            {
                if ( !initialized && offscreenCanvas.getGLDrawable( ).isInitialized( ) )
                {
                    TextureProjected2D texture = offscreenCanvas.getProjectedTexture( );
                    texture.setProjection( new PolarProjection( 0, 10, 0, 360 ) );
                    addDrawableTexture( texture );

                    initialized = true;
                }

                super.doPaintTo( context );
            }
        };

        ColorTexture2DProgram program = new ColorTexture2DProgram( );
        painter.setProgram( program );

        layout2.addPainter( new BackgroundPainter( true ) );
        layout2.addPainter( painter );

        return layout2;
    }
}
