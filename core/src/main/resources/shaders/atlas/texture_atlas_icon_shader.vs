#version 150

in vec4 a_position;

in vec4 pixelCoords;
in vec4 texCoords;
in vec3 pickColor;

out VertexData {
    vec4 vpixelCoords;
    vec4 vtexCoords;
    vec3 vpickColor; 
} VertexOut;

uniform mat4 mvpMatrix;

void main( )
{
    // pass through icon widths and heights and center to geometry shader
    // order in vector: width, height, offsetX, offsetY
    VertexOut.vpixelCoords = pixelCoords;

    // pass through icon texture coordinates to geometry shader
    // order in vector: minX, maxX, minY, maxY
    VertexOut.vtexCoords = texCoords;
    
    // pass through picking color
    VertexOut.vpickColor = pickColor;

    // transform vertex (this will have to change with later OpenGL versions)
    gl_Position = mvpMatrix * vec4( a_position.xy, 0, 1 );
    
    // replace the scale and rotation so they make it to the geometry shader    
    gl_Position.z = a_position.z;
    gl_Position.w = a_position.w;
}