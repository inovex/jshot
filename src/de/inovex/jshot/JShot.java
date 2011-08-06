package de.inovex.jshot;


import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 * @author ruben.jenster@inovex.de
 *
 */
public class JShot {
	
	public static final int KEY_ENTER = 0x0d;
	
	private Display display;
	private Shell transparentShell;
	
	private String imageFilepath;
	private static boolean DEBUG;
	
	public static void main(String [] args) {
		
		DEBUG = Boolean.valueOf(System.getProperty("debug"));

		String filename;
		if (args.length > 0) {
			filename = args[0];
		} else {
			filename = "screenshot.png";
		}
		new JShot(filename);
		System.out.println(filename);
	}
	
	public JShot(String imageFilepath) {
		
		this.imageFilepath = imageFilepath;
		
		
		transparentShell = new Shell(SWT.NONE);
		transparentShell.setAlpha(0);
		display = transparentShell.getDisplay();
		transparentShell.setSize(display.getBounds().width, display.getBounds().height);

		JShotListener myListener = new JShotListener(this);
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
	
	public static void debug(String format, Object ... params) {
		if (DEBUG) {
			if (params.length == 0) {
				System.out.println(format);
			} else {
				System.out.println(String.format(format, params));
			}
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
	
	public void shot(Rectangle rectangle) {
		GC gc = new GC(display);
		final Image image = new Image(display, rectangle);
		gc.copyArea(image, rectangle.x, rectangle.y);
		gc.dispose();
		
		ScrolledComposite sc = new ScrolledComposite (transparentShell, SWT.V_SCROLL | SWT.H_SCROLL);
		Canvas canvas = new Canvas(sc, SWT.NONE);
		sc.setContent(canvas);
		canvas.setBounds(display.getBounds ());
		canvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				e.gc.drawImage(image, 0, 0);
			}
		});

		ImageLoader imageLoader = new ImageLoader();
		imageLoader.data = new ImageData[]{image.getImageData()};
		imageLoader.save(imageFilepath, SWT.IMAGE_PNG);
	}
	
	public void quit() {
		transparentShell.dispose();
	};
	
	public Shell getShell() {
		return transparentShell;
	}
}
