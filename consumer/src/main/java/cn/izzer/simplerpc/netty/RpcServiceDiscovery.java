package cn.izzer.simplerpc.netty;

import cn.izzer.common.entity.ServerAddress;
import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yintianhao
 * @createTime 2020/12/20 15:53
 * @description
 */
@Component
public class RpcServiceDiscovery {


    private static final Logger logger = LoggerFactory.getLogger(RpcServiceDiscovery.class);

    @NacosInjected
    private NamingService namingService;

    @Value("${rpc.provider}")
    private String providerRegisterName;

    @Autowired
    private ConnectionManager connectionManager;

    private volatile List<ServerAddress> serverList = new ArrayList<>();

    @PostConstruct
    public void init(){
        List<Instance> instanceList;
        try {
            instanceList = namingService.getAllInstances(providerRegisterName);
            for (Instance ins : instanceList){
                ServerAddress addr = new ServerAddress();
                addr.setIp(ins.getIp());
                addr.setPort(ins.getPort());
                serverList.add(addr);
            }
            updateConnection();
        } catch (NacosException e) {
            logger.info(String.format("Nacos 获取全部实例异常 %s",e));
        }
    }

    /**
     * 监听服务变化
     * */
    private void watchService(NamingService namingService){
        try {
            namingService.subscribe(providerRegisterName, new EventListener() {
                @Override
                public void onEvent(Event event) {
                    logger.info(String.format("服务发生变化 %s ",event));
                    serverList.clear();
                    //更新服务列表
                    List<Instance> instanceList = ((NamingEvent)event).getInstances();
                    for (Instance ins : instanceList){
                        ServerAddress addr = new ServerAddress();
                        addr.setIp(ins.getIp());
                        addr.setPort(ins.getPort());
                        serverList.add(addr);
                    }
                    updateConnection();
                }
            });
        }catch (NacosException e){
            logger.info(String.format("Nacos 服务监听异常 %s",e));
        }
    }

    private void updateConnection(){
        connectionManager.updateConnection(serverList);
    }

}
