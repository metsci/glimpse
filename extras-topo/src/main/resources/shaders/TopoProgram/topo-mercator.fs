//
// Copyright (c) 2016, Metron, Inc.
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

#define PI 3.14159265358979323846
#define HALF_PI 1.57079632679489661923


uniform sampler2D DATA_TEX_UNIT;
uniform float DATA_DENORM_FACTOR;

uniform sampler1D BATHY_COLORMAP_TEX_UNIT;
uniform float BATHY_COLORMAP_MIN_VALUE;

uniform sampler1D TOPO_COLORMAP_TEX_UNIT;
uniform float TOPO_COLORMAP_MAX_VALUE;

uniform float Y_MIN;
uniform float Y_SPAN;
uniform float LAT_MIN_DEG;
uniform float LAT_SPAN_DEG;

in vec2 vSt;

out vec4 outRgba;


void main( )
{

    // For Plate-Carree, fraction-of-surface coords and fraction-of-texture coords are identical
    vec2 uv = vSt;



    // FIXME: Account for border
    float y = Y_MIN + ( vSt.t * Y_SPAN );
    float latFrac = atan( exp( y ) ) / HALF_PI;
    float lat_DEG = -90.0 + ( latFrac * 180.0 );
    float v = ( lat_DEG - LAT_MIN_DEG ) / LAT_SPAN_DEG;



    float zLinear = DATA_DENORM_FACTOR * texture( DATA_TEX_UNIT, uv ).r;

    ivec2 textureSize = textureSize( DATA_TEX_UNIT, 0 );
    vec2 uvNearest = ( floor( uv * textureSize ) + 0.5 ) / textureSize;
    float zNearest = DATA_DENORM_FACTOR * texture( DATA_TEX_UNIT, uvNearest ).r;

    if ( zNearest < 0.0 )
    {
        float bathyFrac = clamp( ( zLinear - BATHY_COLORMAP_MIN_VALUE ) / ( 0.0 - BATHY_COLORMAP_MIN_VALUE ), 0.0, 1.0 );
        outRgba = texture( BATHY_COLORMAP_TEX_UNIT, bathyFrac );
    }
    else
    {
        float topoFrac = clamp( ( zLinear - 0.0 ) / ( TOPO_COLORMAP_MAX_VALUE - 0.0 ), 0.0, 1.0 );
        outRgba = texture( TOPO_COLORMAP_TEX_UNIT, topoFrac );
    }

}
