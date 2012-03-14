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
package com.metsci.glimpse.examples.icon;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener2D;
import com.metsci.glimpse.axis.painter.NumericXYAxisPainter;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.base.GlimpseDataPainter2D;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.support.atlas.TextureAtlas;
import com.metsci.glimpse.support.atlas.painter.IconPainter;
import com.metsci.glimpse.support.atlas.support.ImageDrawer;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.util.io.StreamOpener;

/**
 * Demonstrates basic usage of the Glimpse TextureAtlas utility class. TextureAtlas
 * allows icons and images to be defined once using Swing Graphics2D drawing (or
 * via a BufferedImage). The image or icon is then packed onto an OpenGL texture
 * and can be efficiently painted onto the screen.
 * 
 * TextureAtlas provides basic methods for drawing a single icon from the atlas.
 * However, when large numbers of icons must be drawn, using
 * {@link com.metsci.glimpse.support.atlas.painter.IconPainter} is often much
 * more efficient.
 * 
 * @author ulman
 */
public class TextureAtlasTestExample implements GlimpseLayoutProvider
{
    public static void main( String[] args ) throws Exception
    {
        Example.showWithSwing( new TextureAtlasTestExample( ) );
    }

    @Override
    public GlimpseLayout getLayout( ) throws Exception
    {
        TextureAtlas atlas = new TextureAtlas( 512, 512 );
        loadTextureAtlas( atlas );

        GlimpseLayout layout = new GlimpseLayout( );

        GlimpseLayout child1 = new GlimpseAxisLayout2D( new Axis2D( ) );
        child1.setLayoutData( "cell 1 1, grow, push" );
        child1.addGlimpseMouseAllListener( new AxisMouseListener2D( ) );

        GlimpseLayout child2 = new GlimpseAxisLayout2D( new Axis2D( ) );
        child1.setLayoutData( "cell 1 2, grow, push" );
        child2.addGlimpseMouseAllListener( new AxisMouseListener2D( ) );

        TextureAtlasAllPainter painter1 = new TextureAtlasAllPainter( atlas );
        painter1.init( );

        child1.addPainter( new BackgroundPainter( ).setColor( GlimpseColor.getWhite( ) ) );
        child1.addPainter( painter1 );

        TextureAtlasTestPainter painter2 = new TextureAtlasTestPainter( atlas );
        painter2.init( );

        child2.addPainter( new BackgroundPainter( ).setColor( GlimpseColor.getGray( ) ) );
        child2.addPainter( new NumericXYAxisPainter( ) );
        child2.addPainter( painter2 );

        layout.addLayout( child1 );
        layout.addLayout( child2 );

        return layout;
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
        
        atlas.loadImage( "image9", 100, 100, 10, 10, new ImageDrawer( )
        {
            @Override
            public void drawImage( Graphics2D g, int width, int height )
            {
                g.setColor( Color.black );
                
                for ( int x = 0 ; x < width ; x++ )
                {
                    for ( int y = 0 ; y < height ; y++ )
                    {
                        if ( ( x % 2 == 0 && y % 2 == 0 ) || ( x % 2 != 0 && y % 2 != 0 ) )
                            g.fillRect( x,y,1,1 );
                    }
                }
                
                g.setColor( Color.red );
                g.drawRect( 0, 0, width-1, height-1 );
            }
        } );

        atlas.loadImage( "glimpse", ImageIO.read( StreamOpener.fileThenResource.openForRead( "images/GlimpseLogo.png" ) ) );
    }

    public class TextureAtlasAllPainter extends GlimpseDataPainter2D
    {
        protected TextureAtlas atlas;
        protected int i = 10;

        public TextureAtlasAllPainter( TextureAtlas atlas )
        {
            this.atlas = atlas;
        }

        public void init( ) throws IOException
        {
            ( new Thread( )
            {
                public void run( )
                {
                    while ( true )
                    {
                        int x = ( int ) ( Math.random( ) * 100 + 10 );
                        int y = ( int ) ( Math.random( ) * 100 + 10 );

                        atlas.loadImage( "image" + i, x, y, new ImageDrawer( )
                        {
                            @Override
                            public void drawImage( Graphics2D g, int width, int height )
                            {
                                g.setColor( GlimpseColor.toColorAwt( GlimpseColor.fromColorRgb( ( float ) Math.random( ), ( float ) Math.random( ), ( float ) Math.random( ) ) ) );
                                g.fillRect( 0, 0, width, height );
                            }
                        } );

                        i = i + 1;

                        try
                        {
                            Thread.sleep( 50 );
                        }
                        catch ( InterruptedException e )
                        {
                        }
                    }
                }
            } ).start( );
        }

        protected void drawAll( GL gl, Axis2D axis )
        {
            drawAll( gl, 0, 10, 0, 10 );
        }

        protected void drawAll( GL gl, double minX, double maxX, double minY, double maxY )
        {
            gl.glBegin( GL.GL_QUADS );
            try
            {
                gl.glTexCoord2f( 0, 1 );
                gl.glVertex2d( minX, minY );
                gl.glTexCoord2f( 1, 1 );
                gl.glVertex2d( maxX, minY );
                gl.glTexCoord2f( 1, 0 );
                gl.glVertex2d( maxX, maxY );
                gl.glTexCoord2f( 0, 0 );
                gl.glVertex2d( minX, maxY );
            }
            finally
            {
                gl.glEnd( );
            }
        }

        @Override
        public void paintTo( GL gl, GlimpseBounds bounds, Axis2D axis )
        {
            this.atlas.beginRendering( );
            try
            {
                drawAll( gl, axis );
            }
            finally
            {
                this.atlas.endRendering( );
            }
        }
    }

    public class TextureAtlasTestPainter extends GlimpseDataPainter2D
    {
        protected TextureAtlas atlas;
        protected IconPainter iconPainter;

        public TextureAtlasTestPainter( TextureAtlas atlas )
        {
            this.atlas = atlas;
            this.iconPainter = new IconPainter( atlas );
        }

        public void init( ) throws IOException
        {
            this.iconPainter.addIcon( "group1", "image4", 0, 5 );
        }

        @Override
        public void paintTo( GL gl, GlimpseBounds bounds, Axis2D axis )
        {
            this.atlas.beginRendering( );
            try
            {
                this.atlas.drawImage( gl, "image5", axis, 0, 0 );
                this.atlas.drawImage( gl, "glimpse", axis, 5, 2 );
            }
            finally
            {
                this.atlas.endRendering( );
            }

            this.iconPainter.paintTo( gl, bounds, axis );
        }
    }
}
