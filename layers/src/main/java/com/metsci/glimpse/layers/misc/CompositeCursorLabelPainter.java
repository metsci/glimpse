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
