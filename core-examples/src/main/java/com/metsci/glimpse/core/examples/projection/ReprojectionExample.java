/*
 * Copyright (c) 2020, Metron, Inc.
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
package com.metsci.glimpse.core.examples.projection;

import static com.metsci.glimpse.core.gl.util.GLUtils.getDefaultGLProfile;
import static com.metsci.glimpse.core.gl.util.GLUtils.newOffscreenDrawable;
import static com.metsci.glimpse.core.support.DisposableUtils.onWindowClosed;
import static com.metsci.glimpse.core.support.FrameUtils.fireWindowClosing;
import static com.metsci.glimpse.core.support.QuickUtils.quickDefaultAnimator;
import static com.metsci.glimpse.core.support.QuickUtils.quickGlimpseWindow;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLProfile;
import com.metsci.glimpse.core.axis.Axis2D;
import com.metsci.glimpse.core.axis.AxisUtil;
import com.metsci.glimpse.core.canvas.FBOGlimpseCanvas;
import com.metsci.glimpse.core.context.GlimpseContext;
import com.metsci.glimpse.core.examples.heatmap.HeatMapExample;
import com.metsci.glimpse.core.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.core.layout.GlimpseLayout;
import com.metsci.glimpse.core.painter.base.GlimpsePainter;
import com.metsci.glimpse.core.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.core.painter.texture.ShadedTexturePainter;
import com.metsci.glimpse.core.plot.ColorAxisPlot2D;
import com.metsci.glimpse.core.support.projection.PolarProjection;
import com.metsci.glimpse.core.support.projection.Projection;
import com.metsci.glimpse.core.support.settings.SwingLookAndFeel;
import com.metsci.glimpse.core.support.shader.triangle.ColorTexture2DProgram;
import com.metsci.glimpse.core.support.swing.NewtSwingEDTGlimpseCanvas;
import com.metsci.glimpse.core.support.swing.SwingEDTAnimator;
import com.metsci.glimpse.core.support.texture.TextureProjected2D;

/**
 * Demonstrates using Glimpse offscreen rendering to distort an existing Glimpse plot.
 *
 * @author ulman
 */
public class ReprojectionExample
{
    public static void main( String[] args ) throws Exception
    {
        SwingUtilities.invokeLater( ( ) ->
        {
            GLProfile glProfile = getDefaultGLProfile( );
            GLContext glContext = newOffscreenDrawable( glProfile ).getContext( );

            // Ordinary heatmap plot
            ColorAxisPlot2D heatmapPlot = HeatMapExample.newHeatMapPlot( );

            // Reprojected version of the heatmap plot
            Projection proj = new PolarProjection( 0, 10, 0, 360 );
            Axis2D reprojAxis = new Axis2D( );
            reprojAxis.set( -10, 10, -10, 10 );
            GlimpseAxisLayout2D reprojPlot = new GlimpseAxisLayout2D( reprojAxis );
            AxisUtil.attachMouseListener( reprojPlot );
            reprojPlot.addPainter( new BackgroundPainter( true ) );
            reprojPlot.addPainter( createReprojectingPainter( glContext, heatmapPlot, proj ) );

            // Canvas with ordinary plot
            NewtSwingEDTGlimpseCanvas canvas1 = new NewtSwingEDTGlimpseCanvas( glContext );
            canvas1.addLayout( heatmapPlot );
            canvas1.setLookAndFeel( new SwingLookAndFeel( ) );

            // Canvas with reprojected plot
            NewtSwingEDTGlimpseCanvas canvas2 = new NewtSwingEDTGlimpseCanvas( glContext );
            canvas2.addLayout( reprojPlot );
            canvas2.setLookAndFeel( new SwingLookAndFeel( ) );

            // Animator
            SwingEDTAnimator animator = quickDefaultAnimator( );
            animator.add( canvas1.getGLDrawable( ) );
            animator.add( canvas2.getGLDrawable( ) );
            animator.start( );

            // Windows
            JFrame frame1 = quickGlimpseWindow( "Original", canvas1 );
            JFrame frame2 = quickGlimpseWindow( "Reprojected", canvas2 );
            onWindowClosed( frame1, ev -> fireWindowClosing( frame2 ) );
            onWindowClosed( frame2, ev -> fireWindowClosing( frame1 ) );
            frame1.setLocation( frame1.getX( )-100, frame1.getY( )-80 );
            frame2.setLocation( frame2.getX( )+100, frame2.getY( )+80 );
        } );
    }

    public static GlimpsePainter createReprojectingPainter( GLContext glContext, GlimpseLayout origLayout, Projection proj )
    {
        return new ShadedTexturePainter( )
        {
            FBOGlimpseCanvas offscreen;
            TextureProjected2D texture;

            // Instance initializer
            {
                this.setProgram( new ColorTexture2DProgram( ) );

                this.offscreen = new FBOGlimpseCanvas( glContext, 800, 800 );
                this.offscreen.addLayout( origLayout );
                this.texture = null;
            }

            @Override
            public void doPaintTo( GlimpseContext context )
            {
                this.offscreen.paint( );
                if ( this.offscreen.getGLDrawable( ).isInitialized( ) && this.texture == null )
                {
                    this.texture = this.offscreen.getProjectedTexture( );
                    this.texture.setProjection( proj );
                    this.addDrawableTexture( this.texture );
                }
                super.doPaintTo( context );
            }

            @Override
            public void doDispose( GlimpseContext context )
            {
                super.doDispose( context );
                this.texture.dispose( context.getGLContext( ) );
                this.offscreen.destroy( );
            }
        };
    }
}
