package com.dtss.commons;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.util.SafeEncoder;

import javax.annotation.PreDestroy;
import java.util.*;
import java.util.Map.Entry;

/**
 * Redis 组件<br>
 * ～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～
 * ｜｜｜｜｜｜｜｜｜｜｜｜｜｜｜｜｜｜｜ 配置文件｜｜｜｜｜｜｜｜｜｜｜｜｜｜｜｜｜｜
 * ～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～
 * redis.ip=192.168.XXX.XXX
 * redis.port=6379
 * redis.timeout=500
 * redis.pool.maxTotal=1024
 * redis.pool.maxIdle=200
 * redis.pool.maxWaitMillis=1000
 * redis.pool.testOnBorrow=true
 * redis.pool.testOnReturn=true
 * ～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～
 * ｜｜｜｜｜｜｜｜｜｜｜｜｜Spring 配置文件中添加redis配置｜｜｜｜｜｜｜｜｜｜｜｜｜
 * ～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～
 * <bean id="jedisPoolConfig" class="redis.clients.jedis.JedisPoolConfig">
 * <property name="maxTotal" value="${redis.pool.maxTotal}" />
 * <property name="maxIdle" value="${redis.pool.maxIdle}" />
 * <property name="maxWaitMillis" value="${redis.pool.maxWaitMillis}" />
 * <property name="testOnBorrow" value="${redis.pool.testOnBorrow}" />
 * <property name="testOnReturn" value="${redis.pool.testOnReturn}" />
 * </bean>
 * <bean id="pool" class="redis.clients.jedis.ShardedJedisPool">
 * <constructor-arg index="0" ref="jedisPoolConfig" />
 * <constructor-arg index="1">
 * <list>
 * <bean class="redis.clients.jedis.JedisShardInfo">
 * <constructor-arg index="0" value="${redis.ip}" />
 * <constructor-arg index="1" value="${redis.port}" type="int" />
 * <constructor-arg index="2" value="${redis.timeout}" />
 * </bean>
 * </list>
 * </constructor-arg>
 * </bean>
 * ～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～
 */
public class RedisComponent implements InitializingBean {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final Long LOCK_EXPIRED_TIME = Millisecond.FIVE_SECONDS;// 分布式锁的失效时间

    @Autowired
    private ShardedJedisPool pool;

    private Serializer serializer;

    /*--***************** 常用方法 *******************/

    /**
     * 获取一个锁 必须保证分布式环境的多个主机的时钟是一致的
     *
     * @param lockKey 锁Key
     * @param expired 锁的失效时间（毫秒）
     */
    public boolean acquireLock(String lockKey, long expired) {

        ShardedJedis jedis = null;

        try {

            jedis = pool.getResource();
            String value = String.valueOf(System.currentTimeMillis() + expired + 1);
            int tryTimes = 0;

            while (tryTimes++ < 3) {

                /*
                 *  1. 尝试锁
                 *  setnx : set if not exist
                 */
                if (jedis.setnx(lockKey, value).equals(1L)) {
                    return true;
                }

                /*
                 * 2. 已经被别的线程锁住，判断是否失效
                 */
                String oldValue = jedis.get(lockKey);
                if (StringUtils.isBlank(oldValue)) {
                    /*
                     * 2.1 value存的是超时时间，如果为空有2种情况
                     *      1. 异常数据，没有value 或者 value为空字符
                     *      2. 锁恰好被别的线程释放了
                     * 此时需要尝试重新尝试，为了避免出现情况1时导致死循环，只重试3次
                     */
                    continue;
                }

                Long oldValueL = Long.valueOf(oldValue);
                if (oldValueL < System.currentTimeMillis()) {
                    /*
                     * 已超时，重新尝试锁
                     *
                     * Redis:getSet 操作步骤：
                     *      1.获取 Key 对应的 Value 作为返回值，不存在时返回null
                     *      2.设置 Key 对应的 Value 为传入的值
                     * 这里如果返回的 getValue != oldValue 表示已经被其它线程重新修改了
                     */
                    String getValue = jedis.getSet(lockKey, value);
                    return oldValue.equals(getValue);
                } else {
                    // 未超时，则直接返回失败
                    return false;
                }
            }

            return false;

        } catch (Throwable e) {
            logger.error("acquireLock error", e);
            return false;

        } finally {
            returnResource(jedis);
        }
    }

    /**
     * 释放锁
     *
     * @param lockKey key
     */
    public void releaseLock(String lockKey) {
        ShardedJedis jedis = null;
        try {
            jedis = pool.getResource();
            long current = System.currentTimeMillis();
            // 避免删除非自己获取到的锁
            String value = jedis.get(lockKey);
            if (StringUtils.isNotBlank(value) && current < Long.valueOf(value)) {
                jedis.del(lockKey);
            }
        } catch (Throwable e) {
            logger.error("releaseLock error", e);
        } finally {
            returnResource(jedis);
        }
    }

    /**
     * 获取 key-value 的 value
     */
    public <T> T get(final String key, final Class<T> c) {
        return this.execute(new JedisAction<T>() {
            public T action(ShardedJedis jedis) {
                byte[] bs = jedis.get(SafeEncoder.encode(key));
                return deserialization(bs, c);
            }
        });
    }

    /**
     * 获取 key-value 的 value. <br>
     * 如果 value 是一个 list, 请使用此方法.
     */
    public <T> List<T> getList(final String key, final Class<T> c) {
        return this.executeForList(new JedisActionForList<T>() {
            public List<T> action(ShardedJedis jedis) {
                byte[] bs = jedis.get(SafeEncoder.encode(key));
                return deserializationList(bs, c);
            }
        });
    }

    /**
     * 缓存 key-value
     */
    public void set(final String key, final Object value) {
        this.execute(new JedisActionNoResult() {
            public void action(ShardedJedis jedis) {
                jedis.set(SafeEncoder.encode(key), serialization(value));
            }
        });
    }

    /**
     * 缓存 key-value , seconds 过期时间,单位为秒.
     */
    public void set(final String key, final Object value, final int seconds) {
        this.execute(new JedisActionNoResult() {
            public void action(ShardedJedis jedis) {
                jedis.setex(SafeEncoder.encode(key), seconds, serialization(value));
            }
        });
    }

    /**
     * 获取 key mapKey mapValue 中的 mapValue 列表.
     */
    public <T> List<T> hvals(final String key, final Class<T> c) {
        return this.executeForList(new JedisActionForList<T>() {
            public List<T> action(ShardedJedis jedis) {
                Collection<byte[]> value = jedis.hvals(SafeEncoder.encode(key));
                List<T> list = new ArrayList<T>(value.size());
                for (byte[] bs : value) {
                    list.add(deserialization(bs, c));
                }
                return list;
            }
        });
    }

    /**
     * 获取 key mapKey mapValue 中指定的 mapValue.
     */
    public <T> T hget(final String key, final Object mapKey, final Class<T> c) {
        return this.execute(new JedisAction<T>() {
            public T action(ShardedJedis jedis) {
                byte[] bs = jedis.hget(SafeEncoder.encode(key), serialization(mapKey));
                return deserialization(bs, c);
            }
        });
    }

    /**
     * 获取 key mapKey mapValue 中指定的 mapValue.<br>
     * 如果 mapValue 是一个 list, 请使用此方法.
     */
    public <T> List<T> hgetList(final String key, final Object mapKey, final Class<T> c) {
        return this.executeForList(new JedisActionForList<T>() {
            public List<T> action(ShardedJedis jedis) {
                byte[] value = jedis.hget(SafeEncoder.encode(key), serialization(mapKey));
                return deserializationList(value, c);
            }
        });
    }

    /**
     * 缓存 key mapKey mapValue.
     */
    public void hset(final String key, final Object mapKey, final Object mapValue) {
        this.execute(new JedisActionNoResult() {
            public void action(ShardedJedis jedis) {
                jedis.hset(SafeEncoder.encode(key), serialization(mapKey), serialization(mapValue));
            }
        });
    }

    public void hset(final String key, final Object mapKey, final Object mapValue, final int second) {
        this.execute(new JedisActionNoResult() {
            public void action(ShardedJedis jedis) {
                jedis.hset(SafeEncoder.encode(key), serialization(mapKey), serialization(mapValue));
                jedis.expire(key, second);
            }
        });
    }

    /**
     * 删除集合中对应的key/value
     */
    public void hdel(final String key, final Object mapKey) {
        this.execute(new JedisActionNoResult() {
            public void action(ShardedJedis jedis) {
                jedis.hdel(SafeEncoder.encode(key), serialization(mapKey));
            }
        });
    }

    /**
     * 缓存 key map<mapKey,mapValue>.
     */
    public void hmset(final String key, final Map<Object, Object> map) {
        this.execute(new JedisActionNoResult() {
            public void action(ShardedJedis jedis) {
                if (MapUtils.isNotEmpty(map)) {
                    Map<byte[], byte[]> m = new HashMap<byte[], byte[]>(map.size());

                    for (Entry<Object, Object> next : map.entrySet()) {
                        m.put(serialization(next.getKey()), serialization(next.getValue()));
                    }
                    jedis.hmset(SafeEncoder.encode(key), m);
                }
            }
        });
    }

    /**
     * 缓存 key map<mapKey,mapValue>，expireSeconds秒时间失效
     */
    public void hmset(final String key, final Map<Object, Object> map, final int expireSeconds) {
        this.execute(new JedisActionNoResult() {
            public void action(ShardedJedis jedis) {
                if (MapUtils.isNotEmpty(map)) {
                    Map<byte[], byte[]> m = new HashMap<byte[], byte[]>(map.size());

                    for (Entry<Object, Object> next : map.entrySet()) {
                        m.put(serialization(next.getKey()), serialization(next.getValue()));
                    }
                    jedis.hmset(SafeEncoder.encode(key), m);
                    jedis.expire(key, expireSeconds);
                }
            }
        });
    }

    /**
     * 删除一个 Key.
     */
    public Long del(final String key) {
        return this.execute(new JedisAction<Long>() {
            public Long action(ShardedJedis jedis) {
                return jedis.del(key);
            }
        });
    }

    /**
     * redis zadd command.
     */
    public Long zadd(final String key, final double score, final Object member) {
        return this.execute(new JedisAction<Long>() {
            public Long action(ShardedJedis jedis) {
                return jedis.zadd(SafeEncoder.encode(key), score, serialization(member));
            }
        });
    }

    /**
     * redis zrange command.
     */
    public <T> List<T> zrange(final String key, final long start, final long end, final Class<T> clazz) {
        return this.executeForList(new JedisActionForList<T>() {
            public List<T> action(ShardedJedis jedis) {
                Collection<byte[]> value = jedis.zrange(SafeEncoder.encode(key), start, end);
                List<T> list = new ArrayList<T>(value.size());
                for (byte[] b : value) {
                    list.add(deserialization(b, clazz));
                }
                return list;
            }
        });
    }

    /**
     * redis zrangeByScore command.
     */
    public <T> List<T> zrangeByScore(final String key, final double min, final double max, final Class<T> clazz) {
        return this.executeForList(new JedisActionForList<T>() {
            public List<T> action(ShardedJedis jedis) {
                Collection<byte[]> value = jedis.zrangeByScore(SafeEncoder.encode(key), min, max);
                List<T> list = new ArrayList<T>(value.size());
                for (byte[] b : value) {
                    list.add(deserialization(b, clazz));
                }
                return list;
            }
        });
    }

    /**
     * redis zremrangeByScore command.
     */
    public Long zremrangeByScore(final String key, final double start, final double end) {
        return this.execute(new JedisAction<Long>() {
            public Long action(ShardedJedis jedis) {
                return jedis.zremrangeByScore(key, start, end);
            }
        });
    }

    public Long zremrange(final String key, final String... members) {
        return this.execute(new JedisAction<Long>() {
            public Long action(ShardedJedis jedis) {
                return jedis.zrem(key, members);
            }
        });
    }

    /**
     * redis incr command.
     */
    public Long incr(final String key) {
        return this.execute(new JedisAction<Long>() {
            public Long action(ShardedJedis jedis) {
                return jedis.incr(key);
            }
        });
    }

    /**
     * redis incrby command.
     */
    public Long incrBy(final String key, final long integer) {
        return this.execute(new JedisAction<Long>() {
            public Long action(ShardedJedis jedis) {
                return jedis.incrBy(key, integer);
            }
        });
    }

    /**
     * redis decr command.
     */
    public Long decr(final String key) {
        return this.execute(new JedisAction<Long>() {
            public Long action(ShardedJedis jedis) {
                return jedis.decr(key);
            }
        });
    }

    /**
     * redis decrby command.
     */
    public Long decrBy(final String key, final long integer) {
        return this.execute(new JedisAction<Long>() {
            public Long action(ShardedJedis jedis) {
                return jedis.decrBy(key, integer);
            }
        });
    }

    /**
     * redis expire command.
     */
    public Long expire(final String key, final int seconds) {
        return this.execute(new JedisAction<Long>() {
            public Long action(ShardedJedis jedis) {
                return jedis.expire(key, seconds);
            }
        });
    }

    public Long sadd(final String key, final Object value) {
        return this.execute(new JedisAction<Long>() {
            public Long action(ShardedJedis jedis) {
                return jedis.sadd(SafeEncoder.encode(key), serialization(value));
            }
        });
    }

    public Long srem(final String key, final Object value) {
        return this.execute(new JedisAction<Long>() {
            public Long action(ShardedJedis jedis) {
                return jedis.srem(SafeEncoder.encode(key), serialization(value));
            }
        });
    }

    public Set<String> smembers(final String key) {
        return this.execute(new JedisAction<Set<String>>() {
            public Set<String> action(ShardedJedis jedis) {
                return jedis.smembers(key);
            }
        });
    }

    // internal method
    // -----------------------------------------------------------------------

    /**
     * 执行有返回结果的action。
     */
    public <T> T execute(JedisAction<T> jedisAction) {
        ShardedJedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedisAction.action(jedis);
        } finally {
            returnResource(jedis);
        }
    }

    /**
     * 执行有返回结果,并且返回结果是List的action。
     */
    public <T> List<T> executeForList(JedisActionForList<T> jedisAction) {
        ShardedJedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedisAction.action(jedis);
        } finally {
            returnResource(jedis);
        }
    }

    /**
     * 执行无返回结果的action。
     */
    public void execute(JedisActionNoResult jedisAction) {
        ShardedJedis jedis = null;
        try {
            jedis = pool.getResource();
            jedisAction.action(jedis);
        } finally {
            returnResource(jedis);
        }
    }

    /**
     * 有返回结果的回调接口定义。
     */
    public interface JedisAction<T> {
        T action(ShardedJedis jedis);
    }

    /**
     * 有返回结果的回调接口定义。
     */
    public interface JedisActionForList<T> {
        List<T> action(ShardedJedis jedis);
    }

    /**
     * 无返回结果的回调接口定义。
     */
    public interface JedisActionNoResult {
        void action(ShardedJedis jedis);
    }

    // private method
    // -----------------------------------------------------------------------
    private byte[] serialization(Object object) {
        return serializer.serialization(object);
    }

    private <T> T deserialization(byte[] byteArray, Class<T> c) {
        return serializer.deserialization(byteArray, c);
    }

    private <E> List<E> deserializationList(byte[] byteArray, Class<E> elementC) {
        return serializer.deserializationList(byteArray, elementC);
    }

    private void returnResource(ShardedJedis jedis) {
        // 返还到连接池
        if (jedis != null) {
            try {
                pool.returnResource(jedis);
            } catch (Throwable e) {
                returnBrokenResource(jedis);
            }
        }
    }

    private void returnBrokenResource(ShardedJedis jedis) {
        if (jedis != null) {
            try {
                pool.returnBrokenResource(jedis);
            } catch (Throwable e) {
                logger.error("", e);
            }
        }
    }

    @PreDestroy
    public void destroy() {
        try {
            pool.destroy();
        } catch (Throwable e) {
            logger.error("", e);
        }
    }

    public ShardedJedisPool getPool() {
        return pool;
    }

    public void setPool(ShardedJedisPool pool) {
        this.pool = pool;
    }

    public Serializer getSerializer() {
        return serializer;
    }

    public void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }

    public void afterPropertiesSet() throws Exception {
        if (this.serializer == null) {
            // 为了向下兼容默认,如果没有提供序列化器,默认使用,json序列化
            serializer = new JsonSerializer();
        }
        logger.info("RedisComponent [" + this.toString() + "] is done! serializer:" + serializer.toString());
    }

}