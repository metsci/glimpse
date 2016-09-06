#version 150

uniform sampler2D tex;
uniform bool isPickMode;

in VertexData {
    vec2 texCoord;
    vec3 pickColor;
} VertexIn;

out vec4 outRgba;

void main()
{
    vec4 texColor = texture2D( tex, VertexIn.texCoord.st );
    
    if ( isPickMode )
    {
        if ( texColor.a != 0.0 )
        {
            outRgba = vec4( VertexIn.pickColor.r / 255., VertexIn.pickColor.g / 255., VertexIn.pickColor.b / 255., 1.0 );
        }
        else
        {
            discard;
        }
    }
    else
    {
        outRgba = texColor;
    }
}