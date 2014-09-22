// this extension is required in order to use integer textures (isampler2D)
#extension GL_EXT_gpu_shader4 : require

uniform usampler2D datatex;
uniform sampler1D colortex;

uniform float dataMin;
uniform float dataMax;

uniform float alpha;

void main()
{
    int dataVal = texture2D( datatex, gl_TexCoord[0].st ).r;
    float normalizedVal = ( float(dataVal) - dataMin ) / ( dataMax - dataMin );
    clamp( normalizedVal, 0.0, 1.0 );

    vec4 color = texture1D( colortex, normalizedVal );
    gl_FragColor = color;
    gl_FragColor.a = alpha;
}