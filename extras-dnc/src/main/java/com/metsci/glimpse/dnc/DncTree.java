/*
 * Copyright (c) 2019, Metron, Inc.
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

import static com.metsci.glimpse.dnc.convert.Flat2Query.boxContainsPoint;
import static com.metsci.glimpse.dnc.convert.Flat2Query.boxIntersectsLine;
import static com.metsci.glimpse.dnc.convert.Flat2Query.boxIntersectsTriangle;
import static com.metsci.glimpse.dnc.convert.Query.intsPerQueryLineItem;
import static com.metsci.glimpse.dnc.convert.Query.intsPerQueryPointItem;
import static com.metsci.glimpse.dnc.convert.Query.intsPerQueryTriangleItem;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.last;
import static java.lang.Float.intBitsToFloat;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class DncTree
{

    protected static interface Node
    {
        void search( float xMin, float xMax, float yMin, float yMax, IntSet featureNums );
    }


    protected static class EmptyNode implements Node
    {
        public EmptyNode( )
        { }

        @Override
        public void search( float xMin, float xMax, float yMin, float yMax, IntSet featureNums )
        { }
    }


    protected static class InteriorNode implements Node
    {
        public final float xDivider;
        public final float yDivider;
        public final Node child0;
        public final Node child1;
        public final Node child2;
        public final Node child3;

        public InteriorNode( float xDivider, float yDivider, Node child0, Node child1, Node child2, Node child3 )
        {
            this.xDivider = xDivider;
            this.yDivider = yDivider;
            this.child0 = child0;
            this.child1 = child1;
            this.child2 = child2;
            this.child3 = child3;
        }

        @Override
        public void search( float xMin, float xMax, float yMin, float yMax, IntSet featureNums )
        {
            // Treating both min and max as inclusive simplifies handling of degenerate items (e.g. points)
            boolean includeSmallX = ( xMin <= xDivider );
            boolean includeLargeX = ( xMax >= xDivider );
            boolean includeSmallY = ( yMin <= yDivider );
            boolean includeLargeY = ( yMax >= yDivider );

            if ( includeSmallX && includeSmallY ) child0.search( xMin, xMax, yMin, yMax, featureNums );
            if ( includeLargeX && includeSmallY ) child1.search( xMin, xMax, yMin, yMax, featureNums );
            if ( includeSmallX && includeLargeY ) child2.search( xMin, xMax, yMin, yMax, featureNums );
            if ( includeLargeX && includeLargeY ) child3.search( xMin, xMax, yMin, yMax, featureNums );
        }
    }


    protected class LeafNode implements Node
    {
        public final float xMin;
        public final float xMax;
        public final float yMin;
        public final float yMax;
        public final int pointFirst;
        public final int pointCount;
        public final int lineFirst;
        public final int lineCount;
        public final int triangleFirst;
        public final int triangleCount;

        public LeafNode( float xMin, float xMax, float yMin, float yMax, int pointFirst, int pointCount, int lineFirst, int lineCount, int triangleFirst, int triangleCount )
        {
            this.xMin = xMin;
            this.xMax = xMax;
            this.yMin = yMin;
            this.yMax = yMax;
            this.pointFirst = pointFirst;
            this.pointCount = pointCount;
            this.lineFirst = lineFirst;
            this.lineCount = lineCount;
            this.triangleFirst = triangleFirst;
            this.triangleCount = triangleCount;
        }

        @Override
        public void search( float xMin, float xMax, float yMin, float yMax, IntSet featureNums )
        {
            boolean xAll = ( xMin <= this.xMin && this.xMax <= xMax );
            boolean yAll = ( yMin <= this.yMin && this.yMax <= yMax );
            if ( xAll && yAll )
            {
                for ( int pointNum = pointFirst; pointNum < ( pointFirst + pointCount ); pointNum++ )
                {
                    int featureNum = pointFeatureNum( pointNum );
                    featureNums.add( featureNum );
                }
                for ( int lineNum = lineFirst; lineNum < ( lineFirst + lineCount ); lineNum++ )
                {
                    int featureNum = lineFeatureNum( lineNum );
                    featureNums.add( featureNum );
                }
                for ( int triangleNum = triangleFirst; triangleNum < ( triangleFirst + triangleCount ); triangleNum++ )
                {
                    int featureNum = triangleFeatureNum( triangleNum );
                    featureNums.add( featureNum );
                }
            }
            else
            {
                for ( int pointNum = pointFirst; pointNum < ( pointFirst + pointCount ); pointNum++ )
                {
                    int featureNum = pointFeatureNum( pointNum );
                    if ( !featureNums.contains( featureNum ) )
                    {
                        float x = pointX( pointNum );
                        float y = pointY( pointNum );
                        if ( boxContainsPoint( xMin, yMin, xMax, yMax, x, y ) )
                        {
                            featureNums.add( featureNum );
                        }
                    }
                }
                for ( int lineNum = lineFirst; lineNum < ( lineFirst + lineCount ); lineNum++ )
                {
                    int featureNum = lineFeatureNum( lineNum );
                    if ( !featureNums.contains( featureNum ) )
                    {
                        float xA = lineXA( lineNum );
                        float yA = lineYA( lineNum );
                        float xB = lineXB( lineNum );
                        float yB = lineYB( lineNum );
                        if ( boxIntersectsLine( xMin, yMin, xMax, yMax, xA, yA, xB, yB ) )
                        {
                            featureNums.add( featureNum );
                        }
                    }
                }
                for ( int triangleNum = triangleFirst; triangleNum < ( triangleFirst + triangleCount ); triangleNum++ )
                {
                    int featureNum = triangleFeatureNum( triangleNum );
                    if ( !featureNums.contains( featureNum ) )
                    {
                        float xA = triangleXA( triangleNum );
                        float yA = triangleYA( triangleNum );
                        float xB = triangleXB( triangleNum );
                        float yB = triangleYB( triangleNum );
                        float xC = triangleXC( triangleNum );
                        float yC = triangleYC( triangleNum );
                        if ( boxIntersectsTriangle( xMin, yMin, xMax, yMax, xA, yA, xB, yB, xC, yC ) )
                        {
                            featureNums.add( featureNum );
                        }
                    }
                }
            }
        }
    }




    protected final Node root;
    protected final IntBuffer pointsBuf;
    protected final IntBuffer linesBuf;
    protected final IntBuffer trianglesBuf;


    public DncTree( IntBuffer interiorNodesBuf,
                    IntBuffer leafNodesBuf,
                    IntBuffer pointsBuf,
                    IntBuffer linesBuf,
                    IntBuffer trianglesBuf )
    {
        List<LeafNode> leafNodes = new ArrayList<>( );
        while ( leafNodesBuf.hasRemaining( ) )
        {
            float xMin = intBitsToFloat( leafNodesBuf.get( ) );
            float xMax = intBitsToFloat( leafNodesBuf.get( ) );
            float yMin = intBitsToFloat( leafNodesBuf.get( ) );
            float yMax = intBitsToFloat( leafNodesBuf.get( ) );
            int pointFirst = leafNodesBuf.get( );
            int pointCount = leafNodesBuf.get( );
            int lineFirst = leafNodesBuf.get( );
            int lineCount = leafNodesBuf.get( );
            int triangleFirst = leafNodesBuf.get( );
            int triangleCount = leafNodesBuf.get( );

            leafNodes.add( new LeafNode( xMin, xMax, yMin, yMax, pointFirst, pointCount, lineFirst, lineCount, triangleFirst, triangleCount ) );
        }

        List<InteriorNode> interiorNodes = new ArrayList<>( );
        while ( interiorNodesBuf.hasRemaining( ) )
        {
            float xDivider = intBitsToFloat( interiorNodesBuf.get( ) );
            float yDivider = intBitsToFloat( interiorNodesBuf.get( ) );
            int childNum0 = interiorNodesBuf.get( );
            int childNum1 = interiorNodesBuf.get( );
            int childNum2 = interiorNodesBuf.get( );
            int childNum3 = interiorNodesBuf.get( );

            // A non-negative child number is an index into the interiorNodes list
            // Interior nodes are written post-order depth-first, so children are already in the list
            // A negative child number gets bitwise NOT-ed and used as an index into the leaves list
            Node child0 = ( childNum0 >= 0 ? interiorNodes.get( childNum0 ) : leafNodes.get( ~childNum0 ) );
            Node child1 = ( childNum1 >= 0 ? interiorNodes.get( childNum1 ) : leafNodes.get( ~childNum1 ) );
            Node child2 = ( childNum2 >= 0 ? interiorNodes.get( childNum2 ) : leafNodes.get( ~childNum2 ) );
            Node child3 = ( childNum3 >= 0 ? interiorNodes.get( childNum3 ) : leafNodes.get( ~childNum3 ) );

            interiorNodes.add( new InteriorNode( xDivider, yDivider, child0, child1, child2, child3 ) );
        }

        if ( !interiorNodes.isEmpty( ) )
        {
            this.root = last( interiorNodes );
        }
        else if ( !leafNodes.isEmpty( ) )
        {
            this.root = last( leafNodes );
        }
        else
        {
            this.root = new EmptyNode( );
        }

        this.pointsBuf = pointsBuf;
        this.linesBuf = linesBuf;
        this.trianglesBuf = trianglesBuf;
    }

    public IntSet search( float xMin, float xMax, float yMin, float yMax )
    {
        IntSet featureNums = new IntOpenHashSet( );
        root.search( xMin, xMax, yMin, yMax, featureNums );
        return featureNums;
    }

    /**
     * The featureNums arg is a dual input/output arg. Features already in the set can
     * be pruned early, avoiding potentially expensive computation. New search results
     * are added to the set.
     */
    protected void search( float xMin, float xMax, float yMin, float yMax, IntSet featureNums )
    {
        root.search( xMin, xMax, yMin, yMax, featureNums );
    }


    // Points
    //

    protected int pointFeatureNum( int pointNum )
    {
        return pointsBuf.get( ( pointNum * intsPerQueryPointItem ) + 0 );
    }

    protected float pointX( int pointNum )
    {
        return intBitsToFloat( pointsBuf.get( ( pointNum * intsPerQueryPointItem ) + 1 ) );
    }

    protected float pointY( int pointNum )
    {
        return intBitsToFloat( pointsBuf.get( ( pointNum * intsPerQueryPointItem ) + 2 ) );
    }


    // Lines
    //

    protected int lineFeatureNum( int lineNum )
    {
        return linesBuf.get( ( lineNum * intsPerQueryLineItem ) + 0 );
    }

    protected float lineXA( int lineNum )
    {
        return intBitsToFloat( linesBuf.get( ( lineNum * intsPerQueryLineItem ) + 1 ) );
    }

    protected float lineYA( int lineNum )
    {
        return intBitsToFloat( linesBuf.get( ( lineNum * intsPerQueryLineItem ) + 2 ) );
    }

    protected float lineXB( int lineNum )
    {
        return intBitsToFloat( linesBuf.get( ( lineNum * intsPerQueryLineItem ) + 3 ) );
    }

    protected float lineYB( int lineNum )
    {
        return intBitsToFloat( linesBuf.get( ( lineNum * intsPerQueryLineItem ) + 4 ) );
    }


    // Triangles
    //

    protected int triangleFeatureNum( int triangleNum )
    {
        return trianglesBuf.get( ( triangleNum * intsPerQueryTriangleItem ) + 0 );
    }

    protected float triangleXA( int triangleNum )
    {
        return intBitsToFloat( trianglesBuf.get( ( triangleNum * intsPerQueryTriangleItem ) + 1 ) );
    }

    protected float triangleYA( int triangleNum )
    {
        return intBitsToFloat( trianglesBuf.get( ( triangleNum * intsPerQueryTriangleItem ) + 2 ) );
    }

    protected float triangleXB( int triangleNum )
    {
        return intBitsToFloat( trianglesBuf.get( ( triangleNum * intsPerQueryTriangleItem ) + 3 ) );
    }

    protected float triangleYB( int triangleNum )
    {
        return intBitsToFloat( trianglesBuf.get( ( triangleNum * intsPerQueryTriangleItem ) + 4 ) );
    }

    protected float triangleXC( int triangleNum )
    {
        return intBitsToFloat( trianglesBuf.get( ( triangleNum * intsPerQueryTriangleItem ) + 5 ) );
    }

    protected float triangleYC( int triangleNum )
    {
        return intBitsToFloat( trianglesBuf.get( ( triangleNum * intsPerQueryTriangleItem ) + 6 ) );
    }

}
