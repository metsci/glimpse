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

mat4 kermat = mat4(
  0, -0.5, 1, -0.5,
  1, 0, -2.5, 1.5,
  0, 0.5, 2, -1.5,
  0, 0, -0.5, 0.5
);

in vec2 gSt;

out vec4 outRgba;

// This is the Keys-type interpolator in 1D
float kernel( float f, vec4 vals )
{
    float f2 = f*f;
    vec4 v = vec4(1, f, f2, f2*f);
    return dot(v, kermat * vals);
}

float fixupLeft( mat4 points, int m )
{
    float val = points[0][m+1];
    if (isnan(val)) {
       val = 2*points[1][m+1] - points[2][m+1];
    }
    return val;
}

float fixupRight( mat4 points, int m )
{
    float val = points[3][m+1];
    if (isnan(val)) {
       val = 2*points[2][m+1] - points[1][m+1];
    }
    return val;
}

float fixupTop( mat4 points, int n )
{
    float val = points[n+1][3];
    if (isnan(val)) {
       val = 2*points[n+1][2] - points[n+1][1];
    }
    return val;
}

float fixupBot( mat4 points, int n )
{
    float val = points[n+1][0];
    if (isnan(val)) {
        val = 2*points[n+1][1] - points[n+1][2];
    }
    return val;
}

void main()
{
    ivec2 texSize = textureSize( datatex, 0 );

    if ( texSize.x < 2 || texSize.y < 2) {
        vec4 color = texture( colortex, 0 );
        outRgba = color;
        if ( overrideAlpha ) {
            outRgba.a = alpha;
        }
        return;
    }

    // get texel size
    float texelSizeX = 1.0 / texSize.x;
    float texelSizeY = 1.0 / texSize.y;

    float exactVal = texture2D( datatex, gSt ).r;
    if( isnan( exactVal ) && discardNaN )
        discard;

    // otherwise the interpolated pixels are shifted down and left
    vec2 gSS = gSt - vec2( 0.5 * texelSizeX, 0.5 * texelSizeY );

    float a = fract( gSS.x * texSize.x );
    float b = fract( gSS.y * texSize.y );

    vec4 row = vec4(0);
    mat4 points = mat4(0);
    for ( int m = -1; m <= 2; m++ )
    {
        for ( int n = -1; n <= 2; n++ )
        {
            float data = texture2D( datatex, gSS + vec2( texelSizeX * float( m ), texelSizeY * float( n ) ) ).r;
            points[m+1][n+1] = data;
        }
    }
    // build from the center which we know is valid; extrapolate edges if missing
    for (int m=0; m <= 1; m++) {
        points[m+1][0] = fixupBot(points, m);
        points[m+1][3] = fixupTop(points, m);
    }
    for (int n=0; n <= 1; n++) {
        points[0][n+1] = fixupLeft(points, n);
        points[3][n+1] = fixupRight(points, n);
    }
    points[0][0] = 0.5 * (fixupLeft(points, -1) + fixupBot(points, -1));
    points[0][3] = 0.5 * (fixupLeft(points, 2) + fixupTop(points, -1));
    points[3][0] = 0.5 * (fixupRight(points, -1) + fixupBot(points, 2));
    points[3][3] = 0.5 * (fixupRight(points, 2) + fixupTop(points, 2));

    for (int m=-1; m<=2; m++) {
        vec4 col = points[m+1];
        row[m+1] = kernel(b, col);
    }

    float dataVal = kernel(a, row);

    if( discardAbove && dataVal > dataMax )
        discard;
    if( discardBelow && dataVal < dataMin )
        discard;

    float normalizedVal = ( dataVal - dataMin ) / ( dataMax - dataMin );
    normalizedVal = clamp( normalizedVal, 0.0, 1.0 );

    vec4 color = texture( colortex, normalizedVal );
    outRgba = color;
    if ( overrideAlpha )
        outRgba.a = alpha;
}

