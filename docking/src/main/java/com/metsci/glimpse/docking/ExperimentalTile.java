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

import static com.metsci.glimpse.docking.DockingUtils.*;
import static java.lang.Math.*;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.util.List;
import java.util.Map;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ExperimentalTile extends JPanel implements Tile
{

    protected final JTabbedPane tabbedPane;

    protected final JToolBar overflowBar;
    protected final JToggleButton overflowPopupButton;
    protected final JPopupMenu overflowPopup;

    protected final JToolBar cornerBar;

    protected final JPanel viewBarHolder;

    protected final CardLayout cardLayout;
    protected final JPanel cardPanel;

    protected final Map<ViewKey,ViewEntry> viewMap;
    protected final List<View> views;


    public ExperimentalTile( )
    {
        this.tabbedPane = new JTabbedPane( );

        this.overflowBar = newToolbar( true );
        this.overflowPopupButton = new JToggleButton( "\u00BB" );
        this.overflowPopup = newButtonPopup( overflowPopupButton );
        overflowBar.add( overflowPopupButton );

        this.cornerBar = newToolbar( true );

        this.viewBarHolder = new JPanel( new GridLayout( 1, 1 ) );

        this.cardLayout = new CardLayout( );
        this.cardPanel = new JPanel( cardLayout );

        add( cardPanel );
        add( cornerBar );
        add( overflowBar );
        add( viewBarHolder );
        add( tabbedPane );



        this.viewMap = newHashMap( );
        this.views = newArrayList( );


        tabbedPane.getModel( ).addChangeListener( new ChangeListener( )
        {
            public void stateChanged( ChangeEvent ev )
            {
                ViewPlaceholder viewPlaceholder = ( ViewPlaceholder ) tabbedPane.getSelectedComponent( );
                String viewId = ( viewPlaceholder == null ? null : viewPlaceholder.viewKey.viewId );
                cardLayout.show( cardPanel, viewId );
            }
        } );


        setLayout( new LayoutManager( )
        {
            Dimension minSize = new Dimension( 130, 40 );
            Dimension prefSize = new Dimension( 200, 100 );

            public void layoutContainer( Container parent )
            {
                int wTile = getWidth( );
                int hTile = getHeight( );
                int wCornerBar = cornerBar.getMinimumSize( ).width;
                int wOverflowBar = overflowBar.getMinimumSize( ).width;

                // Don't shrink tabbedPane until we know its minimum width (see below)
                int wTabbedPane0 = max( tabbedPane.getWidth( ), wTile );

                tabbedPane.setBounds( 0, 0, wTabbedPane0, hTile );


                // Remove all existing tabs, and then add a tab for every view
                // (but leave selected tab alone, so as not to generate spurious
                // selection events). Below, we remove tabs that don't fit.
                //
                for ( int i = tabbedPane.getTabCount( ) - 1; i >= 0; i-- )
                {
                    if ( i != tabbedPane.getSelectedIndex( ) )
                    {
                        tabbedPane.removeTabAt( i );
                    }
                }
                for ( int i = 0; i < views.size( ); i++ )
                {
                    View view = views.get( i );
                    ViewEntry viewEntry = viewMap.get( view.viewKey );

                    if ( viewEntry.placeholder != tabbedPane.getSelectedComponent( ) )
                    {
                        tabbedPane.insertTab( view.title, view.icon, viewEntry.placeholder, view.tooltip, i );
                    }

                    viewEntry.overflowMenuItem.setVisible( false );
                }


                // Remove tabs until:
                //   - all tabs fit on the first tab run, and
                //   - there's room for the mandatory toolbars after the tabs
                //
                while ( tabbedPane.getTabCount( ) > 1 )
                {
                    int numTabs = tabbedPane.getTabCount( );
                    int iLast = numTabs - 1;
                    Rectangle lastTabBounds = tabbedPane.getBoundsAt( iLast );

                    boolean needsOverflow = ( numTabs < views.size( ) );
                    int wAvail = wTile - wCornerBar - ( needsOverflow ? wOverflowBar : 0 );
                    if ( tabbedPane.getTabRunCount( ) <= 1 && xAfter( lastTabBounds ) <= wAvail )
                    {
                        break;
                    }

                    int iVictim = ( tabbedPane.getSelectedIndex( ) == iLast ? 0 : iLast );
                    ViewKey viewKey = ( ( ViewPlaceholder ) tabbedPane.getComponentAt( iVictim ) ).viewKey;
                    viewMap.get( viewKey ).overflowMenuItem.setVisible( true );
                    tabbedPane.removeTabAt( iVictim );
                }


                // Place tabbedPane, overflowBar, cornerBar
                //
                int xAfterTabs = 0;
                int yAfterTabs = 0;
                for ( int i = 0; i < tabbedPane.getTabCount( ); i++ )
                {
                    Rectangle tabBounds = tabbedPane.getBoundsAt( i );
                    if ( tabBounds != null )
                    {
                        xAfterTabs = max( xAfterTabs, xAfter( tabBounds ) );
                        yAfterTabs = max( yAfterTabs, yAfter( tabBounds ) );
                    }
                }

                // XXX: -1 works around TinyLaF issue?
                int hTopBar = yAfterTabs - 1;

                boolean showOverflow = ( tabbedPane.getTabCount( ) < views.size( ) );

                // Now we have enough info to compute tabbedPane's minimum width
                int wTabbedPaneMin = xAfterTabs + wCornerBar + ( showOverflow ? wOverflowBar : 0 );
                int wTabbedPane = max( wTile, wTabbedPaneMin );
                int xCornerBar = wTabbedPane - wCornerBar;

                int hTabbedPaneMin = yAfterTabs + 6;
                int hTabbedPane = max( hTile, hTabbedPaneMin );

                tabbedPane.setBounds( 0, 0, wTabbedPane, hTabbedPane );
                tabbedPane.doLayout( );
                cornerBar.setBounds( xCornerBar, 0, wCornerBar, hTopBar );

                if ( showOverflow )
                {
                    overflowBar.setBounds( xAfterTabs, 0, wOverflowBar, hTopBar );
                    overflowBar.setVisible( true );
                }
                else
                {
                    overflowBar.setVisible( false );
                }


                // Place cardPanel and current view's toolbar
                //
                viewBarHolder.setVisible( false );
                viewBarHolder.removeAll( );

                ViewPlaceholder viewPlaceholder = ( ViewPlaceholder ) tabbedPane.getSelectedComponent( );
                if ( viewPlaceholder == null )
                {
                    cardPanel.setVisible( false );
                }
                else
                {
                    ViewEntry viewEntry = viewMap.get( viewPlaceholder.viewKey );
                    JToolBar viewBar = viewEntry.view.toolbar;
                    if ( viewBar != null )
                    {
                        JPanel viewCard = viewEntry.card;
                        int wViewBar = viewBar.getMinimumSize( ).width;
                        int xViewBar = xCornerBar - wViewBar;
                        int xViewBarMin = xAfterTabs + ( showOverflow ? wOverflowBar : 0 );

                        if ( xViewBar >= xViewBarMin )
                        {
                            viewCard.remove( viewBar );
                            viewBarHolder.add( viewBar );
                            viewBarHolder.setBounds( xViewBar, 0, wViewBar, hTopBar );
                            viewBarHolder.setVisible( true );
                        }
                        else
                        {
                            viewCard.add( viewBar, BorderLayout.NORTH );
                        }
                    }

                    cardPanel.setBounds( viewPlaceholder.getBounds( ) );
                    cardPanel.setVisible( true );
                }


                // Minimum and preferred sizes
                //
                int xAfterFirstTab = ( tabbedPane.getTabCount( ) > 0 ? xAfter( tabbedPane.getBoundsAt( 0 ) ) : 50 );
                this.minSize = new Dimension( xAfterFirstTab + wCornerBar + wOverflowBar, hTabbedPaneMin );
                this.prefSize = new Dimension( wTabbedPane, hTabbedPane );
            }

            public void addLayoutComponent( String name, Component comp )
            { }

            public void removeLayoutComponent( Component comp )
            { }

            public Dimension preferredLayoutSize( Container parent )
            {
                return prefSize;
            }

            public Dimension minimumLayoutSize( Container parent )
            {
                return minSize;
            }
        } );
    }

    @Override
    public boolean isOptimizedDrawingEnabled( )
    {
        return false;
    }

    @Override
    public int numViews( )
    {
        return views.size( );
    }

    @Override
    public View view( int viewNum )
    {
        return views.get( viewNum );
    }

    @Override
    public View selectedView( )
    {
        Component placeholder = tabbedPane.getSelectedComponent( );
        for ( ViewEntry viewEntry : viewMap.values( ) )
        {
            if ( viewEntry.placeholder == placeholder )
            {
                return viewEntry.view;
            }
        }
        return null;
    }

    @Override
    public void addView( final View view, int viewNum )
    {
        JPanel card = new JPanel( new BorderLayout( ) );
        card.add( view.component, BorderLayout.CENTER );
        cardPanel.add( card, view.viewKey.viewId );

        final ViewPlaceholder placeholder = new ViewPlaceholder( view.viewKey );

        JMenuItem overflowMenuItem = new JMenuItem( view.title, view.icon );
        overflowMenuItem.setToolTipText( view.tooltip );
        overflowMenuItem.addActionListener( new ActionListener( )
        {
            public void actionPerformed( ActionEvent ev )
            {
                selectView( view );
            }
        } );
        overflowPopup.add( overflowMenuItem );

        viewMap.put( view.viewKey, new ViewEntry( view, card, placeholder, overflowMenuItem ) );
        views.add( viewNum, view );
    }

    @Override
    public void removeView( View view )
    {
        ViewEntry viewEntry = viewMap.remove( view.viewKey );
        overflowPopup.remove( viewEntry.overflowMenuItem );
        tabbedPane.remove( viewEntry.placeholder );
        cardPanel.remove( viewEntry.card );
        views.remove( view );
    }

    @Override
    public void selectView( View view )
    {
        ViewEntry viewEntry = viewMap.get( view.viewKey );

        int tabNum = tabbedPane.indexOfComponent( viewEntry.placeholder );
        if ( tabNum < 0 )
        {
            // Something in here triggers a re-layout, which is going to remove and
            // re-add all tabs -- so doesn't matter what index we put the new tab at
            //
            tabNum = 0;
            tabbedPane.insertTab( view.title, view.icon, viewEntry.placeholder, view.tooltip, tabNum );
        }
        tabbedPane.setSelectedIndex( tabNum );

        // Would be nice to give the tab the keyboard focus, but can't find a
        // way to do it
    }

    @Override
    public int viewNumForTabAt( int x, int y )
    {
        int tabNum = tabbedPane.getUI( ).tabForCoordinate( tabbedPane, x, y );

        if ( 0 <= tabNum && tabNum < tabbedPane.getTabCount( ) )
        {
            Component placeholder = tabbedPane.getComponentAt( tabNum );
            for ( int viewNum = 0; viewNum < views.size( ); viewNum++ )
            {
                if ( viewEntry( viewNum ).placeholder == placeholder )
                {
                    return viewNum;
                }
            }
        }

        return -1;
    }

    @Override
    public Rectangle viewTabBounds( int viewNum )
    {
        int tabNum = tabbedPane.indexOfComponent( viewEntry( viewNum ).placeholder );
        if ( 0 <= tabNum && tabNum < tabbedPane.getTabCount( ) )
        {
            return tabbedPane.getBoundsAt( tabNum );
        }
        else
        {
            return null;
        }
    }

    @Override
    public void addDockingMouseAdapter( MouseAdapter mouseAdapter )
    {
        tabbedPane.addMouseListener( mouseAdapter );
        tabbedPane.addMouseMotionListener( mouseAdapter );
    }

    protected ViewEntry viewEntry( int viewNum )
    {
        return viewMap.get( views.get( viewNum ).viewKey );
    }



    protected static class ViewPlaceholder extends JPanel
    {
        public final ViewKey viewKey;

        public ViewPlaceholder( ViewKey viewKey )
        {
            this.viewKey = viewKey;
        }
    }


    protected static class ViewEntry
    {
        public final View view;
        public final JPanel card;
        public final ViewPlaceholder placeholder;
        public final JMenuItem overflowMenuItem;

        public ViewEntry( View view, JPanel card, ViewPlaceholder placeholder, JMenuItem overflowMenuItem )
        {
            this.view = view;
            this.card = card;
            this.placeholder = placeholder;
            this.overflowMenuItem = overflowMenuItem;
        }
    }

}
