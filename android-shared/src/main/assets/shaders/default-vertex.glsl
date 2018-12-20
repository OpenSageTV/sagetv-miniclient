attribute vec4 myVertex;
uniform mat4 myPMVMatrix;
void main(void)
{
    gl_Position = myPMVMatrix * myVertex;
    //gl_Position = myVertex;
}
