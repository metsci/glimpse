package com.metsci.glimpse.examples.basic;

import com.metsci.glimpse.axis.AxisUtil;
import com.metsci.glimpse.axis.painter.label.GridAxisLabelHandler;
import com.metsci.glimpse.axis.tagged.Tag;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.axis.tagged.painter.TaggedColorYAxisPainter;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.gl.texture.ColorTexture1D;
import com.metsci.glimpse.layout.GlimpseAxisLayoutY;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.plot.EmptyPlot2D;
import com.metsci.glimpse.support.color.GlimpseColor;
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

        /*
        GlimpseAxisLayoutX layoutX = new GlimpseAxisLayoutX( );
        
        TaggedColorXAxisPainter painter = new TaggedColorXAxisPainter( new GridAxisLabelHandler( ) );
        
        TaggedAxis1D axis = new TaggedAxis1D( );
        axis.addTag( "T1", 0.0 );
        axis.addTag( "T2", 1.0 );
        axis.addTag( "T3", 2.0 ).setAttribute( Tag.TAG_COLOR_ATTR, GlimpseColor.getRed( ) );
        layoutX.setAxis( axis );
        
        ColorTexture1D texture = new ColorTexture1D( 1024 );
        texture.setColorGradient( ColorGradients.prism );
        painter.setColorScale( texture );
        
        AxisUtil.attachHorizontalMouseListener( layoutX );
        plot.addLayout( layoutX );
        layoutX.addPainter( painter );
        */

        GlimpseAxisLayoutY layoutY = new GlimpseAxisLayoutY( );

        TaggedColorYAxisPainter painter = new TaggedColorYAxisPainter( new GridAxisLabelHandler( ) );

        TaggedAxis1D axis = new TaggedAxis1D( );
        axis.addTag( "T1", 0.0 );
        axis.addTag( "T2", 1.0 );
        axis.addTag( "T3", 2.0 ).setAttribute( Tag.TAG_COLOR_ATTR, GlimpseColor.getRed( ) );
        layoutY.setAxis( axis );

        ColorTexture1D texture = new ColorTexture1D( 1024 );
        texture.setColorGradient( ColorGradients.viridis );
        painter.setColorScale( texture );

        AxisUtil.attachVerticalMouseListener( layoutY );
        plot.addLayout( layoutY );
        layoutY.addPainter( painter );

        /*
        GlimpseAxisLayoutX layoutX = new GlimpseAxisLayoutX( );
        AxisUtil.attachHorizontalMouseListener( layoutX );
        plot.addLayout( layoutX );
        TimeXAxisPainter painter = new TimeXAxisPainter( new Epoch( TimeStamp.currentTime( ) ) );
        painter.setTickSize( 10 );
        layoutX.addPainter( painter );
        */

        /*
        GlimpseAxisLayoutY layoutY = new GlimpseAxisLayoutY( );
        AxisUtil.attachVerticalMouseListener( layoutY );
        plot.addLayout( layoutY );
        TimeYAxisPainter painter = new TimeYAxisPainter( new Epoch( TimeStamp.currentTime( ) ) );
        layoutY.addPainter( painter );
        */

        /*
        plot.addPainter( new NumericXYAxisPainter( ) );
        */

        return plot;
    }
}
