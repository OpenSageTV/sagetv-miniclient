package sagex.miniclient.android.opengl.shaders;

public class DefaultShader extends Shader {
    public int a_myVertex;
    public int a_myColor;
    public int u_myPMVMatrix;

    public DefaultShader(String fragmentSource, String vertexSource) {
        super("default", fragmentSource, vertexSource,
                new String[]{"myPMVMatrix"},
                new String[]{"myVertex", "myColor"});
    }

    @Override
    public void load() {
        super.load();
        a_myVertex = attribute("myVertex");
        a_myColor = attribute("myColor");
        u_myPMVMatrix = uniform("myPMVMatrix");
    }
}
