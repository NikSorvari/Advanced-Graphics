package code;

import javax.swing.*;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;

public class Code extends JFrame implements GLEventListener
{	private GLCanvas myCanvas;

	public Code()
	{	setTitle("Chapter2 - supplemental notes");
		setSize(100,100);
		setLocation(300,300);
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		getContentPane().add(myCanvas);
		setVisible(true);
	}

	public void display(GLAutoDrawable drawable) {}

	public void init(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		float[] size = new float[2];
		gl.glGetFloatv(GL_POINT_SIZE_RANGE, size, 0);
		System.out.println(size[0]);
		System.out.println(size[1]);
	}
	
	public static void main(String[] args) { new Code(); }
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}
	public void dispose(GLAutoDrawable drawable) {}
}