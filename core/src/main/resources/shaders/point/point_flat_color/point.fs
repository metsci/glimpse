#version 150

uniform float FEATHER_THICKNESS_PX;
uniform float POINT_SIZE_PX;
uniform vec4 RGBA;

out vec4 outRgba;

void main( )
{
  // -1 on left edge, 0 in center, 1 on right edge
  vec2 circCoord = 2.0 * gl_PointCoord - 1.0;

  // distance from center in pixels (squared)
	float distance_PX = dot(circCoord, circCoord) * POINT_SIZE_PX;

	if ( distance_PX <= POINT_SIZE_PX - FEATHER_THICKNESS_PX  )
	{
		outRgba.rgba = RGBA.rgba;
	}
	else
	{
		float frac = ( POINT_SIZE_PX - distance_PX ) / FEATHER_THICKNESS_PX;
		frac = clamp( frac, 0, 1 );

		outRgba.rgb = RGBA.rgb;
		outRgba.a = RGBA.a * frac;
	}
}
