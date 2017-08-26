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
package com.metsci.glimpse.layers.misc;

import static com.google.common.base.Objects.equal;
import static com.jogamp.common.nio.Buffers.SIZEOF_FLOAT;
import static com.jogamp.opengl.util.texture.awt.AWTTextureIO.newTextureData;
import static com.metsci.glimpse.gl.util.GLUtils.disableBlending;
import static com.metsci.glimpse.gl.util.GLUtils.enableStandardBlending;
import static com.metsci.glimpse.layers.misc.UiUtils.paintComponentToImage;
import static com.metsci.glimpse.support.color.GlimpseColor.toColorAwt;
import static com.metsci.glimpse.support.settings.AbstractLookAndFeel.TOOLTIP_BACKGROUND_COLOR;
import static com.metsci.glimpse.support.settings.AbstractLookAndFeel.TOOLTIP_TEXT_COLOR;
import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;
import static java.awt.Font.MONOSPACED;
import static java.awt.Font.PLAIN;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static javax.swing.BorderFactory.createEmptyBorder;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES3;
import javax.swing.JLabel;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;
import com.jogamp.opengl.util.texture.TextureData;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.GLEditableBuffer;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.support.settings.LookAndFeel;

public abstract class CursorLabelPainter extends GlimpsePainterBase
{

    protected int xOffset_PX;
    protected int yOffset_PX;
    protected boolean offsetBySelectionSize;
    protected boolean clampToScreenEdges;

    protected boolean dirty;
    protected String content;
    protected final JLabel label;
    protected final Texture texture;
    protected final GLEditableBuffer texCoords;
    protected final GLEditableBuffer vertCoords;
    protected final CursorLabelProgram prog;


    public CursorLabelPainter( )
    {
        this.xOffset_PX = 6;
        this.yOffset_PX = 6;
        this.offsetBySelectionSize = true;
        this.clampToScreenEdges = true;

        this.dirty = true;
        this.content = null;
        this.label = new JLabel( )
        {
            @Override
            protected void paintComponent( Graphics g )
            {
                g.setColor( getBackground( ) );
                g.fillRect( 0, 0, getWidth( ), getHeight( ) );
                super.paintComponent( g );
            }
        };
        this.texture = new Texture( GL_TEXTURE_2D );
        this.texCoords = new GLEditableBuffer( GL_STATIC_DRAW, 8 * SIZEOF_FLOAT );
        this.vertCoords = new GLEditableBuffer( GL_STATIC_DRAW, 8 * SIZEOF_FLOAT );
        this.prog = new CursorLabelProgram( );

        // Default to something visible, in case setLaf fails for some reason
        this.label.setFont( new Font( MONOSPACED, PLAIN, 12 ) );
        this.label.setBackground( BLACK );
        this.label.setForeground( WHITE );

        this.label.setBorder( createEmptyBorder( 2, 5, 3, 5 ) );
    }

    protected abstract String getCursorText( Axis2D axis );

    @Override
    public void doDispose( GlimpseContext context )
    {
        GL gl = context.getGL( );
        this.texture.destroy( gl );
        this.texCoords.dispose( gl );
        this.vertCoords.dispose( gl );
    }

    @Override
    public void setLookAndFeel( LookAndFeel laf )
    {
        // XXX: Add a TOOLTIP_FONT entry to lafs -- typical AXIS_FONT is too small
        //this.label.setFont( laf.getFont( AXIS_FONT ) );
        this.label.setBackground( toColorAwt( laf.getColor( TOOLTIP_BACKGROUND_COLOR ) ) );
        this.label.setForeground( toColorAwt( laf.getColor( TOOLTIP_TEXT_COLOR ) ) );

        this.dirty = true;
    }

    public void setOffsetBySelectionSize( boolean offsetBySelectionSize )
    {
        this.offsetBySelectionSize = offsetBySelectionSize;
    }

    public void setClampToScreenEdges( boolean clampToScreenEdges )
    {
        this.clampToScreenEdges = clampToScreenEdges;
    }

    @Override
    protected void doPaintTo( GlimpseContext context )
    {
        GlimpseBounds bounds = getBounds( context );
        Axis2D axis = requireAxis2D( context );
        GL2ES3 gl = context.getGL( ).getGL2ES3( );


        // Update texture
        //

        String newContent = this.getCursorText( axis );
        if ( this.dirty || !equal( newContent, this.content ) )
        {
            this.label.setText( newContent );
            BufferedImage image = paintComponentToImage( this.label );
            TextureData textureData = newTextureData( gl.getGLProfile( ), image, false );
            this.texture.updateImage( gl, textureData );

            TextureCoords textureCoords = this.texture.getImageTexCoords( );
            float texTop = textureCoords.top( );
            float texLeft = textureCoords.left( );
            float texRight = textureCoords.right( );
            float texBottom = textureCoords.bottom( );

            this.texCoords.clear( );
            this.texCoords.grow2f( texLeft, texTop );
            this.texCoords.grow2f( texLeft, texBottom );
            this.texCoords.grow2f( texRight, texTop );
            this.texCoords.grow2f( texRight, texBottom );

            this.content = newContent;
            this.dirty = false;
        }


        // Compute vertex coords
        //

        if ( axis == null || axis.getAxisX( ) == null || axis.getAxisY( ) == null || !bounds.isValid( ) )
        {
            return;
        }

        double xSelection = axis.getAxisX( ).getSelectionCenter( );
        double ySelection = axis.getAxisY( ).getSelectionCenter( );
        double wSelection = axis.getAxisX( ).getSelectionSize( );

        double x = xSelection + ( this.offsetBySelectionSize ? 0.5*wSelection : 0 );
        double y = ySelection;

        int x_PX = axis.getAxisX( ).valueToScreenPixel( x ) + this.xOffset_PX;
        int y_PX = axis.getAxisY( ).valueToScreenPixel( y ) - this.yOffset_PX;
        int w_PX = this.texture.getImageWidth( );
        int h_PX = this.texture.getImageHeight( );

        if ( this.clampToScreenEdges )
        {
            x_PX = max( 0, min( bounds.getWidth( ) - w_PX, x_PX ) );
            y_PX = min( bounds.getHeight( ), max( h_PX, y_PX ) );
        }

        float vertTop = y_PX;
        float vertLeft = x_PX;
        float vertRight = x_PX + w_PX;
        float vertBottom = y_PX - h_PX;

        this.vertCoords.clear( );
        this.vertCoords.grow2f( vertLeft, vertTop );
        this.vertCoords.grow2f( vertLeft, vertBottom );
        this.vertCoords.grow2f( vertRight, vertTop );
        this.vertCoords.grow2f( vertRight, vertBottom );


        // Render
        //

        enableStandardBlending( gl );
        this.prog.begin( gl );
        try
        {
            this.prog.setViewport( gl, bounds );
            this.prog.draw( gl, this.texture, this.texCoords, this.vertCoords );
        }
        finally
        {
            this.prog.end( gl );
            disableBlending( gl );
        }
    }

}
