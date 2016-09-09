package com.metsci.glimpse.examples.basic;

import com.metsci.glimpse.axis.AxisUtil;
import com.metsci.glimpse.axis.painter.ColorRightYAxisPainter;
import com.metsci.glimpse.axis.painter.label.GridAxisLabelHandler;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.gl.texture.ColorTexture1D;
import com.metsci.glimpse.layout.GlimpseAxisLayoutY;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.plot.EmptyPlot2D;
import com.metsci.glimpse.support.colormap.ColorGradients;

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

        ColorRightYAxisPainter painter = new ColorRightYAxisPainter( new GridAxisLabelHandler( ) );
        ColorTexture1D texture = new ColorTexture1D( 1024 );
        texture.setColorGradient( ColorGradients.autumn );
        painter.setColorScale( texture );

        layoutY.addPainter( painter );

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
        layoutY.addPainter( new NumericYAxisPainter( new GridAxisLabelHandler( ) ) );
        */

        /*
        plot.addPainter( new NumericXYAxisPainter( ) );
        */

        return plot;
    }
}
