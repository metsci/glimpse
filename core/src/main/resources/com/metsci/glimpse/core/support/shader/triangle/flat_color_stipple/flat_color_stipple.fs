//
// Copyright (c) 2020, Metron, Inc.
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//     * Redistributions of source code must retain the above copyright
//       notice, this list of conditions and the following disclaimer.
//     * Redistributions in binary form must reproduce the above copyright
//       notice, this list of conditions and the following disclaimer in the
//       documentation and/or other materials provided with the distribution.
//     * Neither the name of Metron, Inc. nor the
//       names of its contributors may be used to endorse or promote products
//       derived from this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL METRON, INC. BE LIABLE FOR ANY
// DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
// ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//

#version 150

uniform float FEATHER_THICKNESS_PX;
uniform int STIPPLE_ENABLE;
uniform float STIPPLE_SCALE;
uniform int STIPPLE_PATTERN;

uniform vec4 RGBA;

in float vMileage;

out vec4 outRgba;

void main( )
{
    // This assumes that the feather region is thinner than a single stipple-
    // bit region. The alternative would be more complicated than useful.

    float stippleAlpha;

    if ( STIPPLE_ENABLE == 0 )
    {
        stippleAlpha = 1.0;
    }
    else
    {
        float feather_PX = max( 0.01, FEATHER_THICKNESS_PX );
        float halfFeather_PX = 0.5 * feather_PX;
    
        float bitWidth_PX = STIPPLE_SCALE;
        float bitNum = mod( vMileage / bitWidth_PX, 16.0 );
        float bitAlpha = float( sign( STIPPLE_PATTERN & ( 0x1 << int( bitNum ) ) ) );
    
        float posInBit_PX = bitWidth_PX*( bitNum - floor( bitNum ) );
        if ( posInBit_PX < halfFeather_PX )
        {
            float bitNumPrev = mod( bitNum - 1.0, 16.0 );
            float bitAlphaPrev = float( sign( STIPPLE_PATTERN & ( 0x1 << int( bitNumPrev ) ) ) );
            float mixFrac = ( halfFeather_PX + posInBit_PX ) / feather_PX;
            stippleAlpha = mix( bitAlphaPrev, bitAlpha, mixFrac );
        }
        else if ( posInBit_PX > bitWidth_PX - halfFeather_PX )
        {
            float bitNumNext = mod( bitNum + 1.0, 16.0 );
            float bitAlphaNext = float( sign( STIPPLE_PATTERN & ( 0x1 << int( bitNumNext ) ) ) );
            float mixFrac = ( posInBit_PX - ( bitWidth_PX - halfFeather_PX ) ) / feather_PX;
            stippleAlpha = mix( bitAlpha, bitAlphaNext, mixFrac );
        }
        else
        {
            stippleAlpha = bitAlpha;
        }
    }

    outRgba.rgb = RGBA.rgb;
    outRgba.a = RGBA.a * clamp( stippleAlpha, 0.0, 1.0 );
}
