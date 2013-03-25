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
package com.metsci.glimpse.charts.raster;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.metsci.glimpse.charts.vector.MercatorProjection;
import com.metsci.glimpse.gl.texture.ColorTexture1D;
import com.metsci.glimpse.gl.texture.ColorTexture1D.MutatorColor1D;
import com.metsci.glimpse.support.projection.FlatProjection;
import com.metsci.glimpse.support.projection.GenericProjection;
import com.metsci.glimpse.support.projection.Projection;
import com.metsci.glimpse.support.texture.ByteTextureProjected2D;
import com.metsci.glimpse.support.texture.ByteTextureProjected2D.MutatorByte2D;
import com.metsci.glimpse.util.Pair;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.LatLonRect;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.math.stat.StatCollectorNDim;
import com.metsci.glimpse.util.vector.Vector2d;

import static com.metsci.glimpse.util.geo.datum.Datum.*;

/**
 * Data structures and data IO utilities for displaying Electronic Navigation Chart
 * raster images available in the BSB Raster format.<p>
 *
 * @author osborn
 * @see com.metsci.glimpse.examples.charts.rnc.RasterNavigationChartExample
 */
public final class BsbRasterData
{
    private final String _imageName;
    private final String _header;

    private final Set<Pair<IntPoint2d, LatLonGeo>> _registrationPoints;
    private final byte[] _imageData;
    private final IndexColorModel _colorModel;
    private final int _width_PIXELS;
    private final int _height_PIXELS;

    private BsbRasterData( String imageName, String header, int width_PIXELS, int height_PIXELS, byte[] imageData, IndexColorModel colorModel, Set<Pair<IntPoint2d, LatLonGeo>> registrationPoints )
    {
        this._imageName = imageName;
        this._header = header;

        this._imageData = imageData;
        this._colorModel = colorModel;
        this._width_PIXELS = width_PIXELS;
        this._height_PIXELS = height_PIXELS;
        this._registrationPoints = registrationPoints;
    }

    public static BsbRasterData readImage( InputStream in ) throws IOException
    {
        BufferedInputStream bis = new BufferedInputStream( in );
        DataInputStream dis = new DataInputStream( bis );

        String header = extractAsciiHeader( dis );
        int[] dim = extractDimension( header );
        IndexColorModel icm = extractColorModel( header );

        byte colorDepth = dis.readByte( );

        byte[] ucData = new byte[dim[0] * dim[1]];
        decodeImageData( dis, ucData, colorDepth, dim[1] );

        return new BsbRasterData( extractImageName( header ), header, dim[0], dim[1], ucData, icm, extractRegistrationPoints( header ) );
    }

    public IndexColorModel getColorModel( )
    {
        return _colorModel;
    }

    private static String extractAsciiHeader( DataInputStream stream ) throws IOException
    {
        StringBuilder builder = new StringBuilder( );

        byte thisByte = 0;
        byte lastByte = 0;

        while ( true )
        {
            if ( lastByte == 26 && thisByte == 0 )
            {
                break;
            }
            else
            {
                lastByte = thisByte;
                thisByte = stream.readByte( );
                builder.append( ( char ) thisByte );
            }
        }
        return builder.toString( );
    }

    private static Vector<Pair<String, String>> extractTokenData( String header, String tokenPattern )
    {
        Pattern pattern = Pattern.compile( "^\\w{3,}/", Pattern.MULTILINE );
        Matcher matcher = pattern.matcher( header );
        String[] items = pattern.split( header );

        Vector<Pair<String, String>> results = new Vector<Pair<String, String>>( );
        for ( int i = 1; i < items.length; i++ )
        {
            matcher.find( );
            if ( matcher.group( ).replace( "/", "" ).matches( tokenPattern ) )
            {
                results.add( new Pair<String, String>( matcher.group( ).replace( "/", "" ), items[i] ) );
            }
        }

        return results;
    }

    private static int[] extractDimension( String header )
    {
        Vector<Pair<String, String>> allTokenData = extractTokenData( header, "BSB" );
        String tokenData = allTokenData.get( 0 ).second( );

        Scanner s = new Scanner( tokenData );
        s.findWithinHorizon( "RA\\s*=\\s*(\\d+)\\s*,\\s*(\\d+)", tokenData.length( ) );
        MatchResult results = s.match( );

        int width_PIXELS = Integer.parseInt( results.group( 1 ) );
        int height_PIXELS = Integer.parseInt( results.group( 2 ) );

        s.close();
        return new int[] { width_PIXELS, height_PIXELS };
    }

    private static String extractImageName( String header )
    {
        Vector<Pair<String, String>> allTokenData = extractTokenData( header, "BSB" );
        String tokenData = allTokenData.get( 0 ).second( );

        Scanner s = new Scanner( tokenData );
        s.findWithinHorizon( "NA=([\\w;\\s]+)", tokenData.length( ) );
        
        String imageName = s.match( ).group( 1 );
        s.close();
        return imageName;
    }

    private static Set<Pair<IntPoint2d, LatLonGeo>> extractRegistrationPoints( String header )
    {
        Set<Pair<IntPoint2d, LatLonGeo>> refPoints = new HashSet<Pair<IntPoint2d, LatLonGeo>>( );

        Vector<Pair<String, String>> allTokenData = extractTokenData( header, "REF" );
        for ( Pair<String, String> tokenData : allTokenData )
        {
        	// No resource leak -- useDelimiter does not create a new instance. --ttran17
            @SuppressWarnings("resource")
			Scanner s = new Scanner( tokenData.second( ).replaceAll( "\r\n", "" ) ).useDelimiter( "," );

            s.nextInt( );
            int x = s.nextInt( );
            int y = s.nextInt( );
            double lat_DEG = s.nextDouble( );
            double lon_DEG = s.nextDouble( );

            refPoints.add( new Pair<IntPoint2d, LatLonGeo>( new IntPoint2d( x, y ), new LatLonGeo( lat_DEG, lon_DEG ) ) );
            s.close();
        }

        return refPoints;
    }

    private static IndexColorModel extractColorModel( String header )
    {
        Vector<Pair<String, String>> allTokenData = extractTokenData( header, "DAY" );

        byte[] r = new byte[16];
        byte[] g = new byte[16];
        byte[] b = new byte[16];
        for ( Pair<String, String> tokenData : allTokenData )
        {
            Scanner s = new Scanner( tokenData.second( ) );
            s.findWithinHorizon( "\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+),\\s*(\\d+)", tokenData.second( ).length( ) );
            MatchResult results = s.match( );

            int i = Integer.parseInt( results.group( 1 ) );
            int rd = Integer.parseInt( results.group( 2 ) );
            int gn = Integer.parseInt( results.group( 3 ) );
            int bl = Integer.parseInt( results.group( 4 ) );

            Color color = transform( new Color( rd, gn, bl ), 1.0f );

            r[i] = ( byte ) color.getRed( );
            g[i] = ( byte ) color.getGreen( );
            b[i] = ( byte ) color.getBlue( );
            
            s.close();
        }

        return new IndexColorModel( 8, 16, r, g, b );
    }

    private static final Color transform( Color color, double v )
    {
        int rd = ( int ) ( color.getRed( ) * v + ( 1 - v ) * 0 );
        int gn = ( int ) ( color.getGreen( ) * v + ( 1 - v ) * 0 );
        int bl = ( int ) ( color.getBlue( ) * v + ( 1 - v ) * 0 );

        return new Color( rd, gn, bl );
    }

    private static void decodeImageData( DataInputStream stream, byte[] cData, int numColorBits, int numRows ) throws IOException
    {
        byte colorMask = ( byte ) ( ( ( ( 1 << numColorBits ) ) - 1 ) << 7 - numColorBits );
        byte countMask = ( byte ) ( ( 1 << 7 - numColorBits ) - 1 );

        int nextByte = 0;
        int iPix = 0;
        for ( int i = 0; i < numRows; i++ )
        {
            readRowNumber( stream );
            while ( ( nextByte = stream.readUnsignedByte( ) ) != 0 )
            {
                byte colorValue = ( byte ) ( ( nextByte & colorMask ) >> ( 7 - numColorBits ) );
                int runLength = ( nextByte & countMask );

                while ( ( nextByte & 0x80 ) != 0 )
                {
                    nextByte = stream.readUnsignedByte( );
                    runLength = runLength * 128 + ( nextByte & 0x7f );
                }

                for ( int j = 0; j < runLength + 1; j++ )
                {
                    cData[iPix++] = colorValue;
                }
            }
        }
    }

    private static int readRowNumber( DataInputStream stream ) throws IOException
    {
        int nextByte;
        int lineNumber = 0;
        do
        {
            nextByte = stream.readUnsignedByte( );
            lineNumber = lineNumber * 128 + ( nextByte & 0x7f );
        }
        while ( ( nextByte & 0x80 ) != 0 );

        return lineNumber;
    }

    private double distance( double x1, double y1, double x2, double y2 )
    {
        double dx = x1 - x2;
        double dy = y1 - y2;

        return Math.sqrt( dx * dx + dy * dy );
    }

    public Projection getProjection( GeoProjection plane, MercatorProjection projection, int resolution )
    {
        FlatProjection flatProjection = getProjection( projection );

        double sizeX = flatProjection.getMaxX( ) - flatProjection.getMinX( );
        double sizeY = flatProjection.getMaxY( ) - flatProjection.getMinY( );

        double minX = flatProjection.getMinX( );
        double minY = flatProjection.getMinY( );

        double[][] coordsX = new double[resolution][resolution];
        double[][] coordsY = new double[resolution][resolution];

        for ( int x = 0; x < resolution; x++ )
        {
            for ( int y = 0; y < resolution; y++ )
            {
                double fracX = ( ( double ) x / ( double ) ( resolution - 1 ) );
                double fracY = ( ( double ) y / ( double ) ( resolution - 1 ) );

                double valX = minX + fracX * sizeX;
                double valY = minY + fracY * sizeY;

                LatLonGeo geo = projection.unproject( valX, valY );
                Vector2d planePoint = plane.project( geo );

                coordsX[x][y] = planePoint.getX( );
                coordsY[x][y] = planePoint.getY( );
            }
        }

        return new GenericProjection( coordsX, coordsY );
    }

    public LatLonGeo estimateCenterLatLon( )
    {
        if ( _registrationPoints.size( ) == 0 ) return null;

        StatCollectorNDim center = new StatCollectorNDim( 3 );
        for ( Pair<IntPoint2d, LatLonGeo> pair : _registrationPoints )
        {
            LatLonRect posit = pair.second( ).toLatLonRect( wgs84sphere );
            center.addElement( new double[] { posit.getX( ), posit.getY( ), posit.getZ( ) } );
        }

        double[] centerXYZ = center.getMean( );
        LatLonRect centerLLR = LatLonRect.fromXyz( centerXYZ[0], centerXYZ[1], centerXYZ[2] );

        return centerLLR.toLatLonGeo( wgs84sphere );
    }

    public FlatProjection getProjection( MercatorProjection projection )
    {
        if ( _registrationPoints == null || _registrationPoints.isEmpty( ) ) return null;

        Pair<IntPoint2d, LatLonGeo> point1 = _registrationPoints.iterator( ).next( );
        Pair<IntPoint2d, LatLonGeo> point2 = null;
        double maxDistance = Double.NEGATIVE_INFINITY;

        for ( Pair<IntPoint2d, LatLonGeo> pair : _registrationPoints )
        {
            if ( pair.first( ).x != point1.first( ).x && pair.first( ).y != point1.first( ).y )
            {
                double distance = distance( pair.first( ).x, pair.first( ).y, point1.first( ).x, point1.first( ).y );

                if ( distance > maxDistance )
                {
                    maxDistance = distance;
                    point2 = pair;
                    break;
                }
            }
        }

        if ( point2 == null ) return null;

        LatLonGeo latlon1 = point1.second( );
        LatLonGeo latlon2 = point2.second( );

        double x1 = point1.first( ).x;
        double x2 = point2.first( ).x;
        double y1 = point1.first( ).y;
        double y2 = point2.first( ).y;

        Vector2d projected1 = projection.project( latlon1 );
        Vector2d projected2 = projection.project( latlon2 );

        double pixelDiffX = Math.abs( x1 - x2 );
        double projDiffX = Math.abs( projected1.getX( ) - projected2.getX( ) );
        double pixelToProjX = pixelDiffX / projDiffX;

        double pixelDiffY = Math.abs( y1 - y2 );
        double projDiffY = Math.abs( projected1.getY( ) - projected2.getY( ) );
        double pixelToProjY = pixelDiffY / projDiffY;

        double minX = projected1.getX( ) - x1 / pixelToProjX;
        double maxX = projected1.getX( ) + ( _width_PIXELS - x1 ) / pixelToProjX;
        double minY = projected1.getY( ) + y1 / pixelToProjY;
        double maxY = projected1.getY( ) - ( _height_PIXELS - y1 ) / pixelToProjY;

        return new FlatProjection( minX, maxX, minY, maxY );
    }

    public final BufferedImage generateBufferedImage( )
    {
        // Create a data buffer using the byte buffer of pixel data.
        // The pixel data is not copied; the data buffer uses the byte buffer array.
        DataBuffer dbuf = new DataBufferByte( _imageData, _width_PIXELS * _height_PIXELS, 0 );

        // The number of banks should be 1
        dbuf.getNumBanks( ); // 1

        // Prepare a sample model that specifies a storage 4-bits of
        // pixel datavd in an 8-bit data element
        int[] bitMasks = new int[] { ( byte ) 0xf };
        SampleModel sampleModel = new SinglePixelPackedSampleModel( DataBuffer.TYPE_BYTE, _width_PIXELS, _height_PIXELS, bitMasks );

        // Create a raster using the sample model and data buffer
        WritableRaster raster = Raster.createWritableRaster( sampleModel, dbuf, null );

        // Combine the color model and raster into a buffered image
        return new BufferedImage( _colorModel, raster, true, null );
    }

    public final String getName( )
    {
        return _imageName;
    }

    public final String getHeader( )
    {
        return _header;
    }

    public final Set<Pair<IntPoint2d, LatLonGeo>> getRegistrationPoints( )
    {
        return _registrationPoints;
    }

    public final ColorTexture1D getColorTexture( )
    {
        ColorTexture1D texture = new ColorTexture1D( 16 );

        texture.mutate( new MutatorColor1D( )
        {
            @Override
            public void mutate( FloatBuffer floatBuffer, int dim )
            {
                for ( int i = 0; i < dim; i++ )
                {
                    floatBuffer.put( _colorModel.getRed( i ) / 255.0f );
                    floatBuffer.put( _colorModel.getGreen( i ) / 255.0f );
                    floatBuffer.put( _colorModel.getBlue( i ) / 255.0f );
                    floatBuffer.put( _colorModel.getAlpha( i ) / 255.0f );
                }
            }
        } );

        return texture;
    }

    public final ByteTextureProjected2D getDataTexture( )
    {
        ByteTextureProjected2D texture = new ByteTextureProjected2D( _width_PIXELS, _height_PIXELS );

        texture.mutate( new MutatorByte2D( )
        {
            @Override
            public void mutate( ByteBuffer data, int dataSizeX, int dataSizeY )
            {
                for ( int y = 0; y < dataSizeY; y++ )
                {
                    for ( int x = 0; x < dataSizeX; x++ )
                    {
                        data.put( _imageData[x + dataSizeX * y] );
                    }
                }
            }
        } );

        return texture;
    }
}
