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
package com.metsci.glimpse.plot.timeline.group;

import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;

import java.awt.geom.Rectangle2D;

import javax.media.opengl.GL2;

import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.painter.base.GlimpsePainterImpl;
import com.metsci.glimpse.painter.info.SimpleTextPainter;
import com.metsci.glimpse.painter.info.SimpleTextPainter.HorizontalPosition;
import com.metsci.glimpse.painter.info.SimpleTextPainter.VerticalPosition;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.font.FontUtils;
import com.metsci.glimpse.support.settings.AbstractLookAndFeel;
import com.metsci.glimpse.support.settings.LookAndFeel;

public class GroupLabelPainter extends GlimpsePainterImpl
{
    protected int buttonSize = 8;
    protected int padding = 5;

    protected float[] lineColor = GlimpseColor.getBlack( );

    protected SimpleTextPainter textDelegate;

    protected boolean isExpanded = true;

    protected boolean showDivider = true;
    protected boolean showArrow = true;

    public GroupLabelPainter( String name )
    {
        this.textDelegate = new SimpleTextPainter( );
        this.textDelegate.setHorizontalPosition( HorizontalPosition.Left );
        this.textDelegate.setVerticalPosition( VerticalPosition.Top );
        this.textDelegate.setHorizontalLabels( true );
        this.textDelegate.setHorizontalPadding( buttonSize + padding * 2 );
        this.textDelegate.setVerticalPadding( 0 );
        this.textDelegate.setText( name );
        this.textDelegate.setFont( FontUtils.getDefaultPlain( 14 ), true );
    }

    public SimpleTextPainter getTextPainter( )
    {
        return this.textDelegate;
    }

    public void setExpanded( boolean isExpanded )
    {
        this.isExpanded = isExpanded;
    }

    public void setText( String text )
    {
        this.textDelegate.setText( text );
    }

    public void setShowArrow( boolean show )
    {
        this.showArrow = show;
    }

    public boolean isShowArrow( )
    {
        return this.showArrow;
    }

    public void setShowDivider( boolean show )
    {
        this.showDivider = show;
    }

    public boolean isShowDivider( )
    {
        return this.showDivider;
    }

    public void setDividerColor( float[] color )
    {
        this.lineColor = color;
    }

    public float[] getDividerColor( )
    {
        return lineColor;
    }

    public void setArrowSpacing( int size )
    {
        this.padding = size;
    }

    public int getArrowSpacing( )
    {
        return this.padding;
    }

    public void setArrowSize( int size )
    {
        this.buttonSize = size;
    }

    public int getArrowSize( )
    {
        return this.buttonSize;
    }

    @Override
    protected void paintTo( GlimpseContext context, GlimpseBounds bounds )
    {
        this.textDelegate.paintTo( context );

        int width = bounds.getWidth( );
        int height = bounds.getHeight( );

        GL2 gl = context.getGL( ).getGL2( );

        gl.glMatrixMode( GL_PROJECTION );
        gl.glLoadIdentity( );
        gl.glOrtho( 0, bounds.getWidth( ), 0, bounds.getHeight( ), -1, 1 );

        gl.glMatrixMode( GL_MODELVIEW );
        gl.glLoadIdentity( );

        // Paint Line
        if ( showDivider )
        {
            Rectangle2D textBounds = this.textDelegate.getTextBounds( );
            float startY = ( float ) height / 2.0f;
            float startX = ( float ) ( padding + this.textDelegate.getHorizontalPadding( ) + textBounds.getWidth( ) + ( textBounds.getMinX( ) ) - 1 );

            gl.glLineWidth( 1.0f );
            GlimpseColor.glColor( gl, lineColor );

            gl.glBegin( GL2.GL_LINES );
            try
            {
                gl.glVertex2f( startX, startY );
                gl.glVertex2f( width, startY );
            }
            finally
            {
                gl.glEnd( );
            }
        }

        if ( showArrow )
        {
            gl.glLineWidth( 1.0f );
            GlimpseColor.glColor( gl, lineColor );

            float halfSize = buttonSize / 2.0f;
            float centerX = halfSize + padding;
            float centerY = height / 2.0f;

            // Paint Expand/Collapse Button
            gl.glBegin( GL2.GL_POLYGON );
            try
            {
                if ( isExpanded )
                {
                    gl.glVertex2f( centerX - halfSize, centerY + halfSize );
                    gl.glVertex2f( centerX + halfSize, centerY + halfSize );
                    gl.glVertex2f( centerX, centerY - halfSize );
                }
                else
                {
                    gl.glVertex2f( centerX - halfSize, centerY - halfSize );
                    gl.glVertex2f( centerX - halfSize, centerY + halfSize );
                    gl.glVertex2f( centerX + halfSize, centerY );
                }
            }
            finally
            {
                gl.glEnd( );
            }
        }
    }

    @Override
    public void setLookAndFeel( LookAndFeel laf )
    {
        super.setLookAndFeel( laf );

        if ( laf != null )
        {
            this.textDelegate.setLookAndFeel( laf );
            this.lineColor = laf.getColor( AbstractLookAndFeel.BORDER_COLOR );
        }
    }
}