#version 150

uniform float LINE_THICKNESS_PX;
uniform float FEATHER_THICKNESS_PX;
uniform vec4 RGBA;
uniform int STIPPLE_ENABLE;
uniform float STIPPLE_SCALE;
uniform int STIPPLE_PATTERN;

in vec2 gPosInQuad_PX;
in float gCumulativeDistance_PX;
in float gQuadLength_PX;

out vec4 outRgba;

void main( )
{
    // The word "excursion" is used here to mean the distance beyond the edge of the fully
    // filled region. Along each edge, there is a narrow feather region. Half the thickness
    // of the feather region lies inside the quad, and half outside. When excursion <= 0, no
    // extra fading occurs. When excursion >= FEATHER_THICKNESS_PX, the fragment is completely
    // faded, and has an alpha of zero.

    float halfFeather_PX = 0.5 * FEATHER_THICKNESS_PX;

    float normalExcursion_PX;
    {
        float edge_PX = 0.5*LINE_THICKNESS_PX - halfFeather_PX;
        normalExcursion_PX = abs( gPosInQuad_PX.t ) - edge_PX;
    }

    float startExcursion_PX;
    {
        float edge_PX = halfFeather_PX;
        startExcursion_PX = edge_PX - gPosInQuad_PX.s;
    }

    float endExcursion_PX;
    {
        float edge_PX = gQuadLength_PX - halfFeather_PX;
        endExcursion_PX = gPosInQuad_PX.s - edge_PX;
    }

    float stippleExcursion_PX;
    if ( STIPPLE_ENABLE == 0 )
    {
        stippleExcursion_PX = 0.0;
    }
    else
    {
        // This assumes that the feather region is thinner than a single stipple-bit region.
        // The alternative would be complicated, and only very rarely useful.

        float bitNum = mod( gCumulativeDistance_PX / STIPPLE_SCALE, 16.0 );
        int bitNumPrev = int( bitNum );
        int bitNumNext = int( mod( bitNum + 1.0, 16.0 ) );
        int bitPrev = ( STIPPLE_PATTERN & ( 0x1 << bitNumPrev ) );
        int bitNext = ( STIPPLE_PATTERN & ( 0x1 << bitNumNext ) );

        if ( bitPrev == 0 && bitNext == 0 )
        {
            // Off
            stippleExcursion_PX = FEATHER_THICKNESS_PX;
        }
        else if ( bitPrev == 0 )
        {
            // Rising edge
            stippleExcursion_PX = STIPPLE_SCALE * mod( bitNumNext - bitNum, 16.0 );
        }
        else if ( bitNext == 0 )
        {
            // Falling edge
            stippleExcursion_PX = STIPPLE_SCALE * ( bitNum - bitNumPrev );
        }
        else
        {
            // On
            stippleExcursion_PX = 0.0;
        }
    }

    outRgba.rgb = RGBA.rgb;

    float maxExcursion_PX = max( max( startExcursion_PX, endExcursion_PX ), max( normalExcursion_PX, stippleExcursion_PX ) );
    float fade = clamp( maxExcursion_PX / FEATHER_THICKNESS_PX, 0.0, 1.0 );
    outRgba.a = RGBA.a * ( 1.0 - fade );
}
