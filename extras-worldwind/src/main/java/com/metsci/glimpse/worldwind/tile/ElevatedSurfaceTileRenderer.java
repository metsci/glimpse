/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
// see: http://forum.worldwindcentral.com/showthread.php?t=35779
package com.metsci.glimpse.worldwind.tile;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GeographicSurfaceTileRenderer;
import gov.nasa.worldwind.render.SurfaceTile;
import gov.nasa.worldwind.terrain.SectorGeometry;
import gov.nasa.worldwind.terrain.SectorGeometryList;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.OGLUtil;

import java.util.ArrayList;
import java.util.logging.Level;

import javax.media.opengl.GL;

public class ElevatedSurfaceTileRenderer extends GeographicSurfaceTileRenderer
{
    private double sgWidth;
    private double sgHeight;
    private double sgMinWE;
    private double sgMinSN;

    private static final int DEFAULT_ALPHA_TEXTURE_SIZE = 1024;

    private boolean showImageTileOutlines = false;

    SectorGeometryList sgl;

    Globe globe;

    public ElevatedSurfaceTileRenderer( Globe globe )
    {
        this.globe = globe;
    }

    Globe getActiveGlobe( DrawContext dc )
    {
        if ( globe == null )
        {
            return dc.getGlobe( );
        }
        else
        {
            return globe;
        }
    }

    public void tessellate( DrawContext dc )
    {
        sgl = globe.tessellate( dc );
    }

    @Override
    public boolean isShowImageTileOutlines( )
    {
        return showImageTileOutlines;
    }

    @Override
    public void setShowImageTileOutlines( boolean showImageTileOutlines )
    {
        this.showImageTileOutlines = showImageTileOutlines;
    }

    protected static class Transform
    {
        double HScale;
        double VScale;
        double HShift;
        double VShift;
        double rotationDegrees;
    }

    @Override
    public void renderTiles( DrawContext dc, Iterable<? extends SurfaceTile> tiles )
    {
        if ( sgl == null )
        {
            tessellate( dc );
        }

        if ( tiles == null )
        {
            String message = Logging.getMessage( "nullValue.TileIterableIsNull" );
            Logging.logger( ).severe( message );
            throw new IllegalStateException( message );
        }

        if ( dc == null )
        {
            String message = Logging.getMessage( "nullValue.DrawContextIsNull" );
            Logging.logger( ).severe( message );
            throw new IllegalStateException( message );
        }

        GL gl = dc.getGL( );
        int alphaTextureUnit = GL.GL_TEXTURE1;
        boolean showOutlines = this.showImageTileOutlines && dc.getGLRuntimeCapabilities( ).getNumTextureUnits( ) > 2;

        gl.glPushAttrib( GL.GL_COLOR_BUFFER_BIT // for alpha func
                | GL.GL_ENABLE_BIT | GL.GL_CURRENT_BIT | GL.GL_DEPTH_BUFFER_BIT // for
                                                                                // depth
                                                                                // func
                | GL.GL_TRANSFORM_BIT );

        try
        {
            this.alphaTexture = dc.getTextureCache( ).getTexture( this );
            if ( this.alphaTexture == null )
            {
                this.initAlphaTexture( DEFAULT_ALPHA_TEXTURE_SIZE ); // TODO: choose size
                // to match incoming
                // tile sizes?
                dc.getTextureCache( ).put( this, this.alphaTexture );
            }

            if ( showOutlines && this.outlineTexture == null ) this.initOutlineTexture( 128 );

            gl.glEnable( GL.GL_DEPTH_TEST );
            gl.glDepthFunc( GL.GL_LEQUAL );

            gl.glEnable( GL.GL_ALPHA_TEST );
            gl.glAlphaFunc( GL.GL_GREATER, 0.00001f );

            gl.glActiveTexture( GL.GL_TEXTURE0 );
            gl.glEnable( GL.GL_TEXTURE_2D );
            gl.glMatrixMode( GL.GL_TEXTURE );
            gl.glPushMatrix( );
            if ( !dc.isPickingMode( ) )
            {
                gl.glTexEnvi( GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE );
            }
            else
            {
                gl.glTexEnvf( GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_COMBINE );
                gl.glTexEnvf( GL.GL_TEXTURE_ENV, GL.GL_SRC0_RGB, GL.GL_PREVIOUS );
                gl.glTexEnvf( GL.GL_TEXTURE_ENV, GL.GL_COMBINE_RGB, GL.GL_REPLACE );
            }

            int numTexUnitsUsed = 2;
            if ( showOutlines )
            {
                numTexUnitsUsed = 3;
                alphaTextureUnit = GL.GL_TEXTURE2;
                gl.glActiveTexture( GL.GL_TEXTURE1 );
                gl.glEnable( GL.GL_TEXTURE_2D );
                gl.glMatrixMode( GL.GL_TEXTURE );
                gl.glPushMatrix( );
                gl.glTexEnvi( GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_ADD );
            }

            gl.glActiveTexture( alphaTextureUnit );
            gl.glEnable( GL.GL_TEXTURE_2D );
            gl.glMatrixMode( GL.GL_TEXTURE );
            gl.glPushMatrix( );
            gl.glTexEnvi( GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE );

            sgl.beginRendering( dc );

            // For each current geometry tile, find the intersecting image tiles and
            // render the geometry
            // tile once for each intersecting image tile.
            Transform transform = new Transform( );
            for ( SectorGeometry sg : sgl )
            {
                Iterable<SurfaceTile> tilesToRender = this.getIntersectingTiles( dc, sg, tiles );
                if ( tilesToRender == null ) continue;

                sg.beginRendering( dc, numTexUnitsUsed ); // TODO: wrap in try/catch in
                                                          // case of exception

                // Pre-load info to compute the texture transform
                this.preComputeTextureTransform( dc, sg, transform );

                // For each intersecting tile, establish the texture transform necessary
                // to map the image tile
                // into the geometry tile's texture space. Use an alpha texture as a
                // mask to prevent changing the
                // frame buffer where the image tile does not overlap the geometry tile.
                // Render both the image and
                // alpha textures via multi-texture rendering.
                // TODO: Figure out how to apply multi-texture to more than one tile at
                // a time. Use fragment shader?
                for ( SurfaceTile tile : tilesToRender )
                {
                    gl.glActiveTexture( GL.GL_TEXTURE0 );

                    if ( tile.bind( dc ) )
                    {
                        gl.glMatrixMode( GL.GL_TEXTURE );
                        gl.glLoadIdentity( );
                        tile.applyInternalTransform( dc, true );

                        // Determine and apply texture transform to map image tile into
                        // geometry tile's texture space
                        this.computeTextureTransform( dc, tile, transform );
                        gl.glScaled( transform.HScale, transform.VScale, 1d );
                        gl.glTranslated( transform.HShift, transform.VShift, 0d );

                        if ( showOutlines )
                        {
                            gl.glActiveTexture( GL.GL_TEXTURE1 );
                            this.outlineTexture.bind( );

                            // Apply the same texture transform to the outline texture. The
                            // outline textures uses a
                            // different texture unit than the tile, so the transform made
                            // above does not carry over.
                            gl.glMatrixMode( GL.GL_TEXTURE );
                            gl.glLoadIdentity( );
                            gl.glScaled( transform.HScale, transform.VScale, 1d );
                            gl.glTranslated( transform.HShift, transform.VShift, 0d );
                        }

                        // Prepare the alpha texture to be used as a mask where texture
                        // coords are outside [0,1]
                        gl.glActiveTexture( alphaTextureUnit );
                        this.alphaTexture.bind( );

                        // Apply the same texture transform to the alpha texture. The alpha
                        // texture uses a
                        // different texture unit than the tile, so the transform made above
                        // does not carry over.
                        gl.glMatrixMode( GL.GL_TEXTURE );
                        gl.glLoadIdentity( );
                        gl.glScaled( transform.HScale, transform.VScale, 1d );
                        gl.glTranslated( transform.HShift, transform.VShift, 0d );

                        // Render the geometry tile
                        sg.renderMultiTexture( dc, numTexUnitsUsed );
                    }
                }

                sg.endRendering( dc );
            }
        }
        catch ( Exception e )
        {
            Logging.logger( ).log( Level.SEVERE, Logging.getMessage( "generic.ExceptionWhileRenderingLayer", this.getClass( ).getName( ) ), e );
        }
        finally
        {
            sgl.endRendering( dc );

            // gl.glActiveTexture(alphaTextureUnit);
            // gl.glMatrixMode(GL.GL_TEXTURE);
            // gl.glPopMatrix();
            // gl.glDisable(GL.GL_TEXTURE_2D);

            if ( showOutlines )
            {
                gl.glActiveTexture( GL.GL_TEXTURE1 );
                gl.glMatrixMode( GL.GL_TEXTURE );
                gl.glPopMatrix( );
                gl.glDisable( GL.GL_TEXTURE_2D );
            }

            gl.glActiveTexture( GL.GL_TEXTURE0 );
            gl.glMatrixMode( GL.GL_TEXTURE );
            gl.glPopMatrix( );
            gl.glDisable( GL.GL_TEXTURE_2D );

            gl.glTexEnvf( GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, OGLUtil.DEFAULT_TEX_ENV_MODE );
            if ( dc.isPickingMode( ) )
            {
                gl.glTexEnvf( GL.GL_TEXTURE_ENV, GL.GL_SRC0_RGB, OGLUtil.DEFAULT_SRC0_RGB );
                gl.glTexEnvf( GL.GL_TEXTURE_ENV, GL.GL_COMBINE_RGB, OGLUtil.DEFAULT_COMBINE_RGB );
            }

            gl.glPopAttrib( );
        }
    }

    protected void preComputeTextureTransform( DrawContext dc, SectorGeometry sg, Transform t )
    {
        Sector st = sg.getSector( );
        this.sgWidth = st.getDeltaLonRadians( );
        this.sgHeight = st.getDeltaLatRadians( );
        this.sgMinWE = st.getMinLongitude( ).radians;
        this.sgMinSN = st.getMinLatitude( ).radians;
    }

    protected void computeTextureTransform( DrawContext dc, SurfaceTile tile, Transform t )
    {
        Sector st = tile.getSector( );
        double tileWidth = st.getDeltaLonRadians( );
        double tileHeight = st.getDeltaLatRadians( );
        double minLon = st.getMinLongitude( ).radians;
        double minLat = st.getMinLatitude( ).radians;

        t.VScale = tileHeight > 0 ? this.sgHeight / tileHeight : 1;
        t.HScale = tileWidth > 0 ? this.sgWidth / tileWidth : 1;
        t.VShift = - ( minLat - this.sgMinSN ) / this.sgHeight;
        t.HShift = - ( minLon - this.sgMinWE ) / this.sgWidth;
    }

    @Override
    protected Iterable<SurfaceTile> getIntersectingTiles( DrawContext dc, SectorGeometry sg, Iterable<? extends SurfaceTile> tiles )
    {
        ArrayList<SurfaceTile> intersectingTiles = null;

        for ( SurfaceTile tile : tiles )
        {
            // if (!tile.getSector().intersectsInterior(sg.getSector()))
            // continue;

            if ( intersectingTiles == null ) // lazy creation because most common case
                                             // is no intersecting tiles
            intersectingTiles = new ArrayList<SurfaceTile>( );

            intersectingTiles.add( tile );
        }

        return intersectingTiles; // will be null if no intersecting tiles
    }

}