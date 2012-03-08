/*
 * Copyright (c) 2012, Metron, Inc.
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
package com.metsci.glimpse.painter.track;

import java.nio.FloatBuffer;

import com.metsci.glimpse.util.GeneralUtils;
import com.metsci.glimpse.util.quadtree.Xy;

public class Point implements Comparable<Point>, Xy
{
    protected int trackId;
    protected int pointId;
    protected float x;
    protected float y;
    protected long time;

    public Point( long time )
    {
        this.time = time;
        this.trackId = Integer.MIN_VALUE;
        this.pointId = Integer.MIN_VALUE;
    }

    public Point( int trackId, int pointId, double x, double y, long time )
    {
        this.trackId = trackId;
        this.pointId = pointId;
        this.x = ( float ) x;
        this.y = ( float ) y;
        this.time = time;
    }

    public Point( int trackId, int pointId, float x, float y, long time )
    {
        this.trackId = trackId;
        this.pointId = pointId;
        this.x = x;
        this.y = y;
        this.time = time;
    }

    public void loadIntoBuffer( FloatBuffer buffer )
    {
        buffer.put( x ).put( y );
    }

    public float getX( )
    {
        return x;
    }

    public float getY( )
    {
        return y;
    }

    public int getPointId( )
    {
        return pointId;
    }

    public int getTrackId( )
    {
        return trackId;
    }

    public long getTime( )
    {
        return time;
    }

    public int getId( )
    {
        return pointId;
    }

    @Override
    public float x( )
    {
        return x;
    }

    @Override
    public float y( )
    {
        return y;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( o == null ) return false;
        if ( o == this ) return true;
        if ( o.getClass( ) != this.getClass( ) ) return false;
        Point p = ( Point ) o;
        return p.trackId == trackId && p.pointId == pointId && p.time == time;
    }

    @Override
    public int compareTo( Point p )
    {
        if ( time < p.time )
        {
            return -1;
        }
        else if ( time > p.time )
        {
            return 1;
        }
        else
        {
            if ( trackId < p.trackId )
            {
                return -1;
            }
            else if ( trackId > p.trackId )
            {
                return 1;
            }
            else
            {
                if ( pointId < p.pointId )
                {
                    return -1;
                }
                else if ( pointId > p.pointId )
                {
                    return 1;
                }
                else
                {
                    return 0;
                }
            }
        }
    }

    @Override
    public int hashCode( )
    {
        final int prime = 227;
        int result = 1;
        result = prime * result + trackId;
        result = prime * result + pointId;
        result = prime * result + GeneralUtils.hashCode( time );
        return result;
    }

    @Override
    public String toString( )
    {
        return "[ " + pointId + ", " + trackId + ", " + time + " ]";
    }
}
