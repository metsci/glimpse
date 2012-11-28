package com.metsci.glimpse.support.settings;

import static com.metsci.glimpse.support.color.GlimpseColor.*;
import static com.metsci.glimpse.support.font.FontUtils.*;

public class BlackAndWhiteLookAndFeel extends AbstractLookAndFeel
{
    public BlackAndWhiteLookAndFeel( )
    {
        map.put( CROSSHAIR_COLOR, getWhite( ) );
        map.put( BORDER_COLOR, getWhite( ) );
        
        map.put( PLOT_BACKGROUND_COLOR, getBlack( ) );
        map.put( FRAME_BACKGROUND_COLOR, getBlack( ) );

        map.put( AXIS_TEXT_COLOR, getWhite( ) );
        map.put( AXIS_TICK_COLOR, getWhite( ) );
        map.put( AXIS_TAG_COLOR, getWhite( 0.2f ) );
        
        map.put( AXIS_FONT, getDefaultPlain( 11 ) );
        map.put( TITLE_FONT, getDefaultBold( 14 ) );
        
        map.put( TEXT_BACKGROUND_COLOR, getBlack( ) );
    }
}
