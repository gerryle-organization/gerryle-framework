package com.gerryle.framework.cache.redis;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.JEditorPane;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.gerryle.framework.util.lang.DateUtil;
import com.gerryle.framework.util.lang.StringUtil;
import com.google.gson.Gson;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisDataSource {

	private JedisPool pool;
	private Gson gson=new Gson();
	
	/**是否已经初始化*/
	private boolean isInit;
	
	/**默认数据过期时间 3天*/
	private static final int DEFAULT_EXPIRE_SECONDS=3 * 24 * 60 * 60;
	
	private RedisDataSource(){}
	
	public static RedisDataSource create(RedisInitParam initParam){
		RedisDataSource ds=new RedisDataSource();
		ds.init(initParam);
		return ds;
	}

	private void init(RedisInitParam initParam) {
		if(isInit){
			return;
		}
		isInit=true;
		System.out.println("redis初始化参数"+initParam);
		/**建立连接池配置参数*/
		JedisPoolConfig config=new JedisPoolConfig();
		//最大空闲连接数
		config.setMaxIdle(initParam.getMaxIdleCount());
		//设置最大阻塞时间，单位是毫秒数
		config.setMaxWaitMillis(initParam.getMaxWaitMillis());
		//最大连接数
		config.setMaxTotal(initParam.getMaxTotalCount());
		//创建连接池
		pool=new JedisPool(config,initParam.getServer(),initParam.getPort(),5000,StringUtil.isNullOrEmpty(initParam.getPassword())?null:initParam.getPassword(),
				initParam.getDatabase());
	}
	
	
	/**
	 * 判断是否存在指定的key
	 * @param key
	 * @return
	 * @author Gerryle 2018年2月9日 下午3:13:31
	 */
	public boolean existskey(String key){
		Jedis jedis=pool.getResource();
		try {
			return jedis.exists(key);
		} catch (Exception e) {
			pool.returnBrokenResource(jedis);
			e.printStackTrace();
			return false;
		}finally{
			closeResource(jedis);
		}
	}

	/**
	 * key表达式
	 * @param keyExpression
	 * @return
	 * @author Gerryle 2018年2月9日 下午3:18:56
	 */
	public Set<String> getKeys(String keyExpression){
		Jedis jedis=pool.getResource();
		try {
			return jedis.keys(keyExpression);
		} catch (Exception e) {
			pool.returnBrokenResource(jedis);
			e.printStackTrace();
			return null;
		}finally{
			closeResource(jedis);
		}
	}
	
	/**
	 * 移除指定的key
	 * @param key
	 * @author Gerryle 2018年2月9日 下午3:21:10
	 */
	public void delKey(String key){
		Jedis jedis=pool.getResource();
		try {
			jedis.del(key);
		} catch (Exception e) {
			pool.returnBrokenResource(jedis);
			e.printStackTrace();
		}finally{
			closeResource(jedis);
		}
	}
	
	/**
	 * 获取标识flagkey
	 * @param key
	 * @return
	 * @author Gerryle 2018年2月9日 下午3:22:02
	 */
	public String getFlagKey(String key){
		return key+"_flag";
	}
	
	/**
	 * 移除标志key
	 * @param key
	 * @author Gerryle 2018年2月9日 下午3:30:08
	 */
	public void delFlagKey(String key){
		delKey(getFlagKey(key));
	}
	
	/**
	 * 移除key以及标志key
	 * @param key
	 * @author Gerryle 2018年2月9日 下午3:29:38
	 */
	public void delKeyAndFlagKey(String key){
		Jedis jedis=pool.getResource();
		try {
			jedis.del(getFlagKey(key));
			jedis.del(key);
		} catch (Exception e) {
			pool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			closeResource(jedis);
		}
	}
	
	/**
	 * 设置指定可以的过期时间
	 * @param key
	 * @param expireSeconds
	 * @author Gerryle 2018年2月9日 下午3:42:10
	 */
	public void setKeyExpireTime(String key,int expireSeconds){
		if(expireSeconds<=0){
			return;
		}
		Jedis jedis=pool.getResource();
		try {
			jedis.expire(key, expireSeconds);
		} catch (Exception e) {
			pool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			closeResource(jedis);
		}
	}
	
	/**
	 * 单独设置指定key的过期时间
	 * @param key
	 * @param expireTime
	 * @author Gerryle 2018年2月9日 下午3:55:26
	 */
	public void setKeyExpireTime(String key,String expireTime){
		long toTime=DateUtil.getTime(expireTime,DateUtil.pattern1);
		int expireSeconds=(int)(toTime-System.currentTimeMillis())/1000;
		setKeyExpireTime(key,expireSeconds);
	}
	
	/**
	 * 获取key的失效时间
	 *   -1：key存在，但是没有设置失效时间
	 *   -2：key不存在
	 *   -3：redis报错
	 *   大于0，以秒数返回key的失效时间
	 * @param key
	 * @return
	 * @author Gerryle 2018年2月9日 下午3:59:20
	 */
	public long getKeyExpireTime(String key){
		Jedis jedis=pool.getResource();
		try {
			return jedis.ttl(key);
		} catch (Exception e) {
			pool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			closeResource(jedis);
		}
		return -3;
	}
	
	// ===============================key-list======================
	
	/**
	 * 获取自增id
	 * @param key
	 * @return
	 * @author Gerryle 2018年2月9日 下午4:04:43
	 */
   public int getAutoIncreaseId(String key){
	   Jedis jedis=pool.getResource();
	   try {
		return jedis.incr(key).intValue();
	} catch (Exception e) {
		pool.returnBrokenResource(jedis);
		e.printStackTrace();
		return -1;
	} finally {
		closeResource(jedis);
	}
   }
	
 /**  
  * 从缓存获取字符串
  * @param key
  * @return
  * @author Gerryle 2018年2月9日 下午4:08:09
  */
  public String getValue(String key){
	  Jedis jedis=pool.getResource();
	  try {
		String s=jedis.get(key);
		return s==null?"":s;
	} catch (Exception e) {
		pool.returnBrokenResource(jedis);
		e.printStackTrace();
		return "";
	} finally {
		closeResource(jedis);
	}
  }
  
  /**
   * 从缓存获取对象
   * @param key
   * @param t
   * @return
   * @author Gerryle 2018年2月9日 下午4:13:37
   */
  public <T> T  getValue(String key,Class<T> c){
	  String s=getValue(key);
	  if(StringUtil.isNullOrEmpty(s)){
		  return null;
	  }
	  return gson.fromJson(s,c);
  }
  
  
  /**
   * 从缓存获取对象，数据对象与缓存id一致，如果该id不存在，则该位置的缓存对象为null
   * @param keys
   * @param c
   * @return
   * @author Gerryle 2018年2月9日 下午4:24:41
   */
  public <T> List<T> getValues(String[] keys,Class<T> c){
	  Jedis jedis=pool.getResource();
	  List<T> list=new ArrayList<T>();
	  try {
		List<String> results=jedis.mget(keys);
		for(String s:results){
			if(StringUtil.isNullOrEmpty(s)&&s.equals("nil")){
				list.add(gson.fromJson(s, c));
			}else{
				list.add(null);
			}
		}
	} catch (Exception e) {
		pool.returnBrokenResource(jedis);
		e.printStackTrace();
	} finally {
		closeResource(jedis);
	}
	  return list;
  }
  
	private void closeResource(Jedis jedis) {
		try {
			pool.returnResource(jedis);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 保存字符串的过期时间
	 * @param key
	 * @param value
	 * @param expireSeconds
	 * @author Gerryle 2018年2月9日 下午6:16:47
	 */
	public void setValue(String key ,String value,int expireSeconds){
		setValue(key,value,expireSeconds,false);
	}
	
	/**
	 * 保存字符串的过期时间
	 * @param key
	 * @param value
	 * @param expireSeconds
	 * @param needSetFlag
	 * @author Gerryle 2018年2月9日 下午6:17:11
	 */
	public void setValue(String key,String value,int expireSeconds,boolean needSetFlag){
		Jedis jedis=pool.getResource();
		try {
		  onSetValue(jedis,key,value,expireSeconds,needSetFlag);
		} catch (Exception e) {
			pool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			closeResource(jedis);
		}
	}

	/**
	 * 设置过期时间
	 * @param jedis
	 * @param key
	 * @param value
	 * @param expireSeconds
	 * @param needSetFlag
	 * @author Gerryle 2018年2月9日 下午6:17:35
	 */
	private void onSetValue(Jedis jedis, String key, String value, int expireSeconds, boolean needSetFlag) {
		String flagKey=getFlagKey(key);
		jedis.set(key, value);
		if(needSetFlag){
			jedis.set(flagKey, gson.toJson(true));
		}
		if(expireSeconds>0){
			if(needSetFlag){
				jedis.expire(flagKey,expireSeconds);
			}
			jedis.expire(key, expireSeconds);
		}else{//不设置过期时间则需要设置原有的过期时间，获取key的过期时间，因为set后原有的key过期时间将被清空
			long expireTime=jedis.pttl(key);//获取剩余过期时间的毫秒数
			if(expireTime>0){
				int time=(int)Math.ceil((float)expireTime)/1000;
				if(needSetFlag){
					jedis.expire(flagKey,time);
				}
				jedis.expire(key, time);
			}
		}
		
	}
	
	/**
	 * 添加字符串
	 * @param key
	 * @param value
	 * @param expireTime
	 * @author Gerryle 2018年2月9日 下午6:25:40
	 */
	public void setValue(String key,String value,String expireTime){
		long toTime=DateUtil.getTime(expireTime, DateUtil.pattern1);
		int expireSeconds=(int)(toTime-System.currentTimeMillis())/1000;
		setValue(key,value,expireSeconds);
	}
	
	/**
	 * 保存单个对象
	 * @param key
	 * @param value
	 * @author Gerryle 2018年2月9日 下午6:28:13
	 */
	public <T> void setValue(String key,T value){
		setValue(key,value,0);
	}

	/**
	 * 保存单个对象（如果对象已经存在，则覆盖）
	 * @param key
	 * @param value
	 * @param expireSeconds
	 * @author Gerryle 2018年2月11日 上午11:35:32
	 */
	public <T> void setValue(String key, T value, int expireSeconds) {
        if(value!=null){
        	setValue(key,gson.toJson(value),expireSeconds);
        }    
	}
	
	/**
	 * 保存单个对象，包括flag(对象存在则覆盖)
	 * @param key
	 * @param value
	 * @param expireSeconds
	 * @param needSetFlag
	 * @author Gerryle 2018年2月11日 上午11:38:59
	 */
	public <T> void setValue(String key,T value,int expireSeconds,boolean needSetFlag){
		if(value!=null){
			setValue(key,gson.toJson(value),expireSeconds,needSetFlag);
		}
	}
 	
	/**
	 * 保存对象（如果对象存在则覆盖）
	 * @param key
	 * @param value
	 * @param expireTime
	 * @author Gerryle 2018年2月11日 上午11:42:34
	 */
	public <T> void setValue(String key,T value,String expireTime){
		if(value!=null){
			setValue(key,gson.toJson(value),expireTime);
		}
	}
	
	/**
	 * 保存对象（不存在key，返回成功true，存在key，返回失败false）
	 * @param key
	 * @param value
	 * @return
	 * @author Gerryle 2018年2月11日 上午11:44:02
	 */
	public <T> boolean setnx(String key,T value){
		return setnx(key,gson.toJson(value));
	}
	
	/**
	 * 保存字符串（key不存在返回true，key存在返回false）
	 * @param key
	 * @param value
	 * @return
	 * @author Gerryle 2018年2月11日 上午11:45:58
	 */
	public boolean setnx(String key ,String value){
		return setnx(key,value,0);
	}
	
	/**
	 * 保存字符串（key不存在返回true，key存在返回false）
	 * @param key
	 * @param value
	 * @param expireSeconds
	 * @return
	 * @author Gerryle 2018年2月11日 下午2:23:09
	 */
	public boolean setnx(String key,String value,int expireSeconds){
		Jedis jedis=pool.getResource();
		try {
			boolean setnxOK=jedis.setnx(key, value)==1;
			if(expireSeconds>0){
				if(setnxOK){
					jedis.expire(key, expireSeconds);
				}else if(!setnxOK && jedis.pttl(key)<0){
					jedis.expire(key,expireSeconds);
				}
			}
			if(setnxOK){
				return true;
			}
			return false;
		}  catch (Exception e) {
			pool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			closeResource(jedis);
		}
		return false;
	}
	
	// ===============================key-list======================
	
	/**
	 * 添加一个list列表到原有列表尾部
	 * @param key
	 * @param list
	 * @author Gerryle 2018年2月11日 下午2:53:07
	 */
	public <T> void addList(String key,List<T> list){
		if(list==null||list.size()==0){
			return;
		}
		Jedis jedis=pool.getResource();
		try {
			String[] ss=new String[list.size()];
			for(int i=0;i<list.size();i++){
				ss[i]=gson.toJson(list.get(i));
			}
			jedis.rpush(key, ss);
		} catch (Exception e) {
			pool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			closeResource(jedis);
		}
	}
	
	/**
	 * 添加一个value到原有列表到尾部
	 * @param key
	 * @param value
	 * @author Gerryle 2018年2月11日 下午3:04:49
	 */
	public <T> void addList(String key,T value){
		Jedis jedis=pool.getResource();
		try {
			String[] ss=new String[1];
			ss[0]=gson.toJson(value);
			jedis.rpush(key, ss);
		} catch (Exception e) {
			pool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			closeResource(jedis);
		}
	}
	
	/**
	 * 添加一个list列表到原有列表头部
	 * @param key
	 * @param list
	 * @author Gerryle 2018年2月11日 下午3:11:59
	 */
	public <T> void addListToHead(String key,List<T> list){
		if(list==null||list.isEmpty()){
			System.out.println("list is null");
			return;
		}
		Jedis jedis=pool.getResource();
		try {
			String[] ss=new String[list.size()];
			for(int i=0;i<list.size();i++){
				ss[i]=gson.toJson(list.get(i));
			}
			jedis.lpush(key, ss);
		} catch (Exception e) {
			pool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			closeResource(jedis);
		}
	}
	
	/**
	 * 添加一个value到原有列表头部
	 * @param key
	 * @param value
	 * @author Gerryle 2018年2月11日 下午3:14:34
	 */
	public <T> void addListToHead(String key,T value){
		Jedis jedis=pool.getResource();
		try {
			String[] ss=new String[1];
			ss[0]=gson.toJson(value);
			jedis.lpush(key, gson.toJson(value));
		} catch (Exception e) {
			pool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			closeResource(jedis);
		}
	}
	
	/**
	 * 这是list中指定索引的值
	 * @param key
	 * @param index
	 * @param value
	 * @author Gerryle 2018年2月11日 下午3:16:31
	 */
	public <T> void setListElement(String key,int index,T value){
		Jedis jedis=pool.getResource();
		try {
			jedis.lset(key, index, gson.toJson(value));
		} catch (Exception e) {
			pool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			closeResource(jedis);
		}
	}
	
	/**
	 * 返回某个范围内的集合，无结果则返回空list
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @author Gerryle 2018年2月11日 下午3:18:32
	 */
	public List<String> getListRange(String key,int start,int end){
		Jedis jedis=pool.getResource();
		try {
			return jedis.lrange(key, start, end);
		} catch (Exception e) {
			pool.returnBrokenResource(jedis);
			e.printStackTrace();
			return null;
		} finally {
			closeResource(jedis);
		}
	}
	
	/**
	 * 返回某个范围内的集合，无结果集则返回空list
	 * @param key
	 * @param start 起索引（从0开始）
	 * @param end
	 * @param c
	 * @return
	 * @author Gerryle 2018年2月11日 下午3:23:34
	 */
	public <T> List<T> getListRange(String key,int start,int end,Class<T> c){
		List<String> slist=getListRange(key,start,end);
		List<T> list=new ArrayList<T>();
		for(String s:slist){
			list.add(gson.fromJson(s, c));
		}
		return list;
	}
	
	/**
	 * 分页获取list
	 * @param key
	 * @param pageNow
	 * @param pageSize
	 * @param c
	 * @return
	 * @author Gerryle 2018年2月11日 下午3:27:22
	 */
	public <T> List<T> getListPage(String key,int pageNow,int pageSize,Class<T> c){
		int startIndex=(pageNow-1)*pageSize;
		int endIndex=pageNow*pageSize-1;
		return getListRange(key,startIndex,endIndex,c);
	}
	
	/**
	 * 获取指定key下的所有列表记录
	 * @param key
	 * @param c
	 * @return
	 * @author Gerryle 2018年2月11日 下午3:29:20
	 */
	public <T> List<T> getAllList(String key,Class<T> c){
		return getListRange(key,0,-1,c);
	}
	
	/**
	 * 获取列表第一个元素
	 * @param key
	 * @param c
	 * @return
	 * @author Gerryle 2018年2月11日 下午3:33:17
	 */
	public <T> T getListFirstElement(String key,Class<T> c){
		Jedis jedis=pool.getResource();
		try {
			return gson.fromJson(jedis.lindex(key,0),c);
		} catch (Exception e) {
			pool.returnBrokenResource(jedis);
			e.printStackTrace();
			return null;
		}finally{
			closeResource(jedis);
		}
	}
	
	/**
	 * 返回并删除list中的首元素
	 * @param key
	 * @param c
	 * @return
	 * @author Gerryle 2018年2月11日 下午3:38:07
	 */
	public <T> T getListPop(String key,Class<T> c){
		Jedis jedis=pool.getResource();
		try {
			return gson.fromJson(jedis.lpop(key), c);
		} catch (Exception e) {
			pool.returnBrokenResource(jedis);
			e.printStackTrace();
			return null;
		}finally{
			closeResource(jedis);
		}
	}
	
	/**
	 * 返回并删除list中的首元素
	 * @param key
	 * @return
	 * @author Gerryle 2018年2月11日 下午3:40:38
	 */
	public <T> String getListPop(String key){
		Jedis jedis=pool.getResource();
		try {
			return jedis.lpop(key);
		} catch (Exception e) {
			pool.returnBrokenResource(jedis);
			e.printStackTrace();
			return null;
		}finally{
			closeResource(jedis);
		}
	}
	
	/**
	 * 获取最后一个元素
	 * @param key
	 * @param c
	 * @return
	 * @author Gerryle 2018年2月11日 下午3:45:01
	 */
	public <T> T getListLastElement(String key,Class<T> c){
		Jedis jedis=pool.getResource();
		try {
			return gson.fromJson(jedis.lindex(key,-1), c);
		} catch (Exception e) {
			pool.returnBrokenResource(jedis);
			e.printStackTrace();
			return null;
		}finally{
			closeResource(jedis);
		}
	}
	
	/**
	 * 返回list集合数
	 * @param key
	 * @return
	 * @author Gerryle 2018年2月11日 下午3:47:15
	 */
	public int getListSize(String key){
		Jedis jedis=pool.getResource();
		try {
			return jedis.llen(key).intValue();
		} catch (Exception e) {
			pool.returnBrokenResource(jedis);
			e.printStackTrace();
			return 0;
		}finally{
			closeResource(jedis);
		}
	}
	
	/**
	 * 删除list的指定对象
	 * @param key
	 * @param values
	 * @return
	 * @author Gerryle 2018年2月11日 下午4:04:01
	 */
	public <T> long removeValueFromList(String key,T... values){
		long removed=0;
		Jedis jedis=pool.getResource();
		try {
			for(T v:values){
				removed+=jedis.lrem(key, 0, gson.toJson(v));
			}
		} catch (Exception e) {
			pool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			closeResource(jedis);
		}
		return removed;
	}
	
	public void removeList(String key){
		delKey(key);
	}
	
	// ===============================set start======================
	@SuppressWarnings("unchecked")
	public <T> int sAdd(final String key,final T... values){
		if(StringUtil.isNullOrBlank(key)||values==null||values.length<=0){
			return -1;
		}
		return execute(new RedisCall<Integer>() {
			@Override
			public Integer execute(Jedis jedis) {
				String[] val=new String[values.length];
				for(int i=0;i<values.length;i++){
					val[i]=gson.toJson(values[i]);
				}
				return jedis.sadd(key, val).intValue();
			}
		});
	}
	
	/**
	 * set中元素的数量
	 * @param key
	 * @return
	 * @author Gerryle 2018年2月11日 下午4:31:32
	 */
	public Integer sSize(final String key){
		return execute(new RedisCall<Integer>(){
			@Override
			public Integer execute(Jedis jedis){
				return jedis.scard(key).intValue();
			}
		});
	}
	
	
	/**
	 * set元素中的集合
	 * @param key
	 * @param c
	 * @return
	 * @author Gerryle 2018年2月11日 下午5:12:23
	 */
	public <T> Set<T> sSet(final String key,Class<T> c){
		Set<String> set=execute(new RedisCall<Set<String>>() {
			@Override
			public Set<String> execute(Jedis jedis) {
				return jedis.smembers(key);
			}});
		if(CollectionUtils.isEmpty(set)){
			return Collections.emptySet();
		}
		Set<T> resultSet=new HashSet<T>();
		for(String s:set){
			resultSet.add(gson.fromJson(s, c));
		}
		return resultSet;
	}
	
	/**
	 * 删除set中的元素
	 * @param key
	 * @param members
	 * @return
	 * @author Gerryle 2018年2月11日 下午5:18:12
	 */
	@SuppressWarnings("unchecked")
	public <T> int sRemove(final String key,T... members){
		if(StringUtil.isNullOrBlank(key)||members==null||members.length<=0){
			return -1;
		}
		final String[] val=new String[members.length];
		for(int i=0;i<members.length;i++){
			val[i]=gson.toJson(members[i]);
		}
		return execute(new RedisCall<Integer>() {
			@Override
			public Integer execute(Jedis jedis) {
				return jedis.srem(key, val).intValue();
			}
			
		});
	}
	
	/**
	 * 判断某个元素是否在set集合中
	 * @param key
	 * @param member
	 * @return
	 * @author Gerryle 2018年2月11日 下午5:20:30
	 */
	public <T> boolean sExists(final String key,final T member){
		if(StringUtil.isNullOrBlank(key)){
			return false;
		}
		return execute(new RedisCall<Boolean>() {

			@Override
			public Boolean execute(Jedis jedis) {
				return jedis.sismember(key, gson.toJson(member));
			}
		});
	}
	
	//=================================map start=====================================
	/**
	 * 保存map
	 * @param hashKey
	 * @param map
	 * @author Gerryle 2018年2月11日 下午5:25:24
	 */
	public <T> void addToHashMap(String hashKey,Map<String, T> map){
		if(map==null||map.isEmpty()){
		    System.out.println("map is null");
		    return;
		}
		Jedis jedis=pool.getResource();
		try {
			for(String key:map.keySet()){
				String value=gson.toJson(map.get(key));
				jedis.hset(hashKey, key, value);
			}
		} catch (Exception e) {
			pool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			closeResource(jedis);
		}
	}
	
	/**
	 * 添加map
	 * @param hashKey
	 * @param key
	 * @param value
	 * @author Gerryle 2018年2月11日 下午5:29:42
	 */
	public <T> void addToHashMap(String hashKey,String key,T value){
		Jedis jedis=pool.getResource();
		try {
			jedis.hset(hashKey, key, gson.toJson(value));
		} catch (Exception e) {
			pool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			closeResource(jedis);
		}
	}
	
	/**
	 * 获取hashmap中返回某个key的值
	 * @param hashKey
	 * @param key
	 * @param c
	 * @return
	 * @author Gerryle 2018年2月11日 下午5:32:12
	 */
	public <T> T getValueFromHashMap(String hashKey,String key,Class<T> c){
		Jedis jedis=pool.getResource();
		try {
			String s=jedis.hget(hashKey, key);
			if(StringUtil.isNullOrEmpty(s)){
				return null;
			}
			return gson.fromJson(s, c);
		} catch (Exception e) {
			pool.returnBrokenResource(jedis);
			e.printStackTrace();
			return null;
		} finally {
			closeResource(jedis);
		}
	}
	
	/**
	 * 返回map中某个值
	 * @param hashKey
	 * @param key
	 * @param typeOfT
	 * @return
	 * @author Gerryle 2018年2月11日 下午5:35:16
	 */
	public <T> T getValueFromHashMap(String hashKey,String key,Type typeOfT){
		Jedis jedis=pool.getResource();
		try {
			String s=jedis.hget(hashKey, key);
			
			if (StringUtil.isNullOrEmpty(s)) {
				return null;
			}
			return gson.fromJson(s, typeOfT);
		} catch (Exception e) {
			pool.returnBrokenResource(jedis);
			e.printStackTrace();
			return null;
		} finally {
			closeResource(jedis);
		}
	}
	
	/**
	 * 返回map集合对象
	 * @param hashKey
	 * @param c
	 * @return
	 * @author Gerryle 2018年2月11日 下午5:40:35
	 */
	public <T> Map<String, T> getAllFromHashMap(String hashKey,Class<T> c){
		Jedis jedis=pool.getResource();
		try {
			Map<String, String> map=jedis.hgetAll(hashKey);
			Map<String, T> tMap=new HashMap<String,T>(map.size());
			for(String key:map.keySet()){
				T value=gson.fromJson(map.get(key), c);
				tMap.put(key, value);
			}
			return tMap;
		}catch (Exception e) {
			pool.returnBrokenResource(jedis);
			e.printStackTrace();
			return null;
		} finally {
			closeResource(jedis);
		}
	}
	
	
	/**
	 * 返回map的size
	 * @param hashKey
	 * @return
	 * @author Gerryle 2018年2月11日 下午5:42:28
	 */
	public int getSizeFromHashMap(String hashKey){
		Jedis jedis=pool.getResource();
		try {
			return jedis.hlen(hashKey).intValue();
		} catch (Exception e) {
			pool.returnBrokenResource(jedis);
			e.printStackTrace();
			return 0;
		} finally {
			closeResource(jedis);
		}
	}
	
	public String type(String key){
		Jedis jedis=pool.getResource();
		try {
			return jedis.type(key);
		} catch (Exception e) {
			pool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			closeResource(jedis);
		}
		return null;
	}
	
	/**
	 * 删除key
	 * @param hashKey
	 * @param keys
	 * @return
	 * @author Gerryle 2018年2月11日 下午5:45:44
	 */
	public long removeFromHashMap(String hashKey,String... keys){
		long removed=0;
		Jedis jedis=pool.getResource();
		try {
			removed=jedis.hdel(hashKey, keys);
		} catch (Exception e) {
			pool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			closeResource(jedis);
		}
		return removed;
	}
	
	/**
	 * 获取map中所有的key
	 * @param hashKey
	 * @return
	 * @author Gerryle 2018年2月11日 下午5:47:21
	 */
	public Set<String> getKeysFromHashMap(String hashKey){
		Jedis jedis=pool.getResource();
		try {
		   return jedis.hkeys(hashKey);
		} catch (Exception e) {
			pool.returnBrokenResource(jedis);
			e.printStackTrace();
			return null;
		} finally {
			closeResource(jedis);
		}
	}
	
	/**
	 * 删除map中所有的元素
	 * @param hashKey
	 * @author Gerryle 2018年2月11日 下午5:48:19
	 */
	public void removeHashMap(String hashKey){
		delKey(hashKey);
	}
	
	/**
	 * 判断map中是否存在指定的key
	 * @param hashKey
	 * @param key
	 * @return
	 * @author Gerryle 2018年2月11日 下午5:50:11
	 */
   public boolean hasKeyHashMap(String hashKey,String key){
	   Jedis jedis=pool.getResource();
	   try {
		return jedis.hexists(hashKey, key);
	} catch (Exception e) {
		pool.returnBrokenResource(jedis);
		e.printStackTrace();
		return false;
	} finally {
		closeResource(jedis);
	}
   }
	
   /**
    * redis是否可能已经死亡
    * @return
    * @author Gerryle 2018年2月11日 下午5:53:34
    */
   public boolean mayBeDead(){
	   String key="RedisMayBeDead_"+ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
	   try {
		setValue(key,key,10);
		String val=getValue(key);
		return !key.equals(val);
	} catch (Exception e) {
		e.printStackTrace();
		return true;
	}
   }
	
	/**
	 * Redis 执行的模板
	 * @param call
	 * @return
	 * @author Gerryle 2018年2月11日 下午4:26:31
	 */
	private <T> T execute(RedisCall<T> call){
		Jedis jedis=pool.getResource();
		try {
		    return call.execute(jedis);
		} catch (Exception e) {
			pool.returnBrokenResource(jedis);
			e.printStackTrace();
		}finally{
			closeResource(jedis);
		}
		return null;
	}
}
