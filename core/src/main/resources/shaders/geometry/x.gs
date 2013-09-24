
#version 120
#extension GL_EXT_geometry_shader4 : enable

void main() {
	vec4 a = gl_PositionIn[0],
	     b = gl_PositionIn[1];
	vec4 delta = a - b;
	vec4 across = vec4(-delta.y, delta.x, delta.z, delta.w);
	vec4 halfway = (a + b) / 2;
	
	vec4 locations[5];
	locations[0] = a;
	locations[1] = b;
	locations[2] = halfway;
	locations[3] = halfway + across / 2;
	locations[4] = halfway - across / 2;
	
	gl_FrontColor = gl_FrontColorIn[0];
	for(int i = 0; i < 5; i++) {
		gl_Position = locations[i];
		EmitVertex();
	}
}