
#version 120
#extension GL_EXT_geometry_shader4 : enable

uniform int N;
uniform float radiusX;
uniform float radiusY;
uniform bool solid;

void main() {
	const float PI = 3.1415927; //rounded up from 26535...
	float step = (2 * PI) / N;
	vec4 center = gl_PositionIn[0];
	vec4 x = vec4(radiusX, 0, 0, 0),
	     y = vec4(0, radiusY, 0, 0); 
	
	gl_FrontColor = gl_FrontColorIn[0];
	vec4 firstPoint = center + x;
	if(solid) {
		for(int i = 0; i < N - 1; i++) {
			gl_Position = firstPoint;
			EmitVertex();
			
			float phase = step * i;
			gl_Position = center + cos(phase) * x + sin(phase) * y;
			EmitVertex();
			
			phase = phase + step;
			gl_Position = center + cos(phase) * x + sin(phase) * y;
			EmitVertex();
		}
	} else {
	for(int i = 0; i < N; i++) {
			float phase = step * i;
			gl_Position = center + cos(phase) * x + sin(phase) * y;
			EmitVertex();
		}

		gl_Position = center + x;
		EmitVertex();
	}
}