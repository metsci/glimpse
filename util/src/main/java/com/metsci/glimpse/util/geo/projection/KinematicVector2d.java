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
package com.metsci.glimpse.util.geo.projection;

import com.metsci.glimpse.util.vector.Vector2d;

/**
 * Represents kinematic state (position and velocity) within a two coordinate projection of the
 * surface of the Earth (GeoProjection).  The projection itself is not identified within instances
 * of this class.  Immutable.
 *
 * @author moskowitz
 */
public final class KinematicVector2d
{
    private final Vector2d _pos;
    private final Vector2d _vel;

    public KinematicVector2d( Vector2d pos, Vector2d vel )
    {
        _pos = pos;
        _vel = vel;
    }

    public Vector2d getPosition( )
    {
        return _pos;
    }

    public Vector2d getVelocity( )
    {
        return _vel;
    }

    /**
     * Returns the KinematicVector2d produced by time updating with constant velocity motion for for
     * the given amount of time forward.
     *
     * @param   deltaT  amount of time forward
     * @return  new kinematic vector
     */
    public KinematicVector2d timeUpdatedConstantVelocity( double deltaT )
    {
        Vector2d updatedPos = new Vector2d( _pos.getX( ) + ( _vel.getX( ) * deltaT ), _pos.getY( ) + ( _vel.getY( ) * deltaT ) );

        return new KinematicVector2d( updatedPos, _vel );
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( obj == null )
        {
            return false;
        }
        else if ( ! ( obj instanceof KinematicVector2d ) )
        {
            return false;
        }
        else
        {
            KinematicVector2d other = ( KinematicVector2d ) obj;

            return ( _pos.equals( other._pos ) && _vel.equals( other._vel ) );
        }
    }

    @Override
    public int hashCode( )
    {
        return _pos.hashCode( ) ^ _vel.hashCode( );
    }

    /**
     * Get formatted String representation.
     *
     * @param   coordFormat  format applied to each coordinate (as in String.format)
     * @return  formatted string with comma separated coordinates
     */
    public String format( String coordFormat )
    {
        return String.format( "PositionVelocity2d[%s, %s]", _pos.format( coordFormat ), _vel.format( coordFormat ) );
    }

    @Override
    public String toString( )
    {
        return format( "%.4f" );
    }
}
