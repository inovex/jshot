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
	
	private int moveY;
	private int moveX;
	private int endX;
	private int endY;
	
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
		this.moveX = x;
		this.moveY = y;
	}
	
	public synchronized void move(int endMoveX, int endMoveY) {
		
		JShot.debug("move");
				
		int diffMoveX = endMoveX - moveX;
		int diffMoveY =  endMoveY - moveY;
		/*
		JShot.debug("Move Begin (x,y) = (%d,%d)", this.moveX, this.moveY);
		JShot.debug("Move End (x,y) = (%d,%d)", endMoveX, endMoveY);
		JShot.debug("Diff (x,y) = (%d,%d)", diffMoveX, diffMoveY);
		*/
		this.startX += diffMoveX;
		this.startY += diffMoveY;
		
		// next move we have start at the last move end position
		this.moveX = endMoveX;
		this.moveY = endMoveY;
		
		draw(this.endX + diffMoveX, this.endY + diffMoveY);
	}
	
	public synchronized void draw(int endX, int endY) {
		
		JShot.debug("draw");
		
		this.endX = endX;
		this.endY = endY;

		// dispose previous region
		if (region != null) {
			region.dispose();
		}
		
		// find the quadrant of the coordinate system that 
		// is the origin. depending of the origin quadrant,
		// we have to switch the coordinates.
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
			region.subtract(new Rectangle(x1, y1, x2-x1-borderWidth, y2-y1-borderWidth));
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
	}
	
	// TODO use region to get the bounds?
	public synchronized Rectangle getBounds() {
		return region.getBounds();
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
	
	public synchronized int getFramePosition(int x, int y) {
		/*
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
		} else */if (getCorner(TOP).contains(x, y)) {
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
		JShot.debug("outside");
		return -1;
	}
	
	public synchronized Rectangle getCorner(int position) {
		Rectangle bounds = this.region.getBounds();
		int x1 = bounds.x + borderWidth;
		int x2 = bounds.x + bounds.width - borderWidth;

		int y1 = bounds.y + borderWidth;
		int y2 = bounds.y + bounds.height - borderWidth;
		
		// the effective width of the borders, excluding the corners
		int width = bounds.width - 2*borderWidth;
		int height = bounds.height - 2*borderWidth;

		switch (position) {
		/*
		case TOP | LEFT | CORNER:
			return new Rectangle(x1, y1, borderWidth, borderWidth);
		case TOP | RIGHT | CORNER:
			return new Rectangle(x2 - borderWidth, y1, borderWidth, borderWidth);
		case BOTTOM | RIGHT | CORNER:
			return new Rectangle(x2 - borderWidth, y2-borderWidth, borderWidth, borderWidth);
		case BOTTOM | LEFT  | CORNER:
			return new Rectangle(x1 - borderWidth, y2-borderWidth, borderWidth, borderWidth);
		*/
		
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
		getFramePosition(x, y);
	}
}