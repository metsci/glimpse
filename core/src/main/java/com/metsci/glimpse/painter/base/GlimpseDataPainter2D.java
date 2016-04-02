/*
 * Copyright (c) 2016, Metron, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Metron, Inc. nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL METRON, INC. BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.metsci.glimpse.painter.base;

import javax.media.opengl.GL2;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;

/**
 * Takes care of most of the common setup steps that GlimpsePainters must perform.
 * It uses the provided {@link com.metsci.glimpse.axis.Axis2D} to set
 * the GL Orthographic projection, and sets up a standard blend function to enable
 * transparency.</p>
 *
 * Users of GlimpseDataPainter2D should simply be able to start making glVertex(...)
 * calls using data coordinates and have them display correctly (and be correctly
 * affected by adjustments to the axis).
 *
 * @author ulman
 *
 */
public abstract class GlimpseDataPainter2D extends GlimpsePainter2D
{
    public abstract void paintTo( GL2 gl, GlimpseBounds bounds, Axis2D axis );

    protected volatile boolean pointSmooth = true;
    protected volatile boolean lineSmooth = true;
    protected volatile boolean blend = true;

    public boolean isPointSmooth( )
    {
        return pointSmooth;
    }

    public void setPointSmooth( boolean pointSmooth )
    {
        this.pointSmooth = pointSmooth;
    }

    public boolean isLineSmooth( )
    {
        return lineSmooth;
    }

    public void setLineSmooth( boolean lineSmooth )
    {
        this.lineSmooth = lineSmooth;
    }

    public boolean isAlphaBlend( )
    {
        return blend;
    }

    public void setAlphaBlend( boolean blend )
    {
        this.blend = blend;
    }

    @Override
    public void paintTo( GlimpseContext context, GlimpseBounds bounds, Axis2D axis )
    {
        if ( axis == null || axis.getAxisX( ) == null || axis.getAxisY( ) == null || !bounds.isValid( ) ) return;

        GL2 gl = context.getGL( ).getGL2( );

        gl.glMatrixMode( GL2.GL_PROJECTION );
        gl.glLoadIdentity( );
        gl.glOrtho( axis.getMinX( ), axis.getMaxX( ), axis.getMinY( ), axis.getMaxY( ), -1, 1 );

        if ( blend )
        {
            // When blending, we want the resulting RGB to be the obvious weighted average
            // of RGB_s and RGB_d (weighted by A_s and 1-A_s, respectively).
            //
            // However, the resulting A should be (1 - (1-A_d)*(1-A_s)). With a little bit
            // of algebra, you'll find that:
            //
            //     A_s*(1) + A_d*(1-A_s)  =  (1 - (1-A_d)*(1-A_s))
            //
            // So that's why the third and fourth args here are (1) and (1-A_s).
            //
            gl.glBlendFuncSeparate( GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA, GL2.GL_ONE, GL2.GL_ONE_MINUS_SRC_ALPHA );
            gl.glEnable( GL2.GL_BLEND );
        }

        if ( lineSmooth ) gl.glEnable( GL2.GL_LINE_SMOOTH );
        if ( pointSmooth ) gl.glEnable( GL2.GL_POINT_SMOOTH );

        paintTo( context.getGL( ).getGL2( ), bounds, axis );
    }
}
