#version 150

uniform float LINE_THICKNESS_PX;
uniform float FEATHER_THICKNESS_PX;
uniform vec4 RGBA;
uniform int STIPPLE_ENABLE;
uniform float STIPPLE_SCALE;
uniform int STIPPLE_PATTERN;

// in vec2 gPosInQuad_PX;
// in float gMileage_PX;
// in float gQuadLength_PX;
in vec4 gRgba;
in float gFeather;

out vec4 outRgba;

void main( )
{
    // float feather_PX = max( 0.01, FEATHER_THICKNESS_PX );
    // float halfFeather_PX = 0.5 * feather_PX;
    //
    // float normalAlpha;
    // {
    //     float edge_PX = 0.5*LINE_THICKNESS_PX - halfFeather_PX;
    //     float excursion_PX = abs( gPosInQuad_PX.t ) - edge_PX;
    //     normalAlpha = 1.0 - ( excursion_PX / feather_PX );
    // }
    //
    // float startAlpha;
    // {
    //     float edge_PX = halfFeather_PX;
    //     float excursion_PX = edge_PX - gPosInQuad_PX.s;
    //     startAlpha = 1.0 - ( excursion_PX / feather_PX );
    // }
    //
    // float endAlpha;
    // {
    //     float edge_PX = gQuadLength_PX - halfFeather_PX;
    //     float excursion_PX = gPosInQuad_PX.s - edge_PX;
    //     endAlpha = 1.0 - ( excursion_PX / feather_PX );
    // }
    //
    // float stippleAlpha;
    // if ( STIPPLE_ENABLE == 0 )
    // {
    //     stippleAlpha = 1.0;
    // }
    // else
    // {
    //     // This assumes that the feather region is thinner than a single stipple-
    //     // bit region. The alternative would be more complicated than useful.
    //
    //     float bitWidth_PX = STIPPLE_SCALE;
    //     float bitNum = mod( gMileage_PX / bitWidth_PX, 16.0 );
    //     float bitAlpha = float( sign( STIPPLE_PATTERN & ( 0x1 << int( bitNum ) ) ) );
    //
    //     float posInBit_PX = bitWidth_PX*( bitNum - floor( bitNum ) );
    //     if ( posInBit_PX < halfFeather_PX )
    //     {
    //         float bitNumPrev = mod( bitNum - 1.0, 16.0 );
    //         float bitAlphaPrev = float( sign( STIPPLE_PATTERN & ( 0x1 << int( bitNumPrev ) ) ) );
    //         float mixFrac = ( halfFeather_PX + posInBit_PX ) / feather_PX;
    //         stippleAlpha = mix( bitAlphaPrev, bitAlpha, mixFrac );
    //     }
    //     else if ( posInBit_PX > bitWidth_PX - halfFeather_PX )
    //     {
    //         float bitNumNext = mod( bitNum + 1.0, 16.0 );
    //         float bitAlphaNext = float( sign( STIPPLE_PATTERN & ( 0x1 << int( bitNumNext ) ) ) );
    //         float mixFrac = ( posInBit_PX - ( bitWidth_PX - halfFeather_PX ) ) / feather_PX;
    //         stippleAlpha = mix( bitAlpha, bitAlphaNext, mixFrac );
    //     }
    //     else
    //     {
    //         stippleAlpha = bitAlpha;
    //     }
    // }
    //
    // outRgba.rgb = RGBA.rgb;
    //
    // float minAlpha = min( min( startAlpha, endAlpha ), min( normalAlpha, stippleAlpha ) );
    // outRgba.a = RGBA.a * clamp( minAlpha, 0.0, 1.0 );

    outRgba.rgb = gRgba.rgb;
    outRgba.a = gRgba.a * gFeather;
}
