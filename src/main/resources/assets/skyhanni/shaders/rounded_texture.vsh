#version 120

varying vec2 outTextureCoords;
varying vec4 outColor;

void main(){
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
    outColor = gl_Color;
    outTextureCoords = gl_MultiTexCoord0.st;
}
