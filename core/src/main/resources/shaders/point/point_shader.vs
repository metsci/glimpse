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

uniform mat4 mvpMatrix;

 // the attribute
attribute float valColor;
attribute float valSize;
attribute vec2 a_position;

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
	    	float valInverseWidth_color = valMax_color - valMin_color;
	        float valNorm_color = ( valColor - valMin_color ) / valInverseWidth_color;
	        clamp( valNorm_color, 0.0, 1.0 );
	        gl_FrontColor = texture1D( valTexture_color, valNorm_color );
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

    gl_Position = mvpMatrix * vec4( a_position, 0, 1 );
}