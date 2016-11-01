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

import static com.metsci.glimpse.docking.AppConfigUtils.loadAppConfig;
import static com.metsci.glimpse.docking.AppConfigUtils.saveAppConfig;
import static java.awt.ComponentOrientation.RIGHT_TO_LEFT;
import static java.awt.Frame.MAXIMIZED_HORIZ;
import static java.awt.Frame.MAXIMIZED_VERT;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableCollection;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import com.metsci.glimpse.docking.xml.DockerArrangementNode;
import com.metsci.glimpse.docking.xml.DockerArrangementSplit;
import com.metsci.glimpse.docking.xml.DockerArrangementTile;
import com.metsci.glimpse.docking.xml.FrameArrangement;
import com.metsci.glimpse.docking.xml.GroupArrangement;

public class DockingUtils
{

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

    public static JPopupMenu newButtonPopup( final JToggleButton button )
    {
        final JPopupMenu popup = new JPopupMenu( );

        button.addActionListener( new ActionListener( )
        {
            @Override
            public void actionPerformed( ActionEvent ev )
            {
                if ( button.isSelected( ) )
                {
                    popup.show( button, 0, button.getHeight( ) );
                }
                else
                {
                    popup.setVisible( false );
                }
            }
        } );

        popup.addPopupMenuListener( new PopupMenuListener( )
        {
            @Override
            public void popupMenuWillBecomeInvisible( PopupMenuEvent ev )
            {
                button.setSelected( false );

                // If this popup-hide was triggered by a mouse-press, then don't allow
                // that mouse-press to begin a click of the button (which would toggle
                // the popup back to visible again)
                //
                button.setEnabled( false );
                SwingUtilities.invokeLater( new Runnable( )
                {
                    @Override
                    public void run( )
                    {
                        button.setEnabled( true );
                    }
                } );
            }

            @Override
            public void popupMenuWillBecomeVisible( PopupMenuEvent ev )
            { }

            @Override
            public void popupMenuCanceled( PopupMenuEvent ev )
            { }
        } );

        return popup;
    }

    @SuppressWarnings("serial")
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

    public static int getFrameExtendedState( FrameArrangement frameArr )
    {
        int state = 0;
        if ( frameArr.isMaximizedHoriz ) state |= MAXIMIZED_HORIZ;
        if ( frameArr.isMaximizedVert ) state |= MAXIMIZED_VERT;
        return state;
    }

    public static Color getUiColor( Object key, Color fallback )
    {
        Color color = UIManager.getColor( key );
        return ( color == null ? fallback : color );
    }

    public static ImageIcon requireIcon( String resourcePath )
    {
        try
        {
            return new ImageIcon( ImageIO.read( DockingUtils.class.getClassLoader( ).getResource( resourcePath ) ) );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e );
        }
    }

    public static final Collection<Class<?>> dockingXmlClasses = unmodifiableCollection( asList( GroupArrangement.class, FrameArrangement.class, DockerArrangementNode.class, DockerArrangementSplit.class, DockerArrangementTile.class ) );

    public static void saveDockingArrangement( String appName, GroupArrangement groupArr )
    {
        saveAppConfig( appName, "arrangement.xml", groupArr, dockingXmlClasses );
    }

    public static GroupArrangement loadDockingArrangement( String appName, URL fallbackUrl )
    {
        return loadAppConfig( appName, "arrangement.xml", fallbackUrl, GroupArrangement.class, dockingXmlClasses );
    }

    public static <C extends Component> C findLargestComponent( Collection<C> components )
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

    public static Set<View> findViews( MultiSplitPane docker )
    {
        Set<View> views = new HashSet<>( );
        for ( Component c : docker.leaves( ) )
        {
            if ( c instanceof Tile )
            {
                Tile tile = ( Tile ) c;
                for ( int i = 0; i < tile.numViews( ); i++ )
                {
                    views.add( tile.view( i ) );
                }
            }
        }
        return views;
    }

    public static boolean allViewsAreCloseable( Iterable<View> views )
    {
        for ( View view : views )
        {
            if ( !view.closeable )
            {
                return false;
            }
        }
        return true;
    }

    public static void appendViewsToTile( Tile tile, Collection<View> views )
    {
        for ( View view : views )
        {
            int viewNum = tile.numViews( );
            tile.addView( view, viewNum );
        }
    }

}
