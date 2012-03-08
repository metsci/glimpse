
// texture storing data values associated with each axis tag
uniform sampler1D vcoordtex;

// texture storing texture coordinates associated with each axis tag
uniform sampler1D tcoordtex;

// the size of the vcoordtex and tcoordtex textures
uniform int size;


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

 // the attribute
attribute float valColor;
attribute float valSize;



void main()
{
    if( (!constant_size  && discardBelow_size  && valSize  < valMin_size  ) ||
        (!constant_size  && discardAbove_size  && valSize  > valMax_size  ) ||
        (!constant_color && discardBelow_color && valColor < valMin_color ) ||
        (!constant_color && discardAbove_color && valColor > valMax_color ) )
    {
    	gl_FrontColor = vec4( 0.0, 0.0, 0.0, 0.0 );
    }
    else
    {
    	if ( !constant_color )
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
    			float val = texture1D( vcoordtex, float_index ).r;
    			if ( valColor > val )
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

 			float normalizedVal = ( ( valColor - dataMin ) / ( dataMax - dataMin ) );
 			normalizedVal = normalizedVal * ( tvalMax - tvalMin ) + tvalMin;
    		clamp( normalizedVal, 0.0, 1.0 );

   		 	vec4 color = texture1D( valTexture_color, normalizedVal );
    		gl_FrontColor = color;

		}
		else
		{
			gl_FrontColor = gl_Color;
		}

		if ( !constant_size )
		{
			float valInverseWidth_size = valMax_size - valMin_size;
	        float valNorm_size  = ( valSize - valMin_size ) / valInverseWidth_size;
	        clamp( valNorm_size, 0.0, 1.0 );
	        gl_PointSize = texture1D( valTexture_size, valNorm_size ).r;
        }
    }

    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
}