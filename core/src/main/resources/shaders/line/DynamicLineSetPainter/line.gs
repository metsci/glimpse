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

layout( lines ) in;
layout( triangle_strip, max_vertices = 4 ) out;

vec2 pxToNdc( vec2 xy_PX, vec2 viewportSize_PX )
{
    vec2 xy_FRAC = xy_PX / viewportSize_PX;
    return ( -1.0 + 2.0*xy_FRAC );
}

uniform vec2 VIEWPORT_SIZE_PX;
uniform float LINE_THICKNESS_PX;
uniform float FEATHER_THICKNESS_PX;

in vec4 vRgba[];

out vec2 gPosInQuad_PX;
out float gMileage_PX;
out float gQuadLength_PX;
out vec4 gRgba;

void main( )
{
    vec2 posA_PX = gl_in[ 0 ].gl_Position.xy;
    vec2 posB_PX = gl_in[ 1 ].gl_Position.xy;

    vec4 rgbaA = vRgba[ 0 ];
    vec4 rgbaB = vRgba[ 1 ];

    vec2 lineDelta_PX = posB_PX - posA_PX;
    float lineLength_PX = length( lineDelta_PX );

    if ( lineLength_PX > 0.0 )
    {
        float halfFeather_PX = 0.5 * FEATHER_THICKNESS_PX;

        vec2 parallelDir = lineDelta_PX / lineLength_PX;
        vec2 parallelOffset_PX = halfFeather_PX * parallelDir;

        vec2 normalDir = vec2( -parallelDir.y, parallelDir.x );
        float halfNormal_PX = 0.5*LINE_THICKNESS_PX + halfFeather_PX;
        vec2 normalOffset_PX = halfNormal_PX * normalDir;


        gl_Position.xy = pxToNdc( posA_PX - parallelOffset_PX + normalOffset_PX, VIEWPORT_SIZE_PX );
        gl_Position.zw = vec2( 0.0, 1.0 );
        gPosInQuad_PX = vec2( -halfFeather_PX, halfNormal_PX );
        gMileage_PX = 0;
        gQuadLength_PX = lineLength_PX;
        gRgba = rgbaA;
        EmitVertex( );

        gl_Position.xy = pxToNdc( posA_PX - parallelOffset_PX - normalOffset_PX, VIEWPORT_SIZE_PX );
        gl_Position.zw = vec2( 0.0, 1.0 );
        gPosInQuad_PX = vec2( -halfFeather_PX, -halfNormal_PX );
        gMileage_PX = 0;
        gQuadLength_PX = lineLength_PX;
        gRgba = rgbaA;
        EmitVertex( );

        gl_Position.xy = pxToNdc( posB_PX + parallelOffset_PX + normalOffset_PX, VIEWPORT_SIZE_PX );
        gl_Position.zw = vec2( 0.0, 1.0 );
        gPosInQuad_PX = vec2( lineLength_PX + halfFeather_PX, halfNormal_PX );
        gMileage_PX = lineLength_PX;
        gQuadLength_PX = lineLength_PX;
        gRgba = rgbaB;
        EmitVertex( );

        gl_Position.xy = pxToNdc( posB_PX + parallelOffset_PX - normalOffset_PX, VIEWPORT_SIZE_PX );
        gl_Position.zw = vec2( 0.0, 1.0 );
        gPosInQuad_PX = vec2( lineLength_PX + halfFeather_PX, -halfNormal_PX );
        gMileage_PX = lineLength_PX;
        gQuadLength_PX = lineLength_PX;
        gRgba = rgbaB;
        EmitVertex( );


        EndPrimitive( );
    }
}
