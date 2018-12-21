uniform mediump vec4 u_argbTL;
uniform mediump vec4 u_argbTR;
uniform mediump vec4 u_argbBL;
uniform mediump vec4 u_argbBR;
uniform mediump vec2 u_resolution;

void main(void)
{
    mediump vec2 st = gl_FragCoord.xy/u_resolution.xy;

//    if ignoring alpha channel
//    if (u_argbTR==u_argbTL) {
//        mediump vec3 colorY = mix(u_argbTL.rgb, u_argbBL.rgb, vec3(st.y));
//        gl_FragColor = vec4(colorY,1.0);
//    } else {
//        mediump vec3 colorX = mix(u_argbTL.rgb, u_argbTR.rgb, vec3(st.x));
//        gl_FragColor = vec4(colorX,1.0);
//    }

    if (u_argbTL == u_argbTR && u_argbBL == u_argbBR) {
        gl_FragColor = mix(u_argbTL, u_argbBL, st.y);
    } else if(u_argbTL == u_argbBL && u_argbTR == u_argbBR) {
        gl_FragColor = mix(u_argbTL, u_argbTR, st.x);
    } else {
        gl_FragColor = u_argbTL;
    }
}
