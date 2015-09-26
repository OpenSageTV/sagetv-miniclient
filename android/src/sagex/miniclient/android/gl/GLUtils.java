package sagex.miniclient.android.gl;

import android.opengl.GLES20;

/**
 * Created by seans on 24/09/15.
 */
public class GLUtils {
    // --------- BEGIN GL Stuff
    // http://androidblog.reindustries.com/a-real-opengl-es-2-0-2d-tutorial-part-2/
    public static class TextureShaderUtil {

        // Program variables
        public static int sp_SolidColor;
        public static int sp_Image;

        /* SHADER Image
         *
         * This shader is for rendering 2D images straight from a texture
         * No additional effects.
         *
         */
        public static final String vs_Image =
                "uniform mat4 uMVPMatrix;" +
                        "attribute vec4 vPosition;" +
                        "attribute vec4 a_Color;" +
                        "attribute vec2 a_texCoord;" +
                        "varying vec4 v_Color;" +
                        "varying vec2 v_texCoord;" +
                        "void main() {" +
                        "  gl_Position = uMVPMatrix * vPosition;" +
                        "  v_texCoord = a_texCoord;" +
                        "  v_Color = a_Color;" +
                        "}";
        public static final String fs_Image =
                "precision mediump float;" +
                        "varying vec2 v_texCoord;" +
                        "varying vec4 v_Color;" +
                        "uniform sampler2D s_texture;" +
                        "void main() {" +
                        "  gl_FragColor = texture2D( s_texture, v_texCoord ) * v_Color;" +
                        "  gl_FragColor.rgb *= v_Color.a;" +
                        "}";
    }


    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        // return the shader
        return shader;
    }

}
