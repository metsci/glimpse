package com.metsci.glimpse.docking;

public class ExperimentalDockingPane extends DockingPane<ExperimentalTile>
{

    public ExperimentalDockingPane( )
    {
        super( ExperimentalTile.class );
    }

    @Override
    protected ExperimentalTile newTile( )
    {
        return new ExperimentalTile( );
    }

}
