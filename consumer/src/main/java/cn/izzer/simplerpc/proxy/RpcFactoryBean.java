package cn.izzer.simplerpc.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Proxy;

/**
 * @author yintianhao
 * @createTime 2020/12/20 1:33
 * @description
 */
public class RpcFactoryBean<T> implements FactoryBean<T> {

    private Class<T> rpcInterface;

    private static final Logger logger = LoggerFactory.getLogger(RpcFactoryBean.class);

    @Autowired
    private RpcFactory<T> factory;

    public RpcFactoryBean(){
    }

    public RpcFactoryBean(Class<T> rpcInterface){
        this.rpcInterface = rpcInterface;
    }

    /**
     * 返回对象实例
     * */
    @Override
    public T getObject() throws Exception {
        return (T) Proxy.newProxyInstance(rpcInterface.getClassLoader(),new Class[]{rpcInterface},factory);
    }

    /**
     * Bean的类型
     * */
    @Override
    public Class<?> getObjectType() {
        return this.rpcInterface;
    }

    /**
     * 是否是单例的
     * */
    @Override
    public boolean isSingleton(){
        return true;
    }
 }
