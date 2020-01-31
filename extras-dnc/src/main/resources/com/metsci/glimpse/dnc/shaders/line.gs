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

layout( lines ) in;
layout( triangle_strip, max_vertices = 4 ) out;

vec2 rotate( float x, float y, float cosR, float sinR )
{
    return vec2( x*cosR - y*sinR, x*sinR + y*cosR );
}

uniform vec2 VIEWPORT_SIZE_PX;
uniform float FEATHER_THICKNESS_PX;

in float vLineThickness_PX[];
in float vCumulativeDistance_PX[];

out float gLineThickness_PX;
out vec2 gPosInQuad_PX;

void main( )
{
    vec2 posA = gl_in[ 0 ].gl_Position.xy;
    vec2 posB = gl_in[ 1 ].gl_Position.xy;
    vec2 lineDelta = posB - posA;
    float lineLength = length( lineDelta );

    float cumulativeDistanceA_PX = vCumulativeDistance_PX[ 0 ];
    float cumulativeDistanceB_PX = vCumulativeDistance_PX[ 1 ];

    if ( lineLength > 0.0 && cumulativeDistanceB_PX >= cumulativeDistanceA_PX )
    {
        vec2 lineDir = lineDelta / lineLength;
        vec2 lineNormal = vec2( -lineDir.y, lineDir.x );

        vec2 pxToNdc = 2.0 / VIEWPORT_SIZE_PX;
        float lineThickness_PX = vLineThickness_PX[ 0 ];
        float quadHalfHeight_PX = lineThickness_PX + FEATHER_THICKNESS_PX;
        vec2 edgeDelta = quadHalfHeight_PX * pxToNdc * lineNormal;


        gl_Position.xy = posA + edgeDelta;
        gl_Position.zw = vec2( 0.0, 1.0 );
        gLineThickness_PX = lineThickness_PX;
        gPosInQuad_PX = vec2( cumulativeDistanceA_PX, quadHalfHeight_PX );
        EmitVertex( );

        gl_Position.xy = posA - edgeDelta;
        gl_Position.zw = vec2( 0.0, 1.0 );
        gLineThickness_PX = lineThickness_PX;
        gPosInQuad_PX = vec2( cumulativeDistanceA_PX, -quadHalfHeight_PX );
        EmitVertex( );

        gl_Position.xy = posB + edgeDelta;
        gl_Position.zw = vec2( 0.0, 1.0 );
        gLineThickness_PX = lineThickness_PX;
        gPosInQuad_PX = vec2( cumulativeDistanceB_PX, quadHalfHeight_PX );
        EmitVertex( );

        gl_Position.xy = posB - edgeDelta;
        gl_Position.zw = vec2( 0.0, 1.0 );
        gLineThickness_PX = lineThickness_PX;
        gPosInQuad_PX = vec2( cumulativeDistanceB_PX, -quadHalfHeight_PX );
        EmitVertex( );


        EndPrimitive( );
    }
}
