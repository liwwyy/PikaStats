#version 120
uniform sampler2D scene;
uniform vec2 screen;
uniform vec2 size;
uniform float opacity;
varying vec2 local;

void main() {
    vec2 q = abs(local - size * .5) - (size * .5 - vec2(6.));
    float distance = length(max(q, 0.)) + min(max(q.x, q.y), 0.) - 6.;
    if (distance > 0.) discard;

    vec3 color = vec3(0.);
    for (int x = -2; x <= 2; x++) {
        for (int y = -2; y <= 2; y++) {
            vec2 offset = vec2(float(x), float(y)) * 3.;
            color += texture2D(scene, (gl_FragCoord.xy + offset) / screen).rgb;
        }
    }
    color /= 25.;
    float shine = .025 * (1. - local.y / size.y);
    vec3 tint = vec3(.025, .032, .045);
    color = mix(color, tint, max(.35, opacity)) + shine;
    float edge = 1. - smoothstep(-1., 0., distance);
    gl_FragColor = vec4(color, edge);
}
