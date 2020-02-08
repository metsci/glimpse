//
// Copyright (c) 2019 Metron, Inc.
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

uniform sampler2D elevtex;
uniform sampler2D shadetex;

uniform float alpha;

uniform vec4 colors[20];
uniform int nColors;

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

vec3 hsv2rgb( vec3 c )
{
    vec4 K = vec4( 1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0 );
    vec3 p = abs( fract( c.xxx + K.xyz ) * 6.0 - K.www );
    return c.z * mix( K.xxx, clamp( p - K.xxx, 0.0, 1.0 ), c.y );
}

vec3 colorize( float shade, float elev )
{
    vec3 hsb = vec3( 0, 0, 0 );
    for ( int i = 0; i < nColors; i++ )
    {
        if ( elev <= colors[i].x )
        {
            hsb = colors[i].yzw;
            break;
        }
    }

    float alpha = clamp( ( shade - 0.4f ), 0, 0.6f ) + 0.7f;
    hsb.z = clamp( hsb.z * alpha, 0, 1 );
    return hsv2rgb( hsb );
}

void main()
{
    ivec2 texSize = textureSize( elevtex, 0 );

    // get texel size
    float texelSizeX = 1.0 / texSize.x;
    float texelSizeY = 1.0 / texSize.y;
    float a = fract( vS.x * texSize.x );
    float b = fract( vS.y * texSize.y );

    float sumElev = 0;
    float sumShade = 0;
    float denom = 0;
    for ( int m = -1; m <= 2; m++ )
    {
        for ( int n = -1; n <= 2; n++ )
        {
			float elev = texture2D( elevtex, vS + vec2( texelSizeX * float( m ), texelSizeY * float( n ) ) ).r;
			float shade = texture2D( shadetex, vS + vec2( texelSizeX * float( m ), texelSizeY * float( n ) ) ).r;
			float f1 = kernel( float( m ) - a );
			float f2 = kernel( -float( n ) + b );

            sumElev = sumElev + ( elev * f1 * f2  );
            sumShade = sumShade + ( shade * f1 * f2 );
            denom = denom + ( f1 * f2 );
        }
    }

    float elev = sumElev / denom;
    float shade = sumShade / denom;
    
    vec3 rgb = colorize( shade, elev );
    fRgba = vec4( rgb, alpha );
}

