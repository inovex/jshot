package de.inovex.jshot;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO
 * - inverse frame painting, starting at the top-right,bottom-right and bottom-left corner
 * - screen shot the framed region
 * - actions dialog (save, send, start program, open inkscape)
 * - 
 * @author ruben
 *
 */
public class JShot {
	
	public static final int KEY_ENTER = 0x0d;
	
	private Display display;
	private Shell transparentShell;
	
	private static final Logger LOG = LoggerFactory.getLogger(JShot.class);
	
	public static void main(String [] args) {
		new JShot();
	}
	
	public JShot() {
		
		transparentShell = new Shell(SWT.NONE);
		transparentShell.setAlpha(0);
		display = transparentShell.getDisplay();
		transparentShell.setSize(display.getBounds().width, display.getBounds().height);

		MyListener myListener = new MyListener(transparentShell);
		transparentShell.addMouseListener(myListener);
		transparentShell.addMouseMoveListener(myListener);
		transparentShell.addKeyListener(myListener);
		
		transparentShell.open();
		
		while (!transparentShell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	public static Region drawFrame(Display display, final Shell shell, int width, int height, int border) {
		Region region = new Region(display);
		region.add(new Rectangle(0, 0, width, border));
		region.add(new Rectangle(0, 0, border, height));
		region.add(new Rectangle(width-border, 0, border, height));
		region.add(new Rectangle(0, height-border, width, border));
		shell.setRegion(region);
		return region;
	}
	
	public static class Frame {
		
		private Region region;
		private int borderWidth;
		
		private Shell shell;
		private int startX;
		private int startY;
		
		private int x1;
		private int y1;
		private int x2;
		private int y2;

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
		
		public void setStart(int x, int y) {
			this.startX = x;
			this.startY = y;
		}
		
		public void draw(int endX, int endY) {

			// dispose previous region
			if (region != null) {
				region.dispose();
			}
			
			Rectangle topBorder = null;
			Rectangle rightBorder = null;
			Rectangle bottomBorder = null;
			Rectangle leftBorder = null;

			int origin = calculateOrigin(startX, startY, endX, endY);
				
			// calculate the rectangle
			boolean top = (origin & TOP_LEFT) > 0 || (origin & TOP_RIGHT) > 0;
			boolean left = (origin & TOP_LEFT) > 0 || (origin & BOTTOM_LEFT) > 0;  
			
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
				
			topBorder = new Rectangle(x1 - borderX, y1 - borderY, width, borderWidth);
			rightBorder = new Rectangle(x2 - borderX, y1 - borderY, borderWidth, height);
			bottomBorder = new Rectangle(x1 - borderX, y2 - borderY, width, borderWidth);
			leftBorder = new Rectangle(x1 - borderX, y1 - borderY, borderWidth, height);

			// create new region
			try {
				region = new Region();
				region.add(topBorder);
				region.add(leftBorder);
				region.add(rightBorder);
				region.add(bottomBorder);
				shell.setRegion(region);
				shell.layout();
			} catch (IllegalArgumentException e) {
				LOG.debug("###################################");
				LOG.debug("Point1: x1[{}], y1[{}]", x1, y1);
				LOG.debug("Point2: x2[{}], y2[{}]", x2, y2);
				LOG.debug("topBorder: [{}]", topBorder);
				LOG.debug("rightBorder: [{}]", rightBorder);
				LOG.debug("bottomBorder: [{}]", bottomBorder);
				LOG.debug("leftBorder: [{}]", leftBorder);
				LOG.debug("###################################");
			}
		}
		
		public Rectangle getBounds() {
			return new Rectangle(x1, y1, x2-x1, y2-y1);
		}
		
	}
	
	public static final int TOP_LEFT = 0x01;
	public static final int TOP_RIGHT = 0x02;
	public static final int BOTTOM_RIGHT = 0x04;
	public static final int BOTTOM_LEFT = 0x08;
	
	public static int calculateOrigin(int x1, int y1, int x2, int y2) {
		int width = x2 - x1;
		int height = y2 - y1;
		return calculateOrigin(width, height);
	}

	public static int calculateOrigin(int width, int height) {
		
		if (width > 0 && height > 0) {
			return TOP_LEFT;
		} else if (width < 0 && height > 0) {
			return TOP_RIGHT;
		} else if (width < 0 && height < 0) {
			return BOTTOM_RIGHT;
		} else {
			return BOTTOM_LEFT;
		}
		
	
	}
		
	
	public static class MyListener implements MouseMoveListener, MouseListener,KeyListener {

		private Shell parentShell;
		private Frame frame;
		private boolean draw = false;
		
		
		public MyListener(Shell parentShell) {
			this.parentShell = parentShell;
			this.frame = new Frame(parentShell);
		}
		
		@Override
		public void mouseDoubleClick(MouseEvent e) {
			// unused
			
		}

		@Override
		public void mouseDown(MouseEvent e) {
			// check if click was in the frame
			this.frame.setStart(e.x, e.y);
			this.draw = true;
		}

		@Override
		public void mouseUp(MouseEvent e) {
			this.draw = false;
		}

		@Override
		public void mouseMove(MouseEvent e) {
			if (draw) {
				this.frame.draw(e.x, e.y);
			} else {
				//LOG.debug("Not drawing. x[{}] y[{}]", e.x, e.y);
			}
			
		}

		@Override
		public void keyPressed(KeyEvent e) {
			// unused
			
		}

		@Override
		public void keyReleased(KeyEvent e) {
			switch (e.keyCode) {
			case SWT.ESC: 
				frame.clear();
				break;
			case KEY_ENTER:
				System.out.println("taking screenshot from region: " + frame.getBounds());
				parentShell.dispose();
				break;
			}
		}
		
	}
}