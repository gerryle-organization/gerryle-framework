package com.gerryle.framework.util.lang;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import scala.sys.process.ProcessBuilderImpl.Simple;


public class DateUtil {

	/** 日期格式化对象 格式为yyyy/MM/dd HH:mm */
	public static SimpleDateFormat DATE_FORMATER_1 = new SimpleDateFormat("yyyy/MM/dd HH:mm");
	/** 日期格式化对象 格式为yyyy-MM-dd HH:mm:ss */
	public static SimpleDateFormat DATE_FORMATER_2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	/** 日期格式化对象 格式为MM-dd HH:mm */
	public static SimpleDateFormat DATE_FORMATER_3 = new SimpleDateFormat("MM-dd HH:mm");
	public static SimpleDateFormat DATE_FORMATER_4 = new SimpleDateFormat("yyyy-MM-dd");
	public static SimpleDateFormat DATE_FORMATER_5 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	/** 一天的毫秒数 */
	public static long DAY_MS = 1000 * 24 * 60 * 60;

	/** yyyy-MM-dd HH:mm:ss */
	public static final String pattern1 = "yyyy-MM-dd HH:mm:ss";
	/** yyyy-MM-dd */
	public static final String pattern2 = "yyyy-MM-dd";
	public static final String pattern3 = "yyyy-MM-dd HH:mm";
	public static final String pattern4 = "yyyyMMddHHmm";
	public static final String BEGIN_TIME = "yyyy-MM-dd 00:00:00";
	public static final String END_TIME = "yyyy-MM-dd 23:59:59";

	/** yyyy-MM-dd的正则表达式 */
	private static final String yyyy_MM_dd_EL = "^(?:(?!0000)[0-9]{4}([-/.]?)(?:(?:0?[1-9]|1[0-2])([-/.]?)(?:0?[1-9]|1[0-9]|2[0-8])|(?:0?[13-9]|1[0-2])([-/.]?)(?:29|30)|(?:0?[13578]|1[02])([-/.]?)31)|(?:[0-9]{2}(?:0[48]|[2468][048]|[13579][26])|(?:0[48]|[2468][048]|[13579][26])00)([-/.]?)0?2([-/.]?)29)$";
   
	/**
	 * 获取当前时间指定格式 yyyy-MM-dd HH:mm:ss
	 * @param pattern
	 * @return
	 * @author Gerryle 2018年2月9日 上午11:00:21
	 */
	public static String getCurTimeStr(String pattern){
		SimpleDateFormat sdf=new SimpleDateFormat(pattern);
		return sdf.format(new Date());
	}
	
	/**
	 * 获取当前时间的面膜人字符串  yyyy-MM-dd HH:mm:ss
	 * @return
	 * @author Gerryle 2018年2月9日 上午11:56:15
	 */
	public static String getDefaultCurTimeStr(){
		return DateFormatThreadLocal.getDateFormat().format(new Date());
	}
	
	/**
	 * 返回指定时间格式的毫秒数
	 * @param timeStr yyyy-MM-dd HH:mm:ss
	 * @param pattern
	 * @return
	 * @author Gerryle 2018年2月9日 下午12:01:47
	 */
	public static long getTime(String timeStr,String pattern){
		try {
			SimpleDateFormat sdf=new SimpleDateFormat(pattern);
			Date date=sdf.parse(timeStr);
			return date.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	/**
	 * 返回指定时间格式的日期
	 * @param timeStr
	 * @param pattern
	 * @return
	 * @author Gerryle 2018年2月9日 下午2:25:24
	 */
	public static Date getDate(String timeStr,String pattern){
		try {
			SimpleDateFormat sdf=new SimpleDateFormat(pattern);
			return sdf.parse(timeStr);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
}
