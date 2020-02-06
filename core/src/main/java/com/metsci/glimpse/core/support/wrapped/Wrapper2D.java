/*
 * Copyright (c) 2019, Metron, Inc.
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
package com.metsci.glimpse.core.support.wrapped;

import static com.metsci.glimpse.core.support.wrapped.NoopWrapper1D.NOOP_WRAPPER_1D;
import static com.metsci.glimpse.core.support.wrapped.Wrapper1D.getWrapper1D;

import java.util.Objects;

import com.metsci.glimpse.core.axis.Axis2D;

public class Wrapper2D
{
    public static final Wrapper2D NOOP_WRAPPER_2D = new Wrapper2D( NOOP_WRAPPER_1D, NOOP_WRAPPER_1D );


    public final Wrapper1D x;
    public final Wrapper1D y;


    public Wrapper2D( Axis2D axis )
    {
        this( getWrapper1D( axis.getAxisX( ) ), getWrapper1D( axis.getAxisY( ) ) );
    }

    public Wrapper2D( Wrapper1D x, Wrapper1D y )
    {
        this.x = x;
        this.y = y;
    }

    @Override
    public int hashCode( )
    {
        final int prime = 32941;
        int result = 1;
        result = prime * result + Objects.hashCode( this.x );
        result = prime * result + Objects.hashCode( this.y );
        return result;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( o == this ) return true;
        if ( o == null ) return false;
        if ( o.getClass( ) != this.getClass( ) ) return false;

        Wrapper2D other = ( Wrapper2D ) o;
        return ( Objects.equals( other.x, this.x )
              && Objects.equals( other.y, this.y ) );
    }

}
