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

import static com.metsci.glimpse.gl.util.GLUtils.*;
import static com.metsci.glimpse.util.logging.LoggerUtils.*;
import static javax.media.opengl.GL.*;

import java.io.Serializable;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.media.opengl.GL3;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.GLEditableBuffer;
import com.metsci.glimpse.gl.util.GLUtils;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.support.polygon.Polygon;
import com.metsci.glimpse.support.polygon.Polygon.Interior;
import com.metsci.glimpse.support.polygon.Polygon.Loop.LoopBuilder;
import com.metsci.glimpse.support.polygon.PolygonTessellator;
import com.metsci.glimpse.support.polygon.PolygonTessellator.TessellationException;
import com.metsci.glimpse.support.polygon.SimpleVertexAccumulator;
import com.metsci.glimpse.support.shader.triangle.FlatColorProgram;

/**
 * A simpler/alternate implementation of {@link PolygonPainter} which allows
 * arbitrary polygons to be turned on and off.
 *
 * @author osborn
 */
public class PolygonPainterSimple extends GlimpsePainterBase
{
    private static final Logger logger = Logger.getLogger( PolygonPainterSimple.class.getName( ) );

    protected PolygonTessellator tessellator;

    private long startTime = Long.MIN_VALUE;
    private long endTime = Long.MAX_VALUE;
    private int nextPolyId = 0;

    private NavigableSet<PolyStruct> polyByTime = new TreeSet<PolyStruct>( new TimeComparator( ) );
    private ArrayList<PolyStruct> polyById = new ArrayList<PolyStruct>( );
    private boolean allShown = false;

    protected FlatColorProgram prog;
    protected GLEditableBuffer buffer;

    public PolygonPainterSimple( )
    {
        this.tessellator = new PolygonTessellator( );

        this.prog = new FlatColorProgram( );
        this.buffer = new GLEditableBuffer( GL_DYNAMIC_DRAW, 0 );
    }

    public void setShowOn( int[] ids )
    {
        this.painterLock.lock( );
        try
        {
            for ( int i = 0; i < ids.length; i++ )
            {
                polyById.get( ids[i] ).show = true;
            }
        }
        finally
        {
            this.painterLock.unlock( );
        }
    }

    public void setShowOff( int[] ids )
    {
        this.painterLock.lock( );
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
            this.painterLock.unlock( );
        }
    }

    public void setShowAll( )
    {
        this.painterLock.lock( );
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
            this.painterLock.unlock( );
        }
    }

    public void setShowNone( )
    {
        this.painterLock.lock( );
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
            this.painterLock.unlock( );
        }
    }

    public void setTimeRangeToDraw( long startTime, long endTime )
    {
        this.painterLock.lock( );
        try
        {
            this.startTime = startTime;
            this.endTime = endTime;
        }
        finally
        {
            this.painterLock.unlock( );
        }
    }

    public void setShowAllTimeRange( )
    {
        this.painterLock.lock( );
        try
        {
            startTime = Long.MIN_VALUE;
            endTime = Long.MAX_VALUE;
        }
        finally
        {
            this.painterLock.unlock( );
        }
    }

    public void clear( )
    {
        this.painterLock.lock( );
        try
        {
            polyByTime.clear( );
            polyById.clear( );
            nextPolyId = 0;
        }
        finally
        {
            this.painterLock.unlock( );
        }
    }

    public int addPolygon( long time, float[] dataX, float[] dataY, float[] color )
    {
        this.painterLock.lock( );

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
            this.painterLock.unlock( );
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
    public void doPaintTo( GlimpseContext context )
    {
        Axis2D axis = requireAxis2D( context );
        GL3 gl = context.getGL( ).getGL3( );

        enableStandardBlending( gl );
        try
        {
            prog.begin( gl );
            try
            {
                prog.setAxisOrtho( gl, axis );

                for ( PolyStruct p : polyByTime )
                {
                    float[] color = p.color;
                    float[] data = p.data;

                    if ( !p.show ) continue;

                    if ( p.time > endTime ) continue;

                    if ( p.time < startTime ) continue;

                    prog.setColor( gl, color );

                    // XXX: Repeated edit-after-draw may cause GL pipeline stalls
                    buffer.clear( );
                    FloatBuffer floatBuffer = buffer.editFloats( 0, data.length );
                    floatBuffer.put( data );

                    prog.draw( gl, buffer, 0, data.length / 2 );
                }
            }
            finally
            {
                prog.end( gl );
            }
        }
        finally
        {
            GLUtils.disableBlending( gl );
        }
    }

    @Override
    public void doDispose( GlimpseContext context )
    {
        GL3 gl = context.getGL( ).getGL3( );

        prog.dispose( gl );
        buffer.dispose( gl );
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
