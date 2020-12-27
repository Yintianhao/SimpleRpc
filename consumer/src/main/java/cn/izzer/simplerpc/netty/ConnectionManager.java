package cn.izzer.simplerpc.netty;

import cn.izzer.common.entity.ServerAddress;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yintianhao
 * @createTime 2020/12/27 15:54
 * @description
 */
@Component
public class ConnectionManager {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);

    private AtomicInteger channelIndex = new AtomicInteger(0);

    private CopyOnWriteArrayList<Channel> channelList = new CopyOnWriteArrayList<>();

    private Map<SocketChannel,Channel> channelMap = new ConcurrentHashMap<>();

    @Autowired
    private NettyClient client;

    /**
     * 选择一个可用Channel
     * */
    public Channel chooseOneVariableChannel(){
        if(channelList.size()>0){
            int size = channelList.size();
            //轮询算法
            int index = (channelIndex.getAndAdd(1)+size)%size;
            return channelList.get(index);
        }else{
            return null;
        }
    }

    /**
     * 更新连接列表
     * */
    public synchronized void updateConnection(List<ServerAddress> serverList)
    {
        if (serverList==null||serverList.size()==0){
            logger.info("没有可用的服务");
            for (Channel ch : channelList){
                SocketAddress remoteServerAddr = ch.remoteAddress();
                Channel channel = channelMap.get(remoteServerAddr);
                channel.close();
            }
            channelMap.clear();
            channelList.clear();
            return;
        }
        //去重
        HashSet<SocketAddress> serverNodeList = new HashSet<>();
        for(ServerAddress sa : serverList){
            serverNodeList.add(new InetSocketAddress(sa.getIp(),sa.getPort()));
        }

        for (SocketAddress addr : serverNodeList){
            Channel channel = channelMap.get(addr);
            if(channel!=null&&channel.isOpen()){
                logger.info("服务{}已经存在，不需要重新连接",addr);
            }
            //Channel没打开的情况下，重新连接
            connectToServer(addr);
        }
        //移除无效节点
        for (Channel ch : channelList){
            SocketAddress addr = ch.remoteAddress();
            if(!serverNodeList.contains(addr)){
                logger.info("服务{}无效，自动移除",addr);
                Channel channel = channelMap.get(addr);
                if (channel!=null){
                    channel.close();
                }
                channelList.remove(channel);
                channelMap.remove(addr);
            }
        }
    }
    /**
     * 连接到服务器
     * */
    private void connectToServer(SocketAddress addr){
        try {
            Channel channel = client.connect(addr);
            channelList.add(channel);
            logger.info("成功连接到服务器{}",addr);
        }catch (Exception e){
            logger.info("未能连接到服务器{}",addr);
        }
    }
    /**
     * 移除连接
     * */
    public void removeConnection(Channel channel){
        logger.info("Channel:{}已经被移除",channel.remoteAddress());
        SocketAddress address = channel.remoteAddress();
        channelList.remove(channel);
        channelMap.remove(address);
    }
}
