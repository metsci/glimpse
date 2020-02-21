/*
 * Copyright (c) 2020, Metron, Inc.
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
package com.metsci.glimpse.dnc;

import static com.metsci.glimpse.util.GeneralUtils.compareInts;
import static java.lang.String.CASE_INSENSITIVE_ORDER;
import it.unimi.dsi.fastutil.chars.Char2IntMap;
import it.unimi.dsi.fastutil.chars.Char2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.Comparator;

import com.metsci.glimpse.dnc.DncChunks.DncGroup;

public class DncPainterUtils
{


    public static final Comparator<DncGroup> groupRenderingOrder = new Comparator<DncGroup>( )
    {
        public int compare( DncGroup a, DncGroup b )
        {
            int libraryComparison = libraryRenderingOrder.compare( a.chunkKey.library, b.chunkKey.library );
            if ( libraryComparison != 0 ) return libraryComparison;

            int priorityComparison = compareInts( a.geosymAssignment.displayPriority, b.geosymAssignment.displayPriority );
            if ( priorityComparison != 0 ) return priorityComparison;

            // We happen to want delineations in alphabetical order: Area, Line, Point
            int delineationComparison = CASE_INSENSITIVE_ORDER.compare( a.geosymAssignment.delineation, b.geosymAssignment.delineation );
            if ( delineationComparison != 0 ) return delineationComparison;

            // Which row appeared first in the assignment file
            int assignmentIdComparison = compareInts( a.geosymAssignment.id, b.geosymAssignment.id );
            return assignmentIdComparison;
        }
    };


    public static final Comparator<DncLibrary> libraryRenderingOrder = new Comparator<DncLibrary>( )
    {
        private final Char2IntMap typePriorities = new Char2IntOpenHashMap( )
        {{
            defaultReturnValue( -1 );
            put( 'b', 0 );
            put( 'B', 0 );
            put( 'g', 1 );
            put( 'G', 1 );
            put( 'c', 2 );
            put( 'C', 2 );
            put( 'a', 3 );
            put( 'A', 3 );
            put( 'h', 4 );
            put( 'H', 4 );
        }};

        public int compare( DncLibrary a, DncLibrary b )
        {
            int aPriority = typePriorities.get( a.libraryName.charAt( 0 ) );
            int bPriority = typePriorities.get( b.libraryName.charAt( 0 ) );
            return compareInts( aPriority, bPriority );
        }
    };


    public static final Comparator<DncCoverage> coverageSignificanceComparator = new Comparator<DncCoverage>( )
    {
        private final Object2IntMap<String> coverageRanks = new Object2IntOpenHashMap<String>( )
        {{
            put( "ecr", 1 );
            put( "hyd", 2 );
            defaultReturnValue( 3 );
        }};

        public int compare( DncCoverage a, DncCoverage b )
        {
            int aRank = coverageRanks.getInt( a.coverageName );
            int bRank = coverageRanks.getInt( b.coverageName );
            return compareInts( aRank, bRank );
        }
    };


}
