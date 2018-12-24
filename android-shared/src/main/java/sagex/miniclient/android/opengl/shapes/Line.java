package sagex.miniclient.android.opengl.shapes;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import sagex.miniclient.android.opengl.OpenGLSurface;
import sagex.miniclient.android.opengl.OpenGLUtils;

public class Line {

    private FloatBuffer vertexBuffer;
    private static ShortBuffer drawListBuffer;
    private FloatBuffer colorBuffer;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 2;

    private static short drawOrder[] = {0, 1}; // order to draw vertices

    static {
        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

    }

    float lineCoords[] = {
            0, 0,   // top left
            0, 0,   // bottom right
    }; // top right

    public Line() {
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                lineCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(lineCoords);
        vertexBuffer.position(0);

        // note we use normalized float values because using
        // normalized compact rgba ints caused color issues
        colorBuffer = ByteBuffer.allocateDirect(lineCoords.length * 4 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        colorBuffer.position(0);
    }

    public void draw(int x1, int y1, int x2, int y2, int argbTL, int argbTR, int thickness, OpenGLSurface surface) {
        // Add program to OpenGL ES environment
        OpenGLUtils.useProgram(OpenGLUtils.defaultShader);

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(OpenGLUtils.defaultShader.a_myVertex);
        GLES20.glEnableVertexAttribArray(OpenGLUtils.defaultShader.a_myColor);

        // top/left
        lineCoords[0] = x1;
        lineCoords[1] = y1;

        // bottom/left
        lineCoords[2] = x2;
        lineCoords[3] = y2;

        vertexBuffer.put(lineCoords);
        vertexBuffer.position(0);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(OpenGLUtils.defaultShader.a_myVertex, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                0, vertexBuffer);

        // set the colors per vertex
        colorBuffer.position(0);
        OpenGLUtils.putToFloatBuffer(argbTL, colorBuffer);
        OpenGLUtils.putToFloatBuffer(argbTR, colorBuffer);
        colorBuffer.position(0);
        GLES20.glVertexAttribPointer(
                OpenGLUtils.defaultShader.a_myColor, 4,
                GLES20.GL_FLOAT, true, 0, colorBuffer);

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(OpenGLUtils.defaultShader.u_myPMVMatrix, 1, false, surface.viewMatrix, 0);

        GLES20.glLineWidth(thickness);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        GLES20.glDrawElements(GLES20.GL_LINES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        GLES20.glDisable(GLES20.GL_BLEND);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(OpenGLUtils.defaultShader.a_myVertex);
        GLES20.glDisableVertexAttribArray(OpenGLUtils.defaultShader.a_myColor);

        GLES20.glLineWidth(1);
    }
}