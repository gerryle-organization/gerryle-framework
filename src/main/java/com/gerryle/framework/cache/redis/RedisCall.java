package com.gerryle.framework.cache.redis;

import redis.clients.jedis.Jedis;

public interface RedisCall<T> {

	T execute(Jedis jedis);
}
