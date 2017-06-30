package com.metsci.glimpse.docking;

import static com.metsci.glimpse.docking.DockingUtils.getAncestorOfClass;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.metsci.glimpse.util.var.Disposable;
import com.metsci.glimpse.util.var.DisposableGroup;

public class DockingFrameTitler extends DockingGroupAdapter
{

    protected final Function<DockingFrame,String> titleFn;
    protected final Map<View,Disposable> viewDisposables;


    public DockingFrameTitler( Function<DockingFrame,String> titleFn )
    {
        this.titleFn = titleFn;
        this.viewDisposables = new HashMap<>( );
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
        Runnable updateFrameTitle = ( ) ->
        {
            updateFrameTitle( getAncestorOfClass( DockingFrame.class, tile ) );
        };

        updateFrameTitle.run( );

        DisposableGroup disposables = new DisposableGroup( );
        disposables.add( view.component.addListener( false, updateFrameTitle ) );
        disposables.add( view.tooltip.addListener( false, updateFrameTitle ) );
        disposables.add( view.title.addListener( false, updateFrameTitle ) );
        disposables.add( view.icon.addListener( false, updateFrameTitle ) );

        viewDisposables.put( view, disposables );
    }

    @Override
    public void removedView( Tile tile, View view )
    {
        updateFrameTitle( getAncestorOfClass( DockingFrame.class, tile ) );
        viewDisposables.remove( view ).dispose( );
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
