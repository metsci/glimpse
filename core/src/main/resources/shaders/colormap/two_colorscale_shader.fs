#version 150

uniform sampler2D tex;
uniform sampler1D colortex_high;
uniform sampler1D colortex_low;

uniform float dataMin;
uniform float dataZero;
uniform float dataMax;

uniform float alpha;

in vec2 vS;

out vec4 fRgba;

void main()
{
    float dataVal = texture( tex, vS.st ).r;

    if ( dataVal > dataZero )
    {
      float normalizedVal = ( dataVal - dataZero ) / ( dataMax - dataZero );
      normalizedVal = clamp( normalizedVal, 0.0, 1.0 );

      vec4 color = texture( colortex_high, normalizedVal );
      fRgba = color;
      fRgba.a = alpha;
    }
    else
    {
      float normalizedVal = ( dataZero - dataVal ) / ( dataZero - dataMin );
      normalizedVal = clamp( normalizedVal, 0.0, 1.0 );

      vec4 color = texture( colortex_low, normalizedVal );
      fRgba = color;
      fRgba.a = alpha;
    }
}
