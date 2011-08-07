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
	private int moveY;
	private int moveX;
	private int endX;
	private int endY;

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
	
	public synchronized void setStartMove(int x, int y) {
		this.moveX = x;
		this.moveY = y;
	}
	
	public synchronized void move(int endMoveX, int endMoveY) {
				
		int diffMoveX = endMoveX - moveX;
		int diffMoveY =  endMoveY - moveY;
		
		JShot.debug("Move Begin (x,y) = (%d,%d)", this.moveX, this.moveY);
		JShot.debug("Move End (x,y) = (%d,%d)", endMoveX, endMoveY);
		JShot.debug("Diff (x,y) = (%d,%d)", diffMoveX, diffMoveY);
		
		this.startX += diffMoveX;
		this.startY += diffMoveY;
		
		// next move we have start at the last move end position
		this.moveX = endMoveX;
		this.moveY = endMoveY;
		
		draw(this.endX + diffMoveX, this.endY + diffMoveY);
	}
	
	public synchronized void draw(int endX, int endY) {
		
		this.endX = endX;
		this.endY = endY;

		// dispose previous region
		if (region != null) {
			region.dispose();
		}
		
		int origin = JShot.calculateOrigin(startX, startY, endX, endY);
			
		top = (origin & JShot.TOP_LEFT) > 0 || (origin & JShot.TOP_RIGHT) > 0;
		left = (origin & JShot.TOP_LEFT) > 0 || (origin & JShot.BOTTOM_LEFT) > 0;  
		
		/*
		 * distinguish between top and bottom
		 * left- and right border are calculated the same for top or bottom
		 */
		
		if (top) {
			y1 = startY;
			y2 = endY;
		} else { // bottom
			y1 = endY;
			y2 = startY;
		}
		
		if (left) {
			x1 = startX;
			x2 = endX;
		} else { // right
			x1 = endX;
			x2 = startX;
		}
		// create new region
		try {
			region = new Region();
			region.add(new Rectangle(x1-borderWidth, y1-borderWidth, x2-x1, y2-y1));
			region.subtract(new Rectangle(x1, y1, x2-x1-2*borderWidth, y2-y1-2*borderWidth));
			shell.setRegion(region);
			shell.layout();
		
			JShot.debug("###################################");
			JShot.debug("Point1: (x1,y1) = (%d,%d)", startX, startY);
			JShot.debug("Point2: (x2,y2) = (%d,%d)", endX, endY);
			JShot.debug("Point1c switched: (x1,y1) = (%d,%d)", x1, y1);
			JShot.debug("Point2c switched: (x2,y2) = (%d,%d)", x2, y2);
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
	
	public synchronized boolean inFrame(int x, int y) {
		if (region != null && region.contains(x,y)) {
			return true;
		}
		return false;
	}
}