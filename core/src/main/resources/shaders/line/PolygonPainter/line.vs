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

vec2 axisXyToPx( vec3 xy_AXIS, vec4 axisRect, vec2 viewportSize_PX )
{
    vec2 xy_FRAC = ( xy_AXIS.xy - axisMin( axisRect ) ) / axisSize( axisRect );
    return ( xy_FRAC * viewportSize_PX );
}

// AXIS_RECT is (xMin, xMax, yMin, yMax)
uniform vec4 AXIS_RECT;
uniform vec2 NEAR_FAR;
uniform vec2 VIEWPORT_SIZE_PX;

in vec3 inXy;
in int inFlags;
in float inMileage;

out int vFlags;
out float vMileage_PX;

void main( )
{
    vFlags = inFlags;

    float mileage_AXIS = inMileage;
    vec2 ppv = VIEWPORT_SIZE_PX / axisSize( AXIS_RECT );
    vMileage_PX = mileage_AXIS * ppv.x;

    vec3 xy_AXIS = inXy;
    gl_Position.xy = axisXyToPx( xy_AXIS, AXIS_RECT, VIEWPORT_SIZE_PX );
    gl_Position.z = ( xy_AXIS.z - near( NEAR_FAR ) ) / ( far( NEAR_FAR ) - near( NEAR_FAR ) );
}
