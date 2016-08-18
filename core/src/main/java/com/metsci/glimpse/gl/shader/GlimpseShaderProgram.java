package com.metsci.glimpse.gl.shader;

import java.util.Collection;
import java.util.logging.Logger;

import javax.media.opengl.GL;
import javax.media.opengl.GL2ES2;
import javax.media.opengl.GL3;
import javax.media.opengl.GLArrayData;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLUniformData;

import com.google.common.collect.Lists;
import com.jogamp.opengl.util.GLArrayDataClient;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import com.jogamp.opengl.util.glsl.ShaderState;
import com.metsci.glimpse.gl.util.GLErrorUtils;

/**
 * Simple Glimpse-specific convenience wrapper around {@link ShaderProgram}.
 * 
 * @author ulman
 */
public class GlimpseShaderProgram
{
    private static final Logger logger = Logger.getLogger( GlimpseShaderProgram.class.getName( ) );

    protected Collection<ShaderCode> codes;
    protected Collection<GLUniformData> uniforms;
    protected Collection<GLArrayDataClient> arrays;

    protected ShaderProgram program;
    protected ShaderState state;

    protected boolean loaded;

    public GlimpseShaderProgram( )
    {
        this.codes = Lists.newArrayList( );
        this.uniforms = Lists.newArrayList( );
        this.arrays = Lists.newArrayList( );
    }

    public void loadProgram( GL gl )
    {
        load( gl.getGL2ES2( ), this.codes );
    }

    public void useProgram( GL gl, boolean on )
    {
        if ( !load( gl.getGL2ES2( ), this.codes ) ) return;

        this.state.useProgram( gl.getGL2ES2( ), on );

        if ( on ) this.updateUniformData( gl );

        for ( GLArrayDataClient array : arrays )
        {
            array.enableBuffer( gl, on );
        }
    }

    public ShaderCode addFragmentShader( String path )
    {
        return addShader( GL2ES2.GL_FRAGMENT_SHADER, path );
    }

    public ShaderCode addVertexShader( String path )
    {
        return addShader( GL2ES2.GL_VERTEX_SHADER, path );
    }

    public ShaderCode addGeometryShader( String path )
    {
        return addShader( GL3.GL_GEOMETRY_SHADER, path );
    }

    public ShaderCode addShader( int type, String path )
    {
        return this.addShader( ShaderCode.create( null, type, 1, getClass( ), new String[] { path }, true ) );
    }

    public ShaderCode addShader( ShaderCode code )
    {
        codes.add( code );
        return code;
    }

    public GLArrayDataClient addArrayData( GLArrayDataClient array )
    {
        this.arrays.add( array );
        return array;
    }

    public GLUniformData addUniformData( GLUniformData uniform )
    {
        this.uniforms.add( uniform );
        return uniform;
    }

    public void dispose( GLContext context )
    {
        if ( this.loaded )
        {
            this.state.destroy( context.getGL( ).getGL2ES2( ) );
            this.loaded = false;
        }
    }

    protected void updateUniformData( GL gl )
    {
        GL2ES2 gl2es2 = gl.getGL2ES2( );

        for ( GLUniformData uniform : uniforms )
        {
            this.state.uniform( gl2es2, uniform );
            GLErrorUtils.logGLError( logger, gl, "Trouble in GlimpseShaderProgram.load( ). ShaderState.uniform( ): " + uniform.getName( ) );
        }
    }

    protected boolean load( GL gl, Collection<ShaderCode> codes )
    {
        if ( this.loaded ) return true;

        GL2ES2 gl2es2 = gl.getGL2ES2( );

        this.state = new ShaderState( );
        this.state.setVerbose( true );
        this.program = new ShaderProgram( );

        for ( ShaderCode code : codes )
        {
            boolean success = this.program.add( gl2es2, code, System.err );
            GLErrorUtils.logGLError( logger, gl, "Trouble in GlimpseShaderProgram.load( ). ShaderProgram.add( ): " + code );
            GLErrorUtils.logGLShaderInfoLog( logger, gl, this.program.program( ), "Trouble in GlimpseShaderProgram.load( ). ShaderProgram.add( ) Log:" );

            if ( !success )
            {
                return false;
            }
        }

        this.state.attachShaderProgram( gl2es2, this.program, true );
        GLErrorUtils.logGLError( logger, gl, "Trouble in GlimpseShaderProgram.load( ). ShaderState.attachShaderProgram( )" );
        GLErrorUtils.logGLShaderInfoLog( logger, gl, this.program.program( ), "Trouble in GlimpseShaderProgram.load( ). ShaderState.attachShaderProgram( ) Log:" );

        for ( GLArrayData array : arrays )
        {
            this.state.ownAttribute( array, true );
            GLErrorUtils.logGLError( logger, gl, "Trouble in GlimpseShaderProgram.load( ). ShaderState.ownAttribute( ): " + array.getName( ) );
        }

        for ( GLUniformData uniform : uniforms )
        {
            this.state.ownUniform( uniform );
            GLErrorUtils.logGLError( logger, gl, "Trouble in GlimpseShaderProgram.load( ). ShaderState.ownUniform( ): " + uniform.getName( ) );
        }

        this.loaded = true;

        return true;
    }
}