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
package com.metsci.glimpse.painter.shape;

import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;

import javax.media.opengl.GL2;
import javax.media.opengl.GLContext;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.painter.base.GlimpseDataPainter2D;
import com.metsci.glimpse.support.polygon.Polygon;
import com.metsci.glimpse.support.polygon.Polygon.Interior;
import com.metsci.glimpse.support.polygon.Polygon.Loop.LoopBuilder;
import com.metsci.glimpse.support.polygon.PolygonTessellator;
import com.metsci.glimpse.support.polygon.PolygonTessellator.TessellationException;
import com.metsci.glimpse.support.polygon.SimpleVertexAccumulator;

/**
 * A simpler/alternate implementation of {@link PolygonPainter} which allows
 * arbitrary polygons to be turned on and off.
 *
 * @author osborn
 */
public class PolygonPainterSimple extends GlimpseDataPainter2D
{
    protected ReentrantLock lock = new ReentrantLock( );

    protected PolygonTessellator tessellator;

    private long startTime = Long.MIN_VALUE;
    private long endTime = Long.MAX_VALUE;
    private int nextPolyId = 0;

    private NavigableSet<PolyStruct> polyByTime = new TreeSet<PolyStruct>( new TimeComparator( ) );
    private ArrayList<PolyStruct> polyById = new ArrayList<PolyStruct>( );
    private boolean allShown = false;

    public PolygonPainterSimple( )
    {
        this.tessellator = new PolygonTessellator( );
    }

    public void setShowOn( int[] ids )
    {
        lock.lock( );
        try
        {
            for ( int i = 0; i < ids.length; i++ )
            {
                polyById.get( ids[i] ).show = true;
            }
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void setShowOff( int[] ids )
    {
        lock.lock( );
        try
        {
            for ( int i = 0; i < ids.length; i++ )
            {
                polyById.get( ids[i] ).show = false;
            }

            allShown = false;
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void setShowAll( )
    {
        lock.lock( );
        try
        {
            if ( allShown ) return;

            for ( int i = 0; i < polyById.size( ); i++ )
            {
                polyById.get( i ).show = true;
            }

            allShown = true;
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void setShowNone( )
    {
        lock.lock( );
        try
        {
            for ( int i = 0; i < polyById.size( ); i++ )
            {
                polyById.get( i ).show = false;
            }

            allShown = false;
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void setTimeRangeToDraw( long startTime, long endTime )
    {
        lock.lock( );
        try
        {
            this.startTime = startTime;
            this.endTime = endTime;
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void setShowAllTimeRange( )
    {
        lock.lock( );
        try
        {
            startTime = Long.MIN_VALUE;
            endTime = Long.MAX_VALUE;
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void clear( )
    {
        lock.lock( );
        try
        {
            polyByTime.clear( );
            polyById.clear( );
            nextPolyId = 0;
        }
        finally
        {
            lock.unlock( );
        }
    }

    public int addPolygon( long time, float[] dataX, float[] dataY, float[] color )
    {
        lock.lock( );

        try
        {
            Polygon polygon = buildPolygon( dataX, dataY );

            SimpleVertexAccumulator accumulator = new SimpleVertexAccumulator( );
            tessellator.tessellate( polygon, accumulator );

            PolyStruct p = new PolyStruct( );
            p.data = accumulator.getVertices( );
            p.color = color;
            p.show = true;
            p.time = time;
            p.id = nextPolyId;

            polyByTime.add( p );
            polyById.add( p );
        }
        catch ( TessellationException e )
        {
            logWarning( logger, "Problem tessellating polygon.", e );
        }
        finally
        {
            lock.unlock( );
        }

        return nextPolyId++;
    }

    protected static Polygon buildPolygon( float[] geometryX, float[] geometryY )
    {
        Polygon p = new Polygon( );

        int size = Math.min( geometryX.length, geometryY.length );

        double[] geometry = new double[size * 2];
        for ( int i = 0; i < size; i++ )
        {
            geometry[2 * i] = geometryX[i];
            geometry[2 * i + 1] = geometryY[i];
        }

        LoopBuilder b = new LoopBuilder( );
        b.addVertices( geometry, size );

        p.add( b.complete( Interior.onRight ) );

        return p;
    }

    @Override
    public void paintTo( GL2 gl, GlimpseBounds bounds, Axis2D axis )
    {
        lock.lock( );
        try
        {
            gl.glBegin( GL2.GL_TRIANGLES );
            try
            {
                for ( PolyStruct p : polyByTime )
                {
                    float[] color = p.color;
                    float[] data = p.data;

                    if ( !p.show ) continue;

                    if ( p.time > endTime ) continue;

                    if ( p.time < startTime ) continue;

                    gl.glColor4f( color[0], color[1], color[2], color[3] );
                    for ( int i = 0; i < data.length; i += 2 )
                    {
                        gl.glVertex2f( data[i], data[i + 1] );
                    }
                }
            }
            finally
            {
                gl.glEnd( );
            }
        }
        finally
        {
            lock.unlock( );
        }
    }

    @Override
    public void dispose( GLContext context )
    {
        // nothing to dispose
    }

    private static class PolyStruct
    {
        long id;
        long time;

        float[] data;
        float[] color;
        boolean show;
    }

    @SuppressWarnings( "serial" )
    private static class TimeComparator implements Comparator<PolyStruct>, Serializable
    {
        @Override
        public int compare( PolyStruct p1, PolyStruct p2 )
        {
            if ( p1 == p2 ) return 0;

            if ( p1.time < p2.time )
            {
                return -1;
            }
            else if ( p1.time > p2.time )
            {
                return 1;
            }
            else
            {
                if ( p1.id < p2.id )
                {
                    return -1;
                }
                else if ( p1.id > p2.id )
                {
                    return 1;
                }
                else
                {
                    throw new RuntimeException( );
                }
            }
        }
    }
}
