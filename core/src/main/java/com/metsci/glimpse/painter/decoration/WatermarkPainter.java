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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.painter.base.GlimpsePainterImpl;
import com.metsci.glimpse.util.io.StreamOpener;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureData;

import static com.metsci.glimpse.util.GeneralUtils.*;
import static com.metsci.glimpse.util.logging.LoggerUtils.*;
import static com.sun.opengl.util.texture.TextureIO.*;
import static java.lang.Math.*;
import static javax.media.opengl.GL.*;

public class WatermarkPainter extends GlimpsePainterImpl
{
    private static final Logger logger = getLogger( WatermarkPainter.class );

    protected static final double maxWidthPixels = 350;
    protected static final double maxHeightPixels = 350;
    protected static final double maxAreaFraction = 0.04;
    protected static final double maxWidthFraction = 0.28;
    protected static final double maxHeightFraction = 0.28;
    protected static final double maxPaddingPixels = 10;


    protected final Supplier<BufferedImage> imageSupplier;
    protected Texture texture;
    protected boolean initialized;


    public WatermarkPainter( BufferedImage image )
    {
        this( Suppliers.ofInstance( image ) );
    }

    public WatermarkPainter( StreamOpener imageOpener, String imageLocation )
    {
        this( newImageLoader( imageOpener, imageLocation ) );
    }

    public WatermarkPainter( Supplier<BufferedImage> imageSupplier )
    {
        this.imageSupplier = imageSupplier;
        this.texture = null;
        this.initialized = false;
    }

    public static Supplier<BufferedImage> newImageLoader( final StreamOpener opener, final String location )
    {
        return new Supplier<BufferedImage>( )
        {
            public BufferedImage get( )
            {
                try
                {
                    InputStream stream = null;
                    try
                    {
                        stream = opener.openForRead( location );
                        return ImageIO.read( stream );
                    }
                    finally
                    {
                        if ( stream != null ) stream.close( );
                    }
                }
                catch ( IOException e )
                {
                    throw new RuntimeException( e );
                }
            }
        };
    }

    protected void initIfNecessary( )
    {
        if ( initialized ) return;

        try
        {
            BufferedImage image = imageSupplier.get( );
            texture = newTexture( new TextureData( 0, 0, false, image ) );
        }
        catch ( Exception e )
        {
            logWarning( logger, "Failed to create watermark image texture", e );
        }

        initialized = true;
    }

    // width, height, padding
    protected double[] computeQuadGeometry( double wImage, double hImage, double wBounds, double hBounds )
    {
        double maxArea = maxAreaFraction * wBounds*hBounds;

        double w = maxWidthPixels;
        w = min( w, maxWidthFraction*wBounds );
        w = min( w, sqrt( maxArea*wImage/hImage ) );

        double h = maxHeightPixels;
        h = min( h, maxHeightFraction*hBounds );
        h = min( h, sqrt( maxArea*hImage/wImage ) );

        w = min( w, h * wImage/hImage );
        h = w * hImage/wImage;

        double p = 0;
        p = max( p, w / maxWidthPixels );
        p = max( p, h / maxHeightPixels );
        //p = max( p, w*h / maxArea );
        double padding = p * maxPaddingPixels;

        return doubles( w, h, padding );
    }

    protected void paintTo( GlimpseContext context, GlimpseBounds bounds )
    {
        initIfNecessary( );
        if ( texture == null ) return;

        GL gl = context.getGL( );

        gl.glMatrixMode( GL_PROJECTION );
        gl.glLoadIdentity( );
        gl.glOrtho( 0, bounds.getWidth( ), 0, bounds.getHeight( ), -1, 1 );

        gl.glMatrixMode( GL_TEXTURE );
        gl.glActiveTexture( GL.GL_TEXTURE0 );
        gl.glLoadIdentity( );

        gl.glMatrixMode( GL_MODELVIEW );
        gl.glLoadIdentity( );

        texture.setTexParameteri( GL_TEXTURE_MAG_FILTER, GL_NEAREST );
        texture.setTexParameteri( GL_TEXTURE_MIN_FILTER, GL_NEAREST );
        texture.enable( );
        texture.bind( );


        // See the "Alpha premultiplication" section in Texture's class comment
        gl.glEnable( GL_BLEND );
        gl.glBlendFunc( GL_ONE, GL_ONE_MINUS_SRC_ALPHA );
        gl.glTexEnvi( GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE );


        gl.glBegin( GL_QUADS );
        try
        {
            double[] quadGeometry = computeQuadGeometry( texture.getWidth( ), texture.getHeight( ), bounds.getWidth( ), bounds.getHeight( ) );
            double wQuad = quadGeometry[0];
            double hQuad = quadGeometry[1];
            double padding = quadGeometry[2];

            float xRight = (float) ( bounds.getWidth( ) - padding );
            float xLeft = (float) ( xRight - wQuad );
            float yBottom = (float) ( padding );
            float yTop = (float) ( yBottom + hQuad );

            gl.glTexCoord2f( 0, 1 );
            gl.glVertex2f( xLeft, yBottom );

            gl.glTexCoord2f( 1, 1 );
            gl.glVertex2f( xRight, yBottom );

            gl.glTexCoord2f( 1, 0 );
            gl.glVertex2f( xRight, yTop );

            gl.glTexCoord2f( 0, 0 );
            gl.glVertex2f( xLeft, yTop);
        }
        finally
        {
            gl.glEnd( );
            texture.disable( );
        }
    }

}