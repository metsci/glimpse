
#version 120
#extension GL_EXT_geometry_shader4 : enable

uniform int N;
uniform bool solid;

void main() {
	const float PI = 3.1415927; //rounded up from 26535...
	float step = (2 * PI) / N;
	vec4 center = gl_PositionIn[0];
	vec4 s = gl_PositionIn[1] - gl_PositionIn[0];
	vec4 t = vec4(-s.y, s.x, s.z, s.w);
	
	gl_FrontColor = gl_FrontColorIn[0];
	if(solid) {
		vec4 start = gl_PositionIn[1];
		for(int i = 0; i < N - 1; i++) {
			gl_Position = start;
			EmitVertex();
			
			float phase = step * i;
			gl_Position = center + cos(phase) * s + sin(phase) * t;
			EmitVertex();
			
			phase = phase + step;
			gl_Position = center + cos(phase) * s + sin(phase) * t;
			EmitVertex();
		}
	
	} else {
		for(int i = 0; i < N; i++) {
			float phase = step * i;
			gl_Position = center + cos(phase) * s + sin(phase) * t;
			EmitVertex();
		}
	
		gl_Position = center + s;
		EmitVertex();
	}

}