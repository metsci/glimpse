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

import javax.media.opengl.GL;
import javax.media.opengl.GL3;

import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.GLEditableBuffer;
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
    protected GLEditableBuffer xyBuffer;
    protected GLEditableBuffer mileageBuffer;

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
        this.xyBuffer = new GLEditableBuffer( GL.GL_STATIC_DRAW, 0 );
        this.mileageBuffer = new GLEditableBuffer( GL.GL_STATIC_DRAW, 0 );
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

            xyBuffer.clear( );
            mileageBuffer.clear( );

            float mileage = 0;

            if ( drawBottom )
            {
                // upper quad
                xyBuffer.grow2f( 0, 0 );
                xyBuffer.grow2f( drawLeft ? inset_PX : 0, inset_PX );
                xyBuffer.grow2f( drawRight ? width - inset_PX : width, inset_PX );

                mileageBuffer.grow1f( mileage );
                mileageBuffer.grow1f( drawLeft ? mileage + inset_PX : mileage );
                mileageBuffer.grow1f( drawRight ? mileage + width - inset_PX : mileage + width );

                // lower quad
                xyBuffer.grow2f( 0, 0 );
                xyBuffer.grow2f( drawRight ? width - inset_PX : width, inset_PX );
                xyBuffer.grow2f( width, 0 );

                mileageBuffer.grow1f( mileage );
                mileageBuffer.grow1f( drawRight ? mileage + width - inset_PX : mileage + width );
                mileageBuffer.grow1f( width );

                mileage += width;
            }

            if ( drawRight )
            {
                // upper quad
                xyBuffer.grow2f( width - inset_PX, drawBottom ? inset_PX : 0 );
                xyBuffer.grow2f( width - inset_PX, drawTop ? height - inset_PX : height );
                xyBuffer.grow2f( width, height );

                mileageBuffer.grow1f( drawBottom ? mileage + inset_PX : mileage );
                mileageBuffer.grow1f( drawTop ? mileage + height - inset_PX : mileage + height );
                mileageBuffer.grow1f( mileage + height );

                // lower quad
                xyBuffer.grow2f( width - inset_PX, drawBottom ? inset_PX : 0 );
                xyBuffer.grow2f( width, height );
                xyBuffer.grow2f( width, 0 );

                mileageBuffer.grow1f( drawBottom ? mileage + inset_PX : mileage );
                mileageBuffer.grow1f( mileage + height );
                mileageBuffer.grow1f( mileage );

                mileage += height;
            }

            if ( drawTop )
            {
                // upper quad
                xyBuffer.grow2f( drawLeft ? inset_PX : 0, height - inset_PX );
                xyBuffer.grow2f( 0, height );
                xyBuffer.grow2f( width, height );

                mileageBuffer.grow1f( drawLeft ? mileage + width - inset_PX : mileage + width );
                mileageBuffer.grow1f( mileage + width );
                mileageBuffer.grow1f( mileage );

                // lower quad
                xyBuffer.grow2f( drawLeft ? inset_PX : 0, height - inset_PX );
                xyBuffer.grow2f( width, height );
                xyBuffer.grow2f( drawRight ? width - inset_PX : width, height - inset_PX );

                mileageBuffer.grow1f( drawLeft ? mileage + width - inset_PX : mileage + width );
                mileageBuffer.grow1f( mileage );
                mileageBuffer.grow1f( drawRight ? mileage + inset_PX : mileage );

                mileage += width;
            }

            if ( drawLeft )
            {
                // upper quad
                xyBuffer.grow2f( 0, 0 );
                xyBuffer.grow2f( 0, height );
                xyBuffer.grow2f( inset_PX, drawTop ? height - inset_PX : height );

                mileageBuffer.grow1f( mileage + height );
                mileageBuffer.grow1f( mileage );
                mileageBuffer.grow1f( drawTop ? mileage + inset_PX : mileage );

                // lower quad
                xyBuffer.grow2f( 0, 0 );
                xyBuffer.grow2f( inset_PX, drawTop ? height - inset_PX : height );
                xyBuffer.grow2f( inset_PX, drawBottom ? inset_PX : 0 );

                mileageBuffer.grow1f( mileage + height );
                mileageBuffer.grow1f( drawTop ? mileage + inset_PX : mileage );
                mileageBuffer.grow1f( drawBottom ? mileage + height - inset_PX : mileage + height );

                mileage += height;
            }

            prog.draw( gl, xyBuffer, mileageBuffer, rgba );

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

            xyBuffer.clear( );
            mileageBuffer.clear( );

            float mileage = 0;

            if ( drawBottom )
            {
                // upper quad
                xyBuffer.grow2f( drawLeft ? inset_PX : 0, 0 );
                xyBuffer.grow2f( drawLeft ? inset_PX : 0, inset_PX );
                xyBuffer.grow2f( width, inset_PX );

                mileageBuffer.grow1f( mileage );
                mileageBuffer.grow1f( mileage );
                mileageBuffer.grow1f( drawLeft ? mileage + width - inset_PX : mileage + width );

                // lower quad
                xyBuffer.grow2f( drawLeft ? inset_PX : 0, 0 );
                xyBuffer.grow2f( width, inset_PX );
                xyBuffer.grow2f( width, 0 );

                mileageBuffer.grow1f( mileage );
                mileageBuffer.grow1f( drawLeft ? mileage + width - inset_PX : mileage + width );
                mileageBuffer.grow1f( drawLeft ? mileage + width - inset_PX : mileage + width );

                mileage += drawLeft ? width - inset_PX : width;
            }

            if ( drawRight )
            {
                // upper quad
                xyBuffer.grow2f( width - inset_PX, drawBottom ? inset_PX : 0 );
                xyBuffer.grow2f( width - inset_PX, height );
                xyBuffer.grow2f( width, height );

                mileageBuffer.grow1f( mileage );
                mileageBuffer.grow1f( drawBottom ? mileage + height - inset_PX : mileage + height );
                mileageBuffer.grow1f( drawBottom ? mileage + height - inset_PX : mileage + height );

                // lower quad
                xyBuffer.grow2f( width - inset_PX, drawBottom ? inset_PX : 0 );
                xyBuffer.grow2f( width, height );
                xyBuffer.grow2f( width, drawBottom ? inset_PX : 0 );

                mileageBuffer.grow1f( mileage );
                mileageBuffer.grow1f( drawBottom ? mileage + height - inset_PX : mileage + height );
                mileageBuffer.grow1f( mileage );

                mileage += drawBottom ? height - inset_PX : height;
            }

            if ( drawTop )
            {
                // upper quad
                xyBuffer.grow2f( 0, height - inset_PX );
                xyBuffer.grow2f( 0, height );
                xyBuffer.grow2f( drawRight ? width - inset_PX : width, height );

                mileageBuffer.grow1f( drawRight ? mileage + width - inset_PX : mileage + width );
                mileageBuffer.grow1f( drawRight ? mileage + width - inset_PX : mileage + width );
                mileageBuffer.grow1f( mileage );

                // lower quad
                xyBuffer.grow2f( 0, height - inset_PX );
                xyBuffer.grow2f( drawRight ? width - inset_PX : width, height );
                xyBuffer.grow2f( drawRight ? width - inset_PX : width, height - inset_PX );

                mileageBuffer.grow1f( drawRight ? mileage + width - inset_PX : mileage + width );
                mileageBuffer.grow1f( mileage );
                mileageBuffer.grow1f( mileage );

                mileage += drawRight ? width - inset_PX : width;
            }

            if ( drawLeft )
            {
                // upper quad
                xyBuffer.grow2f( 0, 0 );
                xyBuffer.grow2f( 0, drawTop ? height - inset_PX : height );
                xyBuffer.grow2f( inset_PX, drawTop ? height - inset_PX : height );

                mileageBuffer.grow1f( drawTop ? mileage + height - inset_PX : mileage + height );
                mileageBuffer.grow1f( mileage );
                mileageBuffer.grow1f( mileage );

                // lower quad
                xyBuffer.grow2f( 0, 0 );
                xyBuffer.grow2f( inset_PX, drawTop ? height - inset_PX : height );
                xyBuffer.grow2f( inset_PX, 0 );

                mileageBuffer.grow1f( drawTop ? mileage + height - inset_PX : mileage + height );
                mileageBuffer.grow1f( mileage );
                mileageBuffer.grow1f( drawTop ? mileage + height - inset_PX : mileage + height );
            }

            prog.draw( gl, xyBuffer, mileageBuffer, rgba );

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
        mileageBuffer.dispose( context.getGL( ) );
        xyBuffer.dispose( context.getGL( ) );
    }
}
