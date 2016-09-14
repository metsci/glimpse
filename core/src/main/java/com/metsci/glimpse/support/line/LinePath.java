package com.metsci.glimpse.support.line;

import static com.metsci.glimpse.support.line.LineUtils.*;
import static javax.media.opengl.GL.GL_ARRAY_BUFFER;
import static javax.media.opengl.GL.GL_STATIC_DRAW;

import java.nio.FloatBuffer;

import javax.media.opengl.GL;

import com.metsci.glimpse.gl.GLStreamingBuffer;

public class LinePath
{

    protected final LinePathData data;

    protected GLStreamingBuffer xyVbo;
    protected boolean xyDirty;

    protected GLStreamingBuffer connectVbo;
    protected boolean connectDirty;

    protected GLStreamingBuffer mileageVbo;
    protected boolean mileageDirty;


    public LinePath( )
    {
        this( 0, 10 );
    }

    public LinePath( int initialNumVertices, int vboBlockSizeFactor )
    {
        this.data = new LinePathData( initialNumVertices );

        this.xyVbo = new GLStreamingBuffer( GL_ARRAY_BUFFER, GL_STATIC_DRAW, vboBlockSizeFactor );
        this.xyDirty = true;

        this.connectVbo = new GLStreamingBuffer( GL_ARRAY_BUFFER, GL_STATIC_DRAW, vboBlockSizeFactor );
        this.connectDirty = true;

        this.mileageVbo = new GLStreamingBuffer( GL_ARRAY_BUFFER, GL_STATIC_DRAW, vboBlockSizeFactor );
        this.mileageDirty = true;
    }

    public void addRectangle( float x1, float y1, float x2, float y2 )
    {
        moveTo( x1, y1 );
        lineTo( x2, y1 );
        lineTo( x2, y2 );
        lineTo( x1, y2 );
        lineTo( x1, y1 );
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
        this.data.addVertex( x, y, connect );

        this.xyDirty = true;
        this.connectDirty = true;
        this.mileageDirty = true;
    }

    public void clear( )
    {
        this.data.clear( );

        this.xyDirty = true;
        this.connectDirty = true;
        this.mileageDirty = true;
    }

    public int numVertices( )
    {
        // First and last vertices get duplicated for GL_LINE_STRIP_ADJACENCY
        int numVertices = data.numVertices( );
        return ( numVertices == 0 ? 0 : numVertices + 2 );
    }

    public GLStreamingBuffer xyVbo( GL gl )
    {
        if ( xyDirty )
        {
            FloatBuffer xyData = data.xyBuffer( );

            // First and last vertices get duplicated for GL_LINE_STRIP_ADJACENCY
            FloatBuffer xyMapped = xyVbo.mapFloats( gl, xyData.remaining( ) + 4 );
            putWithFirstAndLastDuplicated( xyData, xyMapped, 2 );
            xyVbo.seal( gl );

            this.xyDirty = false;
        }

        return xyVbo;
    }

    public GLStreamingBuffer connectVbo( GL gl )
    {
        if ( connectDirty )
        {
            FloatBuffer connectData = data.connectBuffer( );

            // First and last vertices get duplicated for GL_LINE_STRIP_ADJACENCY
            FloatBuffer connectMapped = connectVbo.mapFloats( gl, connectData.remaining( ) + 2 );
            putWithFirstAndLastDuplicated( connectData, connectMapped, 1 );
            connectVbo.seal( gl );

            this.connectDirty = false;
        }

        return connectVbo;
    }

    public GLStreamingBuffer mileageVbo( GL gl, double ppvAspectRatio )
    {
        return mileageVbo( gl, ppvAspectRatio, 1.0000000001 );
    }

    public GLStreamingBuffer mileageVbo( GL gl, double ppvAspectRatio, double ppvAspectRatioChangeThreshold )
    {
        int mileageDirtyCount = data.updateMileage( ppvAspectRatio, ppvAspectRatioChangeThreshold );
        this.mileageDirty |= ( mileageDirtyCount > 0 );

        if ( mileageDirty )
        {
            FloatBuffer mileageData = data.mileageBuffer( );

            // First and last vertices get duplicated for GL_LINE_STRIP_ADJACENCY
            FloatBuffer mileageMapped = mileageVbo.mapFloats( gl, mileageData.remaining( ) + 2 );
            putWithFirstAndLastDuplicated( mileageData, mileageMapped, 1 );
            mileageVbo.seal( gl );

            this.mileageDirty = false;
        }

        return mileageVbo;
    }

}
