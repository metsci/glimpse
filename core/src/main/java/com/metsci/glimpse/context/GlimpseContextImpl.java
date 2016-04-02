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
package com.metsci.glimpse.context;

import javax.media.opengl.GL;
import javax.media.opengl.GLContext;

import com.metsci.glimpse.canvas.GlimpseCanvas;

public class GlimpseContextImpl implements GlimpseContext
{
    private static final String defaultDpiKey = "com.metsci.glimpse.dpi.default";
    private static final int defaultDpiValue = Integer.parseInt( System.getProperty( defaultDpiKey, "96" ) );

    private GLContext glContext;
    private GlimpseTargetStack targetStack;
    private int dpi;
    private final int[] scale;

    public GlimpseContextImpl( GLContext context, int[] scale )
    {
        this.glContext = context;
        this.targetStack = new GlimpseTargetStackImpl( );
        this.dpi = defaultDpiValue; //TODO fix this
        this.scale = scale;
    }

    public GlimpseContextImpl( GlimpseCanvas canvas )
    {
        this.glContext = canvas.getGLContext( );
        this.targetStack = new GlimpseTargetStackImpl( canvas );
        this.dpi = defaultDpiValue; //TODO fix this
        this.scale = canvas.getSurfaceScale( );
    }

    @Override
    public GLContext getGLContext( )
    {
        return this.glContext;
    }

    @Override
    public GL getGL( )
    {
        return this.glContext.getGL( );
    }

    @Override
    public GlimpseTargetStack getTargetStack( )
    {
        return this.targetStack;
    }

    @Override
    public int getDPI( )
    {
        return this.dpi;
    }

    @Override
    public String toString( )
    {
        return String.format( "[stack: %s dpi: %d]", targetStack, dpi );
    }

    @Override
    public int[] getSurfaceScale( )
    {
        return scale;
    }
}
