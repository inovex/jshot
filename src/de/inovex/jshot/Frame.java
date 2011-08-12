package de.inovex.jshot;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 * @author ruben.jenster@inovex.de
 *
 */
public class Frame {
		
	private Shell shell;
	
	// the region representing the frame area
	private Region region;
	
	// the rectangle representing the selected area
	private Rectangle selectedArea;	

	// the width of the frame border
	private int borderWidth;

	// start and end position for normal frame drawing
	private int startX;
	private int startY;
	private int endX;
	private int endY;

	// start position for move action
	private int startMoveY;
	private int startMoveX;
	
	// start position for resize
	private int resizePosition;
	
	// constants used to detect position and orientation of clicks in the frame
	public static final int NONE = 0x00;
	public static final int TOP = 0x01;
	public static final int BOTTOM = 0x02;
	public static final int LEFT = 0x04;
	public static final int RIGHT = 0x08;
	public static final int CORNER = 0x10;


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
	
	public synchronized void start(int x, int y) {
		this.startX = x;
		this.startY = y;
	}
	
	public synchronized void startMove(int x, int y) {
		this.startMoveX = x;
		this.startMoveY = y;
	}
	
	
	public synchronized void move(int endMoveX, int endMoveY) {
		
		JShot.debug("move");
				
		// determine the difference of the move event
		int diffMoveX = endMoveX - startMoveX;
		int diffMoveY =  endMoveY - startMoveY;
		
		// add the differences to the frame start position
		this.startX += diffMoveX;
		this.startY += diffMoveY;
		
		// draw the moved frame with the updated end position
		draw(this.endX + diffMoveX, this.endY + diffMoveY);

		// next move we have to start at the previous end move position
		this.startMoveX = endMoveX;
		this.startMoveY = endMoveY;
		
	}
	
	public synchronized void draw() {
		this.draw(this.endX, this.endY);
	}
	
	public synchronized void draw(int endX, int endY) {
		
		JShot.debug("draw");

		// dispose an existing previous region
		if (region != null) {
			region.dispose();
		}
		
		// we have to shift the coordinates, according to
		// the quadrant from which the frame draw originated 
		int origin = calculateOrigin(startX, startY, endX, endY);
			
		int x1;
		int y1;
		int x2;
		int y2;
		
		if ((origin & TOP) > 0) {
			y1 = startY;
			y2 = endY;
		} else { // bottom
			y1 = endY;
			y2 = startY;
		}
		
		if ((origin & LEFT) > 0) {
			x1 = startX;
			x2 = endX;
		} else { // right
			x1 = endX;
			x2 = startX;
		}
		// create new region
		try {
			region = new Region();
			region.add(new Rectangle(x1-borderWidth, y1-borderWidth, x2-x1+borderWidth, y2-y1+borderWidth));
			selectedArea = new Rectangle(x1, y1, x2-x1-borderWidth, y2-y1-borderWidth);
			region.subtract(selectedArea);
			shell.setRegion(region);
			shell.layout();
		
			/*
			JShot.debug("###################################");
			JShot.debug("Point1: (x1,y1) = (%d,%d)", startX, startY);
			JShot.debug("Point2: (x2,y2) = (%d,%d)", endX, endY);
			JShot.debug("Point1c switched: (x1,y1) = (%d,%d)", x1, y1);
			JShot.debug("Point2c switched: (x2,y2) = (%d,%d)", x2, y2);
			JShot.debug("###################################");
			*/

		} catch (IllegalArgumentException e) {
			// ignore
		}
		
		// next draw we have to start at the previous end position
		this.endX = endX;
		this.endY = endY;
	}
	
	public synchronized Rectangle getSelectedArea() {
		return selectedArea;
	}
	
	public synchronized boolean inFrame(int x, int y) {
		if (region != null && region.contains(x,y)) {
			return true;
		}
		return false;
	}
	
	private int calculateOrigin(int x1, int y1, int x2, int y2) {
		int width = x2 - x1;
		int height = y2 - y1;
		return calculateOrigin(width, height);
	}

	private int calculateOrigin(int width, int height) {
		
		if (width > 0 && height > 0) {
			return TOP | LEFT;
		} else if (width < 0 && height > 0) {
			return TOP | RIGHT;
		} else if (width < 0 && height < 0) {
			return BOTTOM | RIGHT;
		} else {
			return BOTTOM | LEFT;
		}
	}
	
	/**
	 * Returns the location of the given coordinates in the frame.
	 * You have to check if the coordinates are in the frame
	 * with {@link #inFrame(int, int)} before.
	 * 
	 * @param x
	 * @param y
	 * @return and integer value representing the position, or -1 if the position isn't valid
	 */
	public synchronized int getFramePosition(int x, int y) {
		if (getCorner(TOP | LEFT | CORNER).contains(x,y)) {
			JShot.debug("TOP | LEFT | CORNER");
			return TOP | LEFT | CORNER;
		} else if (getCorner(TOP | RIGHT | CORNER).contains(x, y)) {
			JShot.debug("TOP | RIGHT | CORNER");
			return TOP | RIGHT | CORNER;
		} else if (getCorner(BOTTOM | RIGHT | CORNER).contains(x, y)) {
			JShot.debug("BOTTOM | RIGHT | CORNER");
			return BOTTOM | RIGHT | CORNER;
		} else if (getCorner(BOTTOM | LEFT | CORNER).contains(x, y)) {
			JShot.debug("BOTTOM | LEFT | CORNER");
			return BOTTOM | LEFT | CORNER;
		} else if (getCorner(TOP).contains(x, y)) {
			JShot.debug("TOP");
			return TOP;
		} else if (getCorner(BOTTOM).contains(x, y)) {
			JShot.debug("BOTTOM");
			return BOTTOM;
		} else if (getCorner(LEFT).contains(x, y)) {
			JShot.debug("LEFT");
			return LEFT;
		} else if (getCorner(RIGHT).contains(x, y)) {
			JShot.debug("RIGHT");
			return RIGHT;
		}
		// should not happen 
		return -1;
	}
	
	/**
	 * @param position
	 * @return the Rectangle for the area of the frame identified by the given position
	 */
	public synchronized Rectangle getCorner(int position) {
		
		// calculate the coordinates for the inner square (x1,y1), (x2,y1), (x2,y2), (x1,y2)
		Rectangle bounds = this.region.getBounds();
		int x1 = bounds.x + borderWidth;
		int x2 = bounds.x + bounds.width - borderWidth;

		int y1 = bounds.y + borderWidth;
		int y2 = bounds.y + bounds.height - borderWidth;
		
		// the sizes of the inner square
		int width = bounds.width - (2 * borderWidth);
		int height = bounds.height - (2 * borderWidth);

		switch (position) {
		case TOP | LEFT | CORNER:
			return new Rectangle(bounds.x, bounds.y, borderWidth, borderWidth);
		case TOP | RIGHT | CORNER:
			return new Rectangle(x2, bounds.y, borderWidth, borderWidth);
		case BOTTOM | RIGHT | CORNER:
			return new Rectangle(x2, y2, borderWidth, borderWidth);
		case BOTTOM | LEFT  | CORNER:
			return new Rectangle(bounds.x, y2, borderWidth, borderWidth);
		case TOP:
			return new Rectangle(x1, bounds.y, width, borderWidth);
		case RIGHT:
			return new Rectangle(x2, y1, borderWidth, height);
		case BOTTOM:
			return new Rectangle(x1, y2, width, borderWidth);
		case LEFT:
			return new Rectangle(bounds.x, y1, borderWidth, height);
		} 
		return null;
	}

	public void resize(int x, int y) {
		
		JShot.debug("resize");
		
		switch (resizePosition) {
		case TOP | LEFT | CORNER:
			this.startX = x;
			this.startY = y;
			break;
		case TOP | RIGHT | CORNER:
			this.endX = x;
			this.startY = y;
			break;
		case BOTTOM | RIGHT | CORNER:
			this.endX = x;
			this.endY = y;
			break;
		case BOTTOM | LEFT  | CORNER:
			this.startX = x;
			this.endY = y;
			break;
		case TOP:
			this.startY = y;
			break;
		case RIGHT:
			this.endX = x;
			break;
		case BOTTOM:
			this.endY = y;
			break;
		case LEFT:
			this.startX = x;
			break;
		} 
		draw();
	}

	public void setResizePosition(int x, int y) {
		this.resizePosition = getFramePosition(x, y);
	}
}