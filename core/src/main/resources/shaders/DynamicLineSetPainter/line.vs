#version 150

vec2 axisMin( vec4 axisRect )
{
    return axisRect.xy;
}

vec2 axisMax( vec4 axisRect )
{
    return axisRect.zw;
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

uniform vec4 AXIS_RECT;
uniform vec2 VIEWPORT_SIZE_PX;

in vec2 inXy;
in vec4 inRgba;

out vec4 vRgba;

void main( )
{
    vec2 xy_AXIS = inXy;
    gl_Position.xy = axisXyToPx( xy_AXIS, AXIS_RECT, VIEWPORT_SIZE_PX );
    vRgba = inRgba;
}
