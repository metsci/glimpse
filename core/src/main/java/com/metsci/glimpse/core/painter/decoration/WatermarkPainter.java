/*
 * Copyright (c) 2020, Metron, Inc.
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
package com.metsci.glimpse.core.painter.decoration;

import static com.jogamp.opengl.util.texture.TextureIO.newTexture;
import static com.metsci.glimpse.core.gl.util.GLUtils.disableBlending;
import static com.metsci.glimpse.core.gl.util.GLUtils.enablePremultipliedAlphaBlending;
import static com.metsci.glimpse.core.painter.info.SimpleTextPainter.HorizontalPosition.Center;
import static com.metsci.glimpse.core.painter.info.SimpleTextPainter.HorizontalPosition.Left;
import static com.metsci.glimpse.core.painter.info.SimpleTextPainter.HorizontalPosition.Right;
import static com.metsci.glimpse.core.painter.info.SimpleTextPainter.VerticalPosition.Bottom;
import static com.metsci.glimpse.core.painter.info.SimpleTextPainter.VerticalPosition.Top;
import static com.metsci.glimpse.util.GeneralUtils.doubles;
import static com.metsci.glimpse.util.GeneralUtils.floats;
import static com.metsci.glimpse.util.logging.LoggerUtils.getLogger;
import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.awt.AWTTextureData;
import com.metsci.glimpse.core.context.GlimpseBounds;
import com.metsci.glimpse.core.context.GlimpseContext;
import com.metsci.glimpse.core.gl.GLEditableBuffer;
import com.metsci.glimpse.core.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.core.painter.info.SimpleTextPainter.HorizontalPosition;
import com.metsci.glimpse.core.painter.info.SimpleTextPainter.VerticalPosition;
import com.metsci.glimpse.core.support.shader.triangle.ColorTexture2DProgram;

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
        public final float[] rgbaFactor;
        public final float[] rgbaFactorPremultiplied;

        public WatermarkConfig( double maxWidthPixels, double maxHeightPixels, double maxAreaFraction, double maxWidthFraction, double maxHeightFraction, double maxPaddingPixels, VerticalPosition verticalPos, HorizontalPosition horizontalPos, float[] rgbaFactor )
        {
            this.maxWidthPixels = maxWidthPixels;
            this.maxHeightPixels = maxHeightPixels;
            this.maxAreaFraction = maxAreaFraction;
            this.maxWidthFraction = maxWidthFraction;
            this.maxHeightFraction = maxHeightFraction;
            this.maxPaddingPixels = maxPaddingPixels;
            this.verticalPos = verticalPos;
            this.horizontalPos = horizontalPos;
            this.rgbaFactor = Arrays.copyOf( rgbaFactor, 4 );

            float r = this.rgbaFactor[ 0 ];
            float g = this.rgbaFactor[ 1 ];
            float b = this.rgbaFactor[ 2 ];
            float a = this.rgbaFactor[ 3 ];
            this.rgbaFactorPremultiplied = floats( a*r, a*g, a*b, a );
        }

        public WatermarkConfig withMaxWidthPixels( double maxWidthPixels )
        {
            return new WatermarkConfig( maxWidthPixels, this.maxHeightPixels, this.maxAreaFraction, this.maxWidthFraction, this.maxHeightFraction, this.maxPaddingPixels, this.verticalPos, this.horizontalPos, this.rgbaFactor );
        }

        public WatermarkConfig withMaxHeightPixels( double maxHeightPixels )
        {
            return new WatermarkConfig( this.maxWidthPixels, maxHeightPixels, this.maxAreaFraction, this.maxWidthFraction, this.maxHeightFraction, this.maxPaddingPixels, this.verticalPos, this.horizontalPos, this.rgbaFactor );
        }

        public WatermarkConfig withMaxAreaFraction( double maxAreaFraction )
        {
            return new WatermarkConfig( this.maxWidthPixels, this.maxHeightPixels, maxAreaFraction, this.maxWidthFraction, this.maxHeightFraction, this.maxPaddingPixels, this.verticalPos, this.horizontalPos, this.rgbaFactor );
        }

        public WatermarkConfig withMaxWidthFraction( double maxWidthFraction )
        {
            return new WatermarkConfig( this.maxWidthPixels, this.maxHeightPixels, this.maxAreaFraction, maxWidthFraction, this.maxHeightFraction, this.maxPaddingPixels, this.verticalPos, this.horizontalPos, this.rgbaFactor );
        }

        public WatermarkConfig withMaxHeightFraction( double maxHeightFraction )
        {
            return new WatermarkConfig( this.maxWidthPixels, this.maxHeightPixels, this.maxAreaFraction, this.maxWidthFraction, maxHeightFraction, this.maxPaddingPixels, this.verticalPos, this.horizontalPos, this.rgbaFactor );
        }

        public WatermarkConfig withMaxPaddingPixels( double maxPaddingPixels )
        {
            return new WatermarkConfig( this.maxWidthPixels, this.maxHeightPixels, this.maxAreaFraction, this.maxWidthFraction, this.maxHeightFraction, maxPaddingPixels, this.verticalPos, this.horizontalPos, this.rgbaFactor );
        }

        public WatermarkConfig withVerticalPos( VerticalPosition verticalPos )
        {
            return new WatermarkConfig( this.maxWidthPixels, this.maxHeightPixels, this.maxAreaFraction, this.maxWidthFraction, this.maxHeightFraction, this.maxPaddingPixels, verticalPos, this.horizontalPos, this.rgbaFactor );
        }

        public WatermarkConfig withHorizontalPos( HorizontalPosition horizontalPos )
        {
            return new WatermarkConfig( this.maxWidthPixels, this.maxHeightPixels, this.maxAreaFraction, this.maxWidthFraction, this.maxHeightFraction, this.maxPaddingPixels, this.verticalPos, horizontalPos, this.rgbaFactor );
        }

        public WatermarkConfig withPos( VerticalPosition verticalPos, HorizontalPosition horizontalPos )
        {
            return new WatermarkConfig( this.maxWidthPixels, this.maxHeightPixels, this.maxAreaFraction, this.maxWidthFraction, this.maxHeightFraction, this.maxPaddingPixels, verticalPos, horizontalPos, this.rgbaFactor );
        }

        public WatermarkConfig withRgbaFactor( float[] rgbaFactor )
        {
            return new WatermarkConfig( this.maxWidthPixels, this.maxHeightPixels, this.maxAreaFraction, this.maxWidthFraction, this.maxHeightFraction, this.maxPaddingPixels, this.verticalPos, this.horizontalPos, rgbaFactor );
        }

        public WatermarkConfig withRgbaFactor( float rFactor, float gFactor, float bFactor, float aFactor )
        {
            return this.withRgbaFactor( floats( rFactor, gFactor, bFactor, aFactor ) );
        }
    }

    public static final WatermarkConfig defaultConfig = new WatermarkConfig( 350, 350, 0.04, 0.28, 0.28, 10, Bottom, Center, floats( 1f, 1f, 1f, 1f ) );

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

    public WatermarkPainter( URL imageUrl )
    {
        this( imageUrl, defaultConfig );
    }

    public WatermarkPainter( URL imageUrl, WatermarkConfig config )
    {
        this( newImageLoader( imageUrl ), config );
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

    public static Supplier<BufferedImage> newImageLoader( URL imageUrl )
    {
        return new Supplier<BufferedImage>( )
        {
            @Override
            public BufferedImage get( )
            {
                try ( InputStream stream = imageUrl.openStream( ) )
                {
                    return ImageIO.read( stream );
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

        enablePremultipliedAlphaBlending( gl );
        prog.begin( context );
        try
        {
            prog.setPixelOrtho( context, bounds );
            prog.setTexture( context, 0 );
            prog.setColor( context, this.config.rgbaFactorPremultiplied );

            prog.draw( context, texture, inXy, inS );
        }
        finally
        {
            prog.end( context );
            disableBlending( gl );
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
