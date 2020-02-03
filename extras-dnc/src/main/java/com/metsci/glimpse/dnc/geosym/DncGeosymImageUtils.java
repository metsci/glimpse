/*
 * Copyright (c) 2019, Metron, Inc.
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
package com.metsci.glimpse.dnc.geosym;

import static com.metsci.glimpse.util.GeneralUtils.ints;
import static com.metsci.glimpse.util.units.Length.millimetersToInches;
import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.lang.Math.max;
import static java.lang.Math.round;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGException;
import com.kitfox.svg.SVGUniverse;
import com.metsci.glimpse.dnc.util.AnchoredImage;

public class DncGeosymImageUtils
{

    /**
     * The SVG coordinates were generated in 1/100th pixel units assuming this DPI.
     */
    public static final double geosymSvgDpi = 300;


    public static final Color transparentWhite = new Color( 255, 255, 255, 0 );


    public static interface TextLoader
    {
        String loadText( ) throws IOException;
    }


    public static interface KeyedTextLoader
    {
        String loadTextFor( String key ) throws IOException;
    }


    public static AnchoredImage loadGeosymImage( String symbolId, KeyedTextLoader cgmLoader, KeyedTextLoader svgLoader, double screenDpi ) throws IOException, SVGException
    {
        String cgmText = cgmLoader.loadTextFor( symbolId );
        int[] cgmBounds = extractCgmBounds( cgmText, symbolId );
        double cgmScale = extractCgmScale( cgmText, symbolId );

        String svgText = removeSvgTransparency( svgLoader.loadTextFor( symbolId ) );
        String svgName = "geosym-" + symbolId;
        SVGUniverse svgUniverse = new SVGUniverse( );
        SVGDiagram svgDiagram = svgUniverse.getDiagram( svgUniverse.loadSVG( new StringReader( svgText ), svgName ) );
        svgDiagram.setIgnoringClipHeuristic( true );

        // cgm-units * cgm-scale gives millimeters, and inches * screen-dpi gives screen-pixels
        double cgmUnitsToScreenPixels = cgmScale * millimetersToInches * screenDpi;
        double svgUnitsToScreenPixels = screenDpi / geosymSvgDpi;
        int iAnchor = ( int ) round( -cgmBounds[ 0 ] * cgmUnitsToScreenPixels );
        int jAnchor = ( int ) round( -cgmBounds[ 1 ] * cgmUnitsToScreenPixels );
        int width = max( 1, ( int ) round( svgDiagram.getWidth( ) * svgUnitsToScreenPixels ) );
        int height = max( 1, ( int ) round( svgDiagram.getHeight( ) * svgUnitsToScreenPixels ) );

        BufferedImage image = new BufferedImage( width, height, TYPE_INT_ARGB );
        Graphics2D g = image.createGraphics( );
        try
        {
            g.setBackground( transparentWhite );
            g.clearRect( 0, 0, width, height );
            g.setRenderingHint( KEY_ANTIALIASING, VALUE_ANTIALIAS_ON );
            g.scale( svgUnitsToScreenPixels, svgUnitsToScreenPixels );
            svgDiagram.render( g );
            return new AnchoredImage( image, iAnchor, jAnchor );
        }
        finally
        {
            g.dispose( );
        }
    }

    public static final Pattern cgmBoundsPattern = Pattern.compile( "VDCEXT\\s([\\+|-]?\\d+)\\s([\\+|-]?\\d+)\\s([\\+|-]?\\d+)\\s([\\+|-]?\\d+);" );

    public static int[] extractCgmBounds( String cgmText, String symbolId ) throws SVGException
    {
        Matcher m = cgmBoundsPattern.matcher( cgmText );
        if ( m.find( ) )
        {
            return ints( parseInt( m.group( 1 ).trim( ) ),
                         parseInt( m.group( 2 ).trim( ) ),
                         parseInt( m.group( 3 ).trim( ) ),
                         parseInt( m.group( 4 ).trim( ) ) );
        }
        else
        {
            throw new SVGException( "Failed to parse CGM bounds: symbol-id = " + symbolId );
        }
    }

    public static final Pattern cgmScalePattern = Pattern.compile( "SCALEMODE\\sMETRIC\\s(\\d*\\.?\\d*);" );

    public static double extractCgmScale( String cgmText, String symbolId ) throws SVGException
    {
        Matcher m = cgmScalePattern.matcher( cgmText );
        if ( m.find( ) )
        {
            return parseDouble( m.group( 1 ) );
        }
        else
        {
            throw new SVGException( "Failed to parse CGM scale: symbol-id = " + symbolId );
        }
    }

    public static String removeSvgTransparency( String svgText )
    {
        return svgText.replaceAll( "fill-opacity=\"\\d*\\.?\\d*\"", "" ).replaceAll( "stroke-opacity=\"\\d*\\.?\\d*\"", "" );
    }

}
