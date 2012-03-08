
uniform sampler2D datatex;
uniform sampler1D colortex;

uniform float dataMin;
uniform float dataMax;

uniform float alpha;

void main()
{
    float dataVal = texture2D( datatex, gl_TexCoord[0].st ).r;
    float normalizedVal = ( dataVal - dataMin ) / ( dataMax - dataMin );
    clamp( normalizedVal, 0.0, 1.0 );

    vec4 color = texture1D( colortex, normalizedVal );
    gl_FragColor = color;
    gl_FragColor.a = alpha;
}