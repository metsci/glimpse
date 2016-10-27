package com.metsci.glimpse.examples.track;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.listener.AxisListener1D;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.track.StaticTrackPainter;
import com.metsci.glimpse.plot.SimplePlot2D;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.shader.line.LineJoinType;

public class StaticTrackPainterExample implements GlimpseLayoutProvider
{
    public static void main( String args[] ) throws Exception
    {
        Example.showWithSwing( new StaticTrackPainterExample( ) );
    }

    public static final int ParticleCount = 2000;
    public static final int TimeCount = 1000;

    @Override
    public GlimpseLayout getLayout( )
    {
        SimplePlot2D plot = new SimplePlot2D( );

        plot.setAxisSizeZ( 50 );
        plot.setTitleHeight( 0 );

        long[] timeArray = new long[TimeCount];
        for ( int i = 0; i < TimeCount; i++ )
        {
            timeArray[i] = i;
        }

        float[][] xPositions = new float[ParticleCount][TimeCount];
        float[][] yPositions = new float[ParticleCount][TimeCount];
        float[][][] colors = new float[ParticleCount][TimeCount][4];

        for ( int p = 0; p < ParticleCount; p++ )
        {
            for ( int t = 0; t < TimeCount; t++ )
            {
                if ( t == 0 )
                    xPositions[p][t] = p;
                else
                    xPositions[p][t] = xPositions[p][t - 1] + ( float ) ( 2 * Math.random( ) - 1 ) * ( 40.0f / TimeCount );

                yPositions[p][t] = t;

                colors[p][t] = GlimpseColor.fromColorRgba( ( float ) Math.random( ), ( float ) Math.random( ), ( float ) Math.random( ), 1.0f );
            }
        }

        final StaticTrackPainter painter = new StaticTrackPainter( timeArray, xPositions, yPositions, colors );

        painter.getLineStyle( ).joinType = LineJoinType.JOIN_MITER;
        painter.getLineStyle( ).thickness_PX = 4.0f;

        plot.addPainter( painter );

        plot.getAxis( ).set( 0, ParticleCount, 0, TimeCount );
        plot.getAxisZ( ).setMin( 0 );
        plot.getAxisZ( ).setMax( TimeCount );

        plot.getCrosshairPainter( ).setVisible( false );

        painter.displayTimeRange( ( long ) plot.getAxisZ( ).getMin( ), ( long ) plot.getAxisZ( ).getMax( ) );
        plot.getAxisZ( ).addAxisListener( new AxisListener1D( )
        {
            @Override
            public void axisUpdated( Axis1D axis )
            {
                painter.displayTimeRange( ( long ) axis.getMin( ), ( long ) axis.getMax( ) );
            }
        } );

        return plot;
    }
}
