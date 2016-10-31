package com.metsci.glimpse.text;

import static com.metsci.glimpse.support.FrameUtils.*;
import static com.metsci.glimpse.support.font.FontUtils.*;
import static java.awt.Color.*;
import static javax.swing.WindowConstants.*;

import javax.media.opengl.GLAnimatorControl;
import javax.media.opengl.GLProfile;
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
