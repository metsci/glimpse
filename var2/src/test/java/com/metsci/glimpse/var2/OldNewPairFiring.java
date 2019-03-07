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
package com.metsci.glimpse.var2;

import java.util.Objects;

class OldNewPairFiring<V>
{

    public final boolean ongoing;
    public final V vOld;
    public final V vNew;


    public OldNewPairFiring( boolean ongoing, V vOld, V vNew )
    {
        this.ongoing = ongoing;
        this.vOld = vOld;
        this.vNew = vNew;
    }

    @Override
    public String toString( )
    {
        return ( ( this.ongoing ? "ongoing" : "completed" ) + ":" + this.vOld.toString( ) + "→" + this.vNew.toString( ) );
    }

    @Override
    public int hashCode( )
    {
        final int prime = 12611;
        int result = 1;
        result = prime * result + Boolean.hashCode( this.ongoing );
        result = prime * result + Objects.hashCode( this.vOld );
        result = prime * result + Objects.hashCode( this.vNew );
        return result;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( o == this ) return true;
        if ( o == null ) return false;
        if ( o.getClass( ) != this.getClass( ) ) return false;

        OldNewPairFiring<?> other = ( OldNewPairFiring<?> ) o;
        return ( other.ongoing == this.ongoing
              && Objects.equals( other.vOld, this.vOld )
              && Objects.equals( other.vNew, this.vNew ) );
    }

}
