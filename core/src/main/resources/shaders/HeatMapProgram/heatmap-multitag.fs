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


uniform sampler2D VALUES_TEXUNIT;
uniform sampler1D COLORMAP_TEXUNIT;

uniform sampler1D TAG_FRACTIONS_TEXUNIT;
uniform sampler1D TAG_VALUES_TEXUNIT;

uniform float ALPHA;
uniform bool USE_COLORMAP_ALPHA;
uniform bool DISCARD_NAN;
uniform bool DISCARD_ABOVE;
uniform bool DISCARD_BELOW;


in vec2 gSt;

out vec4 outRgba;


void main( )
{
    float value = texture( VALUES_TEXUNIT, gSt ).r;

    if ( DISCARD_NAN && isnan( value ) )
    {
        discard;
    }

    int numTags = textureSize( TAG_VALUES_TEXUNIT, 0 );

    if ( DISCARD_BELOW && value < texelFetch( TAG_VALUES_TEXUNIT, numTags-1, 0 ).r )
    {
        discard;
    }

    if ( DISCARD_ABOVE && value > texelFetch( TAG_VALUES_TEXUNIT, 0, 0 ).r )
    {
        discard;
    }

    // Find the tags above and below value
    int tagIndexBelow;
    for ( tagIndexBelow = 1; tagIndexBelow < numTags; tagIndexBelow++ )
    {
        if ( value > texelFetch( TAG_VALUES_TEXUNIT, tagIndexBelow, 0 ).r )
        {
            break;
        }
    }
    int tagIndexAbove = tagIndexBelow - 1;

    // Interpolate between the tags above and below value
    float tagFractionAbove = texelFetch( TAG_FRACTIONS_TEXUNIT, tagIndexAbove, 0 ).r;
    float tagFractionBelow = texelFetch( TAG_FRACTIONS_TEXUNIT, tagIndexBelow, 0 ).r;
    float tagValueAbove = texelFetch( TAG_VALUES_TEXUNIT, tagIndexAbove, 0 ).r;
    float tagValueBelow = texelFetch( TAG_VALUES_TEXUNIT, tagIndexBelow, 0 ).r;
    float value_SEGMENTFRAC = ( value - tagValueBelow ) / ( tagValueAbove - tagValueBelow );
    float value_FRAC = tagFractionBelow + value_SEGMENTFRAC*( tagFractionAbove - tagFractionBelow );

	if( USE_COLORMAP_ALPHA )
	{
    	outRgba.rgba = texture( COLORMAP_TEXUNIT, clamp( value_FRAC, 0.0, 1.0 ) ).rgba;
    }
    else
    {
    	outRgba.rgb = texture( COLORMAP_TEXUNIT, clamp( value_FRAC, 0.0, 1.0 ) ).rgb;
    	outRgba.a = ALPHA;
   	}
}
