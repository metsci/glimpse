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

import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.support.settings.LookAndFeel;

/**
 * GlimpsePainter is the base class for all Glimpse OpenGL drawing code. A GlimsePainter
 * defines a "layer" which can be painted to a given GlimpseContext.
 *
 * @author ulman
 */
public interface GlimpsePainter
{
    /**
     * Renders this GlimpsePainter to the provided GlimpseContext. The context defines
     * the GLContext to use. The GlimpseContext also specifies where this GlimpsePainter
     * should be rendered via the RenderTarget contained in the GlimpseContext.
     */
    public void paintTo( GlimpseContext context );

    /**
     * Sets display options for the painter based on the provided LookAndFeel.
     */
    public void setLookAndFeel( LookAndFeel laf );

    /**
     * A painter which is set to non-visible should make no OpenGL calls when its
     * paintTo method is called.
     */
    public void setVisible( boolean visible );

    public boolean isVisible( );

    /**
     * Free GPU and CPU memory associated with this this GlimpsePainter.
     * After this call returns, the results of calling other GlimpsePainter
     * methods besides isDisposed( ) are undefined.
     */
    public void dispose( GlimpseContext context );

    public boolean isDisposed( );
}
