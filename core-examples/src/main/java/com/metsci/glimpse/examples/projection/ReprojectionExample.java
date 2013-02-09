package com.metsci.glimpse.examples.projection;

import static com.metsci.glimpse.gl.util.GLPBufferUtils.createPixelBuffer;

import javax.media.opengl.GLContext;
import javax.swing.JFrame;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.AxisUtil;
import com.metsci.glimpse.canvas.FrameBufferGlimpseCanvas;
import com.metsci.glimpse.canvas.SwingGlimpseCanvas;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.examples.basic.HeatMapExample;
import com.metsci.glimpse.gl.Jogular;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.painter.texture.ShadedTexturePainter;
import com.metsci.glimpse.plot.ColorAxisPlot2D;
import com.metsci.glimpse.support.projection.PolarProjection;
import com.metsci.glimpse.support.repaint.RepaintManager;
import com.metsci.glimpse.support.repaint.SwingRepaintManager;
import com.metsci.glimpse.support.settings.SwingLookAndFeel;
import com.metsci.glimpse.support.texture.TextureProjected2D;

/**
 * Demonstrates using Glimpse offscreen rendering to distort an existing Glimpse plot.
 * This can be used to create graphical effects (like 
 * @author ulman
 *
 */
public class ReprojectionExample
{
    public static void main( String[] args ) throws Exception
    {
        Jogular.initJogl( );

        GLContext context = createPixelBuffer( 1, 1 ).getContext( );
        final SwingGlimpseCanvas canvas = new SwingGlimpseCanvas( true, context );
        ColorAxisPlot2D layout = new HeatMapExample( ).getLayout( );
        canvas.addLayout( layout );
        canvas.setLookAndFeel( new SwingLookAndFeel( ) );

        final RepaintManager manager = SwingRepaintManager.newRepaintManager( canvas );

        final FrameBufferGlimpseCanvas offscreenCanvas = new FrameBufferGlimpseCanvas( 800, 800, context );
        offscreenCanvas.addLayout( layout );
        manager.addGlimpseCanvas( offscreenCanvas );

        final SwingGlimpseCanvas canvas2 = new SwingGlimpseCanvas( true, context );
        canvas2.addLayout( new ReprojectionExample( ).getLayout( offscreenCanvas ) );
        canvas2.setLookAndFeel( new SwingLookAndFeel( ) );
        manager.addGlimpseCanvas( canvas2 );

        createFrame( "Original", canvas );
        createFrame( "Reprojected", canvas2 );

        Runtime.getRuntime( ).addShutdownHook( new Thread( )
        {
            @Override
            public void run( )
            {
                canvas.dispose( manager );
                canvas2.dispose( manager );
                offscreenCanvas.dispose( manager );
            }
        } );
    }

    public static JFrame createFrame( String name, SwingGlimpseCanvas canvas )
    {
        JFrame frame = new JFrame( name );
        frame.add( canvas );

        frame.pack( );
        frame.setSize( 800, 800 );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.setVisible( true );

        return frame;
    }

    public GlimpseLayout getLayout( final FrameBufferGlimpseCanvas offscreenCanvas ) throws Exception
    {
        Axis2D axis = new Axis2D( );
        axis.set( -10, 10, -10, 10 );
        GlimpseAxisLayout2D layout2 = new GlimpseAxisLayout2D( axis );
        AxisUtil.attachMouseListener( layout2 );

        ShadedTexturePainter painter = new ShadedTexturePainter( )
        {
            boolean initialized = false;

            @Override
            public void paintTo( GlimpseContext context, GlimpseBounds bounds, Axis2D axis )
            {
                super.paintTo( context, bounds, axis );

                if ( !initialized && offscreenCanvas.getFrameBuffer( ).isInitialized( ) )
                {
                    System.out.println( "initializing" );

                    TextureProjected2D texture = offscreenCanvas.getGlimpseTexture( );
                    texture.setProjection( new PolarProjection( 0, 10, 0, 360 ) );
                    addDrawableTexture( texture );
                    initialized = true;
                }
            }
        };

        layout2.addPainter( new BackgroundPainter( true ) );
        layout2.addPainter( painter );

        return layout2;
    }
}
