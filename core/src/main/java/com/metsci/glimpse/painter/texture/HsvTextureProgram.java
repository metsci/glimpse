package com.metsci.glimpse.painter.texture;

import static com.metsci.glimpse.gl.shader.GLShaderUtils.createProgram;
import static com.metsci.glimpse.gl.shader.GLShaderUtils.requireResourceText;
import static com.metsci.glimpse.gl.util.GLUtils.defaultVertexAttributeArray;
import static com.metsci.glimpse.support.wrapped.WrappedGlimpseContext.getWrapper2D;
import static javax.media.opengl.GL.GL_ARRAY_BUFFER;
import static javax.media.opengl.GL.GL_FLOAT;

import javax.media.opengl.GL;
import javax.media.opengl.GL2ES2;
import javax.media.opengl.GL2ES3;

import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.GLEditableBuffer;
import com.metsci.glimpse.gl.texture.DrawableTextureProgram;
import com.metsci.glimpse.support.wrapped.Wrapper2D;

/**
 * @author borkholder
 */
public class HsvTextureProgram implements DrawableTextureProgram
{
    public static final String lineVertShader_GLSL = requireResourceText( "shaders/HeatMapProgram/heatmap.vs" );
    public static final String lineGeomShader_GLSL = requireResourceText( "shaders/HeatMapProgram/heatmap.gs" );
    public static final String lineFragShader_GLSL = requireResourceText( "shaders/HeatmapProgram/heatmap-hsv.fs" );

    public static class Handles
    {
        public final int program;

        public final int AXIS_RECT;
        public final int WRAP_RECT;

        public final int HUE_TEXUNIT;
        public final int VALUE_TEXUNIT;

        public final int ALPHA;
        public final int SATURATION;
        public final int DISCARD_NAN;

        public final int inXy;
        public final int inSt;

        public Handles( GL2ES2 gl )
        {
            this.program = createProgram( gl, lineVertShader_GLSL, lineGeomShader_GLSL, lineFragShader_GLSL );

            this.AXIS_RECT = gl.glGetUniformLocation( program, "AXIS_RECT" );
            this.WRAP_RECT = gl.glGetUniformLocation( program, "WRAP_RECT" );

            this.HUE_TEXUNIT = gl.glGetUniformLocation( program, "HUE_TEXUNIT" );
            this.VALUE_TEXUNIT = gl.glGetUniformLocation( program, "VALUE_TEXUNIT" );

            this.ALPHA = gl.glGetUniformLocation( program, "ALPHA" );
            this.SATURATION = gl.glGetUniformLocation( program, "SATURATION" );
            this.DISCARD_NAN = gl.glGetUniformLocation( program, "DISCARD_NAN" );

            this.inXy = gl.glGetAttribLocation( program, "inXy" );
            this.inSt = gl.glGetAttribLocation( program, "inSt" );
        }
    }

    protected final int hueTexUnit;
    protected final int valueTexUnit;

    protected float alpha;
    protected float saturation;
    protected boolean discardNan;

    protected Handles handles;

    public HsvTextureProgram( int hueTexUnit, int valueTexUnit )
    {
        this.hueTexUnit = hueTexUnit;
        this.valueTexUnit = valueTexUnit;

        this.alpha = 1f;
        this.saturation = 1f;
        this.discardNan = false;

        this.handles = null;
    }

    public void setAlpha( float alpha )
    {
        this.alpha = alpha;
    }

    public void setSaturation( float saturation )
    {
        this.saturation = saturation;
    }

    public void setDiscardNan( boolean discardNan )
    {
        this.discardNan = discardNan;
    }

    /**
     * Returns the raw GL handles for the shader program, uniforms, and attributes. Compiles and
     * links the program, if necessary.
     */
    public Handles handles( GL2ES2 gl )
    {
        if ( this.handles == null )
        {
            this.handles = new Handles( gl );
        }
        return this.handles;
    }

    @Override
    public void begin( GlimpseContext context, float xMin, float xMax, float yMin, float yMax )
    {
        GL2ES3 gl = context.getGL( ).getGL2ES3( );

        if ( this.handles == null )
        {
            this.handles = new Handles( gl );
        }

        gl.glBindVertexArray( defaultVertexAttributeArray( gl ) );
        gl.glUseProgram( this.handles.program );

        gl.glUniform4f( this.handles.AXIS_RECT, xMin, xMax, yMin, yMax );

        Wrapper2D wrapper = getWrapper2D( context );
        gl.glUniform4f( this.handles.WRAP_RECT, ( float ) wrapper.x.wrapMin( ), ( float ) wrapper.x.wrapMax( ), ( float ) wrapper.y.wrapMin( ), ( float ) wrapper.y.wrapMax( ) );

        // The appropriate calls to glActiveTexture() happen in ShadedTexturePainter.prepare()
        gl.glUniform1i( this.handles.VALUE_TEXUNIT, this.valueTexUnit );
        gl.glUniform1i( this.handles.HUE_TEXUNIT, this.hueTexUnit );

        gl.glUniform1f( this.handles.ALPHA, this.alpha );
        gl.glUniform1f( this.handles.SATURATION, this.saturation );
        gl.glUniform1i( this.handles.DISCARD_NAN, ( this.discardNan ? 1 : 0 ) );

        gl.glEnableVertexAttribArray( this.handles.inXy );
        gl.glEnableVertexAttribArray( this.handles.inSt );
    }

    @Override
    public void draw( GlimpseContext context, int mode, GLEditableBuffer xyVbo, GLEditableBuffer stVbo, int first, int count )
    {
        GL gl = context.getGL( );
        this.draw( context, mode, xyVbo.deviceBuffer( gl ), stVbo.deviceBuffer( gl ), first, count );
    }

    @Override
    public void draw( GlimpseContext context, int mode, int xyVbo, int stVbo, int first, int count )
    {
        GL2ES2 gl = context.getGL( ).getGL2ES2( );

        gl.glBindBuffer( GL_ARRAY_BUFFER, xyVbo );
        gl.glVertexAttribPointer( this.handles.inXy, 2, GL_FLOAT, false, 0, 0 );

        gl.glBindBuffer( GL_ARRAY_BUFFER, stVbo );
        gl.glVertexAttribPointer( this.handles.inSt, 2, GL_FLOAT, false, 0, 0 );

        gl.glDrawArrays( mode, first, count );
    }

    @Override
    public void end( GlimpseContext context )
    {
        GL2ES3 gl = context.getGL( ).getGL2ES3( );

        gl.glDisableVertexAttribArray( this.handles.inXy );
        gl.glDisableVertexAttribArray( this.handles.inSt );

        gl.glUseProgram( 0 );
        gl.glBindVertexArray( 0 );
    }

    @Override
    public void dispose( GlimpseContext context )
    {
        GL2ES2 gl = context.getGL( ).getGL2ES2( );

        if ( this.handles != null )
        {
            gl.glDeleteProgram( this.handles.program );
            this.handles = null;
        }
    }
}
