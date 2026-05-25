package com.lingx.jtools.ui.dialog;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.lingx.jtools.ui.FlexLayout;
import com.lingx.jtools.ui.JttoolsFrame;
import com.lingx.jtools.ui.MJTextField;
import com.lingx.jtools.ui.PropUtils;

public class DialogCustomCommand extends JDialog {

	private final MJTextField serverIpField;
	private final MJTextField serverPortField;
	private final MJTextField userField;
	private final MJTextField passField;
	private final MJTextField uniqueIdField;
	private final MJTextField deviceIdField;
	private final MJTextField descField;
	private final MJTextField dataField;
	private final JCheckBox textChannelBox;
	private final JCheckBox noQueueBox;
	private final JButton saveButton;
	private final JButton sendButton;
	private final JButton cancelButton;

	public DialogCustomCommand(String serverIp, String uniqueId) {
		super();
		DialogCustomCommand self = this;
		this.setTitle("自定义指令");
		this.setSize(760, 420);
		this.setLocationRelativeTo(null);
		this.getContentPane().setBackground(Color.decode("#dfe9f6"));
		this.setBackground(Color.decode("#dfe9f6"));
		this.setLayout(new FlexLayout(32, 5, 10));

		ImageIcon iconSave = new ImageIcon(JttoolsFrame.class.getResource("/images/disk.png"));
		ImageIcon iconCancel = new ImageIcon(JttoolsFrame.class.getResource("/images/cancel.png"));

		serverIpField = new MJTextField(defaultValue(serverIp, PropUtils.getProp("server.ip", "121.40.187.223")));
		serverPortField = new MJTextField(PropUtils.getProp("traccar.http.port", "8082"));
		userField = new MJTextField(PropUtils.getProp("traccar.user", "admin"));
		passField = new MJTextField(PropUtils.getProp("traccar.password", "admin"));
		uniqueIdField = new MJTextField(defaultValue(uniqueId, PropUtils.getProp("device.tid", "")));
		deviceIdField = new MJTextField(PropUtils.getProp("traccar.deviceId", ""));
		descField = new MJTextField(PropUtils.getProp("traccar.command.desc", "JT808自定义指令"));
		dataField = new MJTextField(PropUtils.getProp("traccar.command.data", "7E01027E"));
		textChannelBox = new JCheckBox("textChannel");
		noQueueBox = new JCheckBox("noQueue");
		textChannelBox.setSelected("true".equalsIgnoreCase(PropUtils.getProp("traccar.command.textChannel", "false")));
		noQueueBox.setSelected("true".equalsIgnoreCase(PropUtils.getProp("traccar.command.noQueue", "false")));

		saveButton = new JButton("保存到指令表");
		saveButton.setIcon(iconSave);
		sendButton = new JButton("发送(并保存)");
		sendButton.setIcon(iconSave);
		cancelButton = new JButton("取消");
		cancelButton.setIcon(iconCancel);

		this.add(new JLabel("说明:", JLabel.RIGHT), "flex:2");
		this.add(new JLabel("发送会先写入Traccar指令表(/api/commands)，再下发到设备(/api/commands/send)"), "flex:10;wrap;");

		this.add(new JLabel("服务器IP:", JLabel.RIGHT), "flex:2");
		this.add(serverIpField, "flex:4");
		this.add(new JLabel("HTTP端口:", JLabel.RIGHT), "flex:2");
		this.add(serverPortField, "flex:4;wrap;");

		this.add(new JLabel("Traccar账号:", JLabel.RIGHT), "flex:2");
		this.add(userField, "flex:4");
		this.add(new JLabel("Traccar密码:", JLabel.RIGHT), "flex:2");
		this.add(passField, "flex:4;wrap;");

		this.add(new JLabel("设备号(uniqueId):", JLabel.RIGHT), "flex:2");
		this.add(uniqueIdField, "flex:4");
		this.add(new JLabel("目标deviceId(可空):", JLabel.RIGHT), "flex:2");
		this.add(deviceIdField, "flex:4;wrap;");

		this.add(new JLabel("指令名称:", JLabel.RIGHT), "flex:2");
		this.add(descField, "flex:10;wrap;");
		this.add(new JLabel("指令数据(data):", JLabel.RIGHT), "flex:2");
		this.add(dataField, "flex:10;wrap;");
		this.add(new JLabel("选项:", JLabel.RIGHT), "flex:2");
		this.add(textChannelBox, "flex:2");
		this.add(noQueueBox, "flex:2;wrap;");

		this.add(new JLabel(), "flex:6");
		this.add(saveButton, "flex:2");
		this.add(sendButton, "flex:2");
		this.add(cancelButton, "flex:2");

		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				execute(false);
			}
		});
		sendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				execute(true);
			}
		});
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				self.dispose();
			}
		});
		this.setModal(true);
	}

	private void execute(boolean alsoSend) {
		final String serverIp = serverIpField.getText().trim();
		final String port = serverPortField.getText().trim();
		final String user = userField.getText().trim();
		final String pass = passField.getText().trim();
		final String uniqueId = uniqueIdField.getText().trim();
		final String deviceId = deviceIdField.getText().trim();
		final String description = descField.getText().trim();
		final String data = dataField.getText().trim();
		final boolean textChannel = textChannelBox.isSelected();
		final boolean noQueue = noQueueBox.isSelected();

		if (serverIp.isEmpty() || port.isEmpty()) {
			JOptionPane.showMessageDialog(this, "服务器IP和HTTP端口不能为空");
			return;
		}
		if (user.isEmpty() || pass.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Traccar账号密码不能为空");
			return;
		}
		if (description.isEmpty()) {
			JOptionPane.showMessageDialog(this, "指令名称不能为空");
			return;
		}
		if (data.isEmpty()) {
			JOptionPane.showMessageDialog(this, "指令数据(data)不能为空");
			return;
		}
		if (alsoSend && uniqueId.isEmpty() && deviceId.isEmpty()) {
			JOptionPane.showMessageDialog(this, "发送时至少需要设备号(uniqueId)或deviceId");
			return;
		}
		if (!deviceId.isEmpty() && !isNumber(deviceId)) {
			JOptionPane.showMessageDialog(this, "deviceId必须是数字");
			return;
		}

		persist(serverIp, port, user, pass, uniqueId, deviceId, description, data, textChannel, noQueue);
		setLoading(true);

		new Thread(() -> {
			try {
				String baseUrl = "http://" + serverIp + ":" + port;
				AuthTry authTry = resolveAuthForCommands(baseUrl, user, pass);
				String auth = authTry.authHeader;

				String createBody = buildCreateBody(description, data, textChannel, noQueue);
				HttpResult createResult = httpRequest("POST", baseUrl + "/api/commands", auth, createBody);
				if (createResult.code < 200 || createResult.code >= 300) {
					throw new Exception("保存指令失败 HTTP " + createResult.code + " " + shortBody(createResult.body));
				}
				StringBuilder message = new StringBuilder();
				message.append("保存成功: /api/commands HTTP ").append(createResult.code);

				if (alsoSend) {
					int targetDeviceId = resolveDeviceId(baseUrl, auth, uniqueId, deviceId);
					String sendBody = buildSendBody(targetDeviceId, data, textChannel, noQueue);
					HttpResult sendResult = httpRequest("POST", baseUrl + "/api/commands/send", auth, sendBody);
					if (sendResult.code < 200 || sendResult.code >= 300) {
						throw new Exception("发送失败 HTTP " + sendResult.code + " " + shortBody(sendResult.body));
					}
					message.append("\n发送成功: /api/commands/send HTTP ").append(sendResult.code);
					message.append("\n目标deviceId: ").append(targetDeviceId);
				}
				if (!authTry.finalUser.equals(user)) {
					message.append("\n鉴权账号已自动回退为: ").append(authTry.finalUser);
					PropUtils.setProp("traccar.user", authTry.finalUser);
					PropUtils.save();
				}

				SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(DialogCustomCommand.this, message.toString()));
			} catch (Exception ex) {
				SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(DialogCustomCommand.this, ex.getMessage()));
			} finally {
				SwingUtilities.invokeLater(() -> setLoading(false));
			}
		}, "traccar-custom-command").start();
	}

	private void setLoading(boolean loading) {
		saveButton.setEnabled(!loading);
		sendButton.setEnabled(!loading);
		cancelButton.setEnabled(!loading);
	}

	private void persist(String serverIp, String port, String user, String pass, String uniqueId, String deviceId,
			String description, String data, boolean textChannel, boolean noQueue) {
		PropUtils.setProp("server.ip", serverIp);
		PropUtils.setProp("traccar.http.port", port);
		PropUtils.setProp("traccar.user", user);
		PropUtils.setProp("traccar.password", pass);
		PropUtils.setProp("traccar.deviceId", deviceId);
		PropUtils.setProp("device.tid", uniqueId);
		PropUtils.setProp("traccar.command.desc", description);
		PropUtils.setProp("traccar.command.data", data);
		PropUtils.setProp("traccar.command.textChannel", textChannel ? "true" : "false");
		PropUtils.setProp("traccar.command.noQueue", noQueue ? "true" : "false");
		PropUtils.save();
	}

	private static String buildCreateBody(String description, String data, boolean textChannel, boolean noQueue) {
		return "{"
				+ "\"type\":\"custom\","
				+ "\"description\":\"" + jsonEscape(description) + "\","
				+ "\"textChannel\":" + textChannel + ","
				+ "\"deviceId\":0,"
				+ "\"attributes\":{\"data\":\"" + jsonEscape(data) + "\",\"noQueue\":" + noQueue + "}"
				+ "}";
	}

	private static String buildSendBody(int deviceId, String data, boolean textChannel, boolean noQueue) {
		return "{"
				+ "\"deviceId\":" + deviceId + ","
				+ "\"type\":\"custom\","
				+ "\"textChannel\":" + textChannel + ","
				+ "\"attributes\":{\"data\":\"" + jsonEscape(data) + "\",\"noQueue\":" + noQueue + "}"
				+ "}";
	}

	private static int resolveDeviceId(String baseUrl, String auth, String uniqueId, String manualDeviceId) throws Exception {
		if (manualDeviceId != null && !manualDeviceId.trim().isEmpty()) {
			return Integer.parseInt(manualDeviceId.trim());
		}
		HttpResult devices = httpRequest("GET", baseUrl + "/api/devices", auth, null);
		if (devices.code != 200) {
			throw new Exception("查询设备失败 HTTP " + devices.code);
		}
		String uid = uniqueId == null ? "" : uniqueId.trim();
		if (uid.isEmpty()) {
			throw new Exception("设备号(uniqueId)为空，无法自动匹配deviceId");
		}
		String marker = "\"uniqueId\":\"" + jsonEscape(uid) + "\"";
		int uniqueIdx = devices.body.indexOf(marker);
		if (uniqueIdx < 0) {
			throw new Exception("未找到设备号(uniqueId): " + uid);
		}
		int idIdx = devices.body.lastIndexOf("\"id\":", uniqueIdx);
		if (idIdx < 0) {
			throw new Exception("匹配到设备号但未解析到deviceId");
		}
		int start = idIdx + "\"id\":".length();
		int end = start;
		while (end < devices.body.length() && Character.isDigit(devices.body.charAt(end))) {
			end++;
		}
		if (end <= start) {
			throw new Exception("deviceId解析失败");
		}
		return Integer.parseInt(devices.body.substring(start, end));
	}

	private static AuthTry resolveAuthForCommands(String baseUrl, String user, String pass) throws Exception {
		List<String> users = buildUserCandidates(user);
		Exception last = null;
		for (String candidate : users) {
			String auth = buildBasicAuth(candidate, pass);
			try {
				HttpResult probe = httpRequest("GET", baseUrl + "/api/server", auth, null);
				if (probe.code >= 200 && probe.code < 300) {
					return new AuthTry(candidate, auth);
				}
				if (probe.code != 401) {
					last = new Exception("鉴权探测失败 HTTP " + probe.code + " " + shortBody(probe.body));
				}
			} catch (Exception e) {
				last = e;
			}
		}
		if (last != null) {
			throw last;
		}
		throw new Exception("账号密码错误，请检查Traccar账号（例如 admin/admin）");
	}

	private static List<String> buildUserCandidates(String user) {
		Set<String> set = new LinkedHashSet<>();
		if (user != null && !user.trim().isEmpty()) {
			set.add(user.trim());
			int p = user.indexOf('@');
			if (p > 0) {
				set.add(user.substring(0, p));
			}
		}
		set.add("admin");
		return new ArrayList<>(set);
	}

	private static String buildBasicAuth(String user, String pass) throws Exception {
		return "Basic " + Base64.getEncoder()
				.encodeToString((user + ":" + pass).getBytes("UTF-8"));
	}

	private static HttpResult httpRequest(String method, String urlStr, String authHeader, String body) throws Exception {
		HttpURLConnection conn = null;
		try {
			URL url = new URL(urlStr);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod(method);
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(8000);
			conn.setRequestProperty("Authorization", authHeader);
			if (body != null) {
				conn.setDoOutput(true);
				conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
				conn.getOutputStream().write(body.getBytes("UTF-8"));
				conn.getOutputStream().flush();
			}
			int code = conn.getResponseCode();
			String responseBody = readResponseBody(conn);
			return new HttpResult(code, responseBody);
		} finally {
			if (conn != null) conn.disconnect();
		}
	}

	private static String readResponseBody(HttpURLConnection conn) throws Exception {
		InputStream stream = conn.getResponseCode() >= 400 ? conn.getErrorStream() : conn.getInputStream();
		if (stream == null) {
			return "";
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			sb.append(line);
		}
		reader.close();
		return sb.toString();
	}

	private static String shortBody(String text) {
		if (text == null) {
			return "";
		}
		String t = text.trim();
		if (t.length() > 180) {
			return t.substring(0, 180) + "...";
		}
		return t;
	}

	private static String jsonEscape(String s) {
		if (s == null) {
			return "";
		}
		return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "\\r").replace("\n", "\\n");
	}

	private static boolean isNumber(String value) {
		if (value == null || value.trim().isEmpty()) {
			return false;
		}
		for (int i = 0; i < value.length(); i++) {
			if (!Character.isDigit(value.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	private static String defaultValue(String first, String second) {
		if (first != null && !first.trim().isEmpty()) {
			return first.trim();
		}
		return second == null ? "" : second;
	}

	private static class HttpResult {
		private final int code;
		private final String body;

		private HttpResult(int code, String body) {
			this.code = code;
			this.body = body == null ? "" : body;
		}
	}

	private static class AuthTry {
		private final String finalUser;
		private final String authHeader;

		private AuthTry(String finalUser, String authHeader) {
			this.finalUser = finalUser;
			this.authHeader = authHeader;
		}
	}
}
