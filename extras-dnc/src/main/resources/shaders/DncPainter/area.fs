#version 150

uniform vec4 RGBA;

out vec4 outRgba;

void main( )
{
    outRgba.rgb = RGBA.rgb * RGBA.a;
    outRgba.a = RGBA.a;
}
