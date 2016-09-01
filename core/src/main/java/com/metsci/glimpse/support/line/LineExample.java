package com.metsci.glimpse.support.line;

import static com.metsci.glimpse.support.FrameUtils.disposeOnWindowClosing;
import static com.metsci.glimpse.support.FrameUtils.newFrame;
import static com.metsci.glimpse.support.FrameUtils.showFrameCentered;
import static com.metsci.glimpse.support.FrameUtils.stopOnWindowClosing;
import static javax.media.opengl.GL.GL_LINES;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

import javax.media.opengl.GL;
import javax.media.opengl.GLAnimatorControl;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.painter.base.GlimpsePainter2D;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.plot.Plot2D;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.settings.SwingLookAndFeel;
import com.metsci.glimpse.support.swing.NewtSwingEDTGlimpseCanvas;
import com.metsci.glimpse.support.swing.SwingEDTAnimator;

public class LineExample
{

    public static void main( String[] args )
    {
        final Plot2D plot = new Plot2D( "" );
        plot.setAxisSizeZ( 0 );
        plot.setTitleHeight( 0 );

        plot.getLayoutCenter( ).addPainter( new BackgroundPainter( ) );



        plot.getLayoutCenter( ).addPainter( new GlimpsePainter2D( )
        {
            float lineThickness_PX = 5;

            LineProgram prog;

            {
                this.prog = new LineProgram( );
                prog.setLineThickness( lineThickness_PX );
                prog.setFeatherThickness( 1f );
                prog.setColor( GlimpseColor.getBlack( ) );
            }

            public void paintTo( GlimpseContext context, GlimpseBounds bounds, Axis2D axis )
            {
                prog.setViewport( bounds );
                prog.setPixelOrtho( bounds );

                float xLeft_PX = ( float ) ( 0.5*lineThickness_PX );
                float xRight_PX = ( float ) ( bounds.getWidth( ) - 0.5*lineThickness_PX );
                float yBottom_PX = ( float ) ( 0.5*lineThickness_PX );
                float yTop_PX = ( float ) ( bounds.getHeight( ) - 0.5*lineThickness_PX );

                prog.vertices.seal( false );

                prog.vertices.clear( );
                prog.vertices.addVertex( xLeft_PX,  yBottom_PX );
                prog.vertices.addVertex( xRight_PX, yBottom_PX );
                prog.vertices.addVertex( xRight_PX, yTop_PX    );
                prog.vertices.addVertex( xLeft_PX,  yTop_PX    );
                prog.vertices.addVertex( xLeft_PX,  yBottom_PX );

                prog.vertices.seal( true );

                GL gl = context.getGL( );

                gl.glBlendFuncSeparate( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA, GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA );
                gl.glEnable( GL.GL_BLEND );

                prog.useProgram( gl, true );
                gl.glDrawArrays( GL_LINES, 0, 8 );
                prog.useProgram( gl, false );
            }
        } );



        SwingUtilities.invokeLater( new Runnable( )
        {
            public void run( )
            {
                NewtSwingEDTGlimpseCanvas canvas = new NewtSwingEDTGlimpseCanvas( );
                canvas.addLayout( plot );
                canvas.setLookAndFeel( new SwingLookAndFeel( ) );

                GLAnimatorControl animator = new SwingEDTAnimator( 30 );
                animator.add( canvas.getGLDrawable( ) );
                animator.start( );

                JFrame frame = newFrame( "Line Example", canvas, DISPOSE_ON_CLOSE );
                stopOnWindowClosing( frame, animator );
                disposeOnWindowClosing( frame, canvas );
                showFrameCentered( frame );
            }
        } );
    }

}
