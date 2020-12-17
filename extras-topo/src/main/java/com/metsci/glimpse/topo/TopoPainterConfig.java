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

public class TopoPainterConfig
{

    public static final TopoPainterConfig topoPainterConfig_DEFAULT = new TopoPainterConfig( 2048, 2048, 1, 1, 1, true, false );


    public final int maxRowsPerBand;
    public final int maxColsPerTile;
    public final int hTileDisposalsPerFrame;
    public final int dTileDisposalsPerFrame;
    public final int tileXfersPerFrame;
    public final boolean preloadLowerResTiles;
    public final boolean preloadHigherResTiles;


    public TopoPainterConfig( int maxRowsPerBand,
                              int maxColsPerTile,
                              int hTileDisposalsPerFrame,
                              int dTileDisposalsPerFrame,
                              int tileXfersPerFrame,
                              boolean preloadLowerResTiles,
                              boolean preloadHigherResTiles )
    {
        this.maxRowsPerBand = maxRowsPerBand;
        this.maxColsPerTile = maxColsPerTile;
        this.hTileDisposalsPerFrame = hTileDisposalsPerFrame;
        this.dTileDisposalsPerFrame = dTileDisposalsPerFrame;
        this.tileXfersPerFrame = tileXfersPerFrame;
        this.preloadLowerResTiles = preloadLowerResTiles;
        this.preloadHigherResTiles = preloadHigherResTiles;
    }

    public String toLongString( String linePrefix )
    {
        StringBuilder s = new StringBuilder( );

        s.append( linePrefix ).append( "maxRowsPerBand:         " ).append( this.maxRowsPerBand ).append( "\n" );
        s.append( linePrefix ).append( "maxColsPerTile:         " ).append( this.maxColsPerTile ).append( "\n" );
        s.append( linePrefix ).append( "hTileDisposalsPerFrame: " ).append( this.hTileDisposalsPerFrame ).append( "\n" );
        s.append( linePrefix ).append( "dTileDisposalsPerFrame: " ).append( this.dTileDisposalsPerFrame ).append( "\n" );
        s.append( linePrefix ).append( "tileXfersPerFrame:      " ).append( this.tileXfersPerFrame ).append( "\n" );
        s.append( linePrefix ).append( "preloadLowerResTiles:   " ).append( this.preloadLowerResTiles ).append( "\n" );
        s.append( linePrefix ).append( "preloadHigherResTiles:  " ).append( this.preloadHigherResTiles ).append( "\n" );

        return s.toString( );
    }

}
