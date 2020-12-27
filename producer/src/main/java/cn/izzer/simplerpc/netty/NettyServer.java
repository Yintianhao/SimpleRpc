package cn.izzer.simplerpc.netty;

import cn.izzer.simplerpc.annonation.SimpleRpc;
import cn.izzer.simplerpc.json.JsonDecoder;
import cn.izzer.simplerpc.json.JsonEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author yintianhao
 * @createTime 2020/12/27 14:35
 * @description
 */
@Component
public class NettyServer implements ApplicationContextAware,InitializingBean {

    /**
     * 负责连接处理
     * */
    private static final EventLoopGroup bossGroup = new NioEventLoopGroup();

    /**
     * 负责读写时间
     * */
    private static final EventLoopGroup workerGroup = new NioEventLoopGroup();


    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    /**
     * 服务器的ip
     * */
    @Value("${app.ip}")
    private String serverIp;

    /**
     * 端口
     * */
    @Value("${app.port}")
    private Integer port;

    /**
     * 心跳时间
     * */
    @Value("${rpc.heartbeat}")
    private Integer heartBeatTime;

    /**
     * 线程池，单个线程，用于异步启动
     * */
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * Key 待@SimpleRpc的Bean的名称
     * Value Bean实例
     * */
    private HashMap<String,Object> serviceDic = new HashMap<>();
    @Override
    public void afterPropertiesSet() throws Exception {
        startNettyServerAsync();
    }

    /**
     * 加载接口
     * */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String,Object> beans =  applicationContext.getBeansWithAnnotation(SimpleRpc.class);
        for (Object bean : beans.values()){
            Class<?> clazz = bean.getClass();
            Class<?>[] interfaces = clazz.getInterfaces();
            for (Class<?> intf : interfaces){
                String interfaceName = intf.getName();
                logger.info("加载接口:{}",interfaceName);
                serviceDic.put(interfaceName,bean);
            }
        }
        logger.info("已加载完全部接口{}");
    }

    /**
     * 异步启动Netty服务器
     * */
    public void startNettyServerAsync(){
        Task task = new Task();
        executor.submit(task);
    }

    /**
     * 启动Netty服务的Task
     * */
    private class Task implements Runnable{

        private final NettyServerHandler handler = new NettyServerHandler(serviceDic);
        //异步启动
        @Override
        public void run() {
            try {
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup,workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .option(ChannelOption.SO_BACKLOG,1024)
                        .childOption(ChannelOption.SO_KEEPALIVE,true)
                        .childOption(ChannelOption.TCP_NODELAY,true)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel channel)throws Exception{
                                ChannelPipeline pipeline = channel.pipeline();
                                pipeline.addLast(new IdleStateHandler(0,0,heartBeatTime));
                                pipeline.addLast(new JsonEncoder());
                                pipeline.addLast(new JsonDecoder());
                                pipeline.addLast(handler);
                            }
                        });
                ChannelFuture future = bootstrap.bind(serverIp,port).sync();
                logger.info(String.format("RPC服务启动:%s:%d",serverIp,port));
                future.channel().closeFuture().sync();
            }catch (Exception e){
                logger.info(String.format("RPC 服务启动异常:%s",e.getMessage()));
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        }
    }

}
