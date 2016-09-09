package com.metsci.glimpse.support.line.example;

import static com.metsci.glimpse.support.line.util.LineUtils.*;
import static com.metsci.glimpse.util.GeneralUtils.*;

import java.util.Random;

import javax.media.opengl.GL;
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



        path.moveTo( 8, 7 );
        path.lineTo( 3, 6 );
        path.lineTo( 7, 3 );

//        path.lineTo( 2, 2 );
        path.lineTo( 4, 10 );


//        path.moveTo( 0, 0 );
//        path.lineTo( 3, 3 );
//        path.lineTo( 6, 6 );





//        Random r = new Random( 0 );
//        for ( int i = 0; i < 25; i++ )
//        {
//            boolean axisAlign = ( i % 2 == 0 );
//
//            float x0 = 2 + 6 * r.nextFloat( );
//            float y0 = 2 + 6 * r.nextFloat( );
//
//            float x1 = x0 + ( -1 + 2 * r.nextFloat( ) );
//            float y1 = ( axisAlign ? y0 : y0 + ( -1 + 2 * r.nextFloat( ) ) );
//
//            float x2 = ( axisAlign ? x1 : x1 + ( -1 + 2 * r.nextFloat( ) ) );
//            float y2 = y1 + ( -1 + 2 * r.nextFloat( ) );
//
//            path.moveTo( x0, y0 );
//            path.lineTo( x1, y1 );
//            path.lineTo( x2, y2 );
//        }

        this.style = new LineStyle( );
        style.rgba = floats( 0.7f, 0, 0, 0.5f );
        style.thickness_PX = 100;
        style.stippleEnable = false;
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


        gl.glDisable( GL.GL_CULL_FACE );


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

    @Override
    protected void doDispose( GlimpseContext context )
    {
        //XXX should LineProgram or MappableBuffer be disposed?
    }
}
