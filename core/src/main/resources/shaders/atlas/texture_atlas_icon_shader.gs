
#version 120
#extension GL_EXT_geometry_shader4 : enable

// viewport width and height ( see comment in main )
uniform float viewportWidth;
uniform float viewportHeight;

// icons can be globally scaled by a constant factor
uniform float globalScale;

// height should include any border around the image
// and offset should also move to the edge of the image+border
// order in vector: width, height, offsetX, offsetY
in vec4 vpixelCoords[];

// order in vector: minX, maxX, minY, maxY
in vec4 vtexCoords[];

// vector containing picking color
in vec3 vpickColor[];

varying out vec2 TexCoord;

varying out vec3 pickColor;


void main()
{
    pickColor = vpickColor[0];

    // http://stackoverflow.com/questions/4202456/how-do-you-get-the-modelview-and-projection-matrices-in-opengl
    // apparently at this point, the vertex shader has already run
    // and coordinates have been transformed on -1 to 1
    // so we need to transform our desired pixelWidth/pixelHeight
    // into that space
    float scaleX = globalScale * 2.0 / viewportWidth;
    float scaleY = globalScale * 2.0 / viewportHeight;
    
    float width = vpixelCoords[0][0] * scaleX;
    float height = vpixelCoords[0][1] * scaleY;

    float offsetX = vpixelCoords[0][2] * scaleX;
    float offsetY = vpixelCoords[0][3] * scaleY;

    float cornerX = gl_PositionIn[0].x - offsetX;
    float cornerY = gl_PositionIn[0].y - offsetY;

    // transform the single vertex into a triangle strip with the correct size in pixels
    // and the correct texture coordinates for each vertex

    gl_Position.x = cornerX;
    gl_Position.y = cornerY;
    gl_Position.z = 0;
    gl_Position.w = 1;
    TexCoord      = vec2( vtexCoords[0][0], vtexCoords[0][3] );
    EmitVertex();
    
    gl_Position.x = cornerX + width;
    gl_Position.y = cornerY;
    gl_Position.z = 0;
    gl_Position.w = 1;
    TexCoord    = vec2( vtexCoords[0][1], vtexCoords[0][3] );
    EmitVertex();

    gl_Position.x = cornerX;
    gl_Position.y = cornerY + height;
    gl_Position.z = 0;
    gl_Position.w = 1;
    TexCoord    = vec2( vtexCoords[0][0], vtexCoords[0][2] );
    EmitVertex();
   
    gl_Position.x = cornerX + width;
    gl_Position.y = cornerY + height;
    gl_Position.z = 0;
    gl_Position.w = 1;
    TexCoord    = vec2( vtexCoords[0][1], vtexCoords[0][2] );
    EmitVertex();
    
    EndPrimitive();
}