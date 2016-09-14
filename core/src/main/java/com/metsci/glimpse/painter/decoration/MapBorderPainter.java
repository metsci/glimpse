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

import static javax.media.opengl.GL.*;

import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL3;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.painter.label.AxisLabelHandler;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.support.line.LinePath;
import com.metsci.glimpse.support.line.LineProgram;
import com.metsci.glimpse.support.line.LineStyle;
import com.metsci.glimpse.support.line.util.MappableBuffer;
import com.metsci.glimpse.support.shader.ArrayColorProgram;
import com.metsci.glimpse.support.shader.FlatColorProgram;

/**
 * An alternative {@link BorderPainter} which displays alternating
 * white and black lines (to emulate a geographic map border).
 *
 * @author ulman
 */
public class MapBorderPainter extends GlimpsePainterBase
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
    
    protected ArrayColorProgram triangleProg;
    
    protected MappableBuffer inXys;
    protected MappableBuffer inRgba;
    
//    protected LineStyle style;
//    protected LinePath path;

    public MapBorderPainter( AxisLabelHandler ticksX, AxisLabelHandler ticksY )
    {
        this.ticksX = ticksX;
        this.ticksY = ticksY;
        
        this.inXys = new MappableBuffer( GL_ARRAY_BUFFER, GL_DYNAMIC_DRAW, 10 );
        this.inRgba = new MappableBuffer( GL_ARRAY_BUFFER, GL_DYNAMIC_DRAW, 10 );
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

    private float[] getColor( int i, boolean orient )
    {
        if ( i % 2 == 0 )
        {
            if ( orient )
            {
                return outerColor;
            }
            else
            {
                return innerColor;
            }
        }
        else
        {
            if ( orient )
            {
                return innerColor;
            }
            else
            {
                return outerColor;
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
    public void doPaintTo( GlimpseContext context )
    {
        if ( ticksX == null || ticksY == null ) return;

        GL3 gl = context.getGL( ).getGL3( );
        Axis2D axis = getAxis2D( context );
        GlimpseBounds bounds = getBounds( context );
        
        Axis1D axisX = axis.getAxisX( );
        Axis1D axisY = axis.getAxisY( );

        if ( axisX == null || axisY == null ) return;

        int width = bounds.getWidth( );
        int height = bounds.getHeight( );

        double[] xPositions = ticksX.getTickPositions( axis.getAxisX( ) );
        double[] yPositions = ticksY.getTickPositions( axis.getAxisY( ) );

        boolean orientX = innerOrOuterFirstX( xPositions );
        boolean orientY = innerOrOuterFirstY( yPositions );

        if ( triangleProg == null )
        {
            triangleProg = new ArrayColorProgram( gl );
        }
        
        int count = xPositions.length * 12;
        
        FloatBuffer xy = inXys.mapFloats( gl, count * 2 );
        FloatBuffer rgba = inRgba.mapFloats( gl, count * 4 );
        
        for ( int i = 0; i < xPositions.length; i++ )
        {
            float[] color1 = getColor( i, orientX );

            int pos1X = axisX.valueToScreenPixel( xPositions[i] );
            int pos2X = i == xPositions.length - 1 ? width : axisX.valueToScreenPixel( xPositions[i + 1] );

            xy.put( pos1X ).put( borderSize );
            xy.put( pos1X ).put( 0 );
            xy.put( pos2X ).put( borderSize );

            xy.put( pos2X ).put( 0 );
            xy.put( pos2X ).put( borderSize );
            xy.put( pos1X ).put( 0 );
            
            rgba.put( color1 );
            rgba.put( color1 );
            rgba.put( color1 );
            
            rgba.put( color1 );
            rgba.put( color1 );
            rgba.put( color1 );

            float[]  color2 = getColor( i, !orientX );

            xy.put( pos1X ).put( height - borderSize );
            xy.put( pos1X ).put( height );
            xy.put( pos2X ).put( height - borderSize );

            xy.put( pos2X ).put( height );
            xy.put( pos2X ).put( height - borderSize );
            xy.put( pos1X ).put( height );
            
            rgba.put( color2 );
            rgba.put( color2 );
            rgba.put( color2 );
            
            rgba.put( color2 );
            rgba.put( color2 );
            rgba.put( color2 );
        }
        
        inXys.seal( gl );
        inRgba.seal( gl );
        
        triangleProg.begin( gl );
        try
        {
            triangleProg.setPixelOrtho( gl, bounds );
            
            triangleProg.draw( gl, GL.GL_TRIANGLES, inXys, inRgba, 0, count );
        }
        finally
        {
            triangleProg.end( gl );
        }

//        for ( int i = 0; i < yPositions.length; i++ )
//        {
//            glSetColor( gl, i, orientY );
//
//            int pos1Y = axisY.valueToScreenPixel( yPositions[i] );
//            int pos2Y = i == yPositions.length - 1 ? height : axisY.valueToScreenPixel( yPositions[i + 1] );
//
//            gl.glBegin( GL2.GL_POLYGON );
//            try
//            {
//                gl.glVertex2d( borderSize, pos1Y );
//                gl.glVertex2d( 0, pos1Y );
//                gl.glVertex2d( 0, pos2Y );
//                gl.glVertex2d( borderSize, pos2Y );
//            }
//            finally
//            {
//                gl.glEnd( );
//            }
//
//            glSetColor( gl, i, !orientY );
//
//            gl.glBegin( GL2.GL_POLYGON );
//            try
//            {
//                gl.glVertex2d( width - borderSize, pos1Y );
//                gl.glVertex2d( width, pos1Y );
//                gl.glVertex2d( width, pos2Y );
//                gl.glVertex2d( width - borderSize, pos2Y );
//            }
//            finally
//            {
//                gl.glEnd( );
//            }
//        }
//
//        gl.glColor4fv( innerColor, 0 );
//        glDrawCorners( gl, GL2.GL_POLYGON, width, height );
//
//        gl.glColor4fv( outerColor, 0 );
//        glDrawCorners( gl, GL2.GL_LINE_LOOP, width, height );
//
//        gl.glBegin( GL2.GL_LINE_LOOP );
//        try
//        {
//            gl.glVertex2d( borderSize, borderSize );
//            gl.glVertex2d( borderSize, height - borderSize );
//            gl.glVertex2d( width - borderSize, height - borderSize );
//            gl.glVertex2d( width - borderSize, borderSize );
//        }
//        finally
//        {
//            gl.glEnd( );
//        }
//
//        gl.glBegin( GL2.GL_LINE_LOOP );
//        try
//        {
//            gl.glVertex2d( 0.5, 0.5 );
//            gl.glVertex2d( 0.5, height - 0.5 );
//            gl.glVertex2d( width - 0.5, height - 0.5 );
//            gl.glVertex2d( width - 0.5, 0.5 );
//        }
//        finally
//        {
//            gl.glEnd( );
//        }
    }

    @Override
    protected void doDispose( GlimpseContext context )
    {
        // TODO Auto-generated method stub
        
    }
}
