package com.nio.util;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *  StuffProto
 * Created by lenovo on 2017/1/19.
 */
public class StuffSerializer implements Serializer {

    /**
     * If true, the constructor will always be obtained from {@code ReflectionFactory.newConstructorFromSerialization}.
     *
     * Enable this if you intend to avoid deserialize objects whose no-args constructor initializes (unwanted)
     * internal state. This applies to complex/framework objects.
     *
     * If you intend to fill default field values using your default constructor, leave this disabled. This normally
     * applies to java beans/data objects.
     */
    public static final boolean ALWAYS_USE_SUN_REFLECTION_FACTORY = true;


    private static final ConcurrentMap<Class<?>,Schema<?>> schemaCache= new ConcurrentHashMap<>();

    private static final ThreadLocal<LinkedBuffer> bufThreadLocal=new ThreadLocal<LinkedBuffer>(){

        protected LinkedBuffer initalVaule() throws Exception {
            return LinkedBuffer.allocate(DEFAULT_BUF_SIZE);
        }
    };


    @Override
    public <T> T decode(byte[] bytes, Class<T> clazz) {
        return decode(bytes,0,bytes.length,clazz);
    }

    @Override
    public <T> T decode(byte[] bytes, int offset, int length, Class<T> clazz) {
        T msg= null;
        try {
            msg = clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        Schema<T> schema=getSchema(clazz);
        ProtostuffIOUtil.mergeFrom(bytes,offset,length,msg,schema);
        return msg;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> byte[] encode(T obj) {


            Schema<T> schema=getSchema((Class<T>)obj.getClass());
      LinkedBuffer buf=bufThreadLocal.get();

        try {
            // TODO toByteArray里面一坨的 memory copy 需要优化一下
            return ProtostuffIOUtil.toByteArray(obj,schema,buf);
        }finally {
            buf.clear();
        }

    }

    @SuppressWarnings("unchecked")
    private <T> Schema<T> getSchema(Class<T> clazz){
        Schema<T> schema=(Schema<T>) schemaCache.get(clazz);
        if(schema==null){
            Schema<T> newSchema= RuntimeSchema.createFrom(clazz);
            schema=(Schema<T>)schemaCache.putIfAbsent(clazz,newSchema);
            if(schema==null){
                schema=newSchema;
            }

        }
        return schema;
    }



}
