package cn.izzer.common.entity;

import cn.izzer.common.enums.RequestTypeEnum;
import lombok.Getter;
import lombok.Setter;

/**
 * @author yintianhao
 * @createTime 2020/12/13 14:33
 * @description
 */
public class RpcRequest {
    /**
     * 请求id，唯一，有雪花算法生成
     * */
    @Getter
    @Setter
    private Long id;

    /**
     * 请求类型：正常请求0，心跳：1
     * */
    @Getter
    @Setter
    private RequestTypeEnum type;

    /**
     * 类名称
     * */
    @Getter
    @Setter
    private String className;

    /**
     * 指定运行的方法名称
     * */
    @Getter
    @Setter
    private String methodName;

    /**
     * 参数类型
     * */
    @Getter
    @Setter
    private Class<?>[] paramTypes;

    /**
     * 参数值
     * */
    @Getter
    @Setter
    private Object[] params;

}
