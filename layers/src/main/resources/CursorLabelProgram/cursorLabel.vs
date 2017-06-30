#version 150

vec4 pixelXyToNdc( vec2 xy_PX, vec2 viewportSize_PX )
{
    vec2 xy_FRAC = xy_PX / viewportSize_PX;
    return vec4( 2.0*xy_FRAC - 1.0, 0.0, 1.0 );
}


uniform vec2 VIEWPORT_SIZE_PX;


in vec2 inSt;
in vec2 inXy;


out vec2 vSt;


void main( )
{
    vSt = inSt;
    gl_Position = pixelXyToNdc( inXy, VIEWPORT_SIZE_PX );
}
