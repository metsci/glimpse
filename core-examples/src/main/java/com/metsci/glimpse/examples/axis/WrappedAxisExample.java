package com.metsci.glimpse.examples.axis;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.WrappedAxis1D;
import com.metsci.glimpse.axis.painter.label.GridAxisLabelHandler;
import com.metsci.glimpse.axis.painter.label.WrappedLabelHandler;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.examples.basic.HeatMapExample;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.plot.ColorAxisPlot2D;

public class WrappedAxisExample implements GlimpseLayoutProvider
{
    public static void main( String[] args ) throws Exception
    {
        Example.showWithSwing( new WrappedAxisExample( ) );
    }

    @Override
    public GlimpseLayout getLayout( ) throws Exception
    {
        // create a plot from the heat map example, but with wrapped axes and modified painters
        HeatMapExample example = new HeatMapExample( )
        {
            @Override
            protected ColorAxisPlot2D newPlot( )
            {
                return new ColorAxisPlot2D( )
                {
                    /*
                    @Override
                    protected NumericAxisPainter createAxisPainterX( AxisLabelHandler tickHandler )
                    {
                        return new NumericXAxisPainter( tickHandler );
                    }

                    @Override
                    protected NumericAxisPainter createAxisPainterY( AxisLabelHandler tickHandler )
                    {
                        return new NumericXAxisPainter( tickHandler );
                    }
                    */

                    @Override
                    protected GridAxisLabelHandler createLabelHandlerX( )
                    {
                        return new WrappedLabelHandler( );
                    }

                    @Override
                    protected GridAxisLabelHandler createLabelHandlerY( )
                    {
                        return new WrappedLabelHandler( );
                    }

                    @Override
                    protected Axis1D createAxisX( )
                    {
                        return new WrappedAxis1D( 0, 1000 );
                    }

                    @Override
                    protected Axis1D createAxisY( )
                    {
                        return new WrappedAxis1D( 0, 1000 );
                    }
                };
            }
        };

        ColorAxisPlot2D plot = example.getLayout( );

        // don't let the user zoom out too far (especially important with wrapped axes
        // since this will cause the scene to be painted many times)
        plot.getAxis( ).getAxisX( ).setMaxSpan( 2000 );
        plot.getAxis( ).getAxisY( ).setMaxSpan( 2000 );
        
        return plot;
    }

}
