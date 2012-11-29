package com.metsci.glimpse.support.settings;

import static com.metsci.glimpse.support.color.GlimpseColor.*;
import static com.metsci.glimpse.support.font.FontUtils.*;

import com.metsci.glimpse.support.color.GlimpseColor;

/**
 * A Glimpse LookAndFeel with a blue color scheme.
 * 
 * @author ulman
 */
public class OceanLookAndFeel extends AbstractLookAndFeel
{
    public OceanLookAndFeel( )
    {
        map.put( CROSSHAIR_COLOR, getBlack( ) );
        map.put( BORDER_COLOR, getWhite( ) );
        
        map.put( PLOT_BACKGROUND_COLOR, addRgb( GlimpseColor.fromColorRgb( 25, 42, 62 ), -0.08f ) );
        map.put( FRAME_BACKGROUND_COLOR, GlimpseColor.fromColorRgb( 25, 42, 62 ) );
        
        map.put( AXIS_TEXT_COLOR, getWhite( ) );
        map.put( AXIS_TICK_COLOR, getWhite( ) );
        map.put( AXIS_TAG_COLOR, getWhite( 0.2f ) );
        
        map.put( AXIS_FONT, getDefaultPlain( 11 ) );
        map.put( TITLE_FONT, getDefaultPlain( 14 ) );
        
        map.put( TEXT_BACKGROUND_COLOR, getBlack( ) );
    }
}
