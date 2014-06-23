package com.metsci.glimpse.docking2;

import static java.util.Collections.unmodifiableSet;

import java.awt.Rectangle;
import java.awt.geom.Area;
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
        landingIndicator.getContentPane( ).setBackground( theme.landingIndicatorColor );
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

            Area shape = new Area( new Rectangle( 0, 0, bounds.width, bounds.height ) );
            int thickness = theme.landingIndicatorThickness;
            shape.subtract( new Area( new Rectangle( thickness, thickness, bounds.width - 2*thickness, bounds.height - 2*thickness ) ) );
            landingIndicator.setShape( shape );

            landingIndicator.setVisible( true );
        }
    }

}
