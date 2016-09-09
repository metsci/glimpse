package com.metsci.glimpse.examples.basic;

import com.metsci.glimpse.axis.AxisUtil;
import com.metsci.glimpse.axis.painter.TimeXAxisPainter;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.layout.GlimpseAxisLayoutX;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.plot.EmptyPlot2D;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.util.units.time.TimeStamp;

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

        /*
        GlimpseAxisLayoutY layoutY = new GlimpseAxisLayoutY( );
        AxisUtil.attachVerticalMouseListener( layoutY );
        plot.addLayout( layoutY );
        
        ColorRightYAxisPainter painter = new ColorRightYAxisPainter( new GridAxisLabelHandler( ) );
        ColorTexture1D texture = new ColorTexture1D( 1024 );
        texture.setColorGradient( ColorGradients.autumn );
        painter.setColorScale( texture );
        
        layoutY.addPainter( painter );
        */

        /*
        GlimpseAxisLayoutX layoutX = new GlimpseAxisLayoutX( );
        AxisUtil.attachHorizontalMouseListener( layoutX );
        plot.addLayout( layoutX );
        
        ColorXAxisPainter painter = new ColorXAxisPainter( new GridAxisLabelHandler( ) );
        ColorTexture1D texture = new ColorTexture1D( 1024 );
        texture.setColorGradient( ColorGradients.autumn );
        painter.setColorScale( texture );
        
        layoutX.addPainter( painter );
        */

        /*
        GlimpseAxisLayoutY layoutY = new GlimpseAxisLayoutY( );
        AxisUtil.attachVerticalMouseListener( layoutY );
        plot.addLayout( layoutY );
        layoutY.addPainter( new NumericRotatedYAxisPainter( new GridAxisLabelHandler( ) ) );
        */

        GlimpseAxisLayoutX layoutX = new GlimpseAxisLayoutX( );
        AxisUtil.attachHorizontalMouseListener( layoutX );
        plot.addLayout( layoutX );
        TimeXAxisPainter painter = new TimeXAxisPainter( new Epoch( TimeStamp.currentTime( ) ) );
        painter.setTickSize( 10 );
        layoutX.addPainter( painter );

        /*
        plot.addPainter( new NumericXYAxisPainter( ) );
        */

        return plot;
    }
}
