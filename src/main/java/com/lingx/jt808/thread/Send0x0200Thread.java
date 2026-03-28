package com.lingx.jt808.thread;

import com.lingx.jt808.JT808ClientContext;
import com.lingx.jt808.cmd.Cmd0200;
import com.lingx.jtools.ui.PropUtils;

import io.netty.channel.Channel;

public class Send0x0200Thread implements Runnable {

	private final JT808ClientContext session;

	public Send0x0200Thread(JT808ClientContext session, String tid) {
		this.session = session;
		this.tid = tid;
	}

	public Send0x0200Thread(JT808ClientContext session, String tid, double lat, double lng, float speed, int height,
			int fx, float meilage, float oil, int bj, int zt, int time) {
		this.session = session;
		this.tid = tid;
		this.lat = lat;
		this.lng = lng;
		this.speed = speed;
		this.height = height;
		this.fx = fx;
		this.meilage = meilage;
		this.oil = oil;
		this.bj = bj;
		this.zt = zt;
		this.time = time;
	}

	private String tid;
	private double lat;
	private double lng;
	private float speed;
	private int height;
	private int fx;
	private float meilage;
	private float oil;
	private int bj;
	private int zt;
	private int time;
	private boolean isRun = true;

	/** 未连接或断线时避免 while 空转占满 CPU */
	private static final long WAIT_DISCONNECTED_MS = 500L;

	@Override
	public void run() {
		while (isRun) {
			try {
				Channel ch = session.getChannel();
				if (ch != null && ch.isActive()) {
					this.lat = Double.parseDouble(PropUtils.getProp("jt808.0x0200.lat", "0"));
					this.lng = Double.parseDouble(PropUtils.getProp("jt808.0x0200.lng", "0"));
					this.speed = Float.parseFloat(PropUtils.getProp("jt808.0x0200.speed", "0"));
					this.height = Integer.parseInt(PropUtils.getProp("jt808.0x0200.height", "0"));
					this.fx = Integer.parseInt(PropUtils.getProp("jt808.0x0200.direction", "0"));
					this.meilage = Float.parseFloat(PropUtils.getProp("jt808.0x0200.mileage", "0"));
					this.oil = Float.parseFloat(PropUtils.getProp("jt808.0x0200.oil", "0"));
					this.time = Integer.parseInt(PropUtils.getProp("jt808.0x0200.interval", "15"));
					this.bj = 0;
					if ("true".equals(PropUtils.getProp("jt808.0x0200.bj0"))) {
						bj += 1;
					}
					if ("true".equals(PropUtils.getProp("jt808.0x0200.bj1"))) {
						bj += 2;
					}
					if ("true".equals(PropUtils.getProp("jt808.0x0200.bj2"))) {
						bj += 4;
					}
					this.zt = 0;
					if ("true".equals(PropUtils.getProp("jt808.0x0200.zt0"))) {
						zt += 1;
					}
					if ("true".equals(PropUtils.getProp("jt808.0x0200.zt1"))) {
						zt += 2;
					}
					Cmd0200 cmd = new Cmd0200(tid, lat, lng, speed, height, fx, meilage, oil, bj, zt);
					String hexstring = cmd.toMessageHexstring();
					session.sendMessage(hexstring);
					long intervalMs = Math.max(1000L, (long) this.time * 1000L);
					Thread.sleep(intervalMs);
				} else {
					Thread.sleep(WAIT_DISCONNECTED_MS);
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
		}
	}

	public void send() {
		Cmd0200 cmd = new Cmd0200(tid, lat, lng, speed, height, fx, meilage, oil, bj, zt);
		String hexstring = cmd.toMessageHexstring();
		session.sendMessage(hexstring);
	}

	public void setTid(String tid) {
		this.tid = tid;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setFx(int fx) {
		this.fx = fx;
	}

	public void setMeilage(float meilage) {
		this.meilage = meilage;
	}

	public void setOil(float oil) {
		this.oil = oil;
	}

	public void setBj(int bj) {
		this.bj = bj;
	}

	public void setZt(int zt) {
		this.zt = zt;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public void setRun(boolean isRun) {
		this.isRun = isRun;
	}

	public String getTid() {
		return tid;
	}

	public double getLat() {
		return lat;
	}

	public double getLng() {
		return lng;
	}

	public float getSpeed() {
		return speed;
	}

	public int getHeight() {
		return height;
	}

	public int getFx() {
		return fx;
	}

	public float getMeilage() {
		return meilage;
	}

	public float getOil() {
		return oil;
	}

	public int getBj() {
		return bj;
	}

	public int getZt() {
		return zt;
	}

	public int getTime() {
		return time;
	}

	public boolean isRun() {
		return isRun;
	}
}
