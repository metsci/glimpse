
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

void main()
{
	// retrieve the data value for this texel
    float dataVal = texture2D( datatex, gl_TexCoord[0].st ).r;
    if( discardNaN )
       if( isnan( dataVal ) )
          discard;
 
 	float fsize = float(size);
    float numer = fsize - 0.5;
 
    float min_float_index = numer / fsize;
    if( discardBelow )
       if( dataVal < texture1D( vcoordtex, min_float_index ).r )
          discard;
          
    float max_float_index = 0.5 / fsize;
    if( discardAbove )
       if( dataVal > texture1D( vcoordtex, max_float_index ).r )
          discard;
    
 
 	// loop through the axis tag values, finding the one just less than ours
 	// older versions of glsl don't support texelFetch, so we use normal
 	// texture1D to index into the texture, but offset by 0.5 to stay in the
 	// middle of the texel 
    float i = 1.5;
    for ( ; i < fsize ; i += 1.0 )
    {
        float float_index = i / fsize; 
    	float val = texture1D( vcoordtex, float_index ).r;
    	if ( dataVal > val )
    		break;
    }
    
    // linearly interpolate between the texture and data coordinates
    // for the two surrounding axis tags
    float i1 = (i-1.0) / fsize; 
 	float i2 = (i) / fsize; 
 
 	float tvalMin = texture1D( tcoordtex, i1 ).r;
 	float tvalMax = texture1D( tcoordtex, i2 ).r;
 	
 	float dataMin = texture1D( vcoordtex, i1 ).r;
 	float dataMax = texture1D( vcoordtex, i2 ).r;
 	
 	float normalizedVal = ( ( dataVal - dataMin ) / ( dataMax - dataMin ) );
 	normalizedVal = normalizedVal * ( tvalMax - tvalMin ) + tvalMin;
    normalizedVal = clamp( normalizedVal, 0.0, 1.0 );

    vec4 color = texture1D( colortex, normalizedVal );
    gl_FragColor = color;
    gl_FragColor.a = alpha;
}