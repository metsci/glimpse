package com.metsci.glimpse.docking;

import java.awt.Component;

public interface DockingGroupListener
{

    void addedView( Tile tile, View view );

    void removedView( Tile tile, View view );

    void selectedView( Tile tile, View view );

    void addedLeaf( MultiSplitPane docker, Component leaf );

    void removedLeaf( MultiSplitPane docker, Component leaf );

    void movedDivider( MultiSplitPane docker, SplitPane splitPane );

    void maximizedLeaf( MultiSplitPane docker, Component leaf );

    void unmaximizedLeaf( MultiSplitPane docker, Component leaf );

    void restoredTree( MultiSplitPane docker );

    void addedFrame( DockingGroup group, DockingFrame frame );

    void disposingAllFrames( DockingGroup group );

    void disposingFrame( DockingGroup group, DockingFrame frame );

    void disposedFrame( DockingGroup group, DockingFrame frame );

    void closingView( DockingGroup group, View view );

    void closedView( DockingGroup group, View view );

}
