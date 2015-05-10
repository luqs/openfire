package com.skyseas.openfireplugins.chatlogs;

import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * function: 聊天记录Redis操作类
 */
public class JedisManager {
	private static final Logger log = LoggerFactory.getLogger(JedisManager.class);
	private static JedisManager instance ;
	private JedisPool pool;
	
	private JedisManager(Properties properties) {
		if (properties == null) {
			throw new IllegalArgumentException(
					"cannot find the SignVerProp.properties");
		}
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(Integer.valueOf(properties.getProperty("redis.pool.maxActive")));
		config.setMaxIdle(Integer.valueOf(properties.getProperty("redis.pool.maxIdle")));
		pool = new JedisPool(config, properties.getProperty("redis.host"),
				Integer.valueOf(properties.getProperty("redis.port")));
	}
	
	public static synchronized JedisManager getInstance(Properties properties) {
		if (instance == null) { 
			instance = new JedisManager(properties);
		}
		return instance;  
    }  

	public Jedis getJedis() {  
        Jedis jedis  = null;  
        try{   
            jedis = pool.getResource();  
            //log.info("get redis master1!");  
        } catch (Exception e) {  
            log.error("get redis master1 failed!", e);  
             // 销毁对象    
            pool.returnBrokenResource(jedis);    
        }  
        
        return jedis;  
    }  
	
	public void closeJedis(Jedis jedis) {  
        if(jedis != null) {  
            pool.returnResource(jedis);  
        }  
    }  
}
