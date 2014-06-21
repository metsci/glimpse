package com.metsci.glimpse.docking2;

import static java.util.Collections.unmodifiableSet;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.JFrame;

import com.metsci.glimpse.docking.DockingTheme;

public class DockingPaneGroup
{

    public final DockingTheme theme;

    protected final Set<DockingPane> dockersMod;
    public final Set<DockingPane> dockers;

    protected final JFrame landingIndicator;


    public DockingPaneGroup( DockingTheme theme )
    {
        this.theme = theme;

        this.dockersMod = new LinkedHashSet<DockingPane>( );
        this.dockers = unmodifiableSet( dockersMod );

        this.landingIndicator = new JFrame( );
        landingIndicator.setAlwaysOnTop( true );
        landingIndicator.setFocusable( false );
        landingIndicator.setUndecorated( true );
        landingIndicator.setBackground( new Color( 1f, 0f, 0f, 0.5f ) );
    }

    public DockingPane addNewDocker( )
    {
        DockingPane docker = new DockingPane( theme.dividerSize );
        dockersMod.add( docker );
        return docker;
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
            landingIndicator.setVisible( true );
        }
    }

}
