/*
 * Copyright (c) 2019 Metron, Inc.
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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author hogye
 */
public class LandFile implements LandShapeCapable
{
    private final LandShape _shape;

    protected LandFile( List<List<LandVertex>> segments, LandBox box, boolean allowSegmentReversal )
    {
        if ( segments == null || box == null )
        {
            _shape = null;
            return;
        }

        List<List<LandVertex>> segments2 = new ArrayList<List<LandVertex>>( segments.size( ) );
        for ( List<LandVertex> segment : segments )
            if ( !segment.isEmpty( ) ) segments2.add( new ArrayList<LandVertex>( segment ) );
        joinConnectedSegments( segments2, allowSegmentReversal );

        List<LandSegment> segments3 = Collections.unmodifiableList( toLandSegments( segments2, box ) );
        _shape = new LandShape( segments3, box );
    }

    /**
     * Reduces segment list to the smallest number of segments by joining connected
     * segments. Empty segments are removed.
     *
     * Iff segment reversal is allowed, segments are joined even if they are connected
     * head-to-head or tail-to-tail. In such a case, we first reverse the vertices of
     * one of the segments so that they are joined head-to-tail, then append as usual.
     *
     * Allowing segment reversal sacrifices any hope of use a segment's winding direction
     * to infer which side its interior is on, but helps with data in which winding
     * direction is meaningless to begin with.
     */
    private static void joinConnectedSegments( List<List<LandVertex>> segments, boolean allowSegmentReversal )
    {
        int joinlessLoopsRemaining = segments.size( );
        while ( joinlessLoopsRemaining > 0 )
        {
            List<LandVertex> base = segments.remove( 0 );
            if ( base.isEmpty( ) )
            {
                joinlessLoopsRemaining--;
                continue;
            }

            boolean anyJoins = false;
            for ( Iterator<List<LandVertex>> i = segments.iterator( ); i.hasNext( ); )
            {
                List<LandVertex> extension = i.next( );

                if ( extension.get( 0 ).equals( base.get( base.size( ) - 1 ) ) )
                {
                    base.addAll( extension );
                    i.remove( );
                    anyJoins = true;
                }
                else if ( allowSegmentReversal && extension.get( extension.size( ) - 1 ).equals( base.get( base.size( ) - 1 ) ) )
                {
                    Collections.reverse( extension );
                    base.addAll( extension );
                    i.remove( );
                    anyJoins = true;
                }
                else if ( allowSegmentReversal && extension.get( 0 ).equals( base.get( 0 ) ) )
                {
                    Collections.reverse( base );
                    base.addAll( extension );
                    i.remove( );
                    anyJoins = true;
                }
            }
            segments.add( base );
            joinlessLoopsRemaining = ( anyJoins ? segments.size( ) : joinlessLoopsRemaining - 1 );
        }
    }

    private static List<LandSegment> toLandSegments( List<List<LandVertex>> segments, LandBox box )
    {
        LandSegmentFactory segmentFactory = new LandSegmentFactory( box );
        List<LandSegment> segments2 = new ArrayList<LandSegment>( );
        for ( List<LandVertex> vertices : segments )
            segments2.add( segmentFactory.newLandSegment( vertices ) );
        return segments2;
    }

    @Override
    public LandShape toShape( )
    {
        return _shape;
    }
}
