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
package com.metsci.glimpse.timing;

import static com.metsci.glimpse.support.FrameUtils.*;
import static com.metsci.glimpse.timing.GLVersionLogger.*;
import static com.metsci.glimpse.util.logging.LoggerUtils.*;
import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.*;
import static javax.swing.WindowConstants.*;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAnimatorControl;
import com.jogamp.opengl.GLProfile;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.plot.EmptyPlot2D;
import com.metsci.glimpse.support.settings.SwingLookAndFeel;
import com.metsci.glimpse.support.swing.NewtSwingEDTGlimpseCanvas;
import com.metsci.glimpse.support.swing.SwingEDTAnimator;


public class ImmediateModeTimingTest
{

    public static void main( String[] args )
    {
        initializeLogging( "timing/logging.properties" );

        final EmptyPlot2D plot = new EmptyPlot2D( );
        plot.addPainter( new BackgroundPainter( ) );
        plot.addPainter( new TestPainter( ) );
        plot.addPainter( new FpsPrinter( ) );

        SwingUtilities.invokeLater( new Runnable( )
        {
            public void run( )
            {
                NewtSwingEDTGlimpseCanvas canvas = new NewtSwingEDTGlimpseCanvas( GLProfile.GL2 );
                addGLVersionLogger( canvas );
                canvas.addLayout( plot );
                canvas.setLookAndFeel( new SwingLookAndFeel( ) );

                GLAnimatorControl animator = new SwingEDTAnimator( 1000 );
                animator.add( canvas.getGLDrawable( ) );
                animator.start( );

                JFrame frame = newFrame( "ImmediateModeTimingTest", canvas, DISPOSE_ON_CLOSE );
                stopOnWindowClosing( frame, animator );
                disposeOnWindowClosing( frame, canvas );
                showFrameCentered( frame );
            }
        } );
    }

    protected static class TestPainter extends GlimpsePainterBase
    {
        protected static final int numIterations = 10000;
        protected static final int verticesPerIteration = 4;

        public TestPainter( )
        { }

        @Override
        public void doPaintTo( GlimpseContext context )
        {
            GlimpseBounds bounds = getBounds( context );

            GL2 gl = context.getGL( ).getGL2( );
            gl.glColor4f( 0, 0, 0, 1 );

            gl.glMatrixMode( GL_PROJECTION );
            gl.glLoadIdentity( );
            gl.glOrtho( 0, bounds.getWidth( ), 0, bounds.getHeight( ), -1, 1 );

            for ( int i = 0; i < numIterations; i++ )
            {
                gl.glBegin( GL_POINTS );

                for ( int v = 0; v < verticesPerIteration; v++ )
                {
                    float x = 2 + v + 3*i;
                    float y = 2 + v;
                    gl.glVertex2f( x, y );
                }

                gl.glEnd( );
            }
        }

        @Override
        protected void doDispose( GlimpseContext context )
        { }
    }

}
