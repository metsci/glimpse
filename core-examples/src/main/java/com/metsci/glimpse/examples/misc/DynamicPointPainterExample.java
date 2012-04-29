package com.metsci.glimpse.examples.misc;

import static com.metsci.glimpse.util.logging.LoggerUtils.logInfo;

import java.util.Collection;
import java.util.logging.Logger;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.listener.AxisListener2D;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.shape.DynamicPointSetPainter;
import com.metsci.glimpse.plot.SimplePlot2D;
import com.metsci.glimpse.support.color.GlimpseColor;

public class DynamicPointPainterExample implements GlimpseLayoutProvider
{
    private static final Logger logger = Logger.getLogger( DynamicPointPainterExample.class.getSimpleName( ) );

    public static void main( String[] args ) throws Exception
    {
        Example.showWithSwing( new DynamicPointPainterExample( ) );
    }

    @Override
    public SimplePlot2D getLayout( )
    {
        SimplePlot2D plot = new SimplePlot2D( );

        plot.getAxis( ).set( -1, 2, -1, 2 );

        final DynamicPointSetPainter painter = new DynamicPointSetPainter( );

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
                        float[] x = new float[20];
                        float[] y = new float[20];

                        for ( int i = 0; i < 20; i++ )
                        {
                            ids[i] = count++;
                            x[i] = ( float ) Math.random( );
                            y[i] = ( float ) Math.random( );
                        }

                        float[] color = GlimpseColor.fromColorRgba( ( float ) Math.random( ), ( float ) Math.random( ), ( float ) Math.random( ), ( float ) Math.random( ) );

                        painter.putPoints( ids, x, y, color );

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

        plot.addAxisListener( new AxisListener2D( )
        {
            @Override
            public void axisUpdated( Axis2D axis )
            {
                double centerX = axis.getAxisX( ).getSelectionCenter( );
                double sizeX = axis.getAxisX( ).getSelectionSize( );

                double centerY = axis.getAxisY( ).getSelectionCenter( );
                double sizeY = axis.getAxisY( ).getSelectionSize( );

                Collection<Object> selection = painter.getGeoRange( centerX - sizeX / 2.0, centerX + sizeX / 2.0, centerY - sizeY / 2.0, centerY + sizeY / 2.0 );

                logInfo( logger, "Selected Ids: %s", selection );
            }
        } );

        return plot;
    }
}