/*
 * Copyright (c) 2020, Metron, Inc.
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
package com.metsci.glimpse.core.timing;

import com.metsci.glimpse.core.context.GlimpseContext;
import com.metsci.glimpse.core.painter.base.GlimpsePainterBase;

public class FpsPrinter extends GlimpsePainterBase
{

    protected long startTime_PMILLIS;
    protected long frameCount;


    public FpsPrinter( )
    {
        this.startTime_PMILLIS = -1;
        this.frameCount = 0;
    }

    @Override
    public void doPaintTo( GlimpseContext context )
    {
        long frameTime_PMILLIS = System.currentTimeMillis( );

        this.frameCount++;

        if ( this.startTime_PMILLIS < 0 )
        {
            this.startTime_PMILLIS = frameTime_PMILLIS;
            this.frameCount = 0;
        }
        else if ( this.frameCount >= 50 || frameTime_PMILLIS >= this.startTime_PMILLIS + 5000 )
        {
            double avgFrameDuration_MILLIS = ( frameTime_PMILLIS - this.startTime_PMILLIS ) / ( ( double ) this.frameCount );
            double avgFps = 1000.0 / avgFrameDuration_MILLIS;
            System.err.format( "%9.1f ms/frame  (%.1f fps)\n", avgFrameDuration_MILLIS, avgFps );

            this.startTime_PMILLIS = frameTime_PMILLIS;
            this.frameCount = 0;
        }
    }

    @Override
    protected void doDispose( GlimpseContext context )
    { }

}
