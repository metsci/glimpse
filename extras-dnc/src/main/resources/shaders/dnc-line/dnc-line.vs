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
uniform vec2 VIEWPORT_SIZE_PX;
uniform float LINE_THICKNESS_PX;
uniform isampler2D HIGHLIGHT_SET;
uniform float HIGHLIGHT_EXTRA_THICKNESS_PX;

in vec4 inLineVertex;

out float vLineThickness_PX;
out float vCumulativeDistance_PX;

void main( )
{
    float featureNum = inLineVertex.z;
    bool highlight = setContains( HIGHLIGHT_SET, featureNum );
    vLineThickness_PX = LINE_THICKNESS_PX + ( highlight ? HIGHLIGHT_EXTRA_THICKNESS_PX : 0.0 );

    // Assume that ppv-aspect-ratio is 1.0 -- handling variable
    // ppv-aspect-ratio would be difficult, and not very useful
    float cumulativeDistance = inLineVertex.w;
    vec2 ppv = axisSize( AXIS_RECT ) / VIEWPORT_SIZE_PX;
    vCumulativeDistance_PX = cumulativeDistance * ppv.x;

    vec2 xy_AXIS = inLineVertex.xy;
    gl_Position.xy = axisXyToNdc( xy_AXIS, AXIS_RECT );
    gl_Position.zw = vec2( 0.0, 1.0 );
}
