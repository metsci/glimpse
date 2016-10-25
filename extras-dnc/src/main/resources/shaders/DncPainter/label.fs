#version 150
#extension GL_EXT_gpu_shader4 : enable

uniform sampler2D ATLAS;

in vec2 gAtlasCoords;
in float gHighlight;

out vec4 outRgba;

void main( )
{
    // For scaled labels we need interpolation, but for unscaled
    // labels we want to snap to pixels to get crisp text

    vec2 st;
    bool highlight = ( gHighlight >= 0.5 );
    if ( highlight )
    {
        st = gAtlasCoords;
    }
    else
    {
        vec2 atlasSize_PX = textureSize2D( ATLAS, 0 );
        vec2 st_PX = floor( gAtlasCoords * atlasSize_PX + 0.5 );
        st = ( st_PX + 0.5 ) / atlasSize_PX;
    }

    outRgba = texture2D( ATLAS, st );
}
