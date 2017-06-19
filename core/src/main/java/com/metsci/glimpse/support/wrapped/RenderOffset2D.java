package com.metsci.glimpse.support.wrapped;

import static com.metsci.glimpse.support.wrapped.NoopWrapper1D.NOOP_WRAPPER_1D;

import java.util.Collection;

import com.google.common.collect.ImmutableList;

public class RenderOffset2D
{

    public static final RenderOffset2D ZERO_OFFSET = new RenderOffset2D( 0.0, 0.0 );
    public static final ImmutableList<RenderOffset2D> ZERO_OFFSET_ONLY = ImmutableList.of( ZERO_OFFSET );

    public static Collection<? extends RenderOffset2D> getRenderOffsets( Wrapper1D wrapperX, Wrapper1D wrapperY )
    {
        boolean xNoop = ( wrapperX == NOOP_WRAPPER_1D );
        boolean yNoop = ( wrapperY == NOOP_WRAPPER_1D );

        if ( xNoop && yNoop )
        {
            return ZERO_OFFSET_ONLY;
        }
        else if ( xNoop )
        {
            double ySpan = wrapperY.wrapMax( ) - wrapperY.wrapMin( );
            return ImmutableList.of( new RenderOffset2D( 0.0, -ySpan ),
                                     ZERO_OFFSET,
                                     new RenderOffset2D( 0.0, +ySpan ) );
        }
        else if ( yNoop )
        {
            double xSpan = wrapperX.wrapMax( ) - wrapperX.wrapMin( );
            return ImmutableList.of( new RenderOffset2D( -xSpan, 0.0 ),
                                     ZERO_OFFSET,
                                     new RenderOffset2D( +xSpan, 0.0 ) );
        }
        else
        {
            double ySpan = wrapperY.wrapMax( ) - wrapperY.wrapMin( );
            double xSpan = wrapperX.wrapMax( ) - wrapperX.wrapMin( );
            return ImmutableList.of( new RenderOffset2D( -xSpan, -ySpan ),
                                     new RenderOffset2D( -xSpan,    0.0 ),
                                     new RenderOffset2D( -xSpan, +ySpan ),
                                     new RenderOffset2D(    0.0, -ySpan ),
                                     ZERO_OFFSET,
                                     new RenderOffset2D(    0.0, +ySpan ),
                                     new RenderOffset2D( +xSpan, -ySpan ),
                                     new RenderOffset2D( +xSpan,    0.0 ),
                                     new RenderOffset2D( +xSpan, +ySpan ) );
        }
    }


    public final double x;
    public final double y;


    public RenderOffset2D( double x, double y )
    {
        this.x = x;
        this.y = y;
    }

    @Override
    public int hashCode( )
    {
        final int prime = 22621;
        int result = 1;
        result = prime * result + Double.hashCode( this.x );
        result = prime * result + Double.hashCode( this.y );
        return result;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( o == this ) return true;
        if ( o == null ) return false;
        if ( o.getClass( ) != this.getClass( ) ) return false;

        RenderOffset2D other = ( RenderOffset2D ) o;
        return ( Double.compare( other.x, this.x ) == 0
              && Double.compare( other.y, this.y ) == 0 );
    }

}
