package com.lingx.jtools.ui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.lingx.jt808.msg.JT808MessageHandler;

public class JT808DecodePanel extends JPanel {

	private final JT808MessageHandler decodeHandler = new JT808MessageHandler();

	public JT808DecodePanel() {

		this.setBackground(Color.decode("#dfe9f6"));
		this.setLayout(new FlexLayout(32, 5, 10));

		ImageIcon icon1 = new ImageIcon(JttoolsFrame.class.getResource("/images/bullet_lightning.png"));
		JButton startButton = new JButton("解析");
		startButton.setIcon(icon1);
		JTextArea text1 = new MJTextArea(
				"7E0200005E016200130328000A00000820000C10010159B8C406CA2D0700450000000024110410493031010001040000000030011D030200002504000000002A0200002B040000000014040000000515040000000916040000000017020001E10A0201786600FF00FF00FFDB7E");

		JScrollPane scrollPane1 = new JScrollPane(text1);
		this.add(scrollPane1, "flex:12;height:60px");

		this.add(new JLabel(""), "flex:5;");
		this.add(startButton, "flex:2;wrap;");
		JTextArea textArea = new MJTextArea();
		textArea.setEditable(false);
		textArea.setBackground(Color.decode("#ffffff"));
		JScrollPane scrollPane = new JScrollPane(textArea);
		this.add(scrollPane, "flex:12;height:400px");
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String raw = text1.getText();
				if (raw == null) {
					textArea.setText("");
					return;
				}
				String hex = raw.trim().toUpperCase();
				if (!hex.startsWith("7E") || !hex.endsWith("7E")) {
					textArea.setText("非JT808报文");
					return;
				}
				try {
					textArea.setText(decodeHandler.handler(hex));
				} catch (Exception ex) {
					textArea.setText(ex.getMessage());
				}
			}
		});
	}
}
