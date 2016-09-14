#version 150

in vec2 vS;

out vec4 outRgba;

uniform sampler2D TEXTURE2D;

void main( )
{
    outRgba = texture( TEXTURE2D, vS );
}
