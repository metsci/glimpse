package com.metsci.glimpse.support.line.example;

import static com.metsci.glimpse.gl.util.GLUtils.*;
import static com.metsci.glimpse.support.line.LineJoinType.*;
import static com.metsci.glimpse.support.line.LineUtils.*;
import static com.metsci.glimpse.util.GeneralUtils.*;
import static java.lang.System.*;

import java.util.Random;

import javax.media.opengl.GL2ES2;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.support.line.LinePath;
import com.metsci.glimpse.support.line.LineProgram;
import com.metsci.glimpse.support.line.LineStyle;

public class RandomLinesPainter extends GlimpsePainterBase
{

    protected LinePath path;
    protected LineStyle style;
    protected LineProgram prog;

    public RandomLinesPainter( )
    {
        this.path = new LinePath( );
        Random r = new Random( 0 );
        for ( int i = 0; i < 25; i++ )
        {
            boolean axisAlign = ( i % 2 == 0 );

            float x0 = 2 + 6 * r.nextFloat( );
            float y0 = 2 + 6 * r.nextFloat( );

            float x1 = x0 + ( -1 + 2 * r.nextFloat( ) );
            float y1 = ( axisAlign ? y0 : y0 + ( -1 + 2 * r.nextFloat( ) ) );

            float x2 = ( axisAlign ? x1 : x1 + ( -1 + 2 * r.nextFloat( ) ) );
            float y2 = y1 + ( -1 + 2 * r.nextFloat( ) );

            path.moveTo( x0, y0 );
            path.lineTo( x1, y1 );
            path.lineTo( x2, y2 );
        }

        this.style = new LineStyle( );
        style.joinType = JOIN_MITER;
        style.rgba = floats( 0.7f, 0, 0, 1 );
        style.stippleEnable = true;
        style.stippleScale = 2;
        style.stipplePattern = 0b0001010111111111;

        this.prog = null;
    }

    @Override
    public void doPaintTo( GlimpseContext context )
    {
        GlimpseBounds bounds = getBounds( context );
        Axis2D axis = getAxis2D( context );
        GL2ES2 gl = context.getGL( ).getGL2ES2( );

        enableStandardBlending( gl );

        if ( prog == null )
        {
            this.prog = new LineProgram( gl );
        }

        prog.begin( gl );
        try
        {
            prog.setViewport( gl, bounds );
            prog.setAxisOrtho( gl, axis );

            // Make lines pulsate
            {
                float maxThickness_PX = 10;
                float minThickness_PX = 2;
                long halfPeriod_MILLIS = 800;

                long t = currentTimeMillis( );
                long stepNum = t / halfPeriod_MILLIS;
                float stepFrac = ( t % halfPeriod_MILLIS ) / ( ( float ) halfPeriod_MILLIS );
                if ( stepNum % 2 == 0 )
                {
                    style.thickness_PX = minThickness_PX + ( maxThickness_PX - minThickness_PX )*stepFrac;
                }
                else
                {
                    style.thickness_PX = maxThickness_PX - ( maxThickness_PX - minThickness_PX )*stepFrac;
                }
            }

            prog.draw( gl, style, path, ppvAspectRatio( axis ) );
        }
        finally
        {
            prog.end( gl );
        }
    }

    @Override
    protected void doDispose( GlimpseContext context )
    {
        //XXX should LineProgram or MappableBuffer be disposed?
    }
}
