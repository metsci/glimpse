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
package com.metsci.glimpse.painter.decoration;

import javax.media.opengl.GL3;

import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.support.line.LinePath;
import com.metsci.glimpse.support.line.LineProgram;
import com.metsci.glimpse.support.line.LineStyle;
import com.metsci.glimpse.support.line.util.LineUtils;
import com.metsci.glimpse.support.settings.AbstractLookAndFeel;
import com.metsci.glimpse.support.settings.LookAndFeel;

/**
 * Paints a simple solid color line border around the outside
 * of the plot.
 *
 * @author ulman
 */
public class BorderPainter extends GlimpsePainterBase
{
    protected boolean colorSet = false;

    protected boolean drawTop = true;
    protected boolean drawBottom = true;
    protected boolean drawRight = true;
    protected boolean drawLeft = true;

    protected LineProgram prog;
    protected LineStyle style;
    protected LinePath path;

    public BorderPainter( )
    {
        this.style = new LineStyle( );
        this.style.feather_PX = 0;
        this.style.stippleEnable = false;
        this.style.thickness_PX = 1.0f;
        this.style.stipplePattern = ( short ) 0x00FF;
        this.style.stippleScale = 1;
        this.style.rgba = new float[] { 0.5f, 0.5f, 0.5f, 1.0f };

        this.path = new LinePath( );
    }

    public BorderPainter setDrawTop( boolean draw )
    {
        this.drawTop = draw;
        return this;
    }

    public BorderPainter setDrawBottom( boolean draw )
    {
        this.drawBottom = draw;
        return this;
    }

    public BorderPainter setDrawLeft( boolean draw )
    {
        this.drawLeft = draw;
        return this;
    }

    public BorderPainter setDrawRight( boolean draw )
    {
        this.drawRight = draw;
        return this;
    }

    public BorderPainter setDotted( boolean dotted )
    {
        this.style.stippleEnable = dotted;
        return this;
    }

    public BorderPainter setLineWidth( float lineWidth )
    {
        this.style.thickness_PX = lineWidth;
        return this;
    }

    public BorderPainter setColor( float[] rgba )
    {
        this.style.rgba = rgba;
        this.colorSet = true;
        return this;
    }

    public BorderPainter setColor( float r, float g, float b, float a )
    {
        this.style.rgba[0] = r;
        this.style.rgba[1] = g;
        this.style.rgba[2] = b;
        this.style.rgba[3] = a;
        this.colorSet = true;
        return this;
    }

    protected BorderPainter setColor0( float[] rgba )
    {
        this.style.rgba = rgba;
        return this;
    }

    @Override
    public void setLookAndFeel( LookAndFeel laf )
    {
        // ignore the look and feel if a color has been manually set
        if ( !colorSet )
        {
            setColor0( laf.getColor( AbstractLookAndFeel.BORDER_COLOR ) );
            colorSet = false;
        }
    }

    @Override
    protected void doPaintTo( GlimpseContext context )
    {
        GL3 gl = context.getGL( ).getGL3( );
        GlimpseBounds bounds = getBounds( context );

        int x = bounds.getX( );
        int y = bounds.getY( );
        int width = bounds.getWidth( );
        int height = bounds.getHeight( );

        if ( prog == null )
        {
            prog = new LineProgram( gl );
        }

        LineUtils.enableStandardBlending( gl );
        prog.begin( gl );
        try
        {
            prog.setPixelOrtho( gl, bounds );
            prog.setViewport( gl, bounds );

            path.clear( );

            if ( drawBottom )
            {
                path.moveTo( x, y );
                path.lineTo( x + width, y );
            }

            if ( drawRight )
            {
                path.moveTo( x + width, y );
                path.lineTo( x + width, y + height );
            }

            if ( drawTop )
            {
                path.moveTo( x + width, y + height );
                path.lineTo( x, y + height );
            }

            if ( drawLeft )
            {
                path.moveTo( x, y + height );
                path.lineTo( x, y );
            }

            prog.draw( gl, style, path );

        }
        finally
        {
            prog.end( gl );
            LineUtils.disableStandardBlending( gl );
        }
    }

    @Override
    protected void doDispose( GlimpseContext context )
    {
        // TODO Auto-generated method stub
    }
}
