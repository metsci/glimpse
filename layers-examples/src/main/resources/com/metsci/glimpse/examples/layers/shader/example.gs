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
uniform float FEATHER_THICKNESS_PX;


in vec4[] vColor;
in float[] vPointSize_PX;


out vec2 gXy_PX;
out vec4 gColor;
out float gPointSize_PX;


void main( )
{
    vec2 center_PX = gl_in[ 0 ].gl_Position.xy;
    vec2 center_NDC = -1.0 + center_PX / ( 0.5 * VIEWPORT_SIZE_PX );

    float pointSize_PX = vPointSize_PX[ 0 ];
    float offset_PX = 0.5*pointSize_PX + 0.5*FEATHER_THICKNESS_PX;
    vec2 offset_NDC = offset_PX / ( 0.5 * VIEWPORT_SIZE_PX );

    float top_NDC = center_NDC.y + offset_NDC.y;
    float left_NDC = center_NDC.x - offset_NDC.x;
    float right_NDC = center_NDC.x + offset_NDC.x;
    float bottom_NDC = center_NDC.y - offset_NDC.y;

    vec4 color = vColor[ 0 ];

    gl_Position = vec4( left_NDC, bottom_NDC, 0.0, 1.0 );
    gXy_PX = vec2( -offset_PX, -offset_PX );
    gPointSize_PX = pointSize_PX;
    gColor = color;
    EmitVertex( );

    gl_Position = vec4( left_NDC, top_NDC, 0.0, 1.0 );
    gXy_PX = vec2( -offset_PX, +offset_PX );
    gPointSize_PX = pointSize_PX;
    gColor = color;
    EmitVertex( );

    gl_Position = vec4( right_NDC, bottom_NDC, 0.0, 1.0 );
    gXy_PX = vec2( +offset_PX, -offset_PX );
    gPointSize_PX = pointSize_PX;
    gColor = color;
    EmitVertex( );

    gl_Position = vec4( right_NDC, top_NDC, 0.0, 1.0 );
    gXy_PX = vec2( +offset_PX, +offset_PX );
    gPointSize_PX = pointSize_PX;
    gColor = color;
    EmitVertex( );

    EndPrimitive( );
}
