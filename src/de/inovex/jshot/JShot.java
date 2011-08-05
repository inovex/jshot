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

public class JShot {
	
	public static final int KEY_ENTER = 0x0d;
	
	private Display display;
	private Shell transparentShell;
	
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
		private int width;
		private int height;

		public Frame(Shell parent) {
			this(parent, 5, new Color(parent.getDisplay(), 255, 0, 0));
			
		}
		
		public Frame(Shell parent, int borderWidth, Color borderColor) {
			
			// create shell that is used for painting the frame
			shell = new Shell(parent, SWT.NO_TRIM);
			this.borderWidth = borderWidth;
			shell.setBackground(borderColor);
			shell.setSize(parent.getDisplay().getBounds().width, parent.getDisplay().getBounds().height);
			shell.setRegion(new Region());
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
			this.width = endX - this.startX;
			this.height = endY - this.startY;

			// calculate the rectangle
			Rectangle topBorder = new Rectangle(this.startX - borderWidth, this.startY - borderWidth, width + borderWidth, borderWidth);
			Rectangle bottomBorder = new Rectangle(this.startX - borderWidth, endY-borderWidth, width, borderWidth);

			Rectangle leftBorder = new Rectangle(this.startX - borderWidth, this.startY, borderWidth, height);
			Rectangle rightBorder = new Rectangle(endX - borderWidth, this.startY, borderWidth, height);
			
			// create new region
			region = new Region();
			region.add(topBorder);
			region.add(leftBorder);
			region.add(rightBorder);
			region.add(bottomBorder);
			shell.setRegion(region);
			shell.layout();
		}
		
		public Rectangle getBounds() {
			return new Rectangle(startX, startY, width, height);
		}
	}
	
	public static class MyListener implements MouseMoveListener, MouseListener,KeyListener {

		private Shell motherShell;
		private Frame frame;
		private boolean draw = false;
		
		
		public MyListener(Shell motherShell) {
			this.motherShell = motherShell;
			this.frame = new Frame(motherShell);
		}
		
		@Override
		public void mouseDoubleClick(MouseEvent e) {
			// TODO Auto-generated method stub
			
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
				System.out.println("Not drawing. " + e.x + " " + e.y);
			}
			
		}

		@Override
		public void keyPressed(KeyEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void keyReleased(KeyEvent e) {
			switch (e.keyCode) {
			case SWT.ESC: 
				motherShell.dispose();
				break;
			case KEY_ENTER:
				System.out.println("taking screenshot from region: " + frame.getBounds());
				motherShell.dispose();
				break;
			}
		}
		
	}
}
