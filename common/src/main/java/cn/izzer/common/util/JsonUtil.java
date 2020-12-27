package cn.izzer.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * @author yintianhao
 * @createTime 2020/12/24 22:16
 * @description Json和对象转换器
 */
public class JsonUtil{

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * 对象转Json字符串
     * */
    public static String parseToJsonStr(Object object) {
        String result = null;
        try {
            result = mapper.writeValueAsString(object);
        }catch (JsonProcessingException e){
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 字节数组转Json字符串
     * */
    public static byte[] parseToJsonBytes(Object object) {
        byte[] result = null;
        try {
            result = mapper.writeValueAsBytes(object);
        }catch (JsonProcessingException e){
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Json字符串转为对象
     * */
    public static <T> T parseToObject(String json,Class<T> clazz){
        T t = null;
        try {
            t = mapper.readValue(json,clazz);
        }catch (JsonProcessingException e){
            e.printStackTrace();
        }
        return t;
    }


    /**
     * 字节数组转为对象
     * */
    public static <T> T parseToObject(byte[] jsonBytes,Class<T> clazz){
        T t = null;
        try {
            t = mapper.readValue(jsonBytes,clazz);
        }catch (JsonProcessingException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        return t;
    }

    /**
     * 获取ObjectMapper
     * */
    public static ObjectMapper getMapper() {
        return mapper;
    }
}
