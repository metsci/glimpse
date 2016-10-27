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
package com.metsci.glimpse.examples.line;

import static com.metsci.glimpse.gl.util.GLUtils.*;
import static com.metsci.glimpse.support.FrameUtils.*;
import static com.metsci.glimpse.support.shader.line.LineJoinType.*;
import static com.metsci.glimpse.support.shader.line.LineUtils.*;
import static com.metsci.glimpse.util.GeneralUtils.*;
import static java.lang.System.*;
import static javax.media.opengl.GLProfile.*;
import static javax.swing.WindowConstants.*;

import java.util.Random;

import javax.media.opengl.GL2ES2;
import javax.media.opengl.GL2ES3;
import javax.media.opengl.GLAnimatorControl;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.plot.EmptyPlot2D;
import com.metsci.glimpse.support.settings.SwingLookAndFeel;
import com.metsci.glimpse.support.shader.line.LinePath;
import com.metsci.glimpse.support.shader.line.LineProgram;
import com.metsci.glimpse.support.shader.line.LineStyle;
import com.metsci.glimpse.support.swing.NewtSwingEDTGlimpseCanvas;
import com.metsci.glimpse.support.swing.SwingEDTAnimator;

public class LinePathExample
{

    public static void main( String[] args ) throws Exception
    {
        final EmptyPlot2D plot = new EmptyPlot2D( );
        plot.addPainter( new BackgroundPainter( ) );
        plot.addPainter( new CustomLinesPainter( ) );

        SwingUtilities.invokeLater( new Runnable( )
        {
            @Override
            public void run( )
            {
                NewtSwingEDTGlimpseCanvas canvas = new NewtSwingEDTGlimpseCanvas( GL3 );
                canvas.addLayout( plot );
                canvas.setLookAndFeel( new SwingLookAndFeel( ) );

                GLAnimatorControl animator = new SwingEDTAnimator( 30 );
                animator.add( canvas.getGLDrawable( ) );
                animator.start( );

                JFrame frame = newFrame( "LinePathExample", canvas, DISPOSE_ON_CLOSE );
                stopOnWindowClosing( frame, animator );
                disposeOnWindowClosing( frame, canvas );
                showFrameCentered( frame );
            }
        } );
    }

    public static class CustomLinesPainter extends GlimpsePainterBase
    {
        protected LinePath path;
        protected LineStyle style;
        protected LineProgram prog;

        public CustomLinesPainter( )
        {
            // Generate a random LinePath
            this.path = new LinePath( );
            Random r = new Random( 0 );
            for ( int i = 0; i < 25; i++ )
            {
                // Make sure we get a few lines that are exactly horizontal or vertical
                boolean axisAlign = ( i % 3 == 0 );

                float x0 = 2 + 6 * r.nextFloat( );
                float y0 = 2 + 6 * r.nextFloat( );

                float x1 = x0 + ( -1 + 2 * r.nextFloat( ) );
                float y1 = ( axisAlign ? y0 : y0 + ( -1 + 2 * r.nextFloat( ) ) );

                float x2 = ( axisAlign ? x1 : x1 + ( -1 + 2 * r.nextFloat( ) ) );
                float y2 = y1 + ( -1 + 2 * r.nextFloat( ) );

                path.moveTo( x0, y0 );
                path.lineTo( x1, y1 );
                path.lineTo( x2, y2 );

                // Make some of the line-strips closed loops
                if ( i % 2 == 0 )
                {
                    path.closeLoop( );
                }
            }

            // Set line appearance (except for thickness, which is set in doPaintTo)
            this.style = new LineStyle( );
            style.joinType = JOIN_MITER;
            style.rgba = floats( 0.7f, 0, 0, 1 );
            style.stippleEnable = true;
            style.stippleScale = 2;
            style.stipplePattern = 0b0001010111111111;

            // Create the shader program for drawing lines
            this.prog = new LineProgram( );
        }

        @Override
        public void doPaintTo( GlimpseContext context )
        {
            GlimpseBounds bounds = getBounds( context );
            Axis2D axis = requireAxis2D( context );
            GL2ES3 gl = context.getGL( ).getGL2ES3( );

            enableStandardBlending( gl );
            prog.begin( gl );
            try
            {
                // Tell the shader program the pixel-size of our viewport
                prog.setViewport( gl, bounds );

                // Tell the shader program that our line coords will be in xy-axis space
                prog.setAxisOrtho( gl, axis );

                // Make the lines pulsate, by changing thickness over time       return null;
                {
                    float maxThickness_PX = 10;
                    float minThickness_PX = 2;
                    long halfPeriod_MILLIS = 800;

                    long t = currentTimeMillis( );
                    long stepNum = t / halfPeriod_MILLIS;
                    float stepFrac = ( t % halfPeriod_MILLIS ) / ( ( float ) halfPeriod_MILLIS );
                    if ( stepNum % 2 == 0 )
                    {
                        style.thickness_PX = minThickness_PX + ( maxThickness_PX - minThickness_PX ) * stepFrac;
                    }
                    else
                    {
                        style.thickness_PX = maxThickness_PX - ( maxThickness_PX - minThickness_PX ) * stepFrac;
                    }
                }

                // Do the actual drawing
                prog.draw( gl, style, path, ppvAspectRatio( axis ) );
            }
            finally
            {
                prog.end( gl );
                disableBlending( gl );
            }
        }

        @Override
        protected void doDispose( GlimpseContext context )
        {
            GL2ES2 gl = context.getGL( ).getGL2ES2( );
            this.path.dispose( gl );
            this.prog.dispose( gl );
        }
    }
}
