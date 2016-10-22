package a2;


public class Camera {
	private float cameraX, cameraY, cameraZ;
   private float cameraHorAngle;
   private float cameraVertAngle;
	
	
	public Camera(float cameraX, float cameraY, float cameraZ) {
		this.cameraX = cameraX;
		this.cameraY = cameraY;
		this.cameraZ = cameraZ;
      cameraHorAngle = 0;
      cameraVertAngle = 0;

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
      cameraHorAngle = cameraHorAngle - 10;
   }
   
   public void panRight() {
      cameraHorAngle = cameraHorAngle + 10;
   }
   
   public void pitchUp() {
      cameraVertAngle = cameraVertAngle - 10;
   }
   
   public void pitchDown() {
      cameraVertAngle = cameraVertAngle + 10;
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
   
   public float getHorAngle() {
      return cameraHorAngle;
   }
   
   public float getVertAngle() {
      return cameraVertAngle;
   }

}