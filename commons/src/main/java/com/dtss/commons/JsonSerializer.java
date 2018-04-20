package com.dtss.commons;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.util.SafeEncoder;

import java.util.List;

/**
 * Json 序列化
 */
public class JsonSerializer implements Serializer {

	private Logger logger = LoggerFactory.getLogger(JsonSerializer.class);

	public byte[] serialization(Object object) {

		if (object == null){
			return null;
		}

		try {
			return SafeEncoder.encode(JSONObject.toJSONString(object));
		} catch (Exception e) {
			logger.error("JsonSerializer.serialization > exception", e);
			return null;
		}
	}

	public <T> T deserialization(byte[] byteArray, Class<T> c) {

		if (ArrayUtils.isEmpty(byteArray)){
			return null;
		}

		try {
			return JSON.parseObject(SafeEncoder.encode(byteArray), c);
		} catch (Exception e) {
			logger.error("JsonSerializer.deserialization > exception", e);
			return null;
		}
	}

	public <E> List<E> deserializationList(byte[] byteArray, Class<E> elementC) {

		if (ArrayUtils.isEmpty(byteArray)){
			return null;
		}

		try {
			return JSON.parseArray(SafeEncoder.encode(byteArray), elementC);
		} catch (Exception e) {
			logger.error("JsonSerializer.deserializationList > exception", e);
			return null;
		}
	}

}