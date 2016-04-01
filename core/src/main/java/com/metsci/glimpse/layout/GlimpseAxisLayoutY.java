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
package com.metsci.glimpse.layout;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;

/**
 * A vertical GlimpseAxisLayout1D implementation.
 *
 * @author ulman
 */
public class GlimpseAxisLayoutY extends GlimpseAxisLayout1D
{
    public GlimpseAxisLayoutY( GlimpseLayout parent, String name, Axis1D axis )
    {
        super( parent, name, axis );
    }

    public GlimpseAxisLayoutY( GlimpseLayout parent, Axis1D axis )
    {
        super( parent, axis );
    }

    public GlimpseAxisLayoutY( String name, Axis1D axis )
    {
        super( name, axis );
    }

    public GlimpseAxisLayoutY( Axis1D axis )
    {
        super( axis );
    }

    public GlimpseAxisLayoutY( GlimpseLayout parent, String name )
    {
        super( parent, name );
    }

    public GlimpseAxisLayoutY( GlimpseLayout parent )
    {
        super( parent );
    }

    public GlimpseAxisLayoutY( )
    {
        super( null, null, null );
    }

    @Override
    protected Axis1D getAxis( Axis2D axis )
    {
        return axis.getAxisY( );
    }

    @Override
    protected int getSize( GlimpseBounds bounds )
    {
        return bounds.getHeight( );
    }

    @Override
    public boolean isHorizontal( )
    {
        return false;
    }
}
