package com.lingx.jt808.thread;

import com.lingx.jt808.JT808Tools;
import com.lingx.jt808.cmd.Cmd0200;
import com.lingx.jt808.netty.TcpClient;
import com.lingx.jtools.ui.MJTextField;
import com.lingx.jtools.ui.PropUtils;

public class Send0x0200Thread implements Runnable {
	public Send0x0200Thread(String tid) {
		this.tid=tid;
	}
	public Send0x0200Thread(String tid, double lat, double lng, float speed, int height, int fx, float meilage,
			float oil, int bj, int zt, int time) {
		this.tid=tid;
		this.lat=lat;
		this.lng=lng;
		this.speed=speed;
		this.height=height;
		this.fx=fx;
		this.meilage=meilage;
		this.oil=oil;
		this.bj=bj;
		this.zt=zt;
		this.time=time;
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

	@Override
	public void run() {
		while (isRun) {
			try {
				if(TcpClient.channel!=null) {

					this.lat=Double.parseDouble(PropUtils.getProp("jt808.0x0200.lat"));
					this.lng=Double.parseDouble(PropUtils.getProp("jt808.0x0200.lng"));
					this.speed=Float.parseFloat(PropUtils.getProp("jt808.0x0200.speed"));
					this.height=Integer.parseInt(PropUtils.getProp("jt808.0x0200.height"));
					this.fx=Integer.parseInt(PropUtils.getProp("jt808.0x0200.direction"));
					this.meilage=Float.parseFloat(PropUtils.getProp("jt808.0x0200.mileage"));
					this.oil=Float.parseFloat(PropUtils.getProp("jt808.0x0200.oil"));
					this.time=Integer.parseInt(PropUtils.getProp("jt808.0x0200.interval"));
				this.bj=0;
				if("true".equals(PropUtils.getProp("jt808.0x0200.bj0")))bj+=1;
				if("true".equals(PropUtils.getProp("jt808.0x0200.bj1")))bj+=2;
				if("true".equals(PropUtils.getProp("jt808.0x0200.bj2")))bj+=4;
				this.zt=0;
				if("true".equals(PropUtils.getProp("jt808.0x0200.zt0")))zt+=1;
				if("true".equals(PropUtils.getProp("jt808.0x0200.zt1")))zt+=2;
				Cmd0200 cmd = new Cmd0200(tid, lat, lng, speed, height, fx, meilage, oil, bj, zt);
				String hexstring = cmd.toMessageHexstring();
				JT808Tools.sendMessage(hexstring);
				Thread.currentThread().sleep(time * 1000);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void send() {
		Cmd0200 cmd = new Cmd0200(tid, lat, lng, speed, height, fx, meilage, oil, bj, zt);
		String hexstring = cmd.toMessageHexstring();
		JT808Tools.sendMessage(hexstring);
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
