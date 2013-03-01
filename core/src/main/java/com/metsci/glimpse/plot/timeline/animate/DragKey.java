package com.metsci.glimpse.plot.timeline.animate;

import com.metsci.glimpse.plot.stacked.PlotInfo;

public class DragKey
{
    public Object id;
    public boolean top;

    public DragKey( DragInfo drag )
    {
        this.id = drag.info.getId( );
        this.top = drag.top;
    }

    public DragKey( PlotInfo info, boolean top )
    {
        this.id = info.getId( );
        this.top = top;
    }

    @Override
    public int hashCode( )
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( id == null ) ? 0 : id.hashCode( ) );
        result = prime * result + ( top ? 1231 : 1237 );
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass( ) != obj.getClass( ) ) return false;
        DragKey other = ( DragKey ) obj;
        if ( id == null )
        {
            if ( other.id != null ) return false;
        }
        else if ( !id.equals( other.id ) ) return false;
        if ( top != other.top ) return false;
        return true;
    }
}
