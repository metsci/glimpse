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

vec2 axisSize( vec4 axisRect )
{
    return ( axisMax( axisRect ) - axisMin( axisRect ) );
}

vec2 axisXyToPx( vec2 xy_AXIS, vec4 axisRect, vec2 viewportSize_PX )
{
    vec2 xy_FRAC = ( xy_AXIS - axisMin( axisRect ) ) / axisSize( axisRect );
    return ( xy_FRAC * viewportSize_PX );
}

// AXIS_RECT is (xMin, xMax, yMin, yMax)
uniform vec4 AXIS_RECT;
uniform vec2 VIEWPORT_SIZE_PX;

in vec2 inXy;
in float inMileage;

out float vMileage_PX;

void main( )
{
    float mileage_AXIS = inMileage;
    vec2 ppv = VIEWPORT_SIZE_PX / axisSize( AXIS_RECT );
    vMileage_PX = mileage_AXIS * ppv.x;

    vec2 xy_AXIS = inXy;
    gl_Position.xy = axisXyToPx( xy_AXIS, AXIS_RECT, VIEWPORT_SIZE_PX );
}
