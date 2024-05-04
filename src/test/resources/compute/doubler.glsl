// doubler.glsl
#version 430

layout(local_size_x = 256) in;

layout(std430, binding = 0) buffer inputData {
    float data[];
} In;

layout(std430, binding = 1) buffer outputData {
    float data[];
} Out;

void main() {
    uint index = gl_GlobalInvocationID.x;
    if (index < In.data.length()) {
        Out.data[index] = In.data[index] * 2.0;
    }
}
