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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import static com.metsci.glimpse.docking.DockingUtils.*;
import static java.lang.Math.*;
import static javax.swing.BorderFactory.*;

public class CustomTile extends JComponent implements Tile
{


    protected class CustomTab extends JLabel
    {
        protected final View view;
        protected boolean selected;

        public CustomTab( View view )
        {
            super( view.title, view.icon, LEFT );
            setToolTipText( view.tooltip );

            this.view = view;
            this.selected = false;
            setForeground( Color.lightGray );

            setBorder( createEmptyBorder( 0, 4, 0, 4 ) );
        }

        public void setSelected( boolean selected )
        {
            if ( selected != this.selected )
            {
                this.selected = selected;
                setForeground( selected ? Color.black : Color.lightGray );
            }
        }
    }



    protected final JPanel topBar;

    protected final JToolBar tabBar;

    protected final JToolBar overflowBar;
    protected final JToggleButton overflowPopupButton;
    protected final JPopupMenu overflowPopup;

    protected final JToolBar cornerBar;

    protected final JPanel viewBarHolder;

    protected final CardLayout cardLayout;
    protected final JPanel cardPanel;

    protected final List<MouseAdapter> dockingMouseAdapters;

    protected final Map<ViewKey,ViewEntry> viewMap;
    protected final List<View> views;
    protected View selectedView;


    public CustomTile( )
    {
        this.tabBar = newToolbar( false );

        this.overflowBar = newToolbar( true );
        this.overflowPopupButton = new JToggleButton( "\u00BB" );
        this.overflowPopup = newButtonPopup( overflowPopupButton );
        overflowBar.add( overflowPopupButton );

        this.cornerBar = newToolbar( true );
        cornerBar.add( new JButton( "Min" ) );
        cornerBar.add( new JButton( "Max" ) );

        this.viewBarHolder = new JPanel( new GridLayout( 1, 1 ) );

        this.cardLayout = new CardLayout( );
        this.cardPanel = new JPanel( cardLayout );

        this.dockingMouseAdapters = newArrayList( );


        this.viewMap = newHashMap( );
        this.views = newArrayList( );
        this.selectedView = null;


        this.topBar = new JPanel( );
        topBar.setOpaque( false );

        topBar.add( tabBar );
        topBar.add( overflowBar );
        topBar.add( viewBarHolder );
        topBar.add( cornerBar );

        topBar.setLayout( new LayoutManager( )
        {
            public void layoutContainer( Container parent )
            {

                int wTotal = topBar.getWidth( );
                int wCornerBar = cornerBar.getMinimumSize( ).width;
                int wOverflowBar = overflowBar.getMinimumSize( ).width;

                for ( View view : views )
                {
                    ViewEntry viewEntry = viewMap.get( view.viewKey );
                    viewEntry.overflowMenuItem.setVisible( false );
                    viewEntry.tab.setVisible( true );
                }
                overflowBar.setVisible( false );


                boolean needsOverflow = false;
                while ( true )
                {
                    tabBar.doLayout( );
                    int wTabBar = tabBar.getMinimumSize( ).width;
                    int wAvail = wTotal - wCornerBar - ( needsOverflow ? wOverflowBar : 0 );
                    if ( wTabBar <= wAvail ) break;

                    int numVisible = 0;
                    ViewEntry firstVisible = null;
                    ViewEntry lastVisible = null;
                    for ( View view : views )
                    {
                        ViewEntry viewEntry = viewMap.get( view.viewKey );
                        if ( viewEntry.tab.isVisible( ) )
                        {
                            numVisible++;
                            lastVisible = viewEntry;
                            if ( firstVisible == null ) firstVisible = viewEntry;
                        }
                    }
                    if ( numVisible <= 1 ) break;

                    ViewEntry victim = ( lastVisible.view == selectedView ? firstVisible : lastVisible );
                    victim.overflowMenuItem.setVisible( true );
                    victim.tab.setVisible( false );
                    needsOverflow = true;
                }


                int y = 0;
                int hTotal = topBar.getHeight( );

                int xTabBar = 0;
                int wTabBar = tabBar.getMinimumSize( ).width;
                tabBar.setBounds( xTabBar, y, wTabBar, hTotal );

                if ( needsOverflow )
                {
                    int xOverflowBar = tabBar.getX( ) + tabBar.getWidth( );
                    overflowBar.setBounds( xOverflowBar, y, wOverflowBar, hTotal );
                    overflowBar.setVisible( true );
                }

                int xCornerBar = wTotal - wCornerBar;
                cornerBar.setBounds( xCornerBar, y, wCornerBar, hTotal );


                viewBarHolder.removeAll( );

                if ( selectedView == null || selectedView.toolbar == null )
                {
                    viewBarHolder.setVisible( false );
                }
                else
                {
                    JPanel viewCard = viewMap.get( selectedView.viewKey ).card;

                    JToolBar viewBar = selectedView.toolbar;
                    int xViewBar = xTabBar + wTabBar + ( needsOverflow ? wOverflowBar : 0 );
                    int wViewBar = xCornerBar - xViewBar;

                    if ( viewBar.getMinimumSize( ).width <= wViewBar )
                    {
                        viewCard.remove( viewBar );
                        viewBarHolder.add( viewBar );
                        viewBarHolder.setBounds( xViewBar, y, wViewBar, hTotal );
                        viewBarHolder.setVisible( true );
                    }
                    else
                    {
                        viewBarHolder.setVisible( false );
                        viewCard.add( viewBar, BorderLayout.NORTH );
                    }
                }

            }

            public void addLayoutComponent( String name, Component comp )
            { }

            public void removeLayoutComponent( Component comp )
            { }

            public Dimension preferredLayoutSize( Container parent )
            {
                int wTile = getWidth( );
                int hBars = max( tabBar.getPreferredSize( ).height, max( overflowBar.getPreferredSize( ).height, cornerBar.getPreferredSize( ).height ) );
                return new Dimension( wTile, hBars );
            }

            public Dimension minimumLayoutSize( Container parent )
            {
                int wTab = 0;
                for ( View view : views )
                {
                    ViewEntry viewEntry = viewMap.get( view.viewKey );
                    wTab = max( wTab, viewEntry.tab.getMinimumSize( ).width );
                }
                int wBars = wTab + overflowBar.getMinimumSize( ).width + cornerBar.getMinimumSize( ).width;
                int hBars = max( tabBar.getMinimumSize( ).height, max( overflowBar.getMinimumSize( ).height, cornerBar.getMinimumSize( ).height ) );
                return new Dimension( wBars, hBars );
            }
        } );


        setLayout( new BorderLayout( ) );
        add( topBar, BorderLayout.NORTH );
        add( cardPanel, BorderLayout.CENTER );
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
        return selectedView;
    }

    @Override
    public void addView( final View view, int viewNum )
    {
        JPanel card = new JPanel( new BorderLayout( ) );
        card.add( view.component, BorderLayout.CENTER );
        cardPanel.add( card, view.viewKey.viewId );

        CustomTab tab = new CustomTab( view );
        tab.addMouseListener( new MouseAdapter( )
        {
            public void mousePressed( MouseEvent ev )
            {
                selectView( view );
            }
        } );
        for ( MouseAdapter mouseAdapter : dockingMouseAdapters )
        {
            addMouseAdapter( tab, mouseAdapter );
        }
        tabBar.add( tab, viewNum );

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

        viewMap.put( view.viewKey, new ViewEntry( view, card, tab, overflowMenuItem ) );
        views.add( viewNum, view );

        if ( selectedView == null )
        {
            selectView( view );
        }
    }

    @Override
    public void removeView( View view )
    {
        boolean removingSelectedView = ( view == selectedView );

        if ( removingSelectedView )
        {
            selectView( null );
        }

        ViewEntry viewEntry = viewMap.remove( view.viewKey );
        overflowPopup.remove( viewEntry.overflowMenuItem );
        tabBar.remove( viewEntry.tab );
        cardPanel.remove( viewEntry.card );
        views.remove( view );

        if ( removingSelectedView && !views.isEmpty( ) )
        {
            selectView( views.get( 0 ) );
        }
    }

    @Override
    public void selectView( View view )
    {
        if ( view == selectedView ) return;

        if ( selectedView != null )
        {
            ViewEntry viewEntry = viewMap.get( selectedView.viewKey );
            viewEntry.tab.setSelected( false );
            cardPanel.setVisible( false );
        }

        if ( view != null )
        {
            ViewEntry viewEntry = viewMap.get( view.viewKey );
            viewEntry.tab.setSelected( true );
            cardLayout.show( cardPanel, view.viewKey.viewId );
            cardPanel.setVisible( true );
        }

        selectedView = view;

        topBar.doLayout( );
    }

    @Override
    public int viewNumForTabAt( int x, int y )
    {
        for ( int viewNum = 0; viewNum < numViews( ); viewNum++ )
        {
            Rectangle tabBounds = viewTabBounds( viewNum );
            if ( tabBounds != null && tabBounds.contains( x, y ) )
            {
                return viewNum;
            }
        }
        return -1;
    }

    @Override
    public Rectangle viewTabBounds( int viewNum )
    {
        CustomTab tab = viewEntry( viewNum ).tab;
        if ( !tab.isVisible( ) ) return null;

        // Tab position relative to tile
        int x = 0;
        int y = 0;
        for ( Component c = tab; c != this; c = c.getParent( ) )
        {
            x += c.getX( );
            y += c.getY( );
        }

        return new Rectangle( x, y, tab.getWidth( ), tab.getHeight( ) );
    }

    @Override
    public void addDockingMouseAdapter( MouseAdapter mouseAdapter )
    {
        for ( View view : views )
        {
            CustomTab tab = viewMap.get( view.viewKey ).tab;
            addMouseAdapter( tab, mouseAdapter );
        }

        this.dockingMouseAdapters.add( mouseAdapter );
    }

    public static void addMouseAdapter( Component c, MouseAdapter mouseAdapter )
    {
        c.addMouseListener( mouseAdapter );
        c.addMouseMotionListener( mouseAdapter );
        c.addMouseWheelListener( mouseAdapter );
    }

    protected ViewEntry viewEntry( int viewNum )
    {
        return viewMap.get( views.get( viewNum ).viewKey );
    }



    protected static class ViewEntry
    {
        public final View view;
        public final JPanel card;
        public final CustomTab tab;
        public final JMenuItem overflowMenuItem;

        public ViewEntry( View view, JPanel card, CustomTab tab, JMenuItem overflowMenuItem )
        {
            this.view = view;
            this.card = card;
            this.tab = tab;
            this.overflowMenuItem = overflowMenuItem;
        }
    }

}
