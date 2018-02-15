package com.metsci.glimpse.painter.texture;

import static com.metsci.glimpse.gl.shader.GLShaderUtils.createProgram;
import static com.metsci.glimpse.gl.shader.GLShaderUtils.requireResourceText;
import static com.metsci.glimpse.gl.util.GLUtils.defaultVertexAttributeArray;
import static com.metsci.glimpse.support.wrapped.WrappedGlimpseContext.getWrapper2D;
import static javax.media.opengl.GL.GL_ARRAY_BUFFER;
import static javax.media.opengl.GL.GL_FLOAT;

import java.util.function.DoubleSupplier;

import javax.media.opengl.GL;
import javax.media.opengl.GL2ES2;
import javax.media.opengl.GL2ES3;
import javax.media.opengl.GL3;

import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.GLEditableBuffer;
import com.metsci.glimpse.gl.texture.DrawableTextureProgram;
import com.metsci.glimpse.support.wrapped.Wrapper2D;

public class HeatMapProgram implements DrawableTextureProgram
{
    public static final String lineVertShader_GLSL = requireResourceText( "shaders/HeatMapProgram/heatmap.vs" );
    public static final String lineGeomShader_GLSL = requireResourceText( "shaders/HeatMapProgram/heatmap.gs" );
    public static final String lineFragShader_GLSL = requireResourceText( "shaders/HeatMapProgram/heatmap.fs" );


    public static class HeatMapProgramHandles
    {
        public final int program;

        public final int AXIS_RECT;
        public final int WRAP_RECT;

        public final int VALUES_TEXUNIT;
        public final int COLORMAP_TEXUNIT;
        public final int COLORMAP_MIN;
        public final int COLORMAP_MAX;

        public final int ALPHA;

        public final int DISCARD_NAN;

        public final int inXy;
        public final int inSt;

        public HeatMapProgramHandles( GL2ES2 gl )
        {
            this.program = createProgram( gl, lineVertShader_GLSL, lineGeomShader_GLSL, lineFragShader_GLSL );

            this.AXIS_RECT = gl.glGetUniformLocation( program, "AXIS_RECT" );
            this.WRAP_RECT = gl.glGetUniformLocation( program, "WRAP_RECT" );

            this.VALUES_TEXUNIT = gl.glGetUniformLocation( program, "VALUES_TEXUNIT" );
            this.COLORMAP_TEXUNIT = gl.glGetUniformLocation( program, "COLORMAP_TEXUNIT" );
            this.COLORMAP_MIN = gl.glGetUniformLocation( program, "COLORMAP_MIN" );
            this.COLORMAP_MAX = gl.glGetUniformLocation( program, "COLORMAP_MAX" );

            this.ALPHA = gl.glGetUniformLocation( program, "ALPHA" );

            this.DISCARD_NAN = gl.glGetUniformLocation( program, "DISCARD_NAN" );

            this.inXy = gl.glGetAttribLocation( program, "inXy" );
            this.inSt = gl.glGetAttribLocation( program, "inSt" );
        }
    }


    protected final int valuesTexUnit;
    protected final int colormapTexUnit;
    protected final DoubleSupplier colormapMinFn;
    protected final DoubleSupplier colormapMaxFn;

    protected float alpha;
    protected boolean discardNans;
    protected HeatMapProgramHandles handles;


    public HeatMapProgram( int valuesTexUnit, int colormapTexUnit, DoubleSupplier colormapMinFn, DoubleSupplier colormapMaxFn )
    {
        this.valuesTexUnit = valuesTexUnit;
        this.colormapTexUnit = colormapTexUnit;
        this.colormapMinFn = colormapMinFn;
        this.colormapMaxFn = colormapMaxFn;

        this.discardNans = false;
        this.alpha = 1f;
        this.handles = null;
    }

    public void setAlpha( float alpha )
    {
        this.alpha = alpha;
    }

    public void setDiscardNans( boolean discardNans )
    {
        this.discardNans = discardNans;
    }

    /**
     * Returns the raw GL handles for the shader program, uniforms, and attributes. Compiles and
     * links the program, if necessary.
     */
    public HeatMapProgramHandles handles( GL2ES2 gl )
    {
        if ( this.handles == null )
        {
            this.handles = new HeatMapProgramHandles( gl );
        }
        return this.handles;
    }

    @Override
    public void begin( GlimpseContext context, float xMin, float xMax, float yMin, float yMax )
    {
        GL2ES3 gl = context.getGL( ).getGL2ES3( );

        if ( this.handles == null )
        {
            this.handles = new HeatMapProgramHandles( gl );
        }

        gl.glBindVertexArray( defaultVertexAttributeArray( gl ) );
        gl.glUseProgram( this.handles.program );

        gl.glUniform4f( this.handles.AXIS_RECT, xMin, xMax, yMin, yMax );

        Wrapper2D wrapper = getWrapper2D( context );
        gl.glUniform4f( this.handles.WRAP_RECT, ( float ) wrapper.x.wrapMin( ), ( float ) wrapper.x.wrapMax( ), ( float ) wrapper.y.wrapMin( ), ( float ) wrapper.y.wrapMax( ) );

        // The appropriate call to glActiveTexture() is triggered by ShadedTexturePainter.prepare()
        gl.glUniform1i( this.handles.VALUES_TEXUNIT, this.valuesTexUnit );

        // The appropriate call to glActiveTexture() is triggered by ShadedTexturePainter.prepare()
        gl.glUniform1i( this.handles.COLORMAP_TEXUNIT, this.colormapTexUnit );
        gl.glUniform1f( this.handles.COLORMAP_MIN, ( float ) this.colormapMinFn.getAsDouble( ) );
        gl.glUniform1f( this.handles.COLORMAP_MAX, ( float ) this.colormapMaxFn.getAsDouble( ) );

        gl.glUniform1f( this.handles.ALPHA, this.alpha );

        gl.glUniform1i( this.handles.DISCARD_NAN, ( this.discardNans ? 1 : 0 ) );

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

    /**
     * Deletes the program, and resets this object to the way it was before {@link #begin(GL3)}
     * was first called.
     * <p>
     * This object can be safely reused after being disposed, but in most cases there is no
     * significant advantage to doing so.
     */
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
