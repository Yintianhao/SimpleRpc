package cn.izzer.simplerpc.proxy;

import cn.izzer.common.entity.RpcRequest;
import cn.izzer.common.entity.RpcResponse;
import cn.izzer.common.enums.RequestTypeEnum;
import cn.izzer.common.enums.RespCodeEnum;
import cn.izzer.common.util.IDUtil;
import cn.izzer.common.util.JsonUtil;
import cn.izzer.simplerpc.netty.NettyClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

/**
 * @author yintianhao
 * @createTime 2020/12/13 23:34
 * @description
 */
@Component
public class RpcFactory<T> implements InvocationHandler {

    @Autowired
    private NettyClient client;

    private static final Logger logger = LoggerFactory.getLogger(RpcFactory.class);

    public RpcFactory(){
        logger.info("RpcFactory初始化,{}",this);
    }
    /**
     * 发送请求的地方
     * */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest request = new RpcRequest();
        request.setType(RequestTypeEnum.NORMAL);
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParams(args);
        request.setParamTypes(method.getParameterTypes());
        request.setId(IDUtil.getRpcRequestId());

        Object result = client.send(request);

        Class<?> returnType = method.getReturnType();

        ObjectMapper mapper = new ObjectMapper();

        RpcResponse response = mapper.readValue(String.valueOf(result),RpcResponse.class);

        if(response.getCode()==RespCodeEnum.ERROR){
            throw new Exception(response.getErrorMsg());
        }
        String respData = mapper.writeValueAsString(response.getData());
        if(returnType.isPrimitive()||String.class.isAssignableFrom(returnType)){
            return respData;
        }else if(Collection.class.isAssignableFrom(returnType)){
            CollectionType collectionType = mapper.getTypeFactory().constructCollectionType(Collection.class,Object.class);
            return mapper.readValue(respData,collectionType);
        }else if (Map.class.isAssignableFrom(returnType)){
            MapType mapType = mapper.getTypeFactory().constructMapType(Map.class,Object.class,Object.class);
            return mapper.readValue(respData,mapType);
        }else{
            Object data = response.getData();
            return mapper.readValue(respData,returnType);
        }
    }
}
