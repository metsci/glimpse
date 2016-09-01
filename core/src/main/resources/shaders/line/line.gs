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
    vec2 lineDelta = posB - posA;
    float lineLength = length( lineDelta );

    float cumulativeDistanceA_PX = vCumulativeDistance_PX[ 0 ];
    float cumulativeDistanceB_PX = vCumulativeDistance_PX[ 1 ];

    if ( lineLength > 0.0 && cumulativeDistanceB_PX >= cumulativeDistanceA_PX )
    {
        vec2 lineDir = lineDelta / lineLength;
        vec2 lineNormal = vec2( -lineDir.y, lineDir.x );

        vec2 pxToNdc = 2.0 / VIEWPORT_SIZE_PX;
        float quadHalfHeight_PX = LINE_THICKNESS_PX + FEATHER_THICKNESS_PX;
        vec2 edgeDelta = quadHalfHeight_PX * pxToNdc * lineNormal;


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
