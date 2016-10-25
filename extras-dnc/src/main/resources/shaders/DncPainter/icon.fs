#version 150

uniform sampler2D ATLAS;

in vec2 gAtlasCoords;

out vec4 outRgba;

void main( )
{
    outRgba = texture2D( ATLAS, gAtlasCoords );
}
