package maintest;


import sauds.image.tools.Img;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JFrame;
import javax.swing.JLabel;
import sauds.image.tools.external.AdvancedxMaths;
import sauds.image.tools.Blob;
import sauds.image.tools.external.FrameTimer;
import sauds.image.tools.ImgInterface;
import sauds.image.tools.ImgViewer;
import sauds.image.tools.Kernel;
import sauds.image.tools.external.ProfilingTools;
import sauds.image.tools.ROI;
import sauds.toolbox.multiprocessing.tools.MPT;
import sauds.toolbox.multiprocessing.tools.MTPListRunnable;
import sauds.toolbox.timer.Timer;

/**
 *
 * @author saud
 */
public class Test {
    
    public static void main(String[] args) throws IOException, InterruptedException {
		System.out.println("waiting");
		//System.out.println(Arrays.toString(AdvancedxMaths.rref(-3+Math.sqrt(11), 2, 1, 3+Math.sqrt(11))));
		//testImgIconWrapper();
		testRegionBlur();
    }
    
    public static void test1b() throws IOException {
		int start = 0;
		int stop = 10;
		int step = 1;
		byte[] bb = new byte[10];

		MPT.run(4, start, stop, step, (MTPListRunnable) (int procID, int idx, Object val) -> {
			System.out.println(procID + " - " + idx);
			bb[idx] = (byte) idx;
		});
		System.out.println();
		MPT.run(4, bb, (int procID, int idx, Byte val) -> {
			System.out.println(procID + " - " + idx + " - " + val);
		});
    }
    
	public static void testRunOp() throws IOException {
		//Img im2 = new Img(new File("C:\\Users\\demon\\Desktop\\New Bitmap Image.jpg"));
		Img im1 = Img.createNew(new File("C:\\Users\\demon\\Desktop\\New Bitmap Image.jpg"));
		//im1.show();
		Img out = im1.runOp(new Img.Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return prevVal>>>1;
			}
		});
		//out.show();
    }
    
    public static void test2() throws IOException {
		Img im1 = Img.createNew(new File("C:\\Users\\demon\\Desktop\\New Bitmap Image.jpg"));
		/*ArrayList<Img> lap = im1.laplacianPyramid(4);
		//Img2.showImgs(lap);
		Img.showImgs(Img.laplacianPyramid(lap));*/
    }
	
    private static void test3() throws IOException {
		Img im1 = Img.createNew(new File("C:\\Users\\demon\\Desktop\\2019-03-29 23.04.25.png"));
		for(int i = 0; i < 10; i++) {
			Timer t1 = new Timer("T1");
			for (int j = 0; j < 20; j++) {
				im1.add(j).sub(j);
			}
			t1.print();
		}
    }
	
    private static void testToGrey() throws IOException {
		ImgInterface im1 = Img.createNew(new File("C:\\Users\\demon\\Desktop\\2019-03-29 23.04.25.png"));
		im1 = im1.downScale2x().downScale2x().downScale2x();
		im1.show();
		im1 = im1.toGrey();
		im1.show();
    }
    
    private static void test6() throws IOException {
		Img.threadCount = 1;
		ImgInterface im = Img.createNew(new File("C:\\Users\\demon\\Desktop\\2019-03-29 23.04.25.png")).downScale2x().downScale2x().downScale2x();
		ImgInterface im2 = im.downScale2x().downScale2x();
		int targetW = im2.getWidth(), targetH = im2.getHeight();
		//int targetW = im.getWidth()+1, targetH = im.getHeight()+1;
		//im.rescale(targetW, targetH);

		/*Timer t1 = new Timer("T1");
		for (int i = 0; i < 10; i++) {
			im.downScale2x();
		}
		t1.print();

		Timer t2 = new Timer("T2");
		for (int i = 0; i < 10; i++) {
			im.rescale(targetW, targetH);
		}
		t2.print();*/

		/*Img im4 = im.downScale2x().downScale2x();
		Img im3 = im.rescale(im4.getWidth(), im4.getHeight());
		for(int i=0; i<im3.getChannels(); i++)
			im3.convolve(Kernel.boxBlur(9), i, Img.BORDER_IGNORE, 1, -1);
		}
		Img.showImgs(im4.upscale(4), im3.upscale(4));*/

		// downscale
		//Img2.showImgs(im.downScale2x().downScale2x().upscale(4),
		//	im.rescale(im.getWidth()/4, im.getHeight()/4).upscale(4));


		// upscale
		//Img2 im3 = im.downScale2x().downScale2x();
		//Img2.showImgs(im3.upscale(4), im3.rescale(im3.getWidth()*4, im3.getHeight()*4));

		// mixed scale
		ImgInterface im3 = im;
		ImgViewer.showAll(im3.downScale2x().downScale2x(), im3.rescale(2, 1/2.0));
    }
    
    private static void testRoiToImg() throws IOException {
		Img im1 = Img.createNew(new File("C:\\Users\\demon\\Desktop\\2019-03-29 23.04.25.png"));
		im1 = (Img) im1.downScale2x().downScale2x();
		int w = im1.getWidth();
		int h = im1.getHeight();
		ROI roi = new ROI(w/4, w/2, h/4, h/2, 0, im1.getChannels(), im1);
		roi.show();
		Img im2 = (Img) roi.toImg();
		im2.show();
	}
    
    private static void testRotation() throws IOException, InterruptedException {
		ImgInterface im1 = Img.createNew(new File("C:\\Users\\demon\\Desktop\\2019-03-29 23.04.25.png"));
		im1 = im1.rescale(1.0/16, 1.0/8);
		im1 = im1.downScale2x();
		//im1 = im1.downScale2x();
		im1.show();
		ImgInterface im2 = im1;
		ImgViewer imv = new ImgViewer("rotate test");
		FrameTimer ft = new FrameTimer(45) {
			@Override
			public void frameCall(int frameNo, double lagMs) {
				int i = (frameNo*3) % 360; 
				ImgInterface im3 = im2.rotate(i/180.0*Math.PI);
				imv.setImg(0,im3);
				imv.setImg(1,im3);
				imv.repaint();
				System.out.println(dTime()+", "+getDelay());
			}
		};
		ft.start();
	}
	
	private static void testFlip() throws IOException {
		ImgInterface im1 = Img.createNew(new File("C:\\Users\\demon\\Desktop\\2019-03-29 23.04.25.png"));
		im1 = im1.rescale(1.0/16, 1.0/16);
		im1.show();
		im1 = im1.rescale(-1, 1);
		im1.show();
	}
	
	private static void testPixelPlacement() throws IOException {
		Img im1 = Img.createNew(3, 3, 3);
		im1.setInt(0, 0, 0, 1);
		im1.setInt(0, 0, 1, 2);
		im1.setInt(0, 0, 2, 3);
		im1.setInt(1, 0, 0, 4);
		im1.setInt(1, 0, 1, 5);
		im1.setInt(1, 0, 2, 6);
		im1.setInt(2, 0, 0, 7);
		im1.setInt(2, 0, 1, 8);
		im1.setInt(2, 0, 2, 9);
		System.out.println(Arrays.toString(im1.getValues()));
	}
	
	private static void testProfiler() throws IOException, InterruptedException {
		Thread.sleep(5000);
		for(int i=0; i<100000000; i++) {
			mult(i);
			profi(i);
		}
		System.out.println(ProfilingTools.getTotalTime(0));
	}
	private static int mult(int i) {
		return i * 3;
	}
	private static int profi(int i) {
		ProfilingTools.start(0);
		ProfilingTools.stop(0);
		return i;
	}
	
	private static void testSpeed() throws IOException, InterruptedException {
		//Thread.sleep(5000);
		Img im1 = Img.createNew(new File("C:\\Users\\demon\\Desktop\\2019-03-29 23.04.25.png"));
		
		ProfilingTools.start(0);
		im1.add(1);
		ProfilingTools.stopAndPrintMeanTime(0, "add");
		ProfilingTools.start(1);
		im1.div(2);
		ProfilingTools.stopAndPrintMeanTime(1, "div");
		ProfilingTools.start(2);
		ArrayList<ImgInterface> lap = im1.laplacianPyramid(4);
		ProfilingTools.stopAndPrintMeanTime(2, "lap1");
		ProfilingTools.start(3);
		Img.laplacianPyramid(lap);
		ProfilingTools.stopAndPrintMeanTime(3, "lap2");
		ProfilingTools.start(4);
		im1.toGrey();
		ProfilingTools.stopAndPrintMeanTime(4, "grey");
		ProfilingTools.start(5);
		im1.sumAllValues();
		ProfilingTools.stopAndPrintMeanTime(5, "sum");
	}

	private static void testConv() throws IOException, InterruptedException {
		Img.threadCount = 1;
		Thread.sleep(5000);
		Img im1 = Img.createNew(new File("C:\\Users\\demon\\Desktop\\2019-03-29 23.04.25.png"));
		im1 = (Img) im1.toGrey().rescale(0.25, 0.25);
		im1.show();
		System.out.println(Kernel.boxBlur(1).isSeparable());
		Img.threadCount = 1;
		for (int i = 0; i < 1; i++) {
			ProfilingTools.start(0);
			im1 = im1.convolve(Kernel.gaussian3x3(), Img.BORDER_IGNORE, 1, Img.CONV_MEAN, true, 0);
			im1 = im1.convolve(Kernel.edgeDetection4(), Img.BORDER_IGNORE, 1, Img.CONV_SUM, false, 127);
			//im1.convolve(Kernel.sobelY(), Img.BORDER_IGNORE, 1, Img.CONV_MEAN, true, 0).show();
			im1.show();
			System.out.println(Arrays.toString(im1.minMax()));
			im1 = (Img) im1.greaterThanEq(160).mult(255);
			ArrayList<Blob> blobs = im1.detectBlobs(true);
			for(Blob blob : blobs) {
				if(blob.getSize()<20
						|| blob.getMajorAxis()/blob.getMinorAxis() > 5)
					im1.setBlobValue(blob, 0, 0);
			}
			ProfilingTools.stop(0);
		}
		ProfilingTools.printMeanTime(0, "conv");
		Img.threadCount = 1;
		im1.show();
	}
	
	private static void testRegionBlur() throws IOException {
		Img im1 = Img.createNew(new File("C:\\Users\\demon\\Desktop\\IMG_0307.png"));
		Img im2 = im1.convolve(Kernel.boxBlur(2), Img.BORDER_IGNORE, 1, Img.CONV_MEDIAN);
		Img imX = im2.convolve(Kernel.sobelX(), Img.BORDER_IGNORE, 1, Img.CONV_MEAN, true, 0);
		Img imY = im2.convolve(Kernel.sobelY(), Img.BORDER_IGNORE, 1, Img.CONV_MEAN, true, 0);
		Img im3 = (Img) imX.add(imY);
		ROI im3r = new ROI(0,-1,0,-1,0,1,im3), im3g = new ROI(0,-1,0,-1,1,1,im3), im3b = new ROI(0,-1,0,-1,2,1,im3);
		im3r.runOp(new ImgInterface.Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return prevVal + im3g.getInt(pos) + im3b.getInt(pos);
			}
		});
		//im3 = (Img) im3.greaterThanEq(10).mult(255);
		im3.show();
	}
	
}
