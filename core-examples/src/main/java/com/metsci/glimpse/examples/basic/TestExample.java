package com.metsci.glimpse.examples.basic;

import com.metsci.glimpse.axis.AxisUtil;
import com.metsci.glimpse.axis.painter.NumericYAxisPainter;
import com.metsci.glimpse.axis.painter.label.GridAxisLabelHandler;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.layout.GlimpseAxisLayoutY;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.plot.EmptyPlot2D;

public class TestExample implements GlimpseLayoutProvider
{
    public static void main( String[] args ) throws Exception
    {
        Example.showWithSwing( new TestExample( ) );
    }

    @Override
    public GlimpseLayout getLayout( )
    {
        EmptyPlot2D plot = new EmptyPlot2D( );

        GlimpseAxisLayoutY layoutY = new GlimpseAxisLayoutY( );

        AxisUtil.attachVerticalMouseListener( layoutY );

        plot.addLayout( layoutY );

        layoutY.addPainter( new NumericYAxisPainter( new GridAxisLabelHandler( ) ) );

        return plot;
    }
}
