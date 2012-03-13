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

    // transform vertex (this will have to change with later OpenGL versions)
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
}