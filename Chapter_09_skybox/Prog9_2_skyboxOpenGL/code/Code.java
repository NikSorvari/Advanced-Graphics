package code;

import graphicslib3D.*;
import graphicslib3D.shape.*;
import graphicslib3D.GLSLUtils.*;

import javax.swing.JFrame;
import java.io.*;
import java.nio.*;

import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.texture.*;
import com.jogamp.common.nio.Buffers;

public class Code extends JFrame implements GLEventListener
{
	private String[] vertShaderSource, fragShaderSource;
	private GLCanvas myCanvas;
	private GLSLUtils util = new GLSLUtils();
	private int rendering_program, rendering_program_cube_map;
	private int vao[] = new int[1];
	private int vbo[] = new int[4];
	private int mv_location, proj_location, tx_location;
	private int textureID1, textureID2;
	private float aspect;
	
	private Torus myTorus = new Torus(0.8f, 0.4f, 48);
	private int numTorusVertices;
	
	// location of torus and camera
	private Point3D torusLoc = new Point3D(0.0, 0.0, 0.0);
	private Point3D cameraLoc = new Point3D(0.0, 0.0, 5.0);
	
	private Matrix3D m_matrix = new Matrix3D();
	private Matrix3D v_matrix = new Matrix3D();
	private Matrix3D mv_matrix = new Matrix3D();
	private Matrix3D proj_matrix = new Matrix3D();
	private Matrix3D cubeV_matrix = new Matrix3D();
	
	public Code()
	{	setTitle("Chapter9 - program 2");
		setSize(800, 800);
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		this.getContentPane().add(myCanvas);
		this.setVisible(true);
	}
	
	public void init(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();

		createShaderPrograms();
		setupVertices();

		Texture t = loadTexture("brick1.jpg");
		textureID1 = t.getTextureObject();
		
		textureID2 = loadCubeMap();
		gl.glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);
	}

	public void display(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		float depthClearVal[] = new float[1]; depthClearVal[0] = 1.0f;
		gl.glClearBufferfv(GL_DEPTH,0,depthClearVal,0);
	
		//  build the PROJECTION matrix
		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();	
		proj_matrix = perspective(60.0f, aspect, 0.1f, 1000.0f);

		//  build the VIEW matrix
		v_matrix.setToIdentity();
		v_matrix.translate(-cameraLoc.getX(), -cameraLoc.getY(), -cameraLoc.getZ());
		
		// draw cube map
		
		gl.glUseProgram(rendering_program_cube_map);

		//  put the V matrix into the corresponding uniforms
		cubeV_matrix = (Matrix3D) v_matrix.clone();
		cubeV_matrix.scale(1.0, -1.0, -1.0);
		int v_location = gl.glGetUniformLocation(rendering_program_cube_map, "v_matrix");
		gl.glUniformMatrix4fv(v_location, 1, false, cubeV_matrix.getFloatValues(), 0);
		
		// put the P matrix into the corresponding uniform
		int ploc = gl.glGetUniformLocation(rendering_program_cube_map, "p_matrix");
		gl.glUniformMatrix4fv(ploc, 1, false, proj_matrix.getFloatValues(), 0);
		
		// set up vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_CUBE_MAP, textureID2);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glDisable(GL_DEPTH_TEST);
		gl.glDrawArrays(GL_TRIANGLES, 0, 36);
		gl.glEnable(GL_DEPTH_TEST);

		// draw the torus
		
		gl.glUseProgram(rendering_program);
		
		mv_location = gl.glGetUniformLocation(rendering_program, "mv_matrix");
		proj_location = gl.glGetUniformLocation(rendering_program, "proj_matrix");
		
		//  build the MODEL matrix
		m_matrix.setToIdentity();
		m_matrix.translate(torusLoc.getX(),torusLoc.getY(),torusLoc.getZ());
		m_matrix.rotateX(35.0);
		
		//  build the MODEL-VIEW matrix
		mv_matrix.setToIdentity();
		mv_matrix.concatenate(v_matrix);
		mv_matrix.concatenate(m_matrix);
		
		//  put the MV and PROJ matrices into the corresponding uniforms
		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
		
		// set up torus vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		// set up torus texture coordinates buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		
		// make the texture map the active texture
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, textureID1);
	
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glDepthFunc(GL_LEQUAL);
	
		gl.glDrawArrays(GL_TRIANGLES, 0, numTorusVertices);
	}

	// -----------------------------

	private void setupVertices()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		float[] cube_vertices =
	        {	-1.0f,  1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f,
			1.0f, -1.0f, -1.0f, 1.0f,  1.0f, -1.0f, -1.0f,  1.0f, -1.0f,
			1.0f, -1.0f, -1.0f, 1.0f, -1.0f,  1.0f, 1.0f,  1.0f, -1.0f,
			1.0f, -1.0f,  1.0f, 1.0f,  1.0f,  1.0f, 1.0f,  1.0f, -1.0f,
			1.0f, -1.0f,  1.0f, -1.0f, -1.0f,  1.0f, 1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f,  1.0f, -1.0f,  1.0f,  1.0f, 1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f,  1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f, -1.0f, -1.0f,  1.0f, -1.0f, -1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f,  1.0f,  1.0f, -1.0f,  1.0f,  1.0f, -1.0f, -1.0f,
			1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f,
			-1.0f,  1.0f, -1.0f, 1.0f,  1.0f, -1.0f, 1.0f,  1.0f,  1.0f,
			1.0f,  1.0f,  1.0f, -1.0f,  1.0f,  1.0f, -1.0f,  1.0f, -1.0f
		};
		
		Vertex3D[] torus_vertices = myTorus.getVertices();
		
		int[] torus_indices = myTorus.getIndices();	
		float[] torus_fvalues = new float[torus_indices.length*3];
		float[] torus_tvalues = new float[torus_indices.length*2];
		
		for (int i=0; i<torus_indices.length; i++)
		{	torus_fvalues[i*3]   = (float) (torus_vertices[torus_indices[i]]).getX();			
			torus_fvalues[i*3+1] = (float) (torus_vertices[torus_indices[i]]).getY();
			torus_fvalues[i*3+2] = (float) (torus_vertices[torus_indices[i]]).getZ();
			
			torus_tvalues[i*2]   = (float) (torus_vertices[torus_indices[i]]).getS();
			torus_tvalues[i*2+1] = (float) (torus_vertices[torus_indices[i]]).getT();
		}
		
		numTorusVertices = torus_indices.length;

		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);

		gl.glGenBuffers(4, vbo, 0);

		//  put the Torus vertices into the first buffer,
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(torus_fvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()*4, vertBuf, GL_STATIC_DRAW);
		
		// load the torus texture coordinates into the second buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		FloatBuffer torusTexBuf = Buffers.newDirectFloatBuffer(torus_tvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, torusTexBuf.limit()*4, torusTexBuf, GL_STATIC_DRAW);
		
		// load the cube vertex coordinates into the third buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		FloatBuffer cubeVertBuf = Buffers.newDirectFloatBuffer(cube_vertices);
		gl.glBufferData(GL_ARRAY_BUFFER, cubeVertBuf.limit()*4, cubeVertBuf, GL_STATIC_DRAW);
	}

	public static void main(String[] args) { new Code(); }
	public void dispose(GLAutoDrawable drawable) {}
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}
	
//-----------------
	private void createShaderPrograms()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();

		// first create rendering program for scene

		vertShaderSource = util.readShaderSource("code/vert.shader");
		fragShaderSource = util.readShaderSource("code/frag.shader");

		int vertexShader = gl.glCreateShader(GL_VERTEX_SHADER);
		int fragmentShader = gl.glCreateShader(GL_FRAGMENT_SHADER);
		
		gl.glShaderSource(vertexShader, vertShaderSource.length, vertShaderSource, null, 0);
		gl.glShaderSource(fragmentShader, fragShaderSource.length, fragShaderSource, null, 0);

		gl.glCompileShader(vertexShader);
		gl.glCompileShader(fragmentShader);

		rendering_program = gl.glCreateProgram();
		gl.glAttachShader(rendering_program, vertexShader);
		gl.glAttachShader(rendering_program, fragmentShader);
		gl.glLinkProgram(rendering_program);

		// now create rendering program for cube map

		vertShaderSource = util.readShaderSource("code/vertC.shader");
		fragShaderSource = util.readShaderSource("code/fragC.shader");

		vertexShader = gl.glCreateShader(GL_VERTEX_SHADER);
		fragmentShader = gl.glCreateShader(GL_FRAGMENT_SHADER);
		
		gl.glShaderSource(vertexShader, vertShaderSource.length, vertShaderSource, null, 0);
		gl.glShaderSource(fragmentShader, fragShaderSource.length, fragShaderSource, null, 0);

		gl.glCompileShader(vertexShader);
		gl.glCompileShader(fragmentShader);

		rendering_program_cube_map = gl.glCreateProgram();
		gl.glAttachShader(rendering_program_cube_map, vertexShader);
		gl.glAttachShader(rendering_program_cube_map, fragmentShader);
		gl.glLinkProgram(rendering_program_cube_map);
	}

//------------------
	private int loadCubeMap()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		GLProfile glp = gl.getGLProfile();
		Texture tex = new Texture(GL_TEXTURE_CUBE_MAP);
		
		try {
			TextureData topFile = TextureIO.newTextureData(glp, new File("cubeMap/top.jpg"), false, "jpg");
			TextureData leftFile = TextureIO.newTextureData(glp, new File("cubeMap/left.jpg"), false, "jpg");
			TextureData fntFile = TextureIO.newTextureData(glp, new File("cubeMap/center.jpg"), false, "jpg");
			TextureData rightFile = TextureIO.newTextureData(glp, new File("cubeMap/right.jpg"), false, "jpg");
			TextureData bkFile = TextureIO.newTextureData(glp, new File("cubeMap/back.jpg"), false, "jpg");
			TextureData botFile = TextureIO.newTextureData(glp, new File("cubeMap/bottom.jpg"), false, "jpg");
			
			tex.updateImage(gl, rightFile, GL_TEXTURE_CUBE_MAP_POSITIVE_X);
			tex.updateImage(gl, leftFile, GL_TEXTURE_CUBE_MAP_NEGATIVE_X);
			tex.updateImage(gl, botFile, GL_TEXTURE_CUBE_MAP_POSITIVE_Y);
			tex.updateImage(gl, topFile, GL_TEXTURE_CUBE_MAP_NEGATIVE_Y);
			tex.updateImage(gl, fntFile, GL_TEXTURE_CUBE_MAP_POSITIVE_Z);
			tex.updateImage(gl, bkFile, GL_TEXTURE_CUBE_MAP_NEGATIVE_Z);
		} catch (IOException|GLException e) {}
		
		int[] textureIDs = new int[1];
		gl.glGenTextures(1, textureIDs, 0);
		int textureID = tex.getTextureObject();
		
		// reduce seams
		gl.glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);

		return textureID;
	}
//--------------------------
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