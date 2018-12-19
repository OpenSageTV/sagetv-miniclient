package sagex.miniclient.android.opengl;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

import sagex.miniclient.util.IOUtil;

public class ShaderUtils {
    private static Logger log = LoggerFactory.getLogger(ShaderUtils.class);

    static boolean initialized = false;

    public static Shader CURRENT_SHADER = null;

    public static DefaultShader defaultShader = null;
    public static TextureShader textureShader = null;

    public static void loadShaders(Context context) throws IOException {
        if (!initialized) {
            initialized = true;
            defaultShader = new DefaultShader(loadShaderSource(context, "default-fragment"), loadShaderSource(context, "default-vertex"));
            defaultShader.load();
            textureShader = new TextureShader(loadShaderSource(context, "texture-fragment"), loadShaderSource(context, "texture-vertex"));
            textureShader.load();
        }
    }

    private static String loadShaderSource(Context context, String basename) throws IOException {
        try (InputStream is = context.getAssets().open("shaders/" + basename + ".glsl")) {
            return IOUtil.toString(is);
        }
    }


    public static int useProgram(Shader shader) {
        if (shader != CURRENT_SHADER) {
            CURRENT_SHADER = shader;
            log.debug("Using Shader Program: {} - name {}", shader.program(), shader.name());
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


    public static float[] argbToFloatArray(int argb) {
        float r = ((argb>>16) & 0xff);
        float g = ((argb>>8) & 0xff);
        float b = ((argb) & 0xff);
        float a = ((argb>>24) & 0xff);

        return new float[] {r/255.0f,g/255.0f,b/255.0f,a/255.0f};
    }

    public static void logGLErrors(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            log.error("{}: glError: {}; {}", op, error, GLUtils.getEGLErrorString(error));
        }
    }
}
