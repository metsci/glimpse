package com.metsci.glimpse.charts.slippy;

import java.awt.Font;

import com.metsci.glimpse.painter.group.DelegatePainter;
import com.metsci.glimpse.painter.info.SimpleTextPainter;
import com.metsci.glimpse.support.font.FontUtils;

/**
 * This just wraps a tile painter and an attribution text painter (if one is specified).
 * 
 * @author oren
 *
 */
public class SlippyMapPainter extends DelegatePainter
{

    protected final SlippyMapTilePainter tilePainter;
    protected final String attribution;
    protected final SimpleTextPainter attributionPainter;

    private static final Font textFont = FontUtils.getDefaultPlain( 15.0f );
    private static final float[] textColor = new float[] { 0.5f, 0.5f, 0.5f, 1.0f };
    private static final float[] bgColor = new float[] { 0.95f, 0.95f, 0.95f, 1f };

    public SlippyMapPainter( SlippyMapTilePainter tilePainter )
    {
        this( tilePainter, null );
    }

    public SlippyMapPainter( SlippyMapTilePainter tilePainter, String attribution )
    {
        super( );
        this.tilePainter = tilePainter;
        this.attribution = attribution;
        addPainter( tilePainter );
        if ( attribution != null )
        {
            attributionPainter = createAttributionPainter( attribution );
            addPainter( attributionPainter );
        }
        else
        {
            attributionPainter = null;
        }
    }

    public static SimpleTextPainter createAttributionPainter( String text )
    {
        SimpleTextPainter painter = new SimpleTextPainter( );
        painter.setText( text );
        painter.setFont( textFont );
        painter.setColor( textColor );
        painter.setBackgroundColor( bgColor );
        painter.setPaintBackground( true );
        return painter;
    }

    public SlippyMapTilePainter getTilePainter( )
    {
        return tilePainter;
    }

    public SimpleTextPainter getAttributionPainter( )
    {
        return attributionPainter;
    }

}
