package com.metsci.glimpse.support.line;

import static com.jogamp.common.nio.Buffers.SIZEOF_FLOAT;
import static com.metsci.glimpse.gl.util.GLUtils.genBuffer;
import static com.metsci.glimpse.support.FrameUtils.disposeOnWindowClosing;
import static com.metsci.glimpse.support.FrameUtils.newFrame;
import static com.metsci.glimpse.support.FrameUtils.showFrameCentered;
import static com.metsci.glimpse.support.FrameUtils.stopOnWindowClosing;
import static javax.media.opengl.GL.GL_ARRAY_BUFFER;
import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL.GL_MAP_UNSYNCHRONIZED_BIT;
import static javax.media.opengl.GL.GL_MAP_WRITE_BIT;
import static javax.media.opengl.GL.GL_ONE;
import static javax.media.opengl.GL.GL_ONE_MINUS_SRC_ALPHA;
import static javax.media.opengl.GL.GL_SRC_ALPHA;
import static javax.media.opengl.GL2ES2.GL_STREAM_DRAW;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

import java.nio.FloatBuffer;

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
            LineStyle style = new LineStyle( );
            LineProgram prog = null;

            int xyVbo = 0;
            int cumulativeDistanceVbo = 0;

            {
                style.rgba = GlimpseColor.getBlack( );
                style.thickness_PX = 5;
                style.stippleEnable = true;
                style.stippleScale = 5;
                style.stipplePattern = 0b0001010101010101;
            }

            public void paintTo( GlimpseContext context, GlimpseBounds bounds, Axis2D axis )
            {
                GL2ES2 gl = context.getGL( ).getGL2ES2( );


                if ( prog == null )
                {
                    prog = new LineProgram( gl );
                }

                if ( xyVbo == 0 )
                {
                    xyVbo = genBuffer( gl );
                }

                if ( cumulativeDistanceVbo == 0 )
                {
                    cumulativeDistanceVbo = genBuffer( gl );
                }



                gl.glBlendFuncSeparate( GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA );
                gl.glEnable( GL_BLEND );


                prog.begin( gl );
                try
                {
                    prog.setViewport( gl, bounds );
                    prog.setPixelOrtho( gl, bounds );
                    prog.setStyle( gl, style );

                    float inset_PX = 0.5f * style.thickness_PX;
                    float xLeft_PX = inset_PX;
                    float xRight_PX = bounds.getWidth( ) - inset_PX;
                    float yBottom_PX = inset_PX;
                    float yTop_PX = bounds.getHeight( ) - inset_PX;



                    int maxVertices = 100;


                    int xyMaxBytes = maxVertices * 2 * SIZEOF_FLOAT;
                    gl.glBindBuffer( GL_ARRAY_BUFFER, xyVbo );
                    gl.glBufferData( GL_ARRAY_BUFFER, xyMaxBytes, null, GL_STREAM_DRAW );
                    FloatBuffer xyBuffer = gl.glMapBufferRange( GL_ARRAY_BUFFER, 0, xyMaxBytes, GL_MAP_WRITE_BIT | GL_MAP_UNSYNCHRONIZED_BIT ).asFloatBuffer( );

                    xyBuffer.put( xLeft_PX  ).put( yBottom_PX );
                    xyBuffer.put( xRight_PX ).put( yBottom_PX );

                    xyBuffer.put( xRight_PX ).put( yBottom_PX );
                    xyBuffer.put( xRight_PX ).put( yTop_PX    );

                    xyBuffer.put( xLeft_PX ).put( yBottom_PX );
                    xyBuffer.put( xLeft_PX ).put( yTop_PX    );

                    xyBuffer.put( xLeft_PX  ).put( yTop_PX );
                    xyBuffer.put( xRight_PX ).put( yTop_PX );

                    int numVertices = xyBuffer.duplicate( ).flip( ).remaining( ) / 2;
                    gl.glUnmapBuffer( GL_ARRAY_BUFFER );




                    int cumulativeDistanceMaxBytes = maxVertices * 1 * SIZEOF_FLOAT;
                    gl.glBindBuffer( GL_ARRAY_BUFFER, cumulativeDistanceVbo );
                    gl.glBufferData( GL_ARRAY_BUFFER, cumulativeDistanceMaxBytes, null, GL_STREAM_DRAW );
                    FloatBuffer cumulativeDistanceBuffer = gl.glMapBufferRange( GL_ARRAY_BUFFER, 0, cumulativeDistanceMaxBytes, GL_MAP_WRITE_BIT | GL_MAP_UNSYNCHRONIZED_BIT ).asFloatBuffer( );

                    cumulativeDistanceBuffer.put( 0 );
                    cumulativeDistanceBuffer.put( xRight_PX - xLeft_PX );

                    cumulativeDistanceBuffer.put( 0 );
                    cumulativeDistanceBuffer.put( yTop_PX - yBottom_PX );

                    cumulativeDistanceBuffer.put( 0 );
                    cumulativeDistanceBuffer.put( yTop_PX - yBottom_PX );

                    cumulativeDistanceBuffer.put( 0 );
                    cumulativeDistanceBuffer.put( xRight_PX - xLeft_PX );

                    gl.glUnmapBuffer( GL_ARRAY_BUFFER );




                    prog.draw( gl, xyVbo, cumulativeDistanceVbo, 0, numVertices );
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
