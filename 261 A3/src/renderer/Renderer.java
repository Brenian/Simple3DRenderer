package renderer;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

import javax.imageio.ImageIO;

/**
 * Primary class, handles all major operations responsible for rendering the
 * image
 * 
 * 
 * @author Brendan
 *
 */
public class Renderer extends GUI {

	private static ArrayList<Polygon> polygons = new ArrayList<Polygon>(10);
	private Vector3D light;
	private static float amLight = (float) 0.1;

	private String fileName;

	private static Color[][] screen;
	private static int[][] zBuffer;

	private static BufferedImage image;

	public Renderer() {
		screen = new Color[CANVAS_HEIGHT][CANVAS_WIDTH];
		zBuffer = new int[CANVAS_HEIGHT][CANVAS_WIDTH];
	}

	@Override
	protected void onLoad(File file) {
		polygons.clear();
		screen = new Color[CANVAS_HEIGHT][CANVAS_WIDTH];
		zBuffer = new int[CANVAS_HEIGHT][CANVAS_WIDTH];
		try {
			BufferedReader b = new BufferedReader(new InputStreamReader(
					new FileInputStream(file)));
			String line;
			fileName = file.getName().substring(0,
					file.getName().lastIndexOf('.'));

			// Light source
			line = b.readLine();
			Scanner sc = new Scanner(line);
			light = new Vector3D(sc.nextFloat(), sc.nextFloat(), sc.nextFloat());
			sc.close();

			// Polygons in the file
			while ((line = b.readLine()) != null) {
				Scanner scan = new Scanner(line);
				polygons.add(new Polygon(line));
				scan.close();
			}
			b.close();

		} catch (IOException e) {
			System.err.println(e);
		}
		// printAllPolys();
		transformPolys(getPolyLeft(), getPolyRight(), getPolyTop(),
				getPolyBot());
		image = render();
		// create image, display and save
		save(fileName);

	}

	private void transformPolys(int left, int right, int top, int bot) {
		Transform identityTF = Transform.identity();
		Transform translationTF = Transform.newTranslation((left * -1) + 50,
				(top * -1) + 50, 0);
		Transform newTF = identityTF.compose(translationTF);
		for (Polygon poly : polygons) {
			poly.apply(newTF);
		}
	}

	private int getPolyTop() {
		int top = Integer.MAX_VALUE;
		for (Polygon poly : polygons) {
			top = Math.min(top, poly.getMinY());
		}
		return top;
	}

	private int getPolyLeft() {
		int left = Integer.MAX_VALUE;
		for (Polygon poly : polygons) {
			left = Math.min(left, poly.getMinX());
		}
		return left;
	}

	private int getPolyBot() {
		int top = Integer.MIN_VALUE;
		for (Polygon poly : polygons) {
			top = Math.max(top, poly.getMinY());
		}
		return top;
	}

	private int getPolyRight() {
		int left = Integer.MIN_VALUE;
		for (Polygon poly : polygons) {
			left = Math.max(left, poly.getMinX());
		}
		return left;
	}

	@Override
	protected void onKeyPress(KeyEvent ev) {
		//WASD to rotate object
		char c = ev.getKeyChar();
		Transform t = Transform.identity();
		if (c == 'w') {
			t = Transform.newYRotation(1f);
		} else if (c == 's') {
			t = Transform.newYRotation(-1f);
		} else if (c == 'a') {
			t = Transform.newXRotation(-1f);
		} else if (c == 'd') {
			t = Transform.newXRotation(1f);
		}
		for (Polygon poly : polygons) {
			poly.apply(t);
		}
		transformPolys(getPolyLeft(), getPolyRight(), getPolyTop(),
				getPolyBot());
		//IJKLNM to move light source
		//IJ up/down
		//KL left/right
		//NM in/out
		if (c == 'i') {
			light = new Vector3D(light.getX(), light.getY()+1, light.getZ());
		} else if (c == 'k') {
			light = new Vector3D(light.getX(), light.getY()-1, light.getZ());
		} else if (c == 'j') {
			light = new Vector3D(light.getX()+1, light.getY(), light.getZ());
		} else if (c == 'l') {
			light = new Vector3D(light.getX()-1, light.getY(), light.getZ());
		} else if (c == 'm') {
			light = new Vector3D(light.getX(), light.getY(), light.getZ()+0.5f);
		} else if (c == 'n') {
			light = new Vector3D(light.getX(), light.getY(), light.getZ()-0.5f);
		}
	}

	private void initZBuffer() {
		for (int i = 0; i < CANVAS_HEIGHT; i++) {
			for (int j = 0; j < CANVAS_WIDTH; j++) {
				screen[i][j] = Color.gray;
				zBuffer[i][j] = Integer.MAX_VALUE;
			}
		}
	}

	@Override
	protected BufferedImage render() {
		/*
		 * This method should put together the pieces of your renderer, as
		 * described in the lecture. This will involve calling each of the
		 * static method stubs in the Pipeline class, which you also need to
		 * fill in.
		 */
		initZBuffer();
		for (Polygon poly : polygons) {
			if (poly.facing()) {
				poly.shading(light, amLight);
				EdgeList[] edgelist = poly.edgeList();
				int minY = poly.getMinY();
				Color c = poly.getColour();

				for (int j = 0; j < edgelist.length && edgelist[j] != null; j++) {
					int y = minY + j;

					// edgelist[j].print();
					// System.out.println(edgelist[j].getLeftX());
					int x = Math.round(edgelist[j].getLeftX());
					int z = Math.round(edgelist[j].getLeftZ());

					int mz = Math
							.round((edgelist[j].getRightZ() - edgelist[j]
									.getLeftZ())
									/ (edgelist[j].getRightX() - edgelist[j]
											.getLeftX()));

					// System.out.println("Left X: "+ x + " RightX: "+
					// edgelist[j].getRightX());
					while (x <= edgelist[j].getRightX()) {
						if (z < zBuffer[x][y]) {
							// System.out.println("does this work?");
							zBuffer[x][y] = z;
							screen[x][y] = c;
						}
						x++;
						z += mz;
					}
				}
			}
		}
		return convertBitmapToImage(screen);
	}

	/**
	 * Converts a 2D array of Colors to a BufferedImage. Assumes that bitmap is
	 * indexed by column then row and has imageHeight rows and imageWidth
	 * columns. Note that image.setRGB requires x (col) and y (row) are given in
	 * that order.
	 */
	private BufferedImage convertBitmapToImage(Color[][] bitmap) {
		BufferedImage image = new BufferedImage(CANVAS_WIDTH, CANVAS_HEIGHT,
				BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < CANVAS_WIDTH; x++) {
			for (int y = 0; y < CANVAS_HEIGHT; y++) {
				image.setRGB(x, y, bitmap[x][y].getRGB());
			}
		}
		return image;
	}

	/**
	 * writes a BufferedImage to a file of the specified name
	 */
	public void save(String fName) {
		try {
			fName += "Output.png";
			ImageIO.write(image, "png", new File(fName));
		} catch (IOException e) {
			System.out.println("Image saving failed: " + e);
		}
	}

	/*
	 * Prints out a list of all the polygons in the scene along with their
	 * individual properties
	 */
	@SuppressWarnings("unused")
	private void printAllPolys() {
		System.out.println("Number of polygons: " + polygons.size());

		for (Polygon poly : polygons) {
			System.out.println(poly.toString());
		}
	}

	public static void main(String[] args) {
		new Renderer();
	}
}

// code for comp261 assignments
