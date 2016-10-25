#version 150

layout( lines_adjacency ) in;
layout( triangle_strip, max_vertices = 18 ) out;


// Bit mask for whether to draw the line segment to the vertex in
// question, from the preceding vertex
const int FLAGS_CONNECT = 1 << 0;

// Bit mask for whether to use a join at the vertex in question
const int FLAGS_JOIN = 1 << 1;


vec4 pxToNdc( vec2 xy_PX, vec2 viewportSize_PX )
{
    vec2 xy_FRAC = xy_PX / viewportSize_PX;
    return vec4( -1.0 + 2.0*xy_FRAC, 0.0, 1.0 );
}


uniform vec2 VIEWPORT_SIZE_PX;
uniform float LINE_THICKNESS_PX;

// The width of the feather region, which lies along the edge of the
// line region, and across which alpha fades to zero. Half the width
// of the feather region (the more opaque half) lies inside the ideal
// bounds of the line. Half (the more transparent half) lies outside.
uniform float FEATHER_THICKNESS_PX;

// 0 = NONE, 1 = BEVEL, 2 = MITER
uniform int JOIN_TYPE;

// To keep miters from getting too long, mitering is only used when
// miterLength <= miterLimit*lineThickness (where miterLength is the
// distance from the outer tip of the miter to its inner corner).
// Otherwise, a bevel join is used instead.
uniform float MITER_LIMIT;


// Bit-flags for each vertex:
//  * Bit 0: CONNECT  (Least Significant Bit)
//  * Bit 1: JOIN
in int vFlags[];

// Cumulative distance to each vertex from the start of the connected
// line strip.
in float vMileage_PX[];


out float gMileage_PX;
out float gFeatherAlpha;


void main( )
{
    // The segment we're drawing starts at "B" (which is incoming vertex #1) and
    // ends at "C" (vertex #2). The vertex before this segment is "A" (vertex #0),
    // and the one after is "D" (vertex #3).
    //
    // "Inner" and "outer" refer to the inner and outer edges of the feather region.
    //
    // "Above" and "below" mean up and down, respectively, along the normalBC axis.
    //

    bool connectBC = ( ( vFlags[ 2 ] & FLAGS_CONNECT ) != 0 );
    if ( connectBC )
    {
        vec2 posB_PX = gl_in[ 1 ].gl_Position.xy;
        vec2 posC_PX = gl_in[ 2 ].gl_Position.xy;
        vec2 deltaBC_PX = posC_PX - posB_PX;
        float lengthBC_PX = length( deltaBC_PX );

        float normal_PX = 0.5*LINE_THICKNESS_PX;
        float feather_PX = 0.5*FEATHER_THICKNESS_PX;
        float innerNormal_PX = normal_PX - feather_PX;
        float outerNormal_PX = normal_PX + feather_PX;

        if ( lengthBC_PX > 0.0 )
        {
            vec2 dirBC = deltaBC_PX / lengthBC_PX;
            vec2 normalBC = vec2( -dirBC.y, dirBC.x );


            // B
            //

            bool joinB = ( JOIN_TYPE != 0 && ( vFlags[ 1 ] & FLAGS_JOIN ) != 0 );

            // Init to values appropriate for a JOIN_TYPE of NONE, then overwrite below based on JOIN_TYPE
            vec2 innerBelowB_PX = posB_PX - innerNormal_PX*normalBC + feather_PX*dirBC;
            vec2 outerBelowB_PX = posB_PX - outerNormal_PX*normalBC - feather_PX*dirBC;
            vec2 innerAboveB_PX = posB_PX + innerNormal_PX*normalBC + feather_PX*dirBC;
            vec2 outerAboveB_PX = posB_PX + outerNormal_PX*normalBC - feather_PX*dirBC;
            vec2 innerJoinB_PX = innerAboveB_PX;
            vec2 outerJoinB_PX = outerAboveB_PX;
            bool isLeftTurnB = true;

            if ( joinB )
            {
                vec2 posA_PX = gl_in[ 0 ].gl_Position.xy;
                vec2 deltaAB_PX = posB_PX - posA_PX;
                float lengthAB_PX = length( deltaAB_PX );
                if ( lengthAB_PX > 0.0 )
                {
                    vec2 dirAB = deltaAB_PX / lengthAB_PX;
                    vec2 normalAB = vec2( -dirAB.y, dirAB.x );
                    vec2 deltaJoin = normalAB + normalBC;
                    float lengthJoin = length( deltaJoin );
                    if ( lengthJoin > 0.01 )
                    {
                        vec2 dirJoin = deltaJoin / lengthJoin;
                        float bevelScale = dot( dirJoin, normalBC );
                        float miterScale = 1.0 / bevelScale;

                        // For a miter region, we feather out along normalBC
                        float innerMiter_PX = innerNormal_PX * miterScale;
                        float outerMiter_PX = outerNormal_PX * miterScale;

                        // For a bevel region, we feather out along dirJoin
                        float innerBevel_PX = ( normal_PX * bevelScale ) - feather_PX;
                        float outerBevel_PX = ( normal_PX * bevelScale ) + feather_PX;

                        // Extrude is the distance from B outward to the join vertex
                        bool useMiter = ( JOIN_TYPE == 2 && miterScale <= MITER_LIMIT );
                        float innerExtrude_PX = ( useMiter ? innerMiter_PX : innerBevel_PX );
                        float outerExtrude_PX = ( useMiter ? outerMiter_PX : outerBevel_PX );

                        // Intrude is the distance from B inward to where the lines separate
                        float maxIntrudeScale = 1.0 / dot( dirJoin, dirBC );
                        float innerIntrude_PX = min( innerMiter_PX, abs( ( lengthBC_PX - feather_PX ) * maxIntrudeScale ) );
                        float outerIntrude_PX = min( outerMiter_PX, abs( ( lengthBC_PX + feather_PX ) * maxIntrudeScale ) );

                        // Vector for mitering the corners of the feather region
                        vec2 dirFeatherMiter = normalize( dirJoin + normalBC );
                        vec2 featherMiter_PX = ( feather_PX / dot( dirFeatherMiter, normalBC ) ) * dirFeatherMiter;

                        // To get triangle_strip to work, vertex order must differ for left and right turns
                        if ( dot( dirJoin, dirAB ) < 0.0 )
                        {
                            isLeftTurnB = true;

                            innerJoinB_PX = posB_PX - innerExtrude_PX*dirJoin;
                            outerJoinB_PX = posB_PX - outerExtrude_PX*dirJoin;

                            innerBelowB_PX = posB_PX - normal_PX*normalBC + featherMiter_PX;
                            outerBelowB_PX = posB_PX - normal_PX*normalBC - featherMiter_PX;

                            innerAboveB_PX = posB_PX + innerIntrude_PX*dirJoin;
                            outerAboveB_PX = posB_PX + outerIntrude_PX*dirJoin;
                        }
                        else
                        {
                            isLeftTurnB = false;

                            innerJoinB_PX = posB_PX + innerExtrude_PX*dirJoin;
                            outerJoinB_PX = posB_PX + outerExtrude_PX*dirJoin;

                            innerBelowB_PX = posB_PX - innerIntrude_PX*dirJoin;
                            outerBelowB_PX = posB_PX - outerIntrude_PX*dirJoin;

                            innerAboveB_PX = posB_PX + normal_PX*normalBC - featherMiter_PX;
                            outerAboveB_PX = posB_PX + normal_PX*normalBC + featherMiter_PX;
                        }
                    }
                }
            }


            // C
            //

            bool joinC = ( JOIN_TYPE != 0 && ( vFlags[ 2 ] & FLAGS_JOIN ) != 0 );

            // Init to values appropriate for a JOIN_TYPE of NONE, then overwrite below based on JOIN_TYPE
            vec2 innerBelowC_PX = posC_PX - innerNormal_PX*normalBC - feather_PX*dirBC;
            vec2 outerBelowC_PX = posC_PX - outerNormal_PX*normalBC + feather_PX*dirBC;
            vec2 innerAboveC_PX = posC_PX + innerNormal_PX*normalBC - feather_PX*dirBC;
            vec2 outerAboveC_PX = posC_PX + outerNormal_PX*normalBC + feather_PX*dirBC;
            vec2 innerJoinC_PX = innerBelowC_PX;
            vec2 outerJoinC_PX = outerBelowC_PX;
            bool isLeftTurnC = false;

            if ( joinC )
            {
                vec2 posD_PX = gl_in[ 3 ].gl_Position.xy;
                vec2 deltaCD_PX = posD_PX - posC_PX;
                float lengthCD_PX = length( deltaCD_PX );
                if ( lengthCD_PX > 0.0 )
                {
                    vec2 dirCD = deltaCD_PX / lengthCD_PX;
                    vec2 normalCD = vec2( -dirCD.y, dirCD.x );
                    vec2 deltaJoin = normalBC + normalCD;
                    float lengthJoin = length( deltaJoin );
                    if ( lengthJoin > 0.01 )
                    {
                        vec2 dirJoin = deltaJoin / lengthJoin;
                        float bevelScale = dot( dirJoin, normalBC );
                        float miterScale = 1.0 / bevelScale;

                        // For a miter region, we feather out along normalBC
                        float innerMiter_PX = innerNormal_PX * miterScale;
                        float outerMiter_PX = outerNormal_PX * miterScale;

                        // For a bevel region, we feather out along dirJoin
                        float innerBevel_PX = ( normal_PX * bevelScale ) - feather_PX;
                        float outerBevel_PX = ( normal_PX * bevelScale ) + feather_PX;

                        // Extrude is the distance from C outward to the join vertex
                        bool useMiter = ( JOIN_TYPE == 2 && miterScale <= MITER_LIMIT );
                        float innerExtrude_PX = ( useMiter ? innerMiter_PX : innerBevel_PX );
                        float outerExtrude_PX = ( useMiter ? outerMiter_PX : outerBevel_PX );

                        // Intrude is the distance from C inward to where the lines separate
                        float maxIntrudeScale = 1.0 / dot( dirJoin, dirBC );
                        float innerIntrude_PX = min( innerMiter_PX, abs( ( lengthBC_PX - feather_PX ) * maxIntrudeScale ) );
                        float outerIntrude_PX = min( outerMiter_PX, abs( ( lengthBC_PX + feather_PX ) * maxIntrudeScale ) );

                        // Vector for mitering the corners of the feather region
                        vec2 dirFeatherMiter = normalize( dirJoin + normalBC );
                        vec2 featherMiter_PX = ( feather_PX / dot( dirFeatherMiter, normalBC ) ) * dirFeatherMiter;

                        // To get triangle_strip to work, vertex order must differ for left and right turns
                        if ( dot( dirJoin, dirBC ) < 0.0 )
                        {
                            isLeftTurnC = true;

                            innerJoinC_PX = posC_PX - innerExtrude_PX*dirJoin;
                            outerJoinC_PX = posC_PX - outerExtrude_PX*dirJoin;

                            innerBelowC_PX = posC_PX - normal_PX*normalBC + featherMiter_PX;
                            outerBelowC_PX = posC_PX - normal_PX*normalBC - featherMiter_PX;

                            innerAboveC_PX = posC_PX + innerIntrude_PX*dirJoin;
                            outerAboveC_PX = posC_PX + outerIntrude_PX*dirJoin;
                        }
                        else
                        {
                            isLeftTurnC = false;

                            innerJoinC_PX = posC_PX + innerExtrude_PX*dirJoin;
                            outerJoinC_PX = posC_PX + outerExtrude_PX*dirJoin;

                            innerBelowC_PX = posC_PX - innerIntrude_PX*dirJoin;
                            outerBelowC_PX = posC_PX - outerIntrude_PX*dirJoin;

                            innerAboveC_PX = posC_PX + normal_PX*normalBC - featherMiter_PX;
                            outerAboveC_PX = posC_PX + normal_PX*normalBC + featherMiter_PX;
                        }
                    }
                }
            }


            float mileageB_PX = vMileage_PX[ 1 ];


            // Emit triangle-strip for line interior
            //

            gFeatherAlpha = 1.0;

            if ( joinB )
            {
                gl_Position = pxToNdc( innerJoinB_PX, VIEWPORT_SIZE_PX );
                gMileage_PX = mileageB_PX + dot( dirBC, innerJoinB_PX - posB_PX );
                EmitVertex( );
            }

            gl_Position = pxToNdc( innerBelowB_PX, VIEWPORT_SIZE_PX );
            gMileage_PX = mileageB_PX + dot( dirBC, innerBelowB_PX - posB_PX );
            EmitVertex( );

            gl_Position = pxToNdc( innerAboveB_PX, VIEWPORT_SIZE_PX );
            gMileage_PX = mileageB_PX + dot( dirBC, innerAboveB_PX - posB_PX );
            EmitVertex( );

            gl_Position = pxToNdc( innerBelowC_PX, VIEWPORT_SIZE_PX );
            gMileage_PX = mileageB_PX + dot( dirBC, innerBelowC_PX - posB_PX );
            EmitVertex( );

            gl_Position = pxToNdc( innerAboveC_PX, VIEWPORT_SIZE_PX );
            gMileage_PX = mileageB_PX + dot( dirBC, innerAboveC_PX - posB_PX );
            EmitVertex( );

            if ( joinC )
            {
                gl_Position = pxToNdc( innerJoinC_PX, VIEWPORT_SIZE_PX );
                gMileage_PX = mileageB_PX + dot( dirBC, innerJoinC_PX - posB_PX );
                EmitVertex( );
            }

            EndPrimitive( );


            if ( FEATHER_THICKNESS_PX > 0.0 )
            {

                // Emit triangle-strip for feather region below line
                //

                if ( isLeftTurnB )
                {
                    gl_Position = pxToNdc( innerJoinB_PX, VIEWPORT_SIZE_PX );
                    gMileage_PX = mileageB_PX + dot( dirBC, innerJoinB_PX - posB_PX );
                    gFeatherAlpha = 1.0;
                    EmitVertex( );

                    gl_Position = pxToNdc( outerJoinB_PX, VIEWPORT_SIZE_PX );
                    gMileage_PX = mileageB_PX + dot( dirBC, outerJoinB_PX - posB_PX );
                    gFeatherAlpha = 0.0;
                    EmitVertex( );
                }

                gl_Position = pxToNdc( innerBelowB_PX, VIEWPORT_SIZE_PX );
                gMileage_PX = mileageB_PX + dot( dirBC, innerBelowB_PX - posB_PX );
                gFeatherAlpha = 1.0;
                EmitVertex( );

                gl_Position = pxToNdc( outerBelowB_PX, VIEWPORT_SIZE_PX );
                gMileage_PX = mileageB_PX + dot( dirBC, outerBelowB_PX - posB_PX );
                gFeatherAlpha = 0.0;
                EmitVertex( );

                gl_Position = pxToNdc( innerBelowC_PX, VIEWPORT_SIZE_PX );
                gMileage_PX = mileageB_PX + dot( dirBC, innerBelowC_PX - posB_PX );
                gFeatherAlpha = 1.0;
                EmitVertex( );

                gl_Position = pxToNdc( outerBelowC_PX, VIEWPORT_SIZE_PX );
                gMileage_PX = mileageB_PX + dot( dirBC, outerBelowC_PX - posB_PX );
                gFeatherAlpha = 0.0;
                EmitVertex( );

                if ( isLeftTurnC )
                {
                    gl_Position = pxToNdc( innerJoinC_PX, VIEWPORT_SIZE_PX );
                    gMileage_PX = mileageB_PX + dot( dirBC, innerJoinC_PX - posB_PX );
                    gFeatherAlpha = 1.0;
                    EmitVertex( );

                    gl_Position = pxToNdc( outerJoinC_PX, VIEWPORT_SIZE_PX );
                    gMileage_PX = mileageB_PX + dot( dirBC, outerJoinC_PX - posB_PX );
                    gFeatherAlpha = 0.0;
                    EmitVertex( );
                }

                EndPrimitive( );


                // Emit triangle-strip for feather region above line
                //

                if ( !isLeftTurnC )
                {
                    gl_Position = pxToNdc( innerJoinC_PX, VIEWPORT_SIZE_PX );
                    gMileage_PX = mileageB_PX + dot( dirBC, innerJoinC_PX - posB_PX );
                    gFeatherAlpha = 1.0;
                    EmitVertex( );

                    gl_Position = pxToNdc( outerJoinC_PX, VIEWPORT_SIZE_PX );
                    gMileage_PX = mileageB_PX + dot( dirBC, outerJoinC_PX - posB_PX );
                    gFeatherAlpha = 0.0;
                    EmitVertex( );
                }

                gl_Position = pxToNdc( innerAboveC_PX, VIEWPORT_SIZE_PX );
                gMileage_PX = mileageB_PX + dot( dirBC, innerAboveC_PX - posB_PX );
                gFeatherAlpha = 1.0;
                EmitVertex( );

                gl_Position = pxToNdc( outerAboveC_PX, VIEWPORT_SIZE_PX );
                gMileage_PX = mileageB_PX + dot( dirBC, outerAboveC_PX - posB_PX );
                gFeatherAlpha = 0.0;
                EmitVertex( );

                gl_Position = pxToNdc( innerAboveB_PX, VIEWPORT_SIZE_PX );
                gMileage_PX = mileageB_PX + dot( dirBC, innerAboveB_PX - posB_PX );
                gFeatherAlpha = 1.0;
                EmitVertex( );

                gl_Position = pxToNdc( outerAboveB_PX, VIEWPORT_SIZE_PX );
                gMileage_PX = mileageB_PX + dot( dirBC, outerAboveB_PX - posB_PX );
                gFeatherAlpha = 0.0;
                EmitVertex( );

                if ( !isLeftTurnB )
                {
                    gl_Position = pxToNdc( innerJoinB_PX, VIEWPORT_SIZE_PX );
                    gMileage_PX = mileageB_PX + dot( dirBC, innerJoinB_PX - posB_PX );
                    gFeatherAlpha = 1.0;
                    EmitVertex( );

                    gl_Position = pxToNdc( outerJoinB_PX, VIEWPORT_SIZE_PX );
                    gMileage_PX = mileageB_PX + dot( dirBC, outerJoinB_PX - posB_PX );
                    gFeatherAlpha = 0.0;
                    EmitVertex( );
                }

                EndPrimitive( );

            }
        }
    }
}
