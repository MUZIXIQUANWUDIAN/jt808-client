package com.lingx.jtools.ui.dialog;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;

import com.lingx.jtools.ui.FlexLayout;
import com.lingx.jtools.ui.JttoolsFrame;
import com.lingx.jtools.ui.MJTextField;
import com.lingx.jtools.ui.PropUtils;

public class Dialog0x0200 extends JDialog{

	public Dialog0x0200() {
		super();
		Dialog0x0200 _this=this;
		this.setTitle("注册设置");
		this.setSize(600,400);
		this.setLocationRelativeTo(null);
		this.getContentPane().setBackground(Color.decode("#dfe9f6"));
		this.setBackground(Color.decode("#dfe9f6"));
		this.setLayout(new FlexLayout(32,5,10));

        ImageIcon icon3 = new ImageIcon(JttoolsFrame.class.getResource("/images/disk.png"));

        ImageIcon icon4 = new ImageIcon(JttoolsFrame.class.getResource("/images/cancel.png"));
        JButton button=new JButton("保存");
        button.setIcon(icon3);
        JButton button2=new JButton("取消");
        button2.setIcon(icon4);
        MJTextField text1 = new MJTextField(PropUtils.getProp("jt808.0x0200.lng"));
        MJTextField text2 = new MJTextField(PropUtils.getProp("jt808.0x0200.lat"));
        MJTextField text3 = new MJTextField(PropUtils.getProp("jt808.0x0200.speed"));
        MJTextField text4 = new MJTextField(PropUtils.getProp("jt808.0x0200.height"));
        MJTextField text5 = new MJTextField(PropUtils.getProp("jt808.0x0200.direction"));
        MJTextField text6 = new MJTextField(PropUtils.getProp("jt808.0x0200.mileage"));
        MJTextField text7 = new MJTextField(PropUtils.getProp("jt808.0x0200.oil"));
        MJTextField text8 = new MJTextField(PropUtils.getProp("jt808.0x0200.interval"));
        
		this.add(new JLabel("说明:",JLabel.RIGHT),"flex:2");
		this.add(new JLabel("新参数保存后在下次位置上报时生效"),"flex:10;");
		this.add(new JLabel("经度LNG:",JLabel.RIGHT),"flex:2");
		this.add(text1,"flex:4");
		this.add(new JLabel("纬度LAT:",JLabel.RIGHT),"flex:2");
		this.add(text2,"flex:4");
		
		this.add(new JLabel("时速:",JLabel.RIGHT),"flex:2");
		this.add(text3,"flex:4");
		this.add(new JLabel("高程:",JLabel.RIGHT),"flex:2");
		this.add(text4,"flex:4");
		
		this.add(new JLabel("方向:",JLabel.RIGHT),"flex:2");
		this.add(text5,"flex:4");
		this.add(new JLabel("里程:",JLabel.RIGHT),"flex:2");
		this.add(text6,"flex:4");

		this.add(new JLabel("油量:",JLabel.RIGHT),"flex:2");
		this.add(text7,"flex:4;");

		this.add(new JLabel("上报间隔:",JLabel.RIGHT),"flex:2");
		this.add(text8,"flex:4");

		this.add(new JLabel("报警标志:",JLabel.RIGHT),"flex:2");
		JCheckBox bj0 = new JCheckBox("紧急报警");
	    JCheckBox bj1 = new JCheckBox("超速报警");
	    JCheckBox bj2 = new JCheckBox("疲劳报警");
	    bj0.setSelected("true".equals(PropUtils.getProp("jt808.0x0200.bj0")));
	    bj1.setSelected("true".equals(PropUtils.getProp("jt808.0x0200.bj1")));
	    bj2.setSelected("true".equals(PropUtils.getProp("jt808.0x0200.bj2")));
		this.add(bj0,"flex:2");
		this.add(bj1,"flex:2");
		this.add(bj2,"flex:2;wrap;");

		this.add(new JLabel("设备状态:",JLabel.RIGHT),"flex:2");
		JCheckBox zt0 = new JCheckBox("车辆点火");
	    JCheckBox zt1 = new JCheckBox("卫星定位");
	    zt0.setSelected("true".equals(PropUtils.getProp("jt808.0x0200.zt0")));
	    zt1.setSelected("true".equals(PropUtils.getProp("jt808.0x0200.zt1")));
		this.add(zt0,"flex:2");
		this.add(zt1,"flex:2;wrap;");
		
		this.add(new JLabel(),"flex:4;wrap;");
		this.add(new JLabel(),"flex:4;");
		this.add(button,"flex:2");
		this.add(button2,"flex:2");
		button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					PropUtils.setProp("jt808.0x0200.lng",text1.getText().trim());
					PropUtils.setProp("jt808.0x0200.lat",text2.getText().trim());
					PropUtils.setProp("jt808.0x0200.speed",text3.getText().trim());
					PropUtils.setProp("jt808.0x0200.height",text4.getText().trim());
					PropUtils.setProp("jt808.0x0200.direction",text5.getText().trim());
					PropUtils.setProp("jt808.0x0200.mileage",text6.getText().trim());
					PropUtils.setProp("jt808.0x0200.oil",text7.getText().trim());
					PropUtils.setProp("jt808.0x0200.interval",text8.getText().trim());
					
					PropUtils.setProp("jt808.0x0200.bj0",bj0.isSelected()?"true":"false");
					PropUtils.setProp("jt808.0x0200.bj1",bj1.isSelected()?"true":"false");
					PropUtils.setProp("jt808.0x0200.bj2",bj2.isSelected()?"true":"false");
					PropUtils.setProp("jt808.0x0200.zt0",zt0.isSelected()?"true":"false");
					PropUtils.setProp("jt808.0x0200.zt1",zt1.isSelected()?"true":"false");
					PropUtils.save();
					_this.dispose();
				}});
		button2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				_this.dispose();
			}});
		this.setModal(true);
	}
}
