package com.metsci.glimpse.dnc;

import javax.media.opengl.GL2ES2;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;

public class DncShaderUtils
{

    public static void setUniformAxisRect( GL2ES2 gl, int location, Axis2D axis )
    {
        Axis1D xAxis = axis.getAxisX( );
        float xMin = ( float ) xAxis.getMin( );
        float xSize = ( float ) ( xAxis.getMax( ) - xAxis.getMin( ) );

        Axis1D yAxis = axis.getAxisY( );
        float yMin = ( float ) yAxis.getMin( );
        float ySize = ( float ) ( yAxis.getMax( ) - yAxis.getMin( ) );

        gl.glUniform4f( location, xMin, yMin, xSize, ySize );
    }

    public static void setUniformViewport( GL2ES2 gl, int location, GlimpseBounds bounds )
    {
        gl.glUniform2f( location, bounds.getWidth( ), bounds.getHeight( ) );
    }

}
