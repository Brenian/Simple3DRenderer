package renderer;
public class EdgeList {
	// 0 is left x, 1 is left z, 2 is right x and 3 is right z
	public float[] coordinates = new float[4];

	/*
	 * New Edgelist adds the first edge coordinates
	 */
	public EdgeList(float x, float z) {
		coordinates[0] = x;
		coordinates[1] = z;
	}

	/*
	 * Adds the right hand edge coordinates
	 */
	public void add(float x, float z) {
		//left edge
		if(x < coordinates[0]){
			coordinates[2] = coordinates[0];
			coordinates[3] = coordinates[1];
			coordinates[0] = x;
			coordinates[1] = z;			
		} else{	
			coordinates[2] = x;
			coordinates[3] = z;
			
		}
	}

	public float getLeftX() {
		return coordinates[0];
	}

	public float getLeftZ() {
		return coordinates[1];
	}

	public float getRightX() {
		return coordinates[2];
	}

	public float getRightZ() {
		return coordinates[3];
	}

	public void print() {
		for (int i = 0; i < coordinates.length; i++) {

			System.out.println(coordinates[i]);
		}
	}
}
