package sagex.miniclient.android.opengl;

public class DefaultShader extends Shader {
    public int a_myVertex;
    public int u_myColor;
    public int u_myPMVMatrix;

    public DefaultShader(String fragmentSource, String vertexSource) {
        super("default", fragmentSource, vertexSource,
                new String[]{"myPMVMatrix", "myColor"},
                new String[]{"myVertex"});
    }

    @Override
    void load() {
        super.load();
        a_myVertex = attribute("myVertex");
        u_myColor = uniform("myColor");
        u_myPMVMatrix = uniform("myPMVMatrix");
    }
}
