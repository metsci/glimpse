/*
 * Copyright (c) 2012, Metron, Inc.
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
package com.metsci.glimpse.gl;

import static com.metsci.glimpse.gl.util.GLCapabilityUtils.*;
import static java.util.logging.Level.INFO;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.opengl.GLContext;

public class GLCapabilityLogger extends GLSimpleListenerAbstract
{
    private final Logger logger;
    private final String name;

    public GLCapabilityLogger( Logger logger, String name )
    {
        this.logger = logger;
        this.name = name;
    }

    public GLCapabilityLogger( String name )
    {
        this( Logger.getLogger( GLCapabilityLogger.class.getName( ) ), name );
    }

    public GLCapabilityLogger( Logger logger )
    {
        this( logger, "" );
    }

    public GLCapabilityLogger( )
    {
        this( Logger.getLogger( GLCapabilityLogger.class.getName( ) ), "" );
    }

    @Override
    public void init( GLContext context )
    {
        String prefix = "init()";

        if ( name != null && name.length( ) > 0 ) prefix += " on" + name;

        prefix = prefix + ": ";

        logGLVersionInfo( logger, INFO, context, true );
        logGLMaximumValues( logger, INFO, context );
        logGLBufferProperties( logger, Level.INFO, context, prefix );
        logGLExtensions( logger, INFO, context, false );
        logGLExtensions( logger, INFO, context, true );
    }

    @Override
    public void display( GLContext context )
    {
    }

    @Override
    public void reshape( GLContext context, int x, int y, int width, int height )
    {
    }

    @Override
    public void displayChanged( GLContext context, boolean modeChanged, boolean deviceChanged )
    {
    }

    @Override
    public void dispose( GLContext context )
    {
        // nothing to dispose
    }
}
