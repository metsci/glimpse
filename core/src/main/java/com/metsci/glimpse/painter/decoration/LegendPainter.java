/*
 * Copyright (c) 2012, Metron, Inc.
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

import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.media.opengl.GL;
import javax.media.opengl.GLContext;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.painter.base.GlimpsePainter2D;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.font.FontUtils;
import com.sun.opengl.util.j2d.TextRenderer;

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

    //TODO add locks?

    private Font textFont = FontUtils.getDefaultBold( 15.0f );
    private float[] textColor = GlimpseColor.getBlack( );

    //The width of the color item next to the legend.
    protected int itemWidth = 30;

    //space between the items in the legend and from the edges of the window.
    private int spacer = 10;

    private TextRenderer textRenderer;

    //To keep consistent ordering, also keep a list
    private final List<String> list;
    private final Map<String, float[]> map;

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

    public LegendPainter( LegendPlacement placement )
    {
        this.placement = placement;
        this.list = new ArrayList<String>( );
        this.map = new HashMap<String, float[]>( );
        setFont( 15, false );
    }

    public void setFont( Font font )
    {
        textFont = font;
        if ( textRenderer != null )
        {
            textRenderer.dispose( );
        }
        textRenderer = new TextRenderer( textFont, true, false );
    }

    public void setFont( float size, boolean bold )
    {
        setFont( bold ? FontUtils.getDefaultBold( size ) : FontUtils.getDefaultPlain( size ) );
    }

    public void setTextColor( float[] rgba )
    {
        textColor = rgba;
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

    public void addItem( String label, float[] rgba )
    {
        if ( !map.containsKey( label ) )
        {
            list.add( label );
        }
        map.put( label, rgba );
    }

    public void removeItem( String label )
    {
        list.remove( label );
        map.remove( label );
    }

    public void clear( )
    {
        list.clear( );
        map.clear( );
    }

    public void setColor( String label, float r, float g, float b, float a )
    {
        float[] rgba = map.get( label );
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

    private void displayLegend( GL gl, int width, int height )
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
        gl.glBegin( GL.GL_POLYGON );
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
        gl.glBegin( GL.GL_LINE_LOOP );
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
            float[] rgba = map.get( label );
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

    protected abstract void drawLegendItem( GL gl, String label, int xpos, int ypos, float[] rgba, int height );

    @Override
    public void dispose( GLContext context )
    {
        if ( textRenderer != null ) textRenderer.dispose( );
        textRenderer = null;
    }

    public static class BlockLegendPainter extends LegendPainter
    {
        public BlockLegendPainter( LegendPlacement placement )
        {
            super( placement );
        }

        @Override
        protected void drawLegendItem( GL gl, String label, int xpos, int ypos, float[] rgba, int height )
        {
            gl.glColor4fv( rgba, 0 );
            gl.glBegin( GL.GL_POLYGON );
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

        //TODO line widths/stipples per label?
        private Set<String> stippleLabels;

        private int stippleFactor = 1;
        private short stipplePattern = ( short ) 0x00FF;

        public LineLegendPainter( LegendPlacement placement )
        {
            super( placement );
            this.stippleLabels = new HashSet<String>( );
        }

        @Override
        protected void drawLegendItem( GL gl, String label, int xpos, int ypos, float[] rgba, int height )
        {
            gl.glColor4fv( rgba, 0 );
            if ( stippleLabels.contains( label ) )
            {
                gl.glEnable( GL.GL_LINE_STIPPLE );
                gl.glLineStipple( stippleFactor, stipplePattern );
            }
            else
            {
                gl.glDisable( GL.GL_LINE_STIPPLE );
            }
            gl.glLineWidth( 1f );
            gl.glBegin( GL.GL_LINE_STRIP );
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

        public void setLineStipplePattern( int stippleFactor, short stipplePattern )
        {
            this.stippleFactor = stippleFactor;
            this.stipplePattern = stipplePattern;
        }

        public void setLineStipple( String label, boolean stipple )
        {
            if ( stipple )
            {
                stippleLabels.add( label );
            }
            else
            {
                stippleLabels.remove( label );
            }
        }

        public void addItem( String label, float r, float g, float b, float a, boolean stipple )
        {
            addItem( label, r, g, b, a );
            setLineStipple( label, stipple );
        }

        public void addItem( String label, float[] rgba, boolean stipple )
        {
            addItem( label, rgba );
            setLineStipple( label, stipple );
        }

        @Override
        public void removeItem( String label )
        {
            super.removeItem( label );
            stippleLabels.remove( label );
        }

        @Override
        public void clear( )
        {
            super.clear( );
            stippleLabels.clear( );
        }

    }

    @Override
    public void paintTo( GlimpseContext context, GlimpseBounds bounds, Axis2D axis )
    {
        GL gl = context.getGL( );

        int width = bounds.getWidth( );
        int height = bounds.getHeight( );

        gl.glMatrixMode( GL.GL_PROJECTION );
        gl.glLoadIdentity( );
        gl.glOrtho( 0, width, 0, height, -1, 1 );
        gl.glMatrixMode( GL.GL_MODELVIEW );
        gl.glLoadIdentity( );

        gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA );
        gl.glEnable( GL.GL_BLEND );
        gl.glEnable( GL.GL_LINE_SMOOTH );
        gl.glEnable( GL.GL_POINT_SMOOTH );

        if ( isVisible( ) && !list.isEmpty( ) )
        {
            displayLegend( gl, width, height );
        }
    }
}
