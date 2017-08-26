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

import static com.jogamp.common.nio.Buffers.*;
import static com.metsci.glimpse.support.FrameUtils.*;
import static com.metsci.glimpse.timing.GLVersionLogger.*;
import static com.metsci.glimpse.util.buffer.DirectBufferUtils.*;
import static com.metsci.glimpse.util.logging.LoggerUtils.*;
import static com.jogamp.opengl.GL.*;
import static javax.swing.WindowConstants.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAnimatorControl;
import com.jogamp.opengl.GLProfile;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.GLEditableBuffer;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.plot.EmptyPlot2D;
import com.metsci.glimpse.support.settings.SwingLookAndFeel;
import com.metsci.glimpse.support.shader.triangle.FlatColorProgram;
import com.metsci.glimpse.support.shader.triangle.FlatColorProgram.ProgramHandles;
import com.metsci.glimpse.support.swing.NewtSwingEDTGlimpseCanvas;
import com.metsci.glimpse.support.swing.SwingEDTAnimator;


public class GLMultiTimingTest
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
                NewtSwingEDTGlimpseCanvas canvas = new NewtSwingEDTGlimpseCanvas( GLProfile.GL3 );
                addGLVersionLogger( canvas );
                canvas.addLayout( plot );
                canvas.setLookAndFeel( new SwingLookAndFeel( ) );

                GLAnimatorControl animator = new SwingEDTAnimator( 1000 );
                animator.add( canvas.getGLDrawable( ) );
                animator.start( );

                JFrame frame = newFrame( "GLMultiTimingTest", canvas, DISPOSE_ON_CLOSE );
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
        protected static final int floatsPerIteration = 2 * verticesPerIteration;
        protected static final int bytesPerIteration = SIZEOF_FLOAT * floatsPerIteration;

        protected final FlatColorProgram prog;
        protected final GLEditableBuffer buffer;
        protected final IntBuffer firsts;
        protected final IntBuffer counts;

        public TestPainter( )
        {
            this.prog = new FlatColorProgram( );
            this.buffer = new GLEditableBuffer( GL_DYNAMIC_DRAW, bytesPerIteration * numIterations );
            this.firsts = newDirectIntBuffer( numIterations );
            this.counts = newDirectIntBuffer( numIterations );
        }

        @Override
        public void doPaintTo( GlimpseContext context )
        {
            GL3 gl = context.getGL( ).getGL3( );
            GlimpseBounds bounds = getBounds( context );

            this.buffer.clear( );
            this.firsts.clear( );
            this.counts.clear( );

            for ( int i = 0; i < numIterations; i++ )
            {
                firsts.put( i * verticesPerIteration );
                counts.put( verticesPerIteration );

                FloatBuffer editFloats = this.buffer.editFloats( i * floatsPerIteration, floatsPerIteration );
                for ( int v = 0; v < verticesPerIteration; v++ )
                {
                    float x = 2 + v + 3*i;
                    float y = 2 + v;
                    editFloats.put( x ).put( y );
                }
            }

            this.prog.begin( gl );
            this.prog.setColor( gl, 0, 0, 0, 1 );
            this.prog.setPixelOrtho( gl, bounds );

            int b = this.buffer.deviceBuffer( gl );
            gl.glBindBuffer( GL_ARRAY_BUFFER, b );

            ProgramHandles h = this.prog.handles( gl );
            gl.glVertexAttribPointer( h.inXy, 2, GL_FLOAT, false, 0, 0 );

            gl.glMultiDrawArrays( GL_POINTS, flipped( this.firsts ), flipped( this.counts ), numIterations );

            this.prog.end( gl );
        }

        @Override
        protected void doDispose( GlimpseContext context )
        {
            GL gl = context.getGL( );
            this.buffer.dispose( gl );
        }
    }

}
