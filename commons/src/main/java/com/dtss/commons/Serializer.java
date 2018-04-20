package com.dtss.commons;

import java.util.List;

/**
 * 序列化接口
 * <p>用于自定义一些序列化实现</p>
 *
 * @author luyun
 * @since 2016.03.23
 */
public interface Serializer {

    /**
     * 序列化，将对象转换成二进制数组
     *
     * @param object 待序列化的对象
     */
    byte[] serialization(Object object);

    /**
     * 反序列化,将一个二进制数组反序列化为指定的类
     *
     * @param byteArray 需要序列化的数据数组
     * @param c         class
     * @param <T>       序列化的类型
     */
    <T> T deserialization(byte[] byteArray, Class<T> c);

    /**
     * 反序列化为一个List
     */
    <E> List<E> deserializationList(byte[] byteArray, Class<E> elementC);

}
