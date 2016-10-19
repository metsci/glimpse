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
import com.metsci.glimpse.gl.GLStreamingBufferBuilder;
import com.metsci.glimpse.gl.util.GLUtils;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.support.settings.AbstractLookAndFeel;
import com.metsci.glimpse.support.settings.LookAndFeel;
import com.metsci.glimpse.support.shader.triangle.FlatColorProgram;

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

    protected FlatColorProgram prog;
    protected GLStreamingBufferBuilder builder;

    protected float[] rgba;
    protected float thickness;
    protected boolean stippleEnable;

    public BorderPainter( )
    {
        this.prog = new FlatColorProgram( );
        this.builder = new GLStreamingBufferBuilder( );
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
        this.stippleEnable = dotted;
        return this;
    }

    public BorderPainter setLineWidth( float lineWidth )
    {
        this.thickness = lineWidth;
        return this;
    }

    public BorderPainter setColor( float[] rgba )
    {
        this.rgba = rgba;
        this.colorSet = true;
        return this;
    }

    public BorderPainter setColor( float r, float g, float b, float a )
    {
        this.rgba[0] = r;
        this.rgba[1] = g;
        this.rgba[2] = b;
        this.rgba[3] = a;
        this.colorSet = true;
        return this;
    }

    protected BorderPainter setColor0( float[] rgba )
    {
        this.rgba = rgba;
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

        float inset_PX = 0.5f * thickness;
        float width = bounds.getWidth( );
        float height = bounds.getHeight( );

        GLUtils.enableStandardBlending( gl );
        prog.begin( gl );
        try
        {
            prog.setPixelOrtho( gl, bounds );

            builder.clear( );

            if ( drawBottom )
            {
                // upper quad
                builder.addVertex2f( 0, 0 );
                builder.addVertex2f( drawLeft ? inset_PX : 0, inset_PX );
                builder.addVertex2f( drawRight ? width - inset_PX : width, inset_PX );

                // lower quad
                builder.addVertex2f( 0, 0 );
                builder.addVertex2f( drawRight ? width - inset_PX : width, inset_PX );
                builder.addVertex2f( width, 0 );
            }

            if ( drawLeft )
            {
                // upper quad
                builder.addVertex2f( 0, 0 );
                builder.addVertex2f( 0, height );
                builder.addVertex2f( inset_PX, drawTop ? height - inset_PX : height );

                // lower quad
                builder.addVertex2f( 0, 0 );
                builder.addVertex2f( inset_PX, drawTop ? height - inset_PX : height );
                builder.addVertex2f( inset_PX, drawBottom ? inset_PX : 0 );
            }

            if ( drawTop )
            {
                // upper quad
                builder.addVertex2f( drawLeft ? inset_PX : 0, height - inset_PX );
                builder.addVertex2f( 0, height );
                builder.addVertex2f( width, height );

                // lower quad
                builder.addVertex2f( drawLeft ? inset_PX : 0, height - inset_PX );
                builder.addVertex2f( width, height );
                builder.addVertex2f( drawRight ? width - inset_PX : width, height - inset_PX );
            }

            if ( drawRight )
            {
                // upper quad
                builder.addVertex2f( width - inset_PX, drawBottom ? inset_PX : 0 );
                builder.addVertex2f( width - inset_PX, drawTop ? height - inset_PX : height );
                builder.addVertex2f( width, height );

                // lower quad
                builder.addVertex2f( width - inset_PX, drawBottom ? inset_PX : 0 );
                builder.addVertex2f( width, height );
                builder.addVertex2f( width, 0 );
            }

            prog.draw( gl, builder, rgba );

        }
        finally
        {
            prog.end( gl );
            GLUtils.disableBlending( gl );
        }
    }

    @Override
    protected void doDispose( GlimpseContext context )
    {
        prog.dispose( context.getGL( ).getGL3( ) );
        builder.dispose( context.getGL( ) );
    }
}
