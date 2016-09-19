package com.metsci.glimpse.support.line;

import static com.metsci.glimpse.gl.shader.GLShaderUtils.*;
import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL3.*;

import javax.media.opengl.GL2ES2;
import javax.media.opengl.GL2ES3;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.gl.GLStreamingBuffer;

/**
 * Represents the shader program for drawing lines. The program gets compiled and
 * linked on the first call to either {@link #begin(GL2ES2)} or {@link #handles(GL2ES2)}.
 * <p>
 * This class could be extended to support multiple GL instances. Currently, however,
 * it assumes that each instance will only ever be used with a single GL instance.
 */
public class LineProgram
{

    public static final String lineVertShader_GLSL = requireResourceText( "shaders/line/line.vs" );
    public static final String lineGeomShader_GLSL = requireResourceText( "shaders/line/line.gs" );
    public static final String lineFragShader_GLSL = requireResourceText( "shaders/line/line.fs" );


    public static class LineProgramHandles
    {
        public final int program;

        public final int AXIS_RECT;
        public final int VIEWPORT_SIZE_PX;

        public final int LINE_THICKNESS_PX;
        public final int FEATHER_THICKNESS_PX;
        public final int JOIN_TYPE;
        public final int MITER_LIMIT;

        public final int RGBA;
        public final int STIPPLE_ENABLE;
        public final int STIPPLE_SCALE;
        public final int STIPPLE_PATTERN;

        public final int inXy;
        public final int inFlags;
        public final int inMileage;

        public LineProgramHandles( GL2ES2 gl )
        {
            this.program = createProgram( gl, lineVertShader_GLSL, lineGeomShader_GLSL, lineFragShader_GLSL );

            this.AXIS_RECT = gl.glGetUniformLocation( program, "AXIS_RECT" );
            this.VIEWPORT_SIZE_PX = gl.glGetUniformLocation( program, "VIEWPORT_SIZE_PX" );

            this.LINE_THICKNESS_PX = gl.glGetUniformLocation( program, "LINE_THICKNESS_PX" );
            this.FEATHER_THICKNESS_PX = gl.glGetUniformLocation( program, "FEATHER_THICKNESS_PX" );
            this.JOIN_TYPE = gl.glGetUniformLocation( program, "JOIN_TYPE" );
            this.MITER_LIMIT = gl.glGetUniformLocation( program, "MITER_LIMIT" );

            this.RGBA = gl.glGetUniformLocation( program, "RGBA" );
            this.STIPPLE_ENABLE = gl.glGetUniformLocation( program, "STIPPLE_ENABLE" );
            this.STIPPLE_SCALE = gl.glGetUniformLocation( program, "STIPPLE_SCALE" );
            this.STIPPLE_PATTERN = gl.glGetUniformLocation( program, "STIPPLE_PATTERN" );

            this.inXy = gl.glGetAttribLocation( program, "inXy" );
            this.inFlags = gl.glGetAttribLocation( program, "inFlags" );
            this.inMileage = gl.glGetAttribLocation( program, "inMileage" );
        }
    }


    protected LineProgramHandles handles;


    public LineProgram( )
    {
        this.handles = null;
    }

    /**
     * Returns the raw GL handles for the shader program, uniforms, and attributes. Compiles and
     * links the program, if necessary.
     * <p>
     * It is perfectly acceptable to use these handles directly, rather than calling the convenience
     * methods in this class. However, the convenience methods are intended to be a fairly stable API,
     * whereas the handles may change frequently.
     */
    public LineProgramHandles handles( GL2ES2 gl )
    {
        if ( this.handles == null )
        {
            this.handles = new LineProgramHandles( gl );
        }

        return this.handles;
    }

    public void begin( GL2ES2 gl )
    {
        if ( this.handles == null )
        {
            this.handles = new LineProgramHandles( gl );
        }

        gl.glUseProgram( this.handles.program );
        gl.glEnableVertexAttribArray( this.handles.inXy );
        gl.glEnableVertexAttribArray( this.handles.inFlags );
        gl.glEnableVertexAttribArray( this.handles.inMileage );
    }

    public void setViewport( GL2ES2 gl, GlimpseBounds bounds )
    {
        this.setViewport( gl, bounds.getWidth( ), bounds.getHeight( ) );
    }

    public void setViewport( GL2ES2 gl, int viewportWidth, int viewportHeight )
    {
        gl.glUniform2f( this.handles.VIEWPORT_SIZE_PX, viewportWidth, viewportHeight );
    }

    public void setAxisOrtho( GL2ES2 gl, Axis2D axis )
    {
        this.setOrtho( gl, ( float ) axis.getMinX( ), ( float ) axis.getMaxX( ), ( float ) axis.getMinY( ), ( float ) axis.getMaxY( ) );
    }

    public void setPixelOrtho( GL2ES2 gl, GlimpseBounds bounds )
    {
        this.setOrtho( gl, 0, bounds.getWidth( ), 0, bounds.getHeight( ) );
    }

    public void setOrtho( GL2ES2 gl, float xMin, float xMax, float yMin, float yMax )
    {
        gl.glUniform4f( this.handles.AXIS_RECT, xMin, xMax, yMin, yMax );
    }

    public void setStyle( GL2ES2 gl, LineStyle style )
    {
        gl.glUniform1f( this.handles.LINE_THICKNESS_PX, style.thickness_PX );
        gl.glUniform1f( this.handles.FEATHER_THICKNESS_PX, style.feather_PX );
        gl.glUniform1i( this.handles.JOIN_TYPE, style.joinType.value );
        gl.glUniform1f( this.handles.MITER_LIMIT, style.miterLimit );

        gl.glUniform4fv( this.handles.RGBA, 1, style.rgba, 0 );

        if ( style.stippleEnable )
        {
            gl.glUniform1i( this.handles.STIPPLE_ENABLE, 1 );
            gl.glUniform1f( this.handles.STIPPLE_SCALE, style.stippleScale );
            gl.glUniform1i( this.handles.STIPPLE_PATTERN, style.stipplePattern );
        }
        else
        {
            gl.glUniform1i( this.handles.STIPPLE_ENABLE, 0 );
        }
    }

    public void draw( GL2ES3 gl, LineStyle style, LinePath path )
    {
        this.draw( gl, style, path, 1.0 );
    }

    public void draw( GL2ES3 gl, LineStyle style, LinePath path, double ppvAspectRatio )
    {
        this.setStyle( gl, style );

        GLStreamingBuffer xyVbo = path.xyVbo( gl );
        GLStreamingBuffer flagsVbo = path.flagsVbo( gl );
        GLStreamingBuffer mileageVbo = ( style.stippleEnable ? path.mileageVbo( gl, ppvAspectRatio ) : path.rawMileageVbo( gl ) );

        this.draw( gl, xyVbo, flagsVbo, mileageVbo, 0, path.numVertices( ) );
    }

    public void draw( GL2ES3 gl, GLStreamingBuffer xyVbo, GLStreamingBuffer flagsVbo, GLStreamingBuffer mileageVbo, int first, int count )
    {
        gl.glBindBuffer( xyVbo.target, xyVbo.buffer( ) );
        gl.glVertexAttribPointer( this.handles.inXy, 2, GL_FLOAT, false, 0, xyVbo.sealedOffset( ) );

        gl.glBindBuffer( flagsVbo.target, flagsVbo.buffer( ) );
        gl.glVertexAttribIPointer( this.handles.inFlags, 1, GL_BYTE, 0, flagsVbo.sealedOffset( ) );

        gl.glBindBuffer( mileageVbo.target, mileageVbo.buffer( ) );
        gl.glVertexAttribPointer( this.handles.inMileage, 1, GL_FLOAT, false, 0, mileageVbo.sealedOffset( ) );

        gl.glDrawArrays( GL_LINE_STRIP_ADJACENCY, first, count );
    }

    public void draw( GL2ES3 gl, int xyVbo, int flagsVbo, int mileageVbo, int first, int count )
    {
        gl.glBindBuffer( GL_ARRAY_BUFFER, xyVbo );
        gl.glVertexAttribPointer( this.handles.inXy, 2, GL_FLOAT, false, 0, 0 );

        gl.glBindBuffer( GL_ARRAY_BUFFER, flagsVbo );
        gl.glVertexAttribIPointer( this.handles.inFlags, 1, GL_BYTE, 0, 0 );

        gl.glBindBuffer( GL_ARRAY_BUFFER, mileageVbo );
        gl.glVertexAttribPointer( this.handles.inMileage, 1, GL_FLOAT, false, 0, 0 );

        gl.glDrawArrays( GL_LINE_STRIP_ADJACENCY, first, count );
    }

    public void end( GL2ES2 gl )
    {
        gl.glDisableVertexAttribArray( this.handles.inXy );
        gl.glDisableVertexAttribArray( this.handles.inFlags );
        gl.glDisableVertexAttribArray( this.handles.inMileage );
        gl.glUseProgram( 0 );
    }

    /**
     * Deletes the program, and resets this object to the way it was before {@link #begin(GL2ES2)}
     * was first called.
     * <p>
     * This object can be safely reused after being disposed, but in most cases there is no
     * significant advantage to doing so.
     */
    public void dispose( GL2ES2 gl )
    {
        if ( this.handles != null )
        {
            gl.glDeleteProgram( this.handles.program );
            this.handles = null;
        }
    }

}
