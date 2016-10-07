package com.metsci.glimpse.examples.basic;

import static com.metsci.glimpse.gl.util.GLUtils.*;
import static com.metsci.glimpse.support.FrameUtils.*;
import static com.metsci.glimpse.support.shader.line.LineJoinType.*;
import static com.metsci.glimpse.support.shader.line.LineUtils.*;
import static com.metsci.glimpse.util.GeneralUtils.*;
import static java.nio.FloatBuffer.*;
import static java.util.concurrent.Executors.*;
import static java.util.concurrent.TimeUnit.*;
import static javax.media.opengl.GLProfile.*;
import static javax.swing.WindowConstants.*;

import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;

import javax.media.opengl.GL2ES2;
import javax.media.opengl.GL2ES3;
import javax.media.opengl.GLAnimatorControl;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
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

        public LineStripExamplePainter( )
        {
            this.strip = new LineStrip( 64 );


            final Random r = new Random( 0 );
            Runnable addVertex = new Runnable( )
            {
                float x = 0;
                float y = 0;

                public void run( )
                {
                    SwingUtilities.invokeLater( new Runnable( )
                    {
                        public void run( )
                        {
                            strip.put( 0, wrap( floats( r.nextFloat( ) - 0.5f, r.nextFloat( ) - 0.5f ) ) );

                            strip.grow( 2 );
                            strip.put( wrap( floats( x, y ) ) );
                            x += r.nextFloat( );
                            y += 3 * ( r.nextFloat( ) - 0.5f );
                        }
                    } );
                }
            };
            ScheduledExecutorService scheduler = newSingleThreadScheduledExecutor( new ThreadFactoryBuilder( ).setDaemon( true ).build( ) );
            scheduler.scheduleWithFixedDelay( addVertex, 250, 250, MILLISECONDS );


            // Set line appearance (except for thickness, which is set in doPaintTo)
            this.style = new LineStyle( );
            style.thickness_PX = 4;
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
