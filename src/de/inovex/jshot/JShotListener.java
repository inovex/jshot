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
	private JShot jshot;
	
	// constants for the DMR(draw, move, resize) state machine
	private static final int DISABLED = 0x01;
	private static final int RESIZE_ON = 0x02;
	private static final int DRAW = 0x04;
	private static final int MOVE = 0x08;
	private static final int RESIZE = 0x10;
	
	private int state = DISABLED;
	
	public JShotListener(JShot jshot) {
		this.jshot = jshot;
		this.frame = new Frame(jshot.getShell());
	}
	
	@Override
	public void mouseDoubleClick(MouseEvent e) {
		// toggle the resize enabled flag
		if (this.frame.inFrame(e.x, e.y)) {
			this.state ^= RESIZE_ON;
			JShot.debug("Resize mode is: %b", (this.state & RESIZE_ON )> 0);
		}
	}

	@Override
	public void mouseDown(MouseEvent e) {

		// clear all flags but the the resize flag
		this.state &= RESIZE_ON;

		if (this.frame.inFrame(e.x, e.y)) {
			if ((this.state & RESIZE_ON) > 0) {
				this.frame.setResizePosition(e.x, e.y);
				this.state |= RESIZE;
			} else {
				this.state |= MOVE;
				this.frame.startMove(e.x, e.y);				
			}
		} else {
			JShot.debug("draw");
			// check if click was in the frame
			this.frame.start(e.x, e.y);
			this.state |= DRAW;
		}
	}

	@Override
	public void mouseUp(MouseEvent e) {
		this.state |= DISABLED;
	}

	@Override
	public void mouseMove(MouseEvent e) {
		
		//JShot.debug("Move Mouse. State: %d", this.state);
		
		switch(state) {
		case DRAW:
		case DRAW | RESIZE_ON:
			this.frame.draw(e.x, e.y);
			break;
		case MOVE:
		case MOVE | RESIZE_ON:
			this.frame.move(e.x, e.y);
			break;
		case RESIZE | RESIZE_ON:
			this.frame.resize(e.x, e.y);
			break;
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
			break;
		case JShot.KEY_ENTER:
			this.jshot.shot(frame.getSelectedArea());
			jshot.quit();
			break;
		}
	}
}