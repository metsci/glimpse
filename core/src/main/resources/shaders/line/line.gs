#version 150

layout( lines ) in;
layout( triangle_strip, max_vertices = 4 ) out;

uniform vec2 VIEWPORT_SIZE_PX;
uniform float LINE_THICKNESS_PX;
uniform float FEATHER_THICKNESS_PX;

in float vCumulativeDistance_PX[];

out vec2 gPosInQuad_PX;
out float gCumulativeDistance_PX;
out float gQuadLength_PX;

void main( )
{
    vec2 posA = gl_in[ 0 ].gl_Position.xy;
    vec2 posB = gl_in[ 1 ].gl_Position.xy;

    vec2 ndcToPx = 0.5 * VIEWPORT_SIZE_PX;
    vec2 lineDelta_PX = ( posB - posA ) * ndcToPx;
    float lineLength_PX = length( lineDelta_PX );

    float cumulativeDistanceA_PX = vCumulativeDistance_PX[ 0 ];
    float cumulativeDistanceB_PX = vCumulativeDistance_PX[ 1 ];

    if ( lineLength_PX > 0.0 && cumulativeDistanceB_PX >= cumulativeDistanceA_PX )
    {
        vec2 pxToNdc = 2.0 / VIEWPORT_SIZE_PX;
        float halfFeather_PX = 0.5 * FEATHER_THICKNESS_PX;

        vec2 lineDir_PX = lineDelta_PX / lineLength_PX;
        vec2 parallelOffset = lineDir_PX * halfFeather_PX * pxToNdc;

        // Rotation from parallel to normal must be done in pixel-space
        vec2 lineNormal_PX = vec2( -lineDir_PX.y, lineDir_PX.x );
        float halfNormal_PX = 0.5*LINE_THICKNESS_PX + halfFeather_PX;
        vec2 normalOffset = lineNormal_PX * halfNormal_PX * pxToNdc;


        gl_Position.xy = posA - parallelOffset + normalOffset;
        gl_Position.zw = vec2( 0.0, 1.0 );
        gPosInQuad_PX = vec2( -halfFeather_PX, halfNormal_PX );
        gCumulativeDistance_PX = cumulativeDistanceA_PX - halfFeather_PX;
        gQuadLength_PX = lineLength_PX;
        EmitVertex( );

        gl_Position.xy = posA - parallelOffset - normalOffset;
        gl_Position.zw = vec2( 0.0, 1.0 );
        gPosInQuad_PX = vec2( -halfFeather_PX, -halfNormal_PX );
        gCumulativeDistance_PX = cumulativeDistanceA_PX - halfFeather_PX;
        gQuadLength_PX = lineLength_PX;
        EmitVertex( );

        gl_Position.xy = posB + parallelOffset + normalOffset;
        gl_Position.zw = vec2( 0.0, 1.0 );
        gPosInQuad_PX = vec2( lineLength_PX + halfFeather_PX, halfNormal_PX );
        gCumulativeDistance_PX = cumulativeDistanceB_PX + halfFeather_PX;
        gQuadLength_PX = lineLength_PX;
        EmitVertex( );

        gl_Position.xy = posB + parallelOffset - normalOffset;
        gl_Position.zw = vec2( 0.0, 1.0 );
        gPosInQuad_PX = vec2( lineLength_PX + halfFeather_PX, -halfNormal_PX );
        gCumulativeDistance_PX = cumulativeDistanceB_PX + halfFeather_PX;
        gQuadLength_PX = lineLength_PX;
        EmitVertex( );


        EndPrimitive( );
    }
}
