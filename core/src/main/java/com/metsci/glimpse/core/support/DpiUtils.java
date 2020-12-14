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
package com.metsci.glimpse.core.support;

import java.awt.AWTError;
import java.awt.HeadlessException;
import java.awt.Toolkit;

/**
 * A collection of methods to help with DPI-aware layouts and sizings.
 */
public class DpiUtils
{
    public static final String DEFAULT_DPI_KEY = "com.metsci.glimpse.dpi.default";
    private static final int dpiDefaultPropertyValue = Integer.parseInt( System.getProperty( DEFAULT_DPI_KEY, "-1" ) );

    /**
     * The cached desktop DPI value after initial lookup resolves it in {@link #getDefaultDpi()}.
     */
    private static int resolvedDesktopDpi = -1;

    public static int adjustForDesktopScaling( int px )
    {
        return ( int ) adjustForDesktopScaling( ( float ) px );
    }

    public static float adjustForDesktopScaling( float px )
    {
        return px * getDefaultDpi( ) / 96;
    }

    /**
     * Get the default pixels-per-inch value. This can be affected in various
     * ways on Linux and Windows, primarily through adjustable text-scaling.
     */
    public static int getDefaultDpi( )
    {
        if ( 0 < dpiDefaultPropertyValue )
        {
            return dpiDefaultPropertyValue;
        }

        if ( 0 < resolvedDesktopDpi )
        {
            return resolvedDesktopDpi;
        }

        try
        {
            // Works for GTK font-scaling and getting Toolkit.getScreenResolution gives screen DPI
            Object dpiProp = Toolkit.getDefaultToolkit( ).getDesktopProperty( "gnome.Xft/DPI" );
            if ( dpiProp instanceof Number )
            {
                // Don't know why it's multiplied by 1024
                int dpi = ( ( Number ) dpiProp ).intValue( ) / 1024;
                resolvedDesktopDpi = dpi;
                return resolvedDesktopDpi;
            }
        }
        catch ( AWTError ex )
        {
            // ignore
        }

        try
        {
            // Windows apparently tells applications that the screen DPI was changed
            resolvedDesktopDpi = Toolkit.getDefaultToolkit( ).getScreenResolution( );
            return resolvedDesktopDpi;
        }
        catch ( HeadlessException ex )
        {
            // ignore
        }

        // Default is typically 96
        resolvedDesktopDpi = 96;
        return resolvedDesktopDpi;
    }
}
