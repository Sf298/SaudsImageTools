/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maintest;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.JFrame;
import sauds.toolbox.multiprocessing.tools.MPT;

/**
 *
 * @author saud
 */
public class PrivacyTest {
	
	public static void main(String[] args) throws IOException, InterruptedException {
		/*Img im = Img.createNew(new File("C:\\Users\\demon\\Desktop\\Long Term Store\\Dx7UUM2XQAA9cf7.jpg"));
		MyCanvas mc =  new MyCanvas(im, 4);
		
		JFrame frame = new JFrame();
		frame.add(mc);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(500, 500);
		frame.setVisible(true);
		
		FrameTimer ft = new FrameTimer(30) {
			@Override
			public void frameCall(int frameNo, double lagMs) {
				frame.repaint();
			}
		};
		ft.start();*/
	}
	
	private static class MyCanvas extends JComponent {
		
		/*private Image im;
		private Image[][] bim;
		private int fNo = 0;
		private int gridSize;
		private int[][] w;
		private int[][] h;
		
		public MyCanvas(Img img, int gridSize) {
			this.gridSize = gridSize;
			w = MPT.getLRs(gridSize, img.getWidth());
			h = MPT.getLRs(gridSize, img.getHeight());
			im = img.toBufferedImage();
			
			bim = new Image[gridSize][gridSize];
			for(int i=0; i<gridSize; i++) {
				for(int j=0; j<gridSize; j++) {
					bim[i][j] = img
							.crop(w[j][0], h[i][0], w[j][1]-w[j][0], h[i][1]-h[i][0])
							.rescale(0.05, 0.05)
							.resize(w[j][1]-w[j][0], h[i][1]-h[i][0])
							.toBufferedImage();
				}
			}
		}
		
		@Override
		public void paint(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			g2.drawImage(im, 0, 0, null);
			
			g2.setColor(Color.WHITE);
			for(int i=0; i<gridSize; i++) {
				for(int j=0; j<gridSize; j++) {
					if((i+j+fNo)%3 != 0)
					//g2.fillRect(w[j][0], h[i][0], w[j][1]-w[j][0], h[i][1]-h[i][0]);
					g2.drawImage(bim[i][j], w[j][0], h[i][0], null);
				}
			}
			
			fNo++;
		}*/
		
	}
	
	
	public static void println(Object... args) {
			System.out.print(args[0]);
		for (int i=1; i<args.length; i++) {
			System.out.print(", ");
			System.out.print(args[i]);
		}
		System.out.println();
	}
	
}
