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
package com.metsci.glimpse.timing;

import static com.metsci.glimpse.gl.util.GLCapabilityUtils.*;
import static com.metsci.glimpse.util.logging.LoggerUtils.*;
import static java.util.logging.Level.*;

import java.util.logging.Logger;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;

import com.metsci.glimpse.canvas.GlimpseCanvas;

public class GLVersionLogger implements GLEventListener
{
    private static final Logger logger = getLogger( GLVersionLogger.class );

    public static <T extends GlimpseCanvas> T addGLVersionLogger( T canvas )
    {
        canvas.getGLDrawable( ).addGLEventListener( new GLVersionLogger( ) );
        return canvas;
    }

    @Override
    public void init( GLAutoDrawable drawable )
    {
        logGLVersionInfo( logger, INFO, drawable.getContext( ) );
    }

    @Override
    public void reshape( GLAutoDrawable drawable, int x, int y, int width, int height )
    { }

    @Override
    public void display( GLAutoDrawable drawable )
    { }

    @Override
    public void dispose( GLAutoDrawable drawable )
    { }

}
