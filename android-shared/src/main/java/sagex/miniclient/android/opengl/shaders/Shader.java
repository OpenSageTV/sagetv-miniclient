package sagex.miniclient.android.opengl.shaders;

import android.opengl.GLES20;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import sagex.miniclient.android.opengl.OpenGLUtils;

public class Shader {
    private static Logger log = LoggerFactory.getLogger(Shader.class);

    private String fragmentSource;
    private String vertexSource;
    private String[] uniforms;
    private String[] attributes;

    private Map<String, Integer> uniformLocations = new HashMap<>();
    private Map<String, Integer> attributeLocations = new HashMap<>();

    private int program = -1;
    private int vertex = -1;
    private int fragment = -1;

    private String name;

    public Shader(String name, String fragmentSource, String vertexSource, String uniforms[], String attributes[]) {
        this.fragmentSource = fragmentSource;
        this.vertexSource = vertexSource;
        this.uniforms = uniforms;
        this.attributes = attributes;
        this.name = name;
    }

    public void load() {
        this.fragment = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        this.vertex = compileShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        this.program = createProgram(vertex, fragment);
        if (uniforms != null) {
            for (String u : uniforms) {
                uniformLocations.put(u, GLES20.glGetUniformLocation(program, u));
                log.debug("[{}]: Uniform: {}={}", program, u, uniform(u));
                if (uniform(u) == -1) {
                    log.error("Invalid Uniform '{}'={}", u, uniform(u), new Exception(u));
                }
            }
        }
        if (attributes != null) {
            for (String a : attributes) {
                attributeLocations.put(a, GLES20.glGetAttribLocation(program, a));
                log.debug("[{}]: Attribute: {}={}", program, a, attribute(a));
                if (attribute(a) == -1) {
                    log.error("Invalid Attribute '{}'={}", a, uniform(a), new Exception(a));
                }
            }
        }
    }

    int uniform(String id) {
        if (!uniformLocations.containsKey(id)) {
            log.error("Invalid Uniform '{}'", id);
        }
        return uniformLocations.get(id);
    }

    int attribute(String id) {
        if (!attributeLocations.containsKey(id)) {
            log.error("Invalid Attribute '{}'", id);
        }
        return attributeLocations.get(id);
    }

    static int compileShader(final int shaderType, String shaderSource) {
        log.debug("Compiling Shader Type: " + shaderType + " using source " + shaderSource);
        int shaderHandle = GLES20.glCreateShader(shaderType);
        if (shaderHandle != 0) {
            GLES20.glShaderSource(shaderHandle, shaderSource);
            GLES20.glCompileShader(shaderHandle);

            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
            if (compileStatus[0] == GLES20.GL_FALSE) {
                log.error("Compile Shader Failed; {}", shaderSource, new Exception("Shader Failed"));
                new Exception(GLES20.glGetShaderInfoLog(shaderHandle)).printStackTrace(System.err);
                GLES20.glDeleteShader(shaderHandle);
                shaderHandle = 0;
            }
            log.debug("Shader Created: {} for type: {}", shaderHandle, shaderType);
        } else {
            log.error("Failed to get a shader handle for {}", shaderType, new Exception());
        }
        return shaderHandle;
    }

    static int createProgram(final int vtxShader, final int pxlShader) {
        int programHandle = GLES20.glCreateProgram();
        if (programHandle != 0) {
            GLES20.glAttachShader(programHandle, vtxShader);
            OpenGLUtils.logGLErrors("attach vertex shader");
            GLES20.glAttachShader(programHandle, pxlShader);
            OpenGLUtils.logGLErrors("attach frag shader");
            GLES20.glLinkProgram(programHandle);
            OpenGLUtils.logGLErrors("link");

            //get linking status
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] == 0) {
                log.error("Link Shader Failed; {}", GLES20.glGetProgramInfoLog(programHandle), new Exception(GLES20.glGetProgramInfoLog(programHandle)));
                GLES20.glDeleteProgram(programHandle);
                programHandle = 0;
            } else {
                log.debug("Linked Program: {} using Vertex {} and Fragment {}", programHandle, vtxShader, pxlShader);
            }
        } else {
            log.error("Failed to get program handle", new Exception());
        }
        return programHandle;
    }

    public int program() {
        return program;
    }

    public String name() {
        return name;
    }

    public void use() {
        OpenGLUtils.logGLErrors(String.format("BEFORE Using Program Shader: %d - %s", program(), name()));
        GLES20.glUseProgram(program());
        OpenGLUtils.logGLErrors(String.format("Using Program Shader: %d - %s", program(), name()));
    }
}
