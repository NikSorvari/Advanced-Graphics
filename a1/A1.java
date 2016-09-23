package a1;

import java.nio.*;
import javax.swing.*;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.GLContext;
import com.jogamp.common.nio.Buffers;

import com.jogamp.opengl.util.*;
import graphicslib3D.GLSLUtils.*;
import graphicslib3D.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.Math.*;

public class A1 extends JFrame implements GLEventListener, KeyListener, MouseWheelListener
{	private GLCanvas myCanvas;
	private int rendering_program;
	private int vao[] = new int[1];
	private GLSLUtils util = new GLSLUtils();

	private float x = 0.0f; //coordinates for triangle
   private float y = 0.0f;
	private float inc = 0.01f; // vertical movement speed
   private float size;
   private int angle;
   private int motionType = 0; // movement style (vertical or circle)
   private int sizeChange = 0; // flag to signal user's change in the triangle size
   private int colorType = 0;    // triangle color (solid blue or rgb gradient)
   private float colorFrag = 0.0f; // color flag for the  vshader
   
   //panel and buttons to change motion style
   private JPanel bPanel;  
   private JButton bCircle;
   private JButton bVertical;
   
   
   

	public A1()
	{	setTitle("Assignment 1");
		setSize(500, 500);
      
      setLayout(new BorderLayout());
      angle = 45;
      
      bPanel = new JPanel();
      bPanel.setLayout(new FlowLayout());
      bCircle = new JButton("Circle");
      bVertical = new JButton("Vertical");
      bPanel.add(bCircle);
      bPanel.add(bVertical);
      
      //button for vertical movement
      bVertical.addActionListener(
         new ActionListener()
         {
            public void actionPerformed(ActionEvent e)
            {
               System.out.println("Vertical");
               motionType = 1;
               myCanvas.requestFocus();
            }
         });
      
      //button for circular movement
      bCircle.addActionListener(
         new ActionListener()
         {
            public void actionPerformed(ActionEvent e)
            {
               System.out.println("Circle");
               motionType = 2;
               myCanvas.requestFocus();
            }
         });
      
      //adding elements to window
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
      myCanvas.setFocusable(true);
      myCanvas.addKeyListener(this);
      myCanvas.addMouseWheelListener(this);
      
      getContentPane().add(myCanvas, BorderLayout.CENTER);
      getContentPane().add(bPanel, BorderLayout.SOUTH);
      
		setVisible(true);
      myCanvas.requestFocus();
		FPSAnimator animator = new FPSAnimator(myCanvas, 30);
		animator.start();
	}
   
   public void keyPressed(KeyEvent e) {
      System.out.println("keyPressed");
   }
   
   public void keyTyped(KeyEvent e) {
      System.out.println("keyTyped");
   }
   // Color changes back and forth between solid blue and gradient when k is pressed.
   public void keyReleased(KeyEvent e) {
      if(e.getKeyCode()== KeyEvent.VK_K) {
         if( colorType == 0)
            colorType = 1;
      }
   }
   
   //change triangle size when mouse wheel moves
   public void mouseWheelMoved(MouseWheelEvent e) {
      if(e.getWheelRotation() == 1) {
         size = size + 0.05f;
      } 
      else if(e.getWheelRotation() == -1) {
         size = size - 0.05f;
      }
      if(size < 0.05f) {
         size = 0.05f;
      }
      if(size > 1.0f) {
         size = 1.0f;
      }
      
      sizeChange = 1;
      
      myCanvas.requestFocus();
   }
   
   //vertical motion
   public void vertical(GL4 gl) {
   
      y += inc;   
      if (y > 1.0f) inc = -0.01f;
      if (y < -1.0f) inc = 0.01f;
      
      int offset_loc = gl.glGetUniformLocation(rendering_program, "iny");
      gl.glProgramUniform1f(rendering_program, offset_loc, y);
   
      gl.glDrawArrays(GL_TRIANGLES,0,3);
   
   }
   
   //cirular motion
   public void circle(GL4 gl) { 
   
      angle ++;
      x = (float)(Math.cos(angle)*0.5);
      y = (float)(Math.sin(angle)*0.5);
            
      int offset_locc = gl.glGetUniformLocation(rendering_program, "inx");
      gl.glProgramUniform1f(rendering_program, offset_locc, x);
      
      int offset_loc = gl.glGetUniformLocation(rendering_program, "iny");
      gl.glProgramUniform1f(rendering_program, offset_loc, y);
   
      gl.glDrawArrays(GL_TRIANGLES,0,3);
   
   }
   //triangle color change
   public void changeColor(GL4 gl) {
         if(colorFrag == 0.0f) {
            colorFrag = 1.0f;
         } else {
            colorFrag = 0.0f;
         }
   
         int offset_locc = gl.glGetUniformLocation(rendering_program, "colorFrag");
         gl.glProgramUniform1f(rendering_program, offset_locc, colorFrag);
      
         gl.glDrawArrays(GL_TRIANGLES,0,3);
   }

	public void display(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glUseProgram(rendering_program);

		float bkg[] = { 0.0f, 0.0f, 0.0f, 1.0f };
		FloatBuffer bkgBuffer = Buffers.newDirectFloatBuffer(bkg);
		gl.glClearBufferfv(GL_COLOR, 0, bkgBuffer);

		if (colorType == 1)
      {
         changeColor(gl);
         colorType = 0;
      }
      
      if (motionType == 0)
         gl.glDrawArrays(GL_TRIANGLES,0,3);
      else if (motionType == 1)
         vertical(gl);
      else if (motionType == 2) 
         circle(gl);
      
      if (sizeChange == 1)
      {
		   int offset_loc = gl.glGetUniformLocation(rendering_program, "ins");
		   gl.glProgramUniform1f(rendering_program, offset_loc, size);

		   gl.glDrawArrays(GL_TRIANGLES,0,3);
         sizeChange = 0;
      }
      
      
	}

	public void init(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		rendering_program = createShaderProgram();
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
	}

	private int createShaderProgram()
   {	GL4 gl = (GL4) GLContext.getCurrentGL();
   
      // printing JOGL and OpenGL versions
      System.out.println( "JOGL Version: " + Package.getPackage("com.jogamp.opengl").getImplementationVersion() );
      System.out.println("OpenGL Version: " + gl.glGetString(GL.GL_VERSION));
   
      int[] vertCompiled = new int[1];
		int[] fragCompiled = new int[1];
		int[] linked = new int[1];
   
      String vshaderSource[] = util.readShaderSource("a1/vert.shader");
      String fshaderSource[] = util.readShaderSource("a1/frag.shader");
      int lengths[];
   
      int vShader = gl.glCreateShader(GL_VERTEX_SHADER);
      gl.glShaderSource(vShader, vshaderSource.length, vshaderSource, null, 0);
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
      gl.glShaderSource(fShader, fshaderSource.length, fshaderSource, null, 0);
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
      
      return vfprogram;
   }


	public static void main(String[] args) { new A1(); }
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}
	public void dispose(GLAutoDrawable drawable) {}
}