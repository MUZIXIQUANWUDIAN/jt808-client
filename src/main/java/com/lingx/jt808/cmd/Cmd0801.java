package com.lingx.jt808.cmd;

/**
 * 多媒体文件上报
 * @author lingx.com
 *
 */
public class Cmd0801 extends AbstractJT808Command {
	
	public Cmd0801(String tid,byte data[] , int max, int ind) {
		super(0x0801, tid,data,max,ind);
	}
	
}
