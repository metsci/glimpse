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
package com.metsci.glimpse.support.polygon;

import java.util.ArrayList;
import java.util.Iterator;

import com.metsci.glimpse.util.PrimitiveVector;

public class Polygon
{
    public enum Interior
    {
        onLeft, onRight
    };

    private ArrayList<Loop> loops;

    public Polygon( )
    {
        loops = new ArrayList<Loop>( );
    }

    public final void add( Loop loop )
    {
        loops.add( loop );
    }

    public Iterator<Loop> getIterator( )
    {
        return loops.listIterator( );
    }

    public final static class Loop
    {
        private final double[] data;
        private final Interior side;

        public static LoopBuilder start( )
        {
            return new LoopBuilder( );
        }

        private Loop( double[] data, Interior side )
        {
            this.data = data;
            this.side = side;
        }

        public final int size( )
        {
            return data.length / 2;
        }

        public Interior getSide( )
        {
            return side;
        }

        public final double[] get( int i )
        {
            return new double[] { data[2 * i], data[2 * i + 1], 0.0 };
        }

        public final static class LoopBuilder implements VertexAccumulator
        {
            PrimitiveVector.Double data = new PrimitiveVector.Double( );

            @Override
            public void addVertices( double[] vertexData, int nVertices )
            {
                data.add( vertexData, 0, 2 * nVertices );
            }

            @Override
            public void addVertices( float[] vertexData, int nVertices )
            {
                data.add( vertexData, 0, 2 * nVertices );
            }

            public final Loop complete( Interior side )
            {
                return new Loop( data.getCopiedData( ), side );
            }
        }
    }
}
