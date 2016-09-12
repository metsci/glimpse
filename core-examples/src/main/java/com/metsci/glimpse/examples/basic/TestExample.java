package com.metsci.glimpse.examples.basic;

import com.metsci.glimpse.axis.painter.label.GridAxisLabelHandler;
import com.metsci.glimpse.axis.tagged.Tag;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.axis.tagged.TaggedAxisMouseListener1D;
import com.metsci.glimpse.axis.tagged.painter.TaggedPartialColorYAxisPainter;
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
        
        TaggedPartialColorXAxisPainter painter = new TaggedPartialColorXAxisPainter( new GridAxisLabelHandler( ) );
        
        painter.setColorBarSize( 100 );
        
        TaggedAxis1D axis = new TaggedAxis1D( );
        axis.addTag( "T1", 0.0 ).setAttribute( Tag.TEX_COORD_ATTR, 0.0f );
        axis.addTag( "T2", 1.0 ).setAttribute( Tag.TEX_COORD_ATTR, 0.3f );
        axis.addTag( "T3", 2.0 ).setAttribute( Tag.TEX_COORD_ATTR, 0.8f );
        axis.addTag( "T4", 3.0 ).setAttribute( Tag.TAG_COLOR_ATTR, GlimpseColor.getRed( ) ).setAttribute( Tag.TEX_COORD_ATTR, 1.0f );
        
        axis.addConstraint( new OrderedConstraint( "Order", Arrays.asList( "T1", "T2", "T3", "T4" ) ) );
        
        layoutX.setAxis( axis );
        
        ColorTexture1D texture = new ColorTexture1D( 1024 );
        texture.setColorGradient( ColorGradients.prism );
        painter.setColorScale( texture );
        
        layoutX.addGlimpseMouseAllListener( new TaggedAxisMouseListener1D( ) );
        plot.addLayout( layoutX );
        layoutX.addPainter( painter );
        */

        GlimpseAxisLayoutY layoutY = new GlimpseAxisLayoutY( );

        TaggedPartialColorYAxisPainter painter = new TaggedPartialColorYAxisPainter( new GridAxisLabelHandler( ) );

        TaggedAxis1D axis = new TaggedAxis1D( );
        axis.addTag( "T1", 0.0 ).setAttribute( Tag.TEX_COORD_ATTR, 0.0f );
        axis.addTag( "T2", 1.0 ).setAttribute( Tag.TEX_COORD_ATTR, 0.9f );
        axis.addTag( "T3", 2.0 ).setAttribute( Tag.TEX_COORD_ATTR, 1.0f ).setAttribute( Tag.TAG_COLOR_ATTR, GlimpseColor.getRed( ) );
        layoutY.setAxis( axis );

        ColorTexture1D texture = new ColorTexture1D( 1024 );
        texture.setColorGradient( ColorGradients.viridis );
        painter.setColorScale( texture );

        layoutY.addGlimpseMouseAllListener( new TaggedAxisMouseListener1D( ) );
        plot.addLayout( layoutY );
        layoutY.addPainter( painter );

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
