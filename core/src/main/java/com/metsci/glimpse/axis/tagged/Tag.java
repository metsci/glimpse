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
package com.metsci.glimpse.axis.tagged;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import com.metsci.glimpse.painter.texture.TaggedHeatMapPainter;
import com.metsci.glimpse.support.colormap.ColorGradient;
import com.metsci.glimpse.support.shader.colormap.ColorMapTaggedProgram;

/**
 * A labeled marker on a {@link TaggedAxis1D}. Tags have a name and a
 * position along their axis. Tags may also have a number of optional
 * named attributes with double precision values. See
 * {@link com.metsci.glimpse.axis.tagged.painter.TaggedPartialColorXAxisPainter}
 * for one use of Tag attributes.<p>
 *
 * Tags may be manipulated programmatically or via mouse interaction.
 * To enable mouse interaction, a {@link TaggedAxisMouseListener1D} must
 * be added to the {@link com.metsci.glimpse.layout.GlimpseAxisLayout1D}
 * for the {@link TaggedAxis1D}.
 *
 * @author ulman
 */
public class Tag
{
    /**
     * Special tag value used by TaggedHeatMapPainter to indicate which part
     * of a {@link ColorGradient} a Tag should be associated with.
     *
     * @see TaggedHeatMapPainter
     * @see ColorMapTaggedProgram
     */
    public static final String TEX_COORD_ATTR = "TexCoord";

    /**
     * Special tag value for setting the display color of a Tag.
     */
    public static final String TAG_COLOR_ATTR = "TagColor";

    public static final Comparator<Tag> tagValueComparator = new Comparator<Tag>( )
    {
        @Override
        public int compare( Tag tag1, Tag tag2 )
        {
            return Double.compare( tag1.value, tag2.value );
        }
    };

    protected String name;
    protected double value;

    // lazily instantiate
    protected Map<String, Object> attributeMap;

    public Tag( Tag tag )
    {
        this.name = tag.name;
        this.value = tag.value;

        if ( tag.attributeMap != null )
        {
            this.attributeMap = new HashMap<String, Object>( );
            this.attributeMap.putAll( tag.attributeMap );
        }
    }

    public Tag( String name, double value )
    {
        this.name = name;
        this.value = value;
    }

    public Tag( String name )
    {
        this( name, 0.0 );
    }

    public String getName( )
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public double getValue( )
    {
        return value;
    }

    public void setValue( double value )
    {
        this.value = value;
    }

    public Tag setAttribute( String key, Object value )
    {
        if ( this.attributeMap == null ) this.attributeMap = new HashMap<String, Object>( );

        this.attributeMap.put( key, value );
        return this;
    }

    public boolean hasAttribute( String key )
    {
        if ( this.attributeMap == null ) return false;

        return this.attributeMap.containsKey( key );
    }

    public Object getAttribute( String key )
    {
        if ( this.attributeMap == null ) return null;

        return this.attributeMap.get( key );
    }

    public float getAttributeFloat( String key )
    {
        if ( this.attributeMap == null )
        {
            throw new IllegalArgumentException( "No value for key: " + key );
        }

        Object value = this.attributeMap.get( key );

        if ( ! ( value instanceof Number ) )
        {
            String message = String.format( "Value for key: %s of type: %s required type: %s", key, value.getClass( ), Number.class );
            throw new ClassCastException( message );
        }

        return ( ( Number ) value ).floatValue( );
    }

    @Override
    public int hashCode( )
    {
        return 31 + ( ( name == null ) ? 0 : name.hashCode( ) );
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass( ) != obj.getClass( ) ) return false;
        Tag other = ( Tag ) obj;
        if ( name == null ) return other.name == null;
        return name.equals( other.name );
    }

    @Override
    public String toString( )
    {
        return String.format( "%s=%.2f", name, value );
    }
}
