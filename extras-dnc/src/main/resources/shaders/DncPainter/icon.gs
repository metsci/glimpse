//
// Copyright (c) 2019, Metron, Inc.
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

layout( points ) in;
layout( triangle_strip, max_vertices = 4 ) out;

vec2 rotate( float x, float y, float cosR, float sinR )
{
    return vec2( x*cosR - y*sinR, x*sinR + y*cosR );
}

uniform vec2 VIEWPORT_SIZE_PX;
uniform vec2 IMAGE_SIZE_PX;
uniform vec2 IMAGE_ALIGN;
uniform vec4 IMAGE_BOUNDS;
uniform float HIGHLIGHT_SCALE;

in float vHighlight[];
in float vRotation_CCWRAD[];

out vec2 gAtlasCoords;

void main( )
{
    vec2 p = gl_in[ 0 ].gl_Position.xy;

    float rotation_CCWRAD = vRotation_CCWRAD[ 0 ];
    float cosR = cos( rotation_CCWRAD );
    float sinR = sin( rotation_CCWRAD );

    vec2 offsetA_PX = -IMAGE_ALIGN * IMAGE_SIZE_PX;
    vec2 offsetB_PX = offsetA_PX + IMAGE_SIZE_PX;
    bool highlight = ( vHighlight[ 0 ] >= 0.5 );
    if ( highlight )
    {
        offsetA_PX *= HIGHLIGHT_SCALE;
        offsetB_PX *= HIGHLIGHT_SCALE;
    }

    vec2 pxToNdc = 2.0 / VIEWPORT_SIZE_PX;

    float sMin = IMAGE_BOUNDS.s;
    float tMin = IMAGE_BOUNDS.t;
    float sMax = IMAGE_BOUNDS.p;
    float tMax = IMAGE_BOUNDS.q;


    gl_Position.xy = p + rotate( offsetA_PX.x, offsetB_PX.y, cosR, sinR )*pxToNdc;
    gl_Position.zw = vec2( 0.0, 1.0 );
    gAtlasCoords = vec2( sMin, tMin );
    EmitVertex( );

    gl_Position.xy = p + rotate( offsetB_PX.x, offsetB_PX.y, cosR, sinR )*pxToNdc;
    gl_Position.zw = vec2( 0.0, 1.0 );
    gAtlasCoords = vec2( sMax, tMin );
    EmitVertex( );

    gl_Position.xy = p + rotate( offsetA_PX.x, offsetA_PX.y, cosR, sinR )*pxToNdc;
    gl_Position.zw = vec2( 0.0, 1.0 );
    gAtlasCoords = vec2( sMin, tMax );
    EmitVertex( );

    gl_Position.xy = p + rotate( offsetB_PX.x, offsetA_PX.y, cosR, sinR )*pxToNdc;
    gl_Position.zw = vec2( 0.0, 1.0 );
    gAtlasCoords = vec2( sMax, tMax );
    EmitVertex( );


    EndPrimitive( );
}
