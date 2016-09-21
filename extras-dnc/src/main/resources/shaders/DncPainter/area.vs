#version 150
#extension GL_EXT_gpu_shader4 : enable

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

bool setContains( isampler2D setTexture, float index )
{
    ivec2 textureSize = textureSize2D( setTexture, 0 );
    if ( textureSize.x == 0 || textureSize.y == 0 )
    {
        return false;
    }
    else
    {
        float j = floor( index / textureSize.x );
        float i = index - ( j * textureSize.x );
        vec2 st = vec2( i, j ) / textureSize;
        int value = texture2D( setTexture, st ).r;
        return ( value != 0 );
    }
}

uniform vec4 AXIS_RECT;
uniform isampler2D HIGHLIGHT_SET;

in vec3 inAreaVertex;

out float vHighlight;

void main( )
{
    float featureNum = inAreaVertex.z;
    vHighlight = ( setContains( HIGHLIGHT_SET, featureNum ) ? 1.0 : 0.0 );

    vec2 xy_AXIS = inAreaVertex.xy;
    gl_Position.xy = axisXyToNdc( xy_AXIS, AXIS_RECT );
    gl_Position.zw = vec2( 0.0, 1.0 );
}
