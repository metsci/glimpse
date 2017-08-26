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
package com.metsci.glimpse.text;

import static com.metsci.glimpse.support.FrameUtils.*;
import static com.metsci.glimpse.support.font.FontUtils.*;
import static java.awt.Color.*;
import static javax.swing.WindowConstants.*;

import com.jogamp.opengl.GLAnimatorControl;
import com.jogamp.opengl.GLProfile;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.metsci.glimpse.com.jogamp.opengl.util.awt.TextRenderer;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.plot.EmptyPlot2D;
import com.metsci.glimpse.support.settings.SwingLookAndFeel;
import com.metsci.glimpse.support.swing.NewtSwingEDTGlimpseCanvas;
import com.metsci.glimpse.support.swing.SwingEDTAnimator;

public class GarbledTextTest
{

    public static void main( String[] args )
    {
        SwingUtilities.invokeLater( new Runnable( )
        {
            public void run( )
            {
                final TestPainter testPainter = new TestPainter( );

                EmptyPlot2D plot = new EmptyPlot2D( );
                plot.addPainter( new BackgroundPainter( ) );
                plot.addPainter( testPainter );

                NewtSwingEDTGlimpseCanvas canvas = new NewtSwingEDTGlimpseCanvas( GLProfile.GL3 );
                canvas.addLayout( plot );
                canvas.setLookAndFeel( new SwingLookAndFeel( ) );

                GLAnimatorControl animator = new SwingEDTAnimator( 1000 );
                animator.add( canvas.getGLDrawable( ) );
                animator.start( );

                JFrame frame = newFrame( "GarbledTextTest", canvas, DISPOSE_ON_CLOSE );
                stopOnWindowClosing( frame, animator );
                disposeOnWindowClosing( frame, canvas );
                showFrameCentered( frame );
            }
        } );
    }

    protected static class TestPainter extends GlimpsePainterBase
    {
        public final TextRenderer textRenderer;
        protected int offset;

        public TestPainter( )
        {
            this.textRenderer = new TextRenderer( getDefaultBold( 12 ), true, false );
            this.offset = 0;
        }

        @Override
        public void doPaintTo( GlimpseContext context )
        {
            // At some point the glyph atlas will get repacked, resulting in
            // some garbled glyphs. It's helpful to stop incrementing offset
            // at that point, so that the garbled glyphs stay visible on the
            // screen.
            //
            // However, the atlas's initial size varies from system to system,
            // so the point at which a repack becomes necessary varies as well.
            //
            // One way to tell when the repack happens is to set a breakpoint
            // in com.jogamp.opengl.util.packrect.Level.compact(), where it
            // calls Rect.setPosition(). Then comment out the "if" line here,
            // so that offset will keep increasing until the breakpoint is hit.
            // Then replace the "7500" here with the offset value at which the
            // repack occurred.
            //
            if ( this.offset < 7500 )
            {
                this.offset = ( this.offset + 1 ) % 15000;
            }

            GlimpseBounds bounds = getBounds( context );
            this.textRenderer.beginRendering( bounds.getWidth( ), bounds.getHeight( ) );
            try
            {
                int ni = 10;
                int nj = 10;

                this.textRenderer.setColor( BLACK );
                for ( int j = 0; j < nj; j++ )
                {
                    for ( int i = 0; i < ni; i++ )
                    {
                        String s = Character.toString( ( char ) ( offset/100 + j*ni + i ) );
                        this.textRenderer.draw( s, 20 + 20*i, bounds.getHeight( ) - ( 20 + 15*j ) );
                    }
                }
            }
            finally
            {
                this.textRenderer.endRendering( );
            }
        }

        @Override
        protected void doDispose( GlimpseContext context )
        {
            this.textRenderer.dispose( );
        }
    }

}
