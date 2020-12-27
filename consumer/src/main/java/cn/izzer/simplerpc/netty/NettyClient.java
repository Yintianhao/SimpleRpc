package cn.izzer.simplerpc.netty;

import cn.izzer.common.entity.RpcRequest;
import cn.izzer.common.entity.RpcResponse;
import cn.izzer.common.enums.RespCodeEnum;
import cn.izzer.simplerpc.json.JsonDecoder;
import cn.izzer.simplerpc.json.JsonEncoder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.net.SocketAddress;
import java.util.concurrent.SynchronousQueue;

/**
 * @author yintianhao
 * @createTime 2020/12/14 23:23
 * @description
 */
@Component
@Slf4j
public class NettyClient {

    private EventLoopGroup eventExecutors = new NioEventLoopGroup();

    private Bootstrap bootstrap = new Bootstrap();

    @Value("${rpc.heartbeat}")
    private int heartBeatTime;

    @Autowired
    private NettyClientHandler handler;

    @Autowired
    private ConnectionManager manager;

    public NettyClient(){
        bootstrap.group(eventExecutors)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY,true)
                .option(ChannelOption.SO_KEEPALIVE,true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast(new IdleStateHandler(0,0,heartBeatTime))
                                .addLast(new JsonEncoder())
                                .addLast(new JsonDecoder())
                                .addLast("handler",handler);
                    }
                });

    }

    @PreDestroy
    public void destroy(){
        log.info(String.format("RPC客户端退出，释放资源"));
        eventExecutors.shutdownGracefully();
    }

    public Object send(RpcRequest request)throws InterruptedException,JsonProcessingException {
        Channel channel = manager.chooseOneVariableChannel();
        ObjectMapper mapper = new ObjectMapper();
        if(channel!=null&&channel.isActive()){
            //通道已经是可用的
            SynchronousQueue<Object> queue = handler.sendRequest(request,channel);
            Object result = queue.take();
            return mapper.writeValueAsString(result);
        }else{
            RpcResponse result = new RpcResponse();
            result.setCode(RespCodeEnum.ERROR);
            result.setErrorMsg("未能连接到服务器");
            return mapper.writeValueAsString(result);
        }
    }

    public Channel connect(SocketAddress address) throws InterruptedException {
        ChannelFuture future = bootstrap.connect(address);
        Channel channel = future.sync().channel();
        return channel;
    }

}
