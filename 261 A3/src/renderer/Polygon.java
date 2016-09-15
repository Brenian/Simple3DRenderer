package renderer;

import java.awt.Color;
import java.util.Scanner;

public class Polygon {
	private Vector3D[] vertices = new Vector3D[3];
	private Color reflectivity;
	private Vector3D normal;
	private boolean facing = true;
	private BoundingBox boundingBox;
	private Color colour;

	/*
	 * Constructor using a string/line of the file
	 */
	public Polygon(String s) {
		// System.out.println("s=" + s);
		Scanner sc = new Scanner(s);
		for (int v = 0; v < 3; v++) {
			vertices[v] = new Vector3D(sc.nextFloat(), sc.nextFloat(),
					sc.nextFloat());
		}
		reflectivity = new Color(sc.nextInt(), sc.nextInt(), sc.nextInt());

		calculateNormal();
	}

	public int getMinY() {
		return Math.round(boundingBox().getY());
	}

	public int getMinX() {
		return Math.round(boundingBox().getX());
	}

	public int getMaxY() {
		return Math.round(boundingBox().getY()+boundingBox.getHeight());
	}

	public int getMaxX() {
		return Math.round(boundingBox().getX()+boundingBox.getWidth());
	}
	
	public String boundsToString(){
		return ("L: "+getMinY()+" T: "+getMinX()+" R: "+getMaxX()+" B: "+getMaxY());
	}

	/*
	 * Calculates the bounds of the polygon
	 */
	private BoundingBox boundingBox() {
		float minX = Math.min(Math.min(vertices[0].x, vertices[1].x),
				vertices[2].x);
		float minY = Math.min(Math.min(vertices[0].y, vertices[1].y),
				vertices[2].y);
		float maxX = Math.max(Math.max(vertices[0].x, vertices[1].x),
				vertices[2].x);
		float maxY = Math.max(Math.max(vertices[0].y, vertices[1].y),
				vertices[2].y);
		boundingBox = new BoundingBox(minX, minY, (maxX - minX), (maxY - minY));
		return boundingBox;
	}

	/*
	 * Updates/calculates the normal
	 */
	private void calculateNormal() {
		normal = ((vertices[1].minus(vertices[0])).crossProduct(vertices[2]
				.minus(vertices[1]))).unitVector();

		// update facing boolean
		if (normal.z > 0)
			facing = false;
		else
			facing = true;
	}

	/*
	 * Calculates the shading of the polygon
	 */
	public void shading(Vector3D lightSource, float ambientLight) {
		float reflect = ambientLight;
		if (normal.cosTheta(lightSource) > 0)
			reflect = ambientLight + normal.cosTheta(lightSource);

		int r = checkColourRange((int) (reflectivity.getRed() * reflect));
		int g = checkColourRange((int) (reflectivity.getGreen() * reflect));
		int b = checkColourRange((int) (reflectivity.getBlue() * reflect));

		colour = new Color(r, g, b);
	}

	/*
	 * Used to ensure the range of values for rgb are between 0 and 255
	 */
	private int checkColourRange(int x) {

		// System.out.println("Colour range being checked: " + x);

		if (x <= 0)
			x = 0;

		if (x >= 255)
			x = 255;

		return x;
	}

	public boolean facing() {
		return facing;
	}

	public Color getColour() {
		return colour;
	}

	public String toString() {
		StringBuilder s = new StringBuilder("Poly: ");

		for (int i = 0; i < 3; i++) {
			s.append(vertices[i].toString()).append("\t");
		}

		s.append(reflectivity).append("\t");
		s.append(normal.toString()).append("\t");
		s.append(facing);

		return s.toString();
	}

	public EdgeList[] edgeList() {
		boundingBox();
		EdgeList[] e = new EdgeList[(int) (boundingBox.getHeight() + 1)];

		for (int i = 0; i < 3; i++) {
			// System.out.println("Edgelist vertices " +i + " "+ (i+1)%3);
			Vector3D va = vertices[i];
			Vector3D vb = vertices[(i + 1) % 3];

			// System.out.println("va.y " + va.y + " vb.y " + vb.y);

			if (va.y > vb.y) {
				vb = va;
				va = vertices[(i + 1) % 3];
			}

			// System.out.println("va.y " + va.y + " vb.y " + vb.y);

			float mx = (vb.x - va.x) / (vb.y - va.y);
			float mz = (vb.z - va.z) / (vb.z - va.z);
			float x = va.x;
			float z = va.z;

			int j = Math.round(va.y) - Math.round(boundingBox.getY());
			int maxj = Math.round(vb.y) - Math.round(boundingBox.getY());

			while (j < maxj) {
				if (e[j] == null) {
					e[j] = new EdgeList(x, z);
				} else {
					e[j].add(x, z);

				}
				j++;
				x += mx;
				z += mz;
			}

		}
		return e;

	}

	public void apply(Transform t) {
		for (int v = 0; v < 3; v++)
			vertices[v] = (t.multiply(vertices[v]));
		normal = null;
		calculateNormal();
	}
}
