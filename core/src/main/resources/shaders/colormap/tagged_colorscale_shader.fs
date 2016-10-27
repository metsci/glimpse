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

// texture storing heat map data
uniform sampler2D datatex;

// texture storing color scale
uniform sampler1D colortex;

// texture storing data values associated with each axis tag
uniform sampler1D vcoordtex;

// texture storing texture coordinates associated with each axis tag
uniform sampler1D tcoordtex;

// the size of the vcoordtex and tcoordtex textures
uniform int size;

uniform float alpha;

// skips fragments with NaN values
uniform bool discardNaN;

// skips fragments above the highest value
uniform bool discardAbove;

// skips fragments below the lowest value
uniform bool discardBelow;

in vec2 vS;

out vec4 fRgba;

void main()
{
    // retrieve the data value for this texel
    float dataVal = texture( datatex, vS.st ).r;
    if( discardNaN )
    {
       // The isnan() function isn't defined in GLSL 1.20, which causes problems on OSX.
       // NaN semantics in GLSL are mostly unspecified, but "x != x" seems to work.
       //if( isnan( dataVal ) )
       if( dataVal != dataVal )
          discard;
    }

   float fsize = float(size);
    float numer = fsize - 0.5;

    float min_float_index = numer / fsize;
    if( discardBelow )
       if( dataVal < texture( vcoordtex, min_float_index ).r )
          discard;

    float max_float_index = 0.5 / fsize;
    if( discardAbove )
       if( dataVal > texture( vcoordtex, max_float_index ).r )
          discard;


    // loop through the axis tag values, finding the one just less than ours
    // older versions of glsl don't support texelFetch, so we use normal
    // texture1D to index into the texture, but offset by 0.5 to stay in the
    // middle of the texel
    float i = 1.5;
    for ( ; i < fsize ; i += 1.0 )
    {
        float float_index = i / fsize;
        float val = texture( vcoordtex, float_index ).r;
        if ( dataVal > val )
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

    float normalizedVal = ( ( dataVal - dataMin ) / ( dataMax - dataMin ) );
    normalizedVal = normalizedVal * ( tvalMax - tvalMin ) + tvalMin;
    normalizedVal = clamp( normalizedVal, 0.0, 1.0 );

    vec4 color = texture( colortex, normalizedVal );
    fRgba = color;
    fRgba.a = alpha;
}
