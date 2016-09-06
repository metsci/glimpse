package com.metsci.glimpse.support.line.example;

import static com.metsci.glimpse.support.FrameUtils.disposeOnWindowClosing;
import static com.metsci.glimpse.support.FrameUtils.newFrame;
import static com.metsci.glimpse.support.FrameUtils.showFrameCentered;
import static com.metsci.glimpse.support.FrameUtils.stopOnWindowClosing;
import static com.metsci.glimpse.support.line.util.LineUtils.distance;
import static com.metsci.glimpse.support.line.util.LineUtils.enableStandardBlending;
import static com.metsci.glimpse.support.line.util.LineUtils.ppvAspectRatio;
import static com.metsci.glimpse.util.GeneralUtils.floats;
import static javax.media.opengl.GL.GL_ARRAY_BUFFER;
import static javax.media.opengl.GL2ES2.GL_STREAM_DRAW;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

import java.nio.FloatBuffer;
import java.util.Random;

import javax.media.opengl.GL2ES2;
import javax.media.opengl.GLAnimatorControl;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.painter.base.GlimpsePainter2D;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.plot.Plot2D;
import com.metsci.glimpse.support.line.LineProgram;
import com.metsci.glimpse.support.line.LineStyle;
import com.metsci.glimpse.support.line.util.MappableBuffer;
import com.metsci.glimpse.support.settings.SwingLookAndFeel;
import com.metsci.glimpse.support.swing.NewtSwingEDTGlimpseCanvas;
import com.metsci.glimpse.support.swing.SwingEDTAnimator;

public class LineExample2
{

    public static void put1f( FloatBuffer buffer, double a )
    {
        buffer.put( ( float ) a );
    }

    public static void put2f( FloatBuffer buffer, double a, double b )
    {
        buffer.put( ( float ) a )
              .put( ( float ) b );
    }

    public static void main( String[] args )
    {
        final Plot2D plot = new Plot2D( "" );
        plot.setAxisSizeZ( 0 );
        plot.setTitleHeight( 0 );

        plot.getLayoutCenter( ).addPainter( new BackgroundPainter( ) );



        plot.getLayoutCenter( ).addPainter( new GlimpsePainter2D( )
        {
            LineStyle style = new LineStyle( );
            LineProgram prog = null;

            MappableBuffer xyVbo = new MappableBuffer( GL_ARRAY_BUFFER, GL_STREAM_DRAW, 10 );
            MappableBuffer mileageVbo = new MappableBuffer( GL_ARRAY_BUFFER, GL_STREAM_DRAW, 10 );
            int numVertices = 0;

            {
                style.rgba = floats( 0.7f, 0, 0, 1 );
                style.thickness_PX = 4;
                style.stippleEnable = true;
                style.stippleScale = 2;
                style.stipplePattern = 0b0001010111111111;
            }

            public void paintTo( GlimpseContext context, GlimpseBounds bounds, Axis2D axis )
            {
                GL2ES2 gl = context.getGL( ).getGL2ES2( );


                // Create

                if ( prog == null )
                {
                    this.prog = new LineProgram( gl );
                }


                // Populate

                int maxVertices = 100;
                FloatBuffer xyBuffer = xyVbo.mapFloats( gl, 2*maxVertices );
                FloatBuffer mileageBuffer = mileageVbo.mapFloats( gl, 1*maxVertices );

                Random r = new Random( 0 );
                double ppvAspectRatio = ppvAspectRatio( axis );
                for ( int i = 0; i < 25; i++ )
                {
                    double x0 = 2 + 6*r.nextDouble( );
                    double y0 = 2 + 6*r.nextDouble( );

                    double x1 = x0 + ( -1 + 2*r.nextDouble( ) );
                    double y1 = y0 + ( -1 + 2*r.nextDouble( ) );

                    double x2 = x1 + ( -1 + 2*r.nextDouble( ) );
                    double y2 = y1 + ( -1 + 2*r.nextDouble( ) );

                    put2f( xyBuffer, x0, y0 );
                    put2f( xyBuffer, x1, y1 );
                    put2f( xyBuffer, x2, y2 );

                    double mileage = 0;
                    put1f( mileageBuffer, mileage );
                    mileage += distance( x0, y0, x1, y1, ppvAspectRatio );
                    put1f( mileageBuffer, mileage );
                    mileage += distance( x1, y1, x2, y2, ppvAspectRatio );
                    put1f( mileageBuffer, mileage );
                }

                this.numVertices = xyBuffer.position( ) / 2;
                xyVbo.seal( gl );
                mileageVbo.seal( gl );


                // Render

                enableStandardBlending( gl );

                prog.begin( gl );
                try
                {
                    prog.setViewport( gl, bounds );
                    prog.setAxisOrtho( gl, axis );
                    prog.setStyle( gl, style );

                    prog.draw( gl, xyVbo, mileageVbo, 0, numVertices );
                }
                finally
                {
                    prog.end( gl );
                }
            }
        } );



        plot.getLayoutCenter( ).addPainter( new ExampleBorderPainter( ) );


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
                showFrameCentered( frame, 2000, 800 );
            }
        } );
    }

}
