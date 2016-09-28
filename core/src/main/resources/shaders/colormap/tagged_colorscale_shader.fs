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
