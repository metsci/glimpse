package com.metsci.glimpse.docking;

import static com.metsci.glimpse.docking.DockingUtils.findLargestTile;
import static com.metsci.glimpse.docking.MiscUtils.getAncestorOfClass;

import java.awt.Component;

import com.metsci.glimpse.docking.DockingGroup.DockingGroupAdapter;

public class DockingFrameTitlers
{

    public static DockingFrameTitler createDefaultFrameTitler( final String titleRoot )
    {
        return new DockingFrameTitler( )
        {
            public String getTitle( DockingFrame frame )
            {
                Tile tile = findLargestTile( frame.docker );
                if ( tile != null )
                {
                    View view = tile.selectedView( );
                    if ( view != null )
                    {
                        return view.title + " - " + titleRoot;
                    }
                }
                return titleRoot;
            }
        };
    }


    public static abstract class DockingFrameTitler extends DockingGroupAdapter
    {
        public abstract String getTitle( DockingFrame frame );

        public void updateFrameTitle( DockingFrame frame )
        {
            if ( frame != null )
            {
                frame.setTitle( getTitle( frame ) );
            }
        }

        public void addedView( Tile tile, View view )
        {
            updateFrameTitle( getAncestorOfClass( DockingFrame.class, tile ) );
        }

        public void removedView( Tile tile, View view )
        {
            updateFrameTitle( getAncestorOfClass( DockingFrame.class, tile ) );
        }

        public void selectedView( Tile tile, View view )
        {
            updateFrameTitle( getAncestorOfClass( DockingFrame.class, tile ) );
        }

        public void addedLeaf( MultiSplitPane docker, Component leaf )
        {
            updateFrameTitle( getAncestorOfClass( DockingFrame.class, docker ) );
        }

        public void removedLeaf( MultiSplitPane docker, Component leaf )
        {
            updateFrameTitle( getAncestorOfClass( DockingFrame.class, docker ) );
        }

        public void movedDivider( MultiSplitPane docker, SplitPane splitPane )
        {
            updateFrameTitle( getAncestorOfClass( DockingFrame.class, docker ) );
        }

        public void maximizedLeaf( MultiSplitPane docker, Component leaf )
        {
            updateFrameTitle( getAncestorOfClass( DockingFrame.class, docker ) );
        }

        public void unmaximizedLeaf( MultiSplitPane docker, Component leaf )
        {
            updateFrameTitle( getAncestorOfClass( DockingFrame.class, docker ) );
        }

        public void restoredTree( MultiSplitPane docker )
        {
            updateFrameTitle( getAncestorOfClass( DockingFrame.class, docker ) );
        }

        public void addedFrame( DockingGroup group, DockingFrame frame )
        {
            updateFrameTitle( frame );
        }
    }


}
