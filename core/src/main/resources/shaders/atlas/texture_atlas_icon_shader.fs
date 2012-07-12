#version 120

uniform sampler2D tex;

uniform bool isPickMode;

varying vec2 TexCoord;

varying vec3 pickColor;

void main()
{
    vec4 texColor = texture2D( tex, TexCoord.st );
    
    if ( isPickMode )
    {
        if ( texColor.a != 0.0 )
        {
            gl_FragColor = vec4( pickColor.r / 255., pickColor.g / 255., pickColor.b / 255., 1.0 );
        }
        else
        {
            discard;
        }
    }
    else
    {
        gl_FragColor = texColor;
    }
}