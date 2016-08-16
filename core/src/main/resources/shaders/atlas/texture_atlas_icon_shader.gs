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
varying in vec4 vpixelCoords[];

// order in vector: minX, maxX, minY, maxY
varying in vec4 vtexCoords[];

// vector containing picking color
varying in vec3 vpickColor[];

varying out vec2 TexCoord;

varying out vec3 pickColor;


void main()
{
    pickColor = vpickColor[0];

    // extract rotation and scale from the vertex
    float rotation = gl_PositionIn[0].z;
    float scale = gl_PositionIn[0].w;

    // http://stackoverflow.com/questions/4202456/how-do-you-get-the-modelview-and-projection-matrices-in-opengl
    // at this point, the vertex shader has already run and coordinates have been transformed on -1 to 1
    // so we need to transform our desired pixelWidth/pixelHeight into that space
    float scaleX = globalScale * scale * 2.0 / viewportWidth;
    float scaleY = globalScale * scale * 2.0 / viewportHeight;
    
    float width = vpixelCoords[0][0];
    float height = vpixelCoords[0][1];

    float offsetX = -vpixelCoords[0][2];
    float offsetY = -vpixelCoords[0][3];

    // precompute sin/cos of the rotation angle 
    // is trig on GPU slow??? 
    float sina = sin( rotation );
    float cosa = cos( rotation );

    // transform the single vertex into a triangle strip with the correct size in pixels
    // and the correct texture coordinates for each vertex

    gl_Position.x = scaleX * ((offsetX) * cosa - (offsetY) * sina) + gl_PositionIn[0].x;
    gl_Position.y = scaleY * ((offsetX) * sina + (offsetY) * cosa) + gl_PositionIn[0].y;
    gl_Position.z = 0;
    gl_Position.w = 1;
    TexCoord      = vec2( vtexCoords[0][0], vtexCoords[0][3] );
    EmitVertex();
    
    gl_Position.x = scaleX * ((offsetX+width) * cosa - (offsetY) * sina) + gl_PositionIn[0].x;
    gl_Position.y = scaleY * ((offsetX+width) * sina + (offsetY) * cosa) + gl_PositionIn[0].y;
    gl_Position.z = 0;
    gl_Position.w = 1;
    TexCoord    = vec2( vtexCoords[0][1], vtexCoords[0][3] );
    EmitVertex();

    gl_Position.x = scaleX * ((offsetX) * cosa - (offsetY+height) * sina) + gl_PositionIn[0].x;
    gl_Position.y = scaleY * ((offsetX) * sina + (offsetY+height) * cosa) + gl_PositionIn[0].y;    
    gl_Position.z = 0;
    gl_Position.w = 1;
    TexCoord    = vec2( vtexCoords[0][0], vtexCoords[0][2] );
    EmitVertex();
   
    gl_Position.x = scaleX * ((offsetX+width) * cosa - (offsetY+height) * sina) + gl_PositionIn[0].x;
    gl_Position.y = scaleY * ((offsetX+width) * sina + (offsetY+height) * cosa) + gl_PositionIn[0].y;
    gl_Position.z = 0;
    gl_Position.w = 1;
    TexCoord    = vec2( vtexCoords[0][1], vtexCoords[0][2] );
    EmitVertex();
    
    EndPrimitive();
}