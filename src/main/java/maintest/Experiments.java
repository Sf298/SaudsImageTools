package maintest;


import java.awt.*;
import java.io.File;
import java.io.IOException;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import sauds.image.tools.*;
import sauds.image.tools.Image;
import sauds.toolbox.multiprocessing.tools.MPT;
import sauds.toolbox.multiprocessing.tools.MTPListRunnable;

import static java.util.Arrays.asList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author saud
 */
public class Experiments {

	public static void main(String[] args) throws Exception {
		colorRemovalExpt2(	"/home/saud/Downloads/Unsaved Image 1.jpg",
							"/home/saud/Downloads/profile-photo.png");
	}

	private static void colorRemovalExpt1() throws IOException {
		LineGraphComponenet hist = new LineGraphComponenet();
		ImageIcon imgIcon;
		
		System.out.println("running");
		Image tempIm = ImageRaster.create(new File("C:\\Users\\demon\\Desktop\\TEMP\\Temp1.png"));
        Image im = Operations.resize(tempIm, null, 250);
		
		JFrame frame = new JFrame("Histogram test");
			
			JPanel mainPanel = new JPanel(new BorderLayout());
				
				JPanel rgbPanel = new JPanel(new GridLayout(3, 1));
					
					JSlider rSlider = new JSlider(0, 255);
					rgbPanel.add(rSlider);
					
					JSlider gSlider = new JSlider(0, 255);
					rgbPanel.add(gSlider);
					
					JSlider bSlider = new JSlider(0, 255);
					rgbPanel.add(bSlider);
					
				mainPanel.add(rgbPanel, BorderLayout.NORTH);
					
				//hist.setPreferredSize(new Dimension(100, 100));
				mainPanel.add(hist, BorderLayout.CENTER);
				
				imgIcon = new ImageIcon(im.toBufferedImage());
				mainPanel.add(new JLabel(imgIcon), BorderLayout.SOUTH);
				
			frame.add(mainPanel);
			
		frame.setSize(1000, 1000);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		
		ChangeListener updateListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int r = rSlider.getValue();
				int g = gSlider.getValue();
				int b = bSlider.getValue();
				System.out.println(r+", "+g+", "+b);
                Image out = Operations.concatChannels(asList(
                        Operations.add(new ImageROI(im, 0, -1, 0, -1, 0, 1), r),
                        Operations.add(new ImageROI(im, 0, -1, 0, -1, 1, 1), g),
                        Operations.add(new ImageROI(im, 0, -1, 0, -1, 2, 1), b)
                ));


				imgIcon.setImage(out.toBufferedImage());
				
				int[][] hists = out.getHistogram();
				hist.clear();
				hist.addLine(intArr2doubleArr(hists[0]), Color.RED);
				hist.addLine(intArr2doubleArr(hists[1]), Color.GREEN);
				hist.addLine(intArr2doubleArr(hists[2]), Color.BLUE);
				
				frame.repaint();
			}
		};
		rSlider.addChangeListener(updateListener);
		gSlider.addChangeListener(updateListener);
		bSlider.addChangeListener(updateListener);
		
		frame.setVisible(true);
	}
	
	
	private static void colorRemovalExpt2(String f1, String f2) throws IOException {
		System.out.println("running");
		JFrame frame = new JFrame("Histogram test");
			
			JPanel mainPanel = new JPanel(new GridLayout(2, 2));

				Image im1 = ImageRaster.create(new File(f1));
                im1 = Operations.resize(im1, null, 250);
				ImageIcon imgIcon1 = new ImageIcon(im1.toBufferedImage());
				mainPanel.add(new JLabel(imgIcon1));

                Image im2 = ImageRaster.create(new File(f2));
                im2 = Operations.resize(im2, null, 250);
				ImageIcon imgIcon2 = new ImageIcon(im2.toBufferedImage());
				mainPanel.add(new JLabel(imgIcon2));
					
				LineGraphComponenet hist1 = new LineGraphComponenet();
				mainPanel.add(hist1, BorderLayout.CENTER);
				int[][] hists1 = im1.getHistogram();
				hist1.addLine(intArr2doubleArr(hists1[0]), Color.RED);
				hist1.addLine(intArr2doubleArr(hists1[1]), Color.GREEN);
				hist1.addLine(intArr2doubleArr(hists1[2]), Color.BLUE);
				
				LineGraphComponenet hist2 = new LineGraphComponenet();
				mainPanel.add(hist2, BorderLayout.CENTER);
				int[][] hists2 = im2.getHistogram();
				hist2.addLine(intArr2doubleArr(hists2[0]), Color.RED);
				hist2.addLine(intArr2doubleArr(hists2[1]), Color.GREEN);
				hist2.addLine(intArr2doubleArr(hists2[2]), Color.BLUE);
				
			frame.add(mainPanel);
			
		frame.setSize(1000, 1000);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		
		
		
		frame.setVisible(true);
	}
	
	public static double[] intArr2doubleArr(int[] arr) {
		double[] out = new double[arr.length];
		MPT.run(8, arr, new MTPListRunnable<Integer>() {
			@Override
			public void iter(int procID, int idx, Integer oldVal) {
				out[idx] = oldVal;
			}
		});
		return out;
	}

}
