attribute highp vec4 myVertex;
uniform mediump mat4 myPMVMatrix;
void main(void)
{
    gl_Position = myPMVMatrix * myVertex;
}
