package com.nio.util;


/**
 * Created by lenovo on 2017/1/17.
 *
 * because the Serializer Instance Is not status. So it's ThreadSafe And  we Can use it as singleton.
 */

public interface Serializer {

    /** max buffer size for a {@link Serializer}  to cache */
    int MAX_CACHED_BUF_SIZE=64 *1024;

    /**
     *  default buffer size for  a {@link Serializer}
     */
     int DEFAULT_BUF_SIZE=512;


     <T> T decode(byte[] bytes, Class<T> clazz);

     <T> T decode(byte[] bytes, int offset, int length, Class<T> clazz);

     <T> byte[] encode(T obj);

}