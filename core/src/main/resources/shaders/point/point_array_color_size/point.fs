#version 150

uniform float FEATHER_THICKNESS_PX;

in vec4 vRgba;
in float vSize;

out vec4 outRgba;

void main( )
{
  // -1 on left edge, 0 in center, 1 on right edge
  vec2 circCoord = 2.0 * gl_PointCoord - 1.0;

  // distance from center in pixels (squared)
	float distance_PX = dot(circCoord, circCoord) * vSize;

	if ( distance_PX <= vSize - FEATHER_THICKNESS_PX  )
	{
		outRgba.rgba = vRgba.rgba;
	}
	else
	{
		float frac = ( vSize - distance_PX ) / FEATHER_THICKNESS_PX;
		frac = clamp( frac, 0, 1 );

		outRgba.rgb = vRgba.rgb;
		outRgba.a = vRgba.a * frac;
	}
}
