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

uniform float ORIGIN_LON_RAD;

uniform sampler2D DATA_TEX_UNIT;
uniform float DATA_DENORM_FACTOR;
uniform float DATA_LAT_MAX_RAD;
uniform float DATA_LAT_SPAN_RAD;
uniform float DATA_LON_MIN_RAD;
uniform float DATA_LON_SPAN_RAD;

uniform sampler1D BATHY_COLORMAP_TEX_UNIT;
uniform float BATHY_COLORMAP_MIN_VALUE;
uniform sampler1D TOPO_COLORMAP_TEX_UNIT;
uniform float TOPO_COLORMAP_MAX_VALUE;

in vec2 vXy;

out vec4 outRgba;


void main( )
{

    float x = vXy.x;
    float y = vXy.y;

    float lon_RAD = ORIGIN_LON_RAD + x;
    float lat_RAD = y;

    float u = ( lon_RAD - DATA_LON_MIN_RAD ) / DATA_LON_SPAN_RAD;
    float v = ( DATA_LAT_MAX_RAD - lat_RAD ) / DATA_LAT_SPAN_RAD;
    vec2 uv = vec2( u, v );

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
