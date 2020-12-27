package cn.izzer.simplerpc;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.naming.NamingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class ProducerApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ProducerApplication.class);

    @NacosInjected
    private NamingService namingService;

    @Value("${spring.application.name}")
    private String appName;

    @Value("${app.port}")
    private Integer serverPort;

    @Value("${app.ip}")
    private String serverIp;

    public static void main(String[] args) {
        SpringApplication.run(ProducerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        //注册到Nacos
        logger.info(String.format("Register service name:%s,port:%s",appName,serverPort));
        namingService.registerInstance(appName,serverIp,serverPort);
    }
}
