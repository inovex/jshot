package de.inovex.jshot;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Shell;

public class Frame {
	
	private Region region;
	private int borderWidth;
	
	private Shell shell;
	private int startX;
	private int startY;
	
	private int x1;
	private int y1;
	private int x2;
	private int y2;
	private boolean top;
	private boolean left;

	public void clear() {
		if (region != null) {
			region.dispose();
		}
		shell.setRegion(new Region());
	}
	
	public Frame(Shell parent) {
		this(parent, 5, new Color(parent.getDisplay(), 255, 0, 0));
	}
	
	public Frame(Shell parent, int borderWidth, Color borderColor) {
		// create shell that is used for painting the frame
		shell = new Shell(parent, SWT.NO_TRIM);
		this.borderWidth = borderWidth;
		shell.setBackground(borderColor);
		shell.setSize(parent.getDisplay().getBounds().width, parent.getDisplay().getBounds().height);
		this.clear();
		shell.setVisible(true);
	}
	
	public synchronized void setStart(int x, int y) {
		this.startX = x;
		this.startY = y;
	}
	
	public synchronized void draw(int endX, int endY) {

		// dispose previous region
		if (region != null) {
			region.dispose();
		}
		
		Rectangle topBorder = null;
		Rectangle rightBorder = null;
		Rectangle bottomBorder = null;
		Rectangle leftBorder = null;

		int origin = JShot.calculateOrigin(startX, startY, endX, endY);
			
		top = (origin & JShot.TOP_LEFT) > 0 || (origin & JShot.TOP_RIGHT) > 0;
		left = (origin & JShot.TOP_LEFT) > 0 || (origin & JShot.BOTTOM_LEFT) > 0;  
		
		/*
		 * distinguish between top and bottom
		 * left- and right border are calculated the same for top or bottom
		 */
		
		int borderX = 0;
		int borderY = 0;
		
		if (top) {
			y1 = startY;
			y2 = endY;
			borderY = borderWidth;
		} else { // bottom
			y1 = endY;
			y2 = startY;
		}
		
		if (left) {
			x1 = startX;
			x2 = endX;
			borderX = borderWidth;
		} else { // right
			x1 = endX;
			x2 = startX;
		}
		
		int width = x2 - x1 + borderWidth;
		int height = y2 - y1 + borderWidth;
		
		
		/*
		topBorder = new Rectangle(x1 - borderX, y1 - borderY, width, borderWidth);
		rightBorder = new Rectangle(x2 - borderX, y1 - borderY, borderWidth, height);
		bottomBorder = new Rectangle(x1 - borderX, y2 - borderY, width, borderWidth);
		leftBorder = new Rectangle(x1 - borderX, y1 - borderY, borderWidth, height);
		*/
		// create new region
		try {
			region = new Region();
			region.add(new Rectangle(x1-borderWidth, y1-borderWidth, x2-x1, y2-y1));
			region.subtract(new Rectangle(x1, y1, x2-x1-2*borderWidth, y2-y1-2*borderWidth));
			/*
			region.add(topBorder);
			region.add(leftBorder);
			region.add(rightBorder);
			region.add(bottomBorder);
			*/
			shell.setRegion(region);
			shell.layout();
		
			JShot.debug("###################################");
			JShot.debug("Point1: x1[%s], y1[%s]", startX, startY);
			JShot.debug("Point2: x1[%s], y1[%s]", endX, endY);
			JShot.debug("Point1c switched: x1[%s], y1[%s]", x1, y1);
			JShot.debug("Point2c switched: x2[%s], y2[%s]", x2, y2);
			JShot.debug("topBorder: [%s]", topBorder);
			JShot.debug("rightBorder: [%s]", rightBorder);
			JShot.debug("bottomBorder: [%s]", bottomBorder);
			JShot.debug("leftBorder: [%s]", leftBorder);
			JShot.debug("###################################");

		} catch (IllegalArgumentException e) {
			// ignore
		}
	}
	
	public synchronized Rectangle getBounds() {
		if (top && left) {
			return new Rectangle(x1, y1, x2-x1-borderWidth, y2-y1-borderWidth);
		} else if (top && !left) {
			// TOP_RIGHT, left border
			return new Rectangle(x1+borderWidth, y1, x2-x1-borderWidth, y2-y1-borderWidth);
		} else if (!top && left) {
			// BOTTOM_LEFT, top border
			return new Rectangle(x1, y1+borderWidth, x2-x1-borderWidth, y2-y1-borderWidth);
		} else {
			// BOTTOM_RIGHT, left and top border
			return new Rectangle(x1+borderWidth, y1+borderWidth, x2-x1-borderWidth, y2-y1-borderWidth);
		}
	}
}