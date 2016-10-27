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
import com.metsci.glimpse.support.shader.line.LineProgram;
import com.metsci.glimpse.support.shader.line.LineStrip;
import com.metsci.glimpse.support.shader.line.LineStyle;
import com.metsci.glimpse.support.swing.NewtSwingEDTGlimpseCanvas;
import com.metsci.glimpse.support.swing.SwingEDTAnimator;

public class LineStripExample
{

    public static void main( String[] args )
    {
        final EmptyPlot2D plot = new EmptyPlot2D( );
        plot.addPainter( new BackgroundPainter( ) );
        plot.addPainter( new LineStripExamplePainter( ) );

        SwingUtilities.invokeLater( new Runnable( )
        {
            public void run( )
            {
                NewtSwingEDTGlimpseCanvas canvas = new NewtSwingEDTGlimpseCanvas( GL3 );
                canvas.addLayout( plot );
                canvas.setLookAndFeel( new SwingLookAndFeel( ) );

                GLAnimatorControl animator = new SwingEDTAnimator( 30 );
                animator.add( canvas.getGLDrawable( ) );
                animator.start( );

                JFrame frame = newFrame( "LineStripExample", canvas, DISPOSE_ON_CLOSE );
                stopOnWindowClosing( frame, animator );
                disposeOnWindowClosing( frame, canvas );
                showFrameCentered( frame );
            }
        } );
    }

    public static class LineStripExamplePainter extends GlimpsePainterBase
    {
        protected LineStrip strip;
        protected LineStyle style;
        protected LineProgram prog;

        protected long recentChange_PMILLIS = 0;
        protected final Random random = new Random( 0 );

        public LineStripExamplePainter( )
        {
            this.strip = new LineStrip( 64 );

            // Set line appearance (except for thickness, which is set in doPaintTo)
            this.style = new LineStyle( );
            style.thickness_PX = 4;
            style.joinType = JOIN_MITER;
            style.rgba = floats( 0.7f, 0, 0, 1 );
            //style.stippleEnable = true;
            //style.stippleScale = 2;
            //style.stipplePattern = 0b0001010111111111;

            // Create the shader program for drawing lines
            this.prog = new LineProgram( );
        }

        @Override
        public void doPaintTo( GlimpseContext context )
        {
            // Change the line strip periodically
            long time_PMILLIS = currentTimeMillis( );
            if ( time_PMILLIS - this.recentChange_PMILLIS > 100 )
            {
                // Append a vertex
                float x = strip.logicalSize( );
                float y = x + 10*( random.nextFloat( ) - 0.5f );
                strip.grow( 1 );
                strip.edit( 1 ).put( x ).put( y );

                // Move an existing vertex
                int iPerturb = strip.logicalSize( ) / 4;
                float xPerturb = iPerturb;
                float yPerturb = xPerturb + 10*( random.nextFloat( ) - 0.5f );
                strip.edit( iPerturb, 1 ).put( xPerturb ).put( yPerturb );

                this.recentChange_PMILLIS = time_PMILLIS;
            }

            GlimpseBounds bounds = getBounds( context );
            Axis2D axis = requireAxis2D( context );
            GL2ES3 gl = context.getGL( ).getGL2ES3( );

            enableStandardBlending( gl );
            this.prog.begin( gl );
            try
            {
                // Tell the shader program the pixel-size of our viewport
                this.prog.setViewport( gl, bounds );

                // Tell the shader program that our line coords will be in xy-axis space
                this.prog.setAxisOrtho( gl, axis );

                // Do the actual drawing
                this.prog.draw( gl, this.style, this.strip, ppvAspectRatio( axis ) );
            }
            finally
            {
                this.prog.end( gl );
                disableBlending( gl );
            }
        }

        @Override
        protected void doDispose( GlimpseContext context )
        {
            GL2ES2 gl = context.getGL( ).getGL2ES2( );
            this.strip.dispose( gl );
            this.prog.dispose( gl );
        }
    }

}
