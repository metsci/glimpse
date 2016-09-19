#version 150

layout( points ) in;
layout( triangle_strip, max_vertices = 4 ) out;

uniform vec2 VIEWPORT_SIZE_PX;
uniform vec2 ATLAS_SIZE_PX;
uniform float HIGHLIGHT_SCALE;

in float vHighlight[];
in vec2 vImageAlign[];
in vec4 vImageBounds[];

out vec2 gAtlasCoords;
out float gHighlight;

void main( )
{
    vec2 p = gl_in[ 0 ].gl_Position.xy;

    vec2 imageAlign = vImageAlign[ 0 ];

    vec4 imageBounds = vImageBounds[ 0 ];
    float sMin = imageBounds.s;
    float tMin = imageBounds.t;
    float sMax = imageBounds.p;
    float tMax = imageBounds.q;

    vec2 imageSize_PX = vec2( sMax - sMin, tMax - tMin ) * ATLAS_SIZE_PX;
    vec2 offsetA_PX = -imageAlign * imageSize_PX;
    vec2 offsetB_PX = offsetA_PX + imageSize_PX;
    bool highlight = ( vHighlight[ 0 ] >= 0.5 );
    if ( highlight )
    {
        offsetA_PX *= HIGHLIGHT_SCALE;
        offsetB_PX *= HIGHLIGHT_SCALE;
    }

    vec2 pxToNdc = 2.0 / VIEWPORT_SIZE_PX;
    vec2 offsetA = offsetA_PX * pxToNdc;
    vec2 offsetB = offsetB_PX * pxToNdc;


    gl_Position.xy = p + vec2( offsetA.x, offsetB.y );
    gl_Position.zw = vec2( 0.0, 1.0 );
    gAtlasCoords = vec2( sMin, tMin );
    gHighlight = vHighlight[ 0 ];
    EmitVertex( );

    gl_Position.xy = p + offsetB;
    gl_Position.zw = vec2( 0.0, 1.0 );
    gAtlasCoords = vec2( sMax, tMin );
    gHighlight = vHighlight[ 0 ];
    EmitVertex( );

    gl_Position.xy = p + offsetA;
    gl_Position.zw = vec2( 0.0, 1.0 );
    gAtlasCoords = vec2( sMin, tMax );
    gHighlight = vHighlight[ 0 ];
    EmitVertex( );

    gl_Position.xy = p + vec2( offsetB.x, offsetA.y );
    gl_Position.zw = vec2( 0.0, 1.0 );
    gAtlasCoords = vec2( sMax, tMax );
    gHighlight = vHighlight[ 0 ];
    EmitVertex( );


    EndPrimitive( );
}
