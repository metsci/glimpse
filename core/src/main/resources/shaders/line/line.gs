#version 150

layout( lines ) in;
layout( triangle_strip, max_vertices = 4 ) out;

uniform vec2 VIEWPORT_SIZE_PX;
uniform float LINE_THICKNESS_PX;
uniform float FEATHER_THICKNESS_PX;

in float vCumulativeDistance_PX[];

out vec2 gPosInQuad_PX;

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
        // Rotation must be done in pixel-space
        vec2 lineDir_PX = lineDelta_PX / lineLength_PX;
        vec2 lineNormal_PX = vec2( -lineDir_PX.y, lineDir_PX.x );

        vec2 pxToNdc = 2.0 / VIEWPORT_SIZE_PX;
        float quadHalfHeight_PX = LINE_THICKNESS_PX + FEATHER_THICKNESS_PX;
        vec2 edgeDelta = quadHalfHeight_PX * lineNormal_PX * pxToNdc;


        gl_Position.xy = posA + edgeDelta;
        gl_Position.zw = vec2( 0.0, 1.0 );
        gPosInQuad_PX = vec2( cumulativeDistanceA_PX, quadHalfHeight_PX );
        EmitVertex( );

        gl_Position.xy = posA - edgeDelta;
        gl_Position.zw = vec2( 0.0, 1.0 );
        gPosInQuad_PX = vec2( cumulativeDistanceA_PX, -quadHalfHeight_PX );
        EmitVertex( );

        gl_Position.xy = posB + edgeDelta;
        gl_Position.zw = vec2( 0.0, 1.0 );
        gPosInQuad_PX = vec2( cumulativeDistanceB_PX, quadHalfHeight_PX );
        EmitVertex( );

        gl_Position.xy = posB - edgeDelta;
        gl_Position.zw = vec2( 0.0, 1.0 );
        gPosInQuad_PX = vec2( cumulativeDistanceB_PX, -quadHalfHeight_PX );
        EmitVertex( );


        EndPrimitive( );
    }
}
