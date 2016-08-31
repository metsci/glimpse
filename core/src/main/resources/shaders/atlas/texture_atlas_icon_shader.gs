#version 150
#extension GL_EXT_geometry_shader4 : enable

// viewport width and height ( see comment in main )
uniform float viewportWidth;
uniform float viewportHeight;

// icons can be globally scaled by a constant factor
uniform float globalScale;

layout (points) in;
layout (triangle_strip,max_vertices=4) out;

in VertexData {
    // height should include any border around the image
    // and offset should also move to the edge of the image+border
    // order in vector: width, height, offsetX, offsetY
    vec4 vpixelCoords;
    // order in vector: minX, maxX, minY, maxY
    vec4 vtexCoords;
    vec3 vpickColor;
} VertexIn[1];

out VertexData {
    out vec2 texCoord;
    out vec3 pickColor;
} VertexOut;

void main()
{
    VertexOut.pickColor = VertexIn[0].vpickColor;

    // extract rotation and scale from the vertex
    float rotation = gl_in[0].gl_Position.z;
    float scale = gl_in[0].gl_Position.w;

    // http://stackoverflow.com/questions/4202456/how-do-you-get-the-modelview-and-projection-matrices-in-opengl
    // at this point, the vertex shader has already run and coordinates have been transformed on -1 to 1
    // so we need to transform our desired pixelWidth/pixelHeight into that space
    float scaleX = globalScale * scale * 2.0 / viewportWidth;
    float scaleY = globalScale * scale * 2.0 / viewportHeight;
    
    float width = VertexIn[0].vpixelCoords[0];
    float height = VertexIn[0].vpixelCoords[1];

    float offsetX = -VertexIn[0].vpixelCoords[2];
    float offsetY = -VertexIn[0].vpixelCoords[3];

    // precompute sin/cos of the rotation angle 
    // is trig on GPU slow??? 
    float sina = sin( rotation );
    float cosa = cos( rotation );

    // transform the single vertex into a triangle strip with the correct size in pixels
    // and the correct texture coordinates for each vertex

    gl_Position.x = scaleX * ((offsetX) * cosa - (offsetY) * sina) + gl_in[0].gl_Position.x;
    gl_Position.y = scaleY * ((offsetX) * sina + (offsetY) * cosa) + gl_in[0].gl_Position.y;
    gl_Position.z = 0;
    gl_Position.w = 1;
    VertexOut.texCoord      = vec2( VertexIn[0].vtexCoords[0], VertexIn[0].vtexCoords[3] );
    EmitVertex();
    
    gl_Position.x = scaleX * ((offsetX+width) * cosa - (offsetY) * sina) + gl_in[0].gl_Position.x;
    gl_Position.y = scaleY * ((offsetX+width) * sina + (offsetY) * cosa) + gl_in[0].gl_Position.y;
    gl_Position.z = 0;
    gl_Position.w = 1;
    VertexOut.texCoord    = vec2( VertexIn[0].vtexCoords[1], VertexIn[0].vtexCoords[3] );
    EmitVertex();

    gl_Position.x = scaleX * ((offsetX) * cosa - (offsetY+height) * sina) + gl_in[0].gl_Position.x;
    gl_Position.y = scaleY * ((offsetX) * sina + (offsetY+height) * cosa) + gl_in[0].gl_Position.y;    
    gl_Position.z = 0;
    gl_Position.w = 1;
    VertexOut.texCoord    = vec2( VertexIn[0].vtexCoords[0], VertexIn[0].vtexCoords[2] );
    EmitVertex();
   
    gl_Position.x = scaleX * ((offsetX+width) * cosa - (offsetY+height) * sina) + gl_in[0].gl_Position.x;
    gl_Position.y = scaleY * ((offsetX+width) * sina + (offsetY+height) * cosa) + gl_in[0].gl_Position.y;
    gl_Position.z = 0;
    gl_Position.w = 1;
    VertexOut.texCoord    = vec2( VertexIn[0].vtexCoords[1], VertexIn[0].vtexCoords[2] );
    EmitVertex();
    
    EndPrimitive();
}