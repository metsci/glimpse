/*
 * Copyright (c) 2016, Metron, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Metron, Inc. nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL METRON, INC. BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.metsci.glimpse.gl.shader;

import static com.metsci.glimpse.util.logging.LoggerUtils.logFine;

import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import javax.media.opengl.GL2;

/**
 * A wrapper for a shader variable declared in an GLSL source file.
 *
 * @author osborn
 */
public class ShaderArg
{
    private static final Logger logger = Logger.getLogger( ShaderArg.class.getName( ) );

    private final ReentrantLock lock = new ReentrantLock( );

    private final String name;
    private String toStringResult;

    private final ShaderArgType type;
    private final ShaderArgQualifier qual;
    private final ShaderArgInOut inout;

    private Object lastPushedValue;
    private Object currentValue;
    private boolean dirty;

    public ShaderArg( String name, ShaderArgType type, ShaderArgQualifier qual, ShaderArgInOut inout )
    {
        this.name = name;
        this.type = type;
        this.qual = qual;
        this.inout = inout;

        makeDirty( );
    }

    public String getName( )
    {
        return name;
    }

    public ShaderArgType getType( )
    {
        return type;
    }

    public ShaderArgQualifier getQual( )
    {
        return qual;
    }

    public ShaderArgInOut getInOut( )
    {
        return inout;
    }

    public boolean isDirty( )
    {
        return dirty;
    }

    public void makeDirty( )
    {
        dirty = true;
    }

    public void setValue( Object val )
    {
        lock.lock( );
        try
        {
            currentValue = val;
            if ( lastPushedValue != null ) if ( !currentValue.equals( lastPushedValue ) ) makeDirty( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    protected void update( GL2 gl, int glArgHandle )
    {
        lock.lock( );
        try
        {
            if ( lastPushedValue != null ) if ( currentValue.equals( lastPushedValue ) )
            {
                dirty = false;
                return;
            }

            switch ( type )
            {
                case SAMPLER_2D_ARRAY:
                case SAMPLER_1D_ARRAY:
                case SAMPLER_CUBE:
                case SAMPLER_2D:
                case ISAMPLER_2D:
                case USAMPLER_2D:
                case SAMPLER_1D:
                case ISAMPLER_1D:
                case USAMPLER_1D:
                case INT:
                {
                    logFine( logger, "Updating %s to %s.", toString( ), currentValue.toString( ) );
                    gl.glUniform1i( glArgHandle, getIntValue( ) );
                    break;
                }
                case FLOAT:
                {
                    logFine( logger, "Updating %s to %s.", toString( ), currentValue.toString( ) );
                    gl.glUniform1f( glArgHandle, getFloatValue( ) );
                    break;
                }
                case BOOLEAN:
                {
                    logFine( logger, "Updating %s to %s.", toString( ), currentValue.toString( ) );
                    gl.glUniform1i( glArgHandle, getBooleanValue( ) ? 1 : 0 );
                    break;
                }
                case VEC2:
                    gl.glUniform2fv( glArgHandle, 1, getFloatArrayValue( ), 0 );
                    break;
                case VEC3:
                    gl.glUniform3fv( glArgHandle, 1, getFloatArrayValue( ), 0 );
                    break;
                case VEC4:
                    gl.glUniform4fv( glArgHandle, 1, getFloatArrayValue( ), 0 );
                    break;
                case MAT2:
                    gl.glUniformMatrix2fv( glArgHandle, 1, false, getFloatArrayValue( ), 0 );
                    break;
                case MAT3:
                    gl.glUniformMatrix3fv( glArgHandle, 1, false, getFloatArrayValue( ), 0 );
                    break;
                case MAT4:
                    gl.glUniformMatrix4fv( glArgHandle, 1, false, getFloatArrayValue( ), 0 );
                    break;
                default:
                {
                    throw new UnsupportedOperationException( "Unsupported type." );
                }
            }
            lastPushedValue = currentValue;
            dirty = false;
        }
        finally
        {
            lock.unlock( );
        }
    }

    private int getIntValue( )
    {
        return ( ( Number ) currentValue ).intValue( );
    }

    private float getFloatValue( )
    {
        return ( ( Number ) currentValue ).floatValue( );
    }

    private boolean getBooleanValue( )
    {
        return ( Boolean ) currentValue;
    }

    private float[] getFloatArrayValue( )
    {
        return ( float[] ) currentValue;
    }

    @Override
    public String toString( )
    {
        if ( toStringResult == null )
        {
            StringBuilder b = new StringBuilder( );

            b.append( "'" );

            if ( inout != null ) b.append( inout ).append( " " );

            if ( qual != null ) b.append( qual ).append( " " );

            if ( type != null ) b.append( type ).append( " " );

            if ( name != null ) b.append( name );

            b.append( "'" );

            toStringResult = b.toString( );
        }

        return toStringResult;
    }
}
