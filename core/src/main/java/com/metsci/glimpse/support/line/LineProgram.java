package com.metsci.glimpse.support.line;

import static com.jogamp.common.nio.Buffers.newDirectFloatBuffer;

import java.nio.FloatBuffer;

import javax.media.opengl.GLUniformData;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.gl.shader.GlimpseShaderProgram;

public class LineProgram extends GlimpseShaderProgram
{

    protected final GLUniformData AXIS_RECT;
    protected final GLUniformData VIEWPORT_SIZE_PX;

    protected final GLUniformData RGBA;
    protected final GLUniformData STIPPLE_ENABLE;
    protected final GLUniformData STIPPLE_SCALE;
    protected final GLUniformData STIPPLE_PATTERN;
    protected final GLUniformData LINE_THICKNESS_PX;
    protected final GLUniformData FEATHER_THICKNESS_PX;

    public final LineVertexData vertices;


    public LineProgram( )
    {
        this.addVertexShader( "shaders/line/line.vs" );
        this.addGeometryShader( "shaders/line/line.gs" );
        this.addFragmentShader( "shaders/line/line.fs" );

        this.AXIS_RECT = this.addUniformData( new GLUniformData( "AXIS_RECT", 4, newDirectFloatBuffer( 4 ) ) );
        this.VIEWPORT_SIZE_PX = this.addUniformData( new GLUniformData( "VIEWPORT_SIZE_PX", 2, newDirectFloatBuffer( 2 ) ) );

        this.RGBA = this.addUniformData( new GLUniformData( "RGBA", 4, newDirectFloatBuffer( 4 ) ) );
        this.STIPPLE_ENABLE = this.addUniformData( new GLUniformData( "STIPPLE_ENABLE", Integer.valueOf( 0 ) ) );
        this.STIPPLE_SCALE = this.addUniformData( new GLUniformData( "STIPPLE_SCALE", Float.valueOf( 1.0f ) ) );
        this.STIPPLE_PATTERN = this.addUniformData( new GLUniformData( "STIPPLE_PATTERN", Integer.valueOf( 0xFF ) ) );
        this.LINE_THICKNESS_PX = this.addUniformData( new GLUniformData( "LINE_THICKNESS_PX", Float.valueOf( 1.0f ) ) );
        this.FEATHER_THICKNESS_PX = this.addUniformData( new GLUniformData( "FEATHER_THICKNESS_PX", Float.valueOf( 1.0f ) ) );

        this.vertices = new LineVertexData( );
        this.addArrayData( vertices.xyArray );
        this.addArrayData( vertices.cumulativeDistanceArray );
    }

    public void setColor( float[] color )
    {
        FloatBuffer rgba = ( FloatBuffer ) this.RGBA.getBuffer( );
        rgba.put( 0, color[ 0 ] );
        rgba.put( 1, color[ 1 ] );
        rgba.put( 2, color[ 2 ] );
        rgba.put( 3, color[ 3 ] );
    }

    public void enableStipple( float scale, int pattern )
    {
        this.STIPPLE_ENABLE.setData( 1 );
        this.STIPPLE_SCALE.setData( scale );
        this.STIPPLE_PATTERN.setData( pattern );
    }

    public void disableStipple( )
    {
        this.STIPPLE_ENABLE.setData( 0 );
    }

    public void setLineThickness( float thickness_PX )
    {
        this.LINE_THICKNESS_PX.setData( thickness_PX );
    }

    public void setFeatherThickness( float feather_PX )
    {
        this.FEATHER_THICKNESS_PX.setData( feather_PX );
    }

    public void setViewport( GlimpseBounds bounds )
    {
        setViewport( bounds.getWidth( ), bounds.getHeight( ) );
    }

    public void setViewport( int viewportWidth, int viewportHeight )
    {
        FloatBuffer viewportSize_PX = ( FloatBuffer ) this.VIEWPORT_SIZE_PX.getBuffer( );
        viewportSize_PX.put( 0, viewportWidth );
        viewportSize_PX.put( 1, viewportHeight );
    }

    public void setAxisOrtho( Axis2D axis )
    {
        setOrtho( ( float ) axis.getMinX( ),
                  ( float ) axis.getMinY( ),
                  ( float ) axis.getMaxX( ),
                  ( float ) axis.getMaxY( ) );
    }

    public void setPixelOrtho( GlimpseBounds bounds )
    {
        setOrtho( 0,
                  0,
                  bounds.getWidth( ),
                  bounds.getHeight( ) );
    }

    public void setOrtho( float xMin, float yMin, float xMax, float yMax )
    {
        FloatBuffer axisRect = ( FloatBuffer ) this.AXIS_RECT.getBuffer( );
        axisRect.put( 0, xMin );
        axisRect.put( 1, yMin );
        axisRect.put( 2, xMax );
        axisRect.put( 3, yMax );
    }

}
