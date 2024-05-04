#version 430

layout(local_size_x = 8, local_size_y = 8, local_size_z = 1) in;

layout(rgba32f, binding = 0) uniform writeonly image2D Result;
layout(std140, binding = 1) uniform Params {
    mat4 cameraToWorld;
    mat4 cameraInverseProjection;
    sampler2D gradient;
    float tintStrength;
    int starCount;
} params;

struct Star {
    vec3 position;
    float time;
    float brightness;
    float radius;
};

layout(std430, binding = 2) buffer StarBuffer {
    Star stars[];
} starData;

#define TAU 6.28318530718
#define PI 3.14159265359

vec3 CreateRay(vec3 origin, vec3 direction) {
    return direction;
}

struct TraceResult {
    bool collides;
    float distance;
};

TraceResult IntersectSphere(vec3 rayOrigin, vec3 rayDirection, vec3 sphereCenter, float sphereRadius) {
    vec3 oc = rayOrigin - sphereCenter;
    float b = dot(oc, rayDirection);
    float c = dot(oc, oc) - sphereRadius * sphereRadius;
    float h = b * b - c;
    if (h < 0.0) {
        return TraceResult(false, 0.0);
    }
    h = sqrt(h);
    return TraceResult(true, -b - h);
}

vec4 encodeColor(vec3 color) {
    return vec4(color, 0.0);
}

void main() {
    ivec2 pixelCoords = ivec2(gl_GlobalInvocationID.xy);
    ivec2 imageSize = imageSize(Result);

    float longitude = float(pixelCoords.x) / imageSize.x;
    float latitude = float(pixelCoords.y) / imageSize.y;
    float theta = TAU * longitude;
    float phi = PI * latitude;

    vec3 direction = normalize(vec3(cos(theta) * sin(phi), sin(theta) * sin(phi), cos(phi)));
    vec3 origin = vec3(0.0);
    vec3 ray = CreateRay(origin, direction);

    vec3 color = vec3(0.0);
    for (int i = 0; i < params.starCount; i++) {
        Star star = starData.stars[i];
        TraceResult result = IntersectSphere(ray, star.position, star.radius);

        if (result.collides) {
            vec3 temp = texture(params.gradient, vec2(star.time, 0.5)).rgb;
            color = mix(vec3(1.0), temp, params.tintStrength) * star.brightness;
            break;
        }
    }

    imageStore(Result, pixelCoords, encodeColor(color));
}
