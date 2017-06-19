package com.metsci.glimpse.support.wrapped;

import static com.metsci.glimpse.support.wrapped.NoopWrapper1D.NOOP_WRAPPER_1D;
import static com.metsci.glimpse.support.wrapped.RenderOffset2D.getRenderOffsets;
import static com.metsci.glimpse.support.wrapped.Wrapper1D.getWrapper1D;

import java.util.Collection;
import java.util.Objects;

import com.google.common.collect.ImmutableList;
import com.metsci.glimpse.axis.Axis2D;

public class Wrapper2D
{
    public static final Wrapper2D NOOP_WRAPPER_2D = new Wrapper2D( NOOP_WRAPPER_1D, NOOP_WRAPPER_1D );


    public final Wrapper1D x;
    public final Wrapper1D y;
    public final Collection<? extends RenderOffset2D> renderOffsets;


    public Wrapper2D( Axis2D axis )
    {
        this( getWrapper1D( axis.getAxisX( ) ), getWrapper1D( axis.getAxisY( ) ) );
    }

    public Wrapper2D( Wrapper1D x, Wrapper1D y )
    {
        this.x = x;
        this.y = y;
        this.renderOffsets = ImmutableList.copyOf( getRenderOffsets( this.x, this.y ) );
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
