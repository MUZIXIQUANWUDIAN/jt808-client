package com.lingx.jtools.ui;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * Per-tab UI bridge for TCP client views: log area and disconnect button reset, safe from Netty threads.
 */
public final class TcpClientTabBridge {

	private JTextArea logArea;
	private Runnable onDisconnectedUi;

	public void bind(JTextArea log, Runnable onDisconnectedButtons) {
		this.logArea = log;
		this.onDisconnectedUi = onDisconnectedButtons;
	}

	public void appendLog(String line) {
		String text = line + "\r\n";
		JTextArea ta = logArea;
		if (ta == null) {
			System.out.print(text);
			return;
		}
		if (SwingUtilities.isEventDispatchThread()) {
			ta.insert(text, 0);
		} else {
			SwingUtilities.invokeLater(() -> {
				JTextArea t = logArea;
				if (t != null) {
					t.insert(text, 0);
				}
			});
		}
	}

	/** Same format as the legacy TCP client tab: {@code HH:mm:ss -> message}. */
	public void appendArrowLog(String message) {
		String line = new SimpleDateFormat("HH:mm:ss").format(new Date()) + " -> " + message;
		appendLog(line);
	}

	public void clearLog() {
		JTextArea ta = logArea;
		if (ta == null) {
			return;
		}
		Runnable r = () -> {
			JTextArea t = logArea;
			if (t != null) {
				t.setText("");
			}
		};
		if (SwingUtilities.isEventDispatchThread()) {
			r.run();
		} else {
			SwingUtilities.invokeLater(r);
		}
	}

	public void notifyDisconnected() {
		Runnable r = onDisconnectedUi;
		if (r == null) {
			return;
		}
		SwingUtilities.invokeLater(r);
	}
}
