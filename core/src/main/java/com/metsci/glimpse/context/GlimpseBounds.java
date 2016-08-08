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
package com.metsci.glimpse.context;

import java.awt.Dimension;

/**
 * Simple storage class storing the position and bounds for a {@link GlimpseTarget}.
 *
 * @author ulman
 */
public class GlimpseBounds
{
    public static final GlimpseBounds EMPTY_BOUNDS = new GlimpseBounds( 0, 0, 0, 0 );

    private int x;
    private int y;
    private int width;
    private int height;

    public GlimpseBounds( Dimension d )
    {
        this( 0, 0, d.width, d.height );
    }

    public GlimpseBounds( int x, int y, int width, int height )
    {
        super( );
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int getX( )
    {
        return x;
    }

    public int getY( )
    {
        return y;
    }

    public int getWidth( )
    {
        return width;
    }

    public int getHeight( )
    {
        return height;
    }

    public boolean isValid( )
    {
        return width > 0 && height > 0;
    }

    /**
     * @param X
     * @param Y
     * @return true if the given point falls inside the rectangle formed by the bounds
     * @see java.awt.Rectangle#inside(int,int)
     */
    public boolean contains( int X, int Y )
    {
        int w = this.width;
        int h = this.height;
        if ( ( w | h ) < 0 )
        {
            // At least one of the dimensions is negative...
            return false;
        }
        // Note: if either dimension is zero, tests below must return false...
        int x = this.x;
        int y = this.y;
        if ( X < x || Y < y )
        {
            return false;
        }
        w += x;
        h += y;
        //    overflow || intersect
        return ( ( w < x || w > X ) && ( h < y || h > Y ) );
    }

    @Override
    public int hashCode( )
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + height;
        result = prime * result + width;
        result = prime * result + x;
        result = prime * result + y;
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass( ) != obj.getClass( ) ) return false;
        GlimpseBounds other = ( GlimpseBounds ) obj;
        if ( height != other.height ) return false;
        if ( width != other.width ) return false;
        if ( x != other.x ) return false;
        if ( y != other.y ) return false;
        return true;
    }

    @Override
    public String toString( )
    {
        return String.format( "%d,%d,%d,%d", x, y, width, height );
    }
}
