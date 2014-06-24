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

import static java.util.Collections.unmodifiableList;
import static javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import com.metsci.glimpse.docking.DockingThemes.DockingTheme;

public class DockingGroup
{

    public static enum DockingFrameCloseOperation
    {
        DO_NOTHING, DISPOSE_CLOSED_FRAME, DISPOSE_ALL_FRAMES, EXIT_JVM
    }


    public final String title;
    public final DockingTheme theme;
    public final DockingFrameCloseOperation frameCloseOperation;

    protected final List<DockingFrame> framesMod;
    public final List<DockingFrame> frames;

    protected final JFrame landingIndicator;


    public DockingGroup( String title, DockingTheme theme, DockingFrameCloseOperation frameCloseOperation )
    {
        this.title = title;
        this.theme = theme;
        this.frameCloseOperation = frameCloseOperation;

        this.framesMod = new ArrayList<>( );
        this.frames = unmodifiableList( framesMod );

        this.landingIndicator = new JFrame( );
        landingIndicator.setAlwaysOnTop( true );
        landingIndicator.setFocusable( false );
        landingIndicator.setUndecorated( true );
        landingIndicator.getContentPane( ).setBackground( theme.landingIndicatorColor );
    }

    public DockingFrame addNewFrame( )
    {
        DockingPane docker = new DockingPane( theme.dividerSize );

        final DockingFrame frame = new DockingFrame( title, docker );
        frame.setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );
        frame.addWindowListener( new WindowAdapter( )
        {
            public void windowActivated( WindowEvent ev )
            {
                bringFrameToFront( frame );
            }

            // Frame's close button was clicked
            public void windowClosing( WindowEvent ev )
            {
                switch ( frameCloseOperation )
                {
                    case DO_NOTHING:
                    {
                        // Do nothing
                    }
                    break;

                    case DISPOSE_CLOSED_FRAME:
                    {
                        frame.dispose( );
                    }
                    break;

                    case DISPOSE_ALL_FRAMES:
                    {
                        for ( DockingFrame f : frames ) f.dispose( );
                    }
                    break;

                    case EXIT_JVM:
                    {
                        // Even if we try to dispose frames here, the JVM
                        // exits before any disposing actually happens
                        System.exit( 0 );
                    }
                    break;
                }
            }

            // Frame has been disposed, including programmatically
            public void windowClosed( WindowEvent ev )
            {
                removeFrame( frame );
                if ( frames.isEmpty( ) )
                {
                    // Dispose the landingIndicator frame, so that the JVM can shut
                    // down if appropriate. If the landingIndicator is needed again
                    // (e.g. after a new frame is added to the group), it will be
                    // automatically resurrected, and will work fine.
                    landingIndicator.dispose( );
                }
            }
        } );

        framesMod.add( 0, frame );
        return frame;
    }

    public void removeFrame( DockingFrame frame )
    {
        framesMod.remove( frame );
    }

    public void bringFrameToFront( DockingFrame frame )
    {
        boolean found = framesMod.remove( frame );
        if ( !found ) throw new RuntimeException( "Frame does not belong to this docking-group" );

        framesMod.add( 0, frame );
    }

    public void setLandingIndicator( Rectangle bounds )
    {
        if ( bounds == null )
        {
            landingIndicator.setVisible( false );
        }
        else
        {
            landingIndicator.setBounds( bounds );

            Area shape = new Area( new Rectangle( 0, 0, bounds.width, bounds.height ) );
            int thickness = theme.landingIndicatorThickness;
            shape.subtract( new Area( new Rectangle( thickness, thickness, bounds.width - 2*thickness, bounds.height - 2*thickness ) ) );
            landingIndicator.setShape( shape );

            landingIndicator.setVisible( true );
        }
    }


    // Snapshots
    //

    public List<FrameSnapshot> snapshot( )
    {
        List<FrameSnapshot> frameSnapshots = new ArrayList<>( );
        for ( DockingFrame frame : frames )
        {
            SnapshotNode dockerSnapshot = toDockerSnapshot( frame.docker.getSnapshot( ) );
            frameSnapshots.add( new FrameSnapshot( frame.getBounds( ), dockerSnapshot ) );
        }
        return frameSnapshots;
    }

    public static SnapshotNode toDockerSnapshot( DockingPane.Node node )
    {
        if ( node instanceof DockingPane.Leaf )
        {
            DockingPane.Leaf leaf = ( DockingPane.Leaf ) node;

            List<String> viewIds = new ArrayList<>( );
            String selectedViewId = null;
            Component c = leaf.component;
            if ( c instanceof Tile )
            {
                Tile tile = ( Tile ) c;
                for ( int i = 0; i < tile.numViews( ); i++ )
                {
                    String viewId = tile.view( i ).viewKey.viewId;
                    viewIds.add( viewId );
                }
                selectedViewId = tile.selectedView( ).viewKey.viewId;
            }

            return new TileSnapshot( viewIds, selectedViewId, leaf.isMaximized );
        }
        else if ( node instanceof DockingPane.Split )
        {
            DockingPane.Split split = ( DockingPane.Split ) node;
            return new SplitSnapshot( split.arrangeVertically, split.splitFrac, toDockerSnapshot( split.childA ), toDockerSnapshot( split.childB ) );
        }
        else
        {
            return null;
        }
    }

    @XmlType( name="Frame" )
    public static class FrameSnapshot
    {
        public final int x;
        public final int y;
        public final int width;
        public final int height;
        public final SnapshotNode dockerSnapshot;

        public FrameSnapshot( Rectangle frameBounds, SnapshotNode dockerSnapshot )
        {
            this.x = frameBounds.x;
            this.y = frameBounds.y;
            this.width = frameBounds.width;
            this.height = frameBounds.height;
            this.dockerSnapshot = dockerSnapshot;
        }
    }

    @XmlType( name="Node" )
    public static abstract class SnapshotNode
    { }

    @XmlType( name="Split" )
    public static class SplitSnapshot extends SnapshotNode
    {
        public final boolean arrangeVertically;
        public final double splitFrac;
        public final SnapshotNode childA;
        public final SnapshotNode childB;

        public SplitSnapshot( boolean arrangeVertically, double splitFrac, SnapshotNode childA, SnapshotNode childB )
        {
            this.arrangeVertically = arrangeVertically;
            this.splitFrac = splitFrac;
            this.childA = childA;
            this.childB = childB;
        }
    }

    @XmlType( name="Tile" )
    public static class TileSnapshot extends SnapshotNode
    {
        @XmlElementWrapper( name="views" )
        @XmlElement( name="view" )
        public final List<String> viewIds;

        @XmlElement( name="selectedView" )
        public final String selectedViewId;

        public final boolean isMaximized;

        public TileSnapshot( List<String> viewIds, String selectedViewId, boolean isMaximized )
        {
            this.viewIds = unmodifiableList( new ArrayList<>( viewIds ) );
            this.selectedViewId = selectedViewId;
            this.isMaximized = isMaximized;
        }
    }

}
