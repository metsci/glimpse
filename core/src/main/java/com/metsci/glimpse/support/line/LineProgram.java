package com.metsci.glimpse.support.line;

import static com.metsci.glimpse.support.line.util.ShaderUtils.createProgram;
import static com.metsci.glimpse.support.line.util.ShaderUtils.requireResourceText;
import static javax.media.opengl.GL.GL_ARRAY_BUFFER;
import static javax.media.opengl.GL.GL_FLOAT;
import static javax.media.opengl.GL3.GL_LINE_STRIP_ADJACENCY;

import javax.media.opengl.GL2ES2;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.support.line.util.MappableBuffer;

public class LineProgram
{

    public static final String lineVertShader_GLSL = requireResourceText( "shaders/line/line.vs" );
    public static final String lineGeomShader_GLSL = requireResourceText( "shaders/line/line.gs" );
    public static final String lineFragShader_GLSL = requireResourceText( "shaders/line/line.fs" );


    public final int programHandle;


    // Uniforms

    public final int AXIS_RECT;
    public final int VIEWPORT_SIZE_PX;

    public final int RGBA;
    public final int STIPPLE_ENABLE;
    public final int STIPPLE_SCALE;
    public final int STIPPLE_PATTERN;
    public final int LINE_THICKNESS_PX;
    public final int FEATHER_THICKNESS_PX;
    public final int JOIN_TYPE;


    // Vertex attributes

    public final int inXy;
    public final int inMileage;


    public LineProgram( GL2ES2 gl )
    {
        this.programHandle = createProgram( gl, lineVertShader_GLSL, lineGeomShader_GLSL, lineFragShader_GLSL );

        this.AXIS_RECT = gl.glGetUniformLocation( programHandle, "AXIS_RECT" );
        this.VIEWPORT_SIZE_PX = gl.glGetUniformLocation( programHandle, "VIEWPORT_SIZE_PX" );

        this.RGBA = gl.glGetUniformLocation( programHandle, "RGBA" );
        this.STIPPLE_ENABLE = gl.glGetUniformLocation( programHandle, "STIPPLE_ENABLE" );
        this.STIPPLE_SCALE = gl.glGetUniformLocation( programHandle, "STIPPLE_SCALE" );
        this.STIPPLE_PATTERN = gl.glGetUniformLocation( programHandle, "STIPPLE_PATTERN" );
        this.LINE_THICKNESS_PX = gl.glGetUniformLocation( programHandle, "LINE_THICKNESS_PX" );
        this.FEATHER_THICKNESS_PX = gl.glGetUniformLocation( programHandle, "FEATHER_THICKNESS_PX" );
        this.JOIN_TYPE = gl.glGetUniformLocation( programHandle, "JOIN_TYPE" );

        this.inXy = gl.glGetAttribLocation( programHandle, "inXy" );
        this.inMileage = gl.glGetAttribLocation( programHandle, "inMileage" );
    }

    public void begin( GL2ES2 gl )
    {
        gl.glUseProgram( programHandle );
        gl.glEnableVertexAttribArray( inXy );
        gl.glEnableVertexAttribArray( inMileage );
    }

    public void setViewport( GL2ES2 gl, GlimpseBounds bounds )
    {
        setViewport( gl, bounds.getWidth( ), bounds.getHeight( ) );
    }

    public void setViewport( GL2ES2 gl, int viewportWidth, int viewportHeight )
    {
        gl.glUniform2f( VIEWPORT_SIZE_PX, viewportWidth, viewportHeight );
    }

    public void setAxisOrtho( GL2ES2 gl, Axis2D axis )
    {
        setOrtho( gl, ( float ) axis.getMinX( ), ( float ) axis.getMaxX( ), ( float ) axis.getMinY( ), ( float ) axis.getMaxY( ) );
    }

    public void setPixelOrtho( GL2ES2 gl, GlimpseBounds bounds )
    {
        setOrtho( gl, 0, bounds.getWidth( ), 0, bounds.getHeight( ) );
    }

    public void setOrtho( GL2ES2 gl, float xMin, float xMax, float yMin, float yMax )
    {
        gl.glUniform4f( AXIS_RECT, xMin, xMax, yMin, yMax );
    }

    public void setStyle( GL2ES2 gl, LineStyle style )
    {
        gl.glUniform4fv( RGBA, 1, style.rgba, 0 );

        if ( style.stippleEnable )
        {
            gl.glUniform1i( STIPPLE_ENABLE, 1 );
            gl.glUniform1f( STIPPLE_SCALE, style.stippleScale );
            gl.glUniform1i( STIPPLE_PATTERN, style.stipplePattern );
        }
        else
        {
            gl.glUniform1i( STIPPLE_ENABLE, 0 );
        }

        gl.glUniform1f( LINE_THICKNESS_PX, style.thickness_PX );
        gl.glUniform1f( FEATHER_THICKNESS_PX, style.feather_PX );
        gl.glUniform1i( JOIN_TYPE, style.joinType );
    }

    public void draw( GL2ES2 gl, LineStyle style, LinePath path )
    {
        draw( gl, style, path, 1.0 );
    }

    public void draw( GL2ES2 gl, LineStyle style, LinePath path, double ppvAspectRatio )
    {
        setStyle( gl, style );

        MappableBuffer xyVbo = path.xyVbo( gl );
        MappableBuffer mileageVbo = ( style.stippleEnable ? path.mileageVbo( gl, ppvAspectRatio ) : path.connectVbo( gl ) );
        draw( gl, xyVbo, mileageVbo, 0, path.numVertices( ) );
    }

    public void draw( GL2ES2 gl, MappableBuffer xyVbo, MappableBuffer mileageVbo, int first, int count )
    {
        gl.glBindBuffer( xyVbo.target, xyVbo.buffer( ) );
        gl.glVertexAttribPointer( inXy, 2, GL_FLOAT, false, 0, xyVbo.sealedOffset( ) );

        gl.glBindBuffer( mileageVbo.target, mileageVbo.buffer( ) );
        gl.glVertexAttribPointer( inMileage, 1, GL_FLOAT, false, 0, mileageVbo.sealedOffset( ) );

        gl.glDrawArrays( GL_LINE_STRIP_ADJACENCY, first, count );
    }

    public void draw( GL2ES2 gl, int xyVbo, int mileageVbo, int first, int count )
    {
        gl.glBindBuffer( GL_ARRAY_BUFFER, xyVbo );
        gl.glVertexAttribPointer( inXy, 2, GL_FLOAT, false, 0, 0 );

        gl.glBindBuffer( GL_ARRAY_BUFFER, mileageVbo );
        gl.glVertexAttribPointer( inMileage, 1, GL_FLOAT, false, 0, 0 );

        gl.glDrawArrays( GL_LINE_STRIP_ADJACENCY, first, count );
    }

    public void end( GL2ES2 gl )
    {
        gl.glDisableVertexAttribArray( inXy );
        gl.glDisableVertexAttribArray( inMileage );
        gl.glUseProgram( 0 );
    }

}
