package com.metsci.glimpse.examples.basic;

import static com.metsci.glimpse.gl.util.GLUtils.enableStandardBlending;

import javax.media.opengl.GL3;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.gl.util.GLUtils;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.plot.EmptyPlot2D;
import com.metsci.glimpse.support.color.GlimpseColor;
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

        plot.getAxis( ).set( 0, 500, 0, 500 );

        plot.addPainter( new FillPainter( ) );

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
