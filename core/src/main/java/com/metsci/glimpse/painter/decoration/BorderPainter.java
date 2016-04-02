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

import javax.media.opengl.GL2;

import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.painter.base.GlimpsePainterImpl;
import com.metsci.glimpse.support.settings.AbstractLookAndFeel;
import com.metsci.glimpse.support.settings.LookAndFeel;

/**
 * Paints a simple solid color line border around the outside
 * of the plot.
 *
 * @author ulman
 */
public class BorderPainter extends GlimpsePainterImpl
{
    protected float[] borderColor = new float[] { 0.5f, 0.5f, 0.5f, 1.0f };
    protected boolean colorSet = false;

    protected float lineWidth = 1.0f;

    protected int stippleFactor = 1;
    protected short stipplePattern = ( short ) 0x00FF;
    protected boolean stippleOn = false;

    protected boolean drawTop = true;
    protected boolean drawBottom = true;
    protected boolean drawRight = true;
    protected boolean drawLeft = true;

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
        this.stippleOn = dotted;
        return this;
    }

    public BorderPainter setLineWidth( float lineWidth )
    {
        this.lineWidth = lineWidth;
        return this;
    }

    public BorderPainter setColor( float[] rgba )
    {
        borderColor = rgba;
        return this;
    }

    public BorderPainter setColor( float r, float g, float b, float a )
    {
        borderColor[0] = r;
        borderColor[1] = g;
        borderColor[2] = b;
        borderColor[3] = a;
        colorSet = true;
        return this;
    }

    @Override
    public void setLookAndFeel( LookAndFeel laf )
    {
        // ignore the look and feel if a color has been manually set
        if ( !colorSet )
        {
            setColor( laf.getColor( AbstractLookAndFeel.BORDER_COLOR ) );
            colorSet = false;
        }
    }

    @Override
    protected void paintTo( GlimpseContext context, GlimpseBounds bounds )
    {
        GL2 gl = context.getGL( ).getGL2( );

        int x = bounds.getX( );
        int y = bounds.getY( );
        int width = bounds.getWidth( );
        int height = bounds.getHeight( );

        gl.glMatrixMode( GL2.GL_PROJECTION );
        gl.glLoadIdentity( );
        gl.glOrtho( x - 0.5, x + width + 0.5f, y - 0.5, y + height + 0.5f, -1, 1 );

        gl.glBlendFunc( GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA );
        gl.glEnable( GL2.GL_BLEND );

        gl.glLineWidth( lineWidth );
        gl.glColor4fv( borderColor, 0 );

        if ( stippleOn )
        {
            gl.glEnable( GL2.GL_LINE_STIPPLE );
            gl.glLineStipple( stippleFactor, stipplePattern );
        }

        gl.glBegin( GL2.GL_LINES );
        try
        {
            if ( drawBottom )
            {
                gl.glVertex2f( x, y );
                gl.glVertex2f( x + width, y );
            }

            if ( drawRight )
            {
                gl.glVertex2f( x + width, y );
                gl.glVertex2f( x + width, y + height );
            }

            if ( drawTop )
            {
                gl.glVertex2f( x + width, y + height );
                gl.glVertex2f( x, y + height );
            }

            if ( drawLeft )
            {
                gl.glVertex2f( x, y + height );
                gl.glVertex2f( x, y );
            }

        }
        finally
        {
            gl.glEnd( );
        }
    }
}
