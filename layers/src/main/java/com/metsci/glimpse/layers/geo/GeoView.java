package com.metsci.glimpse.layers.geo;

import static com.metsci.glimpse.docking.DockingUtils.requireIcon;
import static com.metsci.glimpse.layers.geo.GeoTrait.requireGeoTrait;
import static java.lang.String.format;

import java.awt.Component;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import javax.media.opengl.GLAutoDrawable;
import javax.swing.Icon;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener1D;
import com.metsci.glimpse.layers.View;
import com.metsci.glimpse.layers.misc.CursorLabelPainter;
import com.metsci.glimpse.painter.decoration.BorderPainter;
import com.metsci.glimpse.painter.decoration.CrosshairPainter;
import com.metsci.glimpse.painter.decoration.GridPainter;
import com.metsci.glimpse.painter.group.DelegatePainter;
import com.metsci.glimpse.plot.MultiAxisPlot2D;
import com.metsci.glimpse.plot.MultiAxisPlot2D.AxisInfo;
import com.metsci.glimpse.support.swing.NewtSwingEDTGlimpseCanvas;
import com.metsci.glimpse.util.var.Disposable;

public class GeoView extends View
{

    public final NewtSwingEDTGlimpseCanvas canvas;

    public final MultiAxisPlot2D plot;
    public final GridPainter gridPainter;
    public final DelegatePainter dataPainter;
    public final CrosshairPainter crosshairPainter;
    public final BorderPainter borderPainter;

    protected String xyCursorTextNumberFormat;
    protected boolean xyCursorTextNumberCleanup;
    protected final CopyOnWriteArrayList<Function<Axis2D,String>> cursorTextFns;
    protected final CursorLabelPainter cursorTextPainter;


    public GeoView( )
    {
        this.title.set( "Geo" );

        this.plot = new MultiAxisPlot2D( );
        this.plot.getCenterAxis( ).lockAspectRatioXY( 1.0 );
        Axis1D xAxis = this.plot.getCenterAxisX( );
        Axis1D yAxis = this.plot.getCenterAxisY( );
        AxisInfo xAxisInfo = this.plot.createAxisBottom( "xBottom", xAxis, new AxisMouseListener1D( ) );
        AxisInfo yAxisInfo = this.plot.createAxisLeft( "yLeft", yAxis, new AxisMouseListener1D( ) );

        this.gridPainter = new GridPainter( xAxisInfo.getTickHandler( ), yAxisInfo.getTickHandler( ) );
        this.plot.addPainter( this.gridPainter );

        this.dataPainter = new DelegatePainter( );
        this.plot.addPainter( this.dataPainter );

        this.crosshairPainter = new CrosshairPainter( );
        this.plot.addPainter( this.crosshairPainter );

        this.xyCursorTextNumberFormat = "% .6g";
        this.xyCursorTextNumberCleanup = true;
        this.cursorTextFns = new CopyOnWriteArrayList<>( );
        this.cursorTextPainter = new CursorLabelPainter( ( axis ) ->
        {
            StringBuilder s = new StringBuilder( );
            s.append( "<html>" );

            String xLabel = cursorTextAxisLabel( xAxisInfo, "X" );
            String yLabel = cursorTextAxisLabel( yAxisInfo, "Y" );
            String xValue = format( xyCursorTextNumberFormat, axis.getAxisX( ).getSelectionCenter( ) );
            String yValue = format( xyCursorTextNumberFormat, axis.getAxisY( ).getSelectionCenter( ) );
            if ( this.xyCursorTextNumberCleanup )
            {
                xValue = cleanUpNumberString( xValue );
                yValue = cleanUpNumberString( yValue );
            }
            s.append( xLabel ).append( ":" ).append( xValue ).append( "<br>" );
            s.append( yLabel ).append( ":" ).append( yValue ).append( "<hr>" );

            for ( Function<Axis2D,String> textFn : this.cursorTextFns )
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
        } );
        this.plot.addPainter( this.cursorTextPainter );

        this.borderPainter = new BorderPainter( );
        this.plot.addPainter( this.borderPainter );

        this.canvas = new NewtSwingEDTGlimpseCanvas( );
        this.canvas.addLayout( this.plot );
    }

    protected static String cursorTextAxisLabel( AxisInfo axisInfo, String fallbackLabel )
    {
        // Could show axis units as well, but it ends up too busy looking
        String label = axisInfo.getTickHandler( ).getAxisLabel( );
        return ( label == null || label.isEmpty( ) ? fallbackLabel : label );
    }

    protected static String cleanUpNumberString( String s )
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

    @Override
    public Icon getIcon( )
    {
        return requireIcon( "fugue-icons/map.png" );
    }

    @Override
    public Component getComponent( )
    {
        return this.canvas;
    }

    @Override
    public GLAutoDrawable getGLDrawable( )
    {
        return this.canvas.getGLDrawable( );
    }

    @Override
    public void init( )
    {
        GeoTrait geoTrait = requireGeoTrait( this );
        this.plot.getCenterAxis( ).setParent( geoTrait.axis );
    }

    @Override
    public GeoView copy( )
    {
        return new GeoView( );
    }

    @Override
    protected void dispose( )
    {
        super.dispose( );
        this.canvas.dispose( );
    }

    public void setCursorTextVisible( boolean visible )
    {
        this.cursorTextPainter.setVisible( visible );
    }

    public void setXYCursorTextNumberFormat( String format )
    {
        this.setXYCursorTextNumberFormat( format, true );
    }

    public void setXYCursorTextNumberFormat( String format, boolean cleanup )
    {
        this.xyCursorTextNumberFormat = format;
        this.xyCursorTextNumberCleanup = cleanup;
    }

    public Disposable addCursorTextFn( Function<Axis2D,String> textFn )
    {
        this.cursorTextFns.add( textFn );
        return ( ( ) -> this.cursorTextFns.remove( textFn ) );
    }

}
