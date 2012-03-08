in vec3 vertex;

void main() {
	gl_FrontColor = gl_Color;
	gl_Position = ftransform();
	//gl_Position = projection_matrix * modelview_matrix * vec4(vertex, 1.0);
}
