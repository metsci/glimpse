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
package com.metsci.glimpse.topo;

import static com.metsci.glimpse.core.support.color.GlimpseColor.getBlack;
import static com.metsci.glimpse.core.support.color.GlimpseColor.toColorAwt;
import static com.metsci.glimpse.topo.ShadedReliefProgram.ELEVATION_TEXTURE_UNIT;
import static com.metsci.glimpse.topo.ShadedReliefProgram.HILLSHADE_TEXTURE_UNIT;
import static com.metsci.glimpse.topo.TopoColorUtils.topoColorsStepped;
import static java.awt.Color.RGBtoHSB;
import static java.util.Arrays.sort;
import static java.util.Comparator.comparing;

import java.awt.Color;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import com.metsci.glimpse.core.gl.texture.DrawableTexture;
import com.metsci.glimpse.core.painter.geo.TileKey;
import com.metsci.glimpse.core.painter.geo.TilePainter;
import com.metsci.glimpse.core.painter.info.SimpleTextPainter;
import com.metsci.glimpse.core.painter.texture.ShadedTexturePainter;
import com.metsci.glimpse.core.support.colormap.ColorGradientUtils.ValueAndColor;
import com.metsci.glimpse.core.support.projection.LatLonProjection;
import com.metsci.glimpse.core.support.texture.FloatTextureProjected2D;
import com.metsci.glimpse.topo.ShadedReliefTileCache.CachedTileData;
import com.metsci.glimpse.topo.ShadedReliefTileCache.ReliefTileKey;
import com.metsci.glimpse.topo.io.TopoDataset;
import com.metsci.glimpse.util.geo.projection.GeoProjection;

/**
 * Paints topography and bathymetry with a discrete set of colors and the Hillshade algorithm.
 *
 * @author borkholder
 */
public class ShadedReliefTiledPainter extends TilePainter<DrawableTexture[]>
{
    protected ShadedReliefTileCache tileCache;
    protected ShadedTexturePainter topoImagePainter;
    protected ShadedReliefProgram shadedReliefProgram;

    public ShadedReliefTiledPainter( GeoProjection projection, TopoDataset topoDataset )
    {
        this( projection, topoDataset, null );
    }

    public ShadedReliefTiledPainter( GeoProjection projection, TopoDataset topoDataset, String attributionText )
    {
        super( projection );

        this.tileCache = new ShadedReliefTileCache( topoDataset );

        topoImagePainter = new ShadedTexturePainter( );
        shadedReliefProgram = new ShadedReliefProgram( );
        topoImagePainter.setProgram( shadedReliefProgram );
        addPainter( topoImagePainter );

        setAlpha( 1 );
        setColors( topoColorsStepped );

        if ( attributionText != null )
        {
            SimpleTextPainter attributionPainter = new SimpleTextPainter( );
            attributionPainter.setPaintBackground( false );
            attributionPainter.setPaintBorder( false );
            attributionPainter.setFont( 10, false );
            attributionPainter.setColor( getBlack( 0.4f ) );
            attributionPainter.setText( attributionText );
            addPainter( attributionPainter );
        }
    }

    public void setAlpha( float alpha )
    {
        shadedReliefProgram.setAlpha( alpha );
    }

    /**
     * @see #setColors(List)
     */
    public void setColors( List<ValueAndColor> levelColors )
    {
        setColors( levelColors.toArray( new ValueAndColor[0] ) );
    }

    /**
     * Sets the color steps. The value provided for each step is in elevation SU (meters).
     * The color shade (hue) is changed with the hillshade value.
     */
    public void setColors( ValueAndColor... levelColors )
    {
        levelColors = levelColors.clone( );
        // Sort in ascending order
        sort( levelColors, comparing( vc -> vc.v ) );

        float[][] newColors = new float[levelColors.length][4];
        for ( int i = 0; i < levelColors.length; i++ )
        {
            ValueAndColor vc = levelColors[i];
            Color awt = toColorAwt( new float[] { vc.r, vc.g, vc.b, vc.a } );
            RGBtoHSB( awt.getRed( ), awt.getGreen( ), awt.getBlue( ), newColors[i] );

            // Shift to add the elevation value
            newColors[i][3] = newColors[i][2];
            newColors[i][2] = newColors[i][1];
            newColors[i][1] = newColors[i][0];
            newColors[i][0] = vc.v;
        }

        shadedReliefProgram.setColors( newColors );
    }

    @Override
    protected DrawableTexture[] loadTileData( TileKey key )
    {
        CachedTileData tile = tileCache.readOrBuildTile( ( ReliefTileKey ) key );

        FloatTextureProjected2D shadeTexture = new FloatTextureProjected2D( tile.numLon, tile.numLat );
        FloatTextureProjected2D elevationTexture = new FloatTextureProjected2D( tile.numLon, tile.numLat );

        shadeTexture.setProjection( getProjection( projection, tile ) );
        elevationTexture.setProjection( getProjection( projection, tile ) );

        tile.shaded.rewind( );
        tile.elevation.rewind( );

        shadeTexture.mutate( ( data, sizeX, sizeY ) -> data.put( tile.shaded.asFloatBuffer( ) ) );
        elevationTexture.mutate( ( data, sizeX, sizeY ) -> data.put( tile.elevation.asFloatBuffer( ) ) );

        return new DrawableTexture[] { shadeTexture, elevationTexture };
    }

    /**
     * Gets the texture projection for a particular tile.
     */
    protected LatLonProjection getProjection( GeoProjection projection, CachedTileData data )
    {
        double endLat = data.startLat_DEG + data.latStep_DEG * data.numLat;
        double endLon = data.startLon_DEG + data.lonStep_DEG * data.numLon;

        /*
         * Topo data is ordered top-to-bottom, so we flip our Latitude extents
         */
        return new LatLonProjection( projection, clampNorthSouth( endLat ), clampNorthSouth( data.startLat_DEG ), clampAntiMeridian( data.startLon_DEG ), clampAntiMeridian( endLon ), false );
    }

    @Override
    protected void replaceTileData( Collection<Entry<TileKey, DrawableTexture[]>> tileData )
    {
        topoImagePainter.removeAllDrawableTextures( );
        for ( Entry<TileKey, DrawableTexture[]> e : tileData )
        {
            topoImagePainter.addDrawableTexture( e.getValue( )[0], HILLSHADE_TEXTURE_UNIT );
            topoImagePainter.addNonDrawableTexture( e.getValue( )[0], e.getValue( )[1], ELEVATION_TEXTURE_UNIT );
        }
    }

    @Override
    protected Collection<TileKey> allKeys( )
    {
        return tileCache.getAllTileKeys( );
    }
}
