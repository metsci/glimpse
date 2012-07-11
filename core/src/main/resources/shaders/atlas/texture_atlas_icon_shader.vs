#version 120

attribute vec4 pixelCoords;
attribute vec4 texCoords;
attribute vec3 pickColor;

varying vec4 vpixelCoords;
varying vec4 vtexCoords;
varying vec3 vpickColor; 

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
    gl_Position = gl_ModelViewProjectionMatrix * vec4( gl_Vertex.xy, 0, 1 );
    
    // replace the scale and rotation so they make it to the geometry shader    
    gl_Position.z = gl_Vertex.z;
    gl_Position.w = gl_Vertex.w;
}