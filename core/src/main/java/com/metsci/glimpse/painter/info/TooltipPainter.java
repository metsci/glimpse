package com.metsci.glimpse.painter.info;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.text.BreakIterator;
import java.util.List;

import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.support.font.SimpleTextLayout;
import com.metsci.glimpse.support.font.SimpleTextLayout.TextBoundingBox;

/**
 * Displays tool tip text at a specified position.
 * 
 * @author ulman
 */
public class TooltipPainter extends SimpleTextPainter
{
    // if true, tooltip text will be wrapped if it extends past the edge of the box
    protected boolean isFixedWidth = false;
    protected int fixedWidth = 50;

    protected SimpleTextLayout textLayout;
    protected BreakIterator breakIterator;
    protected List<TextBoundingBox> lines;

    protected float lineSpacing;
    protected boolean breakOnEol;
    
    public TooltipPainter( )
    {
        breakIterator = BreakIterator.getWordInstance( );
    }
    

    public TooltipPainter setBreakOnEol( boolean breakOnEol )
    {
        this.breakOnEol = breakOnEol;
        this.textLayout = null; // signal that textLayout should be recreated
        return this;
    }

    /**
     * Whether to force a break on the end of line characters (\r \f \n).
     */
    public boolean getBreakOnEol( )
    {
        return breakOnEol;
    }

    public TooltipPainter setLineSpacing( float lineSpacing )
    {
        this.lineSpacing = lineSpacing;
        this.textLayout = null; // signal that textLayout should be recreated
        return this;
    }

    /**
     * The spacing between the bottom (descent) of one line of text to the top
     * (ascent) of the next line.
     */
    public float getLineSpacing( )
    {
        return lineSpacing;
    }
    
    public TooltipPainter setBreakIterator( BreakIterator breakIterator )
    {
        this.breakIterator = breakIterator;
        this.textLayout = null; // signal that textLayout should be recreated
        return this;
    }
    
    @Override
    public TooltipPainter setText( String text )
    {
        this.text = text;
        this.lines = null; // signal that layout should be recalculated
        return this;
    }

    @Override
    protected void paintTo( GlimpseContext context, GlimpseBounds bounds )
    {
        if ( newFont != null )
        {
            updateTextRenderer( );
        }
        
        if ( textLayout == null && textRenderer != null )
        {
            updateTextLayout( );
        }
        
        if ( lines == null && textLayout != null )
        {
            textLayout.doLayout( text, 0, 0, isFixedWidth ? fixedWidth : Float.MAX_VALUE );
            lines = textLayout.getLines( );
        }
        
        
    }
    
    protected void updateTextLayout( )
    {
        Font font = textRenderer.getFont( );
        FontRenderContext frc = textRenderer.getFontRenderContext( );
        textLayout = new SimpleTextLayout( font, frc, breakIterator );
        textLayout.setBreakOnEol( breakOnEol );
        textLayout.setLineSpacing( lineSpacing );
    }
}
