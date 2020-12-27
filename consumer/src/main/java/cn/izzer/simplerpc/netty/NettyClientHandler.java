package cn.izzer.simplerpc.netty;

import cn.izzer.common.entity.RpcRequest;
import cn.izzer.common.entity.RpcResponse;
import cn.izzer.common.enums.RequestTypeEnum;
import cn.izzer.common.util.IDUtil;
import cn.izzer.common.util.JsonUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;

/**
 * @author yintianhao
 * @createTime 2020/12/27 15:39
 * @description
 */
@Component
@ChannelHandler.Sharable
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);

    @Autowired
    private ConnectionManager connectionManager;

    private ConcurrentHashMap<Long,SynchronousQueue<Object>> queueMap = new ConcurrentHashMap<>();

    @Value("${rpc.heartbeat}")
    private int heartBeatTime;

    @Override
    public void channelActive(ChannelHandlerContext ctx){
        logger.info("已连接到RPC服务器:{}",ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx){
        logger.info("与RPC服务器:{}断开连接",ctx.channel().remoteAddress());
        ctx.channel().close();
        connectionManager.removeConnection(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx,Object msg){
        String jsonStr = JsonUtil.parseToJsonStr(msg);
        RpcResponse response = JsonUtil.parseToObject(jsonStr,RpcResponse.class);
        try {
            Long reqId = response.getReqId();
            SynchronousQueue<Object> queue = queueMap.get(reqId);
            queue.put(response);
            queueMap.remove(reqId);
        }catch (InterruptedException e){
            logger.info("SynchronousQueue放入异常");
        }
    }

    /**
     * 发送请求
     * */
    public SynchronousQueue<Object> sendRequest(RpcRequest request, Channel channel){
        SynchronousQueue<Object> queue = new SynchronousQueue<>();
        queueMap.put(request.getId(),queue);
        channel.writeAndFlush(request);
        return queue;
    }
    /**
     * 心跳上报
     * */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx,Object event){

        try {
            if (event instanceof IdleStateEvent){
                IdleStateEvent evt = (IdleStateEvent)event;
                if (evt.state()==IdleState.ALL_IDLE){
                    RpcRequest request = new RpcRequest();
                    request.setId(IDUtil.getRpcRequestId());
                    request.setType(RequestTypeEnum.HEART_BEAT);
                    request.setMethodName("heartBeat");
                    ctx.channel().writeAndFlush(request);
                    logger.info("客户端{} s发送心跳",heartBeatTime);
                }
            }else{
                super.userEventTriggered(ctx,event);
            }
        }catch (Exception e){
            logger.error("心跳异常 %s ",e);
        }
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,Throwable cause){
        logger.info("RPC 服务器异常 %s",cause);
        ctx.channel().close();
    }
}
