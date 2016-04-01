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
package com.metsci.glimpse.axis.painter;

import javax.media.opengl.GL2;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.painter.label.AxisLabelHandler;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.texture.ColorTexture1D;

/**
 * A vertical (y) axis with a color bar and labeled ticks along the right hand side.
 * Suitable for a color bar which should sit flush against the right hand side of a plot.
 *
 * @author ulman
 */
public class ColorYAxisPainter extends NumericYAxisPainter
{
    protected ColorTexture1D colorTexture;

    protected int colorBarSize = 10;
    protected boolean outline = true;

    public ColorYAxisPainter( AxisLabelHandler ticks )
    {
        super( ticks );
        setTickSize( colorBarSize + 0 );
    }

    public void setEnableOutline( boolean doOutline )
    {
        this.outline = doOutline;
    }

    public void setColorScale( ColorTexture1D colorTexture )
    {
        this.colorTexture = colorTexture;
    }

    public void setColorBarSize( int size )
    {
        this.colorBarSize = size;
        setTickSize( colorBarSize + 2 );
    }

    @Override
    public void paintTo( GlimpseContext context, GlimpseBounds bounds, Axis1D axis )
    {
        updateTextRenderer( );
        if ( textRenderer == null ) return;

        GL2 gl = context.getGL( ).getGL2( );

        int width = bounds.getWidth( );
        int height = bounds.getHeight( );

        gl.glBlendFunc( GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA );
        gl.glEnable( GL2.GL_BLEND );

        gl.glMatrixMode( GL2.GL_PROJECTION );
        gl.glLoadIdentity( );
        gl.glOrtho( -0.5, width - 1 + 0.5f, -0.5, height - 1 + 0.5f, -1, 1 );

        paintColorScale( gl, axis, width, height );

        gl.glMatrixMode( GL2.GL_PROJECTION );
        gl.glLoadIdentity( );
        gl.glOrtho( -0.5, width - 1 + 0.5f, axis.getMin( ), axis.getMax( ), -1, 1 );

        paintTicks( gl, axis, width, height );
        paintAxisLabel( gl, axis, width, height );
        paintSelectionLine( gl, axis, width, height );
    }

    protected void paintColorScale( GL2 gl, Axis1D axis, int width, int height )
    {
        if ( colorTexture != null )
        {
            colorTexture.prepare( gl, 0 );

            int x1 = getColorBarMinX( width );
            int x2 = getColorBarMaxX( width );

            gl.glTexEnvf( GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_REPLACE );
            gl.glPolygonMode( GL2.GL_FRONT, GL2.GL_FILL );
            gl.glEnable( GL2.GL_TEXTURE_1D );

            gl.glBegin( GL2.GL_QUADS );
            try
            {
                gl.glTexCoord1f( 0.0f );
                gl.glVertex2f( x2, 0 );

                gl.glTexCoord1f( 0.0f );
                gl.glVertex2f( x1, 0 );

                gl.glTexCoord1f( 1.0f );
                gl.glVertex2f( x1, height - 1 );

                gl.glTexCoord1f( 1.0f );
                gl.glVertex2f( x2, height - 1 );
            }
            finally
            {
                gl.glEnd( );
            }

            gl.glDisable( GL2.GL_TEXTURE_1D );
        }

        if ( outline )
        {
            outlineColorQuad( gl, axis, width, height );
        }
    }

    protected void outlineColorQuad( GL2 gl, Axis1D axis, int width, int height )
    {
        float x1 = getColorBarMinX( width );
        float x2 = getColorBarMaxX( width );

        gl.glColor4fv( tickColor, 0 );

        gl.glBegin( GL2.GL_LINES );
        try
        {
            gl.glVertex2f( x2, 0 );
            gl.glVertex2f( x1, 0 );

            gl.glVertex2f( x1, 0 );
            gl.glVertex2f( x1, height - 1 );

            gl.glVertex2f( x1, height - 1 );
            gl.glVertex2f( x2, height - 1 );

            gl.glVertex2f( x2, height - 1 );
            gl.glVertex2f( x2, 0 );
        }
        finally
        {
            gl.glEnd( );
        }
    }

    public int getColorBarMinX( int width )
    {
        return width - 1 - tickBufferSize - colorBarSize;
    }

    public int getColorBarMaxX( int width )
    {
        return width - 1 - tickBufferSize;
    }
}
