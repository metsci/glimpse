package com.metsci.glimpse.examples.layers;

import static com.metsci.glimpse.gl.util.GLUtils.disableBlending;
import static com.metsci.glimpse.gl.util.GLUtils.enableStandardBlending;
import static java.lang.System.currentTimeMillis;
import static javax.media.opengl.GL.GL_STATIC_DRAW;

import javax.media.opengl.GL2ES2;
import javax.media.opengl.GL2ES3;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.GLEditableBuffer;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;

public class ExampleTimelinePainter extends GlimpsePainterBase
{

    protected final ExampleProgram prog;
    protected final ExampleStyle style;

    protected float tWindowMin;
    protected float tWindowMax;
    protected float xWindowMin;
    protected float xWindowMax;
    protected float yWindowMin;
    protected float yWindowMax;

    protected final GLEditableBuffer txyzBuffer;


    public ExampleTimelinePainter( ExampleStyle style )
    {
        this.prog = new ExampleProgram( );
        this.style = new ExampleStyle( style );

        this.tWindowMin = 0;
        this.tWindowMax = 0;
        this.xWindowMin = 0;
        this.xWindowMax = 0;
        this.yWindowMin = 0;
        this.yWindowMax = 0;

        this.txyzBuffer = new GLEditableBuffer( GL_STATIC_DRAW, 0 );
    }

    public void addPoint( float t, float x, float y, float z )
    {
        this.txyzBuffer.grow4f( t, x, y, z );
    }

    public void setTWindow( float tMin, float tMax )
    {
        this.tWindowMin = tMin;
        this.tWindowMax = tMax;
    }

    public void setXyWindow( float xMin, float xMax, float yMin, float yMax )
    {
        this.xWindowMin = xMin;
        this.xWindowMax = xMax;

        this.yWindowMin = yMin;
        this.yWindowMax = yMax;
    }

    @Override
    protected void doPaintTo( GlimpseContext context )
    {
        GlimpseBounds bounds = getBounds( context );
        Axis2D axis = requireAxis2D( context );
        GL2ES3 gl = context.getGL( ).getGL2ES3( );

        enableStandardBlending( gl );
        this.prog.begin( gl );
        try
        {
            this.prog.setViewport( gl, bounds );
            this.prog.setAxisOrtho( gl, axis );

            this.prog.setTimelineMode( gl );
            this.prog.setStyle( gl, this.style, currentTimeMillis( ) );
            this.prog.setWindow( gl, this.tWindowMin, this.tWindowMax, this.xWindowMin, this.xWindowMax, this.yWindowMin, this.yWindowMax );

            this.prog.draw( gl, this.txyzBuffer );
        }
        finally
        {
            this.prog.end( gl );
            disableBlending( gl );
        }
    }

    @Override
    protected void doDispose( GlimpseContext context )
    {
        GL2ES2 gl = context.getGL( ).getGL2ES2( );
        this.txyzBuffer.dispose( gl );
        this.prog.dispose( gl );
    }

}
