package code;

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

public class Code extends JFrame implements GLEventListener, KeyListener, MouseWheelListener
{	private GLCanvas myCanvas;
   private int rendering_program;
   private int vao[] = new int[1];
   private GLSLUtils util = new GLSLUtils();

   private float x = 0.0f;
   private float inc = 0.01f;

   private float y = 0.0f;
   
   private JPanel btnPanel;
   private JButton btnCircle;
   private JButton btnVert;
   
   private int flag = 0;
   private int sizeFlag = 0;
   
   private float size;
   
   private int theta;

   public Code()
   {	setTitle("CSC155 a1");
      setSize(400, 200);
      //setLayout(new FlowLayout());
      //setLayout(new GridLayout(0, 2));
      setLayout(new BorderLayout());
      
      theta = 45;
      
      btnPanel = new JPanel();
      btnPanel.setLayout(new FlowLayout());
      btnCircle = new JButton("Circle");
      btnVert = new JButton("Vertical");
      btnPanel.add(btnCircle);
      btnPanel.add(btnVert);
      
      btnCircle.addActionListener(
            new ActionListener(){
               public void actionPerformed(ActionEvent e){
               
                  System.out.println("Circle");
                  flag = 2;
                  myCanvas.requestFocus();
               }
            });
        
      btnVert.addActionListener(
            new ActionListener(){
               public void actionPerformed(ActionEvent e){
               
                  System.out.println("Vertical");
                  flag = 1;
                  myCanvas.requestFocus();
               }
            });
        
      
      myCanvas = new GLCanvas();
      myCanvas.addGLEventListener(this);
      myCanvas.setFocusable(true);
      myCanvas.addKeyListener(this);
      myCanvas.addMouseWheelListener(this);
   	
      //getContentPane().add(btnPanel);
      getContentPane().add(myCanvas, BorderLayout.CENTER);
      
      getContentPane().add(btnPanel, BorderLayout.NORTH);
      //getContentPane().add(btnVert);
      setVisible(true);
      myCanvas.requestFocus(); //
      FPSAnimator animator = new FPSAnimator(myCanvas, 30);
      animator.start();
   }
   
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
      
      sizeFlag = 1;
      
      myCanvas.requestFocus();
   }
   
   public void keyPressed(KeyEvent e) {
      System.out.println("keyPressed");
   }
   
   public void keyTyped(KeyEvent e) {
      System.out.println("keyTyped");
   }
   
   public void keyReleased(KeyEvent e) {
      if(e.getKeyCode()== KeyEvent.VK_K) {
         // change triangle color
      }
   }
   
   public void vertical(GL4 gl) {
   
      y += inc;   
      if (y > 1.0f) inc = -0.01f;
      if (y < -1.0f) inc = 0.01f;
      
      int offset_loc = gl.glGetUniformLocation(rendering_program, "iny");
      gl.glProgramUniform1f(rendering_program, offset_loc, y);
   
      gl.glDrawArrays(GL_TRIANGLES,0,3);
   
   }
   
   public void circle(GL4 gl) { 
   
      theta ++;//= (int) Math.toRadians(10);
      x = (float)(Math.cos(theta)*0.5);
      y = (float)(Math.sin(theta)*0.5);
            
      int offset_locc = gl.glGetUniformLocation(rendering_program, "inx");
      gl.glProgramUniform1f(rendering_program, offset_locc, x);
      
      int offset_loc = gl.glGetUniformLocation(rendering_program, "iny");
      gl.glProgramUniform1f(rendering_program, offset_loc, y);
   
      gl.glDrawArrays(GL_TRIANGLES,0,3);
   
   }

   public void display(GLAutoDrawable drawable)
   {	GL4 gl = (GL4) GLContext.getCurrentGL();
      gl.glUseProgram(rendering_program);
   
      float bkg[] = { 0.0f, 0.0f, 0.0f, 1.0f };
      FloatBuffer bkgBuffer = Buffers.newDirectFloatBuffer(bkg);
      gl.glClearBufferfv(GL_COLOR, 0, bkgBuffer);
   
      
      if (sizeFlag == 1) {
         int offset_locc = gl.glGetUniformLocation(rendering_program, "ins");
         gl.glProgramUniform1f(rendering_program, offset_locc, size);
      
         gl.glDrawArrays(GL_TRIANGLES,0,3);
         
         sizeFlag = 0;
      }
   
      
      if (flag == 0) {
         gl.glDrawArrays(GL_TRIANGLES,0,3);
      } 
      else if (flag == 1) {
         vertical(gl);
      } 
      else if (flag == 2) {
         circle(gl);
      }
   	
      
      /*
      int offset_loc = gl.glGetUniformLocation(rendering_program, "inc");
   	gl.glProgramUniform1f(rendering_program, offset_loc, x);
   
   	gl.glDrawArrays(GL_TRIANGLES,0,3);
      */
   }

   public void init(GLAutoDrawable drawable)
   {	GL4 gl = (GL4) GLContext.getCurrentGL();
      rendering_program = createShaderProgram();
      gl.glGenVertexArrays(vao.length, vao, 0);
      gl.glBindVertexArray(vao[0]);
   }

   private int createShaderProgram()
   {	GL4 gl = (GL4) GLContext.getCurrentGL();
   
      String vshaderSource[] = util.readShaderSource("code/vert.shader");
      String fshaderSource[] = util.readShaderSource("code/frag.shader");
      int lengths[];
   
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

   public static void main(String[] args) { new Code(); }
   public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}
   public void dispose(GLAutoDrawable drawable) {}
}