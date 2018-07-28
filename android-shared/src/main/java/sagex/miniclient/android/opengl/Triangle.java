package sagex.miniclient.android.opengl;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Triangle {
    /**
     * TUTORIALS:
     * https://medium.com/@xzan/opengl-a-noobs-guide-for-android-developers-5eed724e07ad
     * https://bitbucket.org/Xzan/opengl-example/src/master/app/src/main/java/be/appkers/example/opengl/MainActivity.java?fileviewer=file-view-default#MainActivity.java-63
     *
     * https://code.tutsplus.com/tutorials/how-to-use-opengl-es-in-android-apps--cms-28464
     *
     * http://www.learnopengles.com/android-lesson-one-getting-started/
     *
     * http://androidblog.reindustries.com/a-real-open-gl-es-2-0-2d-tutorial-part-1/
     *
     * https://gamedev.stackexchange.com/questions/33656/opengl-es-and-screen-coordinates
     *
     */


    private FloatBuffer vertexBuffer;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    float triangleCoords[];

    private final int vertexCount = /*triangleCoords.length*/ 9 / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = ShaderUtils.argbToFloatArray(ShaderUtils.RGBA_to_ARGB(0,255, 0, 255));

    public Triangle(float[] triangleCoords) {
        this.triangleCoords = triangleCoords;
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                triangleCoords.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        vertexBuffer.put(triangleCoords);
        // set the buffer to read the first coordinate
        vertexBuffer.position(0);
    }

    public void draw(float viewMatrix[]) { // pass in the calculated transformation matrix
        //ShaderUtils.setShaderParams(ShaderUtils.Shader.Base, 1280, 720);
        ShaderUtils.useProgram(ShaderUtils.Shader.Base);

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(ShaderUtils.BASE_PROGRAM_MYVERTEX_Handle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(ShaderUtils.BASE_PROGRAM_MYVERTEX_Handle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // Set color for drawing the triangle
        // GLES20.glVertexAttrib4fv(ShaderUtils.BASE_PROGRAM_MYCOLOR_Handle, color, 0);
        GLES20.glUniform4fv(ShaderUtils.BASE_PROGRAM_MYCOLOR_Handle, 1, color, 0);

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(ShaderUtils.BASE_PROGRAM_PMVMatrix_Location, 1, false, viewMatrix, 0);

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(ShaderUtils.BASE_PROGRAM_MYVERTEX_Handle);
        System.out.println("Triangle Rendered");
    }
}