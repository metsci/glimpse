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


vec2 rectMin( vec4 rect )
{
    // Swizzle (xMin, yMin) out of (xMin, xMax, yMin, yMax)
    return rect.xz;
}

vec2 rectMax( vec4 rect )
{
    // Swizzle (xMax, yMax) out of (xMin, xMax, yMin, yMax)
    return rect.yw;
}

vec2 rectSize( vec4 rect )
{
    return ( rectMax( rect ) - rectMin( rect ) );
}

vec2 axisXyToNdc( vec2 xy_AXIS, vec4 axisRect )
{
    vec2 xy_FRAC = ( xy_AXIS - rectMin( axisRect ) ) / rectSize( axisRect );
    return ( -1.0 + 2.0*xy_FRAC );
}

float near( vec2 nearFar )
{
    // Swizzle near out of (near, far)
    return nearFar.x;
}

float far( vec2 nearFar )
{
    // Swizzle far out of (near, far)
    return nearFar.y;
}

float axisZToNdc( float z_AXIS, vec2 axisNearFar )
{
    float near = near( axisNearFar );
    float far = far( axisNearFar );
    return ( z_AXIS - near ) / ( far - near );
}


uniform vec4 AXIS_RECT;
uniform vec2 NEAR_FAR;


in vec3 inXyz;


void main( )
{
    vec3 xyz_AXIS = inXyz;
    gl_Position.xy = axisXyToNdc( xyz_AXIS.xy, AXIS_RECT );
    gl_Position.z = axisZToNdc( xyz_AXIS.z, NEAR_FAR );
}
