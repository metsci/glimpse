package com.metsci.glimpse.painter.info;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.text.BreakIterator;
import java.util.List;

import javax.media.opengl.GL;

import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.support.color.GlimpseColor;
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
    protected int borderSize = 6;
    protected float lineSpacing = 2;
    protected boolean breakOnEol = true;
    protected int offsetX = -20;
    protected int offsetY = -10;
    protected boolean clampToScreenEdges = true;

    protected SimpleTextLayout textLayout;
    protected BreakIterator breakIterator;
    protected List<TextBoundingBox> lines;
    protected Bounds linesBounds;

    protected int x;
    protected int y;

    public TooltipPainter( )
    {
        this.breakIterator = BreakIterator.getWordInstance( );
        this.paintBackground = true;
        this.paintBorder = true;
    }

    /**
     * Sets the location of the upper left corner of the tooltip box
     * in screen/pixel coordinates.
     */
    public TooltipPainter setLocation( int x, int y )
    {
        this.x = x;
        this.y = y;

        return this;
    }
    
    public TooltipPainter setOffset( int x, int y )
    {
        this.offsetX = x;
        this.offsetY = y;

        return this;
    }

    public TooltipPainter setLocation( GlimpseMouseEvent e )
    {
        return setLocation( e.getScreenPixelsX( ), e.getScreenPixelsY( ) );
    }

    public TooltipPainter setBorderSize( int size )
    {
        this.borderSize = size;
        this.lines = null; // signal that layout should be recalculated
        return this;
    }

    public int getBorderSize( )
    {
        return this.borderSize;
    }

    public TooltipPainter setFixedWidth( int fixedWidth )
    {
        this.fixedWidth = fixedWidth;
        this.isFixedWidth = true;
        this.lines = null; // signal that layout should be recalculated
        return this;
    }

    public TooltipPainter setUnlimitedWidth( )
    {
        this.isFixedWidth = false;
        this.lines = null; // signal that layout should be recalculated
        return this;
    }
    
    public void setClampToScreenEdges( boolean clamp )
    {
        this.clampToScreenEdges = clamp;
    }

    public int getFixedWidth( )
    {
        return this.fixedWidth;
    }

    public boolean isFixedWidth( )
    {
        return this.isFixedWidth;
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

    protected void updateTextLayout( )
    {
        Font font = textRenderer.getFont( );
        FontRenderContext frc = textRenderer.getFontRenderContext( );
        textLayout = new SimpleTextLayout( font, frc, breakIterator );
        textLayout.setBreakOnEol( breakOnEol );
        textLayout.setLineSpacing( lineSpacing );
    }

    protected void updateLayout( )
    {
        textLayout.doLayout( text, 0, 0, isFixedWidth ? fixedWidth : Float.MAX_VALUE );
        lines = textLayout.getLines( );

        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;

        if ( !lines.isEmpty( ) )
        {
            for ( TextBoundingBox line : lines )
            {
                minX = Math.min( minX, line.getMinX( ) );
                minY = Math.min( minY, line.getMinY( ) );
                maxX = Math.max( maxX, line.getMaxX( ) );
                maxY = Math.max( maxY, line.getMaxY( ) );
            }
        }

        linesBounds = new Bounds( minX - borderSize, maxX + borderSize, minY, maxY );
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

        if ( lines == null && textLayout != null && text != null )
        {
            updateLayout( );
        }

        if ( textRenderer == null || lines == null ) return;

        GL gl = context.getGL( );
        int width = bounds.getWidth( );
        int height = bounds.getHeight( );
        
        double clampX = 0;
        double clampY = 0;
        if ( clampToScreenEdges )
        {
            double maxX = x + linesBounds.maxX + offsetX;
            if ( maxX > width ) clampX = width - maxX;
            double minX = x + linesBounds.minX + offsetX;
            if ( minX < 0 ) clampX = -minX;
            
            double maxY = height - y + linesBounds.maxY + offsetY;
            if ( maxY > height ) clampY = height - maxY;
            double minY = height - y + linesBounds.minY + offsetY;
            if ( minY < 0 ) clampY = -minY;
        }

        gl.glMatrixMode( GL.GL_PROJECTION );
        gl.glLoadIdentity( );
        gl.glOrtho( -0.5, width - 1 + 0.5, -0.5, height - 1 + 0.5, -1, 1 );
        gl.glMatrixMode( GL.GL_MODELVIEW );
        gl.glLoadIdentity( );

        gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA );
        gl.glEnable( GL.GL_BLEND );

        if ( this.paintBackground || this.paintBorder )
        {
            if ( this.paintBackground )
            {
                // Draw Text Background
                gl.glColor4fv( backgroundColor, 0 );

                gl.glBegin( GL.GL_QUADS );
                try
                {
                    borderVertices( gl, height, clampX+offsetX, clampY+offsetY );
                }
                finally
                {
                    gl.glEnd( );
                }
            }

            if ( this.paintBorder )
            {
                // Draw Text Background
                gl.glColor4fv( borderColor, 0 );
                gl.glEnable( GL.GL_LINE_SMOOTH );

                gl.glBegin( GL.GL_LINE_LOOP );
                try
                {
                    borderVertices( gl, height, clampX+offsetX, clampY+offsetY );
                }
                finally
                {
                    gl.glEnd( );
                }
            }
        }

        gl.glDisable( GL.GL_BLEND );

        GlimpseColor.setColor( textRenderer, textColor );
        textRenderer.beginRendering( width, height );
        try
        {
            for ( TextBoundingBox line : lines )
            {
                int posX = ( int ) ( x + line.leftX + clampX + offsetX );
                int posY = ( int ) ( height - y + line.baselineY + clampY + offsetY );
                textRenderer.draw( line.text, posX, posY );
            }
        }
        finally
        {
            textRenderer.endRendering( );
        }
    }

    protected void borderVertices( GL gl, int height, double offsetX, double offsetY )
    {
        double posX = x + linesBounds.minX + offsetX;
        double posY = height - y + linesBounds.minY + offsetY;
        gl.glVertex2d( posX, posY );

        posX = x + linesBounds.maxX + offsetX;
        posY = height - y + linesBounds.minY + offsetY;
        gl.glVertex2d( posX, posY );

        posX = x + linesBounds.maxX + offsetX;
        posY = height - y + linesBounds.maxY + offsetY;
        gl.glVertex2d( posX, posY );

        posX = x + linesBounds.minX + offsetX;
        posY = height - y + linesBounds.maxY + offsetY;
        gl.glVertex2d( posX, posY );
    }

    protected static class Bounds
    {
        public double minX, maxX, minY, maxY;

        public Bounds( double minX, double maxX, double minY, double maxY )
        {
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;
        }

        @Override
        public String toString( )
        {
            return String.format( "%f %f %f %f", minX, maxX, minY, maxY );
        }
    }
}
