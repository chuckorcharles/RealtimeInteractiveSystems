// ------------------ Vertex Shader --------------------------------
#version 120
#extension GL_EXT_gpu_shader4 : enable
void main(void)
{
    gl_Position =  ftransform();
}