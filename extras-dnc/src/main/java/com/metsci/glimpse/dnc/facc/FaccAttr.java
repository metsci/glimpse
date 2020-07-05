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
package com.metsci.glimpse.dnc.facc;

import java.util.Map;
import java.util.Objects;

import com.google.common.collect.ImmutableMap;

public class FaccAttr
{

    public final String code;
    public final String name;
    public final String text;

    protected final ImmutableMap<Object,Object> valueLookup;


    public FaccAttr( String code, String name, String text, Map<Object,Object> valueLookup )
    {
        this.code = code;
        this.name = name;
        this.text = text;
        this.valueLookup = ( valueLookup == null ? null : ImmutableMap.copyOf( valueLookup ) );
    }

    public Object translateValue( Object rawValue )
    {
        return ( valueLookup == null ? rawValue : valueLookup.get( rawValue ) );
    }

    @Override
    public int hashCode( )
    {
        int prime = 2503;
        int result = 1;
        result = prime * result + Objects.hashCode( code );
        result = prime * result + Objects.hashCode( name );
        result = prime * result + Objects.hashCode( text );
        result = prime * result + Objects.hashCode( valueLookup );
        return result;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( o == this ) return true;
        if ( o == null ) return false;
        if ( o.getClass( ) != getClass( ) ) return false;

        FaccAttr other = ( FaccAttr ) o;
        return ( Objects.equals( other.code, code )
              && Objects.equals( other.name, name )
              && Objects.equals( other.text, text )
              && Objects.equals( other.valueLookup, valueLookup ) );
    }

}
