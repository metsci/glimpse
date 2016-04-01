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

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.AxisNotSetException;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.context.GlimpseTarget;
import com.metsci.glimpse.layout.GlimpseAxisLayout1D;

/**
 * A GlimpsePainter for drawing on {@link com.metsci.glimpse.layout.GlimpseAxisLayout1D}
 * layouts which automatically acquires the appropriate {@link com.metsci.glimpse.axis.Axis1D}
 * from the {@link com.metsci.glimpse.layout.GlimpseAxisLayout1D} based on the
 * {@link com.metsci.glimpse.context.GlimpseContext} provided in the {@code paintTo()} method.
 *
 * @author ulman
 */
public abstract class GlimpsePainter1D extends GlimpsePainterImpl
{
    public abstract void paintTo( GlimpseContext context, GlimpseBounds bounds, Axis1D axis );

    @Override
    public void paintTo( GlimpseContext context, GlimpseBounds bounds )
    {
        GlimpseTarget target = context.getTargetStack( ).getTarget( );
        if ( target instanceof GlimpseAxisLayout1D )
        {
            GlimpseAxisLayout1D layout = ( GlimpseAxisLayout1D ) target;
            Axis1D axis = layout.getAxis( context );

            if ( axis == null )
            {
                // Some GlimpseAxisLayout1D in the GlimpseContext must define an Axis1D
                throw new AxisNotSetException( this, context );
            }

            paintTo( context, bounds, axis );
        }
        else
        {
            // GlimpsePainter1D instances must be painted to GlimpseAxisLayout1D instances
            throw new AxisNotSetException( this, context );
        }
    }
}
