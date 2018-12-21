package sagex.miniclient.android.opengl.shapes;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import sagex.miniclient.android.opengl.OpenGLSurface;
import sagex.miniclient.android.opengl.OpenGLUtils;

public class Triangle {
    /**
     * TUTORIALS:
     * https://medium.com/@xzan/opengl-a-noobs-guide-for-android-developers-5eed724e07ad
     * https://bitbucket.org/Xzan/opengl-example/src/master/app/src/main/java/be/appkers/example/opengl/MainActivity.java?fileviewer=file-view-default#MainActivity.java-63
     * <p>
     * https://code.tutsplus.com/tutorials/how-to-use-opengl-es-in-android-apps--cms-28464
     * <p>
     * http://www.learnopengles.com/android-lesson-one-getting-started/
     * <p>
     * http://androidblog.reindustries.com/a-real-open-gl-es-2-0-2d-tutorial-part-1/
     * <p>
     * https://gamedev.stackexchange.com/questions/33656/opengl-es-and-screen-coordinates
     */


    static private FloatBuffer vertexBuffer;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 2;
    static private final int vertexCount = /*triangleCoords.length*/ 6 / COORDS_PER_VERTEX;
    static private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    public Triangle() {
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                6 * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();
    }

    public void draw(int topX, int topY, int leftX, int leftY, int rightX, int rightY, int color, OpenGLSurface surface) {
        OpenGLUtils.useProgram(OpenGLUtils.defaultShader);

        // add the coordinates to the FloatBuffer
        vertexBuffer.put(new float[]{
                topX, topY,
                leftX, leftY,
                rightX, rightY
        });
        vertexBuffer.position(0);

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(OpenGLUtils.defaultShader.a_myVertex);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(OpenGLUtils.defaultShader.a_myVertex, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // Set color for drawing the triangle
        // GLES20.glVertexAttrib4fv(ShaderUtils.BASE_PROGRAM_MYCOLOR_Handle, color, 0);
        GLES20.glUniform4fv(OpenGLUtils.defaultShader.u_myColor, 1, OpenGLUtils.argbToFloatArray(color), 0);

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(OpenGLUtils.defaultShader.u_myPMVMatrix, 1, false, surface.viewMatrix, 0);

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(OpenGLUtils.defaultShader.a_myVertex);
        OpenGLUtils.logGLErrors("Triangle");
    }
}