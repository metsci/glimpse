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
import com.metsci.glimpse.plot.timeline.data.Epoch;

public class ExampleTimelinePainter extends GlimpsePainterBase
{

    protected final Epoch timelineEpoch;

    protected final ExampleProgram prog;
    protected final ExampleStyle style;
    protected float timeWindowMin;
    protected float timeWindowMax;
    protected final GLEditableBuffer txyzBuffer;


    public ExampleTimelinePainter( Epoch timelineEpoch )
    {
        this.timelineEpoch = timelineEpoch;

        this.prog = new ExampleProgram( );
        this.style = new ExampleStyle( );
        this.timeWindowMin = 0;
        this.timeWindowMax = 0;
        this.txyzBuffer = new GLEditableBuffer( GL_STATIC_DRAW, 0 );
    }

    public void addPoint( long time_PMILLIS, float x_SU, float y_SU, float z_SU )
    {
        float t = ( float ) this.timelineEpoch.fromPosixMillis( time_PMILLIS );
        this.txyzBuffer.grow4f( t, t, z_SU, z_SU );
    }

    public void setGeoSelection( float xMin_SU, float xMax_SU, float yMin_SU, float yMax_SU )
    {
        // WIP
    }

    public void setTimeSelection( long tMin_PMILLIS, long tMax_PMILLIS, long tCursor_PMILLIS )
    {
        this.timeWindowMin = ( float ) this.timelineEpoch.fromPosixMillis( tMin_PMILLIS );
        this.timeWindowMax = ( float ) this.timelineEpoch.fromPosixMillis( tMax_PMILLIS );
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

            this.prog.setTimeWindow( gl, this.timeWindowMin, this.timeWindowMax );

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
