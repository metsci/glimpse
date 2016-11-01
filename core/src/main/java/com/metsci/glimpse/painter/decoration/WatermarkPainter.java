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

import static com.jogamp.opengl.util.texture.TextureIO.newTexture;
import static com.metsci.glimpse.painter.info.SimpleTextPainter.HorizontalPosition.Center;
import static com.metsci.glimpse.painter.info.SimpleTextPainter.HorizontalPosition.Left;
import static com.metsci.glimpse.painter.info.SimpleTextPainter.HorizontalPosition.Right;
import static com.metsci.glimpse.painter.info.SimpleTextPainter.VerticalPosition.Bottom;
import static com.metsci.glimpse.painter.info.SimpleTextPainter.VerticalPosition.Top;
import static com.metsci.glimpse.util.GeneralUtils.doubles;
import static com.metsci.glimpse.util.logging.LoggerUtils.getLogger;
import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.GL3;
import javax.media.opengl.GLProfile;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.awt.AWTTextureData;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.GLEditableBuffer;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.painter.info.SimpleTextPainter.HorizontalPosition;
import com.metsci.glimpse.painter.info.SimpleTextPainter.VerticalPosition;
import com.metsci.glimpse.support.shader.triangle.ColorTexture2DProgram;
import com.metsci.glimpse.util.io.StreamOpener;

public class WatermarkPainter extends GlimpsePainterBase
{
    private static final Logger logger = getLogger( WatermarkPainter.class );

    public static class WatermarkConfig
    {
        public final double maxWidthPixels;
        public final double maxHeightPixels;
        public final double maxAreaFraction;
        public final double maxWidthFraction;
        public final double maxHeightFraction;
        public final double maxPaddingPixels;
        public final VerticalPosition verticalPos;
        public final HorizontalPosition horizontalPos;

        public WatermarkConfig( double maxWidthPixels, double maxHeightPixels, double maxAreaFraction, double maxWidthFraction, double maxHeightFraction, double maxPaddingPixels, VerticalPosition verticalPos, HorizontalPosition horizontalPos )
        {
            this.maxWidthPixels = maxWidthPixels;
            this.maxHeightPixels = maxHeightPixels;
            this.maxAreaFraction = maxAreaFraction;
            this.maxWidthFraction = maxWidthFraction;
            this.maxHeightFraction = maxHeightFraction;
            this.maxPaddingPixels = maxPaddingPixels;
            this.verticalPos = verticalPos;
            this.horizontalPos = horizontalPos;
        }

        public WatermarkConfig withMaxWidthPixels( double maxWidthPixels )
        {
            return new WatermarkConfig( maxWidthPixels, this.maxHeightPixels, this.maxAreaFraction, this.maxWidthFraction, this.maxHeightFraction, this.maxPaddingPixels, this.verticalPos, this.horizontalPos );
        }

        public WatermarkConfig withMaxHeightPixels( double maxHeightPixels )
        {
            return new WatermarkConfig( this.maxWidthPixels, maxHeightPixels, this.maxAreaFraction, this.maxWidthFraction, this.maxHeightFraction, this.maxPaddingPixels, this.verticalPos, this.horizontalPos );
        }

        public WatermarkConfig withMaxAreaFraction( double maxAreaFraction )
        {
            return new WatermarkConfig( this.maxWidthPixels, this.maxHeightPixels, maxAreaFraction, this.maxWidthFraction, this.maxHeightFraction, this.maxPaddingPixels, this.verticalPos, this.horizontalPos );
        }

        public WatermarkConfig withMaxWidthFraction( double maxWidthFraction )
        {
            return new WatermarkConfig( this.maxWidthPixels, this.maxHeightPixels, this.maxAreaFraction, maxWidthFraction, this.maxHeightFraction, this.maxPaddingPixels, this.verticalPos, this.horizontalPos );
        }

        public WatermarkConfig withMaxHeightFraction( double maxHeightFraction )
        {
            return new WatermarkConfig( this.maxWidthPixels, this.maxHeightPixels, this.maxAreaFraction, this.maxWidthFraction, maxHeightFraction, this.maxPaddingPixels, this.verticalPos, this.horizontalPos );
        }

        public WatermarkConfig withMaxPaddingPixels( double maxPaddingPixels )
        {
            return new WatermarkConfig( this.maxWidthPixels, this.maxHeightPixels, this.maxAreaFraction, this.maxWidthFraction, this.maxHeightFraction, maxPaddingPixels, this.verticalPos, this.horizontalPos );
        }

        public WatermarkConfig withVerticalPos( VerticalPosition verticalPos )
        {
            return new WatermarkConfig( this.maxWidthPixels, this.maxHeightPixels, this.maxAreaFraction, this.maxWidthFraction, this.maxHeightFraction, this.maxPaddingPixels, verticalPos, this.horizontalPos );
        }

        public WatermarkConfig withHorizontalPos( HorizontalPosition horizontalPos )
        {
            return new WatermarkConfig( this.maxWidthPixels, this.maxHeightPixels, this.maxAreaFraction, this.maxWidthFraction, this.maxHeightFraction, this.maxPaddingPixels, this.verticalPos, horizontalPos );
        }

        public WatermarkConfig withPos( VerticalPosition verticalPos, HorizontalPosition horizontalPos )
        {
            return new WatermarkConfig( this.maxWidthPixels, this.maxHeightPixels, this.maxAreaFraction, this.maxWidthFraction, this.maxHeightFraction, this.maxPaddingPixels, verticalPos, horizontalPos );
        }
    }

    public static final WatermarkConfig defaultConfig = new WatermarkConfig( 350, 350, 0.04, 0.28, 0.28, 10, Bottom, Center );

    public static final WatermarkConfig bottomRight = defaultConfig.withPos( Bottom, Right );
    public static final WatermarkConfig bottomLeft = defaultConfig.withPos( Bottom, Left );
    public static final WatermarkConfig topRight = defaultConfig.withPos( Top, Right );
    public static final WatermarkConfig topLeft = defaultConfig.withPos( Top, Left );

    protected final Supplier<BufferedImage> imageSupplier;
    protected final WatermarkConfig config;
    protected Texture texture;
    protected boolean initialized;

    protected ColorTexture2DProgram prog;
    protected GLEditableBuffer inXy;
    protected GLEditableBuffer inS;

    public WatermarkPainter( BufferedImage image )
    {
        this( image, defaultConfig );
    }

    public WatermarkPainter( BufferedImage image, WatermarkConfig config )
    {
        this( Suppliers.ofInstance( image ), config );
    }

    public WatermarkPainter( StreamOpener imageOpener, String imageLocation )
    {
        this( imageOpener, imageLocation, defaultConfig );
    }

    public WatermarkPainter( StreamOpener imageOpener, String imageLocation, WatermarkConfig config )
    {
        this( newImageLoader( imageOpener, imageLocation ), config );
    }

    public WatermarkPainter( Supplier<BufferedImage> imageSupplier )
    {
        this( imageSupplier, defaultConfig );
    }

    public WatermarkPainter( Supplier<BufferedImage> imageSupplier, WatermarkConfig config )
    {
        this.imageSupplier = imageSupplier;
        this.config = config;
        this.texture = null;
        this.initialized = false;

        this.prog = new ColorTexture2DProgram( );
        this.inXy = new GLEditableBuffer( GL.GL_STATIC_DRAW, 0 );
        this.inS = new GLEditableBuffer( GL.GL_STATIC_DRAW, 0 );
    }

    public static Supplier<BufferedImage> newImageLoader( final StreamOpener opener, final String location )
    {
        return new Supplier<BufferedImage>( )
        {
            @Override
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

    protected void initIfNecessary( GL gl )
    {
        if ( initialized ) return;

        try
        {
            BufferedImage image = imageSupplier.get( );
            GLProfile profile = gl.getContext( ).getGLDrawable( ).getGLProfile( );
            texture = newTexture( gl, new AWTTextureData( profile, 0, 0, false, image ) );
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
        double maxArea = config.maxAreaFraction * wBounds * hBounds;

        double w = config.maxWidthPixels;
        w = min( w, config.maxWidthFraction * wBounds );
        w = min( w, sqrt( maxArea * wImage / hImage ) );

        double h = config.maxHeightPixels;
        h = min( h, config.maxHeightFraction * hBounds );
        h = min( h, sqrt( maxArea * hImage / wImage ) );

        w = min( w, h * wImage / hImage );
        h = w * hImage / wImage;

        double p = 0;
        p = max( p, w / config.maxWidthPixels );
        p = max( p, h / config.maxHeightPixels );
        //p = max( p, w*h / maxArea );
        double padding = p * config.maxPaddingPixels;

        return doubles( w, h, padding );
    }

    @Override
    protected void doPaintTo( GlimpseContext context )
    {
        GL3 gl = context.getGL( ).getGL3( );
        GlimpseBounds bounds = getBounds( context );

        initIfNecessary( gl );
        if ( texture == null ) return;

        double[] quadGeometry = computeQuadGeometry( texture.getWidth( ), texture.getHeight( ), bounds.getWidth( ), bounds.getHeight( ) );
        double wQuad = quadGeometry[0];
        double hQuad = quadGeometry[1];
        double padding = quadGeometry[2];

        float xLeft;
        float xRight;
        switch ( config.horizontalPos )
        {
            case Left:
            {
                xLeft = ( float ) ( padding );
                xRight = ( float ) ( xLeft + wQuad );
            }
                break;

            case Center:
            {
                xLeft = ( float) ( bounds.getWidth( ) / 2f - wQuad / 2f );
                xRight = ( float ) ( xLeft + wQuad );
            }
                break;

            case Right:
            default:
            {
                xRight = ( float ) ( bounds.getWidth( ) - padding );
                xLeft = ( float ) ( xRight - wQuad );
            }
                break;
        }

        float yTop;
        float yBottom;
        switch ( config.verticalPos )
        {
            case Top:
            {
                yTop = ( float ) ( bounds.getHeight( ) - padding );
                yBottom = ( float ) ( yTop - hQuad );
            }
                break;

            case Center:
            {
                yBottom = ( float) ( bounds.getHeight( ) / 2f - hQuad / 2f );
                yTop = ( float ) ( yBottom + hQuad );
            }
                break;

            case Bottom:
            default:
            {
                yBottom = ( float ) ( padding );
                yTop = ( float ) ( yBottom + hQuad );
            }
                break;
        }

        inXy.clear( );
        inXy.growQuad2f( xLeft, yBottom, xRight, yTop );

        inS.clear( );
        inS.growQuad2f( 0, 1, 1, 0 );

        texture.setTexParameteri( gl, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_LINEAR );
        texture.setTexParameteri( gl, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_LINEAR );
        texture.enable( gl );
        texture.bind( gl );

        // See the "Alpha premultiplication" section in Texture's class comment
        gl.glEnable( GL3.GL_BLEND );
        gl.glBlendFunc( GL3.GL_ONE, GL3.GL_ONE_MINUS_SRC_ALPHA );
        //XXX: this needs to be replaced by shader code
        //gl.glTexEnvi( GL3.GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE );

        prog.begin( context );
        try
        {
            prog.setPixelOrtho( context, bounds );
            prog.setTexture( context, 0 );

            prog.draw( context, texture, inXy, inS );
        }
        finally
        {
            prog.end( context );
            gl.glDisable( GL3.GL_BLEND );
        }
    }

    @Override
    protected void doDispose( GlimpseContext context )
    {
        prog.dispose( context );
        inXy.dispose( context.getGL( ) );
        inS.dispose( context.getGL( ) );
    }
}
