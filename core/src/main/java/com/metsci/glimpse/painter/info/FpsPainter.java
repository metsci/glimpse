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
package com.metsci.glimpse.painter.info;

import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.layout.GlimpseLayoutCache;
import com.metsci.glimpse.support.color.GlimpseColor;

/**
 * Displays an estimate of the current number of times
 * the {@link com.metsci.glimpse.canvas.GlimpseCanvas}
 * is being redrawn per second.
 *
 * @author ulman
 *
 */
public class FpsPainter extends SimpleTextPainter
{
    private static float[] normColor = new float[] { 0.5f, 1.0f, 0.5f, 1.0f };
    private static float[] warnColor = new float[] { 1.0f, 0.5f, 0.5f, 1.0f };

    private GlimpseLayoutCache<FpsHelper> cache;

    public FpsPainter( )
    {
        super( );

        cache = new GlimpseLayoutCache<FpsHelper>( );

        setFont( 12, true, false );
        setBackgroundColor( GlimpseColor.getGray( 0.5f ) );
        setPaintBackground( true );
    }

    private final void tickTock( GlimpseContext context )
    {
        if ( cache.getValue( context ) == null ) cache.setValue( context, new FpsHelper( ) );

        FpsHelper temp = cache.getValue( context );
        temp.setFrameCount( temp.getFrameCount( ) + 1 );
        long currentTime = System.currentTimeMillis( );

        if ( temp.getTimeOfLastCounterReset( ) < 0 )
        {
            temp.setTimeOfCounterReset( System.currentTimeMillis( ) );
            temp.setFrameCount( 0 );
        }
        else if ( currentTime - temp.getTimeOfLastCounterReset( ) >= 1000 || temp.getFrameCount( ) >= 10 )
        {
            temp.setFpsEstimate( temp.getFrameCount( ) / ( currentTime - temp.getTimeOfLastCounterReset( ) ) * 1000f );
            temp.setTimeOfCounterReset( currentTime );
            temp.setFrameCount( 0 );
        }
    }

    @Override
    protected void doPaintTo( GlimpseContext context )
    {
        if ( cache.getValue( context ) == null ) cache.setValue( context, new FpsHelper( ) );

        FpsHelper temp = cache.getValue( context );
        tickTock( context );

        String text = "FPS: Measuring...";
        setColor( GlimpseColor.getBlack( ) );

        if ( temp.getLastFpsEstimate( ) > 0 )
        {
            text = String.format( "FPS:%4.0f", temp.getLastFpsEstimate( ) );

            if ( temp.getLastFpsEstimate( ) > 30 )
            {
                setColor( normColor );
            }
            else
            {
                setColor( warnColor );
            }
        }

        setText( text );

        super.doPaintTo( context );
    }
}
