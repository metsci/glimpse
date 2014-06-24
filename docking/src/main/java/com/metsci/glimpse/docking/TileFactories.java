/*
 * Copyright (c) 2012, Metron, Inc.
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

import static com.metsci.glimpse.docking.MiscUtils.getAncestorOfClass;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import com.metsci.glimpse.docking.DockingThemes.DockingTheme;

public class TileFactories
{

    public static interface TileFactory
    {
        Tile newTile( );
    }


    public static class TileFactoryStandard implements TileFactory
    {
        public final DockingGroup dockingGroup;

        public TileFactoryStandard( DockingGroup dockingGroup )
        {
            this.dockingGroup = dockingGroup;
        }

        @Override
        public Tile newTile( )
        {
            final DockingTheme theme = dockingGroup.theme;
            final JButton maximizeButton = new JButton( theme.maximizeIcon );
            final Tile tile = new Tile( theme, maximizeButton );
            tile.addDockingMouseAdapter( new DockingMouseAdapter( tile, dockingGroup, this ) );

            maximizeButton.addActionListener( new ActionListener( )
            {
                public void actionPerformed( ActionEvent ev )
                {
                    DockingPane docker = getAncestorOfClass( DockingPane.class, tile );
                    if ( docker.getMaximizedTile( ) == tile )
                    {
                        docker.unmaximizeTile( );
                        maximizeButton.setIcon( theme.maximizeIcon );
                    }
                    else
                    {
                        docker.maximizeTile( tile );
                        maximizeButton.setIcon( theme.restoreIcon );
                    }
                }
            } );

            return tile;
        }
    }

}
