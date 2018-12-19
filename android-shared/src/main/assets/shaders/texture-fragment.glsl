precision mediump float;
uniform sampler2D sampler2d;
varying mediump vec2 myTexCoord;
varying mediump vec4 myColorOut;
void main(void)
{
   gl_FragColor = myColorOut * texture2D(sampler2d,myTexCoord);
//    // NOTE: webgl images use unmultipled alpha
//    // https://stackoverflow.com/questions/39341564/webgl-how-to-correctly-blend-alpha-channel-png/
//    // we could do it here, but, we'll do it when we load the texture
//    //gl_FragColor.rgb *= gl_FragColor.a;
}
