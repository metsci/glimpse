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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLContext;

import gov.nasa.worldwind.geom.Extent;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.EllipsoidalGlobe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.SurfaceTile;
import gov.nasa.worldwind.render.SurfaceTileRenderer;
import gov.nasa.worldwind.util.Logging;

/**
 * A SurfaceTile which renders imagery from an OpenGL texture handle.
 *
 * @author ulman
 */
public class TextureSurfaceTile implements SurfaceTile, Renderable
{

    protected int textureHandle;
    protected Sector sector;
    protected List<LatLon> corners;

    protected float scaleX = 1.0f;
    protected float scaleY = 1.0f;
    protected float opacity = 1.0f;

    protected List<TextureSurfaceTile> thisList = Collections.singletonList( this );

    protected EllipsoidalGlobe globe;
    protected SurfaceTileRenderer renderer;

    public TextureSurfaceTile( int textureHandle, Sector sector )
    {
        this( textureHandle, ( Iterable<? extends LatLon> ) sector );
    }

    public TextureSurfaceTile( int textureHandle, Iterable<? extends LatLon> corners )
    {
        this.textureHandle = textureHandle;

        this.initializeGeometry( corners );
    }

    public void setOpacity( double opacity )
    {
        this.opacity = (float) opacity;
    }

    public void setSurfaceTileRenderer( SurfaceTileRenderer renderer )
    {
        this.renderer = renderer;
    }

    public void setTextureScale( float scaleX, float scaleY )
    {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }

    public float getTextureScaleX( )
    {
        return this.scaleX;
    }

    public float getTextureScaleY( )
    {
        return this.scaleY;
    }

    public void setCorners( Iterable<? extends LatLon> corners )
    {
        this.initializeGeometry( corners );
    }

    protected void initializeGeometry( Iterable<? extends LatLon> corners )
    {
        this.corners = new ArrayList<LatLon>( 4 );
        for ( LatLon ll : corners )
        {
            this.corners.add( ll );
        }

        this.sector = Sector.boundingSector( this.corners );
    }

    /////////////// Renderable methods ///////////////

    @Override
    public void render( DrawContext dc )
    {
        GL2 gl = dc.getGL( ).getGL2( );

        gl.glPushAttrib( GL2.GL_POLYGON_BIT );
        gl.glPolygonMode( GL2.GL_FRONT, GL2.GL_FILL );
        gl.glEnable( GL2.GL_CULL_FACE );
        gl.glCullFace( GL2.GL_BACK );

        SurfaceTileRenderer r = renderer != null ? renderer : dc.getGeographicSurfaceTileRenderer( );

        if ( opacity != 1.0f )
        {
            gl.glEnable( GL2.GL_BLEND );
            gl.glBlendFunc( GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA );
            gl.glColor4f( 1.0f, 1.0f, 1.0f, opacity );
        }
        else
        {
            gl.glColor4f( 1.0f, 1.0f, 1.0f, 1.0f );
        }

        try
        {
            r.renderTiles( dc, this.thisList );
        }
        finally
        {
            gl.glPopAttrib( );
        }
    }

    /////////////// SurfaceTile methods ///////////////

    @Override
    public boolean bind( DrawContext dc )
    {
        GL gl = dc.getGL( );

        gl.glBindTexture( GL2.GL_TEXTURE_2D, textureHandle );

        // these settings make fine line drawing against a transparent background appear much more natural
        // but can make other rendering look too jagged/crisp

        gl.glTexParameterf( GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR );
        gl.glTexParameterf( GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR );
        gl.glTexParameterf( GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE );
        gl.glTexParameterf( GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE );

        gl.glBlendFuncSeparate( GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA, GL2.GL_ONE, GL2.GL_ONE_MINUS_SRC_ALPHA );
        gl.glEnable( GL2.GL_BLEND );

        //GlimpseColor.glColor( gl, GlimpseColor.getWhite( 0.5f ) );

        return true;
    }

    @Override
    public void applyInternalTransform( DrawContext dc, boolean textureIdentityActive )
    {
        GL2 gl = GLContext.getCurrent( ).getGL( ).getGL2( );

        if ( !textureIdentityActive )
        {
            gl.glMatrixMode( GL2.GL_TEXTURE );
            gl.glLoadIdentity( );
        }

        gl.glScaled( scaleX, scaleY, 1 );
    }

    @Override
    public Sector getSector( )
    {
        return sector;
    }

    @Override
    public List<? extends LatLon> getCorners( )
    {
        return corners;
    }

    @Override
    public Extent getExtent( DrawContext dc )
    {
        if ( dc == null )
        {
            String msg = Logging.getMessage( "nullValue.DrawContextIsNull" );
            Logging.logger( ).severe( msg );
            throw new IllegalArgumentException( msg );
        }

        return Sector.computeBoundingCylinder( dc.getGlobe( ), dc.getVerticalExaggeration( ), this.getSector( ) );
    }
}
