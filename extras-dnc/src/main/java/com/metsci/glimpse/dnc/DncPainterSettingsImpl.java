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
package com.metsci.glimpse.dnc;

import static com.google.common.collect.Lists.newArrayList;
import static com.metsci.glimpse.dnc.DncProjections.canProjectBrowse;
import static com.metsci.glimpse.painter.group.WrappedPainter.wrappedBoundsIterator;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.Collection;
import java.util.List;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.dnc.proj.DncProjection;
import com.metsci.glimpse.painter.group.WrappedPainter.WrappedTextureBounds;

public class DncPainterSettingsImpl implements DncPainterSettings
{

    protected final double ppvMultiplier;
    protected final boolean useBrowseLibrary;


    public DncPainterSettingsImpl( DncProjection proj )
    {
        this( proj, canProjectBrowse( proj ) );
    }

    public DncPainterSettingsImpl( DncProjection proj, boolean useBrowseLibrary )
    {
        this( proj.suggestedPpvMultiplier( ), useBrowseLibrary );
    }

    public DncPainterSettingsImpl( double ppvMultiplier, boolean useBrowseLibrary )
    {
        this.ppvMultiplier = ppvMultiplier;
        this.useBrowseLibrary = useBrowseLibrary;
    }

    @Override
    public boolean areAreasVisible( Axis2D axis )
    {
        return true;
    }

    @Override
    public boolean areLinesVisible( Axis2D axis )
    {
        // If we are using the browse library, then we can show its lines even when
        // zoomed way out, because they are coarse enough not to clutter things up.
        //
        // If we aren't using the browse library, then we stick with the general
        // library when zoomed way out. In such cases, we want only the polygons,
        // to keep things from looking cluttered.
        //
        return ( useBrowseLibrary || ppvMultiplier * ppv( axis ) >= 50 );
    }

    @Override
    public boolean areIconsVisible( Axis2D axis )
    {
        return ( ppvMultiplier * ppv( axis ) >= 150 );
    }

    @Override
    public boolean areLabelsVisible( Axis2D axis )
    {
        return ( ppvMultiplier * ppv( axis ) >= 300 );
    }

    @Override
    public float iconsGlobalScale( Axis2D axis )
    {
        double ppvMax = 10000;
        double scaleMax = 1.0;

        double ppvMin = 10;
        double scaleMin = 0.6;

        double ppv = ppvMultiplier * ppv( axis );
        double scale = scaleMin + ( ( scaleMax - scaleMin ) / ( ppvMax - ppvMin ) ) * ppv;
        return ( float ) min( scaleMax, max( scaleMin, scale ) );
    }

    @Override
    public boolean isLibraryActive( DncLibrary library, Axis2D axis )
    {
        // If we aren't using the browse library, then stick with
        // the general library when zoomed way out
        return isLibraryActive( library, axis, ppvMultiplier, ( useBrowseLibrary ? 50 : Double.MIN_VALUE ), 2000, 8000, 23000 );
    }

    public static boolean isLibraryPositionVisible( DncLibrary library, Axis2D axis )
    {
        Axis1D xAxis = axis.getAxisX( );
        Axis1D yAxis = axis.getAxisY( );

        // This works fine even for non-wrapped axes -- the bounds lists just have a single entry
        List<WrappedTextureBounds> xWrappedBounds = newArrayList( wrappedBoundsIterator( xAxis, xAxis.getSizePixels( ) ) );
        List<WrappedTextureBounds> yWrappedBounds = newArrayList( wrappedBoundsIterator( yAxis, yAxis.getSizePixels( ) ) );

        for ( WrappedTextureBounds xBounds : xWrappedBounds )
        {
            for ( WrappedTextureBounds yBounds : yWrappedBounds )
            {
                double xMin = xBounds.getStartValueWrapped( );
                double xMax = xBounds.getEndValueWrapped( );
                double yMin = yBounds.getStartValueWrapped( );
                double yMax = yBounds.getEndValueWrapped( );
                if ( xMin <= library.xMax && library.xMin <= xMax && yMin <= library.yMax && library.yMin <= yMax )
                {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isLibraryOfType( DncLibrary library, char type )
    {
        return ( Character.toUpperCase( library.libraryName.charAt( 0 ) ) == Character.toUpperCase( type ) );
    }

    public static boolean isLibraryActive( DncLibrary library, Axis2D axis, double ppvMultiplier, double bgThreshold, double gcThreshold, double caThreshold, double ahThreshold )
    {
        if ( !isLibraryPositionVisible( library, axis ) )
        {
            return false;
        }

        double ppvMin;
        double ppvMax;
        switch ( library.libraryName.charAt( 0 ) )
        {
            case 'B': case 'b':  ppvMin = Double.MIN_VALUE;  ppvMax = bgThreshold;       break;
            case 'G': case 'g':  ppvMin = bgThreshold;       ppvMax = gcThreshold;       break;
            case 'C': case 'c':  ppvMin = gcThreshold;       ppvMax = caThreshold;       break;
            case 'A': case 'a':  ppvMin = caThreshold;       ppvMax = ahThreshold;       break;
            case 'H': case 'h':  ppvMin = ahThreshold;       ppvMax = Double.MAX_VALUE;  break;
            default:             ppvMin = Double.MAX_VALUE;  ppvMax = Double.MIN_VALUE;  break;
        }

        double ppv = ppvMultiplier * ppv( axis );
        return ( ppvMin <= ppv && ppv < ppvMax );
    }

    @Override
    public boolean isLibraryActive( DncLibrary library, Collection<Axis2D> axes )
    {
        for ( Axis2D axis : axes )
        {
            if ( isLibraryActive( library, axis ) )
            {
                return true;
            }
        }
        return false;
    }

    public static double ppv( Axis2D axis )
    {
        return axis.getAxisX( ).getPixelsPerValue( );
    }

}
