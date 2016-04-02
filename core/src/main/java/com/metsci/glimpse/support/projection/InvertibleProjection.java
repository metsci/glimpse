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
 * A complement interface {@link Projection} which
 * provides the inverse transform for invertible projections. Not all projections
 * are required to implement InvertibleProjection, but those that do may work
 * with additional painters.</p>
 *
 * For example, {@link com.metsci.glimpse.painter.info.CursorTextPainter} uses
 * {@code InvertibleProjection} to determine the data displayed at the current
 * mouse location.
 *
 * @author ulman
 *
 */
public interface InvertibleProjection extends Projection
{
    /**
     * Given a position in physical units, return the corresponding location
     * within a rectangular array of data. The corresponding location
     * is returned in relative coordinates, with 0.0 indicating the 0th data
     * index and 1.0 indicating the last data index.<p>
     *
     * This method returns the first (row) index of the data at (vertexX, vertexY).<p>
     *
     * @param vertexX a x coordinate in physical space
     * @param vertexY a y coordinate in physical space
     *
     * @return a relative index (from 0.0 to 1.0) into the data array
     */
    public double getTextureFractionX( double vertexX, double vertexY );

    /**
     * @see #getTextureFractionX( double, double )
     *
     * @param vertexX a x coordinate in physical space
     * @param vertexY a y coordinate in physical space
     * @return the relative y index (from 0.0 to 1.0) into the data array
     */
    public double getTextureFractionY( double vertexX, double vertexY );
}
