package maintest;


import sauds.image.tools.Img;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import sauds.toolbox.multiprocessing.tools.MPT;
import sauds.toolbox.multiprocessing.tools.MTPListRunnable;
import sauds.toolbox.timer.Timer;

/**
 *
 * @author saud
 */
public class Test {
    
    public static void main(String[] args) throws IOException, InterruptedException {
	Thread.sleep(5000);
	test6();
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
    
    public static void test1() throws IOException {
	Img.threadCount = 1;
	Img im2 = new Img(new File("C:\\Users\\demon\\Desktop\\New Bitmap Image.jpg"));
	Img im1 = new Img(new File("C:\\Users\\demon\\Desktop\\2019-03-29 23.04.25.png"));
	im1.insert(0, 0, im2);
	im1 = im1.squareifyPow2();
	//Img2.showImgs(im1);
	ArrayList<Img> lap = im1.laplacianPyramid(4);
	//Img2.showImgs(lap);
	im1 = Img.laplacianPyramid(im1.laplacianPyramid(4)).sub(im1).mult(255);
	im1.save("jpg", new File("C:\\Users\\demon\\Desktop\\2019-03-29 23.04.255.png"));
	//Img2.showImgs(im1);
	//im1.save("png", new File("C:\\Users\\demon\\Desktop\\2019-03-29 23.04.252.png"));
    }
    
    public static void test2() throws IOException {
	Img im1 = new Img(new File("C:\\Users\\demon\\Desktop\\New Bitmap Image.jpg"));
	ArrayList<Img> lap = im1.laplacianPyramid(4);
	//Img2.showImgs(lap);
	Img.showImgs(Img.laplacianPyramid(lap));
    }

    private static void test3() throws IOException {
	Img im1 = new Img(new File("C:\\Users\\demon\\Desktop\\2019-03-29 23.04.25.png"));
	for (int i = 0; i < 10; i++) {
	    Timer t1 = new Timer("T1");
	    for (int j = 0; j < 20; j++) {
		im1.add(j).sub(j);
	    }
	    t1.print();
	}
    }

    private static void test4() throws IOException {
	Img im1 = new Img(new File("C:\\Users\\demon\\Desktop\\2019-03-29 23.04.25.png"));
	im1 = im1.downScale2x().downScale2x().downScale2x();
	//im1.show();
	im1 = im1.toGrey();
	im1.show();
    }
    
    private static void test5() throws IOException {
	Img im = new Img(new File("C:\\Users\\demon\\Desktop\\2019-03-29 23.04.25.png")).downScale2x().downScale2x().downScale2x();
	Img im1 = new Img(im);
	Img im2 = new Img(im);
	
	//im2.setVal(1, 1, 0, (byte) (im2.getVal(1, 1, 0)+1));
	
	for (int ii = 0; ii < 10; ii++) {
	    Timer t1 = new Timer("T1");
	    for (int i = 0; i < 10; i++) {
		im1 = im1.add(2);
	    }
	    t1.print();

	    Timer t2 = new Timer("T2");
	    for (int i = 0; i < 10; i++) {
		im2.opAdd(2);
	    }
	    im2 = im2.runOps();
	    t2.print();
	}
	
	//im2.sub(im1).abs().mult(255).show();
	System.out.println(Arrays.toString(im1.sub(im2).minMax()));
    }

    private static void test6() throws IOException {
	Img.threadCount = 1;
	Img im = new Img(new File("C:\\Users\\demon\\Desktop\\2019-03-29 23.04.25.png")).downScale2x().downScale2x().downScale2x();
	Img im2 = im.downScale2x().downScale2x();
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
	Img im3 = im;
	Img.showImgs(im3.downScale2x().downScale2x(), im3.rescale(2, 1/2.0));
    }
    
}
