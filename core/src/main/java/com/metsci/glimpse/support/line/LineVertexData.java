package com.metsci.glimpse.support.line;

import static com.metsci.glimpse.support.line.LineUtils.distance;
import static com.metsci.glimpse.support.line.LineUtils.put1f;
import static com.metsci.glimpse.support.line.LineUtils.put2f;
import static javax.media.opengl.GL.GL_FLOAT;
import static javax.media.opengl.GL.GL_STATIC_DRAW;

import com.jogamp.opengl.util.GLArrayDataClient;
import com.jogamp.opengl.util.GLArrayDataServer;

public class LineVertexData
{

    protected final GLArrayDataClient xyArray;
    protected final GLArrayDataClient cumulativeDistanceArray;

    protected float xHead;
    protected float yHead;
    protected double cumulativeDistanceHead;
    protected boolean connectFromHead;


    public LineVertexData( )
    {
        this.xyArray = GLArrayDataServer.createGLSL( "inXy", 2, GL_FLOAT, false, 0, GL_STATIC_DRAW );
        this.cumulativeDistanceArray = GLArrayDataServer.createGLSL( "inCumulativeDistance", 1, GL_FLOAT, false, 0, GL_STATIC_DRAW );

        this.xHead = 0;
        this.yHead = 0;
        this.cumulativeDistanceHead = 0;
        this.connectFromHead = false;
    }

    public void clear( )
    {
        xyArray.reset( );
        cumulativeDistanceArray.reset( );
        this.connectFromHead = false;
    }

    public void addVertex( float x, float y )
    {
        if ( connectFromHead )
        {
            put2f( xyArray, xHead, yHead );
            put1f( cumulativeDistanceArray, ( float ) cumulativeDistanceHead );

            this.cumulativeDistanceHead += distance( xHead, yHead, x, y );
            this.xHead = x;
            this.yHead = y;

            put2f( xyArray, xHead, yHead );
            put1f( cumulativeDistanceArray, ( float ) cumulativeDistanceHead );
        }
        else
        {
            this.xHead = x;
            this.yHead = y;
            this.cumulativeDistanceHead = 0;
            this.connectFromHead = true;
        }
    }

    public void breakLine( )
    {
        this.connectFromHead = false;
    }

    public void seal( boolean seal )
    {
        xyArray.seal( seal );
        cumulativeDistanceArray.seal( seal );
    }

}
