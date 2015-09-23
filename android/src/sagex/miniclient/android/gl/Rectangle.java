package sagex.miniclient.android.gl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

public class Rectangle {



    private short[] indices = {0,1,2,0,2,3};

    private FloatBuffer vertexBuffer;
    private ShortBuffer indexBuffer;

    public Rectangle(int x,int y,int w,int h, int sw, int sh) {
        float vertices[]={
                X(x,sw), Y(y,sh), 0.0f,
                X(x,sw),Y(y + h, sh),0.0f,
                X(x + w, sw),Y(y + h, sh),0.0f,
                X(x+w, sw),Y(y,sh),0.0f
        };
        ByteBuffer vbb  = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        vertexBuffer = vbb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        ByteBuffer ibb = ByteBuffer.allocateDirect(indices.length * 2);
        ibb.order(ByteOrder.nativeOrder());
        indexBuffer = ibb.asShortBuffer();
        indexBuffer.put(indices);
        indexBuffer.position(0);
    }

    private float X(float x, float sw) {
        return -1f+(x/sw);
    }

    private float Y(float y, float sh) {
        return -1f+((sh-y)/sh);
    }
    Random random = new Random();
    public void draw(GL10 gl){
        gl.glColor4f(random.nextFloat(), 0.5f, 1.0f, 1.0f);
        gl.glFrontFace(GL10.GL_CCW);
        gl.glEnable(GL10.GL_CULL_FACE);
        gl.glCullFace(GL10.GL_BACK);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
        gl.glDrawElements(GL10.GL_TRIANGLES, indices.length, GL10.GL_UNSIGNED_SHORT, indexBuffer);
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisable(GL10.GL_CULL_FACE);
    }

}