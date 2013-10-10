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
package com.metsci.glimpse.examples.projection;

import static com.metsci.glimpse.gl.util.GLPBufferUtils.*;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.GLContext;
import javax.swing.JFrame;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.AxisUtil;
import com.metsci.glimpse.canvas.FrameBufferGlimpseCanvas;
import com.metsci.glimpse.canvas.NewtSwingGlimpseCanvas;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.examples.basic.HeatMapExample;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.painter.texture.ShadedTexturePainter;
import com.metsci.glimpse.plot.ColorAxisPlot2D;
import com.metsci.glimpse.support.projection.PolarProjection;
import com.metsci.glimpse.support.repaint.NEWTRepaintManager;
import com.metsci.glimpse.support.repaint.RepaintManager;
import com.metsci.glimpse.support.settings.SwingLookAndFeel;
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
        GLContext context = createPixelBuffer( 1, 1 ).getContext( );
        final NewtSwingGlimpseCanvas canvas = new NewtSwingGlimpseCanvas( context );
        ColorAxisPlot2D layout = new HeatMapExample( ).getLayout( );
        canvas.addLayout( layout );
        canvas.setLookAndFeel( new SwingLookAndFeel( ) );

        final RepaintManager manager = NEWTRepaintManager.newRepaintManager( canvas );

        final FrameBufferGlimpseCanvas offscreenCanvas = new FrameBufferGlimpseCanvas( 800, 800, context );
        offscreenCanvas.addLayout( layout );
        manager.addGlimpseCanvas( offscreenCanvas );

        final NewtSwingGlimpseCanvas canvas2 = new NewtSwingGlimpseCanvas( context );
        canvas2.addLayout( new ReprojectionExample( ).getLayout( offscreenCanvas ) );
        canvas2.setLookAndFeel( new SwingLookAndFeel( ) );
        manager.addGlimpseCanvas( canvas2 );

        createFrame( "Original", canvas );
        createFrame( "Reprojected", canvas2 );

        Runtime.getRuntime( ).addShutdownHook( new Thread( )
        {
            @Override
            public void run( )
            {
                offscreenCanvas.dispose( );
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
                canvas.dispose( );

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

    public GlimpseLayout getLayout( final FrameBufferGlimpseCanvas offscreenCanvas ) throws Exception
    {
        Axis2D axis = new Axis2D( );
        axis.set( -10, 10, -10, 10 );
        GlimpseAxisLayout2D layout2 = new GlimpseAxisLayout2D( axis );
        AxisUtil.attachMouseListener( layout2 );

        ShadedTexturePainter painter = new ShadedTexturePainter( )
        {
            boolean initialized = false;

            @Override
            public void paintTo( GlimpseContext context, GlimpseBounds bounds, Axis2D axis )
            {
                super.paintTo( context, bounds, axis );

                if ( !initialized && offscreenCanvas.getFrameBuffer( ).isInitialized( ) )
                {
                    TextureProjected2D texture = offscreenCanvas.getGlimpseTexture( );
                    texture.setProjection( new PolarProjection( 0, 10, 0, 360 ) );
                    addDrawableTexture( texture );
                    initialized = true;
                }
            }
        };

        layout2.addPainter( new BackgroundPainter( true ) );
        layout2.addPainter( painter );

        return layout2;
    }
}
