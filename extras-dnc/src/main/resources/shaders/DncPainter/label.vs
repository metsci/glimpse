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
#extension GL_EXT_gpu_shader4 : enable

vec2 axisMin( vec4 axisRect )
{
    return axisRect.xy;
}

vec2 axisSize( vec4 axisRect )
{
    return axisRect.zw;
}

vec4 axisXyToNdc( vec2 xy_AXIS, vec4 axisRect )
{
    vec2 xy_FRAC = ( xy_AXIS - axisMin( axisRect ) ) / axisSize( axisRect );
    return vec4( 2.0*xy_FRAC - 1.0, 0.0, 1.0 );
}

bool setContains( isampler2D setTexture, float index )
{
    ivec2 textureSize = textureSize( setTexture, 0 );
    if ( textureSize.x == 0 || textureSize.y == 0 )
    {
        return false;
    }
    else
    {
        float j = floor( index / textureSize.x );
        float i = index - ( j * textureSize.x );
        vec2 st = vec2( i, j ) / textureSize;
        int value = texture( setTexture, st ).r;
        return ( value != 0 );
    }
}

uniform vec4 AXIS_RECT;
uniform isampler2D HIGHLIGHT_SET;

in vec3 inLabelVertex;
in vec2 inImageAlign;
in vec4 inImageBounds;

out float vHighlight;
out vec2 vImageAlign;
out vec4 vImageBounds;

void main( )
{
    float featureNum = inLabelVertex.z;
    vHighlight = ( setContains( HIGHLIGHT_SET, featureNum ) ? 1.0 : 0.0 );

    vImageAlign = inImageAlign;
    vImageBounds = inImageBounds;

    vec2 xy_AXIS = inLabelVertex.xy;
    gl_Position = axisXyToNdc( xy_AXIS, AXIS_RECT );
}
