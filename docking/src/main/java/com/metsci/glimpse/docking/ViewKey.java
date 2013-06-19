package com.metsci.glimpse.docking;

import static com.metsci.glimpse.docking.DockingUtils.*;

public class ViewKey
{

    public final String viewId;


    public ViewKey( String viewId )
    {
        this.viewId = viewId;
    }

    @Override
    public int hashCode( )
    {
        int prime = 5923;
        int result = 1;
        result = prime * result + ( viewId == null ? 0 : viewId.hashCode( ) );
        return result;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( o == this ) return true;
        if ( o == null ) return false;
        if ( o.getClass( ) != getClass( ) ) return false;

        ViewKey other = ( ViewKey ) o;
        return areEqual( other.viewId, viewId );
    }

}
