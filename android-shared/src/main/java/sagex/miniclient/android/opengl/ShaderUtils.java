package sagex.miniclient.android.opengl;

import android.opengl.GLES20;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShaderUtils {
    static final Logger log = LoggerFactory.getLogger(ShaderUtils.class);

    public enum Shader {Base, Texture, Gradient}

    public static final int VERTEX_ARRAY = 0;
    public static final int COLOR_ARRAY = 1;
    public static final int COORD_ARRAY = 2;

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

    static final String gradientVertexShader2d = "    attribute highp vec4 myVertex;\n" +
            "    uniform mediump mat4 myPMVMatrix;\n" +
            "\n" +
            "    void main(void)\n" +
            "    {\n" +
            "        gl_Position = myPMVMatrix * myVertex;\n" +
            "    }\n";

    static final String fragmentShader2d = "    varying mediump vec4 myColorOut;\n" +
            "    void main(void)\n" +
            "    {\n" +
            "        gl_FragColor = myColorOut;\n" +
            "    }\n";

    static final String vertexShader2d = "    attribute highp vec4 myVertex;\n" +
            "    attribute mediump vec4 myColor;\n" +
            "    uniform mediump mat4 myPMVMatrix;\n" +
            "    varying mediump vec4 myColorOut;\n" +
            "\n" +
            "    void main(void)\n" +
            "    {\n" +
            "        gl_Position = myPMVMatrix * myVertex;\n" +
            "        myColorOut = myColor;\n" +
            "    }\n";

    static final String textureFragmentShader2d = "    uniform sampler2D sampler2d;\n" +
            "    varying mediump vec2 myTexCoord;\n" +
            "    varying mediump vec4 myColorOut;\n" +
            "    void main (void)\n" +
            "    {\n" +
            "        gl_FragColor = myColorOut * texture2D(sampler2d,myTexCoord);\n" +
            "        // NOTE: webgl images use unmultipled alpha\n" +
            "        // https://stackoverflow.com/questions/39341564/webgl-how-to-correctly-blend-alpha-channel-png/\n" +
            "        // we could do it here, but, we'll do it when we load the texture\n" +
            "        //gl_FragColor.rgb *= gl_FragColor.a;\n" +
            "    }";

    static final String textureVertexShader2d = "    attribute highp vec4 myVertex;\n" +
            "    attribute mediump vec4 myUV;\n" +
            "    uniform vec4 myColor;\n" +
            "    uniform mediump mat4 myPMVMatrix;\n" +
            "    varying mediump vec2 myTexCoord;\n" +
            "    varying mediump vec4 myColorOut;\n" +
            "    void main(void)\n" +
            "    {\n" +
            "        gl_Position = myPMVMatrix * myVertex;\n" +
            "        myTexCoord = myUV.st;\n" +
            "        myColorOut = myColor;\n" +
            "    }\n";

    static final String[] ALL_SHADERS = {gradientFragmentShader2d, gradientVertexShader2d, fragmentShader2d, vertexShader2d, textureFragmentShader2d, textureVertexShader2d};

    public static int GRADIENT_FRAGMENT_SHADER = -1;
    public static int GRADIENT_VERTEX_SHADER = -1;
    public static int TEXTURE_FRAGMENT_SHADER = -1;
    public static int TEXTURE_VERTEX_SHADER = -1;
    public static int FRAGMENT_SHADER = -1;
    public static int VERTEX_SHADER = -1;

    public static int BASE_PROGRAM = -1;
    public static int BASE_PROGRAM_PMVMatrix_Location = -1;
    public static int TEXTURE_PROGRAM = -1;
    public static int TEXTURE_PROGRAM_PMVMatrix_Location =-1;
    public static int TEXTURE_PROGRAM_Color_Location = -1;

    public static int GRADIENT_PROGRAM = -1;
    public static int GRADIENT_PROGRAM_PMVMatrix_Location;
    public static int GRADIENT_PROGRAM_resolution;
    public static int GRADIENT_PROGRAM_argb_tl;
    public static int GRADIENT_PROGRAM_argb_tr;
    public static int GRADIENT_PROGRAM_argb_bl;
    public static int GRADIENT_PROGRAM_argb_br;

    public static Shader CURRENT_SHADER = null;

    static int compileShader(final int shaderType,
                                    String shaderSource)
    {
        log.debug("Loading Shader: " + shaderType);
        int shaderHandle=GLES20.glCreateShader(shaderType);
        if (shaderHandle !=0)
        {
            GLES20.glShaderSource(shaderHandle, shaderSource);
            GLES20.glCompileShader(shaderHandle);

            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
            if (compileStatus[0] == GLES20.GL_FALSE)
            {
                log.error("[{}]Error compiling shader: {}", shaderHandle, GLES20.glGetShaderInfoLog(shaderHandle), new Exception(GLES20.glGetShaderInfoLog(shaderHandle)));
                log.error("[{}]SHADER:\n{}\n", shaderHandle, shaderSource);
                GLES20.glDeleteShader(shaderHandle);
                shaderHandle = 0;
            }
        }
        return shaderHandle;
    }

    static int createProgram(final int vtxShader, final int pxlShader,
                                    final String[] attributes)
    {
        int programHandle=GLES20.glCreateProgram();
        if (programHandle!=0)
        {
            GLES20.glAttachShader(programHandle, vtxShader);
            GLES20.glAttachShader(programHandle, pxlShader);
            // Bind attributes
            if (attributes != null)
            {
                final int size = attributes.length;
                for (int i = 0; i < size; i++)
                {
                    GLES20.glBindAttribLocation(
                            programHandle, i,
                            attributes[i]);
                }
            }

            GLES20.glLinkProgram(programHandle);

            //get linking status
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0]==0)
            {
                log.error("Error linking program: " +
                        GLES20.glGetProgramInfoLog(programHandle));
                GLES20.glDeleteProgram(programHandle);
                programHandle = 0;
            }
        }
        return programHandle;
    }

    static void loadShaders() {
        GRADIENT_FRAGMENT_SHADER = compileShader(GLES20.GL_FRAGMENT_SHADER, gradientFragmentShader2d);
        TEXTURE_FRAGMENT_SHADER = compileShader(GLES20.GL_FRAGMENT_SHADER, textureFragmentShader2d);
        FRAGMENT_SHADER = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader2d);

        GRADIENT_VERTEX_SHADER = compileShader(GLES20.GL_VERTEX_SHADER, gradientVertexShader2d);
        TEXTURE_VERTEX_SHADER = compileShader(GLES20.GL_VERTEX_SHADER, textureVertexShader2d);
        VERTEX_SHADER = compileShader(GLES20.GL_VERTEX_SHADER, vertexShader2d);
    }

    public static void createPrograms() {
        loadShaders();
        BASE_PROGRAM = createProgram(VERTEX_SHADER, FRAGMENT_SHADER, new String[] {"myVertex", "myColor"});
        BASE_PROGRAM_PMVMatrix_Location =GLES20.glGetUniformLocation(BASE_PROGRAM, "myPMVMatrix");

        TEXTURE_PROGRAM = createProgram(TEXTURE_VERTEX_SHADER, TEXTURE_FRAGMENT_SHADER, new String[] {"myVertex", "myUV"});
        TEXTURE_PROGRAM_PMVMatrix_Location =GLES20.glGetUniformLocation(TEXTURE_PROGRAM, "myPMVMatrix");
        TEXTURE_PROGRAM_Color_Location=GLES20.glGetUniformLocation(TEXTURE_PROGRAM, "myColor");

        GRADIENT_PROGRAM = createProgram(GRADIENT_VERTEX_SHADER, GRADIENT_FRAGMENT_SHADER, new String[] {"myVertex"});
        GRADIENT_PROGRAM_PMVMatrix_Location=GLES20.glGetUniformLocation(GRADIENT_PROGRAM, "myPMVMatrix");
        GRADIENT_PROGRAM_resolution=GLES20.glGetUniformLocation(GRADIENT_PROGRAM, "u_resolution");
        GRADIENT_PROGRAM_argb_tl=GLES20.glGetUniformLocation(GRADIENT_PROGRAM, "u_argbTL");
        GRADIENT_PROGRAM_argb_tr=GLES20.glGetUniformLocation(GRADIENT_PROGRAM, "u_argbTR");
        GRADIENT_PROGRAM_argb_bl=GLES20.glGetUniformLocation(GRADIENT_PROGRAM, "u_argbBL");
        GRADIENT_PROGRAM_argb_br=GLES20.glGetUniformLocation(GRADIENT_PROGRAM, "u_argbBR");
    }

    public static int useProgram(Shader shader) {
        if (shader!=CURRENT_SHADER) {
            switch (shader) {
                case Base:
                    GLES20.glUseProgram(BASE_PROGRAM);
                    return BASE_PROGRAM_PMVMatrix_Location;
                case Texture:
                    GLES20.glUseProgram(TEXTURE_PROGRAM);
                    return TEXTURE_PROGRAM_PMVMatrix_Location;
                case Gradient:
                    GLES20.glUseProgram(GRADIENT_PROGRAM);
                    return GRADIENT_PROGRAM_PMVMatrix_Location;
            }
        }
        return -1;
    }

    public static void setShaderParams(Shader shader, OpenGLSurface surface) {
        GLES20.glUniformMatrix4fv( useProgram(shader), 1, false, surface.viewMatrix, 0);
    }

    public static float[] rgbToFloatArray(int red, int green, int blue, int alpha) {
        float r = red & 0xFF;
        float g = green & 0xFF;
        float b = blue & 0xFF;
        float a = alpha & 0xFF;

        return new float[] {r/255.0f,g/255.0f,b/255.0f,a/255.0f};
    }


    public static int[] glCreateBuffer() {
        int buffers[] = new int[1];
        GLES20.glGenBuffers(1, buffers, 0);
        return buffers;
    }
}
