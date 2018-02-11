package com.gerryle.framework.util.lang;

public class StringUtil {

	/**
	 * 判断字符串是否为null或空
	 * @param str
	 * @return
	 * @author Gerryle 2018年2月11日 下午4:07:37
	 */
	public static boolean isNullOrEmpty(String str){
		return str==null||str.isEmpty();
	}
	
	/**
	 * 判断字符串是否为null或空字符串
	 * @param str
	 * @return
	 * @author Gerryle 2018年2月11日 下午4:09:32
	 */
	public static boolean isNullOrBlank(String str){
		return str==null || "".equals(str.trim());
	}
}
