#version 150

vec2 axisMin( vec4 axisRect )
{
    return axisRect.xy;
}

vec2 axisSize( vec4 axisRect )
{
    return axisRect.zw;
}

vec2 axisXyToNdc( vec2 xy_AXIS, vec4 axisRect )
{
    return ( ( xy_AXIS - axisMin( axisRect ) ) / axisSize( axisRect ) );
}


uniform vec4 AXIS_RECT;
uniform vec2 VIEWPORT_SIZE_PX;

in vec2 inXy;
in float inCumulativeDistance;

out float vCumulativeDistance_PX;

void main( )
{
    float cumulativeDistance_AXIS = inCumulativeDistance;
    vec2 ppv = axisSize( AXIS_RECT ) / VIEWPORT_SIZE_PX;
    vCumulativeDistance_PX = cumulativeDistance_AXIS * ppv.x;

    vec2 xy_AXIS = inXy;
    gl_Position.xy = axisXyToNdc( xy_AXIS, AXIS_RECT );
    gl_Position.zw = vec2( 0.0, 1.0 );
}
