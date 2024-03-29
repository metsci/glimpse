/*
 * Copyright (c) 2020, Metron, Inc.
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

import static com.metsci.glimpse.docking.ViewCloseOption.VIEW_AUTO_CLOSEABLE;
import static com.metsci.glimpse.util.AppConfigUtils.getAppConfigPath;
import static java.awt.ComponentOrientation.RIGHT_TO_LEFT;
import static java.awt.Frame.MAXIMIZED_HORIZ;
import static java.awt.Frame.MAXIMIZED_VERT;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.google.common.collect.ImmutableList;
import com.metsci.glimpse.docking.xml.DockerArrangementNode;
import com.metsci.glimpse.docking.xml.DockerArrangementSplit;
import com.metsci.glimpse.docking.xml.DockerArrangementTile;
import com.metsci.glimpse.docking.xml.FrameArrangement;
import com.metsci.glimpse.docking.xml.GroupArrangement;
import com.metsci.glimpse.util.var.Disposable;
import com.metsci.glimpse.util.var.DisposableGroup;

public class DockingUtils
{
    private static final Logger LOGGER = Logger.getLogger( DockingUtils.class.getName( ) );

    public static void requireSwingThread( )
    {
        if ( !SwingUtilities.isEventDispatchThread( ) )
        {
            throw new RuntimeException( "This operation is only allowed on the Swing/AWT event-dispatch thread" );
        }
    }

    public static interface Runnable1<T>
    {
        void run( T t );
    }

    public static <T> Runnable partial( final Runnable1<T> runnable1, final T t )
    {
        return new Runnable( )
        {
            @Override
            public void run( )
            {
                runnable1.run( t );
            }
        };
    }

    public static void swingRun( final Runnable runnable )
    {
        try
        {
            SwingUtilities.invokeAndWait( new Runnable( )
            {
                @Override
                public void run( )
                {
                    runnable.run( );
                }
            } );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    public static <T> void swingRun( final Runnable1<T> runnable1, final T t )
    {
        swingRun( partial( runnable1, t ) );
    }

    /**
     * @deprecated Use {@link DockingUtils#attachPopupMenu(JToggleButton, JPopupMenu)} instead.
     */
    @Deprecated
    public static JPopupMenu newButtonPopup( JToggleButton button )
    {
        JPopupMenu popup = new JPopupMenu( );
        attachPopupMenu( button, popup );
        return popup;
    }

    public static Disposable attachPopupMenu( JToggleButton button, JPopupMenu popup )
    {
        DisposableGroup disposables = new DisposableGroup( );

        disposables.add( addActionListener( button, ( ) -> {
            if ( button.isSelected( ) )
            {
                popup.show( button, 0, button.getHeight( ) );
            }
            else
            {
                popup.setVisible( false );
            }
        } ) );

        disposables.add( onPopupWillBecomeInvisible( popup, ( ) -> {
            button.setSelected( false );

            // If this popup-hide was triggered by a mouse-press, then don't allow
            // that mouse-press to begin a click of the button (which would toggle
            // the popup back to visible again)
            button.setEnabled( false );
            SwingUtilities.invokeLater( ( ) -> {
                button.setEnabled( true );
            } );
        } ) );

        return disposables;
    }

    private static Disposable onPopupWillBecomeInvisible( JPopupMenu popup, Runnable fn )
    {
        return addPopupListener( popup, new PopupMenuListener( )
        {
            @Override
            public void popupMenuWillBecomeInvisible( PopupMenuEvent ev )
            {
                fn.run( );
            }

            @Override
            public void popupMenuWillBecomeVisible( PopupMenuEvent ev )
            {
            }

            @Override
            public void popupMenuCanceled( PopupMenuEvent ev )
            {
            }
        } );
    }

    private static Disposable addPopupListener( JPopupMenu popup, PopupMenuListener listener )
    {
        popup.addPopupMenuListener( listener );

        return ( ) -> {
            popup.removePopupMenuListener( listener );
        };
    }

    private static Disposable addActionListener( AbstractButton button, Runnable fn )
    {
        return addActionListener( button, ( ev ) -> fn.run( ) );
    }

    private static Disposable addActionListener( AbstractButton button, ActionListener listener )
    {
        button.addActionListener( listener );

        return ( ) -> {
            button.removeActionListener( listener );
        };
    }

    @SuppressWarnings( "serial" )
    public static JToolBar newToolbar( boolean anchorRight )
    {
        JToolBar toolbar;

        if ( anchorRight )
        {
            toolbar = new JToolBar( )
            {
                @Override
                protected void addImpl( Component c, Object constraints, int index )
                {
                    int reverseIndex;
                    if ( index == -1 )
                    {
                        reverseIndex = 0;
                    }
                    else
                    {
                        int oldIndex = getComponentIndex( c );
                        if ( oldIndex >= 0 && index > oldIndex )
                        {
                            index--;
                        }

                        reverseIndex = getComponentCount( ) - index;
                    }

                    super.addImpl( c, constraints, reverseIndex );
                }
            };
            toolbar.setComponentOrientation( RIGHT_TO_LEFT );
        }
        else
        {
            toolbar = new JToolBar( );
        }

        toolbar.setBorder( null );
        toolbar.setBorderPainted( false );
        toolbar.setFloatable( false );
        toolbar.setRollover( true );
        toolbar.setOpaque( false );
        return toolbar;
    }

    public static <T> T getAncestorOfClass( Class<? extends T> clazz, Component c )
    {
        return clazz.cast( SwingUtilities.getAncestorOfClass( clazz, c ) );
    }

    public static int getFrameExtendedState( FrameArrangement frameArr )
    {
        return getFrameExtendedState( frameArr.isMaximizedHoriz, frameArr.isMaximizedVert );
    }

    public static int getFrameExtendedState( boolean isMaximizedHoriz, boolean isMaximizedVert )
    {
        int state = 0;
        if ( isMaximizedHoriz ) state |= MAXIMIZED_HORIZ;
        if ( isMaximizedVert ) state |= MAXIMIZED_VERT;
        return state;
    }

    public static Color getUiColor( Object key, Color fallback )
    {
        Color color = UIManager.getColor( key );
        return ( color == null ? fallback : color );
    }

    public static ImageIcon requireIcon( URL url )
    {
        try
        {
            return new ImageIcon( ImageIO.read( url ) );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e );
        }
    }

    public static void setArrangementAndSaveOnDispose( DockingGroup dockingGroup, String appName, URL fallbackInput )
    {
        setArrangementAndSaveOnDispose( dockingGroup, getDockingArrangementFile( appName ), fallbackInput );
    }

    public static void setArrangementAndSaveOnDispose( DockingGroup dockingGroup, File targetFile, URL fallbackInput )
    {
        GroupArrangement loadArr = loadDockingArrangement( targetFile, fallbackInput );
        dockingGroup.setArrangement( loadArr );

        dockingGroup.addListener( new DockingGroupAdapter( )
        {
            @Override
            public void disposingAllWindows( DockingGroup group )
            {
                GroupArrangement saveArr = dockingGroup.captureArrangement( );
                saveDockingArrangement( targetFile, saveArr );
            }
        } );
    }

    public static File getDockingArrangementFile( String appName )
    {
        return getAppConfigPath( appName, "arrangement.xml" );
    }

    public static GroupArrangement loadDockingArrangement( String appName, URL fallbackUrl )
    {
        File file = getDockingArrangementFile( appName );
        return loadDockingArrangement( file, fallbackUrl );
    }

    public static void saveDockingArrangement( String appName, GroupArrangement groupArr )
    {
        File file = getDockingArrangementFile( appName );
        saveDockingArrangement( file, groupArr );
    }

    public static GroupArrangement loadDockingArrangement( File file, URL fallbackUrl )
    {
        // First, try to load from file
        //
        // If file does not exist, skip this whole block without logging a warning. If file
        // does exist, run this block -- even if it is a dir or is unreadable, in which case
        // a warning will be logged.
        //
        if ( file != null && file.exists( ) )
        {
            LOGGER.log( INFO, "Reading docking arrangement from file: file = " + file );
            try (InputStream stream = new FileInputStream( file ))
            {
                return loadDockingArrangement( stream );
            }
            catch ( Exception e )
            {
                LOGGER.log( WARNING, "Failed to load docking arrangement from file: file = " + file, e );
            }
        }

        // Next, try to load from fallbackUrl
        if ( fallbackUrl != null )
        {
            LOGGER.log( INFO, "Reading fallback docking arrangement from resource: resource = " + fallbackUrl );
            try (InputStream stream = fallbackUrl.openStream( ))
            {
                return loadDockingArrangement( stream );
            }
            catch ( Exception e )
            {
                LOGGER.log( WARNING, "Failed to load fallback docking arrangement from resource: resource = " + fallbackUrl.toString( ), e );
            }
        }

        // Finally, give up and return null
        return null;
    }

    public static void saveDockingArrangement( File file, GroupArrangement groupArr )
    {
        LOGGER.log( INFO, "Writing docking arrangement to file: file = " + file );
        try (OutputStream stream = new BufferedOutputStream( new FileOutputStream( file ) ))
        {
            saveDockingArrangement( stream, groupArr );
        }
        catch ( Exception e )
        {
            LOGGER.log( WARNING, "Failed to write docking arrangement to file: file = " + file, e );
        }
    }

    public static final Collection<Class<?>> dockingXmlClasses = ImmutableList.of( GroupArrangement.class, FrameArrangement.class, DockerArrangementNode.class, DockerArrangementSplit.class, DockerArrangementTile.class );

    public static GroupArrangement loadDockingArrangement( InputStream is ) throws JAXBException
    {
        Class<?>[] classes = dockingXmlClasses.toArray( new Class[0] );
        Unmarshaller unmarshaller = JAXBContext.newInstance( classes ).createUnmarshaller( );
        Object unmarshalled = unmarshaller.unmarshal( is );
        return castUnmarshalled( unmarshalled, GroupArrangement.class );
    }

    public static void saveDockingArrangement( OutputStream os, GroupArrangement groupArr ) throws JAXBException
    {
        Class<?>[] classes = dockingXmlClasses.toArray( new Class[0] );
        Marshaller marshaller = JAXBContext.newInstance( classes ).createMarshaller( );
        marshaller.setProperty( JAXB_FORMATTED_OUTPUT, true );
        marshaller.marshal( groupArr, os );
    }

    private static <T> T castUnmarshalled( Object unmarshalled, Class<T> clazz )
    {
        if ( clazz.isInstance( unmarshalled ) )
        {
            return clazz.cast( unmarshalled );
        }
        else if ( unmarshalled instanceof JAXBElement )
        {
            return castUnmarshalled( ( ( JAXBElement<?> ) unmarshalled ).getValue( ), clazz );
        }
        else
        {
            throw new ClassCastException( "Unmarshalled object is neither a " + clazz.getName( ) + " nor a " + JAXBElement.class.getName( ) + ": classname = " + unmarshalled.getClass( ).getName( ) );
        }
    }

    public static <C extends Component> C findLargestComponent( Collection<? extends C> components )
    {
        int largestArea = -1;
        C largestComponent = null;
        for ( C c : components )
        {
            int area = c.getWidth( ) * c.getHeight( );
            if ( area > largestArea )
            {
                largestComponent = c;
                largestArea = area;
            }
        }
        return largestComponent;
    }

    public static Tile findLargestTile( MultiSplitPane docker )
    {
        int largestArea = -1;
        Tile largestTile = null;
        for ( Tile tile : findTiles( docker ) )
        {
            int area = tile.getWidth( ) * tile.getHeight( );
            if ( area > largestArea )
            {
                largestTile = tile;
                largestArea = area;
            }
        }
        return largestTile;
    }

    public static Set<Tile> findTiles( Collection<? extends DockingWindow> windows )
    {
        Set<Tile> tiles = new LinkedHashSet<>( );
        for ( DockingWindow window : windows )
        {
            tiles.addAll( findTiles( window ) );
        }
        return tiles;
    }

    public static Set<Tile> findTiles( DockingWindow window )
    {
        return findTiles( window.docker( ) );
    }

    public static Set<Tile> findTiles( MultiSplitPane docker )
    {
        Set<Tile> tiles = new LinkedHashSet<>( );
        for ( Component c : docker.leaves( ) )
        {
            if ( c instanceof Tile )
            {
                tiles.add( ( Tile ) c );
            }
        }
        return tiles;
    }

    public static Map<String, View> findViews( Collection<? extends DockingWindow> windows )
    {
        Map<String, View> views = new LinkedHashMap<>( );
        for ( DockingWindow window : windows )
        {
            views.putAll( findViews( window ) );
        }
        return views;
    }

    public static Map<String, View> findViews( DockingWindow window )
    {
        return findViews( window.docker( ) );
    }

    public static Map<String, View> findViews( MultiSplitPane docker )
    {
        Map<String, View> views = new LinkedHashMap<>( );
        for ( Tile tile : findTiles( docker ) )
        {
            for ( int i = 0; i < tile.numViews( ); i++ )
            {
                View view = tile.view( i );
                views.put( view.viewId, view );
            }
        }
        return views;
    }

    public static boolean allViewsAreAutoCloseable( Iterable<? extends View> views )
    {
        for ( View view : views )
        {
            if ( view.closeOption != VIEW_AUTO_CLOSEABLE )
            {
                return false;
            }
        }
        return true;
    }

    public static void appendViewsToTile( Tile tile, Collection<? extends View> views )
    {
        for ( View view : views )
        {
            int viewNum = tile.numViews( );
            tile.addView( view, viewNum );
        }
    }

}
