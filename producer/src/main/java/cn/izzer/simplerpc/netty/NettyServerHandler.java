package cn.izzer.simplerpc.netty;

import cn.izzer.common.entity.RpcRequest;
import cn.izzer.common.entity.RpcResponse;
import cn.izzer.common.enums.RequestTypeEnum;
import cn.izzer.common.enums.RespCodeEnum;
import cn.izzer.common.util.JsonUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * @author yintianhao
 * @createTime 2020/12/27 13:21
 * @description 服务入站处理
 */
@ChannelHandler.Sharable
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    /**
     * Key 带了@SimpleRpc的Bean名称
     * Value Bean实例
     * */
    private final HashMap<String,Object> serviceDictionary;

    public NettyServerHandler(HashMap<String,Object> serviceDictionary){
        this.serviceDictionary = serviceDictionary;
    }

    /**
     * 有新的Channel连接
     * */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.info(String.format("客户端(%s)连接成功", ctx.channel().remoteAddress()));
    }

    /**
     * 有Channel关闭
     * */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        logger.info(String.format("客户端(%s)断开连接", ctx.channel().remoteAddress()));
    }

    /**
     * 读取通道内的消息
     * */
    @Override
    public void channelRead(ChannelHandlerContext ctx,Object msg){
        String jsonStr = JsonUtil.parseToJsonStr(msg);
        RpcRequest request = JsonUtil.parseToObject(jsonStr,RpcRequest.class);
        if(request.getType()==RequestTypeEnum.HEART_BEAT){
            logger.info("服务端收到来自{}的心跳",ctx.channel().remoteAddress());
        }else{
            logger.info("RPC请求,请求接口:{},请求方法:{}",request.getClassName(),request.getMethodName());
            RpcResponse response = new RpcResponse();
            response.setReqId(request.getId());
            //处理请求
            try {
                Object result = handleRequest(request);
                response.setData(result);
                response.setCode(RespCodeEnum.SUCCESS);
            }catch (Exception e){
                logger.error("RPC处理异常");
                response.setCode(RespCodeEnum.ERROR);
                response.setErrorMsg(e.getMessage());
            }
            ctx.writeAndFlush(response);
        }
    }
    /**
     * 处理请求
     * */
    private Object handleRequest(RpcRequest req) throws Exception{
        String className = req.getClassName();
        Object serverInstance = serviceDictionary.get(className);

        if(serverInstance!=null){
            Class<?> serviceClass = serverInstance.getClass();
            String methodName = req.getMethodName();
            Class<?>[] paramTypes = req.getParamTypes();
            Object[] params = req.getParams();
            //获取方法
            Method method = serviceClass.getMethod(methodName,paramTypes);
            method.setAccessible(true);
            return method.invoke(serverInstance,getParamValues(paramTypes,params));
        }else{
            logger.info("没有找到实例:{},方法名:{}",className,req.getMethodName());
            throw new Exception("没有找到合适的RPC实例");
        }
    }

    /**
     * 获取方法参数的值
     * */
    public Object[] getParamValues(Class<?>[] paramTypes,Object[] params){
        if(params==null||paramTypes==null){
            return params;
        }
        Object[] retValues = new Object[params.length];

        for(int i = 0;i < params.length;i++){
            retValues[i] = JsonUtil.parseToObject(String.valueOf(params[i]),paramTypes[i]);
        }
        return retValues;
    }

    /**
     * 检查心跳
     * */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx,Object event) throws Exception{
        if(event instanceof IdleStateEvent){
            IdleStateEvent idleStateEvent = (IdleStateEvent)event;
            if(idleStateEvent.state()==IdleState.ALL_IDLE){
                logger.info("客户端{}心跳未上报,连接关闭",ctx.channel().remoteAddress());
                ctx.channel().close();
            }
        }else{
            super.userEventTriggered(ctx,event);
        }
    }
    /**
     * 异常捕捉
     * */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,Throwable cause){
        logger.info("异常信息:{}",cause.getMessage());
        ctx.close();
    }
}
