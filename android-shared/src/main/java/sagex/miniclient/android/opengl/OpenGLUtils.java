package sagex.miniclient.android.opengl;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;

import sagex.miniclient.android.opengl.shaders.DefaultShader;
import sagex.miniclient.android.opengl.shaders.Shader;
import sagex.miniclient.android.opengl.shaders.TextureShader;
import sagex.miniclient.util.IOUtil;
import sagex.miniclient.util.VerboseLogging;

public class OpenGLUtils {
    private static Logger log = LoggerFactory.getLogger(OpenGLUtils.class);

    //static boolean initialized = false;

    public static Shader CURRENT_SHADER = null;

    public static DefaultShader defaultShader = null;
    public static TextureShader textureShader = null;

    public static void loadShaders(Context context) throws IOException {
            defaultShader = new DefaultShader(loadShaderSource(context, "default-fragment"), loadShaderSource(context, "default-vertex"));
            defaultShader.load();
            textureShader = new TextureShader(loadShaderSource(context, "texture-fragment"), loadShaderSource(context, "texture-vertex"));
            textureShader.load();
    }

    private static String loadShaderSource(Context context, String basename) throws IOException {
        try (InputStream is = context.getAssets().open("shaders/" + basename + ".glsl")) {
            return IOUtil.toString(is);
        }
    }


    public static int useProgram(Shader shader) {
        if (shader != CURRENT_SHADER) {
            CURRENT_SHADER = shader;
            // log.debug("Using Shader Program: {} - name {}", shader.program(), shader.name());
            shader.use();
        }

        return shader.program();
    }

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

    public static int rgba8888(int r, int g, int b, int a) {
        return ((r & 0xff) << 24) | ((g & 0xff) << 16) | ((b & 0xFF) << 8) | (a & 0xff);
    }

    public static int flipRGBA(int argb) {
        int r = ((argb >> 16) & 0xff);
        int g = ((argb >> 8) & 0xff);
        int b = ((argb) & 0xff);
        int a = ((argb >> 24) & 0xff);

        return rgba8888(r, g, b, a);


//        return  (argb << 8) | (argb >> 24);

        //return ((argb >> 16) & 0xFF) | ((argb >> 8) & 0xFF) | ((argb) & 0xFF) | ((argb >> 24) & 0xFF);
    }

    public static String hexColor(int argb) {
        int r = ((argb >> 16) & 0xff);
        int g = ((argb >> 8) & 0xff);
        int b = ((argb) & 0xff);
        int a = ((argb >> 24) & 0xff);

        return String.format("%2x%2x%2x.%2x", r, g, b, a);
    }


    public static void putToFloatBuffer(int argb, FloatBuffer buffer) {
        float r = ((argb >> 16) & 0xff);
        float g = ((argb >> 8) & 0xff);
        float b = ((argb) & 0xff);
        float a = ((argb >> 24) & 0xff);

        buffer.put(r / 255f);
        buffer.put(g / 255f);
        buffer.put(b / 255f);
        buffer.put(a / 255f);
    }


    public static float[] argbToFloatArray(int argb) {
        float r = ((argb>>16) & 0xff);
        float g = ((argb>>8) & 0xff);
        float b = ((argb) & 0xff);
        float a = ((argb>>24) & 0xff);

        return new float[] {r/255.0f,g/255.0f,b/255.0f,a/255.0f};
    }

    public static void logGLErrors(String op) {
        if (VerboseLogging.LOG_GL_ERRORS) {
            int error;
            while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
                log.error("{}: glError: {}; {}", op, error, GLUtils.getEGLErrorString(error));
            }
        }
    }

}
