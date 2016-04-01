/*
 * Copyright (c) 2016, Metron, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Metron, Inc. nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL METRON, INC. BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.metsci.glimpse.painter.decoration;

import static com.metsci.glimpse.support.font.FontUtils.getDefaultBold;
import static com.metsci.glimpse.support.font.FontUtils.getDefaultPlain;

import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.media.opengl.GL2;

import com.jogamp.opengl.util.awt.TextRenderer;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.painter.base.GlimpsePainter2D;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.settings.AbstractLookAndFeel;
import com.metsci.glimpse.support.settings.LookAndFeel;

/**
 * Displays a simple color based legend floating on top of the plot.
 *
 * @author bumgarner
 */
public abstract class LegendPainter extends GlimpsePainter2D
{

    //TODO is there a better way to support this?
    public static enum LegendPlacement
    {
        N, NE, E, SE, S, SW, W, NW;
    }

    private float[] textColor = GlimpseColor.getBlack( );

    //The width of the color item next to the legend.
    protected int itemWidth = 30;

    //space between the items in the legend and from the edges of the window.
    private int spacer = 10;

    private TextRenderer textRenderer;

    //To keep consistent ordering, also keep a list
    private final List<String> list;
    private final Map<String, float[]> colors;

    //Relative placement in the window
    private LegendPlacement placement;

    /*
     * Offsets are relative to the corner specified. Increasing offsetX
     * will move the legend further to the left if on the east side, and
     * right if on the west side. It has no effect for straight North/South.
     * Similarly for offsetY and the north/south borders.
     */
    private int offsetX = 10;
    private int offsetY = 10;

    private boolean fontSet = false;

    private volatile Font newFont = null;
    private volatile boolean antialias = false;

    public LegendPainter( LegendPlacement placement )
    {
        this.placement = placement;
        this.list = new ArrayList<String>( );
        this.colors = new HashMap<String, float[]>( );
        setFont( 15, false );
    }

    public LegendPainter setFont( Font font )
    {
        setFont( font, false );
        return this;
    }

    public LegendPainter setFont( Font font, boolean antialias )
    {
        this.newFont = font;
        this.antialias = antialias;
        this.fontSet = true;
        return this;
    }

    public LegendPainter setFont( int size, boolean bold )
    {
        setFont( size, bold, false );
        return this;
    }

    public LegendPainter setFont( int size, boolean bold, boolean antialias )
    {
        if ( bold )
        {
            setFont( getDefaultBold( size ), antialias );
        }
        else
        {
            setFont( getDefaultPlain( size ), antialias );
        }

        return this;
    }

    public Font getFont( )
    {
        if ( textRenderer == null )
        {
            return newFont;
        }
        else
        {
            return textRenderer.getFont( );
        }
    }

    public LegendPainter setTextColor( float[] rgba )
    {
        textColor = rgba;
        return this;
    }

    public void setTextColor( float r, float g, float b, float a )
    {
        textColor[0] = r;
        textColor[1] = g;
        textColor[2] = b;
        textColor[3] = a;
    }

    public void addItem( String label, float r, float g, float b, float a )
    {
        addItem( label, new float[] { r, g, b, a } );
    }

    public synchronized void addItem( String label, float[] rgba )
    {
        if ( !colors.containsKey( label ) )
        {
            list.add( label );
        }
        colors.put( label, rgba );
    }

    public synchronized void removeItem( String label )
    {
        list.remove( label );
        colors.remove( label );
    }

    public synchronized void clear( )
    {
        list.clear( );
        colors.clear( );
    }

    public synchronized void setColor( String label, float r, float g, float b, float a )
    {
        float[] rgba = colors.get( label );
        rgba[0] = r;
        rgba[1] = g;
        rgba[2] = b;
        rgba[3] = a;
    }

    public void setColor( String label, float[] rgba )
    {
        setColor( label, rgba[0], rgba[1], rgba[2], rgba[3] );
    }

    public void setPlacement( LegendPlacement placement )
    {
        this.placement = placement;
    }

    public void setOffsetX( int offset )
    {
        this.offsetX = offset;
    }

    public void setOffsetY( int offset )
    {
        this.offsetY = offset;
    }

    public void setOffset( int offsetX, int offsetY )
    {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    /**
     * Sets the space between elements in the legend.
     * @param spacer
     */
    public void setSpacing( int spacer )
    {
        this.spacer = spacer;
    }

    /**
     * Sets the width of the item to the left of the label in the legend.
     * @param width
     */
    public void setLegendItemWidth( int width )
    {
        this.itemWidth = width;
    }

    private synchronized void displayLegend( GL2 gl, int width, int height )
    {
        //Figure out dimensions and position
        int lw = 0;
        int lh = spacer;
        for ( String l : list )
        {
            Rectangle2D bounds = textRenderer.getBounds( l );
            lh += bounds.getHeight( ) + spacer;
            lw = ( int ) Math.max( bounds.getWidth( ) + itemWidth + spacer, lw );
        }
        lw += spacer * 2;

        int lx = upperLeftX( width, height, lw, lh );
        int ly = upperLeftY( width, height, lw, lh );

        //draw a white box and black border
        gl.glColor4fv( GlimpseColor.getWhite( ), 0 );
        gl.glBegin( GL2.GL_POLYGON );
        try
        {
            gl.glVertex2f( lx, ly );
            gl.glVertex2f( lx, ly - lh );
            gl.glVertex2f( lx + lw, ly - lh );
            gl.glVertex2f( lx + lw, ly );
        }
        finally
        {
            gl.glEnd( );
        }
        gl.glColor4fv( GlimpseColor.getBlack( ), 0 );
        gl.glLineWidth( 2f );
        gl.glBegin( GL2.GL_LINE_LOOP );
        try
        {
            gl.glVertex2f( lx, ly );
            gl.glVertex2f( lx, ly - lh );
            gl.glVertex2f( lx + lw, ly - lh );
            gl.glVertex2f( lx + lw, ly );
        }
        finally
        {
            gl.glEnd( );
        }

        //draw the color items
        int xpos = lx + spacer;
        int ypos = ly - spacer;
        for ( String label : list )
        {
            Rectangle2D bounds = textRenderer.getBounds( label );
            int labelHeight = ( int ) bounds.getHeight( );
            float[] rgba = colors.get( label );
            drawLegendItem( gl, label, xpos, ypos, rgba, labelHeight );
            ypos -= ( labelHeight + spacer );
        }

        //draw the text labels
        xpos = lx + spacer + itemWidth + spacer;
        ypos = ly - spacer;
        textRenderer.beginRendering( width, height );
        GlimpseColor.setColor( textRenderer, textColor );
        try
        {
            for ( String label : list )
            {
                Rectangle2D bounds = textRenderer.getBounds( label );
                int labelHeight = ( int ) bounds.getHeight( );
                textRenderer.draw( label, xpos, ypos - labelHeight );
                ypos -= ( labelHeight + spacer );
            }
        }
        finally
        {
            textRenderer.endRendering( );
        }

    }

    private int upperLeftX( int width, int height, int lw, int lh )
    {
        switch ( placement )
        {
            case N:
            case S:
                return ( width - lw ) / 2;
            case NE:
            case E:
            case SE:
                return width - ( lw + offsetX );
            case NW:
            case SW:
            case W:
                return offsetX;
            default:
                return 0;
        }
    }

    private int upperLeftY( int width, int height, int lw, int lh )
    {
        switch ( placement )
        {
            case NW:
            case N:
            case NE:
                return height - offsetY;
            case SW:
            case S:
            case SE:
                return lh + offsetY;
            case E:
            case W:
                return ( height + lh ) / 2;
            default:
                return 0;
        }
    }

    protected abstract void drawLegendItem( GL2 gl, String label, int xpos, int ypos, float[] rgba, int height );

    public static class BlockLegendPainter extends LegendPainter
    {
        public BlockLegendPainter( LegendPlacement placement )
        {
            super( placement );
        }

        @Override
        protected void drawLegendItem( GL2 gl, String label, int xpos, int ypos, float[] rgba, int height )
        {
            gl.glColor4fv( rgba, 0 );
            gl.glBegin( GL2.GL_POLYGON );
            try
            {
                gl.glVertex2d( xpos, ypos );
                gl.glVertex2d( xpos + itemWidth, ypos );
                gl.glVertex2d( xpos + itemWidth, ypos - height );
                gl.glVertex2d( xpos, ypos - height );
            }
            finally
            {
                gl.glEnd( );
            }
        }

    }

    public static class LineLegendPainter extends LegendPainter
    {
        private Map<String, LineLegendPainterItem> items;

        public LineLegendPainter( LegendPlacement placement )
        {
            super( placement );
            items = new HashMap<String, LineLegendPainterItem>( );
        }

        @Override
        protected void drawLegendItem( GL2 gl, String label, int xpos, int ypos, float[] rgba, int height )
        {
            gl.glColor4fv( rgba, 0 );

            LineLegendPainterItem item = items.get( label );
            if ( item.doStipple )
            {
                gl.glEnable( GL2.GL_LINE_STIPPLE );
                gl.glLineStipple( item.stippleFactor, item.stipplePattern );
            }
            else
            {
                gl.glDisable( GL2.GL_LINE_STIPPLE );
            }
            gl.glLineWidth( item.lineWidth );
            gl.glBegin( GL2.GL_LINE_STRIP );
            double ymid = ypos - ( height / 2. );
            try
            {
                gl.glVertex2d( xpos, ymid );
                gl.glVertex2d( xpos + itemWidth, ymid );
            }
            finally
            {
                gl.glEnd( );
            }
        }

        /**
         * Enables stipple and sets the pattern on the legend item.
         */
        public void setLineStipple( String label, int stippleFactor, short stipplePattern )
        {
            LineLegendPainterItem item = items.get( label );
            item.stippleFactor = stippleFactor;
            item.stipplePattern = stipplePattern;
            item.doStipple = true;
        }

        /**
         * Toggles stipple on the legend item.
         */
        public void setLineStipple( String label, boolean stipple )
        {
            items.get( label ).doStipple = stipple;
        }

        public void setLineWidth( String label, float width )
        {
            items.get( label ).lineWidth = width;
        }

        @Override
        public void addItem( String label, float[] rgba )
        {
            super.addItem( label, rgba );
            items.put( label, new LineLegendPainterItem( ) );
        }

        @Override
        public void removeItem( String label )
        {
            super.removeItem( label );
            items.remove( label );
        }

        @Override
        public void clear( )
        {
            super.clear( );
            items.clear( );
        }

        public static class LineLegendPainterItem
        {
            public short stipplePattern = 0x00FF;
            public int stippleFactor = 1;
            public float lineWidth = 1;
            public boolean doStipple = false;
        }
    }

    @Override
    public synchronized void paintTo( GlimpseContext context, GlimpseBounds bounds, Axis2D axis )
    {
        if ( newFont != null )
        {
            if ( textRenderer != null )
            {
                textRenderer.dispose( );
            }
            textRenderer = new TextRenderer( newFont, antialias, false );
            newFont = null;
        }

        if ( textRenderer == null ) return;

        GL2 gl = context.getGL( ).getGL2( );

        int width = bounds.getWidth( );
        int height = bounds.getHeight( );

        gl.glMatrixMode( GL2.GL_PROJECTION );
        gl.glLoadIdentity( );
        gl.glOrtho( 0, width, 0, height, -1, 1 );
        gl.glMatrixMode( GL2.GL_MODELVIEW );
        gl.glLoadIdentity( );

        gl.glBlendFunc( GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA );
        gl.glEnable( GL2.GL_BLEND );
        gl.glEnable( GL2.GL_LINE_SMOOTH );
        gl.glEnable( GL2.GL_POINT_SMOOTH );

        if ( isVisible( ) && !list.isEmpty( ) )
        {
            displayLegend( gl, width, height );
        }
    }

    @Override
    public void setLookAndFeel( LookAndFeel laf )
    {
        // ignore the look and feel if a font has been manually set
        if ( !fontSet )
        {
            setFont( laf.getFont( AbstractLookAndFeel.TITLE_FONT ), false );
            fontSet = false;
        }
    }

    @Override
    public void dispose( GlimpseContext context )
    {
        if ( textRenderer != null )
        {
            textRenderer.dispose( );
        }
        textRenderer = null;
    }

}