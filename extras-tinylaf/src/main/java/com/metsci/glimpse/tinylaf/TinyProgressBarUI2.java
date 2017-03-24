package com.metsci.glimpse.tinylaf;

import static java.awt.BasicStroke.CAP_BUTT;
import static java.awt.BasicStroke.JOIN_BEVEL;
import static javax.swing.SwingConstants.HORIZONTAL;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

import net.sf.tinylaf.TinyProgressBarUI;

public class TinyProgressBarUI2 extends TinyProgressBarUI
{

    /**
     * @see ComponentUI#createUI(JComponent)
     */
    public static ComponentUI createUI( JComponent c )
    {
        return new TinyProgressBarUI2( );
    }

    @Override
    protected void paintDeterminate( Graphics g, JComponent c )
    {
        Insets insets = this.progressBar.getInsets( );
        int x = insets.left;
        int y = insets.top;
        int w = this.progressBar.getWidth( ) - ( insets.right + insets.left );
        int h = this.progressBar.getHeight( ) - ( insets.top + insets.bottom );
        int amountFull = this.getAmountFull( insets, w, h );

        // Background
        g.translate( x, y );
        if ( !this.progressBar.isOpaque( ) )
        {
            g.setColor( this.progressBar.getBackground( ) );
            g.fillRect( 0, 0, w, h );
        }
        g.translate( -x, -y );

        // Bar
        g.setColor( this.progressBar.getForeground( ) );
        if ( this.progressBar.getOrientation( ) == HORIZONTAL )
        {
            ( ( Graphics2D ) g ).setStroke( new BasicStroke( ( float ) h, CAP_BUTT, JOIN_BEVEL ) );
            if ( c.getComponentOrientation( ).isLeftToRight( ) )
            {
                g.drawLine( x, y+(h/2), x + amountFull, y+(h/2) );
            }
            else
            {
                g.drawLine( x+w, y+(h/2), x+w - amountFull, y+(h/2) );
            }
        }
        else
        {
            ( ( Graphics2D ) g ).setStroke( new BasicStroke( ( float ) w, CAP_BUTT, JOIN_BEVEL ) );
            g.drawLine( x+(w/2), y+h, x+(w/2), y+h - amountFull );
        }

        // Text
        if ( progressBar.isStringPainted( ) )
        {
            g.setFont( c.getFont( ) );
            this.paintString( g, x, y, w, h, amountFull, insets );
        }
    }

    @Override
    protected void paintIndeterminate( Graphics g, JComponent c )
    {
        Insets insets = this.progressBar.getInsets( );
        int x = insets.left;
        int y = insets.top;
        int w = this.progressBar.getWidth( ) - ( insets.right + insets.left );
        int h = this.progressBar.getHeight( ) - ( insets.top + insets.bottom );

        // Background
        if ( !this.progressBar.isOpaque( ) )
        {
            g.setColor( this.progressBar.getBackground( ) );
            g.fillRect( x, y, w, h );
        }

        // Bar
        this.boxRect = this.getBox( this.boxRect );
        if ( this.boxRect != null )
        {
            g.setColor( this.progressBar.getForeground( ) );
            g.fillRect( this.boxRect.x, this.boxRect.y, this.boxRect.width, this.boxRect.height );
        }

        // Text
        if ( progressBar.isStringPainted( ) )
        {
            g.setFont( c.getFont( ) );
            this.paintString( g, x, y, w, h, -1, insets );
        }
    }

}
