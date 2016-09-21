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

// the attribute
in float valColor;
in float valSize;
in vec2 a_position;

out vec4 vRgba;
out float vPointSize_PX;

void main()
{
    if( (!constant_size  && discardBelow_size  && valSize  < valMin_size  ) ||
        (!constant_size  && discardAbove_size  && valSize  > valMax_size  ) ||
        (!constant_color && discardBelow_color && valColor < valMin_color ) ||
        (!constant_color && discardAbove_color && valColor > valMax_color ) )
    {
    	vRgba = vec4( 0.0, 0.0, 0.0, 0.0 );
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
	    	float valInverseWidth_color = valMax_color - valMin_color;
	        float valNorm_color = ( valColor - valMin_color ) / valInverseWidth_color;
	        valNorm_color = clamp( valNorm_color, 0.0, 1.0 );
	        vRgba = texture( valTexture_color, valNorm_color );
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

    gl_Position = mvpMatrix * vec4( a_position, 0, 1 );
    vPointSize_PX = gl_PointSize;
}