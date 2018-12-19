attribute highp vec4 myVertex;
attribute mediump vec4 myUV;
uniform vec4 myColor;
uniform mediump mat4 myPMVMatrix;
varying mediump vec2 myTexCoord;
varying mediump vec4 myColorOut;
void main(void)
{
    gl_Position = myPMVMatrix * myVertex;
    myTexCoord = myUV.st;
    myColorOut = myColor;
}