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
package com.metsci.glimpse.painter.decoration;

import static com.metsci.glimpse.support.settings.AbstractLookAndFeel.FRAME_BACKGROUND_COLOR;
import static com.metsci.glimpse.support.settings.AbstractLookAndFeel.PLOT_BACKGROUND_COLOR;

import javax.media.opengl.GL;

import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.painter.base.GlimpsePainter;
import com.metsci.glimpse.support.settings.LookAndFeel;

/**
 * Paints a simple solid color background.
 *
 * @author ulman
 */
public class BackgroundPainter implements GlimpsePainter
{
    protected float[] backgroundColor = new float[4];
    protected boolean colorSet = false;
    protected String lafColorKey;

    protected boolean displayOn = true;

    public BackgroundPainter( )
    {
        this( false );
    }

    public BackgroundPainter( boolean isFrameBackground )
    {
        setLookAndFeelKey( isFrameBackground ? FRAME_BACKGROUND_COLOR : PLOT_BACKGROUND_COLOR );
    }

    @Override
    public void setVisible( boolean show )
    {
        this.displayOn = show;
    }

    @Override
    public boolean isVisible( )
    {
        return displayOn;
    }

    public float[] getColor( )
    {
        return this.backgroundColor;
    }

    public BackgroundPainter setColor( float[] rgba )
    {
        backgroundColor = rgba;
        colorSet = true;

        return this;
    }

    public BackgroundPainter setColor( float r, float g, float b, float a )
    {
        backgroundColor[0] = r;
        backgroundColor[1] = g;
        backgroundColor[2] = b;
        backgroundColor[3] = a;

        colorSet = true;

        return this;
    }

    public void setLookAndFeelKey( String key )
    {
        this.lafColorKey = key;
    }

    @Override
    public void paintTo( GlimpseContext context )
    {
        if ( !displayOn ) return;

        GL gl = context.getGL( );
        gl.glClearColor( backgroundColor[0], backgroundColor[1], backgroundColor[2], backgroundColor[3] );
        gl.glClear( GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT );
    }

    @Override
    public void setLookAndFeel( LookAndFeel laf )
    {
        // ignore the look and feel if a color has been manually set
        if ( !colorSet )
        {
            setColor( laf.getColor( lafColorKey ) );
            colorSet = false;
        }
    }

    @Override
    public void dispose( GlimpseContext context )
    {
        // do nothing
    }

    @Override
    public boolean isDisposed( )
    {
        return false;
    }
}
