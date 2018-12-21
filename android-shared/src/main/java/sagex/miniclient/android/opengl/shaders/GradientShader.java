package sagex.miniclient.android.opengl.shaders;

public class GradientShader extends Shader {
    public int a_myVertex;
    public int u_resolution;
    public int u_myPMVMatrix;
    public int u_argbTL;
    public int u_argbTR;
    public int u_argbBL;
    public int u_argbBR;

    public GradientShader(String fragmentSource, String vertexSource) {
        super("gradient", fragmentSource, vertexSource,
                new String[]{"myPMVMatrix", "u_argbTL", "u_argbTR", "u_argbBL", "u_argbBR", "u_resolution"},
                new String[]{"myVertex"});
    }

    @Override
    public void load() {
        super.load();
        a_myVertex = attribute("myVertex");
        u_myPMVMatrix = uniform("myPMVMatrix");
        u_resolution = uniform("u_resolution");
        u_argbTL = uniform("u_argbTL");
        u_argbTR = uniform("u_argbTR");
        u_argbBL = uniform("u_argbBL");
        u_argbBR = uniform("u_argbBR");
    }
}
