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

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.painter.label.AxisLabelHandler;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.painter.base.GlimpsePainter2D;

/**
 * An alternative {@link BorderPainter} which displays alternating
 * white and black lines (to emulate a geographic map border).
 *
 * @author ulman
 */
public class MapBorderPainter extends GlimpsePainter2D
{
    protected float[] outerColor = new float[] { 0.0f, 0.0f, 0.0f, 1.0f };
    protected float[] innerColor = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };

    protected int borderSize = 20;

    protected AxisLabelHandler ticksX;
    protected AxisLabelHandler ticksY;

    protected double savedValueX = -1;
    protected boolean savedOrientX = false;

    protected double savedValueY = -1;
    protected boolean savedOrientY = false;

    public MapBorderPainter( AxisLabelHandler ticksX, AxisLabelHandler ticksY )
    {
        this.ticksX = ticksX;
        this.ticksY = ticksY;
    }

    public int getBorderSize( )
    {
        return borderSize;
    }

    public void setBorderSize( int size )
    {
        borderSize = size;
    }

    public void setInnerColor( float r, float g, float b, float a )
    {
        innerColor[0] = r;
        innerColor[1] = g;
        innerColor[2] = b;
        innerColor[3] = a;
    }

    public void setInnerColor( float[] rgba )
    {
        innerColor = rgba;
    }

    public void setOuterColor( float r, float g, float b, float a )
    {
        outerColor[0] = r;
        outerColor[1] = g;
        outerColor[2] = b;
        outerColor[3] = a;
    }

    public void setOuterColor( float[] rgba )
    {
        outerColor = rgba;
    }

    private void glDrawCorners( GL2 gl, int type, int width, int height )
    {
        gl.glBegin( type );
        try
        {
            gl.glVertex2d( 0, 0 );
            gl.glVertex2d( 0, borderSize );
            gl.glVertex2d( borderSize, borderSize );
            gl.glVertex2d( borderSize, 0 );
        }
        finally
        {
            gl.glEnd( );
        }

        gl.glBegin( type );
        try
        {
            gl.glVertex2d( 0, height );
            gl.glVertex2d( 0, height - borderSize );
            gl.glVertex2d( borderSize, height - borderSize );
            gl.glVertex2d( borderSize, height );
        }
        finally
        {
            gl.glEnd( );
        }

        gl.glBegin( type );
        try
        {
            gl.glVertex2d( width, 0 );
            gl.glVertex2d( width, borderSize );
            gl.glVertex2d( width - borderSize, borderSize );
            gl.glVertex2d( width - borderSize, 0 );
        }
        finally
        {
            gl.glEnd( );
        }

        gl.glBegin( type );
        try
        {
            gl.glVertex2d( width, height );
            gl.glVertex2d( width, height - borderSize );
            gl.glVertex2d( width - borderSize, height - borderSize );
            gl.glVertex2d( width - borderSize, height );
        }
        finally
        {
            gl.glEnd( );
        }
    }

    private void glSetColor( GL2 gl, int i, boolean orient )
    {
        if ( i % 2 == 0 )
        {
            if ( orient )
            {
                gl.glColor4fv( outerColor, 0 );
            }
            else
            {
                gl.glColor4fv( innerColor, 0 );
            }
        }
        else
        {
            if ( orient )
            {
                gl.glColor4fv( innerColor, 0 );
            }
            else
            {
                gl.glColor4fv( outerColor, 0 );
            }
        }
    }

    private boolean innerOrOuterFirstX( double[] ticks )
    {
        for ( int i = 0; i < ticks.length; i++ )
        {
            if ( ticks[i] == savedValueX )
            {
                if ( i % 2 == 0 )
                {
                    return savedOrientX;
                }
                else
                {
                    return !savedOrientX;
                }
            }
        }

        savedValueX = ticks[ticks.length / 2];
        savedOrientX = true;
        return savedOrientX;
    }

    private boolean innerOrOuterFirstY( double[] ticks )
    {
        for ( int i = 0; i < ticks.length; i++ )
        {
            if ( ticks[i] == savedValueY )
            {
                if ( i % 2 == 0 )
                {
                    return savedOrientY;
                }
                else
                {
                    return !savedOrientY;
                }
            }
        }

        savedValueY = ticks[ticks.length / 2];
        savedOrientY = true;
        return savedOrientY;
    }

    @Override
    public void paintTo( GlimpseContext context, GlimpseBounds bounds, Axis2D axis )
    {
        if ( ticksX == null || ticksY == null ) return;

        Axis1D axisX = axis.getAxisX( );
        Axis1D axisY = axis.getAxisY( );

        if ( axisX == null || axisY == null ) return;

        int width = bounds.getWidth( );
        int height = bounds.getHeight( );

        GL2 gl = context.getGL( ).getGL2( );

        gl.glMatrixMode( GL2.GL_PROJECTION );
        gl.glLoadIdentity( );
        gl.glOrtho( 0, width, 0, height, -1, 1 );
        gl.glMatrixMode( GL2.GL_MODELVIEW );
        gl.glLoadIdentity( );

        double[] xPositions = ticksX.getTickPositions( axis.getAxisX( ) );
        double[] yPositions = ticksY.getTickPositions( axis.getAxisY( ) );

        boolean orientX = innerOrOuterFirstX( xPositions );
        boolean orientY = innerOrOuterFirstY( yPositions );

        for ( int i = 0; i < xPositions.length; i++ )
        {
            glSetColor( gl, i, orientX );

            int pos1X = axisX.valueToScreenPixel( xPositions[i] );
            int pos2X = i == xPositions.length - 1 ? width : axisX.valueToScreenPixel( xPositions[i + 1] );

            gl.glBegin( GL2.GL_POLYGON );
            try
            {
                gl.glVertex2d( pos1X, borderSize );
                gl.glVertex2d( pos1X, 0 );
                gl.glVertex2d( pos2X, 0 );
                gl.glVertex2d( pos2X, borderSize );
            }
            finally
            {
                gl.glEnd( );
            }

            glSetColor( gl, i, !orientX );

            gl.glBegin( GL2.GL_POLYGON );
            try
            {
                gl.glVertex2d( pos1X, height - borderSize );
                gl.glVertex2d( pos1X, height );
                gl.glVertex2d( pos2X, height );
                gl.glVertex2d( pos2X, height - borderSize );
            }
            finally
            {
                gl.glEnd( );
            }
        }

        for ( int i = 0; i < yPositions.length; i++ )
        {
            glSetColor( gl, i, orientY );

            int pos1Y = axisY.valueToScreenPixel( yPositions[i] );
            int pos2Y = i == yPositions.length - 1 ? height : axisY.valueToScreenPixel( yPositions[i + 1] );

            gl.glBegin( GL2.GL_POLYGON );
            try
            {
                gl.glVertex2d( borderSize, pos1Y );
                gl.glVertex2d( 0, pos1Y );
                gl.glVertex2d( 0, pos2Y );
                gl.glVertex2d( borderSize, pos2Y );
            }
            finally
            {
                gl.glEnd( );
            }

            glSetColor( gl, i, !orientY );

            gl.glBegin( GL2.GL_POLYGON );
            try
            {
                gl.glVertex2d( width - borderSize, pos1Y );
                gl.glVertex2d( width, pos1Y );
                gl.glVertex2d( width, pos2Y );
                gl.glVertex2d( width - borderSize, pos2Y );
            }
            finally
            {
                gl.glEnd( );
            }
        }

        gl.glColor4fv( innerColor, 0 );
        glDrawCorners( gl, GL2.GL_POLYGON, width, height );

        gl.glColor4fv( outerColor, 0 );
        glDrawCorners( gl, GL2.GL_LINE_LOOP, width, height );

        gl.glBegin( GL2.GL_LINE_LOOP );
        try
        {
            gl.glVertex2d( borderSize, borderSize );
            gl.glVertex2d( borderSize, height - borderSize );
            gl.glVertex2d( width - borderSize, height - borderSize );
            gl.glVertex2d( width - borderSize, borderSize );
        }
        finally
        {
            gl.glEnd( );
        }

        gl.glBegin( GL2.GL_LINE_LOOP );
        try
        {
            gl.glVertex2d( 0.5, 0.5 );
            gl.glVertex2d( 0.5, height - 0.5 );
            gl.glVertex2d( width - 0.5, height - 0.5 );
            gl.glVertex2d( width - 0.5, 0.5 );
        }
        finally
        {
            gl.glEnd( );
        }
    }
}
