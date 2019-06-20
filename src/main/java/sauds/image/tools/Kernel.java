
package sauds.image.tools;

/**
 *
 * @author saud
 */
public class Kernel {
    
    private final int width;
    private final int halfWidth;
    private final int height;
    private final int halfHeight;
    private final int[] k;
    
    public Kernel(int width, int... values) {
	if(values.length % width != 0)
	    throw new RuntimeException("Length of values is not a multiple of width, cannot make a rectangle");
	
	this.width = width;
	this.height = values.length/width;
	
	if(width%2==0 || height%2==0)
	    throw new RuntimeException("Kernel must have an odd numbered width and height");
	
	this.halfWidth = width/2;
	this.halfHeight = height/2;
	this.k = values;
    }
    
    public static Kernel boxBlur(int width) {
	Kernel out = new Kernel(width, new int[width*width]);
	for(int i=0; i<out.k.length; i++) {
	    out.k[i] = 1;
	}
	return out;
    }
    
    public int getWidth() {
	return width;
    }
    public int getHW() {
	return halfWidth;
    }
    public int getHeight() {
	return height;
    }
    public int getHH() {
	return halfHeight;
    }
    
    public int getC(int xFromCenter, int yFromCenter) {
	return get(xFromCenter+halfWidth, yFromCenter+halfHeight);
    }
    
    public int get(int x, int y) {
	return k[y*width + x]; 
    }
    
}
