package com.metsci.glimpse.docking;

public interface TileListener
{

    void addedView( View view );

    void removedView( View view );

    void selectedView( View view );

}
