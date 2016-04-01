/*
 * Copyright (c) 2016, Metron, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Metron, Inc. nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL METRON, INC. BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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


}
