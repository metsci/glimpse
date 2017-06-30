#version 150

uniform sampler2D IMAGE;

in vec2 vSt;

out vec4 outRgba;

void main( )
{
    outRgba = texture( IMAGE, vSt );
}
