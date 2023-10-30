/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maintest;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author saud
 */
public class LineGraphComponenet extends JComponent {
	
	private ArrayList<double[]> x = new ArrayList<>();
	private ArrayList<double[]> y = new ArrayList<>();
	private ArrayList<Color> c = new ArrayList<>();
	
	public void addLine(double[] y, Color c) {
		addLine(null, y, c);
	}
	public void addLine(double[] x, double[] y, Color c) {
		if(x == null) {
			x = new double[y.length];
			for(int i=0; i<y.length; i++) {
				x[i] = i;
			}
		}
		if(c == null)
			c = Color.BLACK;
		this.x.add(x);
		this.y.add(y);
		this.c.add(c);
	}
	
	public void clear() {
		x.clear();
		y.clear();
		c.clear();
	}
	
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		Rectangle textSize = getStringBounds(g2, "1000.00", 0, 0);
		int marginN = 30;
		int marginS = (textSize.height+5)*x.size()+5;
		int marginE = 30;
		int marginW = (textSize.width+5)*y.size()+5;
		int w = Math.abs(getWidth());
		int h = Math.abs(getHeight());
		g2.setColor(Color.LIGHT_GRAY);
		g2.fillRect(0, 0, w, h);
		w = w-marginE-marginW;
		h = h-marginN-marginS;
		g2.setColor(Color.BLACK);
		g2.drawLine(marginW, getHeight()-marginS, getWidth()-marginE, getHeight()-marginS);
		g2.drawLine(marginW, getHeight()-marginS, marginW, marginN);
		
		int minGapW = 50;
		int minGapH = 25;
		int[][] lrWs = getLRs(w/minGapW, w);
		int[][] lrHs = getLRs(h/minGapH, h);
		
		// gridlines
		g2.setColor(Color.GRAY);
		for(int j=0; j<lrWs.length; j++) {
			if(j==0)
				g2.drawLine(lrWs[0][0], getHeight()-marginS, lrWs[0][0], marginN);
			g2.drawLine(lrWs[j][1]+marginW, getHeight()-marginS, lrWs[j][1]+marginW, marginN);
		}
		for(int j=0; j<lrHs.length; j++) {
			if(j==0)
				g2.drawLine(marginW, lrHs[0][0], getWidth()-marginE, lrHs[0][0]);
			g2.drawLine(marginW, lrHs[j][1], getWidth()-marginE, lrHs[j][1]);
		}
		
		// numbers
		for(int i=0; i<x.size(); i++) {
			double maxX = Double.MIN_VALUE, minX=Double.MAX_VALUE;
			double maxY = Double.MIN_VALUE, minY=Double.MAX_VALUE;
			for(int j=0; j<x.get(i).length; j++) {
				if(x.get(i)[j] > maxX) maxX = x.get(i)[j];
				if(x.get(i)[j] < minX) minX = x.get(i)[j];
				if(y.get(i)[j] > maxY) maxY = y.get(i)[j];
				if(y.get(i)[j] < minY) minY = y.get(i)[j];
			}
			
			g2.setColor(c.get(i));
			for(int j=1; j<x.get(i).length; j++) {
				int x1 = (int) remapToScale(x.get(i)[j-1], maxX, minX, w, 0)+marginW;
				int x2 = (int) remapToScale(x.get(i)[j], maxX, minX, w, 0)+marginW;
				int y1 = (int) remapToScale(y.get(i)[j-1], maxY, minY, h, 0)+marginS;
				int y2 = (int) remapToScale(y.get(i)[j], maxY, minY, h, 0)+marginS;
				g2.drawLine(x1, getHeight()-y1, x2, getHeight()-y2);
			}
			
			for(int j=0; j<lrWs.length; j++) {
				if(j==0) {
					String str = String.format("%.2f", x.get(i)[0]);
					Rectangle bounds = getStringBounds(g2, str, 0, 0);
					g2.drawString(str, marginW-bounds.width/2, getHeight()-marginS+(i+1)*(bounds.height+5));
				}
				double notchVal = x.get(i)[remapToScale(lrWs[j][1], w, 0, x.get(i).length-1, 0)];
				String str = String.format("%.2f", notchVal);
				Rectangle bounds = getStringBounds(g2, str, 0, 0);
				g2.drawString(str, lrWs[j][1]+marginW-bounds.width/2, getHeight()-marginS+(i+1)*(bounds.height+5));
			}
			for(int j=0; j<lrHs.length; j++) {
				if(j==0) {
					double notchVal = remapToScale(lrHs[0][0], h, 0, maxY, minY);
					String str = String.format("%.2f", notchVal);
					g2.drawString(str, i*(textSize.width+5)+5, getHeight()-(marginS+lrHs[j][0])+textSize.height/2);
				}
				double notchVal = remapToScale(lrHs[j][1], h, 0, maxY, minY);
				String str = String.format("%.2f", notchVal);
				g2.drawString(str, i*(textSize.width+5)+5, getHeight()-(marginS+lrHs[j][1])+textSize.height/2);
			}
		}
	}
	private Rectangle getStringBounds(Graphics2D g2, String str, float x, float y) {
        FontRenderContext frc = g2.getFontRenderContext();
        GlyphVector gv = g2.getFont().createGlyphVector(frc, str);
        return gv.getPixelBounds(null, x, y);
    }
	
	/**
     * Gets the range of elements to assign to an individual processor/thread.
     * @param proc The index of the current processor
     * @param nprocs The number of processors available.
     * @param maxI Number of elements to partition.
     * @return An array of the left (inclusive) and right (exclusive) indexes respectively.
     */
    private static int[] getLR(int proc, int nprocs, int maxI) {
        // calc ileft and iright
        int a = maxI/nprocs; // cols per process
        int rem = maxI%nprocs; // remaining cols
        int left = proc * a + ((proc<rem) ? proc : rem); // is 0 based
        int right = left + a + ((proc<rem) ? 1 : 0); // right is exclusive
        return new int[] {left, right};
    }
    /**
     * Generates the left and right indexes for each of the threads available.
     * @param nprocs The number of processors available.
     * @param maxI Number of elements to partition.
     * @return Returns an array with values in the range of the LR values
     */
    private static int[][] getLRs(int nprocs, int maxI) {
		int[][] out = new int[nprocs][];
		for(int i=0; i<out.length; i++) {
			out[i] = getLR(i, nprocs, maxI);
		}
		return out;
    }
	private static double remapToScale(double oVal, double oMax, double oMin, double nMax, double nMin) {
		return ((oVal-oMin)/(oMax-oMin))*(nMax-nMin)+nMin;
	}
	private static int remapToScale(int oVal, int oMax, int oMin, int nMax, int nMin) {
		return ((oVal-oMin)*(nMax-nMin))/(oMax-oMin)+nMin;
	}
	
	public void plot() {
		plot(this);
	}
	public static void plot(double[] y) {
		plot(null, y);
	}
	public static void plot(double[] x, double[] y) {
		JPanel mainPanel = new JPanel(new BorderLayout());
		//lgc.setPreferredSize(new Dimension(400, 600));
		LineGraphComponenet lgc = new LineGraphComponenet();
		lgc.addLine(x, y, Color.BLACK);
		mainPanel.add(lgc);
		
		JFrame frame = new JFrame();
		frame.setSize(1000, 450);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(mainPanel);
		frame.setVisible(true);
	}
	public static void plot(LineGraphComponenet lgc) {
		JPanel mainPanel = new JPanel(new BorderLayout());
		//lgc.setPreferredSize(new Dimension(400, 600));
		mainPanel.add(lgc);
		
		JFrame frame = new JFrame();
		frame.setSize(1000, 450);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(mainPanel);
		frame.setVisible(true);
	}
	
}
