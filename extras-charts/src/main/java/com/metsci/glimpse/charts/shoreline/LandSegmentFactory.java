/*
 * Copyright (c) 2016 Metron, Inc.
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
package com.metsci.glimpse.charts.shoreline;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hogye
 */
public class LandSegmentFactory
{
    private final LandBox box;

    public LandSegmentFactory( LandBox box )
    {
        this.box = box;
    }

    /**
     * The returned LandSegment may have land on the wrong side. It is up
     * to the calling code to detect this and correct for it.
     */
    public LandSegment newLandSegment( List<LandVertex> vertices )
    {
        LandVertex start = vertices.get( 0 );
        LandVertex end = vertices.get( vertices.size( ) - 1 );

        if ( start.equals( end ) ) return LandSegment.newFillableSegment( vertices );

        Edge startEdge = Edge.NONE;
        if ( start.lat >= box.northLat )
            startEdge = Edge.NORTH;
        else if ( start.lat <= box.southLat )
            startEdge = Edge.SOUTH;
        else if ( start.lon >= box.eastLon )
            startEdge = Edge.EAST;
        else if ( start.lon <= box.westLon ) startEdge = Edge.WEST;

        Edge endEdge = Edge.NONE;
        if ( end.lat >= box.northLat )
            endEdge = Edge.NORTH;
        else if ( end.lat <= box.southLat )
            endEdge = Edge.SOUTH;
        else if ( end.lon >= box.eastLon )
            endEdge = Edge.EAST;
        else if ( end.lon <= box.westLon ) endEdge = Edge.WEST;

        if ( startEdge == Edge.NONE || endEdge == Edge.NONE ) return LandSegment.newUnfillableSegment( vertices );

        List<LandVertex> ghostVertices = new ArrayList<LandVertex>( );
        if ( startEdge.isSame( endEdge ) )
        {
            // No ghost vertices needed
        }
        else if ( startEdge.isOpposite( endEdge ) )
        {
            switch ( startEdge )
            {
                case NORTH:
                case SOUTH:
                    ghostVertices.add( new LandVertex( end.lat, box.eastLon ) );
                    ghostVertices.add( new LandVertex( start.lat, box.eastLon ) );
                    break;

                case EAST:
                case WEST:
                    ghostVertices.add( new LandVertex( box.northLat, end.lon ) );
                    ghostVertices.add( new LandVertex( box.northLat, start.lon ) );
                    break;
                case NONE:
            }
        }
        else if ( startEdge.isAdjacent( endEdge ) )
        {
            switch ( startEdge )
            {
                case NORTH:
                case SOUTH:
                    ghostVertices.add( new LandVertex( start.lat, end.lon ) );
                    break;

                case EAST:
                case WEST:
                    ghostVertices.add( new LandVertex( end.lat, start.lon ) );
                    break;
                case NONE:
            }
        }

        return LandSegment.newFillableSegment( vertices, ghostVertices );
    }

    private static enum Edge
    {
        NONE, EAST, WEST, NORTH, SOUTH;

        public boolean isSame( Edge edge )
        {
            return ( this != NONE && this == edge );
        }

        public boolean isOpposite( Edge edge )
        {
            switch ( this )
            {
                case EAST:
                    return edge == WEST;
                case WEST:
                    return edge == EAST;
                case NORTH:
                    return edge == SOUTH;
                case SOUTH:
                    return edge == NORTH;
                default:
                    return false;
            }
        }

        public boolean isAdjacent( Edge edge )
        {
            switch ( this )
            {
                case EAST:
                    return edge == NORTH || edge == SOUTH;
                case WEST:
                    return edge == NORTH || edge == SOUTH;
                case NORTH:
                    return edge == EAST || edge == WEST;
                case SOUTH:
                    return edge == EAST || edge == WEST;
                default:
                    return false;
            }
        }
    }

}
