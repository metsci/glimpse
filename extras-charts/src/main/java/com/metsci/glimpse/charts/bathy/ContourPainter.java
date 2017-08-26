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
package com.metsci.glimpse.charts.bathy;

import com.jogamp.opengl.GL3;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.support.shader.line.LinePath;
import com.metsci.glimpse.support.shader.line.LineProgram;
import com.metsci.glimpse.support.shader.line.LineStyle;

/**
 * @author ulman
 */
public class ContourPainter extends GlimpsePainterBase
{
    protected float[] lineColor = new float[] { 0.5f, 0.5f, 0.5f, 0.5f };
    protected float lineWidth = 1;

    protected float[] coordsX;
    protected float[] coordsY;

    protected LinePath path;
    protected LineStyle style;
    protected LineProgram program;

    public ContourPainter( ContourData data )
    {
        this( data.getCoordsX( ), data.getCoordsY( ) );
    }

    public ContourPainter( float[] coordsX, float[] coordsY )
    {
        this.path = new LinePath( );
        this.style = new LineStyle( );
        this.program = new LineProgram( );

        this.coordsX = coordsX;
        this.coordsY = coordsY;

        int size = Math.min( coordsX.length, coordsY.length );

        for ( int i = 0; i < size - 1; i += 2 )
        {
            this.path.moveTo( coordsX[i], coordsY[i] );
            this.path.lineTo( coordsX[i + 1], coordsY[i + 1] );
        }
    }

    @Override
    public void doPaintTo( GlimpseContext context )
    {
        GL3 gl = context.getGL( ).getGL3( );
        Axis2D axis = requireAxis2D( context );
        GlimpseBounds bounds = getBounds( context );

        this.program.begin( gl );
        try
        {
            this.program.setAxisOrtho( gl, axis );
            this.program.setViewport( gl, bounds );
            this.program.draw( gl, style, path );
        }
        finally
        {
            this.program.end( gl );
        }
    }

    public void setLineColor( float r, float g, float b, float a )
    {
        this.style.rgba = new float[] { r, g, b, a };
    }

    public void setLineWidth( float width )
    {
        this.style.thickness_PX = width;
    }

    public void setStyle( LineStyle style )
    {
        this.style = style;
    }

    @Override
    public void doDispose( GlimpseContext context )
    {
        GL3 gl = context.getGL( ).getGL3( );

        this.program.dispose( gl );
        this.path.dispose( gl );
    }
}
