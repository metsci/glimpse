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

import static com.metsci.glimpse.docking.Side.LEFT;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE;
import static javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Area;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import com.metsci.glimpse.docking.DockingThemes.DockingTheme;
import com.metsci.glimpse.docking.TileFactories.TileFactory;

public class DockingGroup
{

    public static enum DockingFrameCloseOperation
    {
        DO_NOTHING, DISPOSE_CLOSED_FRAME, DISPOSE_ALL_FRAMES, EXIT_JVM
    }


    public static interface DockingGroupListener
    {
        void disposingAllFrames( );
        void disposingFrame( DockingFrame frame );
    }


    public static class DockingGroupAdapter implements DockingGroupListener
    {
        public void disposingAllFrames( ) { }
        public void disposingFrame( DockingFrame frame ) { }
    }


    public final String title;
    public final DockingTheme theme;
    public final DockingFrameCloseOperation frameCloseOperation;

    protected final List<DockingFrame> framesMod;
    public final List<DockingFrame> frames;

    protected final JFrame landingIndicator;

    protected final Set<DockingGroupListener> listeners;


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

        this.listeners = new LinkedHashSet<>( );
    }

    public void addListener( DockingGroupListener listener )
    {
        listeners.add( listener );
    }

    public void removeListener( DockingGroupListener listener )
    {
        listeners.remove( listener );
    }

    public DockingFrame addNewFrame( )
    {
        MultiSplitPane docker = new MultiSplitPane( theme.dividerSize );

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
                        for ( DockingGroupListener listener : listeners )
                        {
                            listener.disposingFrame( frame );
                        }
                        frame.dispose( );
                    }
                    break;

                    case DISPOSE_ALL_FRAMES:
                    {
                        for ( DockingGroupListener listener : listeners )
                        {
                            listener.disposingAllFrames( );
                        }
                        for ( DockingFrame frame : frames )
                        {
                            for ( DockingGroupListener listener : listeners )
                            {
                                listener.disposingFrame( frame );
                            }
                            frame.dispose( );
                        }
                    }
                    break;

                    case EXIT_JVM:
                    {
                        for ( DockingGroupListener listener : listeners )
                        {
                            listener.disposingAllFrames( );
                        }
                        for ( DockingFrame frame : frames )
                        {
                            for ( DockingGroupListener listener : listeners )
                            {
                                listener.disposingFrame( frame );
                            }
                            // Even if we try to dispose frames here, the JVM
                            // exits before any disposing actually happens
                            //frame.dispose( );
                        }
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

    public void restoreArrangement( GroupArrangement groupArr, TileFactory tileFactory, View... views )
    {
        restoreArrangement( groupArr, tileFactory, asList( views ) );
    }

    public void restoreArrangement( GroupArrangement groupArr, TileFactory tileFactory, Collection<View> views )
    {
        if ( !frames.isEmpty( ) ) throw new RuntimeException( "At least one frame already exists" );

        Map<String,View> remainingViews = new LinkedHashMap<>( );
        for ( View v : views ) remainingViews.put( v.viewId, v );

        if ( groupArr != null )
        {
            for ( FrameArrangement frameArr : groupArr.frameArrs )
            {
                MultiSplitPane.Node dockerRoot = toDockingPaneNode( frameArr.dockerArr, remainingViews, tileFactory );
                if ( dockerRoot != null )
                {
                    DockingFrame frame = addNewFrame( );
                    frame.docker.restore( dockerRoot );
                    frame.setLocation( frameArr.x, frameArr.y );
                    frame.setPreferredSize( new Dimension( frameArr.width, frameArr.height ) );
                    frame.pack( );
                    frame.setVisible( true );
                }
            }
        }

        if ( !remainingViews.isEmpty( ) )
        {
            DockingFrame frame;
            if ( frames.isEmpty( ) )
            {
                Tile tile = tileFactory.newTile( );
                appendViewsToTile( tile, remainingViews.values( ) );
                frame = addNewFrame( );
                frame.docker.addInitialLeaf( tile );
                frame.setPreferredSize( new Dimension( 1024, 768 ) );
                frame.pack( );
                frame.setLocationByPlatform( true );
                frame.setVisible( true );
            }
            else
            {
                frame = findLargestFrame( frames );
                Tile tile = findLargestTile( frame.docker );
                if ( tile == null )
                {
                    tile = tileFactory.newTile( );
                    appendViewsToTile( tile, remainingViews.values( ) );
                    frame.docker.addEdgeLeaf( tile, LEFT );
                }
                else
                {
                    appendViewsToTile( tile, remainingViews.values( ) );
                }
            }
        }
    }

    protected static DockingFrame findLargestFrame( Collection<DockingFrame> frames )
    {
        int largestArea = -1;
        DockingFrame largestFrame = null;
        for ( DockingFrame frame : frames )
        {
            int area = frame.getWidth( ) * frame.getHeight( );
            if ( area > largestArea )
            {
                largestFrame = frame;
                largestArea = area;
            }
        }
        return largestFrame;
    }

    protected static Tile findLargestTile( MultiSplitPane docker )
    {
        int largestArea = -1;
        Tile largestTile = null;
        for ( Component c : docker.leaves( ) )
        {
            int area = c.getWidth( ) * c.getHeight( );
            if ( area > largestArea && c instanceof Tile )
            {
                largestTile = ( Tile ) c;
                largestArea = area;
            }
        }
        return largestTile;
    }

    protected static void appendViewsToTile( Tile tile, Collection<View> views )
    {
        for ( View view : views )
        {
            int viewNum = tile.numViews( );
            tile.addView( view, viewNum );
        }
    }

    public GroupArrangement captureArrangement( )
    {
        List<FrameArrangement> frameArrs = new ArrayList<>( );
        for ( DockingFrame frame : frames )
        {
            DockerArrangementNode dockerArr = toDockerArrNode( frame.docker.snapshot( ) );
            frameArrs.add( new FrameArrangement( dockerArr, frame.getBounds( ) ) );
        }
        return new GroupArrangement( frameArrs );
    }

    protected static MultiSplitPane.Node toDockingPaneNode( DockerArrangementNode arrNode, Map<String,View> remainingViews_INOUT, TileFactory tileFactory )
    {
        if ( arrNode instanceof DockerArrangementTile )
        {
            DockerArrangementTile arrTile = ( DockerArrangementTile ) arrNode;

            Map<String,View> views = new LinkedHashMap<>( );
            for ( String viewId : arrTile.viewIds )
            {
                View view = remainingViews_INOUT.remove( viewId );
                if ( view != null ) views.put( viewId, view );
            }

            if ( views.isEmpty( ) )
            {
                return null;
            }
            else
            {
                Tile tile = tileFactory.newTile( );

                for ( View view : views.values( ) )
                {
                    int viewNum = tile.numViews( );
                    tile.addView( view, viewNum );
                }

                View selectedView = views.get( arrTile.selectedViewId );
                if ( selectedView != null )
                {
                    tile.selectView( selectedView );
                }

                return new MultiSplitPane.Leaf( tile, arrTile.isMaximized );
            }
        }
        else if ( arrNode instanceof DockerArrangementSplit )
        {
            DockerArrangementSplit arrSplit = ( DockerArrangementSplit ) arrNode;
            MultiSplitPane.Node childA = toDockingPaneNode( arrSplit.childA, remainingViews_INOUT, tileFactory );
            MultiSplitPane.Node childB = toDockingPaneNode( arrSplit.childB, remainingViews_INOUT, tileFactory );

            if ( childA != null && childB != null )
            {
                return new MultiSplitPane.Split( arrSplit.arrangeVertically, arrSplit.splitFrac, childA, childB );
            }
            else if ( childA != null )
            {
                return childA;
            }
            else if ( childB != null )
            {
                return childB;
            }
            else
            {
                return null;
            }
        }
        else if ( arrNode == null )
        {
            return null;
        }
        else
        {
            throw new RuntimeException( "Unrecognized subclass of " + DockerArrangementNode.class.getName( ) + ": " + arrNode.getClass( ).getName( ) );
        }
    }

    protected static DockerArrangementNode toDockerArrNode( MultiSplitPane.Node node )
    {
        if ( node instanceof MultiSplitPane.Leaf )
        {
            MultiSplitPane.Leaf leaf = ( MultiSplitPane.Leaf ) node;

            List<String> viewIds = new ArrayList<>( );
            String selectedViewId = null;
            Component c = leaf.component;
            if ( c instanceof Tile )
            {
                Tile tile = ( Tile ) c;
                for ( int i = 0; i < tile.numViews( ); i++ )
                {
                    String viewId = tile.view( i ).viewId;
                    viewIds.add( viewId );
                }
                selectedViewId = tile.selectedView( ).viewId;
            }
            else
            {
                // XXX: Handle arbitrary components
            }

            return new DockerArrangementTile( viewIds, selectedViewId, leaf.isMaximized );
        }
        else if ( node instanceof MultiSplitPane.Split )
        {
            MultiSplitPane.Split split = ( MultiSplitPane.Split ) node;
            DockerArrangementNode childA = toDockerArrNode( split.childA );
            DockerArrangementNode childB = toDockerArrNode( split.childB );
            return new DockerArrangementSplit( split.arrangeVertically, split.splitFrac, childA, childB );
        }
        else if ( node == null )
        {
            return null;
        }
        else
        {
            throw new RuntimeException( "Unrecognized subclass of " + MultiSplitPane.Node.class.getName( ) + ": " + node.getClass( ).getName( ) );
        }
    }

    @XmlType( name="Group" )
    public static class GroupArrangement
    {
        @XmlElementWrapper( name="frames" )
        @XmlElement( name="frame" )
        public List<FrameArrangement> frameArrs;

        public GroupArrangement( List<FrameArrangement> frameArrs )
        {
            this.frameArrs = frameArrs;
        }
    }

    @XmlType( name="Frame" )
    public static class FrameArrangement
    {
        public int x;
        public int y;
        public int width;
        public int height;

        @XmlElement( name="docker" )
        public DockerArrangementNode dockerArr;

        public FrameArrangement( DockerArrangementNode dockerArr, Rectangle frameBounds )
        {
            this( dockerArr, frameBounds.x, frameBounds.y, frameBounds.width, frameBounds.height );
        }

        public FrameArrangement( DockerArrangementNode dockerArr, int x, int y, int width, int height )
        {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.dockerArr = dockerArr;
        }
    }

    @XmlType( name="Node" )
    public static abstract class DockerArrangementNode
    { }

    @XmlType( name="Split" )
    public static class DockerArrangementSplit extends DockerArrangementNode
    {
        public boolean arrangeVertically;
        public double splitFrac;
        public DockerArrangementNode childA;
        public DockerArrangementNode childB;

        public DockerArrangementSplit( boolean arrangeVertically, double splitFrac, DockerArrangementNode childA, DockerArrangementNode childB )
        {
            this.arrangeVertically = arrangeVertically;
            this.splitFrac = splitFrac;
            this.childA = childA;
            this.childB = childB;
        }
    }

    @XmlType( name="Tile" )
    public static class DockerArrangementTile extends DockerArrangementNode
    {
        @XmlElementWrapper( name="views" )
        @XmlElement( name="view" )
        public List<String> viewIds;

        @XmlElement( name="selectedView" )
        public String selectedViewId;

        public boolean isMaximized;

        public DockerArrangementTile( List<String> viewIds, String selectedViewId, boolean isMaximized )
        {
            this.viewIds = viewIds;
            this.selectedViewId = selectedViewId;
            this.isMaximized = isMaximized;
        }
    }

    public static Marshaller newJaxbMarshaller( ) throws IOException, JAXBException
    {
        Marshaller marshaller = JAXBContext.newInstance( GroupArrangement.class, FrameArrangement.class, DockerArrangementNode.class, DockerArrangementSplit.class, DockerArrangementTile.class ).createMarshaller( );
        marshaller.setProperty( JAXB_FORMATTED_OUTPUT, true );
        return marshaller;
    }

    public static JAXBElement<GroupArrangement> newJaxbRoot( GroupArrangement groupArr )
    {
        return new JAXBElement<GroupArrangement>( new QName( "group" ), GroupArrangement.class, groupArr );
    }

    public static void writeArrangementXml( GroupArrangement groupArr, File file ) throws JAXBException, IOException
    {
        newJaxbMarshaller( ).marshal( newJaxbRoot( groupArr ), file );
    }

    public static void writeArrangementXml( GroupArrangement groupArr, Writer writer ) throws JAXBException, IOException
    {
        newJaxbMarshaller( ).marshal( newJaxbRoot( groupArr ), writer );
    }

    public static void writeArrangementXml( GroupArrangement groupArr, OutputStream stream ) throws JAXBException, IOException
    {
        newJaxbMarshaller( ).marshal( newJaxbRoot( groupArr ), stream );
    }

    public static Unmarshaller newJaxbUnmarshaller( ) throws JAXBException, IOException
    {
        Unmarshaller unmarshaller = JAXBContext.newInstance( GroupArrangement.class, FrameArrangement.class, DockerArrangementNode.class, DockerArrangementSplit.class, DockerArrangementTile.class ).createUnmarshaller( );
        return unmarshaller;
    }

    protected static GroupArrangement castToArrangement( Object object )
    {
        if ( object instanceof GroupArrangement )
        {
            return ( GroupArrangement ) object;
        }
        else if ( object instanceof JAXBElement )
        {
            return castToArrangement( ( ( JAXBElement<?> ) object ).getValue( ) );
        }
        else
        {
            throw new ClassCastException( "Object is neither a " + GroupArrangement.class.getName( ) + " nor a " + JAXBElement.class.getName( ) + ": classname = " + object.getClass( ).getName( ) );
        }
    }

    public static GroupArrangement readArrangementXml( URL url ) throws JAXBException, IOException
    {
        return castToArrangement( newJaxbUnmarshaller( ).unmarshal( url ) );
    }

    public static GroupArrangement readArrangementXml( File file ) throws JAXBException, IOException
    {
        return castToArrangement( newJaxbUnmarshaller( ).unmarshal( file ) );
    }

    public static GroupArrangement readArrangementXml( Reader reader ) throws JAXBException, IOException
    {
        return castToArrangement( newJaxbUnmarshaller( ).unmarshal( reader ) );
    }

    public static GroupArrangement readArrangementXml( InputStream stream ) throws JAXBException, IOException
    {
        return castToArrangement( newJaxbUnmarshaller( ).unmarshal( stream ) );
    }
}
