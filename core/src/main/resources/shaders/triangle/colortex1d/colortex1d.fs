#version 150

in float vS;

out vec4 outRgba;

uniform sampler1D TEXTURE1D;

void main( )
{
    outRgba = texture( TEXTURE1D, vS );
}
