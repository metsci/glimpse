package com.metsci.glimpse.examples.icon;

import java.io.IOException;
import java.util.logging.Logger;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;
import javax.media.opengl.fixedfunc.GLMatrixFunc;

import com.jogamp.graph.curve.Region;
import com.jogamp.graph.curve.opengl.RegionRenderer;
import com.jogamp.graph.curve.opengl.RenderState;
import com.jogamp.graph.curve.opengl.TextRegionUtil;
import com.jogamp.graph.font.Font;
import com.jogamp.graph.font.FontFactory;
import com.jogamp.graph.geom.SVertex;
import com.jogamp.opengl.util.PMVMatrix;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.gl.util.GLUtils;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.base.GlimpsePainter;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.plot.EmptyPlot2D;
import com.metsci.glimpse.support.color.GlimpseColor;

public class TextExample implements GlimpseLayoutProvider
{
    private static final Logger logger = Logger.getLogger( TextExample.class.getName( ) );

    public static void main( String[] args ) throws Exception
    {
        Example.showWithSwing( new TextExample( ) );
    }

    @Override
    public GlimpseLayout getLayout( ) throws Exception
    {
        EmptyPlot2D plot = new EmptyPlot2D( );

        Font loadFont = null;
        //InputStream fontFile = StreamOpener.fileThenResource.openForRead( "fonts/bitstream/Vera.ttf" );
        try
        {
            loadFont = FontFactory.get( getClass( ), "fonts/bitstream/Vera.ttf", false );
        }
        catch ( IOException e )
        {
            System.err.println( "Couldn't open font!" );
            e.printStackTrace( );
        }

        RenderState renderState = RenderState.createRenderState( SVertex.factory( ) );
        renderState.setColorStatic( 0, 0, 0, 1 );
        renderState.setHintMask( RenderState.BITHINT_GLOBAL_DEPTH_TEST_ENABLED );

        final RegionRenderer renderer = RegionRenderer.create( renderState, RegionRenderer.defaultBlendEnable, RegionRenderer.defaultBlendDisable );
        final TextRegionUtil util = new TextRegionUtil( Region.DEFAULT_TWO_PASS_TEXTURE_UNIT | Region.COLORCHANNEL_RENDERING_BIT );
        final Font font = loadFont;

        GlimpsePainter painter = new GlimpsePainterBase( )
        {
            protected boolean initialized = false;

            @Override
            public void doPaintTo( GlimpseContext context )
            {
                Axis2D axis = getAxis2D( context );
                GlimpseBounds bounds = getBounds( context );
                GL3 gl = context.getGL( ).getGL3( );

                if ( !initialized )
                {
                    renderer.init( gl, Region.DEFAULT_TWO_PASS_TEXTURE_UNIT | Region.COLORCHANNEL_RENDERING_BIT );
                    initialized = true;
                }

                GLUtils.enableStandardBlending( gl );
                gl.glEnable( GL.GL_CULL_FACE );
                try
                {
                    PMVMatrix m = renderer.getMatrix( );
                    m.glMatrixMode( GLMatrixFunc.GL_MODELVIEW );
                    m.glLoadIdentity( );
                    m.glTranslatef( 5, 7, 0 );

                    m.glMatrixMode( GLMatrixFunc.GL_PROJECTION );
                    m.glLoadIdentity( );
                    m.glOrthof( ( float ) axis.getMinX( ), ( float ) axis.getMaxX( ), ( float ) axis.getMinY( ), ( float ) axis.getMaxY( ), -1f, 1f );

                    float pixelSize = 1.0f; //font.getPixelSize( 32, 96 );
                    int[] samples = new int[1];
                    //String text = "Test";
                    String text = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
                    util.drawString3D( gl, renderer, font, pixelSize, text, GlimpseColor.getRed( ), samples );

                    m.glMatrixMode( GLMatrixFunc.GL_PROJECTION );
                    m.glLoadIdentity( );
                    m.glOrthof( 0, bounds.getWidth( ), 0, bounds.getHeight( ), -1f, 1f );

                    m.glMatrixMode( GLMatrixFunc.GL_MODELVIEW );
                    m.glLoadIdentity( );
                    m.glTranslatef( ( float ) axis.getAxisX( ).valueToScreenPixelUnits( 5 ), ( float ) axis.getAxisY( ).valueToScreenPixelUnits( 5 ), 0f );

                    pixelSize = font.getPixelSize( 32, 96 );
                    util.drawString3D( gl, renderer, font, pixelSize, text, GlimpseColor.getRed( ), samples );

                    renderer.enable( gl, false );
                }
                finally
                {
                    gl.glDisable( GL.GL_CULL_FACE );
                    GLUtils.disableBlending( gl );
                }
            }

            @Override
            protected void doDispose( GlimpseContext context )
            {
                // TODO Auto-generated method stub

            }
        };

        plot.addPainter( painter );

        return plot;
    }
}
