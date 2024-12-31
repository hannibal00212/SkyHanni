#version 120

varying vec4 color;

void main() {
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
    color = gl_Color;
}
