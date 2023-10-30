/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sauds.image.tools2;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author saud
 */
public class Viewer extends JFrame {
	
	private final ArrayList<ImageIcon> imgs = new ArrayList<>();
	private final GridLayout layout = new GridLayout();
	public static final String DEFAULT_TITLE = "ImgViewer";
	
	public Viewer(String title) {
		super(title);
		setLayout(layout);
		addSlot();
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(700, 700);
		setVisible(true);
	}
	public final void setImg(int pos, Image image) {
		while(imgs.size() <= pos)
			addSlot();
		imgs.get(pos).setImage(image.toBufferedImage());
	}
	public final void addImg(Image image) {
		ImageIcon lastIcon = imgs.get(imgs.size()-1);
		if(lastIcon.getImage()!=null)
			addSlot();
		lastIcon.setImage(image.toBufferedImage());
	}
	private void addSlot() {
		ImageIcon icon = new ImageIcon();
		imgs.add(icon);
		add(new JLabel(icon));
	}
	
    /**
     * Displays a collection of images
	 * @param title the title to display on the ImgViewer
     * @param images the images to display in the ImgViewer
	 * @return returns a reference to the ImgViewer created
     */
	public static Viewer showAll(String title, Collection<Image> images) {
		Viewer imv = new Viewer(title);
		for(Image image : images) {
			imv.addImg(image);
		}
		imv.repaint();
		return imv;
	}
	
    /**
     * Displays one or more images
	 * @param title the title to display on the ImgViewer
     * @param images the images to display in the ImgViewer
	 * @return returns a reference to the ImgViewer created
     */
	public static Viewer showAll(String title, Image... images) {
		return showAll(title, Arrays.asList(images));
	}
	
    /**
     * Displays a collection of images
     * @param images the images to display in the ImgViewer
	 * @return returns a reference to the ImgViewer created
     */
	public static Viewer showAll(Collection<Image> images) {
		return showAll(DEFAULT_TITLE, images);
	}
	
    /**
     * Displays one or more images
     * @param images the images to display in the ImgViewer
	 * @return returns a reference to the ImgViewer created
     */
	public static Viewer showAll(Image... images) {
		return showAll(DEFAULT_TITLE, images);
	}

	/**
	 * Prints the image pixels to the console. (WARNING: it will attempt to print large images)
	 * @param image The image to print
	 */
	public static void print(Image image) {
		for (int k = 0; k < image.getDepth(); k++) {
			for (int j = 0; j < image.getHeight(); j++) {
				for (int i = 0; i < image.getWidth(); i++) {
					System.out.printf("%d,\t", image.getInt(i,j,k));
				}
				System.out.println();
			}
			System.out.println();
		}
	}
}
	