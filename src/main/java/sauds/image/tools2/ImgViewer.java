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
public class ImgViewer extends JFrame {
	
	private ArrayList<ImageIcon> imgs = new ArrayList<>();
	private GridLayout layout = new GridLayout();
	public static final String DEFAULT_TITLE = "ImgViewer";
	
	public ImgViewer(String title) {
		super(title);
		setLayout(layout);
		addSlot();
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(700, 700);
		setVisible(true);
	}
	public final void setImg(int pos, IImgRead img) {
		while(imgs.size() <= pos)
			addSlot();
		imgs.get(pos).setImage(img.toBufferedImage());
	}
	public final void addImg(IImgRead img) {
		ImageIcon lastIcon = imgs.get(imgs.size()-1);
		if(lastIcon.getImage()!=null)
			addSlot();
		lastIcon.setImage(img.toBufferedImage());
	}
	private void addSlot() {
		ImageIcon icon = new ImageIcon();
		imgs.add(icon);
		add(new JLabel(icon));
	}
	
    /**
     * Displays a collection of images
	 * @param title the title to display on the ImgViewer
     * @param imgs the images to display in the ImgViewer
	 * @return returns a reference to the ImgViewer created
     */
	public static ImgViewer showAll(String title, Collection<IImgRead> imgs) {
		ImgViewer imv = new ImgViewer(title);
		for(IImgRead img : imgs) {
			imv.addImg(img);
		}
		imv.repaint();
		return imv;
	}
	
    /**
     * Displays one or more images
	 * @param title the title to display on the ImgViewer
     * @param imgs the images to display in the ImgViewer
	 * @return returns a reference to the ImgViewer created
     */
	public static ImgViewer showAll(String title, IImgRead... imgs) {
		return showAll(title, Arrays.asList(imgs));
	}
	
    /**
     * Displays a collection of images
     * @param imgs the images to display in the ImgViewer
	 * @return returns a reference to the ImgViewer created
     */
	public static ImgViewer showAll(Collection<IImgRead> imgs) {
		return showAll(DEFAULT_TITLE, imgs);
	}
	
    /**
     * Displays one or more images
     * @param imgs the images to display in the ImgViewer
	 * @return returns a reference to the ImgViewer created
     */
	public static ImgViewer showAll(IImgRead... imgs) {
		return showAll(DEFAULT_TITLE, imgs);
	}
	
}
	