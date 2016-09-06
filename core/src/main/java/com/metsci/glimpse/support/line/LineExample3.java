package com.metsci.glimpse.support.line;

import static com.metsci.glimpse.support.FrameUtils.disposeOnWindowClosing;
import static com.metsci.glimpse.support.FrameUtils.newFrame;
import static com.metsci.glimpse.support.FrameUtils.showFrameCentered;
import static com.metsci.glimpse.support.FrameUtils.stopOnWindowClosing;
import static com.metsci.glimpse.support.line.LineUtils.enableStandardBlending;
import static com.metsci.glimpse.support.line.LineUtils.ppvAspectRatio;
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
import com.metsci.glimpse.painter.base.GlimpsePainterImpl;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.plot.Plot2D;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.settings.SwingLookAndFeel;
import com.metsci.glimpse.support.swing.NewtSwingEDTGlimpseCanvas;
import com.metsci.glimpse.support.swing.SwingEDTAnimator;

public class LineExample3
{

    public static void main( String[] args )
    {
        final Plot2D plot = new Plot2D( "" );
        plot.setAxisSizeZ( 0 );
        plot.setTitleHeight( 0 );

        plot.getLayoutCenter( ).addPainter( new BackgroundPainter( ) );



        plot.getLayoutCenter( ).addPainter( new GlimpsePainter2D( )
        {
            LineStyle style;
            LinePath path;
            LineProgram prog;

            {
                this.style = new LineStyle( );
                style.rgba = floats( 0.7f, 0, 0, 1 );
                style.thickness_PX = 4;
                style.stippleEnable = true;
                style.stippleScale = 2;
                style.stipplePattern = 0b0001010111111111;

                this.path = new LinePath( );
                Random r = new Random( 0 );
                for ( int i = 0; i < 25; i++ )
                {
                    float x0 = 2 + 6*r.nextFloat( );
                    float y0 = 2 + 6*r.nextFloat( );

                    float x1 = x0 + ( -1 + 2*r.nextFloat( ) );
                    float y1 = y0 + ( -1 + 2*r.nextFloat( ) );

                    float x2 = x1 + ( -1 + 2*r.nextFloat( ) );
                    float y2 = y1 + ( -1 + 2*r.nextFloat( ) );

                    path.moveTo( x0, y0 );
                    path.lineTo( x1, y1 );
                    path.lineTo( x2, y2 );
                }

                this.prog = null;
            }

            public void paintTo( GlimpseContext context, GlimpseBounds bounds, Axis2D axis )
            {
                GL2ES2 gl = context.getGL( ).getGL2ES2( );

                enableStandardBlending( gl );

                if ( prog == null )
                {
                    this.prog = new LineProgram( gl );
                }

                prog.begin( gl );
                try
                {
                    prog.setViewport( gl, bounds );
                    prog.setAxisOrtho( gl, axis );
                    prog.draw( gl, style, path, ppvAspectRatio( axis ) );
                }
                finally
                {
                    prog.end( gl );
                }
            }
        } );



        plot.getLayoutCenter( ).addPainter( new GlimpsePainterImpl( )
        {
            LineStyle style = new LineStyle( );
            LineProgram prog = null;

            MappableBuffer xyVbo = new MappableBuffer( GL_ARRAY_BUFFER, GL_STREAM_DRAW, 1000 );
            MappableBuffer mileageVbo = new MappableBuffer( GL_ARRAY_BUFFER, GL_STREAM_DRAW, 1000 );

            {
                style.rgba = GlimpseColor.getBlack( );
                style.thickness_PX = 1;
            }

            public void paintTo( GlimpseContext context, GlimpseBounds bounds )
            {
                GL2ES2 gl = context.getGL( ).getGL2ES2( );


                // Create

                if ( prog == null )
                {
                    this.prog = new LineProgram( gl );
                }


                // Populate

                int maxVertices = 8;
                FloatBuffer xyBuffer = xyVbo.mapFloats( gl, 2*maxVertices );
                FloatBuffer mileageBuffer = mileageVbo.mapFloats( gl, 1*maxVertices );

                float inset_PX = 0.5f * style.thickness_PX;
                float xLeft_PX = inset_PX;
                float xRight_PX = bounds.getWidth( ) - inset_PX;
                float yBottom_PX = inset_PX;
                float yTop_PX = bounds.getHeight( ) - inset_PX;

                xyBuffer.put( xLeft_PX  ).put( yBottom_PX );
                xyBuffer.put( xRight_PX ).put( yBottom_PX );
                mileageBuffer.put( 0 );
                mileageBuffer.put( xRight_PX - xLeft_PX );

                xyBuffer.put( xRight_PX ).put( yBottom_PX );
                xyBuffer.put( xRight_PX ).put( yTop_PX    );
                mileageBuffer.put( 0 );
                mileageBuffer.put( yTop_PX - yBottom_PX );

                xyBuffer.put( xLeft_PX ).put( yBottom_PX );
                xyBuffer.put( xLeft_PX ).put( yTop_PX    );
                mileageBuffer.put( 0 );
                mileageBuffer.put( yTop_PX - yBottom_PX );

                xyBuffer.put( xLeft_PX  ).put( yTop_PX );
                xyBuffer.put( xRight_PX ).put( yTop_PX );
                mileageBuffer.put( 0 );
                mileageBuffer.put( xRight_PX - xLeft_PX );

                int numVertices = xyBuffer.position( ) / 2;
                xyVbo.seal( gl );
                mileageVbo.seal( gl );


                // Render

                enableStandardBlending( gl );

                prog.begin( gl );
                try
                {
                    prog.setViewport( gl, bounds );
                    prog.setPixelOrtho( gl, bounds );
                    prog.setStyle( gl, style );

                    prog.draw( gl, xyVbo, mileageVbo, 0, numVertices );
                }
                finally
                {
                    prog.end( gl );
                }
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
