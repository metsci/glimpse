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
package com.metsci.glimpse.axis;

/**
 * A delegate class holding three orthogonal one dimensional axes. Contains convenience methods
 * for accessing the individual "x", "y", and "z" {@link Axis1D}.</p>
 *
 * @author ulman
 * @see com.metsci.glimpse.axis.Axis1D
 */
public class Axis3D extends Axis2D
{
    protected Axis1D z;

    public Axis3D( final Axis1D x, final Axis1D y, final Axis1D z )
    {
        super( x, y );

        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Axis3D( )
    {
        this( new Axis1D( ), new Axis1D( ), new Axis1D( ) );
    }

    @Override
    public Axis3D clone( )
    {
        return new Axis3D( x.clone( ), y.clone( ), z.clone( ) );
    }

    public double getMinZ( )
    {
        return z.getMin( );
    }

    public double getMaxZ( )
    {
        return z.getMax( );
    }

    public Axis1D getAxisZ( )
    {
        return z;
    }

    public void setParent( Axis3D parent )
    {
        if ( x != null ) x.setParent( parent == null ? null : parent.getAxisX( ) );
        if ( y != null ) y.setParent( parent == null ? null : parent.getAxisY( ) );
        if ( z != null ) z.setParent( parent == null ? null : parent.getAxisZ( ) );
    }

    public void setLinkChildren( boolean link )
    {
        if ( x != null ) x.setLinkChildren( link );
        if ( y != null ) y.setLinkChildren( link );
        if ( z != null ) z.setLinkChildren( link );
    }

    public void set( double minX, double maxX, double minY, double maxY, double minZ, double maxZ )
    {
        x.setMin( minX );
        x.setMax( maxX );
        y.setMin( minY );
        y.setMax( maxY );
        z.setMin( minZ );
        z.setMax( maxZ );
    }

    @Override
    public String toString( )
    {
        return String.format( "[%s %s %s]", x, y, z );
    }
}
