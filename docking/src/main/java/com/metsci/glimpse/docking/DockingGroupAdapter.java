package com.metsci.glimpse.docking;

import java.awt.Component;

public class DockingGroupAdapter implements DockingGroupListener
{

    @Override
    public void addedView( Tile tile, View view )
    {
    }

    @Override
    public void removedView( Tile tile, View view )
    {
    }

    @Override
    public void selectedView( Tile tile, View view )
    {
    }

    @Override
    public void addedLeaf( MultiSplitPane docker, Component leaf )
    {
    }

    @Override
    public void removedLeaf( MultiSplitPane docker, Component leaf )
    {
    }

    @Override
    public void movedDivider( MultiSplitPane docker, SplitPane splitPane )
    {
    }

    @Override
    public void maximizedLeaf( MultiSplitPane docker, Component leaf )
    {
    }

    @Override
    public void unmaximizedLeaf( MultiSplitPane docker, Component leaf )
    {
    }

    @Override
    public void restoredTree( MultiSplitPane docker )
    {
    }

    @Override
    public void addedFrame( DockingGroup group, DockingFrame frame )
    {
    }

    @Override
    public void disposingAllFrames( DockingGroup group )
    {
    }

    @Override
    public void disposingFrame( DockingGroup group, DockingFrame frame )
    {
    }

    @Override
    public void disposedFrame( DockingGroup group, DockingFrame frame )
    {
    }

    @Override
    public void closingView( DockingGroup group, View view )
    {
    }

    @Override
    public void closedView( DockingGroup group, View view )
    {
    }

}
