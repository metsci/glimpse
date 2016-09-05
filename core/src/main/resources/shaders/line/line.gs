#version 150

layout( lines ) in;
layout( triangle_strip, max_vertices = 4 ) out;

vec2 pxToNdc( vec2 xy_PX, vec2 viewportSize_PX )
{
    vec2 xy_FRAC = xy_PX / viewportSize_PX;
    return ( -1.0 + 2.0*xy_FRAC );
}

uniform vec2 VIEWPORT_SIZE_PX;
uniform float LINE_THICKNESS_PX;
uniform float FEATHER_THICKNESS_PX;

in float vMileage_PX[];

out vec2 gPosInQuad_PX;
out float gMileage_PX;
out float gQuadLength_PX;

void main( )
{
    vec2 posA_PX = gl_in[ 0 ].gl_Position.xy;
    vec2 posB_PX = gl_in[ 1 ].gl_Position.xy;

    vec2 lineDelta_PX = posB_PX - posA_PX;
    float lineLength_PX = length( lineDelta_PX );

    float mileageA_PX = vMileage_PX[ 0 ];
    float mileageB_PX = vMileage_PX[ 1 ];

    if ( lineLength_PX > 0.0 && mileageB_PX >= mileageA_PX )
    {
        float halfFeather_PX = 0.5 * FEATHER_THICKNESS_PX;

        vec2 parallelDir = lineDelta_PX / lineLength_PX;
        vec2 parallelOffset_PX = halfFeather_PX * parallelDir;

        vec2 normalDir = vec2( -parallelDir.y, parallelDir.x );
        float halfNormal_PX = 0.5*LINE_THICKNESS_PX + halfFeather_PX;
        vec2 normalOffset_PX = halfNormal_PX * normalDir;


        gl_Position.xy = pxToNdc( posA_PX - parallelOffset_PX + normalOffset_PX, VIEWPORT_SIZE_PX );
        gl_Position.zw = vec2( 0.0, 1.0 );
        gPosInQuad_PX = vec2( -halfFeather_PX, halfNormal_PX );
        gMileage_PX = mileageA_PX - halfFeather_PX;
        gQuadLength_PX = lineLength_PX;
        EmitVertex( );

        gl_Position.xy = pxToNdc( posA_PX - parallelOffset_PX - normalOffset_PX, VIEWPORT_SIZE_PX );
        gl_Position.zw = vec2( 0.0, 1.0 );
        gPosInQuad_PX = vec2( -halfFeather_PX, -halfNormal_PX );
        gMileage_PX = mileageA_PX - halfFeather_PX;
        gQuadLength_PX = lineLength_PX;
        EmitVertex( );

        gl_Position.xy = pxToNdc( posB_PX + parallelOffset_PX + normalOffset_PX, VIEWPORT_SIZE_PX );
        gl_Position.zw = vec2( 0.0, 1.0 );
        gPosInQuad_PX = vec2( lineLength_PX + halfFeather_PX, halfNormal_PX );
        gMileage_PX = mileageB_PX + halfFeather_PX;
        gQuadLength_PX = lineLength_PX;
        EmitVertex( );

        gl_Position.xy = pxToNdc( posB_PX + parallelOffset_PX - normalOffset_PX, VIEWPORT_SIZE_PX );
        gl_Position.zw = vec2( 0.0, 1.0 );
        gPosInQuad_PX = vec2( lineLength_PX + halfFeather_PX, -halfNormal_PX );
        gMileage_PX = mileageB_PX + halfFeather_PX;
        gQuadLength_PX = lineLength_PX;
        EmitVertex( );


        EndPrimitive( );
    }
}
