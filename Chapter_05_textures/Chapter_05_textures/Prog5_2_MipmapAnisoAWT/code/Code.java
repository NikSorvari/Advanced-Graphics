package code;

import graphicslib3D.*;
import java.io.*;
import java.nio.*;
import javax.swing.*;

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.Graphics2D;

import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.common.nio.Buffers;

public class Code extends JFrame implements GLEventListener
{	private GLCanvas myCanvas;
	private int rendering_program;
	private int vao[] = new int[1];
	private int vbo[] = new int[2];
	private float cameraX, cameraY, cameraZ;
	private float pyrLocX, pyrLocY, pyrLocZ;
	private GLSLUtils util = new GLSLUtils();
	private int brickTexture;

	public Code()
	{	setTitle("Chapter5 - program2");
		setSize(800, 800);
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		getContentPane().add(myCanvas);
		this.setVisible(true);
	}

	public void display(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();

		gl.glClear(GL_DEPTH_BUFFER_BIT);

		gl.glUseProgram(rendering_program);

		int mv_loc = gl.glGetUniformLocation(rendering_program, "mv_matrix");
		int proj_loc = gl.glGetUniformLocation(rendering_program, "proj_matrix");

		float aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		Matrix3D pMat = perspective(60.0f, aspect, 0.1f, 1000.0f);

		Matrix3D vMat = new Matrix3D();
		vMat.translate(-cameraX, -cameraY, -cameraZ);

		Matrix3D mMat = new Matrix3D();
		mMat.translate(pyrLocX, pyrLocY, pyrLocZ);
		mMat.rotateX(-25.0f);
		mMat.rotateY(35.0f);

		Matrix3D mvMat = new Matrix3D();
		mvMat.concatenate(vMat);
		mvMat.concatenate(mMat);

		gl.glUniformMatrix4fv(mv_loc, 1, false, mvMat.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, brickTexture);

		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glDrawArrays(GL_TRIANGLES, 0, 18);
	}

	public void init(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	
		rendering_program = createShaderProgram();
		setupVertices();
		
		cameraX = 0.0f; cameraY = 0.0f; cameraZ = 4.0f;
		pyrLocX = 0.0f; pyrLocY = 0.0f; pyrLocZ = 0.0f;

		brickTexture = loadTexture("brick1.jpg");
	}

	private void setupVertices()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	
		float[] pyramid_positions =
		{	-1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f,   //front
			1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f,   //right
			1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f, //back
			-1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f, //left
			-1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, //LF
			1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f  //RR
		};
		float[] texture_coordinates =
		{	0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
			0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
			0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
			0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
			0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
			1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f
		};

		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
		gl.glGenBuffers(vbo.length, vbo, 0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		FloatBuffer pyrBuf = Buffers.newDirectFloatBuffer(pyramid_positions);
		gl.glBufferData(GL_ARRAY_BUFFER, pyrBuf.limit()*4, pyrBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		FloatBuffer texBuf = Buffers.newDirectFloatBuffer(texture_coordinates);
		gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit()*4, texBuf, GL_STATIC_DRAW);
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

	public static void main(String[] args) { new Code(); }
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}
	public void dispose(GLAutoDrawable drawable) {}

	private int createShaderProgram()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();

		String vshaderSource[] = util.readShaderSource("code/vert.shader");
		String fshaderSource[] = util.readShaderSource("code/frag.shader");

		int vShader = gl.glCreateShader(GL_VERTEX_SHADER);
		int fShader = gl.glCreateShader(GL_FRAGMENT_SHADER);

		gl.glShaderSource(vShader, vshaderSource.length, vshaderSource, null, 0);
		gl.glShaderSource(fShader, fshaderSource.length, fshaderSource, null, 0);
		gl.glCompileShader(vShader);
		gl.glCompileShader(fShader);

		int vfprogram = gl.glCreateProgram();
		gl.glAttachShader(vfprogram, vShader);
		gl.glAttachShader(vfprogram, fShader);
		gl.glLinkProgram(vfprogram);
		return vfprogram;
	}
	
	private int loadTexture(String textureFileName)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		BufferedImage textureImage = getBufferedImage(textureFileName);
		byte[ ] imgRGBA = getRGBAPixelData(textureImage);
		ByteBuffer rgbaBuffer = Buffers.newDirectByteBuffer(imgRGBA);
		
		int[ ] textureIDs = new int[1];				// array to hold generated texture IDs
		gl.glGenTextures(1, textureIDs, 0);
		int textureID = textureIDs[0];				// ID for the 0th texture object
		gl.glBindTexture(GL_TEXTURE_2D, textureID);	// specifies the currently active 2D texture
		gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA,	// MIPMAP Level, number of color components
			textureImage.getWidth(), textureImage.getHeight(), 0,	// image size, border (ignored)
			GL_RGBA, GL_UNSIGNED_BYTE,				// pixel format and data type
			rgbaBuffer);							// buffer holding texture data
		
		// here we also demonstrate building a mipmap and using anisotropic filtering
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		gl.glGenerateMipmap(GL.GL_TEXTURE_2D);
		
		if (gl.isExtensionAvailable("GL_EXT_texture_filter_anisotropic"))
		{	float anisoset[] = new float[1];
			gl.glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, anisoset, 0);
			gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, anisoset[0]);
		}
		
		return textureID;
	}
	
	private BufferedImage getBufferedImage(String fileName)
	{	BufferedImage img;
		try { img = ImageIO.read(new File(fileName)); }
		catch (IOException e)
		{	System.err.println("Error reading '" + fileName + '"'); throw new RuntimeException(e); }
		return img;
	}
	
	private byte[ ] getRGBAPixelData(BufferedImage img)
	{	byte[ ] imgRGBA;
		int height = img.getHeight(null);
		int width = img.getWidth(null);
		
		WritableRaster raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, width, height, 4, null);
		ComponentColorModel colorModel = new ComponentColorModel(
			ColorSpace.getInstance(ColorSpace.CS_sRGB),
			new int[ ] { 8, 8, 8, 8 }, true, false, // bits, has Alpha, isAlphaPreMultiplied
			ComponentColorModel.TRANSLUCENT, 	// transparency
			DataBuffer.TYPE_BYTE); 			// data transfer type
		BufferedImage newImage = new BufferedImage(colorModel, raster, false, null);
		
		// use an affine transform to “flip” the image to conform to OpenGL orientation.
		// In Java the origin is at the upper left of the window.
		// In OpenGL the origin is at the lower left of the canvas.
		AffineTransform gt = new AffineTransform();
		gt.translate(0, height);
		gt.scale(1, -1d);
		
		Graphics2D g = newImage.createGraphics();
		g.transform(gt);
		g.drawImage(img, null, null);
		g.dispose();
		
		DataBufferByte dataBuf = (DataBufferByte) raster.getDataBuffer();
		imgRGBA = dataBuf.getData();
		return imgRGBA;
	}
}