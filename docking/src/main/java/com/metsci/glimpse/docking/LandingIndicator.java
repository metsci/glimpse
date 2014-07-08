package com.metsci.glimpse.docking;

import static com.metsci.glimpse.docking.LandingIndicator.ReprType.OPAQUE_WINDOW;
import static com.metsci.glimpse.docking.LandingIndicator.ReprType.SHAPED_WINDOW;
import static com.metsci.glimpse.docking.LandingIndicator.ReprType.TRANSLUCENT_WINDOW;
import static java.awt.GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSPARENT;
import static java.awt.GraphicsDevice.WindowTranslucency.TRANSLUCENT;
import static javax.swing.BorderFactory.createLineBorder;

import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.geom.Area;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.metsci.glimpse.docking.DockingThemes.DockingTheme;

public class LandingIndicator
{

    protected static enum ReprType
    {
        OPAQUE_WINDOW,
        TRANSLUCENT_WINDOW,
        SHAPED_WINDOW
    }


    protected final DockingTheme theme;
    protected final JFrame frame;
    protected final JPanel frameContent;

    protected ReprType recentReprType;


    public LandingIndicator( DockingTheme theme )
    {
        this.theme = theme;

        this.frame = new JFrame( );
        frame.setAlwaysOnTop( true );
        frame.setFocusable( false );
        frame.setUndecorated( true );

        this.frameContent = new JPanel( );
        frame.setContentPane( frameContent );

        this.recentReprType = null;
    }

    public void setBounds( Rectangle bounds )
    {
        if ( bounds == null )
        {
            frame.setVisible( false );
        }
        else
        {
            frame.setBounds( bounds );

            GraphicsDevice device = frame.getGraphicsConfiguration( ).getDevice( );
            if ( device.isWindowTranslucencySupported( PERPIXEL_TRANSPARENT ) )
            {
                if ( recentReprType != SHAPED_WINDOW )
                {
                    // Set the whole pane to the bg color, to minimize flicker
                    frameContent.setBackground( theme.landingIndicatorColor );
                    frameContent.setBorder( null );
                    this.recentReprType = SHAPED_WINDOW;
                }

                int thickness = theme.landingIndicatorThickness;
                Area shape = new Area( new Rectangle( 0, 0, bounds.width, bounds.height ) );
                shape.subtract( new Area( new Rectangle( thickness, thickness, bounds.width - 2*thickness, bounds.height - 2*thickness ) ) );
                frame.setShape( shape );
            }
            else if ( device.isWindowTranslucencySupported( TRANSLUCENT ) )
            {
                if ( recentReprType != TRANSLUCENT_WINDOW )
                {
                    frame.setShape( null );
                    frameContent.setBackground( null );
                    frameContent.setBorder( createLineBorder( theme.landingIndicatorColor, theme.landingIndicatorThickness ) );
                    frame.setOpacity( 0.5f );
                    this.recentReprType = TRANSLUCENT_WINDOW;
                }
            }
            else
            {
                if ( recentReprType != OPAQUE_WINDOW )
                {
                    frame.setShape( null );
                    frameContent.setBackground( null );
                    frameContent.setBorder( createLineBorder( theme.landingIndicatorColor, theme.landingIndicatorThickness ) );
                    this.recentReprType = OPAQUE_WINDOW;
                }
            }

            frame.setVisible( true );
        }
    }

    public void dispose( )
    {
        frame.dispose( );
    }

}
