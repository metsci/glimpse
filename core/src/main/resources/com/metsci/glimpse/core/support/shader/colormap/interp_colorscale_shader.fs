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

uniform sampler2D datatex;
uniform sampler1D colortex;

uniform float dataMin;
uniform float dataMax;

uniform float alpha;

uniform bool overrideAlpha;
uniform bool discardNaN;

// skips fragments outside our color range
uniform bool discardAbove;
uniform bool discardBelow;

in vec2 vS;

out vec4 fRgba;

// This is a bicubic b-spline kernel from https://www.codeproject.com/Articles/236394/Bi-Cubic-and-Bi-Linear-Interpolation-with-GLSL#BSpline
float kernel( float f )
{
    if( f < 0.0 )
    {
        f = -f;
    }

    if( f >= 0.0 && f <= 1.0 )
    {
        return ( 2.0 / 3.0 ) + ( 0.5 ) * ( f * f * f ) - ( f * f );
    }
    else if( f > 1.0 && f <= 2.0 )
    {
        return 1.0 / 6.0 * pow( ( 2.0 - f ), 3.0 );
    }
    else
    {
        return 1.0;
    }
}

// The isnan() function isn't defined in GLSL 1.20, which causes problems on OSX.
bool checkNaN( float f )
{
    return ! ( f < 0.0 || 0.0 < f || f == 0.0 );
}

void main()
{
    ivec2 texSize = textureSize( datatex, 0 );

    // get texel size
    float texelSizeX = 1.0 / texSize.x;
    float texelSizeY = 1.0 / texSize.y;

    float exactVal = texture2D( datatex, vS ).r;
    if( checkNaN( exactVal ) && discardNaN )
        discard;

    // otherwise the interpolated pixels are shifted down and left
    vec2 vSS = vS - vec2( 0.5 * texelSizeX, 0.5 * texelSizeY );

    float a = fract( vSS.x * texSize.x );
    float b = fract( vSS.y * texSize.y );

    float sum = 0;
    float denom = 0;
    for ( int m = -1; m <= 2; m++ )
    {
        for ( int n = -1; n <= 2; n++ )
        {
            float data = texture2D( datatex, vSS + vec2( texelSizeX * float( m ), texelSizeY * float( n ) ) ).r;
            if( checkNaN( data ) )
            {
                continue;
            }

            float f1 = kernel( a - float( m ) );
            float f2 = kernel( b - float( n ) );

            sum = sum + ( data * f1 * f2  );
            denom = denom + ( f1 * f2 );
        }
    }

    float dataVal = sum / denom;

    if( discardAbove && dataVal > dataMax )
        discard;
    if( discardBelow && dataVal < dataMin )
        discard;

    float normalizedVal = ( dataVal - dataMin ) / ( dataMax - dataMin );
    normalizedVal = clamp( normalizedVal, 0.0, 1.0 );

    vec4 color = texture( colortex, normalizedVal );
    fRgba = color;
    if ( overrideAlpha )
        fRgba.a = alpha;
}

