package com.metsci.glimpse.docking;

import static com.metsci.glimpse.docking.MiscUtils.getAncestorOfClass;

import java.awt.Component;
import java.util.function.Function;

public class DockingFrameTitler extends DockingGroupAdapter
{

    protected final Function<DockingFrame,String> titleFn;


    public DockingFrameTitler( Function<DockingFrame,String> titleFn )
    {
        this.titleFn = titleFn;
    }

    public void updateFrameTitle( DockingFrame frame )
    {
        if ( frame != null )
        {
            String title = this.titleFn.apply( frame );
            frame.setTitle( title );
        }
    }

    @Override
    public void addedView( Tile tile, View view )
    {
        updateFrameTitle( getAncestorOfClass( DockingFrame.class, tile ) );
    }

    @Override
    public void removedView( Tile tile, View view )
    {
        updateFrameTitle( getAncestorOfClass( DockingFrame.class, tile ) );
    }

    @Override
    public void selectedView( Tile tile, View view )
    {
        updateFrameTitle( getAncestorOfClass( DockingFrame.class, tile ) );
    }

    @Override
    public void addedLeaf( MultiSplitPane docker, Component leaf )
    {
        updateFrameTitle( getAncestorOfClass( DockingFrame.class, docker ) );
    }

    @Override
    public void removedLeaf( MultiSplitPane docker, Component leaf )
    {
        updateFrameTitle( getAncestorOfClass( DockingFrame.class, docker ) );
    }

    @Override
    public void movedDivider( MultiSplitPane docker, SplitPane splitPane )
    {
        updateFrameTitle( getAncestorOfClass( DockingFrame.class, docker ) );
    }

    @Override
    public void maximizedLeaf( MultiSplitPane docker, Component leaf )
    {
        updateFrameTitle( getAncestorOfClass( DockingFrame.class, docker ) );
    }

    @Override
    public void unmaximizedLeaf( MultiSplitPane docker, Component leaf )
    {
        updateFrameTitle( getAncestorOfClass( DockingFrame.class, docker ) );
    }

    @Override
    public void restoredTree( MultiSplitPane docker )
    {
        updateFrameTitle( getAncestorOfClass( DockingFrame.class, docker ) );
    }

    @Override
    public void addedFrame( DockingGroup group, DockingFrame frame )
    {
        updateFrameTitle( frame );
    }

}
