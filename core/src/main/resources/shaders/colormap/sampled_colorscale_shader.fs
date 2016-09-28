#version 150

uniform sampler2D datatex;
uniform sampler1D colortex;

uniform float dataMin;
uniform float dataMax;

uniform float alpha;

// skips fragments with NaN values
uniform bool discardNaN;

in vec2 vS;

out vec4 fRgba;

void main()
{
	// retrieve the data value for this texel
    float dataVal = texture( datatex, vS ).r;
    if( discardNaN )
    {
       // The isnan() function isn't defined in GLSL 1.20, which causes problems on OSX.
       if( ! ( dataVal < 0.0 || 0.0 < dataVal || dataVal == 0.0 ) )
          discard;
    }

    float normalizedVal = ( dataVal - dataMin ) / ( dataMax - dataMin );
    normalizedVal = clamp( normalizedVal, 0.0, 1.0 );

    vec4 color = texture( colortex, normalizedVal );
    fRgba = color;
    fRgba.a = alpha;
}
