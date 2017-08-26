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
package com.metsci.glimpse.worldwind.tile;

import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;

import java.util.List;
import java.util.logging.Logger;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLContext;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.canvas.FBOGlimpseCanvas;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.painter.texture.ShadedTexturePainter;
import com.metsci.glimpse.support.projection.FlatProjection;
import com.metsci.glimpse.support.projection.GeoReprojection;
import com.metsci.glimpse.support.texture.ExternalTextureProjected2D;
import com.metsci.glimpse.support.texture.TextureProjected2D;
import com.metsci.glimpse.util.geo.projection.GeoProjection;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.util.OGLStackHandler;

public class GlimpseReprojectingSurfaceTile extends GlimpseResizingSurfaceTile
{
    private static final Logger logger = Logger.getLogger( GlimpseReprojectingSurfaceTile.class.getSimpleName( ) );

    private static double REPROJECT_DISCRETIZE_FRACTION = 0.01;

    protected FBOGlimpseCanvas reprojectCanvas;
    protected ShadedTexturePainter texturePainter;
    protected GlimpseAxisLayout2D reprojectLayout;

    protected TextureProjected2D texture;
    protected GeoReprojection reproject;

    protected GeoProjection projectionTo;

    protected double minX, maxX, minY, maxY;

    public GlimpseReprojectingSurfaceTile( GlimpseLayout layout, Axis2D axes, GeoProjection from, GeoProjection to, int preferredWidth, int preferredHeight, double minLat, double maxLat, double minLon, double maxLon )
    {
        super( layout, axes, from, 8192, 8192, preferredWidth, preferredHeight, minLat, maxLat, minLon, maxLon );

        this.projectionTo = to;
    }

    public GlimpseReprojectingSurfaceTile( GlimpseLayout layout, Axis2D axes, GeoProjection from, GeoProjection to, int preferredWidth, int preferredHeight, List<LatLon> corners )
    {
        super( layout, axes, from, 8192, 8192, preferredWidth, preferredHeight, corners );

        this.projectionTo = to;
    }

    public GlimpseReprojectingSurfaceTile( GlimpseLayout layout, Axis2D axes, GeoProjection from, GeoProjection to, int maxWidth, int maxHeight, int preferredWidth, int preferredHeight, double minLat, double maxLat, double minLon, double maxLon )
    {
        super( layout, axes, from, maxWidth, maxHeight, preferredWidth, preferredHeight, minLat, maxLat, minLon, maxLon );

        this.projectionTo = to;
    }

    public GlimpseReprojectingSurfaceTile( GlimpseLayout layout, Axis2D axes, GeoProjection from, GeoProjection to, int maxWidth, int maxHeight, int preferredWidth, int preferredHeight, List<LatLon> corners )
    {
        super( layout, axes, from, maxWidth, maxHeight, preferredWidth, preferredHeight, corners );

        this.projectionTo = to;
    }

    protected void init( GLContext context )
    {
        this.reprojectCanvas = new FBOGlimpseCanvas( context, width, height );
        this.texturePainter = new ShadedTexturePainter( );

        this.reprojectLayout = new GlimpseAxisLayout2D( new Axis2D( ) );
        this.reprojectLayout.addPainter( new BackgroundPainter( ).setColor( 0f, 0f, 0f, 0f ) );
        this.reprojectLayout.addPainter( this.texturePainter );

        this.reprojectCanvas.addLayout( this.reprojectLayout );

    }

    @Override
    protected int getTextureHandle( )
    {
        return reprojectCanvas.getTextureName( );
    }

    @Override
    protected void resizeCanvas( int width, int height )
    {
        super.resizeCanvas( width, height );
        reprojectCanvas.resize( width, height );
        updateProjection( width, height );
    }

    @Override
    protected void setTextureScale( TextureSurfaceTile tile, float scaleX, float scaleY )
    {
        // do nothing -- keep the TextureSurfaceTile scale at 1.0
        // (GlimpseReprojectingSurfaceTile fills the entire canvas
        //  when it reprojects the offscreen rendered texture)
    }

    protected void updateProjection( int width, int height )
    {
        minX = axes.getMinX( );
        maxX = axes.getMaxX( );
        minY = axes.getMinY( );
        maxY = axes.getMaxY( );

        // the scene was drawn onto the offscreenCanvas using the bounds specified by axes
        // create a projection which allows us to map to axis coordinates given
        // a 0.0 to 1.0 relative coordinate in the offscreenCanvas texture
        FlatProjection flat = new FlatProjection( axes, 0, scaleX, 0, scaleY );

        // build the reprojection (an instance of com.metsci.glimpse.support.projection.Projection)
        reproject = new GeoReprojection( flat, projection, projectionTo, REPROJECT_DISCRETIZE_FRACTION );

        // set the reprojection on the texture
        if ( texture != null )
        {
            texture.setProjection( reproject );
        }

        // set reprojectLayout axes to reflect the lat/lon bounds of the surface tile
        // in the "to" projection (for Worldwind, this projection is usually a
        // PlateCareeProjection)
        setAxes( reprojectLayout.getAxis( ), bounds, projectionTo );
    }

    @Override
    protected void drawOffscreen( GLContext glContext )
    {
        // draw the GlimpseLayout into the offscreenCanvas
        super.drawOffscreen( glContext );

        // get a handle to the texture offscreenCanvas drew into
        if ( texture == null )
        {
            init( glContext );

            int width = offscreenCanvas.getTargetBounds( ).getWidth( );
            int height = offscreenCanvas.getTargetBounds( ).getHeight( );
            int texHandle = offscreenCanvas.getTextureName( );

            texture = new ExternalTextureProjected2D( texHandle, width, height, false )
            {
                @Override
                protected void putVertexCoords( int texIndex, double texFracX, double texFracY, float[] temp )
                {
                    super.putVertexCoords( texIndex, ( float ) texFracX * scaleX, ( float ) texFracY * scaleY, temp );
                }

                @Override
                protected void putVertexTexCoords( int texIndex, double texFracX, double texFracY )
                {
                    super.putVertexTexCoords( texIndex, ( float ) texFracX * scaleX, ( float ) texFracY * scaleY );
                }

                @Override
                protected void prepare_glState( GL gl )
                {
                    gl.glEnable( GL2.GL_TEXTURE_2D );
                    gl.glDisable( GL2.GL_BLEND );
                }
            };

            texturePainter.removeAllDrawableTextures( );
            texturePainter.addDrawableTexture( texture );

            if ( reproject != null )
            {
                texture.setProjection( reproject );
            }
        }

        // rebuild the projection if the axes changed
        if ( didAxesChange( axes ) )
        {
            updateProjection( currentWidth, currentHeight );
        }

        OGLStackHandler stack = new OGLStackHandler( );
        GL2 gl = glContext.getGL( ).getGL2( );

        stack.pushAttrib( gl, GL2.GL_ALL_ATTRIB_BITS );
        stack.pushClientAttrib( gl, ( int ) GL2.GL_ALL_CLIENT_ATTRIB_BITS );
        stack.pushTexture( gl );
        gl.glLoadIdentity( );
        stack.pushModelview( gl );
        stack.pushProjection( gl );

        GLContext c = offscreenCanvas.getGLContext( );

        if ( c != null )
        {
            c.makeCurrent( );
            try
            {
                reprojectCanvas.paint( );
            }
            catch ( Exception e )
            {
                logWarning( logger, "Trouble drawing to offscreen buffer", e );
            }
            finally
            {
                glContext.makeCurrent( );
                stack.pop( gl );
            }
        }
    }

    protected boolean didAxesChange( Axis2D axis )
    {
        return ! ( axis.getMinX( ) == minX && axis.getMaxX( ) == maxX && axis.getMinY( ) == minY && axis.getMaxY( ) == maxY );
    }
}
