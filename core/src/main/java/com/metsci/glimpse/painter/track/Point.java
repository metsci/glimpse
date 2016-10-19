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
package com.metsci.glimpse.painter.track;

import java.util.Comparator;

import com.metsci.glimpse.util.quadtree.Xy;

public class Point implements Xy
{
    protected Object trackId;
    protected Object pointId;
    protected float x;
    protected float y;
    protected long time;

    public Point( long time )
    {
        this.time = time;
        this.trackId = Integer.MIN_VALUE;
        this.pointId = Integer.MIN_VALUE;
    }

    public Point( Object trackId, Object pointId, double x, double y, long time )
    {
        this.trackId = trackId;
        this.pointId = pointId;
        this.x = ( float ) x;
        this.y = ( float ) y;
        this.time = time;
    }

    public Point( Object trackId, Object pointId, float x, float y, long time )
    {
        this.trackId = trackId;
        this.pointId = pointId;
        this.x = x;
        this.y = y;
        this.time = time;
    }

    public float getX( )
    {
        return x;
    }

    public float getY( )
    {
        return y;
    }

    public Object getPointId( )
    {
        return pointId;
    }

    public Object getTrackId( )
    {
        return trackId;
    }

    public long getTime( )
    {
        return time;
    }

    public Object getId( )
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
    public int hashCode( )
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( pointId == null ) ? 0 : pointId.hashCode( ) );
        result = prime * result + ( int ) ( time ^ ( time >>> 32 ) );
        result = prime * result + ( ( trackId == null ) ? 0 : trackId.hashCode( ) );
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass( ) != obj.getClass( ) ) return false;
        Point other = ( Point ) obj;
        if ( pointId == null )
        {
            if ( other.pointId != null ) return false;
        }
        else if ( !pointId.equals( other.pointId ) ) return false;
        if ( time != other.time ) return false;
        if ( trackId == null )
        {
            if ( other.trackId != null ) return false;
        }
        else if ( !trackId.equals( other.trackId ) ) return false;
        return true;
    }

    @Override
    public String toString( )
    {
        return "[ " + pointId + ", " + trackId + ", " + time + " ]";
    }

    public static Comparator<Point> getTimeComparator( )
    {

        return new Comparator<Point>( )
        {
            @Override
            public int compare( Point o1, Point o2 )
            {
                if ( o1.time < o2.time )
                {
                    return -1;
                }
                else if ( o1.time > o2.time )
                {
                    return 1;
                }
                else
                {
                    return 0;
                }
            }
        };
    }
}
