#version 120

attribute vec4 pixelCoords;
attribute vec4 texCoords;
attribute vec3 pickColor;

varying out vec4 vpixelCoords;
varying out vec4 vtexCoords;
varying out vec3 vpickColor; 

void main( )
{
    // pass through icon widths and heights and center to geometry shader
    // order in vector: width, height, offsetX, offsetY
    vpixelCoords = pixelCoords;

    // pass through icon texture coordinates to geometry shader
    // order in vector: minX, maxX, minY, maxY
    vtexCoords = texCoords;
    
    // pass through picking color
    vpickColor = pickColor;

    // copy out the rotation and scale values in the vertex before applying transform
    float rotation = gl_Vertex.z;
    float scale = gl_Vertex.w;

    // transform vertex (this will have to change with later OpenGL versions)
    gl_Vertex.z = 0;
    gl_Vertex.w = 1;
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
    
    // replace the scale and rotation so they make it to the geometry shader    
    gl_Position.z = rotation;
    gl_Position.w = scale;
}