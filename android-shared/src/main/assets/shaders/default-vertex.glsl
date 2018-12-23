attribute vec4 myVertex;
attribute mediump vec4 myColor;
uniform mat4 myPMVMatrix;
varying mediump vec4 myColorOut;
void main(void)
{
    gl_Position = myPMVMatrix * myVertex;
    myColorOut = myColor;
}
