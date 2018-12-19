package sagex.miniclient.android.opengl;

public class TextureShader extends Shader {
    public int a_myUV;
    public int a_myVertex;
    public int u_myColor;
    public int u_sampler2d;
    public int u_myPMVMatrix;

    public TextureShader(String fragmentSource, String vertexSource) {
        super("texture", fragmentSource, vertexSource,
                new String[]{"myPMVMatrix", "myColor", "sampler2d"},
                new String[]{"myVertex", "myUV"});
    }

    @Override
    void load() {
        super.load();
        a_myUV = attribute("myUV");
        a_myVertex = attribute("myVertex");
        u_myColor = uniform("myColor");
        u_sampler2d = uniform("sampler2d");
        u_myPMVMatrix = uniform("myPMVMatrix");
    }
}
