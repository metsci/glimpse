package com.metsci.glimpse.examples.misc;

import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.shape.DynamicLineSetPainter;
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
                        Object[] ids = new Object[20];
                        float[] x1 = new float[20];
                        float[] y1 = new float[20];
                        float[] x2 = new float[20];
                        float[] y2 = new float[20];

                        for ( int i = 0; i < 20; i++ )
                        {
                            ids[i] = count++;
                            x1[i] = ( float ) Math.random( );
                            y1[i] = ( float ) Math.random( );
                            x2[i] = ( float ) Math.random( );
                            y2[i] = ( float ) Math.random( );
                        }

                        float[] color = GlimpseColor.fromColorRgba( ( float ) Math.random( ), ( float ) Math.random( ), ( float ) Math.random( ), ( float ) Math.random( ) );

                        painter.putLines( ids, x1, y1, x2, y2, color );

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