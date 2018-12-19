package sagex.miniclient.android.opengl;

import android.opengl.GLES20;

import org.intellij.lang.annotations.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShaderUtils {
    private static Logger log = LoggerFactory.getLogger(ShaderUtils.class);

    public enum Shader {Base, Texture, Gradient}

    @Language("GLSL")
    static final String gradientFragmentShader2d = "    uniform mediump vec2 u_resolution;\n" +
            "    uniform mediump vec4 u_argbTL;\n" +
            "    uniform mediump vec4 u_argbTR;\n" +
            "    uniform mediump vec4 u_argbBL;\n" +
            "    uniform mediump vec4 u_argbBR;\n" +
            "\n" +
            "    void main(void)\n" +
            "    {\n" +
            "        mediump vec2 st = gl_FragCoord.xy/u_resolution.xy;\n" +
            "\n" +
            "        if (u_argbTR==u_argbTL) {\n" +
            "            // gradient y\n" +
            "            mediump vec3 colorY = mix(u_argbTL.rgb, u_argbBL.rgb, vec3(st.y));\n" +
            "            gl_FragColor = vec4(colorY,1.0);\n" +
            "        } else {\n" +
            "            mediump vec3 colorX = mix(u_argbTL.rgb, u_argbTR.rgb, vec3(st.x));\n" +
            "            gl_FragColor = vec4(colorX,1.0);\n" +
            "        }\n" +
            "\n" +
            "    }";

    @Language("GLSL")
    static final String gradientVertexShader2d = "    attribute highp vec4 myVertex;\n" +
            "    uniform mediump mat4 myPMVMatrix;\n" +
            "\n" +
            "    void main(void)\n" +
            "    {\n" +
            "        gl_Position = myPMVMatrix * myVertex;\n" +
            "    }\n";

    @Language("GLSL")
    static final String fragmentShader2d = ""+
            "    uniform mediump vec4 myColor;\n" +
            "    void main(void)\n" +
            "    {\n" +
            "        gl_FragColor = myColor;\n" +
            "    }\n";

    @Language("GLSL")
    static final String vertexShader2d = ""+
            "    attribute vec4 myVertex;\n" +
            "    uniform mat4 myPMVMatrix;\n" +
            "    void main(void)\n" +
            "    {\n" +
            "        gl_Position = myPMVMatrix * myVertex;\n" +
            "    }\n";

    @Language("GLSL")
    static final String textureFragmentShader2d = ""+
            "precision mediump float;\n"+
            "uniform sampler2D uTexture;\n" +
            "varying vec2 vTexPos;\n" +
            "void main(void)\n" +
            "{\n" +
            "  gl_FragColor = texture2D(uTexture, vTexPos);\n" +
            "}";

    @Language("GLSL")
    static final String textureVertexShader2d = ""+
            "uniform mat4 uScreen;\n" +
            "attribute vec2 aPosition;\n" +
            "attribute vec2 aTexPos;\n" +
            "varying vec2 vTexPos;\n" +
            "void main() {\n" +
            "  vTexPos = aTexPos;\n" +
            "  gl_Position = uScreen * vec4(aPosition.xy, 0.0, 1.0);\n" +
            "}";

    static boolean initialized = false;

    static final String[] ALL_SHADERS = {gradientFragmentShader2d, gradientVertexShader2d, fragmentShader2d, vertexShader2d, textureFragmentShader2d, textureVertexShader2d};

    public static int GRADIENT_FRAGMENT_SHADER = -1;
    public static int GRADIENT_VERTEX_SHADER = -1;
    public static int TEXTURE_FRAGMENT_SHADER = -1;
    public static int TEXTURE_VERTEX_SHADER = -1;
    public static int FRAGMENT_SHADER = -1;
    public static int VERTEX_SHADER = -1;

    public static int BASE_PROGRAM = -1;
    public static int BASE_PROGRAM_PMVMatrix_Location = -1;
    public static int BASE_PROGRAM_MYVERTEX_Handle;
    public static int BASE_PROGRAM_MYCOLOR_Handle;

    public static int TEXTURE_PROGRAM = -1;
    public static int TEXTURE_PROGRAM_uSCREEN =-1;
    public static int TEXTURE_PROGRAM_uTEXTURE =-1;
    public static int TEXTURE_PROGRAM_aPOSITION =-1;
    public static int TEXTURE_PROGRAM_aTEXPOS =-1;
    public static int TEXTURE_PROGRAM_uColor = -1;

    public static int GRADIENT_PROGRAM = -1;
    public static int GRADIENT_PROGRAM_PMVMatrix_Location;
    public static int GRADIENT_PROGRAM_resolution;
    public static int GRADIENT_PROGRAM_argb_tl;
    public static int GRADIENT_PROGRAM_argb_tr;
    public static int GRADIENT_PROGRAM_argb_bl;
    public static int GRADIENT_PROGRAM_argb_br;

    public static Shader CURRENT_SHADER = null;
    public static int CURRENT_USCREEN_ID = -1;
    //public static float[] currentMatrix = new float[16];

    static int compileShader(final int shaderType,
                                    String shaderSource)
    {
        log.debug("compiling shader....");
        log.debug("Loading Shader: " + shaderType + "; " + shaderSource.hashCode());
        int shaderHandle=GLES20.glCreateShader(shaderType);
        if (shaderHandle !=0)
        {
            GLES20.glShaderSource(shaderHandle, shaderSource);
            GLES20.glCompileShader(shaderHandle);

            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
            if (compileStatus[0] == GLES20.GL_FALSE)
            {
                log.error("Compile Shader Failed; {}", shaderSource);
                new Exception(GLES20.glGetShaderInfoLog(shaderHandle)).printStackTrace(System.err);
                GLES20.glDeleteShader(shaderHandle);
                shaderHandle = 0;
            }
        }
        return shaderHandle;
    }

    static int createProgram(final int vtxShader, final int pxlShader)
    {
        int programHandle=GLES20.glCreateProgram();
        if (programHandle!=0)
        {
            GLES20.glAttachShader(programHandle, vtxShader);
            GLES20.glAttachShader(programHandle, pxlShader);
            GLES20.glLinkProgram(programHandle);

            //get linking status
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0]==0)
            {
                System.err.println("Link Shader Failed; " + GLES20.glGetProgramInfoLog(programHandle));
                GLES20.glDeleteProgram(programHandle);
                programHandle = 0;
            }
        }
        return programHandle;
    }

    private static void loadShaders() {
        GRADIENT_FRAGMENT_SHADER = compileShader(GLES20.GL_FRAGMENT_SHADER, gradientFragmentShader2d);
        TEXTURE_FRAGMENT_SHADER = compileShader(GLES20.GL_FRAGMENT_SHADER, textureFragmentShader2d);
        FRAGMENT_SHADER = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader2d);

        GRADIENT_VERTEX_SHADER = compileShader(GLES20.GL_VERTEX_SHADER, gradientVertexShader2d);
        TEXTURE_VERTEX_SHADER = compileShader(GLES20.GL_VERTEX_SHADER, textureVertexShader2d);
        VERTEX_SHADER = compileShader(GLES20.GL_VERTEX_SHADER, vertexShader2d);
    }

    public static void createPrograms() {
        if (initialized) return;
        initialized=true;

        loadShaders();
        BASE_PROGRAM = createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        BASE_PROGRAM_PMVMatrix_Location =GLES20.glGetUniformLocation(BASE_PROGRAM, "myPMVMatrix");
        BASE_PROGRAM_MYVERTEX_Handle = GLES20.glGetAttribLocation(BASE_PROGRAM, "myVertex");
        BASE_PROGRAM_MYCOLOR_Handle = GLES20.glGetUniformLocation(BASE_PROGRAM, "myColor");

        TEXTURE_PROGRAM = createProgram(TEXTURE_VERTEX_SHADER, TEXTURE_FRAGMENT_SHADER);
        TEXTURE_PROGRAM_uSCREEN =GLES20.glGetUniformLocation(TEXTURE_PROGRAM, "uScreen");
        TEXTURE_PROGRAM_aPOSITION = GLES20.glGetAttribLocation(TEXTURE_PROGRAM, "aPosition");
        TEXTURE_PROGRAM_aTEXPOS = GLES20.glGetAttribLocation(TEXTURE_PROGRAM, "aTexPos");
        TEXTURE_PROGRAM_uTEXTURE =GLES20.glGetUniformLocation(TEXTURE_PROGRAM, "uTexture");
        TEXTURE_PROGRAM_uColor=GLES20.glGetUniformLocation(TEXTURE_PROGRAM, "uColor");

        GRADIENT_PROGRAM = createProgram(GRADIENT_VERTEX_SHADER, GRADIENT_FRAGMENT_SHADER);
        GRADIENT_PROGRAM_PMVMatrix_Location=GLES20.glGetUniformLocation(GRADIENT_PROGRAM, "myPMVMatrix");
        GRADIENT_PROGRAM_resolution=GLES20.glGetUniformLocation(GRADIENT_PROGRAM, "u_resolution");
        GRADIENT_PROGRAM_argb_tl=GLES20.glGetUniformLocation(GRADIENT_PROGRAM, "u_argbTL");
        GRADIENT_PROGRAM_argb_tr=GLES20.glGetUniformLocation(GRADIENT_PROGRAM, "u_argbTR");
        GRADIENT_PROGRAM_argb_bl=GLES20.glGetUniformLocation(GRADIENT_PROGRAM, "u_argbBL");
        GRADIENT_PROGRAM_argb_br=GLES20.glGetUniformLocation(GRADIENT_PROGRAM, "u_argbBR");
    }

    public static int useProgram(Shader shader) {
        if (shader != CURRENT_SHADER) {
            switch (shader) {
                case Base:
                    GLES20.glUseProgram(BASE_PROGRAM);
                    CURRENT_USCREEN_ID = BASE_PROGRAM_PMVMatrix_Location;
                    log.debug("GL USE PROGRAM {} [{}] {}", shader, BASE_PROGRAM, CURRENT_USCREEN_ID);
                    break;
                case Texture:
                    GLES20.glUseProgram(TEXTURE_PROGRAM);
                    CURRENT_USCREEN_ID = TEXTURE_PROGRAM_uSCREEN;
                    log.debug("GL USE PROGRAM {} [{}] {}", shader, TEXTURE_PROGRAM, CURRENT_USCREEN_ID);
                    break;
                case Gradient:
                    GLES20.glUseProgram(GRADIENT_PROGRAM);
                    CURRENT_USCREEN_ID = GRADIENT_PROGRAM_PMVMatrix_Location;
                    log.debug("GL USE PROGRAM {} [{}] {}", shader, GRADIENT_PROGRAM, CURRENT_USCREEN_ID);
                    break;
            }
            CURRENT_SHADER = shader;
        }

        return CURRENT_USCREEN_ID;
    }

//    public static void setShaderParams(Shader shader, OpenGLSurface surface) {
//        GLES20.glUniformMatrix4fv( useProgram(shader), 1, false, surface.viewMatrix, 0);
//    }

//    public static void setShaderParams(Shader shader, float w, float h) {
//        float vm[] = new float[16];
//        Matrix.orthoM(vm, 0,0, w, h, 0, 0, 1);
//        GLES20.glUniformMatrix4fv( useProgram(shader), 1, false, vm, 0);
//    }

    public static float[] rgbaToFloatArray(int red, int green, int blue, int alpha) {
        float r = red & 0xFF;
        float g = green & 0xFF;
        float b = blue & 0xFF;
        float a = alpha & 0xFF;

        return new float[] {r/255.0f,g/255.0f,b/255.0f,a/255.0f};
    }

    public static int RGBA_to_ARGB(int red, int green, int blue, int alpha) {
        return ((alpha&0xFF)<<24)|((red&0xFF)<<16)|
                ((green&0xFF)<<8)|((blue&0xFF));
    }


    public static float[] argbToFloatArray(int argb) {
        float r = ((argb>>16) & 0xff);
        float g = ((argb>>8) & 0xff);
        float b = ((argb) & 0xff);
        float a = ((argb>>24) & 0xff);

        return new float[] {r/255.0f,g/255.0f,b/255.0f,a/255.0f};
    }

    public static int[] glCreateBuffer() {
        int buffers[] = new int[1];
        GLES20.glGenBuffers(1, buffers, 0);
        return buffers;
    }
}
