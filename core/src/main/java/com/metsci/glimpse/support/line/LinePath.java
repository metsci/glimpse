package com.metsci.glimpse.support.line;

import static com.jogamp.common.nio.Buffers.SIZEOF_FLOAT;
import static com.metsci.glimpse.gl.util.GLUtils.genBuffer;
import static javax.media.opengl.GL.GL_ARRAY_BUFFER;
import static javax.media.opengl.GL.GL_STATIC_DRAW;

import java.nio.FloatBuffer;

import javax.media.opengl.GL;

public class LinePath
{

    protected final LinePathData data;

    protected int xyVbo;
    protected boolean xyDirty;

    protected int connectVbo;
    protected boolean connectDirty;

    protected int mileageVbo;
    protected boolean mileageDirty;


    public LinePath( )
    {
        this( 0 );
    }

    public LinePath( int initialNumVertices )
    {
        this.data = new LinePathData( initialNumVertices );

        this.xyVbo = 0;
        this.xyDirty = true;

        this.connectVbo = 0;
        this.connectDirty = true;

        this.mileageVbo = 0;
        this.mileageDirty = true;
    }

    public void moveTo( float x, float y )
    {
        addVertex( x, y, false );
    }

    public void lineTo( float x, float y )
    {
        addVertex( x, y, true );
    }

    public void addVertex( float x, float y, boolean connect )
    {
        data.addVertex( x, y, connect );
        xyDirty = true;
        connectDirty = true;
    }

    public int numVertices( )
    {
        return data.numVertices( );
    }

    public int xyVbo( GL gl )
    {
        if ( xyVbo == 0 )
        {
            this.xyVbo = genBuffer( gl );
        }

        if ( xyDirty )
        {
            gl.glBindBuffer( GL_ARRAY_BUFFER, xyVbo );

            FloatBuffer xyBuffer = data.xyBuffer( );
            gl.glBufferData( GL_ARRAY_BUFFER, xyBuffer.remaining( )*SIZEOF_FLOAT, xyBuffer, GL_STATIC_DRAW );

            this.xyDirty = false;
        }

        return xyVbo;
    }

    public int connectVbo( GL gl )
    {
        if ( connectVbo == 0 )
        {
            this.connectVbo = genBuffer( gl );
        }

        if ( connectDirty )
        {
            gl.glBindBuffer( GL_ARRAY_BUFFER, connectVbo );

            FloatBuffer connectBuffer = data.connectBuffer( );
            gl.glBufferData( GL_ARRAY_BUFFER, connectBuffer.remaining( )*SIZEOF_FLOAT, connectBuffer, GL_STATIC_DRAW );

            this.connectDirty = false;
        }

        return connectVbo;
    }

    public int mileageVbo( GL gl, double ppvAspectRatio )
    {
        return mileageVbo( gl, ppvAspectRatio, 1.0000000001 );
    }

    public int mileageVbo( GL gl, double ppvAspectRatio, double ppvAspectRatioChangeThreshold )
    {
        int mileageDirtyCount = data.updateMileage( ppvAspectRatio, ppvAspectRatioChangeThreshold );
        this.mileageDirty |= ( mileageDirtyCount > 0 );

        if ( mileageVbo == 0 )
        {
            this.mileageVbo = genBuffer( gl );
        }

        if ( mileageDirty )
        {
            gl.glBindBuffer( GL_ARRAY_BUFFER, mileageVbo );

            FloatBuffer mileageBuffer = data.mileageBuffer( );
            gl.glBufferData( GL_ARRAY_BUFFER, mileageBuffer.remaining( )*SIZEOF_FLOAT, mileageBuffer, GL_STATIC_DRAW );

            this.mileageDirty = false;
        }

        return mileageVbo;
    }

}
