package code;

import graphicslib3D.*;
import static java.lang.Math.*;

public class Torus
{
	private int numVertices, numIndices, prec=48;
	private int[] indices;
	private Vertex3D[] vertices;
	private float inner, outer;
	private Vector3D[] sTangent, tTangent;
	
	public Torus(float in, float out, int p)
	{	inner=in; outer=out; prec=p;
		initTorus();
	}

	private void initTorus()
	{	numVertices = (prec+1)*(prec+1);
		numIndices = prec * prec * 6;
		vertices = new Vertex3D[numVertices];
		for (int i=0; i<numVertices; i++)
			{ vertices[i] = new Vertex3D(); }
		indices = new int[numIndices];
		sTangent = new Vector3D[numVertices];
		tTangent = new Vector3D[numVertices];

		// calculate first ring.
		// suitable values for inner and outer radius are 4 and 5
		for (int i=0; i<prec+1; i++)
		{	Point3D initPos = new Point3D(outer, 0.0, 0.0);
			Point3D initPos2 = tRotateZ(initPos,(i*360.0f/prec));
			Point3D initPos3 = new Point3D(inner,0.0, 0.0);
			Point3D initPos4 = initPos2.add(initPos3);
			vertices[i].setLocation(initPos4);
			
			vertices[i].setS(0.0f);
			vertices[i].setT(((float)i)/((float)prec));

			Vector3D negZ = new Vector3D(0.0f, 0.0f, -1.0f);
			Vector3D negY = new Vector3D(0.0f, -1.0f, 0.0f);
			tTangent[i] = new Vector3D(tRotateZ(new Point3D(negY),(i*360.0f/(prec))));
			sTangent[i] = negZ;
			vertices[i].setNormal(sTangent[i].cross(tTangent[i]));
		}

		//  rotate the first ring about Y to get the other rings
		for (int ring=1; ring<prec+1; ring++)
		{	for (int i=0; i<prec+1; i++)
			{	float rotAmt = (float) ((float)ring*360.0f/(prec));
				Vector3D vp = new Vector3D(vertices[i].getLocation());
				Vector3D vpr = tRotateY(vp, rotAmt);
				vertices[ring*(prec+1)+i].setLocation(new Point3D(vpr));

				vertices[ring*(prec+1)+i].setS((float)ring/(float)prec);
				vertices[ring*(prec+1)+i].setT(vertices[i].getT());

				sTangent[ring*(prec+1)+i] = tRotateY(sTangent[i], rotAmt);
				tTangent[ring*(prec+1)+i] = tRotateY(tTangent[i], rotAmt);

				Vector3D normalRotateY = tRotateY(vertices[i].getNormal(), rotAmt);
				vertices[ring*(prec+1)+i].setNormal(normalRotateY);
			}
		}

		// calculate triangle indices
		for(int ring=0; ring<prec; ring++)
		{	for(int i=0; i<prec; i++)
			{	indices[((ring*prec+i)*2)*3+0] = ring*(prec+1)+i;
				indices[((ring*prec+i)*2)*3+1]=(ring+1)*(prec+1)+i;
				indices[((ring*prec+i)*2)*3+2]  = ring*(prec+1)+i+1;
				indices[((ring*prec+i)*2+1)*3+0]= ring*(prec+1)+i+1;
				indices[((ring*prec+i)*2+1)*3+1]=(ring+1)*(prec+1)+i;
				indices[((ring*prec+i)*2+1)*3+2]=(ring+1)*(prec+1)+i+1;
			}
		}
	}

	private Vector3D tRotateY(Vector3D inVec, float amount)
	{	Matrix3D yMat = new Matrix3D();
		yMat.rotateY((double)amount);
		Vector3D result = inVec.mult(yMat);
		return result;
	}

	private Point3D tRotateZ(Point3D inPt, float amount)
	{	Matrix3D zMat = new Matrix3D();
		zMat.rotateZ((double)amount);
		Point3D result = inPt.mult(zMat);
		return result;
	}

	public int[] getIndices() { return indices; }
	public Vertex3D[] getVertices() { return vertices; }
}
