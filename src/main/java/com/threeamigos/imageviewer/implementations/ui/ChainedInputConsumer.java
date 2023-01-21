package com.threeamigos.imageviewer.implementations.ui;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.threeamigos.imageviewer.interfaces.ui.InputConsumer;
import com.threeamigos.imageviewer.interfaces.ui.PrioritizedInputConsumer;

public class ChainedInputConsumer implements InputConsumer {

	private List<PrioritizedInputConsumer> inputConsumers = new LinkedList<>();

	public void addConsumer(PrioritizedInputConsumer adapter) {
		inputConsumers.add(adapter);
		Collections.sort(inputConsumers, (a1, a2) -> {
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

	public void removeConsumer(PrioritizedInputAdapter adapter) {
		inputConsumers.remove(adapter);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		for (InputConsumer adapter : inputConsumers) {
			adapter.mouseWheelMoved(e);
			if (e.isConsumed()) {
				break;
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		for (InputConsumer adapter : inputConsumers) {
			adapter.mouseClicked(e);
			if (e.isConsumed()) {
				break;
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		for (InputConsumer adapter : inputConsumers) {
			adapter.mousePressed(e);
			if (e.isConsumed()) {
				break;
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		for (InputConsumer adapter : inputConsumers) {
			adapter.mouseReleased(e);
			if (e.isConsumed()) {
				break;
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		for (InputConsumer adapter : inputConsumers) {
			adapter.mouseEntered(e);
			if (e.isConsumed()) {
				break;
			}
		}
	}

	@Override
	public void mouseExited(MouseEvent e) {
		for (InputConsumer adapter : inputConsumers) {
			adapter.mouseExited(e);
			if (e.isConsumed()) {
				break;
			}
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		for (InputConsumer adapter : inputConsumers) {
			adapter.mouseDragged(e);
			if (e.isConsumed()) {
				break;
			}
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		for (InputConsumer adapter : inputConsumers) {
			adapter.mouseMoved(e);
			if (e.isConsumed()) {
				break;
			}
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		for (InputConsumer adapter : inputConsumers) {
			adapter.keyTyped(e);
			if (e.isConsumed()) {
				break;
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		for (InputConsumer adapter : inputConsumers) {
			adapter.keyPressed(e);
			if (e.isConsumed()) {
				break;
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		for (InputConsumer adapter : inputConsumers) {
			adapter.keyReleased(e);
			if (e.isConsumed()) {
				break;
			}
		}
	}

}
