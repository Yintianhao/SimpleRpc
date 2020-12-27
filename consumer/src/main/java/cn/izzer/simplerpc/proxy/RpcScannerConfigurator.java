package cn.izzer.simplerpc.proxy;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author yintianhao
 * @createTime 2020/12/20 14:05
 * @description
 */
@Component
public class RpcScannerConfigurator implements BeanDefinitionRegistryPostProcessor {

    String scanPackage = "cn.izzer.simplerpc.service";

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        RpcScanner scanner = new RpcScanner(beanDefinitionRegistry);
        scanner.setAnnotationClass(null);
        scanner.registerFilters();
        //扫描
        scanner.scan(StringUtils.tokenizeToStringArray(this.scanPackage,ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

    }
}
