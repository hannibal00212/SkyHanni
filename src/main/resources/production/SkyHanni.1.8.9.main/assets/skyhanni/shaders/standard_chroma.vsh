// Chroma Vertex Shader
// (Same as textured_chroma.vsh but isn't restricted to only texture elements)

#version 120

varying vec4 originalColor;

void main() {
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;

    // Pass original color to fragment
    originalColor = gl_Color;
}
