package com.metsci.glimpse.docking2;

import static java.util.Collections.unmodifiableSet;

import java.util.LinkedHashSet;
import java.util.Set;

public class DockingPaneGroup
{

    protected final Set<DockingPane> dockersMod;
    public final Set<DockingPane> dockers;


    public DockingPaneGroup( )
    {
        this.dockersMod = new LinkedHashSet<DockingPane>( );
        this.dockers = unmodifiableSet( dockersMod );
    }

    public void add( DockingPane docker )
    {
        dockersMod.add( docker );
    }

}
