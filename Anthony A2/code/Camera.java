package code;


public class Camera {
	private float cameraX, cameraY, cameraZ;
   private float cameraPanAngle;
   private float cameraPitchAngle;
	
	
	public Camera(float cameraX, float cameraY, float cameraZ) {
		this.cameraX = cameraX;
		this.cameraY = cameraY;
		this.cameraZ = cameraZ;
      cameraPanAngle = 0;
      cameraPitchAngle = 0;

	}
   
   public void forward() {
      cameraZ = cameraZ - 1.0f;
   }
   
   public void backward() {
      cameraZ = cameraZ + 1.0f;
   }

   public void strafeLeft() {
      cameraX = cameraX - 1.0f;
   }
   
   public void strafeRight() {
      cameraX = cameraX + 1.0f;
   }
   
   public void down() {
      cameraY = cameraY - 1.0f;
   }
   
   public void up() {
      cameraY = cameraY + 1.0f;
   }
   
   public void panLeft() {
      cameraPanAngle = cameraPanAngle - 10;
   }
   
   public void panRight() {
      cameraPanAngle = cameraPanAngle + 10;
   }
   
   public void pitchUp() {
      cameraPitchAngle = cameraPitchAngle - 10;
   }
   
   public void pitchDown() {
      cameraPitchAngle = cameraPitchAngle + 10;
   }
   
   public float getX() {
      return cameraX;
   }
   
   public float getY() {
      return cameraY;
   }
   
   public float getZ() {
      return cameraZ;
   }
   
   public float getPanAngle() {
      return cameraPanAngle;
   }
   
   public float getPitchAngle() {
      return cameraPitchAngle;
   }

}