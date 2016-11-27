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

    protected final GLEditableBuffer txyzBuffer;
    protected final ExampleStyle style;
    protected final ExampleProgram prog;


    public ExampleGeoPainter( )
    {
        this.txyzBuffer = new GLEditableBuffer( GL_STATIC_DRAW, 0 );
        this.style = new ExampleStyle( );
        this.prog = new ExampleProgram( );
    }

    public void addPoint( long time_PMILLIS, float x_SU, float y_SU, float depth_SU )
    {
        // WIP: Need a time epoch
        float t = asdf;

        this.txyzBuffer.grow3f( t, x_SU, y_SU );
    }

    public void setGeoSelection( float xMin_SU, float xMax_SU, float yMin_SU, float yMax_SU )
    {
        // WIP
    }

    public void setTimeSelection( long tMin_PMILLIS, long tMax_PMILLIS, long tCursor_PMILLIS )
    {
        // WIP
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
