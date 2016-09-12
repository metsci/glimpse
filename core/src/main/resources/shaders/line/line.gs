#version 150

layout( lines_adjacency ) in;
layout( triangle_strip, max_vertices = 18 ) out;

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
out vec4 gRgba;
out float gFeather;

void main( )
{
    vec2 posB_PX = gl_in[ 1 ].gl_Position.xy;
    vec2 posC_PX = gl_in[ 2 ].gl_Position.xy;
    vec2 deltaBC_PX = posC_PX - posB_PX;
    float lengthBC_PX = length( deltaBC_PX );
    float mileageB_PX = vMileage_PX[ 1 ];
    float mileageC_PX = vMileage_PX[ 2 ];

    float normal_PX = 0.5*LINE_THICKNESS_PX;
    float feather_PX = 0.5*FEATHER_THICKNESS_PX;
    float innerNormal_PX = normal_PX - feather_PX;
    float outerNormal_PX = normal_PX + feather_PX;

    if ( lengthBC_PX > 0.0 && mileageC_PX >= mileageB_PX )
    {
        vec2 dirBC = deltaBC_PX / lengthBC_PX;
        vec2 normalBC = vec2( -dirBC.y, dirBC.x );


        // B
        //

        // Start with joinless defaults, and overwrite with joined values below
        vec2 innerBelowB_PX = posB_PX - innerNormal_PX*normalBC + feather_PX*dirBC;
        vec2 outerBelowB_PX = posB_PX - outerNormal_PX*normalBC - feather_PX*dirBC;
        vec2 innerAboveB_PX = posB_PX + innerNormal_PX*normalBC + feather_PX*dirBC;
        vec2 outerAboveB_PX = posB_PX + outerNormal_PX*normalBC - feather_PX*dirBC;
        vec2 innerJoinB_PX = innerAboveB_PX;
        vec2 outerJoinB_PX = outerAboveB_PX;

        vec2 posA_PX = gl_in[ 0 ].gl_Position.xy;
        vec2 deltaAB_PX = posB_PX - posA_PX;
        float lengthAB_PX = length( deltaAB_PX );
        float mileageA_PX = vMileage_PX[ 0 ];
        if ( lengthAB_PX > 0.0 && mileageB_PX >= mileageA_PX )
        {
            vec2 dirAB = deltaAB_PX / lengthAB_PX;
            vec2 normalAB = vec2( -dirAB.y, dirAB.x );
            vec2 deltaJoin = normalAB + normalBC;
            float lengthJoin = length( deltaJoin );
            if ( lengthJoin > 0.0 )
            {
                vec2 dirJoin = deltaJoin / lengthJoin;
                float bevelScale = dot( dirJoin, normalBC );
                float miterScale = 1.0 / bevelScale;

                float innerMiter_PX = innerNormal_PX * miterScale;
                float outerMiter_PX = outerNormal_PX * miterScale;

                float innerBevel_PX = ( normal_PX * bevelScale ) - feather_PX;
                float outerBevel_PX = ( normal_PX * bevelScale ) + feather_PX;

                float innerExtrude_PX = innerBevel_PX;
                float outerExtrude_PX = outerBevel_PX;

                float innerIntrude_PX = innerMiter_PX;
                float outerIntrude_PX = outerMiter_PX;

                // float innerExtrude_PX = ( lengthJoin > 0.25 ? miter_PX : bevel_PX );
                //
                // float innerIntrude_PX;
                // float outerIntrude_PX;
                // if ( )
                // {
                //     innerIntrude_PX = innerMiter_PX;
                //     outerIntrude_PX = outerMiter_PX;
                // }
                // else
                // {
                //     float intrudeScale = 1.0 / dot( dirJoin, dirBC );
                //     innerIntrude_PX = abs( ( lengthBC_PX - feather_PX ) * intrudeScale );
                //     outerIntrude_PX = abs( ( lengthBC_PX + feather_PX ) * intrudeScale );
                // }
                //
                if ( dot( dirJoin, dirAB ) < 0.0 )
                {
                    innerJoinB_PX = posB_PX - innerExtrude_PX*dirJoin;
                    outerJoinB_PX = posB_PX - outerExtrude_PX*dirJoin;

                    vec2 dirTangent = vec2( -dirJoin.y, dirJoin.x );
                    vec2 dirFeatherMiter = normalize( dirTangent + dirBC );
                    innerBelowB_PX = posB_PX - normal_PX*normalBC + feather_PX*dirFeatherMiter/dot( dirFeatherMiter, normalBC );
                    outerBelowB_PX = posB_PX - normal_PX*normalBC - feather_PX*dirFeatherMiter/dot( dirFeatherMiter, normalBC );

                    innerAboveB_PX = posB_PX + innerIntrude_PX*dirJoin;
                    outerAboveB_PX = posB_PX + outerIntrude_PX*dirJoin;
                }
                else
                {
                    innerJoinB_PX = posB_PX + innerExtrude_PX*dirJoin;
                    outerJoinB_PX = posB_PX + outerExtrude_PX*dirJoin;

                    innerBelowB_PX = posB_PX - innerIntrude_PX*dirJoin;
                    outerBelowB_PX = posB_PX - outerIntrude_PX*dirJoin;

                    vec2 dirTangent = vec2( -dirJoin.y, dirJoin.x );
                    vec2 dirFeatherMiter = normalize( dirTangent + dirBC );
                    innerAboveB_PX = posB_PX + normal_PX*normalBC - feather_PX*dirFeatherMiter/dot( dirFeatherMiter, normalBC );
                    outerAboveB_PX = posB_PX + normal_PX*normalBC + feather_PX*dirFeatherMiter/dot( dirFeatherMiter, normalBC );
                }
            }
        }


        // C
        //

        // Start with joinless defaults, and overwrite with joined values below
        vec2 innerBelowC_PX = posC_PX - innerNormal_PX*normalBC - feather_PX*dirBC;
        vec2 outerBelowC_PX = posC_PX - outerNormal_PX*normalBC + feather_PX*dirBC;
        vec2 innerAboveC_PX = posC_PX + innerNormal_PX*normalBC - feather_PX*dirBC;
        vec2 outerAboveC_PX = posC_PX + outerNormal_PX*normalBC + feather_PX*dirBC;
        vec2 innerJoinC_PX = innerBelowC_PX;
        vec2 outerJoinC_PX = outerBelowC_PX;

        vec2 posD_PX = gl_in[ 3 ].gl_Position.xy;
        vec2 deltaCD_PX = posD_PX - posC_PX;
        float lengthCD_PX = length( deltaCD_PX );
        float mileageD_PX = vMileage_PX[ 3 ];
        if ( lengthCD_PX > 0.0 && mileageD_PX >= mileageC_PX )
        {
            vec2 dirCD = deltaCD_PX / lengthCD_PX;
            vec2 normalCD = vec2( -dirCD.y, dirCD.x );
            vec2 deltaJoin = normalBC + normalCD;
            float lengthJoin = length( deltaJoin );
            if ( lengthJoin > 0.0 )
            {
                vec2 dirJoin = deltaJoin / lengthJoin;
                float bevelScale = dot( dirJoin, normalBC );
                float miterScale = 1.0 / bevelScale;

                float innerMiter_PX = innerNormal_PX * miterScale;
                float outerMiter_PX = outerNormal_PX * miterScale;

                float innerBevel_PX = ( normal_PX * bevelScale ) - feather_PX;
                float outerBevel_PX = ( normal_PX * bevelScale ) + feather_PX;

                float innerExtrude_PX = innerBevel_PX;
                float outerExtrude_PX = outerBevel_PX;

                float innerIntrude_PX = innerMiter_PX;
                float outerIntrude_PX = outerMiter_PX;

                if ( dot( dirJoin, dirBC ) < 0.0 )
                {
                    innerJoinC_PX = posC_PX - innerExtrude_PX*dirJoin;
                    outerJoinC_PX = posC_PX - outerExtrude_PX*dirJoin;

                    vec2 dirTangent = vec2( -dirJoin.y, dirJoin.x );
                    vec2 dirFeatherMiter = normalize( dirTangent + dirBC );
                    innerBelowC_PX = posC_PX - normal_PX*normalBC + feather_PX*dirFeatherMiter/dot( dirFeatherMiter, normalBC );
                    outerBelowC_PX = posC_PX - normal_PX*normalBC - feather_PX*dirFeatherMiter/dot( dirFeatherMiter, normalBC );

                    innerAboveC_PX = posC_PX + innerIntrude_PX*dirJoin;
                    outerAboveC_PX = posC_PX + outerIntrude_PX*dirJoin;
                }
                else
                {
                    innerJoinC_PX = posC_PX + innerExtrude_PX*dirJoin;
                    outerJoinC_PX = posC_PX + outerExtrude_PX*dirJoin;

                    innerBelowC_PX = posC_PX - innerIntrude_PX*dirJoin;
                    outerBelowC_PX = posC_PX - outerIntrude_PX*dirJoin;

                    vec2 dirTangent = vec2( -dirJoin.y, dirJoin.x );
                    vec2 dirFeatherMiter = normalize( dirTangent + dirBC );
                    innerAboveC_PX = posC_PX + normal_PX*normalBC - feather_PX*dirFeatherMiter/dot( dirFeatherMiter, normalBC );
                    outerAboveC_PX = posC_PX + normal_PX*normalBC + feather_PX*dirFeatherMiter/dot( dirFeatherMiter, normalBC );
                }
            }
        }




        // Line interior
        gRgba = vec4( 0.7, 0.0, 0.0, 0.5 );
        gFeather = 1.0;

        gl_Position = pxToNdc( innerJoinB_PX, VIEWPORT_SIZE_PX );
        EmitVertex( );

        gl_Position = pxToNdc( innerBelowB_PX, VIEWPORT_SIZE_PX );
        EmitVertex( );

        gl_Position = pxToNdc( innerAboveB_PX, VIEWPORT_SIZE_PX );
        EmitVertex( );

        gl_Position = pxToNdc( innerBelowC_PX, VIEWPORT_SIZE_PX );
        EmitVertex( );

        gl_Position = pxToNdc( innerAboveC_PX, VIEWPORT_SIZE_PX );
        EmitVertex( );

        gl_Position = pxToNdc( innerJoinC_PX, VIEWPORT_SIZE_PX );
        EmitVertex( );

        EndPrimitive( );


        // Feather below
        gRgba = vec4( 0.4, 0.5, 0.7, 0.5 );
        gFeather = 1.0;

        gl_Position = pxToNdc( innerJoinB_PX, VIEWPORT_SIZE_PX );
        EmitVertex( );

        gl_Position = pxToNdc( outerJoinB_PX, VIEWPORT_SIZE_PX );
        EmitVertex( );

        gl_Position = pxToNdc( innerBelowB_PX, VIEWPORT_SIZE_PX );
        EmitVertex( );

        gl_Position = pxToNdc( outerBelowB_PX, VIEWPORT_SIZE_PX );
        EmitVertex( );

        gl_Position = pxToNdc( innerBelowC_PX, VIEWPORT_SIZE_PX );
        EmitVertex( );

        gl_Position = pxToNdc( outerBelowC_PX, VIEWPORT_SIZE_PX );
        EmitVertex( );

        EndPrimitive( );


        // Feather above
        gRgba = vec4( 0.4, 0.5, 0.7, 0.5 );
        gFeather = 1.0;

        gl_Position = pxToNdc( innerJoinC_PX, VIEWPORT_SIZE_PX );
        EmitVertex( );

        gl_Position = pxToNdc( outerJoinC_PX, VIEWPORT_SIZE_PX );
        EmitVertex( );

        gl_Position = pxToNdc( innerAboveC_PX, VIEWPORT_SIZE_PX );
        EmitVertex( );

        gl_Position = pxToNdc( outerAboveC_PX, VIEWPORT_SIZE_PX );
        EmitVertex( );

        gl_Position = pxToNdc( innerAboveB_PX, VIEWPORT_SIZE_PX );
        EmitVertex( );

        gl_Position = pxToNdc( outerAboveB_PX, VIEWPORT_SIZE_PX );
        EmitVertex( );

        EndPrimitive( );
    }
}
