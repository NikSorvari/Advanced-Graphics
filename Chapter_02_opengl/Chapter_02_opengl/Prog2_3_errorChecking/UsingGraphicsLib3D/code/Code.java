package code;

import javax.swing.*;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import graphicslib3D.*;
import graphicslib3D.GLSLUtils.*;

public class Code extends JFrame implements GLEventListener
{	private GLCanvas myCanvas;
	private int rendering_program;
	private int vao[] = new int[1];
	private GLSLUtils util = new GLSLUtils();

	public Code()
	{	setTitle("Chapter2 - program3b");
		setSize(400, 200);
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		getContentPane().add(myCanvas);
		setVisible(true);
	}

	public void display(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) drawable.getGL();
		gl.glUseProgram(rendering_program);
		gl.glDrawArrays(GL_POINTS,0,1);
	}

	public void init(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) drawable.getGL();
		rendering_program = createShaderProgram();
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
	}

	private int createShaderProgram()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		int[] vertCompiled = new int[1];
		int[] fragCompiled = new int[1];
		int[] linked = new int[1];

		String vshaderSource[] =
		{	"#version 430    \n",
			"void main(void) \n",
			"{ gl_Position = vec4(0.0, 0.0, 0.5, 1.0); } \n",
		};
		String fshaderSource[] =
		{	"#version 430    \n",
			"out vec4 color; \n",
			"void main(void) \n",
			"{ color = vec4(0.0, 0.0, 1.0, 1.0); } \n"
		};

		int vShader = gl.glCreateShader(GL_VERTEX_SHADER);
		gl.glShaderSource(vShader, 3, vshaderSource, null, 0);
		gl.glCompileShader(vShader);

		util.checkOpenGLError();  // can use returned boolean
		gl.glGetShaderiv(vShader, GL_COMPILE_STATUS, vertCompiled, 0);
		if (vertCompiled[0] == 1)
		{	System.out.println("vertex compilation success");
		} else
		{	System.out.println("vertex compilation failed");
			util.printShaderLog(vShader);
		}

		int fShader = gl.glCreateShader(GL_FRAGMENT_SHADER);
		gl.glShaderSource(fShader, 4, fshaderSource, null, 0);
		gl.glCompileShader(fShader);

		util.checkOpenGLError();  // can use returned boolean
		gl.glGetShaderiv(fShader, GL_COMPILE_STATUS, fragCompiled, 0);
		if (fragCompiled[0] == 1)
		{	System.out.println("fragment compilation success");
		} else
		{	System.out.println("fragment compilation failed");
			util.printShaderLog(fShader);
		}

		int vfprogram = gl.glCreateProgram();
		gl.glAttachShader(vfprogram, vShader);
		gl.glAttachShader(vfprogram, fShader);

		gl.glLinkProgram(vfprogram);
		util.checkOpenGLError();
		gl.glGetProgramiv(vfprogram, GL_LINK_STATUS, linked, 0);
		if (linked[0] == 1)
		{	System.out.println("linking succeeded");
		} else
		{	System.out.println("linking failed");
			util.printProgramLog(vfprogram);
		}

		gl.glDeleteShader(vShader);
		gl.glDeleteShader(fShader);
		return vfprogram;
	}

	public static void main(String[] args) { new Code(); }
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}
	public void dispose(GLAutoDrawable drawable) {}
}