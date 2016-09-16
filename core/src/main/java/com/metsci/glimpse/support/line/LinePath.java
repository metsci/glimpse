package com.metsci.glimpse.support.line;

import static javax.media.opengl.GL.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.media.opengl.GL;

import com.metsci.glimpse.gl.GLStreamingBuffer;

public class LinePath
{

    protected final LinePathData data;

    protected GLStreamingBuffer xyVbo;
    protected boolean xyDirty;

    protected GLStreamingBuffer flagsVbo;
    protected boolean flagsDirty;

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

        this.flagsVbo = new GLStreamingBuffer( GL_ARRAY_BUFFER, GL_STATIC_DRAW, vboBlockSizeFactor );
        this.flagsDirty = true;

        this.mileageVbo = new GLStreamingBuffer( GL_ARRAY_BUFFER, GL_STATIC_DRAW, vboBlockSizeFactor );
        this.mileageDirty = true;
    }

    public void dispose( GL gl )
    {
        this.data.clear( );

        this.xyVbo.dispose( gl );
        this.flagsVbo.dispose( gl );
        this.mileageVbo.dispose( gl );
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
        this.moveTo( x, y, 0f );
    }

    public void moveTo( float x, float y, float mileage )
    {
        this.data.moveTo( x, y, mileage );
        this.setDirty( );
    }

    public void lineTo( float x, float y )
    {
        this.data.lineTo( x, y );
        this.setDirty( );
    }

    public void closeLoop( )
    {
        this.data.closeLoop( );
        this.setDirty( );
    }

    public void clear( )
    {
        this.data.clear( );
        this.setDirty( );
    }

    protected void setDirty( )
    {
        this.xyDirty = true;
        this.flagsDirty = true;
        this.mileageDirty = true;
    }

    public int numVertices( )
    {
        // The vbo() methods append an extra vertex to make sure GL_LINE_STRIP_ADJACENCY works right
        int numVertices = this.data.numVertices( );
        return ( numVertices == 0 ? 0 : numVertices + 1 );
    }

    public GLStreamingBuffer xyVbo( GL gl )
    {
        if ( this.xyDirty )
        {
            FloatBuffer xyData = this.data.xyBuffer( );

            FloatBuffer xyMapped = this.xyVbo.mapFloats( gl, xyData.remaining( ) + 2 );
            xyMapped.put( xyData );

            // Append an extra vertex to make sure GL_LINE_STRIP_ADJACENCY works right
            if ( xyData.position( ) >= 2 )
            {
                xyData.position( xyData.position( ) - 2 );
                float xTrailer = xyData.get( );
                float yTrailer = xyData.get( );
                xyMapped.put( xTrailer ).put( yTrailer );
            }

            this.xyVbo.seal( gl );

            this.xyDirty = false;
        }

        return this.xyVbo;
    }

    public GLStreamingBuffer flagsVbo( GL gl )
    {
        if ( this.flagsDirty )
        {
            ByteBuffer flagsData = this.data.flagsBuffer( );

            ByteBuffer flagsMapped = this.flagsVbo.mapBytes( gl, flagsData.remaining( ) + 1 );
            flagsMapped.put( flagsData );

            // Append an extra vertex to make sure GL_LINE_STRIP_ADJACENCY works right
            if ( flagsData.position( ) >= 1 )
            {
                byte flagsTrailer = ( byte ) 0;
                flagsMapped.put( flagsTrailer );
            }

            this.flagsVbo.seal( gl );

            this.flagsDirty = false;
        }

        return this.flagsVbo;
    }

    public GLStreamingBuffer mileageVbo( GL gl, double ppvAspectRatio )
    {
        return this.mileageVbo( gl, ppvAspectRatio, 1.0000000001 );
    }

    public GLStreamingBuffer mileageVbo( GL gl, double ppvAspectRatio, double ppvAspectRatioChangeThreshold )
    {
        int mileageDirtyCount = this.data.updateMileage( ppvAspectRatio, ppvAspectRatioChangeThreshold );
        this.mileageDirty |= ( mileageDirtyCount > 0 );

        if ( this.mileageDirty )
        {
            FloatBuffer mileageData = this.data.mileageBuffer( );

            FloatBuffer mileageMapped = this.mileageVbo.mapFloats( gl, mileageData.remaining( ) + 1 );
            mileageMapped.put( mileageData );

            // Append an extra vertex to make sure GL_LINE_STRIP_ADJACENCY works right
            if ( mileageData.position( ) >= 1 )
            {
                float mileageTrailer = 0f;
                mileageMapped.put( mileageTrailer );
            }

            this.mileageVbo.seal( gl );

            this.mileageDirty = false;
        }

        return mileageVbo;
    }

}
