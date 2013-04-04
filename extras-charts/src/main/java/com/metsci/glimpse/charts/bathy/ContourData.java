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
package com.metsci.glimpse.charts.bathy;

import java.util.Arrays;

import com.metsci.glimpse.util.geo.projection.GeoProjection;

/**
 * @author ulman
 */
public class ContourData
{
    private RecordVertices contours;

    public ContourData( BathymetryData bathymetryData, GeoProjection tp, double[] levels )
    {
        // sort the levels array
        Arrays.sort( levels );

        contours = new RecordVertices( tp );

        try
        {
            Conrec contourCalculator = new Conrec( contours );
            double[][] bathyData = bathymetryData.getData( );
            int sizeX = bathyData.length;
            int sizeY = bathyData[0].length;
            contourCalculator.contour( bathyData, 0, sizeX - 1, 0, sizeY - 1, getLongitudes( bathymetryData ), getLatitudes( bathymetryData ), levels.length, levels );
        }
        catch ( Exception e1 )
        {
            e1.printStackTrace( );
        }
    }

    public float[] getCoordsX( )
    {
        return contours.getCoordsX( );
    }

    public float[] getCoordsY( )
    {
        return contours.getCoordsY( );
    }

    protected double[] getLatitudes( BathymetryData bathymetryDataSet )
    {
        double startLat = bathymetryDataSet.getStartLat( );
        double heightStep = bathymetryDataSet.getHeightStep( );
        int imageHeight = bathymetryDataSet.getImageHeight( );

        double[] latitudes = new double[imageHeight];

        for ( int i = 0; i < imageHeight; i++ )
        {
            latitudes[i] = startLat + ( i + 0.5 ) * heightStep;
        }

        return latitudes;
    }

    protected double[] getLongitudes( BathymetryData bathymetryDataSet )
    {
        double startLon = bathymetryDataSet.getStartLon( );
        double widthStep = bathymetryDataSet.getWidthStep( );
        int imageWidth = bathymetryDataSet.getImageWidth( );

        double[] longitudes = new double[imageWidth];

        for ( int i = 0; i < imageWidth; i++ )
        {
            longitudes[i] = startLon + ( i + 0.5 ) * widthStep;
        }

        return longitudes;
    }
}
