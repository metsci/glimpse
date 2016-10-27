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
import static java.lang.Math.*;
import static javax.media.opengl.GLProfile.*;
import static javax.swing.WindowConstants.*;

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

public class LineJoinExample
{

    public static void main( String[] args )
    {
        final EmptyPlot2D plot = new EmptyPlot2D( );
        plot.getAxis( ).lockAspectRatioXY( 1.0 );
        plot.getAxis( ).set( -3, 88, -43, 5 );

        plot.addPainter( new BackgroundPainter( ) );
        plot.addPainter( new CustomLinesPainter( ) );

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

                JFrame frame = newFrame( "LineJoinExample", canvas, DISPOSE_ON_CLOSE );
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
            // Generate a LinePath with joins at lots of different angles
            this.path = new LinePath( );
            int ni = 360;
            float xNext = 0f;
            float yNext = 0f;
            for ( int i = 0; i < ni; i++ )
            {
                double angle_CCWRAD = 0.5*PI - ( 2*PI * i ) / ni;
                double dx = cos( angle_CCWRAD );
                double dy = sin( angle_CCWRAD );

                float x0 = ( float ) ( xNext + max( 0, -dx ) );
                float y0 = yNext;

                float x1 = x0;
                float y1 = y0 + 1f;

                float x2 = ( float ) ( x1 + dx );
                float y2 = ( float ) ( y1 + dy );

                path.moveTo( x0, y0 );
                path.lineTo( x1, y1 );
                path.lineTo( x2, y2 );

                xNext = max( x1, x2 ) + 2f;

                if ( xNext > 85f )
                {
                    yNext -= 4f;
                    xNext = 0f;
                }
            }

            // Set line appearance
            this.style = new LineStyle( );
            style.thickness_PX = 5;
            style.feather_PX = 2;
            style.joinType = JOIN_MITER;
            style.rgba = floats( 0.7f, 0, 0, 0.5f );

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
