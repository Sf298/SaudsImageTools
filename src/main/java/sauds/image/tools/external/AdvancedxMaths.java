/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sauds.image.tools.external;

/**
 *
 * @author saud
 */
public class AdvancedxMaths {
	
	/**
	 * Calculate the eigen vectors for the given 2x2 matrix
	 * @param a
	 * @param b
	 * @param c
	 * @param d
	 * @param eigenValues the eigen values as calculated by eigenValues(double, double, double, double).
	 * @return 
	 */
	public static double[][] eigenVectors(double a, double b, double c, double d, double[] eigenValues) {
		//<editor-fold defaultstate="collapsed" desc="working out">
		/*
		https://www.emathhelp.net/calculators/linear-algebra/eigenvalue-and-eigenvector-calculator/
		a b   e   ae+bf
		c d * f = ce+df
		
		a b   E   aE+bF = 0
		c d * F = cE+dF = 0
		
		aE+bF-cE-dF = 0
		aE-cE+bF-dF = 0
		E(a-c)+F(b-d) = 0
		E(a-c) = -F(b-d)
		E = -F(b-d)/(a-c)
		E = F(d-b)/(a-c)
		*/
		//</editor-fold>
		
		double[] ev = (eigenValues==null) ? eigenValues(a,b,c,d) : eigenValues;
		double[][] out = new double[ev.length][];
		for(int i=0; i<ev.length; i++) {
			double[] rr = rref(a-ev[i], b, c, d-ev[i]);
			double[] tempOut = new double[] {(rr[3]-rr[1])/(rr[0]-rr[2]), 1};
			out[i] = tempOut;
		}
		return out;
	}
	
	/**
	 * Calculate the eigen values for the given 2x2 matrix
	 * @param a
	 * @param b
	 * @param c
	 * @param d
	 * @return 
	 */
	public static double[] eigenValues(double a, double b, double c, double d) {
		double[] eigenVals = solveQuadratic(1, -a-d, a*d-c*b);
		return eigenVals;
	}
	
	/**
	 * Solves equations in the form of ax^2 + bx + c
	 * @param a
	 * @param b
	 * @param c
	 * @return 
	 */
	public static double[] solveQuadratic(double a, double b, double c) {
        double determinant = b * b - 4 * a * c;

        // condition for real and different roots
        if(determinant > 0) {
			return new double[] {
				(-b + Math.sqrt(determinant)) / (2 * a),
				(-b - Math.sqrt(determinant)) / (2 * a)
			};
        }
        // Condition for real and equal roots
        else if(determinant == 0) {
			return new double[] {
				-b / (2 * a),
				-b / (2 * a)
			};
        }
        // If roots are not real
        else {
			throw new IllegalArgumentException("Result is imaginary");
			/*return new double[] {
				-b / (2 * a),
				Math.sqrt(-determinant) / (2 * a)
			};*/
        }
	}
	
	public static double[] rref(int width, int height, double... data) {
		double[] out = new double[data.length];
		for(int col=0; col<width; col++) {
			out[col] = data[col];
		}
		for(int row=1; row<height; row++) {
			double mult = data[to1D(0,row, width)] / data[to1D(0,0, width)];
			System.out.println(mult);
			for(int col=1; col<width; col++) {
				out[to1D(col, row, width)] = data[to1D(col,row, width)] - data[to1D(col,0, width)]*mult;
			}
		}
		return out;
	}
	public static double[] rref(double a, double b, double c, double d) {
		return new double[] {a, b, 0, d - b*(c/a)};
	}
	
	private static int to1D(int x, int y, int width) {
		return y*width + x;
	}
	
	/**
	 * Convert radians to degrees.
	 * @param radians
	 * @return 
	 */
	public static double rad2deg(double radians) {
		return radians/Math.PI*180;
	}
	
	/**
	 * Calculates the covariance of a set of coordinates.
	 * @param data [[x1,y1,z1, ...], [x2,y2,z2, ...], ...]
	 * @return 
	 */
	public static double covariance(double[][] data) {
		if(data.length == 1) return 1;
		double[] means = new double[data[0].length];
		for(int p=0; p<data.length; p++) {
			for(int dim=0; dim<data[p].length; dim++) {
				means[dim] += data[p][dim];
			}
		}
		for(int dim=0; dim<data[0].length; dim++) {
			means[dim] /= data.length;
		}
		
		
		double finalSum = 0;
		for(int p=0; p<data.length; p++) {
			double tempProd = 1;
			for(int dim=0; dim<data[p].length; dim++) {
				tempProd *= data[p][dim] - means[dim];
			}
			finalSum += tempProd;
		}
		return finalSum/(data.length-1);
	}
	
	public static double[] rotateTransform(double x, double y, double rad) {
		return new double[] {
			x*Math.cos(rad) + y*Math.sin(rad),
			-x*Math.sin(rad) + y*Math.cos(rad)
		};
	}
	
}
