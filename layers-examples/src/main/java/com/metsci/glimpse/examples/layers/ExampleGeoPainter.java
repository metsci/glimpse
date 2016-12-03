package com.metsci.glimpse.examples.layers;

import static com.metsci.glimpse.gl.util.GLUtils.disableBlending;
import static com.metsci.glimpse.gl.util.GLUtils.enableStandardBlending;
import static javax.media.opengl.GL.GL_STATIC_DRAW;

import javax.media.opengl.GL2ES2;
import javax.media.opengl.GL2ES3;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.GLEditableBuffer;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;

public class ExampleGeoPainter extends GlimpsePainterBase
{

    protected final ExampleProgram prog;
    protected final ExampleStyle style;

    protected float tSelectionMin;
    protected float tSelectionMax;

    protected final GLEditableBuffer txyzBuffer;


    public ExampleGeoPainter( )
    {
        this.prog = new ExampleProgram( );
        this.style = new ExampleStyle( );

        this.tSelectionMin = 0;
        this.tSelectionMax = 0;

        this.txyzBuffer = new GLEditableBuffer( GL_STATIC_DRAW, 0 );
    }

    public void addPoint( float t, float x, float y, float z )
    {
        this.txyzBuffer.grow4f( t, x, y, z );
    }

    public void setGeoSelection( float xMin, float xMax, float yMin, float yMax )
    {
        // WIP
    }

    public void setTimeSelection( float tMin, float tMax )
    {
        this.tSelectionMin = tMin;
        this.tSelectionMax = tMax;
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

            this.prog.setStyle( gl, this.style );

            this.prog.setTimeWindow( gl, this.tSelectionMin, this.tSelectionMax );

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
