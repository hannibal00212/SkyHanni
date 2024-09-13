#version 120

// Rect specific uniforms
uniform float scaleFactor;
uniform float radius;
uniform vec2 halfSize;
uniform vec2 centerPos;

// Outline specific uniforms
uniform float borderThickness;
uniform float borderBlur;

varying vec4 color;

// From https://www.shadertoy.com/view/WtdSDs
float roundedRectSDF(vec2 center, vec2 halfSize, float radius) {
    return length(max(abs(center) - halfSize + radius, 0.0)) - radius;
}

void main() {
    float xScale = gl_ModelViewMatrix[0][0];
    float yScale = gl_ModelViewMatrix[1][1];
    float xTranslation = gl_ModelViewMatrix[3][0];
    float yTranslation = gl_ModelViewMatrix[3][1];

    vec2 newHalfSize = vec2(halfSize.x * xScale, halfSize.y * yScale);

    float newCenterPosY = centerPos.y;
    if (yScale != 1.0) {
        newCenterPosY = centerPos.y - (halfSize.y * (yScale - 1));
    }

    vec2 newCenterPos = vec2((centerPos.x * xScale) + (xTranslation * scaleFactor), newCenterPosY - (yTranslation * scaleFactor));

    float distance = roundedRectSDF(gl_FragCoord.xy - newCenterPos, newHalfSize, max(radius, borderThickness));

    // In testing, keeping the upper bound at 1.0 and letting the lower be changable seemed the most sensible for nice results
    float smoothed = 1.0 - smoothstep(borderBlur, 1.0, abs(distance / borderThickness));
    gl_FragColor = color * vec4(1.0, 1.0, 1.0, smoothed);
}
