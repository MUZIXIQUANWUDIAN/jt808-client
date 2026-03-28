package com.lingx.jtools.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class PropUtils {
	private static Properties prop=null;
	public static void init() {
    	try {
    		prop=new Properties();
			String basePath=System.getProperty("user.dir");
			prop.load(new FileInputStream(basePath+"/config.properties"));
			System.out.println("参数配置:");
			prop.list(System.out);
		} catch (Exception e) {
		}
	}
	
	public static String getProp(String key) {
		return getProp(key,"");
	}
	public static String getProp(String key,String defaultValue) {
		if(prop==null) {
			init();
		}
		String ret=prop.getProperty(key);
		if(ret==null)ret=defaultValue;
		return ret;
	}
	
	public static void setProp(String key,String value) {
		if(prop==null) {
			init();
		}
		prop.setProperty(key, value);
	}
	public static void save() {
		String basePath=System.getProperty("user.dir");
		try {
			File file=new File(basePath+"/config.properties");
			if(!file.exists())file.createNewFile();
			if(prop!=null)prop.store(new FileOutputStream(basePath+"/config.properties"), "========Map Download========");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
