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

vec2 axisMin( vec4 axisRect )
{
    // Swizzle (xMin, yMin) out of (xMin, xMax, yMin, yMax)
    return axisRect.xz;
}

vec2 axisMax( vec4 axisRect )
{
    // Swizzle (xMax, yMax) out of (xMin, xMax, yMin, yMax)
    return axisRect.yw;
}

vec2 axisSize( vec4 axisRect )
{
    return ( axisMax( axisRect ) - axisMin( axisRect ) );
}

vec2 axisXyToPx( vec2 xy_AXIS, vec4 axisRect, vec2 viewportSize_PX )
{
    vec2 xy_FRAC = ( xy_AXIS - axisMin( axisRect ) ) / axisSize( axisRect );
    return ( xy_FRAC * viewportSize_PX );
}

bool isBetween( float v, float vMin, float vMax )
{
    return ( vMin <= v && v <= vMax );
}

bool isBetween( vec2 v, vec2 vMin, vec2 vMax )
{
    return ( isBetween( v.x, vMin.x, vMax.x ) && isBetween( v.y, vMin.y, vMax.y ) );
}


// If TIMELINE_MODE > 0, (t,z) is used as vertex position
// Otherwise, (x,y) is used as vertex position
uniform int TIMELINE_MODE;

// AXIS_RECT is (xMin, xMax, yMin, yMax)
uniform vec4 AXIS_RECT;
uniform vec2 VIEWPORT_SIZE_PX;

uniform vec4 RGBA_INSIDE_T_WINDOW;
uniform vec4 RGBA_OUTSIDE_T_WINDOW;
uniform float T_WINDOW_MIN;
uniform float T_WINDOW_MAX;

uniform float POINT_SIZE_OUTSIDE_XY_WINDOW_PX;
uniform float POINT_SIZE_INSIDE_XY_WINDOW_PX;
uniform vec2 XY_WINDOW_MIN;
uniform vec2 XY_WINDOW_MAX;


in vec4 inTxyz;


out vec4 vColor;
out float vPointSize_PX;


void main( )
{
    float t = inTxyz.x;
    vec2 xy = inTxyz.yz;
    vColor = ( isBetween( t, T_WINDOW_MIN, T_WINDOW_MAX ) ? RGBA_INSIDE_T_WINDOW : RGBA_OUTSIDE_T_WINDOW );
    vPointSize_PX = ( isBetween( xy, XY_WINDOW_MIN, XY_WINDOW_MAX ) ? POINT_SIZE_INSIDE_XY_WINDOW_PX : POINT_SIZE_OUTSIDE_XY_WINDOW_PX );

    vec2 xy_AXIS = ( TIMELINE_MODE > 0 ? inTxyz.xw : inTxyz.yz );
    gl_Position.xy = axisXyToPx( xy_AXIS, AXIS_RECT, VIEWPORT_SIZE_PX );
}
