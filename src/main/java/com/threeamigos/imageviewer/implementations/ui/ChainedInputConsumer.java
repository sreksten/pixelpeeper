package com.threeamigos.imageviewer.implementations.ui;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.threeamigos.imageviewer.interfaces.ui.InputConsumer;

public class ChainedInputConsumer implements InputConsumer {

	private Map<Integer, List<InputConsumer>> inputConsumers = new TreeMap<>(Comparator.reverseOrder());
	List<InputConsumer> sortedConsumers = Collections.emptyList();

	public void addConsumer(InputConsumer adapter, int priority) {
		inputConsumers.computeIfAbsent(priority, key -> new ArrayList<>()).add(adapter);
		sortedConsumers = new LinkedList<>();
		inputConsumers.entrySet().stream().map(entry -> entry.getValue()).forEach(sortedConsumers::addAll);
	}

	public void removeConsumer(InputConsumer consumer) {
		inputConsumers.entrySet().stream().map(entry -> entry.getValue()).forEach(list -> list.remove(consumer));
		sortedConsumers.remove(consumer);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		for (InputConsumer consumer : sortedConsumers) {
			consumer.mouseWheelMoved(e);
			if (e.isConsumed()) {
				break;
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		for (InputConsumer consumer : sortedConsumers) {
			consumer.mouseClicked(e);
			if (e.isConsumed()) {
				break;
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		for (InputConsumer consumer : sortedConsumers) {
			consumer.mousePressed(e);
			if (e.isConsumed()) {
				break;
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		for (InputConsumer consumer : sortedConsumers) {
			consumer.mouseReleased(e);
			if (e.isConsumed()) {
				break;
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		for (InputConsumer consumer : sortedConsumers) {
			consumer.mouseEntered(e);
			if (e.isConsumed()) {
				break;
			}
		}
	}

	@Override
	public void mouseExited(MouseEvent e) {
		for (InputConsumer adapter : sortedConsumers) {
			adapter.mouseExited(e);
			if (e.isConsumed()) {
				break;
			}
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		for (InputConsumer consumer : sortedConsumers) {
			consumer.mouseDragged(e);
			if (e.isConsumed()) {
				break;
			}
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		for (InputConsumer consumer : sortedConsumers) {
			consumer.mouseMoved(e);
			if (e.isConsumed()) {
				break;
			}
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		for (InputConsumer consumer : sortedConsumers) {
			consumer.keyTyped(e);
			if (e.isConsumed()) {
				break;
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		for (InputConsumer consumer : sortedConsumers) {
			consumer.keyPressed(e);
			if (e.isConsumed()) {
				break;
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		for (InputConsumer consumer : sortedConsumers) {
			consumer.keyReleased(e);
			if (e.isConsumed()) {
				break;
			}
		}
	}

}
