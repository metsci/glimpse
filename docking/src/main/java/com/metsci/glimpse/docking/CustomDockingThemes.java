package com.metsci.glimpse.docking;

import java.awt.Color;
import java.util.logging.Logger;

import static java.awt.Color.*;

public class CustomDockingThemes
{

    protected static final Logger logger = Logger.getLogger( CustomDockingThemes.class.getName( ) );


    public static final CustomDockingTheme defaultDockingTheme = new CustomDockingTheme( 5,

                                                                                         1, // Even lineThickness values do NOT work well
                                                                                         5,
                                                                                         2,
                                                                                         4,

                                                                                         lightGray,
                                                                                         white,
                                                                                         darkGray,
                                                                                         darkGray );


    public static CustomDockingTheme newDockingTheme( Color lineColor, Color textColor )
    {
        return new CustomDockingTheme( defaultDockingTheme.dividerSize,

                                       defaultDockingTheme.lineThickness,
                                       defaultDockingTheme.cornerRadius,
                                       defaultDockingTheme.cardPadding,
                                       defaultDockingTheme.labelPadding,

                                       lineColor,
                                       defaultDockingTheme.highlightColor,
                                       textColor,
                                       textColor );
    }


    public static CustomDockingTheme tinyLafDockingTheme( )
    {
        try
        {
            return tinyLafDockingTheme0( );
        }
        catch ( Exception e )
        {
            logger.warning( "TinyLaF is not accessible; default docking theme will be used" );
            return defaultDockingTheme;
        }
    }

    public static CustomDockingTheme tinyLafDockingTheme0( ) throws Exception
    {
        return newDockingTheme( tinyLafColor( "tabPaneBorderColor" ), tinyLafColor( "tabFontColor" ) );
    }

    public static Color tinyLafColor( String fieldName ) throws Exception
    {
        Object sbRef = Class.forName( "de.muntjak.tinylookandfeel.Theme" ).getField( fieldName ).get( null );
        return ( Color ) Class.forName( "de.muntjak.tinylookandfeel.util.SBReference" ).getMethod( "getColor" ).invoke( sbRef );
    }

}
