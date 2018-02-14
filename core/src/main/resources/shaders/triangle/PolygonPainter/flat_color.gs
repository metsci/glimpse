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

layout( triangles ) in;

// A single triangle takes 3 vertices, but with duplication for wrapping, it
// can take arbitrarily many ... in practice we almost never need more than
// four copies of a segment, so use max_vertices = 4*3 = 12
layout( triangle_strip, max_vertices = 12 ) out;


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

float wrapValue( float value, float wrapMin, float wrapSpan )
{
    float wrapCount = floor( ( value - wrapMin ) / wrapSpan );
    return ( value - ( wrapCount * wrapSpan ) );
}


// RECT uniforms are (xMin, xMax, yMin, yMax)
uniform vec4 AXIS_RECT;
uniform vec4 WRAP_RECT;


void main( )
{
    vec4 posA_NDC = gl_in[ 0 ].gl_Position;
    vec4 posB_NDC = gl_in[ 1 ].gl_Position;
    vec4 posC_NDC = gl_in[ 2 ].gl_Position;


    // Compute render-shift values for wrapping
    //

    vec2 wrapMin_NDC = axisXyToNdc( rectMin( WRAP_RECT ), AXIS_RECT );
    vec2 wrapMax_NDC = axisXyToNdc( rectMax( WRAP_RECT ), AXIS_RECT );
    vec2 wrapSpan_NDC = wrapMax_NDC - wrapMin_NDC;

    float xShiftFirst_NDC;
    float xShiftStep_NDC;
    int xShiftCount;
    if ( isinf( wrapSpan_NDC.x ) )
    {
        xShiftFirst_NDC = 0.0;
        xShiftStep_NDC = 0.0;
        xShiftCount = 1;
    }
    else
    {
        float xMin_NDC = posA_NDC.x;
        xMin_NDC = min( xMin_NDC, posB_NDC.x );
        xMin_NDC = min( xMin_NDC, posC_NDC.x );

        float xMax_NDC = posA_NDC.x;
        xMax_NDC = max( xMax_NDC, posB_NDC.x );
        xMax_NDC = max( xMax_NDC, posC_NDC.x );

        xShiftFirst_NDC = wrapValue( xMin_NDC, wrapMin_NDC.x, wrapSpan_NDC.x ) - xMin_NDC;
        xShiftStep_NDC = wrapSpan_NDC.x;
        float xShiftCount0 = ceil( ( ( xMax_NDC + xShiftFirst_NDC ) - wrapMin_NDC.x ) / xShiftStep_NDC );
        xShiftCount = max( 0, int( xShiftCount0 ) );
    }

    float yShiftFirst_NDC;
    float yShiftStep_NDC;
    int yShiftCount;
    if ( isinf( wrapSpan_NDC.y ) )
    {
        yShiftFirst_NDC = 0.0;
        yShiftStep_NDC = 0.0;
        yShiftCount = 1;
    }
    else
    {
        float yMin_NDC = posA_NDC.y;
        yMin_NDC = min( yMin_NDC, posB_NDC.y );
        yMin_NDC = min( yMin_NDC, posC_NDC.y );

        float yMax_NDC = posA_NDC.y;
        yMax_NDC = max( yMax_NDC, posB_NDC.y );
        yMax_NDC = max( yMax_NDC, posC_NDC.y );

        yShiftFirst_NDC = wrapValue( yMin_NDC, wrapMin_NDC.y, wrapSpan_NDC.y ) - yMin_NDC;
        yShiftStep_NDC = wrapSpan_NDC.y;
        float yShiftCount0 = ceil( ( ( yMax_NDC + yShiftFirst_NDC ) - wrapMin_NDC.y ) / yShiftStep_NDC );
        yShiftCount = max( 0, int( yShiftCount0 ) );
    }

    vec2 shiftFirst_NDC = vec2( xShiftFirst_NDC, yShiftFirst_NDC );
    vec2 shiftStep_NDC = vec2( xShiftStep_NDC, yShiftStep_NDC );


    // Emit primitives for each render-shift
    //

    vec4 shift_NDC = vec4( 0.0, 0.0, 0.0, 0.0 );
    for ( int iShift = 0; iShift < xShiftCount; iShift++ )
    {
        for ( int jShift = 0; jShift < yShiftCount; jShift++ )
        {
            shift_NDC.xy = shiftFirst_NDC - vec2( float( iShift ), float( jShift ) )*shiftStep_NDC;

            gl_Position = posA_NDC + shift_NDC;
            EmitVertex( );

            gl_Position = posB_NDC + shift_NDC;
            EmitVertex( );

            gl_Position = posC_NDC + shift_NDC;
            EmitVertex( );

            EndPrimitive( );
        }
    }
}
