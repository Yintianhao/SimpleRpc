package cn.izzer.simplerpc.proxy;

import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;

/**
 * @author yintianhao
 * @createTime 2020/12/20 1:04
 * @description
 */
public class RpcScanner extends ClassPathBeanDefinitionScanner {

    private RpcFactoryBean<?> rpcFactoryBean = new RpcFactoryBean<>();

    private static final Logger logger = LoggerFactory.getLogger(RpcScanner.class);
    /**
     * 注解类
     * */
    @Setter
    private Class<? extends Annotation> annotationClass;

    public RpcScanner(BeanDefinitionRegistry registry) {
        super(registry);
    }

    @Override
    public Set<BeanDefinitionHolder> doScan(String... packages){
        Set<BeanDefinitionHolder> beanDefineHolders = super.doScan(packages);

        if(beanDefineHolders.isEmpty()){
            logger.warn("No proper Rpc mapper found in such paths : {}",Arrays.asList(packages));
        }else{
            postProcessBeanDefinitions(beanDefineHolders);
        }
        return beanDefineHolders;
    }

    /**
     * 注册过滤器
     * */
    public void registerFilters(){
        boolean acceptAllInterfaces = true;
        //如果事先设置了注解类，那么就只对这个注解类不设置过滤
        if(this.annotationClass!=null){
            addIncludeFilter(new AnnotationTypeFilter(this.annotationClass));
            acceptAllInterfaces = false;
        }
        //没有设置注解类，那么默认将basePackage下的类都进行扫描
        if(acceptAllInterfaces){
            addIncludeFilter(new TypeFilter() {
                @Override
                public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
                    return true;
                }
            });
        }
        addExcludeFilter(new TypeFilter() {
            @Override
            public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
                String className = metadataReader.getClassMetadata().getClassName();
                return className.endsWith("package-info");
            }
        });
    }

    /**
     * 配置自定义BeanDefinition的属性
     */
    private void postProcessBeanDefinitions(Set<BeanDefinitionHolder> holders){
        GenericBeanDefinition definition;
        for (BeanDefinitionHolder holder : holders){
            definition = (GenericBeanDefinition)holder.getBeanDefinition();
            //添加FactoryBean带参构造函数的参数值
            definition.getConstructorArgumentValues().addGenericArgumentValue(definition.getBeanClassName());
            definition.setBeanClass(this.rpcFactoryBean.getClass());
            //设置注入模式
            definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
            logger.info("BeanDefinitionHolder:{}",holder);
        }

    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition definition){
        return definition.getMetadata().isInterface()&&definition.getMetadata().isIndependent();
    }
}
