// Textured Chroma Vertex Shader
// Credit: https://github.com/BiscuitDevelopment/SkyblockAddons/blob/main/src/main/resources/assets/skyblockaddons/shaders/program/chroma_screen_textured.vsh

#version 120

varying vec2 outTextureCoords;
varying vec4 outColor;

void main() {
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;

    // Pass the color & texture coords to the fragment shader
    outColor = gl_Color;
    outTextureCoords = gl_MultiTexCoord0.st;
}
