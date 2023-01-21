package com.threeamigos.imageviewer.implementations.ui;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.threeamigos.imageviewer.interfaces.ui.InputConsumer;

public class ChainedInputAdapter implements InputConsumer {

	private List<PrioritizedInputAdapter> adapters = new LinkedList<>();

	public void addAdapter(PrioritizedInputAdapter adapter) {
		adapters.add(adapter);
		Collections.sort(adapters, (a1, a2) -> {
			int p1 = a1.getPriority();
			int p2 = a2.getPriority();
			if (p1 == p2) {
				return 0;
			} else if (p1 < p2) {
				return -1;
			} else {
				return 1;
			}
		});
	}

	public void removeAdapter(PrioritizedInputAdapter adapter) {
		adapters.remove(adapter);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		for (InputAdapter adapter : adapters) {
			adapter.mouseWheelMoved(e);
			if (e.isConsumed()) {
				break;
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		for (InputAdapter adapter : adapters) {
			adapter.mouseClicked(e);
			if (e.isConsumed()) {
				break;
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		for (InputAdapter adapter : adapters) {
			adapter.mousePressed(e);
			if (e.isConsumed()) {
				break;
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		for (InputAdapter adapter : adapters) {
			adapter.mouseReleased(e);
			if (e.isConsumed()) {
				break;
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		for (InputAdapter adapter : adapters) {
			adapter.mouseEntered(e);
			if (e.isConsumed()) {
				break;
			}
		}
	}

	@Override
	public void mouseExited(MouseEvent e) {
		for (InputAdapter adapter : adapters) {
			adapter.mouseExited(e);
			if (e.isConsumed()) {
				break;
			}
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		for (InputAdapter adapter : adapters) {
			adapter.mouseDragged(e);
			if (e.isConsumed()) {
				break;
			}
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		for (InputAdapter adapter : adapters) {
			adapter.mouseMoved(e);
			if (e.isConsumed()) {
				break;
			}
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		for (InputAdapter adapter : adapters) {
			adapter.keyTyped(e);
			if (e.isConsumed()) {
				break;
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		for (InputAdapter adapter : adapters) {
			adapter.keyPressed(e);
			if (e.isConsumed()) {
				break;
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		for (InputAdapter adapter : adapters) {
			adapter.keyReleased(e);
			if (e.isConsumed()) {
				break;
			}
		}
	}

}
