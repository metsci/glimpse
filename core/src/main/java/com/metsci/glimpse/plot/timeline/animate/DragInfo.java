package com.metsci.glimpse.plot.timeline.animate;

import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.plot.stacked.PlotInfo;

public class DragInfo
{
    public PlotInfo info;
    public GlimpseBounds bounds;
    public double size;
    public boolean top;

    public DragInfo( PlotInfo info, GlimpseBounds bounds, double size )
    {
        this.info = info;
        this.bounds = bounds;
        this.size = size;
    }

    public DragInfo( DragInfo drag, double size )
    {
        this.info = drag.info;
        this.bounds = drag.bounds;
        this.top = drag.top;
        this.size = size;
    }

    public DragInfo( PlotInfo info, double size, boolean top )
    {
        this.info = info;
        this.size = size;
        this.top = top;
    }

    @Override
    public int hashCode( )
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( info == null ) ? 0 : info.hashCode( ) );
        result = prime * result + ( top ? 1231 : 1237 );
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass( ) != obj.getClass( ) ) return false;
        DragInfo other = ( DragInfo ) obj;
        if ( info == null )
        {
            if ( other.info != null ) return false;
        }
        else if ( !info.equals( other.info ) ) return false;
        if ( top != other.top ) return false;
        return true;
    }
}
