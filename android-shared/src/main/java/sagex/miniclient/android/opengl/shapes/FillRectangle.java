package sagex.miniclient.android.opengl.shapes;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import sagex.miniclient.android.opengl.OpenGLSurface;
import sagex.miniclient.android.opengl.OpenGLUtils;

public class FillRectangle {

    private FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;

    private short drawOrder[] = {0, 1, 2, 0, 2, 3}; // order to draw vertices

    static float squareCoords[] = {
            0, 0, 0,   // top left
            0, 0, 0,   // bottom left
            0, 0, 0,   // bottom right
            0, 0, 0}; // top right

    static private final int vertexCount = squareCoords.length / COORDS_PER_VERTEX;
    static private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex


    public FillRectangle() {
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);
    }

    public void draw(int x1, int y1, int width, int height, int argbTL, int argbTR, int argbBR, int argbBL, OpenGLSurface surface) {
        // Add program to OpenGL ES environment
        OpenGLUtils.useProgram(OpenGLUtils.gradientShader);

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(OpenGLUtils.gradientShader.a_myVertex);

        // top/left
        squareCoords[0] = x1;
        squareCoords[1] = y1;
        squareCoords[2] = 0;

        // bottom/left
        squareCoords[3] = x1;
        squareCoords[4] = y1 + height;
        squareCoords[5] = 0;

        // bottom/right
        squareCoords[6] = x1 + width;
        squareCoords[7] = y1 + height;
        squareCoords[8] = 0;

        // top/right
        squareCoords[9] = x1 + width;
        squareCoords[10] = y1;
        squareCoords[11] = 0;

        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(OpenGLUtils.gradientShader.a_myVertex, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // Set colors
        GLES20.glUniform4fv(OpenGLUtils.gradientShader.u_argbTL, 1, OpenGLUtils.argbToFloatArray(argbTL), 0);
        GLES20.glUniform4fv(OpenGLUtils.gradientShader.u_argbTR, 1, OpenGLUtils.argbToFloatArray(argbTR), 0);
        GLES20.glUniform4fv(OpenGLUtils.gradientShader.u_argbBL, 1, OpenGLUtils.argbToFloatArray(argbBL), 0);
        GLES20.glUniform4fv(OpenGLUtils.gradientShader.u_argbBR, 1, OpenGLUtils.argbToFloatArray(argbBR), 0);

        // set resolution for gradients
        //GLES20.glUniform2fv(OpenGLUtils.gradientShader.u_resolution, 1, new float[]{width, height}, 0);
        // not sure why we need to use the surface instead of our h and w, but, using out h,w appears to render black and white
        GLES20.glUniform2fv(OpenGLUtils.gradientShader.u_resolution, 1, new float[]{surface.width, surface.height}, 0);

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(OpenGLUtils.gradientShader.u_myPMVMatrix, 1, false, surface.viewMatrix, 0);

        // Draw the triangle without drawlist buffer
        // GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, vertexCount);

        // Draw the triangles with a draw list buffer
        // Draw the square
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(OpenGLUtils.gradientShader.a_myVertex);
    }
}