package code;

import graphicslib3D.*;
import graphicslib3D.GLSLUtils.*;

import java.io.*;
import java.nio.*;
import javax.swing.JFrame;

import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.texture.*;
import com.jogamp.common.nio.Buffers;

public class Code extends JFrame implements GLEventListener
{
	private GLCanvas myCanvas;
	private GLSLUtils util = new GLSLUtils();
	private String[] vShaderSource, fShaderSource;
	private int rendering_program;
	private int vao[] = new int[2];
	private int vbo[] = new int[3];
	private int mv_location, proj_location;
	private Matrix3D m_matrix = new Matrix3D();
	private Matrix3D v_matrix = new Matrix3D();
	private Matrix3D mv_matrix = new Matrix3D();
	private Matrix3D proj_matrix = new Matrix3D();

	private ImportedModel ground = new ImportedModel("../grid.obj");
	private int numGroundVertices;
	private int textureID0, textureID1;
	private Texture terTex;

	private Point3D groundLoc = new Point3D(0,0,0);
	private Point3D cameraLoc = new Point3D(0.03,0.03f,0.8f);

	public Code()
	{	setSize(800, 800);
		setTitle("Chapter10 - program4");
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		this.getContentPane().add(myCanvas);
		this.setVisible(true);
	}

	public void display(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		
		float bkg[] = { 0.0f, 0.0f, 0.0f, 1.0f };
		FloatBuffer bkgBuffer = Buffers.newDirectFloatBuffer(bkg);
		gl.glClearBufferfv(GL_COLOR, 0, bkgBuffer);
		
		gl.glUseProgram(rendering_program);
		
		mv_location = gl.glGetUniformLocation(rendering_program, "mv_matrix");
		proj_location = gl.glGetUniformLocation(rendering_program, "proj_matrix");
	
		float aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		proj_matrix = perspective(50.0f, aspect, 0.1f, 1000.0f);
		v_matrix.setToIdentity();
		v_matrix.translate(-cameraLoc.getX(),-cameraLoc.getY(),-cameraLoc.getZ());

		m_matrix.setToIdentity();
		m_matrix.translate(groundLoc.getX(),groundLoc.getY(),groundLoc.getZ());
		m_matrix.rotateX(15.0);

		mv_matrix.setToIdentity();
		mv_matrix.concatenate(v_matrix);
		mv_matrix.concatenate(m_matrix);

		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
	
		// vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		// normals buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		
		// texture coordinate buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		gl.glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);
		
		// texture
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, textureID0);
		// height map
		gl.glActiveTexture(GL_TEXTURE1);
		gl.glBindTexture(GL_TEXTURE_2D, textureID1);
		
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		gl.glDrawArrays(GL_TRIANGLES, 0, numGroundVertices);
	}

	public void init(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		createShaderProgram();
		setupVertices();

		terTex = loadTexture("heightTexture.jpg");
		textureID0 = terTex.getTextureObject();
		
		terTex = loadTexture("height.jpg");
		textureID1 = terTex.getTextureObject();
	}

	private void setupVertices()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	
		Vertex3D[] ground_vertices = ground.getVertices();
		numGroundVertices = ground.getNumVertices();
		
		float[] ground_fvalues = new float[numGroundVertices*3];
		float[] ground_tvalues = new float[numGroundVertices*2];
		float[] ground_nvalues = new float[numGroundVertices*3];
		
		for (int i=0; i<numGroundVertices; i++)
		{	ground_fvalues[i*3]   = (float) (ground_vertices[i]).getX();
			ground_fvalues[i*3+1] = (float) (ground_vertices[i]).getY();
			ground_fvalues[i*3+2] = (float) (ground_vertices[i]).getZ();
			ground_tvalues[i*2]   = (float) (ground_vertices[i]).getS();
			ground_tvalues[i*2+1] = (float) (ground_vertices[i]).getT();
			ground_nvalues[i*3]   = (float) (ground_vertices[i]).getNormalX();
			ground_nvalues[i*3+1] = (float) (ground_vertices[i]).getNormalY();
			ground_nvalues[i*3+2] = (float) (ground_vertices[i]).getNormalZ();
		}	

		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
		gl.glGenBuffers(3, vbo, 0);

		//  ground vertices
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(ground_fvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()*4, vertBuf, GL_STATIC_DRAW);

		// ground normals
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		FloatBuffer groundNorBuf = Buffers.newDirectFloatBuffer(ground_nvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, groundNorBuf.limit()*4, groundNorBuf, GL_STATIC_DRAW);
		
		//  ground texture coordinates
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		FloatBuffer groundTexBuf = Buffers.newDirectFloatBuffer(ground_tvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, groundTexBuf.limit()*4, groundTexBuf, GL_STATIC_DRAW);
	}

	public static void main(String[] args) { new Code(); }
	public void dispose(GLAutoDrawable drawable) { }
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}

	private void createShaderProgram()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	
		vShaderSource = util.readShaderSource("code/vert.shader");
		fShaderSource = util.readShaderSource("code/frag.shader");
		
		int vshader = gl.glCreateShader(GL_VERTEX_SHADER);
		int fshader = gl.glCreateShader(GL_FRAGMENT_SHADER);

		gl.glShaderSource(vshader, vShaderSource.length, vShaderSource, null, 0);
		gl.glShaderSource(fshader, fShaderSource.length, fShaderSource, null, 0);
		
		gl.glCompileShader(vshader);
		gl.glCompileShader(fshader);
		
		rendering_program = gl.glCreateProgram();

		gl.glAttachShader(rendering_program, vshader);
		gl.glAttachShader(rendering_program, fshader);
		gl.glLinkProgram(rendering_program);
	}

	private Matrix3D perspective(float fovy, float aspect, float n, float f)
	{	float q = 1.0f / ((float) Math.tan(Math.toRadians(0.5f * fovy)));
		float A = q / aspect;
		float B = (n + f) / (n - f);
		float C = (2.0f * n * f) / (n - f);
		Matrix3D r = new Matrix3D();
		r.setElementAt(0,0,A);
		r.setElementAt(1,1,q);
		r.setElementAt(2,2,B);
		r.setElementAt(3,2,-1.0f);
		r.setElementAt(2,3,C);
		r.setElementAt(3,3,0.0f);
		return r;
	}
	
	public Texture loadTexture(String textureFileName)
	{	Texture tex = null;
		try { tex = TextureIO.newTexture(new File(textureFileName), false); }
		catch (Exception e) { e.printStackTrace(); }
		return tex;
	}
}