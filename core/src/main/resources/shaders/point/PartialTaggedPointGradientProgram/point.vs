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

// texture storing data values associated with each axis tag
uniform sampler1D vcoordtex;

// texture storing texture coordinates associated with each axis tag
uniform sampler1D tcoordtex;

// the size of the vcoordtex and tcoordtex textures
uniform int length;


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

 // the attribute
in float valColor;
in float valSize;
in vec2 a_position;

out vec4 vRgba;

void main()
{
    if( (!constant_size  && discardBelow_size  && valSize  < valMin_size  ) ||
        (!constant_size  && discardAbove_size  && valSize  > valMax_size  ) ||
        (!constant_color && discardBelow_color && valColor < valMin_color ) ||
        (!constant_color && discardAbove_color && valColor > valMax_color ) )
    {
    	gl_FrontColor = vec4( 0.0, 0.0, 0.0, 0.0 );
    	gl_PointSize = 0.0;
    }
    else
    {
    	if ( constant_color )
		{
			vRgba = color;
		}
		else
    	{
	        float fsize = float(size);
    		float numer = fsize - 0.5;

 			// loop through the axis tag values, finding the one just less than ours
 			// older versions of glsl don't support texelFetch, so we use normal
 			// texture1D to index into the texture, but offset by 0.5 to stay in the
 			// middle of the texel
    		float i = 1.5;
    		for ( ; i < fsize ; i += 1.0 )
    		{
     			float float_index = i / fsize;
    			float val = texture( vcoordtex, float_index ).r;
    			if ( valColor > val )
    				break;
    		}

    		// linearly interpolate between the texture and data coordinates
    		// for the two surrounding axis tags
    		float i1 = (i-1.0) / fsize;
 			float i2 = (i) / fsize;

 			float tvalMin = texture( tcoordtex, i1 ).r;
 			float tvalMax = texture( tcoordtex, i2 ).r;

 			float dataMin = texture( vcoordtex, i1 ).r;
 			float dataMax = texture( vcoordtex, i2 ).r;

 			float normalizedVal = ( ( valColor - dataMin ) / ( dataMax - dataMin ) );
 			normalizedVal = normalizedVal * ( tvalMax - tvalMin ) + tvalMin;
    		normalizedVal = clamp( normalizedVal, 0.0, 1.0 );

   		 	vec4 color = texture( valTexture_color, normalizedVal );
    		vRgba = color;
		}

		if ( constant_size )
		{
		    gl_PointSize = size;
		}
		else
		{
			float valInverseWidth_size = valMax_size - valMin_size;
	        float valNorm_size  = ( valSize - valMin_size ) / valInverseWidth_size;
	        valNorm_size = clamp( valNorm_size, 0.0, 1.0 );
	        gl_PointSize = texture( valTexture_size, valNorm_size ).r;
        }
    }

    gl_Position = gl_ModelViewProjectionMatrix * a_Position;
    vPointSize_PX = gl_PointSize;
}