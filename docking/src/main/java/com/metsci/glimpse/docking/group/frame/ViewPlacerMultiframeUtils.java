package com.metsci.glimpse.docking.group.frame;

import static java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment;
import static java.lang.Math.round;

import java.awt.Rectangle;

public class ViewPlacerMultiframeUtils
{

    public static Rectangle fallbackFrameBounds( )
    {
        float fracOfScreen = 0.85f;
        Rectangle screenBounds = getLocalGraphicsEnvironment( ).getMaximumWindowBounds( );
        int width = round( fracOfScreen * screenBounds.width );
        int height = round( fracOfScreen * screenBounds.height );
        int x = screenBounds.x + ( ( screenBounds.width - width ) / 2 );
        int y = screenBounds.y + ( ( screenBounds.height - height ) / 2 );
        return new Rectangle( x, y, width, height );
    }

}
