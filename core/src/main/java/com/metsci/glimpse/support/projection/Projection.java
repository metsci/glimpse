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
package com.metsci.glimpse.support.projection;

/**
 * Provides a mapping from a texture coordinate to the lower left
 * corner of the polygon which it should be mapped onto.
 *
 * The mapping should provide values for at least one texture coordinate
 * beyond the edge of the texture, since these define the top and right
 * side of the polygons on the edge of the texture.
 *
 * @author ulman
 */
public interface Projection
{
    /**
     * Given a position in a rectangular array of data (the texture), return the
     * corresponding location in physical units. The position is given as relative
     * coordinates, with 0.0 indicating the 0th data index and 1.0 indicating the
     * last data index.<p>
     *
     * This method computes the physical coordinate of the data at (textureFractionX,
     * textureFractionY), and stores them in the resultXY output parameter.<p>
     *
     * @param dataFractionX a relative x index (from 0.0 to 1.0) into the data array
     * @param dataFractionY a relative y index (from 0.0 to 1.0) into the data array
     *
     * @param resultXY output parameter to be populated with physical coords of the data
     */
    public void getVertexXY( double dataFractionX, double dataFractionY, float[] resultXY );

    /**
     * Given a position in a rectangular array of data (the texture), return the
     * corresponding location in physical units. The position is given as relative
     * coordinates, with 0.0 indicating the 0th data index and 1.0 indicating the
     * last data index.<p>
     *
     * This method computes the physical coordinate of the data at (textureFractionX,
     * textureFractionY), and stores them in the resultXYZ output parameter.<p>
     *
     * Assigning z coordinates to vertices can be useful even for 2D graphics. For
     * example, it can be used together with z-clipping to avoid rendering some facets.
     *
     * @param dataFractionX a relative x index (from 0.0 to 1.0) into the data array
     * @param dataFractionY a relative y index (from 0.0 to 1.0) into the data array
     *
     * @param resultXYZ output parameter to be populated with physical coords of the data
     */
    public void getVertexXYZ( double dataFractionX, double dataFractionY, float[] resultXYZ );

    /**
     * Returns the number of quads in the x direction that the array of data (the texture)
     * should be broken into. If the projection is linear in the x direction (for example,
     * if the data/texture is simply being displayed on a rectangle) then only one quad is
     * needed.<p>
     *
     * For more complicated projections, the value returned essentially defines the
     * granularity or resolution of the projection (since the projection will ultimately
     * be approximated when displayed by drawing a set of small quads).
     *
     * @param textureSizeX the length of the data array (the texture) in the x direction
     * @return the number of quads in the x direction to draw
     */
    public int getSizeX( int textureSizeX );

    /**
     * @see #getSizeX( int )
     *
     * @param textureSizeY the length of the data array (the texture) in the y direction
     * @return the number of quads in the y direction to draw
     */
    public int getSizeY( int textureSizeY );
}
