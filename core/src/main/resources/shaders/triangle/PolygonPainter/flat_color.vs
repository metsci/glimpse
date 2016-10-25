#version 150

vec2 axisMin( vec4 axisRect )
{
    // Swizzle (xMin, yMin) out of (xMin, xMax, yMin, yMax)
    return axisRect.xz;
}

vec2 axisMax( vec4 axisRect )
{
    // Swizzle (xMax, yMax) out of (xMin, xMax, yMin, yMax)
    return axisRect.yw;
}

float near( vec2 nearFar )
{
    // Swizzle near out of (near, far)
    return nearFar.x;
}

float far( vec2 nearFar )
{
    // Swizzle far out of (near, far)
    return nearFar.y;
}

vec2 axisSize( vec4 axisRect )
{
    return ( axisMax( axisRect ) - axisMin( axisRect ) );
}

vec4 axisXyToNdc( vec3 xy_AXIS, vec4 axisRect, vec2 nearFar )
{
    vec2 xy_FRAC = ( xy_AXIS.xy - axisMin( axisRect ) ) / axisSize( axisRect );
    float z_FRAC = ( xy_AXIS.z - near( nearFar ) ) / ( far( nearFar ) - near( nearFar ) );
    return vec4( 2 * xy_FRAC - 1, z_FRAC, 1.0 );
}

uniform vec4 AXIS_RECT;
uniform vec2 NEAR_FAR;

in vec3 inXy;

void main( )
{
      gl_Position = axisXyToNdc( inXy, AXIS_RECT, NEAR_FAR );
}
