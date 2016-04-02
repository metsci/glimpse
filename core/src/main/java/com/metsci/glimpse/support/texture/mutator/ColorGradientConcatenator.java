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
package com.metsci.glimpse.support.texture.mutator;

import java.nio.FloatBuffer;

import com.metsci.glimpse.gl.texture.ColorTexture1D.MutatorColor1D;
import com.metsci.glimpse.support.colormap.ColorGradient;

public class ColorGradientConcatenator implements MutatorColor1D
{
    protected ColorGradient[] colorGradients;

    public ColorGradientConcatenator( ColorGradient... gradients )
    {
        this.colorGradients = gradients;
    }

    @Override
    public void mutate( FloatBuffer floatBuffer, int dim )
    {
        for ( int i = 0; i < colorGradients.length; i++ )
        {
            ColorGradient colorScale = colorGradients[i];

            int size = ( int ) Math.floor( ( double ) dim / ( double ) colorGradients.length );
            int startIndex = size * i;
            int endIndex = Math.min( size * ( i + 1 ), dim );

            mutate( floatBuffer, colorScale, startIndex, endIndex );
        }
    }

    protected void mutate( FloatBuffer floatBuffer, ColorGradient colorGradient, int startIndex, int endIndex )
    {
        floatBuffer.position( startIndex * 4 );

        float[] rgbaBytes = new float[4];

        int length = endIndex - startIndex;
        for ( int i = 0; i < length; i++ )
        {
            colorGradient.toColor( i / ( float ) ( length - 1 ), rgbaBytes );

            floatBuffer.put( rgbaBytes[0] );
            floatBuffer.put( rgbaBytes[1] );
            floatBuffer.put( rgbaBytes[2] );
            floatBuffer.put( rgbaBytes[3] );
        }
    }
}
