package com.metsci.glimpse.docking;

import static com.metsci.glimpse.docking.MiscUtils.createVerticalBox;
import static com.metsci.glimpse.docking.MiscUtils.getAncestorOfClass;
import static java.awt.AWTEvent.MOUSE_WHEEL_EVENT_MASK;
import static java.awt.event.MouseEvent.BUTTON1;
import static java.lang.Math.ceil;
import static javax.swing.BorderFactory.createEmptyBorder;
import static javax.swing.Box.createVerticalGlue;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;

import com.metsci.glimpse.docking.DockingThemes.DockingTheme;
import com.metsci.glimpse.docking.TileImpl.TabComponentFactory;

@SuppressWarnings( "serial" )
public class TileFactoryStandard implements TileFactory
{

    protected final DockingGroup dockingGroup;

    public TileFactoryStandard( DockingGroup dockingGroup )
    {
        this.dockingGroup = dockingGroup;
    }

    @Override
    public Tile newTile( )
    {
        final DockingTheme theme = dockingGroup.theme;

        final Tile[] tileRef = { null };

        final JButton maximizeButton = new JButton( theme.maximizeIcon )
        {
            @Override
            public void paintComponent( Graphics g )
            {
                Tile tile = tileRef[0];
                MultiSplitPane docker = getAncestorOfClass( MultiSplitPane.class, tile );
                setIcon( docker.getMaximizedLeaf( ) == tile ? theme.unmaximizeIcon : theme.maximizeIcon );

                super.paintComponent( g );
            }
        };
        maximizeButton.setFocusable( false );

        TabComponentFactory tabCornerComponentFactory = new TabComponentFactory( )
        {
            @Override
            public Component createComponent( final Tile tile, final View view )
            {
                if ( view.closeable )
                {
                    JButton closeButton = new JButton( )
                    {
                        @Override
                        public void paintComponent( Graphics g )
                        {
                            Icon icon;
                            if ( model.isPressed( ) )
                            {
                                icon = getPressedIcon( );
                            }
                            else if ( model.isRollover( ) )
                            {
                                icon = getRolloverIcon( );
                            }
                            else
                            {
                                icon = getIcon( );
                            }

                            int y = ( int ) ceil( 0.5 * ( getHeight( ) - icon.getIconHeight( ) ) );
                            icon.paintIcon( this, g, 0, y );
                        }
                    };
                    closeButton.setOpaque( false );
                    closeButton.setBorder( null );

                    closeButton.setIcon( theme.closeViewIcon );
                    closeButton.setRolloverIcon( theme.closeViewHoveredIcon );
                    closeButton.setPressedIcon( theme.closeViewPressedIcon );

                    closeButton.addActionListener( new ActionListener( )
                    {
                        @Override
                        public void actionPerformed( ActionEvent ev )
                        {
                            dockingGroup.closeView( view );
                        }
                    } );

                    Box closeBox = createVerticalBox( createVerticalGlue( ), closeButton, createVerticalGlue( ) );
                    closeBox.setBorder( createEmptyBorder( 2, 0, 0, theme.lineThickness + theme.labelPadding ) );
                    return closeBox;
                }
                else
                {
                    return null;
                }
            }
        };

        final Tile tile = new TileImpl( theme, tabCornerComponentFactory, new Component[] { maximizeButton } );
        tileRef[0] = tile;

        dockingGroup.attachListenerTo( tile );

        final DockingMouseAdapter mouseAdapter = new DockingMouseAdapter( tile, dockingGroup, this );
        tile.addDockingMouseAdapter( mouseAdapter );

        // If the mouse-wheel is scrolled during a drag, the drag target stops receiving mouse events
        // (due to a bug somewhere in java.awt.LightweightDispatcher). It does not receive any more
        // drag events, nor does it receive the release event that should terminate the drag.
        //
        // Since no more drag events are going to get through, the best thing to do is to notify the
        // DockingMouseAdapter that the drag has terminated. This will cause the the dragged view to
        // land, as if the mouse had been released.
        //
        // Not even the wheel event itself makes it through to listeners, so we have to add an AWT global
        // listener. If there is not a drag in progress, DockingMouseAdapter.mouseReleased will have no
        // effect.
        //
        tile.getToolkit( ).addAWTEventListener( new AWTEventListener( )
        {
            @Override
            public void eventDispatched( AWTEvent ev )
            {
                if ( ev instanceof MouseWheelEvent )
                {
                    mouseAdapter.mouseReleased( BUTTON1, ( ( MouseWheelEvent ) ev ).getLocationOnScreen( ) );
                }
            }
        }, MOUSE_WHEEL_EVENT_MASK );

        maximizeButton.addActionListener( new ActionListener( )
        {
            @Override
            public void actionPerformed( ActionEvent ev )
            {
                MultiSplitPane docker = getAncestorOfClass( MultiSplitPane.class, tile );
                if ( docker.getMaximizedLeaf( ) == tile )
                {
                    docker.unmaximizeLeaf( );
                }
                else
                {
                    docker.maximizeLeaf( tile );
                }
            }
        } );

        return tile;
    }

}
