#version 150

layout( lines_adjacency ) in;
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
    vec2 posB_PX = gl_in[ 1 ].gl_Position.xy;
    vec2 posC_PX = gl_in[ 2 ].gl_Position.xy;
    vec2 deltaBC_PX = posC_PX - posB_PX;
    float lengthBC_PX = length( deltaBC_PX );
    float mileageB_PX = vMileage_PX[ 1 ];
    float mileageC_PX = vMileage_PX[ 2 ];

    if ( lengthBC_PX > 0.0 && mileageC_PX >= mileageB_PX )
    {
        vec2 dirBC = deltaBC_PX / lengthBC_PX;
        vec2 normalBC = vec2( -dirBC.y, dirBC.x );

        vec2 extrudeB;
        {
            vec2 posA_PX = gl_in[ 0 ].gl_Position.xy;
            vec2 deltaAB_PX = posB_PX - posA_PX;
            float lengthAB_PX = length( deltaAB_PX );
            float mileageA_PX = vMileage_PX[ 0 ];

            if ( lengthAB_PX > 0.0 && mileageB_PX >= mileageA_PX )
            {
                vec2 dirAB = deltaAB_PX / lengthAB_PX;
                vec2 dirCB = -dirBC;
                float dirABxDirCB = dirAB.x*dirCB.y - dirAB.y*dirCB.x;
                extrudeB = ( dirAB + dirCB ) / dirABxDirCB;
            }
            else
            {
                extrudeB = normalBC;
            }
        }

        vec2 extrudeC;
        {
            vec2 posD_PX = gl_in[ 3 ].gl_Position.xy;
            vec2 deltaDC_PX = posC_PX - posD_PX;
            float lengthDC_PX = length( deltaDC_PX );
            float mileageD_PX = vMileage_PX[ 3 ];

            if ( lengthDC_PX > 0.0 && mileageD_PX >= mileageC_PX )
            {
                vec2 dirDC = deltaDC_PX / lengthDC_PX;
                float dirBCxDirDC = dirBC.x*dirDC.y - dirBC.y*dirDC.x;
                extrudeC = ( dirBC + dirDC ) / dirBCxDirDC;
            }
            else
            {
                extrudeC = normalBC;
            }
        }


        //float halfFeather_PX = 0.5 * FEATHER_THICKNESS_PX;

        float halfNormal_PX = 0.5*LINE_THICKNESS_PX;// + halfFeather_PX;


        gl_Position.xy = pxToNdc( posB_PX + halfNormal_PX*extrudeB, VIEWPORT_SIZE_PX );
        gl_Position.zw = vec2( 0.0, 1.0 );
        gPosInQuad_PX = vec2( 0.0, halfNormal_PX );
        gMileage_PX = mileageB_PX;
        gQuadLength_PX = lengthBC_PX;
        EmitVertex( );

        gl_Position.xy = pxToNdc( posB_PX - halfNormal_PX*extrudeB, VIEWPORT_SIZE_PX );
        gl_Position.zw = vec2( 0.0, 1.0 );
        gPosInQuad_PX = vec2( 0.0, -halfNormal_PX );
        gMileage_PX = mileageB_PX;
        gQuadLength_PX = lengthBC_PX;
        EmitVertex( );

        gl_Position.xy = pxToNdc( posC_PX + halfNormal_PX*extrudeC, VIEWPORT_SIZE_PX );
        gl_Position.zw = vec2( 0.0, 1.0 );
        gPosInQuad_PX = vec2( lengthBC_PX, halfNormal_PX );
        gMileage_PX = mileageC_PX;
        gQuadLength_PX = lengthBC_PX;
        EmitVertex( );

        gl_Position.xy = pxToNdc( posC_PX - halfNormal_PX*extrudeC, VIEWPORT_SIZE_PX );
        gl_Position.zw = vec2( 0.0, 1.0 );
        gPosInQuad_PX = vec2( lengthBC_PX, -halfNormal_PX );
        gMileage_PX = mileageC_PX;
        gQuadLength_PX = lengthBC_PX;
        EmitVertex( );


        EndPrimitive( );
    }
}
