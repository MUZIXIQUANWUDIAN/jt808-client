package com.lingx.jtools.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.HashMap;
import java.util.Map;

public class FlexLayout implements LayoutManager {
	private Map<Component,String> map=new HashMap<>();
	private String direction="column";//column,column-reverse,row,row-reverse,
	private int rowsHeight=-1;//32
	private int cellSpacing=0;//5
	private int margin=0;//10
	public FlexLayout() {}
	public FlexLayout(int rowsHeight,int cellSpacing,int margin) {
		this.rowsHeight=rowsHeight;
		this.cellSpacing=cellSpacing;
		this.margin=margin;
	}
	
	@Override
	public void addLayoutComponent(String name, Component comp) {
		map.put(comp, name);
	}

	@Override
	public void removeLayoutComponent(Component comp) {

	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		 int ncomponents = parent.getComponentCount();
    	 int w=0,h=0;
		  for (int i = 0 ; i < ncomponents ; i++) {
	            Component comp = parent.getComponent(i);
	            Dimension d = comp.getPreferredSize();
	            if (w < d.width) {
	                w = d.width;
	            }
	            if (h < d.height) {
	                h = d.height;
	            }
	        }
		return new Dimension(w,h);
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
        int ncomponents = parent.getComponentCount();
    	 int w=0,h=0;
		  for (int i = 0 ; i < ncomponents ; i++) {
	            Component comp = parent.getComponent(i);
	            Dimension d = comp.getMinimumSize();
	            if (w < d.width) {
	                w = d.width;
	            }
	            if (h < d.height) {
	                h = d.height;
	            }
	        }
		return new Dimension(w,h);
	}
	
	@Override
	public void layoutContainer(Container parent) {
		Insets insets = parent.getInsets();
	    double maxWidth = parent.getWidth() - insets.left - insets.right ;
	    int height=this.rowsHeight;
	    if(this.rowsHeight==-1) {
	    	height=parent.getHeight();
	    }
	    double width=(maxWidth-cellSpacing*11-2*margin)/12;//这里的11是因为12个格子，只需要11个间距
	    int i=0;//1行12格的递增
	    double widthDifference=0;//差值，自定义宽度引起
	    int top=0;//累计高度
	    top+=insets.top;
	    top+=margin;
		for ( Component comp:parent.getComponents()) {
			if(this.rowsHeight>0)height=this.rowsHeight;
			int flex=this.getFlex(comp);
			int wrap=0;
			boolean isgrow=this.isGrow(comp);
			boolean iswrap=this.isWrap(comp);
			if(isgrow) {
				flex=12-(i%12);
			}else if(iswrap) {
				wrap=12-(i%12)-flex;
            }
			double x = insets.left + (width+cellSpacing)*(i%12)+margin-widthDifference;
            int rows=i/12;
            int height2=this.getHeight(comp);//自定义高度
            if(height2>0)height=height2;
            //int y = insets.top + rows*(height+cellSpacing)+margin;
            
            double width1=width*flex+(cellSpacing*(flex-1))+widthDifference;//12格的标准宽度
            int width2=this.getWidth(comp);//自定义宽度
            double w=0;
            if(width2==-1) {
            	w=width1;
            	widthDifference=0;
            }else {
            	w=width2;
            	widthDifference=(width1-width2);
            }
            i=i+flex;
            i=i+wrap;
            comp.setBounds(new Double(x).intValue(), top,new Double(w).intValue() ,height);
            
            if(isgrow||iswrap||i%12==0) {
            	widthDifference=0;//换行的话，重新计算差值
            	top+=height+cellSpacing;
            }
        }
	}
	private int getWidth(Component comp) {
		try {
			if(this.map.containsKey(comp)&&this.map.get(comp).contains("width")) {
				String temp=this.map.get(comp);
				String array[]=temp.split(";");
				for(String str:array) {
					if(str.contains("width")) {
						return Integer.parseInt(str.split(":")[1].replace("px", ""));
					}
				}
			}
		} catch (Exception e) {
		}
		return -1;//auto
	}
	private int getHeight(Component comp) {
		try {
			if(this.map.containsKey(comp)&&this.map.get(comp).contains("height")) {
				String temp=this.map.get(comp);
				String array[]=temp.split(";");
				for(String str:array) {
					if(str.contains("height")) {
						return Integer.parseInt(str.split(":")[1].replace("px", ""));
					}
				}
			}
		} catch (Exception e) {
		}
		return -1;//auto
	}
	private int getFlex(Component comp) {
		try {
			if(this.map.containsKey(comp)&&this.map.get(comp).contains("flex")) {
				String temp=this.map.get(comp);
				String array[]=temp.split(";");
				for(String str:array) {
					if(str.contains("flex")) {
						return Integer.parseInt(str.split(":")[1]);
					}
				}
			}
		} catch (Exception e) {
		}
		return 1;
	}
	private boolean isGrow(Component comp) {
		if(this.map.containsKey(comp))return this.map.get(comp).contains("grow");
		return false;
	}
	private boolean isWrap(Component comp) {
		if(this.map.containsKey(comp))return this.map.get(comp).contains("wrap");
		return false;
	}
	public int getRowsHeight() {
		return rowsHeight;
	}
	public void setRowsHeight(int rowsHeight) {
		this.rowsHeight = rowsHeight;
	}
	public int getCellSpacing() {
		return cellSpacing;
	}
	public void setCellSpacing(int cellSpacing) {
		this.cellSpacing = cellSpacing;
	}
	public int getMargin() {
		return margin;
	}
	public void setMargin(int margin) {
		this.margin = margin;
	}
	
}
