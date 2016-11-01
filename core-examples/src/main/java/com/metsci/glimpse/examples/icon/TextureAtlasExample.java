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
package com.metsci.glimpse.examples.icon;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener2D;
import com.metsci.glimpse.axis.painter.NumericXYAxisPainter;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.gl.util.GLUtils;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.support.atlas.TextureAtlas;
import com.metsci.glimpse.support.atlas.support.ImageDrawer;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.util.io.StreamOpener;

public class TextureAtlasExample implements GlimpseLayoutProvider
{
    public static void main( String[] args ) throws Exception
    {
        Example.showWithSwing( new TextureAtlasExample( ) );
    }

    @Override
    public GlimpseLayout getLayout( ) throws Exception
    {
        // create a texture atlas
        TextureAtlas atlas = new TextureAtlas( 512, 512 );

        // load an images into the atlas
        loadTextureAtlas( atlas );

        // create a simple GlimpseLayout
        GlimpseLayout layout = new GlimpseAxisLayout2D( new Axis2D( ) );

        // add an AxisMouseListener2D so we can pan/zoom around the layout with the mouse
        layout.addGlimpseMouseAllListener( new AxisMouseListener2D( ) );

        // add a simple painter which uses the TexureAtlas to draw some icons
        layout.addPainter( new BackgroundPainter( ).setColor( GlimpseColor.getWhite( ) ) );
        layout.addPainter( new NumericXYAxisPainter( ) );
        layout.addPainter( new SimpleIconPainter( atlas ) );

        return layout;
    }

    public class SimpleIconPainter extends GlimpsePainterBase
    {
        protected TextureAtlas atlas;

        public SimpleIconPainter( TextureAtlas atlas )
        {
            this.atlas = atlas;
        }

        @Override
        public void doPaintTo( GlimpseContext context )
        {
            Axis2D axis = requireAxis2D( context );
            GL gl = context.getGL( );

            GLUtils.enableStandardBlending( gl );
            this.atlas.beginRenderingAxisOrtho( context, axis );
            try
            {
                this.atlas.drawImage( context, "image1", axis, 0, 0, 0.5, 1.0 );
                this.atlas.drawImage( context, "glimpse", axis, 0, 0, 0.5, 0.5 );
                this.atlas.drawImage( context, "glimpse", axis, 5, 4, 0.5, 0.5 );
            }
            finally
            {
                this.atlas.endRendering( context );
                GLUtils.disableBlending( gl );
            }

        }

        @Override
        protected void doDispose( GlimpseContext context )
        {
            this.atlas.dispose( );
        }
    }

    public static void loadTextureAtlas( TextureAtlas atlas ) throws IOException
    {
        atlas.loadImage( "image1", 30, 30, new ImageDrawer( )
        {
            @Override
            public void drawImage( Graphics2D g, int width, int height )
            {
                g.setColor( Color.red );
                g.fillRect( 0, 0, width, height );
            }
        } );

        atlas.loadImage( "image2", 100, 100, new ImageDrawer( )
        {
            @Override
            public void drawImage( Graphics2D g, int width, int height )
            {
                g.setColor( Color.blue );
                g.fillRect( 0, 0, width, height );
            }
        } );

        atlas.loadImage( "image3", 100, 100, new ImageDrawer( )
        {
            @Override
            public void drawImage( Graphics2D g, int width, int height )
            {
                g.setColor( Color.yellow );
                g.drawArc( 0, 0, width, height, 0, 360 );
            }
        } );

        atlas.loadImage( "image4", 80, 80, new ImageDrawer( )
        {
            @Override
            public void drawImage( Graphics2D g, int width, int height )
            {
                g.setColor( Color.CYAN );
                g.fillArc( 0, 0, width, height, 0, 360 );
            }
        } );

        atlas.loadImage( "image5", 70, 70, new ImageDrawer( )
        {
            @Override
            public void drawImage( Graphics2D g, int width, int height )
            {
                g.setColor( Color.black );
                g.drawLine( width, 0, 0, height );
                g.drawLine( 0, 0, width, height );
            }
        } );

        atlas.loadImage( "image6", 10, 10, new ImageDrawer( )
        {
            @Override
            public void drawImage( Graphics2D g, int width, int height )
            {
                g.setColor( Color.black );
                g.drawLine( width, 0, 0, height );
                g.drawLine( 0, 0, width, height );
            }
        } );

        atlas.loadImage( "image7", 100, 100, new ImageDrawer( )
        {
            @Override
            public void drawImage( Graphics2D g, int width, int height )
            {
                g.setColor( Color.red );
                g.fillRect( 0, 0, width / 2, height / 2 );
                g.fillRect( width / 2, height / 2, width / 2, height / 2 );

                g.setColor( Color.blue );
                g.fillRect( 0, height / 2, width / 2, height / 2 );
                g.fillRect( width / 2, 0, width / 2, height / 2 );
            }
        } );

        atlas.loadImage( "image8", 200, 100, new ImageDrawer( )
        {
            @Override
            public void drawImage( Graphics2D g, int width, int height )
            {
                g.setColor( Color.green );
                g.fillRect( 0, 0, width / 2, height / 2 );
                g.fillRect( width / 2, height / 2, width / 2, height / 2 );

                g.setColor( Color.yellow );
                g.fillRect( 0, height / 2, width / 2, height / 2 );
                g.fillRect( width / 2, 0, width / 2, height / 2 );
            }
        } );

        atlas.loadImage( "image9", 100, 100, 0, 0, new ImageDrawer( )
        {
            @Override
            public void drawImage( Graphics2D g, int width, int height )
            {
                g.setColor( Color.black );

                for ( int x = 0; x < width; x++ )
                {
                    for ( int y = 0; y < height; y++ )
                    {
                        if ( ( x % 2 == 0 && y % 2 == 0 ) || ( x % 2 != 0 && y % 2 != 0 ) ) g.fillRect( x, y, 1, 1 );
                    }
                }

                g.setColor( Color.red );
                g.drawRect( 0, 0, width - 1, height - 1 );
            }
        } );

        atlas.loadImage( "glimpse", ImageIO.read( StreamOpener.fileThenResource.openForRead( "images/GlimpseLogo.png" ) ) );
    }
}