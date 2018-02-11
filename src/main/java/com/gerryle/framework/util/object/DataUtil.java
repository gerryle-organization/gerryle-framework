package com.gerryle.framework.util.object;

import com.google.gson.Gson;

/**
 * 数据操作工具类
 * @author Gerryle 2018年2月11日 下午6:03:44
 */
public class DataUtil {

	private static Gson gson=new Gson();
	
	public static <T> T getDataFromRedisOrMongoDbById(Object id,String cacheKey,Class<T> clazz){
		return null;
	}
}
