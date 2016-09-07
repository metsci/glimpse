package com.metsci.glimpse.support.line.example;

import static com.metsci.glimpse.support.line.util.LineUtils.*;
import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2ES2.*;

import java.nio.FloatBuffer;

import javax.media.opengl.GL2ES2;

import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.support.line.LineProgram;
import com.metsci.glimpse.support.line.LineStyle;
import com.metsci.glimpse.support.line.util.MappableBuffer;

public class ExampleBorderPainter extends GlimpsePainterBase
{

    protected final MappableBuffer xyVbo;
    protected final MappableBuffer mileageVbo;

    protected LineStyle style;
    protected LineProgram prog;

    public ExampleBorderPainter( )
    {
        this.xyVbo = new MappableBuffer( GL_ARRAY_BUFFER, GL_STREAM_DRAW, 1000 );
        this.mileageVbo = new MappableBuffer( GL_ARRAY_BUFFER, GL_STREAM_DRAW, 1000 );

        this.style = new LineStyle( );
        this.prog = null;
    }

    @Override
    public void paintTo( GlimpseContext context )
    {
        GlimpseBounds bounds = getBounds( context );
        GL2ES2 gl = context.getGL( ).getGL2ES2( );

        if ( prog == null )
        {
            this.prog = new LineProgram( gl );
        }

        int maxVertices = 8;
        FloatBuffer xyBuffer = xyVbo.mapFloats( gl, 2 * maxVertices );
        FloatBuffer mileageBuffer = mileageVbo.mapFloats( gl, 1 * maxVertices );

        float inset_PX = 0.5f * style.thickness_PX;
        float xLeft_PX = inset_PX;
        float xRight_PX = bounds.getWidth( ) - inset_PX;
        float yBottom_PX = inset_PX;
        float yTop_PX = bounds.getHeight( ) - inset_PX;

        xyBuffer.put( xLeft_PX ).put( yBottom_PX );
        xyBuffer.put( xRight_PX ).put( yBottom_PX );
        mileageBuffer.put( 0 );
        mileageBuffer.put( xRight_PX - xLeft_PX );

        xyBuffer.put( xRight_PX ).put( yBottom_PX );
        xyBuffer.put( xRight_PX ).put( yTop_PX );
        mileageBuffer.put( 0 );
        mileageBuffer.put( yTop_PX - yBottom_PX );

        xyBuffer.put( xLeft_PX ).put( yBottom_PX );
        xyBuffer.put( xLeft_PX ).put( yTop_PX );
        mileageBuffer.put( 0 );
        mileageBuffer.put( yTop_PX - yBottom_PX );

        xyBuffer.put( xLeft_PX ).put( yTop_PX );
        xyBuffer.put( xRight_PX ).put( yTop_PX );
        mileageBuffer.put( 0 );
        mileageBuffer.put( xRight_PX - xLeft_PX );

        int numVertices = xyBuffer.position( ) / 2;
        xyVbo.seal( gl );
        mileageVbo.seal( gl );

        enableStandardBlending( gl );

        prog.begin( gl );
        try
        {
            prog.setViewport( gl, bounds );
            prog.setPixelOrtho( gl, bounds );
            prog.setStyle( gl, style );

            prog.draw( gl, xyVbo, mileageVbo, 0, numVertices );
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
