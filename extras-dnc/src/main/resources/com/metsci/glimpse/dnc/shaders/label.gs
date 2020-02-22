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

layout( points ) in;
layout( triangle_strip, max_vertices = 4 ) out;

uniform vec2 VIEWPORT_SIZE_PX;
uniform vec2 ATLAS_SIZE_PX;
uniform float HIGHLIGHT_SCALE;

in float vHighlight[];
in vec2 vImageAlign[];
in vec4 vImageBounds[];

out vec2 gAtlasCoords;
out float gHighlight;

void main( )
{
    vec2 p = gl_in[ 0 ].gl_Position.xy;

    vec2 imageAlign = vImageAlign[ 0 ];

    vec4 imageBounds = vImageBounds[ 0 ];
    float sMin = imageBounds.s;
    float tMin = imageBounds.t;
    float sMax = imageBounds.p;
    float tMax = imageBounds.q;

    vec2 imageSize_PX = vec2( sMax - sMin, tMax - tMin ) * ATLAS_SIZE_PX;
    vec2 offsetA_PX = -imageAlign * imageSize_PX;
    vec2 offsetB_PX = offsetA_PX + imageSize_PX;
    bool highlight = ( vHighlight[ 0 ] >= 0.5 );
    if ( highlight )
    {
        offsetA_PX *= HIGHLIGHT_SCALE;
        offsetB_PX *= HIGHLIGHT_SCALE;
    }

    vec2 pxToNdc = 2.0 / VIEWPORT_SIZE_PX;
    vec2 offsetA = offsetA_PX * pxToNdc;
    vec2 offsetB = offsetB_PX * pxToNdc;


    gl_Position.xy = p + vec2( offsetA.x, offsetB.y );
    gl_Position.zw = vec2( 0.0, 1.0 );
    gAtlasCoords = vec2( sMin, tMin );
    gHighlight = vHighlight[ 0 ];
    EmitVertex( );

    gl_Position.xy = p + offsetB;
    gl_Position.zw = vec2( 0.0, 1.0 );
    gAtlasCoords = vec2( sMax, tMin );
    gHighlight = vHighlight[ 0 ];
    EmitVertex( );

    gl_Position.xy = p + offsetA;
    gl_Position.zw = vec2( 0.0, 1.0 );
    gAtlasCoords = vec2( sMin, tMax );
    gHighlight = vHighlight[ 0 ];
    EmitVertex( );

    gl_Position.xy = p + vec2( offsetB.x, offsetA.y );
    gl_Position.zw = vec2( 0.0, 1.0 );
    gAtlasCoords = vec2( sMax, tMax );
    gHighlight = vHighlight[ 0 ];
    EmitVertex( );


    EndPrimitive( );
}
