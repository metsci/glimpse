package com.metsci.glimpse.examples.misc;

import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.shape.DynamicLineSetPainter;
import com.metsci.glimpse.painter.shape.DynamicLineSetPainter.BulkLineAccumulator;
import com.metsci.glimpse.plot.SimplePlot2D;
import com.metsci.glimpse.support.color.GlimpseColor;

public class DynamicLinePainterExample implements GlimpseLayoutProvider
{
    public static void main( String[] args ) throws Exception
    {
        Example.showWithSwing( new DynamicLinePainterExample( ) );
    }

    @Override
    public SimplePlot2D getLayout( )
    {
        SimplePlot2D plot = new SimplePlot2D( );

        plot.getAxis( ).set( -1, 2, -1, 2 );

        final DynamicLineSetPainter painter = new DynamicLineSetPainter( );

        plot.addPainter( painter );

        ( new Thread( )
        {
            int count = 0;

            public void run( )
            {
                try
                {

                    while ( true )
                    {
                        BulkLineAccumulator accum = new BulkLineAccumulator( );

                        float[] color = GlimpseColor.fromColorRgba( ( float ) Math.random( ), ( float ) Math.random( ), ( float ) Math.random( ), ( float ) Math.random( ) );

                        for ( int i = 0; i < 20; i++ )
                        {
                            accum.add( count++, ( float ) Math.random( ), ( float ) Math.random( ), ( float ) Math.random( ), ( float ) Math.random( ), color );
                        }

                        painter.putLines( accum );

                        try
                        {
                            Thread.sleep( 20 );
                        }
                        catch ( InterruptedException e )
                        {
                        }
                    }

                }
                catch ( Exception e )
                {
                    e.printStackTrace( );
                }
            }
        } ).start( );

        return plot;
    }
}