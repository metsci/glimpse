/*
 * Copyright (c) 2019 Metron, Inc.
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
package com.metsci.glimpse.layers.misc;

import static java.lang.String.format;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.plot.MultiAxisPlot2D.AxisInfo;
import com.metsci.glimpse.util.var.Disposable;

public class CompositeCursorLabelPainter extends CursorLabelPainter
{

    protected String xLabel;
    protected String yLabel;
    protected String xyNumberFormat;
    protected boolean xyNumberCleanup;
    protected final CopyOnWriteArrayList<Function<Axis2D,String>> extraTextFns;


    public CompositeCursorLabelPainter( )
    {
        this.xLabel = "X";
        this.yLabel = "Y";
        this.xyNumberFormat = "% .6g";
        this.xyNumberCleanup = true;
        this.extraTextFns = new CopyOnWriteArrayList<>( );
    }

    @Override
    protected String getCursorText( Axis2D axis )
    {
        StringBuilder s = new StringBuilder( );
        s.append( "<html>" );

        String xValue = format( this.xyNumberFormat, axis.getAxisX( ).getSelectionCenter( ) );
        String yValue = format( this.xyNumberFormat, axis.getAxisY( ).getSelectionCenter( ) );
        if ( this.xyNumberCleanup )
        {
            xValue = cleanUpNumberString( xValue );
            yValue = cleanUpNumberString( yValue );
        }
        s.append( this.xLabel ).append( ":" ).append( xValue ).append( "<br>" );
        s.append( this.yLabel ).append( ":" ).append( yValue ).append( "<hr>" );

        for ( Function<Axis2D,String> textFn : this.extraTextFns )
        {
            String line = textFn.apply( axis );
            if ( line != null )
            {
                s.append( line ).append( "<br>" );
            }
        }

        // Drop the trailing <br> or <hr>
        s.setLength( s.length( ) - 4 );

        s.append( "</html>" );
        return s.toString( );
    }

    public static String cleanUpNumberString( String s )
    {
        // The regex below matches the following sequence:
        //   1. a decimal point
        //   2. one or more digits
        //   3. zero or more zeros
        //   4. a non-digit, or end-of-string
        //
        // Then we replace #3 (the trailing zeros) with the empty string.
        //
        // Examples:
        //
        //    1.000       ->  1.0
        //    1.23000     ->  1.23
        //    1.000e-5    ->  1.0e-5
        //    1.23000e-5  ->  1.23e-5
        //
        return s.replaceAll( "(?<=\\.[0-9]{1,2147483646})0+(?=[^0-9]|$)", "" );
    }

    public void setXYLabels( AxisInfo xAxisInfo, AxisInfo yAxisInfo )
    {
        this.setXYLabels( cursorTextAxisLabel( xAxisInfo, "X" ),
                          cursorTextAxisLabel( yAxisInfo, "Y" ) );
    }

    public static String cursorTextAxisLabel( AxisInfo axisInfo, String fallbackLabel )
    {
        // Could show axis units as well, but it ends up too busy looking
        String label = axisInfo.getTickHandler( ).getAxisLabel( );
        return ( label == null || label.isEmpty( ) ? fallbackLabel : label );
    }

    public void setXYLabels( String xLabel, String yLabel )
    {
        this.setXLabel( xLabel );
        this.setYLabel( yLabel );
    }

    public void setXLabel( String xLabel )
    {
        this.xLabel = xLabel;
    }

    public void setYLabel( String yLabel )
    {
        this.yLabel = yLabel;
    }

    public void setXYNumberFormat( String format )
    {
        this.setXYNumberFormat( format, true );
    }

    public void setXYNumberFormat( String format, boolean cleanup )
    {
        this.xyNumberFormat = format;
        this.xyNumberCleanup = cleanup;
    }

    public Disposable addTextFn( Function<Axis2D,String> textFn )
    {
        this.extraTextFns.add( textFn );
        return ( ( ) -> this.extraTextFns.remove( textFn ) );
    }

}
