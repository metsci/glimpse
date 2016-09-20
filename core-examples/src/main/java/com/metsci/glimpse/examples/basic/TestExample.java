package com.metsci.glimpse.examples.basic;

import static com.metsci.glimpse.gl.util.GLUtils.enableStandardBlending;

import javax.media.opengl.GL3;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.painter.label.GridAxisLabelHandler;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.gl.util.GLUtils;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.painter.decoration.MapBorderPainter;
import com.metsci.glimpse.painter.plot.XYLinePainter;
import com.metsci.glimpse.plot.EmptyPlot2D;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.colormap.ColorGradients;
import com.metsci.glimpse.support.colormap.ColorMapLinear;
import com.metsci.glimpse.support.line.LineJoinType;
import com.metsci.glimpse.support.line.LineStyle;
import com.metsci.glimpse.support.shader.FlatColorProgram;
import com.metsci.glimpse.support.shader.GLStreamingBufferBuilder;

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

        /*
        GlimpseAxisLayoutY layoutY = new GlimpseAxisLayoutY( );
        
        TaggedPartialColorYAxisPainter painter = new TaggedPartialColorYAxisPainter( new GridAxisLabelHandler( ) );
        
        TaggedAxis1D axis = new TaggedAxis1D( );
        axis.addTag( "T1", 0.0 ).setAttribute( Tag.TEX_COORD_ATTR, 0.0f );
        axis.addTag( "T2", 1.0 ).setAttribute( Tag.TEX_COORD_ATTR, 0.9f );
        axis.addTag( "T3", 2.0 ).setAttribute( Tag.TEX_COORD_ATTR, 1.0f ).setAttribute( Tag.TAG_COLOR_ATTR, GlimpseColor.getRed( ) );
        layoutY.setAxis( axis );
        
        ColorTexture1D texture = new ColorTexture1D( 1024 );
        texture.setColorGradient( ColorGradients.prism );
        painter.setColorScale( texture );
        
        layoutY.addGlimpseMouseAllListener( new TaggedAxisMouseListener1D( ) );
        
        layoutY.addPainter( painter );
        plot.addLayout( layoutY );
        
        plot.addPainter( new FillPainter( ) );
        */

        plot.addPainter( new MapBorderPainter( new GridAxisLabelHandler( ), new GridAxisLabelHandler( ) ) );

        XYLinePainter xypainter = new XYLinePainter( );

        xypainter.setDataAndColor( new float[] { 0, 1, 2 }, new float[] { 0, 0.5f, 2.0f }, new float[] { 0, 0.5f, 0.3f }, new ColorMapLinear( 0, 1, ColorGradients.seismic ) );
        xypainter.setDataAndColor( new float[] { 0, 1, 2 }, new float[] { 0, 0.5f, 2.0f }, new float[][] { GlimpseColor.getBlue( ), GlimpseColor.getRed( ), GlimpseColor.getGreen( ) } );

        LineStyle style = new LineStyle( );
        style.thickness_PX = 10.0f;
        style.joinType = LineJoinType.JOIN_MITER;
        style.stippleEnable = true;
        style.stippleScale = 5.0f;

        xypainter.setLineStyle( style );

        plot.addPainter( xypainter );

        return plot;
    }

    public static class FillPainter extends GlimpsePainterBase
    {
        protected FlatColorProgram fillProg;
        protected GLStreamingBufferBuilder fillBuilder;

        public FillPainter( )
        {
            this.fillProg = new FlatColorProgram( );
            this.fillBuilder = new GLStreamingBufferBuilder( );
        }

        @Override
        protected void doDispose( GlimpseContext context )
        {
            this.fillProg.dispose( context.getGL( ).getGL3( ) );
            this.fillBuilder.dispose( context.getGL( ) );
        }

        @Override
        protected void doPaintTo( GlimpseContext context )
        {
            GlimpseBounds bounds = getBounds( context );
            Axis2D axis = getAxis2D( context );
            GL3 gl = context.getGL( ).getGL3( );

            enableStandardBlending( gl );
            try
            {
                this.fillBuilder.clear( );
                this.fillBuilder.addQuad2f( 10, 10, 20, 20 );

                this.fillProg.begin( gl );
                try
                {
                    this.fillProg.setAxisOrtho( gl, axis );

                    this.fillProg.draw( gl, this.fillBuilder, GlimpseColor.getBlack( ) );
                }
                finally
                {
                    this.fillProg.end( gl );
                }
            }
            finally
            {
                GLUtils.disableBlending( gl );
            }
        }
    }
}
