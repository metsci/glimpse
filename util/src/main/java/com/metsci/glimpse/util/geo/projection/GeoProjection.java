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

import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.vector.Vector2d;

/**
 * Interface for projections from LatLonGeo to two coordinates (x, y).
 *
 * @author  moskowitz
 */
public interface GeoProjection
{

    /**
     * Convert LatLon to (nearly) equivalent projected x,y position.
     *
     * @param   latLon
     * @return  projected x, y
     */
    Vector2d project( LatLonGeo latLon );

    /**
     * Convert projected x,y position back to (nearly) equivalent LatLon.
     *
     * @param   x  projected x position
     * @param   y  projected y position
     * @return  latLon
     */
    LatLonGeo unproject( double x, double y );

    /**
     * Reproject an x,y position from a previous projection to this projection.
     *
     * <p>This is equivalent to unprojecting the x,y position from the old projection and then
     * projecting it to this projection, but in a single step, which may be performed more
     * efficiently.</p>
     *
     * @param   x               previously projected x position
     * @param   y               previously projected y position
     * @param   fromProjection  previous projection
     * @return  projected position
     */
    Vector2d reprojectFrom( double x, double y, GeoProjection fromProjection );

    /**
     * Reproject x, y position and corresponding x, y velocity vector at that location from a
     * previous projection to this projection.
     *
     * @param   x               previously projected x position
     * @param   y               previously projected y position
     * @param   vx              previous x velocity component in projection space
     * @param   vy              previous y velocity component in projection space
     * @param   fromProjection  previous projection
     * @return  projected position and velocity
     */
    KinematicVector2d reprojectPosVelFrom( double x, double y, double vx, double vy, GeoProjection fromProjection );

}
