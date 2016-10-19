#version 150

uniform float FEATHER_THICKNESS_PX;
uniform int STIPPLE_ENABLE;
uniform float STIPPLE_SCALE;
uniform int STIPPLE_PATTERN;

uniform vec4 RGBA;

in float vMileage;

out vec4 outRgba;

void main( )
{
    // This assumes that the feather region is thinner than a single stipple-
    // bit region. The alternative would be more complicated than useful.

    float stippleAlpha;

    if ( STIPPLE_ENABLE == 0 )
    {
        stippleAlpha = 1.0;
    }
    else
    {
        float feather_PX = max( 0.01, FEATHER_THICKNESS_PX );
        float halfFeather_PX = 0.5 * feather_PX;
    
        float bitWidth_PX = STIPPLE_SCALE;
        float bitNum = mod( vMileage / bitWidth_PX, 16.0 );
        float bitAlpha = float( sign( STIPPLE_PATTERN & ( 0x1 << int( bitNum ) ) ) );
    
        float posInBit_PX = bitWidth_PX*( bitNum - floor( bitNum ) );
        if ( posInBit_PX < halfFeather_PX )
        {
            float bitNumPrev = mod( bitNum - 1.0, 16.0 );
            float bitAlphaPrev = float( sign( STIPPLE_PATTERN & ( 0x1 << int( bitNumPrev ) ) ) );
            float mixFrac = ( halfFeather_PX + posInBit_PX ) / feather_PX;
            stippleAlpha = mix( bitAlphaPrev, bitAlpha, mixFrac );
        }
        else if ( posInBit_PX > bitWidth_PX - halfFeather_PX )
        {
            float bitNumNext = mod( bitNum + 1.0, 16.0 );
            float bitAlphaNext = float( sign( STIPPLE_PATTERN & ( 0x1 << int( bitNumNext ) ) ) );
            float mixFrac = ( posInBit_PX - ( bitWidth_PX - halfFeather_PX ) ) / feather_PX;
            stippleAlpha = mix( bitAlpha, bitAlphaNext, mixFrac );
        }
        else
        {
            stippleAlpha = bitAlpha;
        }
    }

    outRgba.rgb = RGBA.rgb;
    outRgba.a = RGBA.a * clamp( stippleAlpha, 0.0, 1.0 );
}
