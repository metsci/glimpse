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
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.settings.AbstractLookAndFeel;
import com.metsci.glimpse.support.settings.LookAndFeel;
import com.metsci.glimpse.support.shader.triangle.FlatColorStippleProgram;

/**
 * Paints a simple colored line border around the outside of the plot.
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

    protected FlatColorStippleProgram prog;
    protected GLStreamingBufferBuilder xyBuilder;
    protected GLStreamingBufferBuilder mileageBuilder;

    protected float[] rgba = GlimpseColor.getBlack( );
    protected float thickness = 1.0f;
    protected boolean stippleEnable = false;
    protected float stippleFactor = 1.0f;
    protected short stipplePattern = 0x0F0F;

    protected CornerType cornerType = CornerType.FLAT;

    public static enum CornerType
    {
        FLAT,
        SLANTED;
    }

    public BorderPainter( )
    {
        this.prog = new FlatColorStippleProgram( );
        this.xyBuilder = new GLStreamingBufferBuilder( );
        this.mileageBuilder = new GLStreamingBufferBuilder( );

    }

    public BorderPainter setCornerType( CornerType cornerType )
    {
        this.cornerType = cornerType;
        return this;
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

    public BorderPainter setStipple( boolean enable, float factor, short pattern )
    {
        this.stippleEnable = enable;
        this.stippleFactor = factor;
        this.stipplePattern = pattern;
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

    protected void drawSlantedCorners( GlimpseContext context )
    {
        GL3 gl = context.getGL( ).getGL3( );
        GlimpseBounds bounds = getBounds( context );

        float inset_PX = thickness;
        float width = bounds.getWidth( );
        float height = bounds.getHeight( );

        GLUtils.enableStandardBlending( gl );
        prog.begin( gl );
        try
        {
            prog.setPixelOrtho( gl, bounds );
            prog.setColor( gl, rgba );
            prog.setStipple( gl, stippleEnable, stippleFactor, stipplePattern );

            xyBuilder.clear( );
            mileageBuilder.clear( );

            float mileage = 0;

            if ( drawBottom )
            {
                // upper quad
                xyBuilder.addVertex2f( 0, 0 );
                xyBuilder.addVertex2f( drawLeft ? inset_PX : 0, inset_PX );
                xyBuilder.addVertex2f( drawRight ? width - inset_PX : width, inset_PX );

                mileageBuilder.addVertex1f( mileage );
                mileageBuilder.addVertex1f( drawLeft ? mileage + inset_PX : mileage );
                mileageBuilder.addVertex1f( drawRight ? mileage + width - inset_PX : mileage + width );

                // lower quad
                xyBuilder.addVertex2f( 0, 0 );
                xyBuilder.addVertex2f( drawRight ? width - inset_PX : width, inset_PX );
                xyBuilder.addVertex2f( width, 0 );

                mileageBuilder.addVertex1f( mileage );
                mileageBuilder.addVertex1f( drawRight ? mileage + width - inset_PX : mileage + width );
                mileageBuilder.addVertex1f( width );

                mileage += width;
            }

            if ( drawRight )
            {
                // upper quad
                xyBuilder.addVertex2f( width - inset_PX, drawBottom ? inset_PX : 0 );
                xyBuilder.addVertex2f( width - inset_PX, drawTop ? height - inset_PX : height );
                xyBuilder.addVertex2f( width, height );

                mileageBuilder.addVertex1f( drawBottom ? mileage + inset_PX : mileage );
                mileageBuilder.addVertex1f( drawTop ? mileage + height - inset_PX : mileage + height );
                mileageBuilder.addVertex1f( mileage + height );

                // lower quad
                xyBuilder.addVertex2f( width - inset_PX, drawBottom ? inset_PX : 0 );
                xyBuilder.addVertex2f( width, height );
                xyBuilder.addVertex2f( width, 0 );

                mileageBuilder.addVertex1f( drawBottom ? mileage + inset_PX : mileage );
                mileageBuilder.addVertex1f( mileage + height );
                mileageBuilder.addVertex1f( mileage );

                mileage += height;
            }

            if ( drawTop )
            {
                // upper quad
                xyBuilder.addVertex2f( drawLeft ? inset_PX : 0, height - inset_PX );
                xyBuilder.addVertex2f( 0, height );
                xyBuilder.addVertex2f( width, height );

                mileageBuilder.addVertex1f( drawLeft ? mileage + width - inset_PX : mileage + width );
                mileageBuilder.addVertex1f( mileage + width );
                mileageBuilder.addVertex1f( mileage );

                // lower quad
                xyBuilder.addVertex2f( drawLeft ? inset_PX : 0, height - inset_PX );
                xyBuilder.addVertex2f( width, height );
                xyBuilder.addVertex2f( drawRight ? width - inset_PX : width, height - inset_PX );

                mileageBuilder.addVertex1f( drawLeft ? mileage + width - inset_PX : mileage + width );
                mileageBuilder.addVertex1f( mileage );
                mileageBuilder.addVertex1f( drawRight ? mileage + inset_PX : mileage );

                mileage += width;
            }

            if ( drawLeft )
            {
                // upper quad
                xyBuilder.addVertex2f( 0, 0 );
                xyBuilder.addVertex2f( 0, height );
                xyBuilder.addVertex2f( inset_PX, drawTop ? height - inset_PX : height );

                mileageBuilder.addVertex1f( mileage + height );
                mileageBuilder.addVertex1f( mileage );
                mileageBuilder.addVertex1f( drawTop ? mileage + inset_PX : mileage );

                // lower quad
                xyBuilder.addVertex2f( 0, 0 );
                xyBuilder.addVertex2f( inset_PX, drawTop ? height - inset_PX : height );
                xyBuilder.addVertex2f( inset_PX, drawBottom ? inset_PX : 0 );

                mileageBuilder.addVertex1f( mileage + height );
                mileageBuilder.addVertex1f( drawTop ? mileage + inset_PX : mileage );
                mileageBuilder.addVertex1f( drawBottom ? mileage + height - inset_PX : mileage + height );

                mileage += height;
            }

            prog.draw( gl, xyBuilder, mileageBuilder, rgba );

        }
        finally
        {
            prog.end( gl );
            GLUtils.disableBlending( gl );
        }
    }

    protected void drawFlatCorners( GlimpseContext context )
    {
        GL3 gl = context.getGL( ).getGL3( );
        GlimpseBounds bounds = getBounds( context );

        float inset_PX = thickness;
        float width = bounds.getWidth( );
        float height = bounds.getHeight( );

        GLUtils.enableStandardBlending( gl );
        prog.begin( gl );
        try
        {
            prog.setPixelOrtho( gl, bounds );
            prog.setColor( gl, rgba );
            prog.setStipple( gl, stippleEnable, stippleFactor, stipplePattern );

            xyBuilder.clear( );
            mileageBuilder.clear( );

            float mileage = 0;

            if ( drawBottom )
            {
                // upper quad
                xyBuilder.addVertex2f( drawLeft ? inset_PX : 0, 0 );
                xyBuilder.addVertex2f( drawLeft ? inset_PX : 0, inset_PX );
                xyBuilder.addVertex2f( width, inset_PX );

                mileageBuilder.addVertex1f( mileage );
                mileageBuilder.addVertex1f( mileage );
                mileageBuilder.addVertex1f( drawLeft ? mileage + width - inset_PX : mileage + width );

                // lower quad
                xyBuilder.addVertex2f( drawLeft ? inset_PX : 0, 0 );
                xyBuilder.addVertex2f( width, inset_PX );
                xyBuilder.addVertex2f( width, 0 );

                mileageBuilder.addVertex1f( mileage );
                mileageBuilder.addVertex1f( drawLeft ? mileage + width - inset_PX : mileage + width );
                mileageBuilder.addVertex1f( drawLeft ? mileage + width - inset_PX : mileage + width );

                mileage += drawLeft ? width - inset_PX : width;
            }

            if ( drawRight )
            {
                // upper quad
                xyBuilder.addVertex2f( width - inset_PX, drawBottom ? inset_PX : 0 );
                xyBuilder.addVertex2f( width - inset_PX, height );
                xyBuilder.addVertex2f( width, height );

                mileageBuilder.addVertex1f( mileage );
                mileageBuilder.addVertex1f( drawBottom ? mileage + height - inset_PX : mileage + height );
                mileageBuilder.addVertex1f( drawBottom ? mileage + height - inset_PX : mileage + height );

                // lower quad
                xyBuilder.addVertex2f( width - inset_PX, drawBottom ? inset_PX : 0 );
                xyBuilder.addVertex2f( width, height );
                xyBuilder.addVertex2f( width, drawBottom ? inset_PX : 0 );

                mileageBuilder.addVertex1f( mileage );
                mileageBuilder.addVertex1f( drawBottom ? mileage + height - inset_PX : mileage + height );
                mileageBuilder.addVertex1f( mileage );

                mileage += drawBottom ? height - inset_PX : height;
            }

            if ( drawTop )
            {
                // upper quad
                xyBuilder.addVertex2f( 0, height - inset_PX );
                xyBuilder.addVertex2f( 0, height );
                xyBuilder.addVertex2f( drawRight ? width - inset_PX : width, height );

                mileageBuilder.addVertex1f( drawRight ? mileage + width - inset_PX : mileage + width );
                mileageBuilder.addVertex1f( drawRight ? mileage + width - inset_PX : mileage + width );
                mileageBuilder.addVertex1f( mileage );

                // lower quad
                xyBuilder.addVertex2f( 0, height - inset_PX );
                xyBuilder.addVertex2f( drawRight ? width - inset_PX : width, height );
                xyBuilder.addVertex2f( drawRight ? width - inset_PX : width, height - inset_PX );

                mileageBuilder.addVertex1f( drawRight ? mileage + width - inset_PX : mileage + width );
                mileageBuilder.addVertex1f( mileage );
                mileageBuilder.addVertex1f( mileage );

                mileage += drawRight ? width - inset_PX : width;
            }

            if ( drawLeft )
            {
                // upper quad
                xyBuilder.addVertex2f( 0, 0 );
                xyBuilder.addVertex2f( 0, drawTop ? height - inset_PX : height );
                xyBuilder.addVertex2f( inset_PX, drawTop ? height - inset_PX : height );

                mileageBuilder.addVertex1f( drawTop ? mileage + height - inset_PX : mileage + height );
                mileageBuilder.addVertex1f( mileage );
                mileageBuilder.addVertex1f( mileage );

                // lower quad
                xyBuilder.addVertex2f( 0, 0 );
                xyBuilder.addVertex2f( inset_PX, drawTop ? height - inset_PX : height );
                xyBuilder.addVertex2f( inset_PX, 0 );

                mileageBuilder.addVertex1f( drawTop ? mileage + height - inset_PX : mileage + height );
                mileageBuilder.addVertex1f( mileage );
                mileageBuilder.addVertex1f( drawTop ? mileage + height - inset_PX : mileage + height );
            }

            prog.draw( gl, xyBuilder, mileageBuilder, rgba );

        }
        finally
        {
            prog.end( gl );
            GLUtils.disableBlending( gl );
        }
    }

    @Override
    protected void doPaintTo( GlimpseContext context )
    {
        switch ( cornerType )
        {
            case SLANTED:
                drawSlantedCorners( context );
                break;
            case FLAT:
            default:
                drawFlatCorners( context );
                break;
        }
    }

    @Override
    protected void doDispose( GlimpseContext context )
    {
        prog.dispose( context.getGL( ).getGL3( ) );
        mileageBuilder.dispose( context.getGL( ) );
        xyBuilder.dispose( context.getGL( ) );
    }
}
