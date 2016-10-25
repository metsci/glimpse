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

import java.util.Iterator;

import javax.media.opengl.GL3;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUtessellator;
import javax.media.opengl.glu.GLUtessellatorCallback;

import com.metsci.glimpse.support.polygon.Polygon.Loop;

public class PolygonTessellator
{
    private final GLUtessellator tess;
    private final TessellatorCallback tessCallback;

    public PolygonTessellator( )
    {
        this.tess = GLU.gluNewTess( );

        GLU.gluTessProperty( tess, GLU.GLU_TESS_WINDING_RULE, GLU.GLU_TESS_WINDING_ODD );

        tessCallback = new TessellatorCallback( );
        GLU.gluTessCallback( tess, GLU.GLU_TESS_BEGIN, tessCallback );
        GLU.gluTessCallback( tess, GLU.GLU_TESS_END, tessCallback );
        GLU.gluTessCallback( tess, GLU.GLU_TESS_VERTEX, tessCallback );
        GLU.gluTessCallback( tess, GLU.GLU_TESS_COMBINE, tessCallback );
        GLU.gluTessCallback( tess, GLU.GLU_TESS_EDGE_FLAG, tessCallback );
        GLU.gluTessCallback( tess, GLU.GLU_TESS_ERROR, tessCallback );
    }

    public final int tessellate( Polygon poly, VertexAccumulator accumulator ) throws TessellationException
    {
        tessCallback.reset( accumulator );
        GLU.gluTessBeginPolygon( tess, null );

        Iterator<Loop> loops = poly.getIterator( );
        while ( loops.hasNext( ) )
        {
            GLU.gluTessBeginContour( tess );

            Loop loop = loops.next( );
            for ( int i = 0, n = loop.size( ); i < n; i++ )
            {
                double[] p = loop.get( i );
                GLU.gluTessVertex( tess, p, 0, p );
                tessCallback.checkErrorFlag( );
            }

            GLU.gluTessEndContour( tess );
        }

        GLU.gluTessEndPolygon( tess );
        tessCallback.checkErrorFlag( );

        return tessCallback.getNumTrianglesGenerated( );
    }

    public final void destroy( )
    {
        GLU.gluDeleteTess( tess );
    }

    private static class TessellatorCallback implements GLUtessellatorCallback
    {
        private Adapter currentAdapter;
        private VertexAccumulator currentAccumulator;
        private int nTrianglesGenerated;

        private boolean errorFlag;

        public void reset( VertexAccumulator accumulator )
        {
            this.errorFlag = false;
            this.currentAdapter = null;
            this.currentAccumulator = accumulator;
            nTrianglesGenerated = 0;
        }

        public void checkErrorFlag( ) throws TessellationException
        {
            if ( errorFlag ) throw new TessellationException( );
        }

        public int getNumTrianglesGenerated( )
        {
            return nTrianglesGenerated;
        }

        @Override
        public void begin( int type )
        {
            if ( type == GL3.GL_TRIANGLE_FAN ) currentAdapter = new TriangleFanAdapter( );

            if ( type == GL3.GL_TRIANGLE_STRIP ) currentAdapter = new TriangleStripAdapter( );

            if ( type == GL3.GL_TRIANGLES ) currentAdapter = new TriangleAdapter( );

            if ( currentAdapter == null ) errorFlag = true;
        }

        @Override
        public void end( )
        {
            currentAdapter = null;
        }

        @Override
        public void combine( double[] coords, Object[] data, float[] weight, Object[] outData )
        {
            double[] vertex = new double[3];
            vertex[0] = coords[0];
            vertex[1] = coords[1];
            vertex[2] = coords[2];
            outData[0] = vertex;
        }

        @Override
        public void vertex( Object vertexData )
        {
            currentAdapter.processVertex( ( double[] ) vertexData );
        }

        @Override
        public void edgeFlag( boolean boundaryEdge )
        {
        }

        @Override
        public void error( int errnum )
        {
            errorFlag = true;
        }

        @Override
        public void beginData( int type, Object polygonData )
        {
        }

        @Override
        public void endData( Object polygonData )
        {
        }

        @Override
        public void vertexData( Object arg0, Object arg1 )
        {
        }

        @Override
        public void combineData( double[] coords, Object[] data, float[] weight, Object[] outData, Object polygonData )
        {
        }

        @Override
        public void edgeFlagData( boolean boundaryEdge, Object polygonData )
        {
        }

        @Override
        public void errorData( int errnum, Object polygonData )
        {
        }

        private final class TriangleFanAdapter implements Adapter
        {
            private double[] origin;
            private double[] prevVertex;

            @Override
            public void processVertex( double[] vertex )
            {
                if ( origin == null )
                {
                    origin = vertex;
                    return;
                }

                if ( prevVertex != null && currentAccumulator != null )
                {
                    currentAccumulator.addVertices( origin, 1 );
                    currentAccumulator.addVertices( vertex, 1 );
                    currentAccumulator.addVertices( prevVertex, 1 );
                    nTrianglesGenerated++;
                }

                prevVertex = vertex;
            }
        };

        private final class TriangleStripAdapter implements Adapter
        {
            private double[] prevVertex;
            private double[] prevPrevVertex;

            @Override
            public void processVertex( double[] vertex )
            {
                if ( prevVertex == null )
                {
                    prevVertex = vertex;
                    return;
                }

                if ( prevPrevVertex != null && currentAccumulator != null )
                {
                    currentAccumulator.addVertices( prevPrevVertex, 1 );
                    currentAccumulator.addVertices( prevVertex, 1 );
                    currentAccumulator.addVertices( vertex, 1 );
                    nTrianglesGenerated++;
                }

                prevPrevVertex = prevVertex;
                prevVertex = vertex;
            }
        }

        private final class TriangleAdapter implements Adapter
        {
            private double[] prevVertex;
            private double[] prevPrevVertex;

            @Override
            public void processVertex( double[] vertex )
            {
                if ( prevPrevVertex == null )
                {
                    prevPrevVertex = vertex;
                    return;
                }

                if ( prevVertex == null )
                {
                    prevVertex = vertex;
                    return;
                }

                currentAccumulator.addVertices( prevPrevVertex, 1 );
                currentAccumulator.addVertices( prevVertex, 1 );
                currentAccumulator.addVertices( vertex, 1 );
                prevPrevVertex = null;
                prevVertex = null;
                nTrianglesGenerated++;
            }
        }
    }

    @SuppressWarnings( "serial" )
    public static class TessellationException extends Exception
    {
    }

    private interface Adapter
    {
        void processVertex( double[] vertex );
    }
}
