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
package com.metsci.glimpse.examples.layers;

import static com.metsci.glimpse.gl.util.GLUtils.disableBlending;
import static com.metsci.glimpse.gl.util.GLUtils.enableStandardBlending;
import static java.lang.System.currentTimeMillis;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;

import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GL2ES3;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.GLEditableBuffer;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;

public class ExampleGeoPainter extends GlimpsePainterBase
{

    protected final ExampleProgram prog;
    protected final ExampleStyle style;

    protected float tWindowMin;
    protected float tWindowMax;
    protected float xWindowMin;
    protected float xWindowMax;
    protected float yWindowMin;
    protected float yWindowMax;

    protected final GLEditableBuffer txyzBuffer;


    public ExampleGeoPainter( ExampleStyle style )
    {
        this.prog = new ExampleProgram( );
        this.style = new ExampleStyle( style );

        this.tWindowMin = 0;
        this.tWindowMax = 0;
        this.xWindowMin = 0;
        this.xWindowMax = 0;
        this.yWindowMin = 0;
        this.yWindowMax = 0;

        this.txyzBuffer = new GLEditableBuffer( GL_STATIC_DRAW, 0 );
    }

    public void addPoint( float t, float x, float y, float z )
    {
        this.txyzBuffer.grow4f( t, x, y, z );
    }

    public void setTWindow( float tMin, float tMax )
    {
        this.tWindowMin = tMin;
        this.tWindowMax = tMax;
    }

    public void setXyWindow( float xMin, float xMax, float yMin, float yMax )
    {
        this.xWindowMin = xMin;
        this.xWindowMax = xMax;

        this.yWindowMin = yMin;
        this.yWindowMax = yMax;
    }

    @Override
    protected void doPaintTo( GlimpseContext context )
    {
        GlimpseBounds bounds = getBounds( context );
        Axis2D axis = requireAxis2D( context );
        GL2ES3 gl = context.getGL( ).getGL2ES3( );

        enableStandardBlending( gl );
        this.prog.begin( gl );
        try
        {
            this.prog.setViewport( gl, bounds );
            this.prog.setAxisOrtho( gl, axis );

            this.prog.setGeoMode( gl );
            this.prog.setStyle( gl, this.style, currentTimeMillis( ) );
            this.prog.setWindow( gl, this.tWindowMin, this.tWindowMax, this.xWindowMin, this.xWindowMax, this.yWindowMin, this.yWindowMax );

            this.prog.draw( gl, this.txyzBuffer );
        }
        finally
        {
            this.prog.end( gl );
            disableBlending( gl );
        }
    }

    @Override
    protected void doDispose( GlimpseContext context )
    {
        GL2ES2 gl = context.getGL( ).getGL2ES2( );
        this.txyzBuffer.dispose( gl );
        this.prog.dispose( gl );
    }

}
