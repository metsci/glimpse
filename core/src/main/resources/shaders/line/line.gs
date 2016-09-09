#version 150

layout( lines_adjacency ) in;
layout( triangle_strip, max_vertices = 6 ) out;

vec4 pxToNdc( vec2 xy_PX, vec2 viewportSize_PX )
{
    vec2 xy_FRAC = xy_PX / viewportSize_PX;
    return vec4( -1.0 + 2.0*xy_FRAC, 0.0, 1.0 );
}

uniform vec2 VIEWPORT_SIZE_PX;
uniform float LINE_THICKNESS_PX;
uniform float FEATHER_THICKNESS_PX;

in float vMileage_PX[];

// out vec2 gPosInQuad_PX;
// out float gMileage_PX;
// out float gQuadLength_PX;

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

        //float halfFeather_PX = 0.5 * FEATHER_THICKNESS_PX;
        float halfNormal_PX = 0.5*LINE_THICKNESS_PX;// + halfFeather_PX;

        // B
        {
            vec2 posA_PX = gl_in[ 0 ].gl_Position.xy;
            vec2 deltaAB_PX = posB_PX - posA_PX;
            float lengthAB_PX = length( deltaAB_PX );
            float mileageA_PX = vMileage_PX[ 0 ];

            if ( lengthAB_PX > 0.0 && mileageB_PX >= mileageA_PX )
            {
                vec2 dirAB = deltaAB_PX / lengthAB_PX;
                vec2 dirCB = -dirBC;
                float dirAB_x_dirCB = dirAB.x*dirCB.y - dirAB.y*dirCB.x;
                vec2 extrudeB = ( dirAB + dirCB ) / dirAB_x_dirCB;

                bool isLeftTurnABC = ( dirAB_x_dirCB < 0.0 );
                if ( isLeftTurnABC )
                {
                    // Join
                    gl_Position = pxToNdc( posB_PX - halfNormal_PX*extrudeB, VIEWPORT_SIZE_PX );
                    EmitVertex( );

                    // Below
                    gl_Position = pxToNdc( posB_PX - halfNormal_PX*normalBC, VIEWPORT_SIZE_PX );
                    EmitVertex( );

                    // Above
                    gl_Position = pxToNdc( posB_PX + halfNormal_PX*extrudeB, VIEWPORT_SIZE_PX );
                    EmitVertex( );
                }
                else
                {
                    // Join
                    gl_Position = pxToNdc( posB_PX + halfNormal_PX*extrudeB, VIEWPORT_SIZE_PX );
                    EmitVertex( );

                    // Below
                    gl_Position = pxToNdc( posB_PX - halfNormal_PX*extrudeB, VIEWPORT_SIZE_PX );
                    EmitVertex( );

                    // Above
                    gl_Position = pxToNdc( posB_PX + halfNormal_PX*normalBC, VIEWPORT_SIZE_PX );
                    EmitVertex( );
                }
            }
            else
            {
                // Below
                gl_Position = pxToNdc( posB_PX - halfNormal_PX*normalBC, VIEWPORT_SIZE_PX );
                EmitVertex( );

                // Above
                gl_Position = pxToNdc( posB_PX + halfNormal_PX*normalBC, VIEWPORT_SIZE_PX );
                EmitVertex( );
            }
        }

        // C
        {
            vec2 posD_PX = gl_in[ 3 ].gl_Position.xy;
            vec2 deltaDC_PX = posC_PX - posD_PX;
            float lengthDC_PX = length( deltaDC_PX );
            float mileageD_PX = vMileage_PX[ 3 ];

            if ( lengthDC_PX > 0.0 && mileageD_PX >= mileageC_PX )
            {
                vec2 dirDC = deltaDC_PX / lengthDC_PX;
                float dirBC_x_dirDC = dirBC.x*dirDC.y - dirBC.y*dirDC.x;
                vec2 extrudeC = ( dirBC + dirDC ) / dirBC_x_dirDC;

                bool isLeftTurnBCD = ( dirBC_x_dirDC < 0.0 );
                if ( isLeftTurnBCD )
                {
                    // Below
                    gl_Position = pxToNdc( posC_PX - halfNormal_PX*normalBC, VIEWPORT_SIZE_PX );
                    EmitVertex( );

                    // Above
                    gl_Position = pxToNdc( posC_PX + halfNormal_PX*extrudeC, VIEWPORT_SIZE_PX );
                    EmitVertex( );

                    // Join
                    gl_Position = pxToNdc( posC_PX - halfNormal_PX*extrudeC, VIEWPORT_SIZE_PX );
                    EmitVertex( );
                }
                else
                {
                    // Below
                    gl_Position = pxToNdc( posC_PX - halfNormal_PX*extrudeC, VIEWPORT_SIZE_PX );
                    EmitVertex( );

                    // Above
                    gl_Position = pxToNdc( posC_PX + halfNormal_PX*normalBC, VIEWPORT_SIZE_PX );
                    EmitVertex( );

                    // Join
                    gl_Position = pxToNdc( posC_PX + halfNormal_PX*extrudeC, VIEWPORT_SIZE_PX );
                    EmitVertex( );
                }
            }
            else
            {
                // Below
                gl_Position = pxToNdc( posC_PX - halfNormal_PX*normalBC, VIEWPORT_SIZE_PX );
                EmitVertex( );

                // Above
                gl_Position = pxToNdc( posC_PX + halfNormal_PX*normalBC, VIEWPORT_SIZE_PX );
                EmitVertex( );
            }
        }

        EndPrimitive( );
    }
}
