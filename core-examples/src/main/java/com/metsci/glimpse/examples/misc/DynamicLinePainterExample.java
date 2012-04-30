package com.metsci.glimpse.examples.misc;

import java.util.LinkedList;
import java.util.List;

import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.shape.DynamicLineSetPainter;
import com.metsci.glimpse.painter.shape.DynamicLineSetPainter.BulkLoadLine;
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
                        List<BulkLoadLine> lines = new LinkedList<BulkLoadLine>( );

                        float[] color = GlimpseColor.fromColorRgba( ( float ) Math.random( ), ( float ) Math.random( ), ( float ) Math.random( ), ( float ) Math.random( ) );

                        for ( int i = 0; i < 20; i++ )
                        {
                            lines.add( new BulkLoadLine( count++, ( float ) Math.random( ), ( float ) Math.random( ), ( float ) Math.random( ), ( float ) Math.random( ), color ) );
                        }

                        painter.putLines( lines );

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