package cn.izzer.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    public static String parseToJson(Object object) {
        String result = null;
        try {
            result = mapper.writeValueAsString(object);
        }catch (JsonProcessingException e){
            e.printStackTrace();
        }
        return result;
    }


    /**
     * Json转为对象
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

}
