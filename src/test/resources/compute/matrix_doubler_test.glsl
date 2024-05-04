// doubler.glsl
#version 430

layout(local_size_x = 1, local_size_y = 1) in;

layout(std430, binding = 0) buffer inputData {
    mat3 data;
} In;

layout(std430, binding = 1) buffer outputData {
    mat3 data;
} Out;

void main() {
    uint col = gl_GlobalInvocationID.x;
    uint row = gl_GlobalInvocationID.y;
    Out.data[col][row] = In.data[col][row] * 2.0;
    //In.data[col][row] = 1.0;
}
