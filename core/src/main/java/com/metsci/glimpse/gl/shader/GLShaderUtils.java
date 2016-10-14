package com.metsci.glimpse.gl.shader;

import static com.google.common.base.Charsets.*;
import static com.metsci.glimpse.util.GeneralUtils.*;
import static java.lang.Thread.*;
import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2ES2.*;
import static javax.media.opengl.GL3.*;
import static jogamp.opengl.glu.error.Error.*;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

import javax.media.opengl.GL;
import javax.media.opengl.GL2ES2;

import com.google.common.io.Resources;
import com.metsci.glimpse.util.primitives.IntsArray;

public class GLShaderUtils
{

    public static String requireResourceText( String resourcePath )
    {
        return requireResourceText( resourcePath, UTF_8 );
    }

    public static String requireResourceText( String resourcePath, Charset charset )
    {
        return requireResourceText( currentThread( ).getContextClassLoader( ), resourcePath, charset );
    }

    public static String requireResourceText( Class<?> contextClass, String resourcePath )
    {
        return requireResourceText( contextClass, resourcePath, UTF_8 );
    }

    public static String requireResourceText( Class<?> contextClass, String resourcePath, Charset charset )
    {
        return requireResourceText( contextClass.getClassLoader( ), resourcePath, charset );
    }

    public static String requireResourceText( ClassLoader classLoader, String resourcePath, Charset charset )
    {
        try
        {
            URL url = classLoader.getResource( resourcePath );
            return Resources.toString( url, charset );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e );
        }
    }

    public static int createProgram( GL2ES2 gl, String[] vertSources, String[] geomSources, String[] fragSources )
    {
        IntsArray shaders = new IntsArray( );
        try
        {
            if ( vertSources != null ) shaders.append( compileShader( gl, GL_VERTEX_SHADER, vertSources ) );
            if ( geomSources != null ) shaders.append( compileShader( gl, GL_GEOMETRY_SHADER, geomSources ) );
            if ( fragSources != null ) shaders.append( compileShader( gl, GL_FRAGMENT_SHADER, fragSources ) );
            return linkProgram( gl, shaders.a );
        }
        finally
        {
            deleteShaders( gl, shaders.a );
        }
    }

    public static int createProgram( GL2ES2 gl, String vertSource, String geomSource, String fragSource )
    {
        IntsArray shaders = new IntsArray( );
        try
        {
            if ( vertSource != null ) shaders.append( compileShader( gl, GL_VERTEX_SHADER, vertSource ) );
            if ( geomSource != null ) shaders.append( compileShader( gl, GL_GEOMETRY_SHADER, geomSource ) );
            if ( fragSource != null ) shaders.append( compileShader( gl, GL_FRAGMENT_SHADER, fragSource ) );
            return linkProgram( gl, shaders.a );
        }
        finally
        {
            deleteShaders( gl, shaders.a );
        }
    }

    public static int compileShader( GL2ES2 gl, int shaderType, String source )
    {
        return compileShader( gl, shaderType, array( source ) );
    }

    public static int compileShader( GL2ES2 gl, int shaderType, String[] sources )
    {
        int shader = gl.glCreateShader( shaderType );
        gl.glShaderSource( shader, sources.length, sources, null );

        gl.glCompileShader( shader );
        int[] compileStatus = new int[1];
        gl.glGetShaderiv( shader, GL_COMPILE_STATUS, compileStatus, 0 );
        if ( compileStatus[0] != GL_TRUE )
        {
            throw new RuntimeException( getShaderInfoLog( gl, shader ) );
        }

        return shader;
    }

    public static String getShaderInfoLog( GL2ES2 gl, int shader )
    {
        int[] maxLength = new int[1];
        gl.glGetShaderiv( shader, GL_INFO_LOG_LENGTH, maxLength, 0 );
        if ( maxLength[0] == 0 )
        {
            return "";
        }

        int[] length = new int[1];
        byte[] bytes = new byte[maxLength[0]];
        gl.glGetShaderInfoLog( shader, maxLength[0], length, 0, bytes, 0 );
        return new String( bytes, 0, length[0], UTF_8 );
    }

    public static void requireNoErrors( GL gl )
    {
        int error = gl.glGetError( );
        if ( error != GL_NO_ERROR )
        {
            throw new RuntimeException( gluErrorString( error ) );
        }
    }

    public static int linkProgram( GL2ES2 gl, int... shaders )
    {
        int program = gl.glCreateProgram( );
        attachShaders( gl, program, shaders );
        try
        {
            gl.glLinkProgram( program );

            int[] linkStatus = new int[1];
            gl.glGetProgramiv( program, GL_LINK_STATUS, linkStatus, 0 );
            if ( linkStatus[0] != GL_TRUE )
            {
                throw new RuntimeException( getProgramInfoLog( gl, program ) );
            }

            return program;
        }
        finally
        {
            detachShaders( gl, program, shaders );
        }
    }

    public static void attachShaders( GL2ES2 gl, int program, int... shaders )
    {
        for ( int shader : shaders )
        {
            gl.glAttachShader( program, shader );
        }
    }

    public static void detachShaders( GL2ES2 gl, int program, int... shaders )
    {
        for ( int shader : shaders )
        {
            gl.glDetachShader( program, shader );
        }
    }

    public static void deleteShaders( GL2ES2 gl, int... shaders )
    {
        for ( int shader : shaders )
        {
            gl.glDeleteShader( shader );
        }
    }

    public static String getProgramInfoLog( GL2ES2 gl, int program )
    {
        int[] maxLength = new int[1];
        gl.glGetProgramiv( program, GL_INFO_LOG_LENGTH, maxLength, 0 );
        if ( maxLength[0] == 0 )
        {
            return "";
        }

        int[] length = new int[1];
        byte[] bytes = new byte[maxLength[0]];
        gl.glGetProgramInfoLog( program, maxLength[0], length, 0, bytes, 0 );
        return new String( bytes, 0, length[0], UTF_8 );
    }
}
