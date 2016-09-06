package com.metsci.glimpse.support.line;

import static javax.media.opengl.GL.GL_ARRAY_BUFFER;
import static javax.media.opengl.GL.GL_STATIC_DRAW;

import javax.media.opengl.GL;

import com.metsci.glimpse.support.line.util.MappableBuffer;

public class LinePath
{

    protected final LinePathData data;

    protected MappableBuffer xyVbo;
    protected boolean xyDirty;

    protected MappableBuffer connectVbo;
    protected boolean connectDirty;

    protected MappableBuffer mileageVbo;
    protected boolean mileageDirty;


    public LinePath( )
    {
        this( 0, 10 );
    }

    public LinePath( int initialNumVertices, int vboBlockSizeFactor )
    {
        this.data = new LinePathData( initialNumVertices );

        this.xyVbo = new MappableBuffer( GL_ARRAY_BUFFER, GL_STATIC_DRAW, vboBlockSizeFactor );
        this.xyDirty = true;

        this.connectVbo = new MappableBuffer( GL_ARRAY_BUFFER, GL_STATIC_DRAW, vboBlockSizeFactor );
        this.connectDirty = true;

        this.mileageVbo = new MappableBuffer( GL_ARRAY_BUFFER, GL_STATIC_DRAW, vboBlockSizeFactor );
        this.mileageDirty = true;
    }
    
    public void clear( )
    {
        this.data.clear( );
        xyDirty = true;
        connectDirty = true;
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

    public MappableBuffer xyVbo( GL gl )
    {
        if ( xyDirty )
        {
            xyVbo.setFloats( gl, data.xyBuffer( ) );
            this.xyDirty = false;
        }

        return xyVbo;
    }

    public MappableBuffer connectVbo( GL gl )
    {
        if ( connectDirty )
        {
            connectVbo.setFloats( gl, data.connectBuffer( ) );
            this.connectDirty = false;
        }

        return connectVbo;
    }

    public MappableBuffer mileageVbo( GL gl, double ppvAspectRatio )
    {
        return mileageVbo( gl, ppvAspectRatio, 1.0000000001 );
    }

    public MappableBuffer mileageVbo( GL gl, double ppvAspectRatio, double ppvAspectRatioChangeThreshold )
    {
        int mileageDirtyCount = data.updateMileage( ppvAspectRatio, ppvAspectRatioChangeThreshold );
        this.mileageDirty |= ( mileageDirtyCount > 0 );

        if ( mileageDirty )
        {
            mileageVbo.setFloats( gl, data.mileageBuffer( ) );
            this.mileageDirty = false;
        }

        return mileageVbo;
    }

}
