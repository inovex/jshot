package de.inovex.jshot;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;


/**
 * 
 * @author ruben.jenster@inovex.de
 *
 */
public class JShotListener implements MouseMoveListener, MouseListener,KeyListener {

	private Frame frame;
	private boolean draw = false;
	private JShot jshot;
	private boolean move;
	
	
	public JShotListener(JShot jshot) {
		this.jshot = jshot;
		this.frame = new Frame(jshot.getShell());
	}
	
	@Override
	public void mouseDoubleClick(MouseEvent e) {
		JShot.debug("double click");
	}

	
	
	@Override
	public void mouseDown(MouseEvent e) {
		if (this.frame.inFrame(e.x, e.y)) {
			this.frame.setStartMove(e.x, e.y);
			this.move = true;
		} else {
			// check if click was in the frame
			this.frame.setStart(e.x, e.y);
			this.draw = true;
		}
	}

	@Override
	public void mouseUp(MouseEvent e) {
		this.draw = false;
		this.move = false;
	}

	@Override
	public void mouseMove(MouseEvent e) {
		if (draw) {
			this.frame.draw(e.x, e.y);
		} else if (move){
			this.frame.move(e.x, e.y);
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
			jshot.quit();
			//frame.clear();
			break;
		case JShot.KEY_ENTER:
			//System.out.println("taking screenshot from region: " + frame.getBounds());
			this.jshot.shot(frame.getBounds());
			jshot.quit();
			break;
		}
	}
}