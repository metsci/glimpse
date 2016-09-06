package com.metsci.glimpse.support.line.example;

import static com.metsci.glimpse.support.line.util.LineUtils.enableStandardBlending;
import static com.metsci.glimpse.support.line.util.LineUtils.ppvAspectRatio;
import static com.metsci.glimpse.util.GeneralUtils.floats;

import java.util.Random;

import javax.media.opengl.GL2ES2;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.painter.base.GlimpsePainter2D;
import com.metsci.glimpse.support.line.LinePath;
import com.metsci.glimpse.support.line.LineProgram;
import com.metsci.glimpse.support.line.LineStyle;

public class RandomLinesPainter extends GlimpsePainter2D
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
            float x0 = 2 + 6*r.nextFloat( );
            float y0 = 2 + 6*r.nextFloat( );

            float x1 = x0 + ( -1 + 2*r.nextFloat( ) );
            float y1 = y0 + ( -1 + 2*r.nextFloat( ) );

            float x2 = x1 + ( -1 + 2*r.nextFloat( ) );
            float y2 = y1 + ( -1 + 2*r.nextFloat( ) );

            path.moveTo( x0, y0 );
            path.lineTo( x1, y1 );
            path.lineTo( x2, y2 );
        }

        this.style = new LineStyle( );
        style.rgba = floats( 0.7f, 0, 0, 1 );
        style.thickness_PX = 4;
        style.stippleEnable = true;
        style.stippleScale = 2;
        style.stipplePattern = 0b0001010111111111;

        this.prog = null;
    }

    @Override
    public void paintTo( GlimpseContext context, GlimpseBounds bounds, Axis2D axis )
    {
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
            prog.draw( gl, style, path, ppvAspectRatio( axis ) );
        }
        finally
        {
            prog.end( gl );
        }
    }

}
