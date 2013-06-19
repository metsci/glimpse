package com.metsci.glimpse.docking;

public class BasicDockingPane extends DockingPane<BasicTile>
{

    public BasicDockingPane( )
    {
        super( BasicTile.class );
    }

    @Override
    protected BasicTile newTile( )
    {
        return new BasicTile( );
    }

}
