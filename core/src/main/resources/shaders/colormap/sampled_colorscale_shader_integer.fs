#version 150

uniform usampler2D datatex;
uniform sampler1D colortex;

uniform float dataMin;
uniform float dataMax;

uniform float alpha;

in vec2 vS;

out vec4 fRgba;

void main()
{
    uint dataVal = texture( datatex, vS.st ).r;
    float normalizedVal = ( float(dataVal) - dataMin ) / ( dataMax - dataMin );
    normalizedVal = clamp( normalizedVal, 0.0, 1.0 );

    vec4 color = texture( colortex, normalizedVal );
    fRgba = color;
    fRgba.a = alpha;
}
