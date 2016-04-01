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
package com.metsci.glimpse.axis;

import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.painter.base.GlimpsePainter;

/**
 * An exception thrown when a {@link com.metsci.glimpse.painter.base.GlimpsePainter} or
 * {@link com.metsci.glimpse.axis.listener.mouse.AxisMouseListener} attempts
 * to retrieve the {@link Axis1D} for a {@link com.metsci.glimpse.layout.GlimpseAxisLayout1D}
 * whose associated {@link Axis1D} has not yet been set.
 *
 * @author ulman
 */
public class AxisNotSetException extends RuntimeException
{
    private static final long serialVersionUID = 5685894473973040909L;

    public AxisNotSetException( )
    {
        super( );
    }

    public AxisNotSetException( GlimpseContext context )
    {
        super( String.format( "Axis not set in context: %s", context ) );
    }

    public AxisNotSetException( GlimpsePainter painter, GlimpseContext context )
    {
        super( String.format( "Axis not set for painter: %s in context: %s", painter, context ) );
    }

    public AxisNotSetException( GlimpseTargetStack stack )
    {
        super( String.format( "Axis not set in: %s", stack ) );
    }

    public AxisNotSetException( GlimpsePainter painter, GlimpseTargetStack stack )
    {
        super( String.format( "Axis not set for painter: %s in: %s", painter, stack ) );
    }

    public AxisNotSetException( String message )
    {
        super( message );
    }
}
