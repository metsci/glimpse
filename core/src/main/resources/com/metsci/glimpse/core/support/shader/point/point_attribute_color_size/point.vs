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

// lookup for color
uniform sampler1D valTexture_color;
uniform float valMin_color;
uniform float valMax_color;

// lookup for point size
uniform sampler1D valTexture_size;
uniform float valMin_size;
uniform float valMax_size;

// visibility thresholds
uniform bool discardBelow_color;
uniform bool discardAbove_color;
uniform bool discardBelow_size;
uniform bool discardAbove_size;

uniform bool constant_color;
uniform bool constant_size;

uniform vec4 color;
uniform float size;

uniform mat4 mvpMatrix;

in float valColor;
in float valSize;
in vec2 a_position;

out vec4 vRgba;
out float vPointSize_PX;

void main()
{
    float pointSize;

    if( (!constant_size  && discardBelow_size  && valSize  < valMin_size  ) ||
        (!constant_size  && discardAbove_size  && valSize  > valMax_size  ) ||
        (!constant_color && discardBelow_color && valColor < valMin_color ) ||
        (!constant_color && discardAbove_color && valColor > valMax_color ) )
    {
    	vRgba = vec4( 0.0, 0.0, 0.0, 0.0 );
    	pointSize = 0.0;
    }
    else
    {
    	if ( constant_color )
		{
			vRgba = color;
		}
		else
    	{
	    	float valInverseWidth_color = valMax_color - valMin_color;
	        float valNorm_color = ( valColor - valMin_color ) / valInverseWidth_color;
	        valNorm_color = clamp( valNorm_color, 0.0, 1.0 );
	        vRgba = texture( valTexture_color, valNorm_color );
		}

		if ( constant_size )
		{
		    pointSize = size;
		}
		else
		{
			float valInverseWidth_size = valMax_size - valMin_size;
	        float valNorm_size  = ( valSize - valMin_size ) / valInverseWidth_size;
	        valNorm_size = clamp( valNorm_size, 0.0, 1.0 );
	        pointSize = texture( valTexture_size, valNorm_size ).r;
        }
    }

    gl_Position = mvpMatrix * vec4( a_position, 0, 1 );
    gl_PointSize = pointSize;
    vPointSize_PX = pointSize;
}