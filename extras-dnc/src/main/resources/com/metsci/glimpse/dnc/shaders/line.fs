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
uniform vec4 RGBA;
uniform int STIPPLE_ENABLE;
uniform float STIPPLE_FACTOR;
uniform int STIPPLE_PATTERN;

in float gLineThickness_PX;
in vec2 gPosInQuad_PX;

out vec4 outRgba;

void main( )
{
    float tFeatherStart_PX = 0.5*( gLineThickness_PX - FEATHER_THICKNESS_PX );
    float fade = clamp( ( abs( gPosInQuad_PX.t ) - tFeatherStart_PX ) / FEATHER_THICKNESS_PX, 0.0, 1.0 );

    if ( STIPPLE_ENABLE != 0 )
    {
        float bitNum = mod( gPosInQuad_PX.s / STIPPLE_FACTOR, 16 );
        int bitMask = ( 0x1 << int( bitNum ) );
        if ( ( STIPPLE_PATTERN & bitMask ) == 0 )
        {
            discard;
            return;
        }
    }

    float alpha = ( 1.0 - fade ) * RGBA.a;
    outRgba.rgb = RGBA.rgb * alpha;
    outRgba.a = alpha;
}
